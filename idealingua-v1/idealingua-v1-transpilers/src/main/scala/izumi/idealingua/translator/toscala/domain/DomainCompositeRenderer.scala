package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.il.ast.typed.Interfaces
import izumi.idealingua.translator.toscala.products.{CogenProduct, RenderableCogenProduct}
import izumi.idealingua.translator.toscala.types.ClassSource
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta._

/** Renders a new-IR `TypeDef.Dto` (or interface impl synthesis) as the same
  * scala.meta `Defn`s the legacy `CompositeRenderer.defns` produces (modulo
  * the extension chain).
  *
  * IMPL-7a.2 Phase B M3 (relaxed parity): produces structurally correct
  * Scala — final case class with all fields, companion object with
  * constructors, tools implicit class. The optional "mirror interface" the
  * legacy emits when `source != CsInterface` is also synthesized via the
  * `defnId(dto)` impl shape.
  *
  * Inputs come off `Domain.flattenedStructs(dto.id)` for the field list and
  * `TypeDef.Dto.struct.superclasses` for the interface bases. Inheritance
  * computations from `Typespace.inheritance` (e.g. `parentsInherited`) are
  * NOT reused — the new IR's `Domain.parents` is consulted instead, and the
  * "mirror" interface synthesis falls back to the declared `superclasses`
  * for parity with legacy.
  */
final class DomainCompositeRenderer(ctx: DomainSTContext) {
  import ctx.conv._

  /** Render a structural DTO (`ClassSource.CsDTO`) or an interface's impl
    * DTO (`ClassSource.CsInterface`).
    */
  def defns(struct: DomainCompositeStructure, source: ClassSource): RenderableCogenProduct = {
    val withMirror = source match {
      case _: ClassSource.CsInterface => false
      case _                          => true
    }

    val bases: List[Init] = source match {
      // legacy emits an empty base list for method input/output, plus the
      // CsDTO arm. M3 mirrors that exactly — bases stay empty.
      case _: ClassSource.CsMethodInput  => List.empty
      case _: ClassSource.CsMethodOutput => List.empty
      case _                             => List.empty
    }

    val (mirrorInterface: List[Defn.Trait], moreBases: Interfaces) = if (withMirror) {
      // Synthesize the mirror trait `<Dto>Defn` matching legacy
      // `typespace.tools.defnId(dto)`. For interface-impl DTOs the legacy
      // arm skips this branch (handled by CsInterface above).
      struct.fields.id match {
        case dto: izumi.idealingua.model.common.TypeId.DTOId =>
          val implIfaceId = izumi.idealingua.model.common.TypeId.InterfaceId(dto, "Defn")
          val mirrorTrait =
            ctx.interfaceRenderer.mkTrait(
              List.empty,
              ctx.conv.toScala(implIfaceId),
              struct.fields,
            )
          (List(mirrorTrait), List(implIfaceId))
        case _ =>
          (List.empty, List.empty)
      }
    } else {
      (List.empty, List.empty)
    }

    val ifDecls = (struct.composite ++ moreBases).map { iface =>
      ctx.conv.toScala(iface).init()
    }

    val superClasses = bases ++ ifDecls

    val tools = struct.t.within(s"${struct.fields.id.name.capitalize}Extensions")

    val qqComposite = q"""final case class ${struct.t.typeName}(..${struct.decls}) extends ..$superClasses {}"""

    val toolBases = List(ctx.rt.Conversions.parameterize(List(struct.t.typeFull)).init())

    val qqTools = q""" implicit class ${tools.typeName}(override protected val _value: ${struct.t.typeFull}) extends ..$toolBases { }"""

    val qqCompositeCompanion =
      q"""object ${struct.t.termName} {
            ..$mirrorInterface

            ..${struct.constructors}
          }"""

    CogenProduct(qqComposite, qqCompositeCompanion, qqTools, List.empty)
  }

  /** Render a top-level user-declared DTO directly (convenience).  */
  def renderDto(dto: NewTypeDef.Dto): RenderableCogenProduct = {
    val flat   = ctx.domain.flattenedStructs.getOrElse(
      dto.id,
      izumi.idealingua.typer.ir.FlatStruct(dto.id, List.empty, List.empty, List.empty),
    )
    val sstruct  = DomainScalaStruct.scalaStruct(dto.id, flat, dto.struct.superclasses, ctx.conv, ctx.domain)
    val composite = new DomainCompositeStructure(ctx, sstruct)
    defns(composite, ClassSource.CsDTO(LegacyDtoStub(dto)))
  }

  /** A minimal legacy `TypeDef.DTO`-shaped value used to satisfy
    * `ClassSource.CsDTO`. The composite renderer never reads any field
    * beyond the type bound, so a stub built from new-IR data is sufficient.
    *
    * (M3 risk: if downstream extensions read the inner DTO definition off
    * `ClassSource`, this stub may surface a defect — flagged as a possible
    * F-followup. For the pre-extension structural output the stub is inert.)
    */
  private def LegacyDtoStub(dto: NewTypeDef.Dto): izumi.idealingua.model.il.ast.typed.TypeDef.DTO = {
    izumi.idealingua.model.il.ast.typed.TypeDef.DTO(
      id     = dto.id,
      struct = izumi.idealingua.model.il.ast.typed.Structure(
        fields         = dto.struct.fields,
        removedFields  = dto.struct.removedFields,
        superclasses   = dto.struct.superclasses,
      ),
      meta   = dto.meta,
    )
  }
}
