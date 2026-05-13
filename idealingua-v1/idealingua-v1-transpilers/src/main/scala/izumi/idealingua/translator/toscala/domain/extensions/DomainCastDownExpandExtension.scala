package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.{Builtin, SigParam, SigParamSource}
import izumi.idealingua.translator.toscala.domain.{DomainSTContext, DomainScalaParseBack}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta.*

/** PR-02 IMPL-7a.2 Phase B M5: new-IR port of `CastDownExpandExtension`.
  *
  * For an interface `I` with implementing DTOs `D1, D2, ...`, emits an
  * `implicit object I_downcast_extend_Dn` in `I`'s companion. Each one
  * exposes a `using(...)` factory that, given an `I` value plus the
  * additional fields not present on `I`, constructs a `Dn`.
  *
  * Inlined port of `StructuralQueriesImpl.conversions` (legacy
  * `StructuralQueriesImpl.scala:195-260`) at the relaxed-parity layer:
  *
  *   - `implementors` = `Domain.implementingDtos.getOrElse(I, Set.empty).toList.sortBy(_.toString)`
  *     (deterministic order required for byte-stable downstream output).
  *   - For each implementor `D`, the "extra" fields are `flat(D) \ flat(I)`.
  *     Mirror is the DTO id itself (no synthetic mirror for the impl
  *     constructor — the new-IR Composite renderer already exposes the
  *     final case class directly).
  *
  * The simplified converter signature here covers the common case: extra
  * fields become positional `using(...)` parameters; inherited fields are
  * pulled off `_value`. This matches legacy's `parentInstanceFields` /
  * `localFields` split for the non-mixin case.
  */
object DomainCastDownExpandExtension {

  def constructorsForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): List[String] =
    constructorsForInterfaceInternal(ctx, i).map(DomainScalaParseBack.renderS30(_))

  private def constructorsForInterfaceInternal(ctx: DomainSTContext, i: NewTypeDef.Interface): List[Stat] = {
    val ifaceFlat = ctx.domain.flattenedStructs.get(i.id).map(_.fields.map(_.field).toSet).getOrElse(Set.empty)
    val implementors = ctx.domain.implementingDtos.getOrElse(i.id, Set.empty).toList.sortBy(_.toString)

    implementors.flatMap { dtoId =>
      ctx.domain.flattenedStructs.get(dtoId).map { dtoFlat =>
        // Apply the legacy sort key
        // `(distance, definedBy.toString, -definedWithIndex).reverse`
        // via `DomainScalaStruct.fromFlat` so the synthesized
        // `Cast.using(...)` factory orders assignments to match legacy
        // `StructuralQueriesImpl.structure`. Plain `sortBy(_.distance)` is
        // insufficient — when two fields share a distance (e.g. two
        // separate ancestor interfaces both at d=2), the legacy emits in
        // sorted-by-definedBy order, which `_.distance`-only sort does
        // not produce.  Defect #7 dedup is folded into `fromFlat`.
        val supers = ctx.domain.userTypes.get(dtoId) match {
          case Some(d: NewTypeDef.Dto)       => d.struct.superclasses
          case Some(i: NewTypeDef.Interface) => i.struct.superclasses
          case _                              => izumi.idealingua.model.il.ast.typed.Super.empty
        }
        val sortedDto = izumi.idealingua.translator.toscala.domain.DomainScalaStruct.fromFlat(dtoId, dtoFlat, supers, ctx.domain)
        val dtoFields = sortedDto.all.map(_.field)
        // IMPL-7a.2-Fi2 (F-covariant-field-type-narrowing): match by FULL Field
        // value (name + typeId), not by name alone. When the implementor
        // covariantly overrides a parent field with a narrower type, the
        // child's Field differs from the parent's Field — it must fall into
        // `localFields` so the `using(...)` factory exposes a parameter typed
        // with the narrower type (e.g. `field: CovariantA` for an interface
        // whose abstract member is `field: Covariant`). Legacy
        // `StructuralQueriesImpl.converters` (lines 197-214) does the same
        // intersect over the full Field set.
        val parentFields = dtoFields.filter(f => ifaceFlat.contains(f))
        val localFields  = dtoFields.filterNot(f => ifaceFlat.contains(f))

        val thisType   = ctx.conv.toScala(i.id)
        val targetType = ctx.conv.toScala(dtoId)

        // Build sig params: locals only as using(...) params; parents pulled off _value.
        val localSigs = localFields.map { f =>
          SigParam(f.name, SigParamSource(f.typeId, f.name), None)
        }
        val parentSigs = parentFields.map { f =>
          SigParam(f.name, SigParamSource(i.id, "_value"), Some(f.name))
        }

        // Convert local sigs into Term.Params; convert parent sigs into Term.Assign.
        val usingParams: List[Term.Param] = localSigs.map { sp =>
          val pType = ctx.conv.toScala(sp.source.sourceType).typeFull
          Term.Param(List.empty, Term.Name(sp.source.sourceName), Some(pType), None)
        }

        val assignments: List[Term.Assign] = (parentSigs ++ localSigs).map(toAssignment)

        val assertions: List[Term] = localSigs.flatMap { sp =>
          if (!sp.source.sourceType.isInstanceOf[Builtin]) {
            List(q"${Term.Name(sp.source.sourceName)}.asInstanceOf[_root_.scala.AnyRef] ne null")
          } else List.empty
        }
        val assertBlock: List[Term] = if (assertions.isEmpty) List.empty
        else {
          val combined = assertions.tail.foldLeft(assertions.head: Term) { case (acc, a) => q"$acc && $a" }
          List(q"assert($combined)")
        }

        val name = Term.Name(s"${thisType.termName.value}_downcast_extend_${dtoId.uniqueDomainName}")

        q"""
           implicit object $name extends ${ctx.rt.Extend.parameterize(List(thisType.typeFull, targetType.typeFull)).init()} {
             class Call(private val _value: ${thisType.typeFull}) extends AnyVal {
                def using(..$usingParams): ${targetType.typeFull} = {
                  assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
                  ..$assertBlock
                  ${targetType.termFull}(..$assignments)
                }
             }

             override type INSTANTIATOR = Call

             override def next(_value: ${thisType.typeFull}): Call = new Call(_value)
           }
         """
      }
    }
  }

  private def toAssignment(f: SigParam): Term.Assign = {
    f.sourceFieldName match {
      case Some(srcFieldName) =>
        q""" ${Term.Name(f.targetFieldName)} = ${Term.Name(f.source.sourceName)}.${Term.Name(srcFieldName)} """
      case None =>
        q""" ${Term.Name(f.targetFieldName)} = ${Term.Name(f.source.sourceName)} """
    }
  }

}
