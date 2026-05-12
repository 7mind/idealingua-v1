package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.il.ast.typed.{Interfaces, TypeDef => LegacyTypeDef}
import izumi.idealingua.translator.toscala.domain.extensions.DomainAnyvalExtension
import izumi.idealingua.translator.toscala.products.CogenProduct.TraitProduct
import izumi.idealingua.translator.toscala.products.{CogenProduct, RenderableCogenProduct}
import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
import izumi.idealingua.translator.toscala.types.{ClassSource, ScalaStruct, ScalaType}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta._

/** Renders a new-IR `TypeDef.Interface` as the same scala.meta `Defn`s the
  * legacy `InterfaceRenderer.renderInterface` produces (modulo the
  * extension chain).
  *
  * IMPL-7a.2 Phase B M3 (relaxed parity): produces structurally correct
  * Scala — sealed trait extending parent interfaces plus
  * `IDLGeneratedType`, companion object with `apply(...)` factory + nested
  * impl DTO (the `Struct` synthetic), tools implicit class.
  *
  * Inputs: `TypeDef.Interface.struct.superclasses.interfaces` for the
  * extends clause; `Domain.flattenedStructs(i.id)` for the field set on
  * both the trait abstract methods and the nested impl DTO.
  */
final class DomainInterfaceRenderer(ctx: DomainSTContext) {
  import ctx.conv._

  def renderInterface(i: NewTypeDef.Interface): RenderableCogenProduct = {
    val flat   = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      izumi.idealingua.typer.ir.FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val fields = DomainScalaStruct.scalaStruct(i.id, flat, i.struct.superclasses, ctx.conv, ctx.domain)
    val t      = ctx.conv.toScala(i.id)

    val qqInterface = mkTrait(i.struct.superclasses.interfaces, t, fields)

    val implId       = DomainScalaStruct.implId(i.id)
    val implFlat     = DomainScalaStruct.implFlatStruct(implId, flat)
    val implFields   = DomainScalaStruct.scalaStruct(implId, implFlat, izumi.idealingua.model.il.ast.typed.Super.empty.copy(interfaces = List(i.id)), ctx.conv, ctx.domain)
    val implStructure = new DomainCompositeStructure(ctx, implFields)

    // Construct a synthetic `TypeDef.Interface` legacy stub for the
    // ClassSource.CsInterface marker. CompositeRenderer reads only the type
    // (not the inner struct) at the pre-extension layer.
    val ifaceStub = LegacyTypeDef.Interface(
      id     = i.id,
      struct = izumi.idealingua.model.il.ast.typed.Structure(
        fields        = i.struct.fields,
        removedFields = i.struct.removedFields,
        superclasses  = i.struct.superclasses,
      ),
      meta   = i.meta,
    )

    val impl = ctx.compositeRenderer.defns(implStructure, ClassSource.CsInterface(ifaceStub)).render

    val qqInterfaceCompanion =
      q"""object ${t.termName} {
             def apply(..${implStructure.decls}) = ${ctx.conv.toScala(implId).termName}(..${implStructure.names})
             ..$impl
         }"""

    val toolBases = List(ctx.rt.Conversions.parameterize(List(t.typeFull)).init())

    val tools   = t.within(s"${i.id.name}Extensions")
    val qqTools = q"""implicit class ${tools.typeName}(override protected val _value: ${t.typeFull}) extends ..$toolBases { }"""

    CogenProduct(qqInterface, qqInterfaceCompanion, qqTools, List.empty)
  }

  /** Build the trait `Defn.Trait` for an interface — exposed so the
    * composite renderer's mirror-interface synthesis can call it.
    */
  def mkTrait(supers: Interfaces, t: ScalaType, fields: ScalaStruct): Defn.Trait = {
    val decls = fields.all.map { f =>
      Decl.Def(List.empty, f.name, List.empty, f.fieldType)
    }

    val ifDecls = (ctx.rt.generated +: supers.map(ctx.conv.toScala)).map(_.init())

    val qqInterface =
      q"""trait ${t.typeName} extends ..$ifDecls {
            ..$decls
          }
       """

    // Legacy parity: AnyvalExtension.handleTrait runs on EVERY trait built
    // through mkTrait — top-level interfaces *and* the mirror `Defn` trait
    // synthesised inside DTO companions. Mirror the prepend here so both
    // call sites pick it up uniformly.
    TraitProduct(qqInterface).defn.prependBase(DomainAnyvalExtension.withAnyForStruct(ctx, fields))
  }
}
