package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.translator.toscala.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.TypeDef

import scala.meta.*

/** Renders a new-IR `TypeDef.Enum` as the same `EnumProduct` (pre-extension)
  * the legacy `EnumRenderer.renderEnumeration` produces before the
  * extension chain runs.
  *
  * IMPL-7a.2 Phase B M1 scope: enum body structure only. The legacy
  * `EnumRenderer` invokes `ctx.ext.extend(i, EnumProduct(...), _.handleEnum)`
  * after constructing the pre-extension product; that extension chain
  * (`CirceDerivationTranslatorExtension.handleEnum` is the only handler
  * that touches enums in the default extension set) requires a legacy
  * `STContext` and a legacy `Enumeration` as input, neither of which the
  * Domain-consuming path holds. M2 will reintegrate the extension chain
  * by adapting new IR back to legacy IR at the extension boundary.
  *
  * For M1, byte parity is asserted in `DomainEnumRendererSpec` with
  * `extensions = Seq.empty` so the legacy and new paths produce the same
  * pre-extension output.
  *
  * `TypeDef.Enum` carries `id: EnumId`, `members: List[EnumMember]`,
  * `meta: NodeMeta` — and `EnumMember` is the legacy `EnumMember` type
  * (see `idealingua-v1-model/.../typed/TypeDef.scala:12`), so the body
  * construction is field-for-field identical to legacy.
  */
final class DomainEnumRenderer(ctx: DomainSTContext) {

  import ctx._
  import conv._

  def renderEnumeration(i: TypeDef.Enum): EnumProduct = {
    val t = conv.toScala(i.id)

    val members = i.members.map {
      m =>
        val mt = t.within(m.value)
        val element =
          q"""case object ${mt.termName} extends ${t.init()} {
              override def toString: String = ${Lit.String(m.value)}
            }"""

        mt.termName -> element
    }

    val parseMembers = members.map {
      case (termName, _) =>
        val termString = termName.value
        p"""case ${Lit.String(termString)} => $termName"""
    }

    val qqEnum = q""" sealed trait ${t.typeName} extends ${rt.enumEl.init()} {} """
    val qqEnumCompanion =
      q"""object ${t.termName} extends ${rt.idlEnum.init()} {
            type Element = ${t.typeFull}

            override def all: Seq[${t.typeFull}] = Seq(..${members.map(_._1)})

            override def parse(value: String): ${t.typeName} = value match {
              ..case $parseMembers
            }
           }"""

    EnumProduct(qqEnum, qqEnumCompanion, members)
  }
}
