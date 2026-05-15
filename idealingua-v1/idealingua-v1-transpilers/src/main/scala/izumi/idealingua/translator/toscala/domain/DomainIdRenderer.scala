package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.PrimitiveId
import izumi.idealingua.model.common.TypeId.{EnumId, IdentifierId}
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.translator.toscala.products.{CogenProduct, RenderableCogenProduct}
import izumi.idealingua.translator.toscala.tools.ScalaTextHelpers
import izumi.idealingua.translator.toscala.types.ScalaField
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Identifier` as the same Defns the
  * legacy `IdRenderer.renderIdentifier` produces (modulo the extension
  * chain).
  *
  * F-TextTree M6..M8f: ported off legacy quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. Type references travel as
  * `ScalaRefHandle.{TypeName, TypeFull, TermFull}` value nodes. Field
  * parameters are spliced as pre-rendered Scala-source strings.
  *
  * **Carrier strategy**: M8f hands rendered Scala-source text to
  * `CogenProduct.fromTexts`; the carrier owns the String → Defn boundary.
  *
  * **Byte parity**: every emitted Defn carries body stats so the empty-
  * brace pitfall (M5) does not apply to the identifier case class or
  * companion. The `tools` implicit class for an Identifier has no parent
  * and no body — `CogenProduct.filterEmptyClasses` drops it via
  * `templ.body.stats.isEmpty && templ.inits.isEmpty`, so braces on the
  * tools shape are irrelevant to the rendered output. Still, we omit
  * `{}` from the tools text for consistency with the M5 pattern.
  *
  * Field sorting matches the legacy renderer: `sortBy(_.field.field.name)`
  * for BOTH `parse(...)` arm AND `toString` `Seq(...)` builder. The case-
  * class constructor parameters retain declaration order. Inputs come
  * directly off `TypeDef.Identifier.fields: List[IdField]` — identifiers
  * have no inheritance, so no flat-struct lookup is needed.
  */
final class DomainIdRenderer(ctx: DomainSTContext) {

  private val resolver = new DomainScalaTextResolver(ctx.conv)

  // Pre-computed bare-text references that the legacy `q"…"` path emitted
  // verbatim via `${ctx.rt.X.termBase}.syntax` / `init().syntax`. These do
  // not depend on per-call typeId, so we precompute once per renderer.
  private val tIDLIdentifierTerm = ScalaTextHelpers.renderTree(ctx.rt.tIDLIdentifier.termBase)
  private val generatedInit      = ScalaTextHelpers.renderTree(ctx.rt.generated.init())
  private val tIDLIdentifierInit = ScalaTextHelpers.renderTree(ctx.rt.tIDLIdentifier.init())

  def renderIdentifier(i: NewTypeDef.Identifier): RenderableCogenProduct = {
    import izumi.idealingua.translator.toscala.types.ScalaField._

    val typeName = i.id.name

    // F-TextTree M8e: `ScalaField` is String-native. Build the field set
    // with pre-rendered Scala-3 keyword-safe names and full type text.
    val scalaFields: List[ScalaField] = i.fields.map { idf =>
      val widened  = idfieldToField(idf)
      val nameSafe = ScalaTextHelpers.escapeIdent(idf.name)
      val tpe      = ctx.conv.toScala(idf.typeId).typeFull.toString
      ScalaField(
        name      = idf.name,
        nameSafe  = nameSafe,
        fieldType = tpe,
        field     = izumi.idealingua.model.common.ExtendedField(
          field = widened,
          defn  = izumi.idealingua.model.common.FieldDef(
            definedBy        = i.id,
            definedWithIndex = 0,
            usedBy           = i.id,
            distance         = 0,
          ),
        ),
      )
    }

    val declsText = scalaFields.toParams.mkString(", ")

    val sortedFields = scalaFields.sortBy(_.field.field.name)

    // Each parser is the body of a single named-arg in the companion's
    // `parse(...)` constructor call. We compose each as a `TextTree` and
    // join later with ", " into the call site.
    val parsers: List[TextTree[ScalaRefHandle]] = sortedFields.zipWithIndex.map {
      case (field, idx) =>
        val nameText = field.name
        val idxLit   = idx.toString
        field.field.field.typeId match {
          case t: EnumId =>
            val termFull: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TermFull(t))
            q"$nameText = $termFull.parse(parts($idxLit))"
          case t: IdentifierId =>
            val termFull: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TermFull(t))
            q"$nameText = $termFull.parse(parts($idxLit))"
          case _: PrimitiveId =>
            val fieldTypeText = field.fieldType
            q"$nameText = parsePart[$fieldTypeText](parts($idxLit), classOf[$fieldTypeText])"
          case o =>
            throw new IDLException(s"Impossible case/id field: $o")
        }
    }

    val partsBuilders: List[TextTree[ScalaRefHandle]] =
      sortedFields.map(fi => q"this.${fi.name}")

    // ---- Tools implicit class -------------------------------------------
    val typeFullTree: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeFull(i.id))
    val typeNameTree: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeName(i.id))

    val toolsName = s"${typeName}Extensions"
    val toolsTree: TextTree[ScalaRefHandle] =
      q"""implicit class $toolsName(_value: $typeFullTree)"""

    // ---- Identifier final case class ------------------------------------
    val toStringInterp = "s\"" + typeName + "#$suffix\""
    val partsSeq       = partsBuilders.join(", ").mapRender(resolver.resolve)

    val identifierTree: TextTree[ScalaRefHandle] =
      q"""final case class $typeNameTree($declsText) extends $generatedInit with $tIDLIdentifierInit {
         |  override def toString: String = {
         |    import $tIDLIdentifierTerm.*
         |    val suffix = Seq($partsSeq).map(part => escape(part.toString)).mkString(":")
         |    $toStringInterp
         |  }
         |}""".stripMargin

    // ---- Companion with `parse(String): T` ------------------------------
    val errorInterpStr  = "s\"Serialized form of $name should start with " + typeName + "#\""
    val parsersJoined   = parsers.join(", ").mapRender(resolver.resolve)
    val startsWithLit   = "\"" + typeName + "#\""
    val errorNameLit    = "\"" + i.id.toString + "\""
    val termNameBare    = typeName

    val companionTree: TextTree[ScalaRefHandle] =
      q"""object $termNameBare {
         |  def parse(s: String): $typeNameTree = {
         |    import $tIDLIdentifierTerm.*
         |    if (!s.startsWith($startsWithLit)) {
         |      val name = $errorNameLit
         |      throw new IllegalArgumentException($errorInterpStr)
         |    }
         |    val withoutPrefix = s.substring(s.indexOf("#") + 1)
         |    val parts = withoutPrefix.split(':').map(part => unescape(part))
         |    $termNameBare($parsersJoined)
         |  }
         |}""".stripMargin

    CogenProduct.fromTexts(
      defnText          = identifierTree.mapRender(resolver.resolve),
      companionBaseText = companionTree.mapRender(resolver.resolve),
      toolsText         = toolsTree.mapRender(resolver.resolve),
    )
  }

  private def idfieldToField(idf: IdField): Field = Field(idf.typeId, idf.name, idf.meta)
}
