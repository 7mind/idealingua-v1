package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.il.ast.typed.{Interfaces, TypeDef => LegacyTypeDef}
import izumi.idealingua.translator.toscala.domain.extensions.{
  DomainAnyvalExtension,
  DomainCastSimilarExtension,
  DomainCastUpExtension,
  DomainCirceDerivationTranslatorExtension,
}
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

    val implRaw = ctx.compositeRenderer.defns(implStructure, ClassSource.CsInterface(ifaceStub))

    // Defect #2 (Fd): the mirror Struct emitted INSIDE the interface
    // companion is the moral equivalent of the legacy "impl DTO routed
    // through CompositeRenderer + extension chain". Legacy emits both a
    // Circe trait (`StructCirce extends IRTTimeInstances`) and a
    // `Struct_upcast_<Self|Iface>` cast set on the inner companion. New IR
    // never ran the extension chain on the impl; reproduce locally:
    //   - StructCirce trait → sibling Defn inside the interface companion.
    //   - Init prepended to the inner `object Struct` companion's bases.
    //   - `Struct_upcast_*` cast set appended to the inner `object Struct`
    //     companion stats.
    val scalaVersions = ctx.options.manifest.sbt.scalaVersions
    val structCirce = DomainCirceDerivationTranslatorExtension.emitForImplStruct(ctx, implId, implFlat, scalaVersions)
    val structCirceInit = ctx.conv.toScala(implId).sibling(structCirce.name).init()
    // Legacy `defaultExtensions` order: CastSimilar before CastUp. Emit
    // `Struct_cast_into_<peer>` BEFORE `Struct_upcast_*` so the inner
    // companion stat order matches legacy.
    val structSims    = DomainCastSimilarExtension.mkConvertersForImplStruct(ctx, implId)
    val structUpcasts = DomainCastUpExtension.generateUpcastsForImplStruct(ctx, i.id, implId, implFlat)

    // AnyVal-eligibility for the impl DTO: legacy `AnyvalExtension.handleComposite`
    // ran on the synthesized impl, prepending `AnyVal` to the impl case class
    // when its single field qualifies. Replicate by computing the predicate
    // off the synthetic flat (impl ID is not in `Domain.flattenedStructs`).
    val implAnyvalBases: List[Init] = {
      val all = implFlat.fields.map(_.field)
      val canBeAnyVal = all.size == 1 && all.forall(f => structFieldQualifiesForAnyVal(f.typeId))
      if (canBeAnyVal) List(ctx.conv.toScala(izumi.idealingua.model.JavaType(Seq.empty, "AnyVal")).init())
      else List.empty
    }

    // The impl render returns `CogenProduct[Defn.Class]` — augment the
    // companion (`Defn.Object`) with the StructCirce init prepend + the
    // upcasts appended, and inject the StructCirce trait alongside the
    // emitted impl Defns inside the interface companion. Order INSIDE the
    // interface companion is `[case class Struct, StructCirce trait,
    // object Struct]` per legacy emit order (the case class is the renderer
    // output; the extension-chain Defns from `handleComposite` follow).
    val implAugmentedDefns: List[Defn] = implRaw match {
      case cp: CogenProduct[_] =>
        val typedCp = cp.asInstanceOf[CogenProduct[Defn.Class]]
        val classWithAnyVal = typedCp.defn.prependBase(implAnyvalBases)
        val newCompanionBase = typedCp.companionBase.prependBase(structCirceInit).appendDefinitions(structSims ++ structUpcasts)
        val newProduct = CogenProduct[Defn.Class](
          defn          = classWithAnyVal,
          companionBase = newCompanionBase,
          tools         = typedCp.tools,
          more          = typedCp.more,
          preamble      = typedCp.preamble,
        )
        val rendered = newProduct.render
        // Splice the StructCirce trait between the case class and the
        // companion: legacy emit is `[case class Struct, trait StructCirce,
        // object Struct extends StructCirce]`.
        rendered match {
          case caseClass :: rest => caseClass :: structCirce.defn :: rest
          case Nil               => List(structCirce.defn)
        }
      case other =>
        structCirce.defn :: other.render
    }

    val qqInterfaceCompanion =
      q"""object ${t.termName} {
             def apply(..${implStructure.decls}) = ${ctx.conv.toScala(implId).termName}(..${implStructure.names})
             ..$implAugmentedDefns
         }"""

    val toolBases = List(ctx.rt.Conversions.parameterize(List(t.typeFull)).init())

    val tools   = t.within(s"${i.id.name}Extensions")
    val qqTools = q"""implicit class ${tools.typeName}(override protected val _value: ${t.typeFull}) extends ..$toolBases { }"""

    CogenProduct(qqInterface, qqInterfaceCompanion, qqTools, List.empty)
  }

  /** AnyVal-field check mirroring `DomainAnyvalExtension.canBeAnyValField`,
    * inlined because that helper is package-private. */
  private def structFieldQualifiesForAnyVal(typeId: izumi.idealingua.model.common.TypeId): Boolean = typeId match {
    case _: izumi.idealingua.model.common.Generic                          => false
    case _: izumi.idealingua.model.common.Builtin                          => true
    case _: izumi.idealingua.model.common.TypeId.EnumId                    => true
    case _: izumi.idealingua.model.common.TypeId.AdtId                     => false
    case a: izumi.idealingua.model.common.TypeId.AliasId =>
      ctx.domain.aliases.get(a) match {
        case Some(target) => structFieldQualifiesForAnyVal(target)
        case None         => false
      }
    case d: izumi.idealingua.model.common.TypeId.DTOId =>
      ctx.domain.flattenedStructs.get(d).exists(_.fields.size > 1)
    case i: izumi.idealingua.model.common.TypeId.InterfaceId =>
      ctx.domain.flattenedStructs.get(i).exists(_.fields.size > 1)
    case t: izumi.idealingua.model.common.TypeId.IdentifierId =>
      ctx.domain.userTypes.get(t) match {
        case Some(izumi.idealingua.typer.ir.TypeDef.Identifier(_, fields, _)) => fields.size > 1
        case _                                                                 => false
      }
    case _ => false
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
