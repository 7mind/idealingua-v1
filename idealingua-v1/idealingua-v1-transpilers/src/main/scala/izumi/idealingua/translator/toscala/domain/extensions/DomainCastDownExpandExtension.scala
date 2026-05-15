package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.{Builtin, SigParam, SigParamSource}
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.tools.ScalaTextHelpers
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

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
  *
  * F-TextTree M8c..M8f: ported off legacy quasiquotes — bodies composed
  * as plain Scala source strings. The produced strings are consumed via
  * `companionCasts` (parse-back at carrier render time).
  */
object DomainCastDownExpandExtension {

  def constructorsForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): List[String] = {
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

        val thisScala   = ctx.conv.toScala(i.id)
        val targetScala = ctx.conv.toScala(dtoId)

        // Build sig params: locals only as using(...) params; parents pulled off _value.
        val localSigs = localFields.map { f =>
          SigParam(f.name, SigParamSource(f.typeId, f.name), None)
        }
        val parentSigs = parentFields.map { f =>
          SigParam(f.name, SigParamSource(i.id, "_value"), Some(f.name))
        }

        // Convert local sigs into Scala param decls (`name: Type`).
        val usingParams: List[String] = localSigs.map { sp =>
          val pType = ctx.conv.toScala(sp.source.sourceType).typeFull.toString
          val pNm   = ScalaTextHelpers.escapeIdent(sp.source.sourceName)
          s"$pNm: $pType"
        }

        val assignments: List[String] = (parentSigs ++ localSigs).map(toAssignment)

        // Generate per-local-non-builtin null-check terms, then combine via &&
        // exactly as the legacy `&&`-infix-applied chain did.
        val assertions: List[String] = localSigs.flatMap { sp =>
          if (!sp.source.sourceType.isInstanceOf[Builtin]) {
            val nm = ScalaTextHelpers.escapeIdent(sp.source.sourceName)
            List(s"$nm.asInstanceOf[_root_.scala.AnyRef] ne null")
          } else List.empty
        }
        // Legacy infix `&&` over infix `ne` chain
        // surfaces precedence-disambiguating parens around each `ne null`
        // operand at print time. Replicate by wrapping each operand in parens
        // before the `&&`-join (skipping the parens-only case where there is
        // exactly one assertion has no `&&`, so the parens are unnecessary —
        // but the legacy printer keeps the operand parens regardless of
        // arity for single-assertion `assert(...)`. Test:
        // scala/izumi/test/domain02/TestInterface2.scala → `assert((a) && (b))`).
        val assertLine: String = assertions match {
          case Nil           => ""
          case single :: Nil => s"\n      assert($single)"
          case xs            => s"\n      assert(${xs.map(a => s"($a)").mkString(" && ")})"
        }

        val name = s"${thisScala.termName.value}_downcast_extend_${dtoId.uniqueDomainName}"

        val extendBase  = ctx.rt.Extend.parameterize(List(thisScala.typeFull, targetScala.typeFull)).typeFull.toString
        val usingParamsStr = usingParams.mkString(", ")
        val assignmentsStr = assignments.mkString(", ")
        val thisTypeFull   = thisScala.typeFull.toString
        val targetTypeFull = targetScala.typeFull.toString
        val targetTermFull = targetScala.termFull.toString

        s"""implicit object $name extends $extendBase {
           |  class Call(private val _value: $thisTypeFull) extends AnyVal {
           |    def using($usingParamsStr): $targetTypeFull = {
           |      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)$assertLine
           |      $targetTermFull($assignmentsStr)
           |    }
           |  }
           |
           |  override type INSTANTIATOR = Call
           |
           |  override def next(_value: $thisTypeFull): Call = new Call(_value)
           |}""".stripMargin
      }
    }
  }

  private def toAssignment(f: SigParam): String = {
    val tgt = ScalaTextHelpers.escapeIdent(f.targetFieldName)
    val src = ScalaTextHelpers.escapeIdent(f.source.sourceName)
    f.sourceFieldName match {
      case Some(srcFieldName) =>
        val sFn = ScalaTextHelpers.escapeIdent(srcFieldName)
        s"$tgt = $src.$sFn"
      case None =>
        s"$tgt = $src"
    }
  }

}
