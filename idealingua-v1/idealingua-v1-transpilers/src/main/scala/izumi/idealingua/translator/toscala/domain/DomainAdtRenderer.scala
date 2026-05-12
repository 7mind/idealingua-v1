package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.translator.toscala.products.CogenProduct.{AdtElementProduct, AdtProduct}
import izumi.idealingua.translator.toscala.products.RenderableCogenProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta._

/** Renders a new-IR `TypeDef.Adt` as the same scala.meta `Defn`s the legacy
  * `AdtRenderer.renderAdt` produces (modulo the extension chain).
  *
  * IMPL-7a.2 Phase B M4 (relaxed parity): produces structurally correct
  * Scala — sealed trait + companion + per-branch `final case class
  * Branch(value: Target) extends Adt`, plus implicit `into<Branch>` /
  * `from<Branch>` converters. Inputs come directly off `TypeDef.Adt.alternatives`
  * (a `List[AdtMember]` shared with the legacy AST).
  *
  * Per F5c T1 (commit `1eb4377`), the new-typer `EphemeralSynthesizer`
  * auto-wraps non-structural alternative-branch targets in a synthetic DTO,
  * so every `AdtMember.typeId` reaching this renderer is already a
  * `StructureId` (DTO / Interface / Identifier) or another `AdtId`. No
  * additional lifting is required here.
  */
final class DomainAdtRenderer(ctx: DomainSTContext) {
  import ctx.conv._

  def renderAdt(i: NewTypeDef.Adt, bases: List[Init] = List.empty): RenderableCogenProduct = {
    val t = ctx.conv.toScala(i.id)

    val members = i.alternatives.map { m =>
      val memberName = m.typename
      val mt         = t.within(memberName)
      val original   = ctx.conv.toScala(m.typeId)

      val qqElement   = q"""final case class ${mt.typeName}(value: ${original.typeAbsolute}) extends ..${List(t.init())}"""
      val qqCompanion = q""" object ${mt.termName} {} """

      val converters = List(
        q"""implicit def ${Term.Name("into" + memberName)}(value: ${original.typeAbsolute}): ${t.typeFull} = ${mt.termFull}(value) """,
        q"""implicit def ${Term.Name("from" + memberName)}(value: ${mt.typeFull}): ${original.typeAbsolute} = value.value""",
      )

      AdtElementProduct(memberName, qqElement, qqCompanion, converters)
    }

    val superClasses = List(ctx.rt.adtEl.init()) ++ bases ++ List(ctx.conv.toScala[Product].init())
    val qqAdt        = q""" sealed trait ${t.typeName} extends ..$superClasses {} """
    val qqAdtCompanion =
      q"""object ${t.termName} extends ${ctx.rt.adt.init()} {
            import _root_.scala.language.implicitConversions

            type Element = ${t.typeFull}

           }"""

    AdtProduct(qqAdt, qqAdtCompanion, members)
  }
}
