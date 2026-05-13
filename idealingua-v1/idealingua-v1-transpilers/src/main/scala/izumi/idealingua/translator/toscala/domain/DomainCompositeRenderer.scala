package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.il.ast.typed.Interfaces
import izumi.idealingua.translator.toscala.products.{CogenProduct, RenderableCogenProduct}
import izumi.idealingua.translator.toscala.types.ClassSource
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}


/** Renders a new-IR `TypeDef.Dto` (or interface impl synthesis) as the same
  * scala.meta `Defn`s the legacy `CompositeRenderer.defns` produces (modulo
  * the extension chain).
  *
  * F-TextTree M6: ported off `scala.meta` quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. Type references travel as
  * `ScalaRefHandle.{TypeName, TypeFull}` value nodes. The supporting
  * scaffolding (`DomainScalaStruct.scalaStruct` → `ScalaStruct.all`,
  * `DomainCompositeStructure.{decls, constructors}`) still produces
  * `scala.meta` fragments (`Term.Param` / `Defn.Def`); we splice them as
  * pre-rendered `.syntax` strings rather than thread `TextTree` through
  * the scaffolding (out of M6 scope; the scaffolding's downstream
  * consumers — extensions — still operate on `Defn`).
  *
  * **Carrier strategy** (same as M5): `CogenProduct[Defn.Class]` carrier
  * preserved; extensions (Circe sibling + base, AnyVal `prependBase`,
  * Cast* `appendDefinitions`) continue to mutate it. The renderer
  * composes textual output via `TextTree`, lowers to `String`, then
  * re-parses to `Defn.Class` / `Defn.Object` via `DomainScalaParseBack`
  * at the boundary.
  *
  * **Byte parity**: the empty-body pitfall (M5) applies — the legacy
  * `q"final case class … extends … {}".syntax` printer drops empty `{}`
  * but the parser keeps source-level braces. The case class and the
  * tools implicit class both have no body in this renderer; emit them
  * without trailing `{}` so the parsed `Defn.{Class}` re-prints
  * byte-equal to the legacy output. The companion object has body stats
  * (constructors + optional mirror trait) so its braces are preserved
  * by both paths.
  *
  * IMPL-7a.2 Phase B parity: produces structurally correct Scala — final
  * case class with all fields, companion object with constructors, tools
  * implicit class. The optional "mirror interface" the legacy emits when
  * `source != CsInterface` is also synthesized via the `defnId(dto)`
  * impl shape.
  *
  * Inputs come off `Domain.flattenedStructs(dto.id)` for the field list
  * and `TypeDef.Dto.struct.superclasses` for the interface bases.
  */
final class DomainCompositeRenderer(ctx: DomainSTContext) {

  private val resolver = new DomainScalaTextResolver(ctx.conv)

  /** Render a structural DTO (`ClassSource.CsDTO`) or an interface's impl
    * DTO (`ClassSource.CsInterface`).
    */
  def defns(struct: DomainCompositeStructure, source: ClassSource): RenderableCogenProduct = {
    val withMirror = source match {
      case _: ClassSource.CsInterface => false
      case _                          => true
    }

    // Legacy emits an empty base list for method input/output, plus the
    // CsDTO arm. Bases always stay empty here — `ifDecls` carries the only
    // declared parents.
    val bases: List[scala.meta.Init] = source match {
      case ClassSource.CsMethodInput  => List.empty
      case ClassSource.CsMethodOutput => List.empty
      case _                          => List.empty
    }

    val (mirrorInterface: List[scala.meta.Defn.Trait], moreBases: Interfaces) = if (withMirror) {
      // Synthesize the mirror trait `<Dto>Defn` matching legacy
      // `typespace.tools.defnId(dto)`.
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

    val superClasses: List[scala.meta.Init] = bases ++ ifDecls

    // Pre-render the superClasses init list to text. `Init.syntax` produces
    // the qualified shape the legacy renderer emitted (e.g.
    // `TestDto.Defn`, `_root_.scala.AnyVal`, etc.).
    val superClassesText: String =
      if (superClasses.isEmpty) ""
      else superClasses.map(DomainScalaParseBack.renderS30(_)).mkString(" extends ", " with ", "")

    val toolsName = s"${struct.fields.id.name.capitalize}Extensions"

    val tFullTree: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeFull(struct.fields.id))
    val tNameTree: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeName(struct.fields.id))

    // ---- final case class -----------------------------------------------
    // F-TextTree M8e: `struct.decls` is now `List[String]` — splice verbatim.
    val declsText = struct.decls.mkString(", ")

    val compositeTree: TextTree[ScalaRefHandle] =
      q"""final case class $tNameTree($declsText)$superClassesText"""

    // ---- Tools implicit class -------------------------------------------
    // Legacy: `extends ..$toolBases { }` (single Init, IRTConversions[T]).
    // Drop the empty `{}` for byte-parity.
    val toolsBase = ctx.rt.Conversions.parameterize(List(struct.t.typeFull)).init()
    val toolsBaseText = DomainScalaParseBack.renderS30(toolsBase)
    val toolsTree: TextTree[ScalaRefHandle] =
      q"""implicit class $toolsName(override protected val _value: $tFullTree) extends $toolsBaseText"""

    // ---- Companion object -----------------------------------------------
    // Splice mirror trait + constructors as pre-rendered text.
    // F-TextTree M8e: `struct.constructors` is now `List[String]`; the
    // mirror trait is still a `scala.meta.Defn.Trait` (built by
    // `interfaceRenderer.mkTrait`) and needs `renderS30` until M8f.
    val mirrorText      = mirrorInterface.map(DomainScalaParseBack.renderS30(_)).mkString("\n")
    val constructorsText = struct.constructors.mkString("\n")
    val bodyText        = (Seq(mirrorText, constructorsText).filter(_.nonEmpty)).mkString("\n")

    // The companion's term-name is the DTO's bare term name — emit as the
    // bare identifier (declaration site).
    val termNameBare = struct.fields.id.name

    val companionTree: TextTree[ScalaRefHandle] =
      q"""object $termNameBare {
         |  ${bodyText}
         |}""".stripMargin

    val compositeDefn = DomainScalaParseBack.parseClass(compositeTree.mapRender(resolver.resolve))
    val companionDefn = DomainScalaParseBack.parseObject(companionTree.mapRender(resolver.resolve))
    val toolsDefn     = DomainScalaParseBack.parseClass(toolsTree.mapRender(resolver.resolve))

    CogenProduct(compositeDefn, companionDefn, toolsDefn, List.empty)
  }

  /** Render a top-level user-declared DTO directly (convenience). */
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
