package izumi.idealingua.translator.toscala.products

import izumi.idealingua.model.common.TypeName
import izumi.idealingua.translator.toscala.tools.ScalaTextHelpers
import izumi.idealingua.translator.toscala.types.runtime.Import

import scala.meta.Defn

/** F-TextTree M8f — CogenProduct carriers are constructed from rendered
  * Scala source strings.
  *
  * Domain-side renderers (`toscala/domain/`) compose every `Defn` shell —
  * outer trait, case class, companion object, tools implicit class — as a
  * Scala-source `String` and hand it to the carrier. The carrier parses
  * each String to its expected `scala.meta.Defn` shape *inside this file*,
  * keeping all `scala.meta` reach concentrated in `toscala/products/` and
  * `toscala/tools/`. The renderer code in `toscala/domain/` no longer
  * touches `scala.meta`.
  *
  * String slot semantics (introduced in M8a, unchanged):
  *   - `defnAnyvalBases` — prepended to the inner `Defn`'s `templ.inits`
  *     (case class / trait header bases). Producer: `DomainAnyvalExtension`.
  *   - `companionCirceBases` — prepended to `companionBase.templ.inits`.
  *     Producer: `DomainCirceDerivationTranslatorExtension` (sibling init).
  *   - `companionCasts` — appended to `companionBase.templ.body.stats`.
  *     Producers: `DomainCast{Similar,Up,DownExpand}Extension`.
  *   - `siblings` — top-level Defn fragments appended to `more` before
  *     printing (extensions like the Circe trait, plus impl-struct splice).
  *
  * Outer-shell slots (`defnText`, `companionBaseText`, `toolsText`,
  * `moreText`, `extraDefns`) are M8f additions: the renderer hands the
  * already-built scala source strings (or for the M5/M6 hot path, the
  * already-parsed `Defn`) to the carrier and the carrier owns the
  * String → Defn parse boundary.
  */
final case class CogenProduct[T <: Defn](
  defn: T,
  companionBase: Defn.Object,
  tools: Defn.Class,
  more: List[Defn] = List.empty,
  preamble: String = "",
  defnAnyvalBases: List[String] = List.empty,
  companionCirceBases: List[String] = List.empty,
  companionCasts: List[String] = List.empty,
  siblings: List[String] = List.empty,
) extends AccompaniedCogenProduct[T] {
  override def defnEffective: T = CogenProductSplice.applyDefnBases(defn, defnAnyvalBases)

  override def companion: Defn.Object = {
    import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
    val implicitClass = filterEmptyClasses(List(tools))
    val withBases     = CogenProductSplice.applyCompanionBases(companionBase, companionCirceBases)
    val withCasts     = CogenProductSplice.applyCompanionStats(withBases, companionCasts)
    withCasts.appendDefinitions(implicitClass: _*)
  }

  override def extraSiblings: List[Defn] = CogenProductSplice.parseSiblings(siblings)
}

object CogenProduct {
  type InterfaceProduct  = CogenProduct[Defn.Trait]
  type CompositeProduct  = CogenProduct[Defn.Class]
  type IdentifierProudct = CogenProduct[Defn.Class]

  /** F-TextTree M8f — opaque alias for a class-shaped ADT element product,
    * exposed so the domain renderer can name its element-list type without
    * referencing `scala.meta.Defn` directly. */
  type AdtElementClassProduct = AdtElementProduct[Defn.Class]

  /** F-TextTree M8f: build a class-shaped product from rendered source
    * Strings produced by `toscala/domain/` renderers. */
  def fromTexts(
    defnText: String,
    companionBaseText: String,
    toolsText: String,
    moreText: List[String] = List.empty,
    preamble: String = "",
    defnAnyvalBases: List[String] = List.empty,
    companionCirceBases: List[String] = List.empty,
    companionCasts: List[String] = List.empty,
    siblings: List[String] = List.empty,
  ): CogenProduct[Defn.Class] = CogenProduct[Defn.Class](
    defn                = ScalaTextHelpers.parseClass(defnText),
    companionBase       = ScalaTextHelpers.parseObject(companionBaseText),
    tools               = ScalaTextHelpers.parseClass(toolsText),
    more                = moreText.map(ScalaTextHelpers.parseDefn),
    preamble            = preamble,
    defnAnyvalBases     = defnAnyvalBases,
    companionCirceBases = companionCirceBases,
    companionCasts      = companionCasts,
    siblings            = siblings,
  )

  /** F-TextTree M8f: build a trait-shaped product (the interface renderer
    * arm) from rendered source Strings. */
  def fromTraitTexts(
    defnTraitText: String,
    companionBaseText: String,
    toolsText: String,
    moreText: List[String] = List.empty,
    preamble: String = "",
    defnAnyvalBases: List[String] = List.empty,
    companionCirceBases: List[String] = List.empty,
    companionCasts: List[String] = List.empty,
    siblings: List[String] = List.empty,
  ): CogenProduct[Defn.Trait] = CogenProduct[Defn.Trait](
    defn                = ScalaTextHelpers.parseTrait(defnTraitText),
    companionBase       = ScalaTextHelpers.parseObject(companionBaseText),
    tools               = ScalaTextHelpers.parseClass(toolsText),
    more                = moreText.map(ScalaTextHelpers.parseDefn),
    preamble            = preamble,
    defnAnyvalBases     = defnAnyvalBases,
    companionCirceBases = companionCirceBases,
    companionCasts      = companionCasts,
    siblings            = siblings,
  )

  final case class TraitProduct(defn: Defn.Trait, more: List[Defn] = List.empty, preamble: String = "") extends MultipleCogenProduct[Defn.Trait]

  /** F-TextTree M8f: `EnumProduct` element slot now carries the bare term
    * name (String) alongside the parsed `Defn`. The String is the legacy
    * `Term.Name(member.value)` token used by upstream consumers; we keep
    * it as plain text and only expose `Defn` material via the parsed
    * companion list when rendering. */
  final case class EnumProduct(
    defn: Defn.Trait,
    companionBase: Defn.Object,
    elements: List[(String, Defn)],
    more: List[Defn] = List.empty,
    preamble: String = "",
    companionCirceBases: List[String] = List.empty,
    siblings: List[String] = List.empty,
  ) extends AccompaniedCogenProduct[Defn.Trait] {
    override def companion: Defn.Object = {
      import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
      val withBases = CogenProductSplice.applyCompanionBases(companionBase, companionCirceBases)
      withBases.appendDefinitions(elements.map(_._2))
    }

    override def extraSiblings: List[Defn] = CogenProductSplice.parseSiblings(siblings)
  }

  object EnumProduct {
    /** F-TextTree M8f: convenience to build an `EnumProduct` from rendered
      * source text (parsed back to `Defn` inside the carrier). */
    def fromTexts(
      defnTraitText: String,
      companionBaseText: String,
      elements: List[(String, String)],
      moreText: List[String] = List.empty,
      preamble: String = "",
      companionCirceBases: List[String] = List.empty,
      siblings: List[String] = List.empty,
    ): EnumProduct = EnumProduct(
      defn                = ScalaTextHelpers.parseTrait(defnTraitText),
      companionBase       = ScalaTextHelpers.parseObject(companionBaseText),
      elements            = elements.map { case (n, s) => n -> ScalaTextHelpers.parseDefn(s) },
      more                = moreText.map(ScalaTextHelpers.parseDefn),
      preamble            = preamble,
      companionCirceBases = companionCirceBases,
      siblings            = siblings,
    )
  }

  final case class AdtElementProduct[T <: Defn](
    name: TypeName,
    defn: T,
    companion: Defn.Object,
    converters: List[Defn.Def],
    evenMore: List[Defn] = List.empty,
    preamble: String     = "",
  ) extends AccompaniedCogenProduct[T] {

    override def more: List[Defn] = evenMore ++ converters
  }

  object AdtElementProduct {
    /** F-TextTree M8f: build an ADT element product from rendered text. */
    def fromTexts(
      name: TypeName,
      defnText: String,
      companionText: String,
      convertersText: List[String],
    ): AdtElementClassProduct = AdtElementProduct[Defn.Class](
      name       = name,
      defn       = ScalaTextHelpers.parseClass(defnText),
      companion  = ScalaTextHelpers.parseObject(companionText),
      converters = convertersText.map(ScalaTextHelpers.parseDef),
    )
  }

  final case class AdtProduct(
    defn: Defn.Trait,
    companionBase: Defn.Object,
    elements: List[AdtElementProduct[Defn.Class]],
    more: List[Defn] = List.empty,
    preamble: String = "",
    companionCirceBases: List[String] = List.empty,
    siblings: List[String] = List.empty,
  ) extends AccompaniedCogenProduct[Defn.Trait] {
    override def companion: Defn.Object = {
      import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
      val withBases = CogenProductSplice.applyCompanionBases(companionBase, companionCirceBases)
      withBases.appendDefinitions(elements.flatMap(_.render))
    }

    override def extraSiblings: List[Defn] = CogenProductSplice.parseSiblings(siblings)
  }

  object AdtProduct {
    /** F-TextTree M8f: build an ADT product from rendered text + already-
      * built element products. */
    def fromTexts(
      defnTraitText: String,
      companionBaseText: String,
      elements: List[AdtElementClassProduct],
      companionCirceBases: List[String] = List.empty,
      siblings: List[String] = List.empty,
    ): AdtProduct = AdtProduct(
      defn                = ScalaTextHelpers.parseTrait(defnTraitText),
      companionBase       = ScalaTextHelpers.parseObject(companionBaseText),
      elements            = elements,
      companionCirceBases = companionCirceBases,
      siblings            = siblings,
    )
  }

  final case class CogenServiceProduct(
    server: Defn.Trait,
    client: Defn.Trait,
    clientWrapped: CogenServiceProduct.Pair[Defn.Class],
    serverWrapped: CogenServiceProduct.Pair[Defn.Class],
    methods: Defn.Object,
    marshallers: Defn.Object,
    imports: List[Import],
  ) extends RenderableCogenProduct {

    override def preamble: String =
      s"""${imports.map(_.render).mkString("\n")}
         |""".stripMargin

    def render: List[Defn] = {
      List(server, client) ++
      List(serverWrapped, clientWrapped).flatMap(_.render) ++
      List(methods, marshallers)
    }
  }

  object CogenServiceProduct {

    final case class Pair[T <: Defn](defn: T, companion: Defn.Object) {
      def render: List[Defn] = List(defn, companion)
    }

    object Pair {
      /** F-TextTree M8f: build a class-shaped pair from rendered text. */
      def fromTexts(defnClassText: String, companionText: String): Pair[Defn.Class] =
        Pair[Defn.Class](
          defn      = ScalaTextHelpers.parseClass(defnClassText),
          companion = ScalaTextHelpers.parseObject(companionText),
        )
    }

    /** F-TextTree M8f: build a `CogenServiceProduct` from rendered Strings.
      * The `methods` arm is "skeleton + appended defStruct text" — the
      * caller hands the skeleton text and the per-method defStruct text
      * fragments; we parse the skeleton then `appendDefinitions` the
      * parsed defStructs so the printer indent matches the legacy
      * `dialect(parsed).syntax` shape exactly (see
      * `DomainServiceRenderer.scala` for the printer-quirk rationale). */
    def fromTexts(
      serverText: String,
      clientText: String,
      serverWrapped: Pair[Defn.Class],
      clientWrapped: Pair[Defn.Class],
      methodsSkeletonText: String,
      methodsDefStructTexts: List[String],
      codecsText: String,
      imports: List[Import],
    ): CogenServiceProduct = {
      val methodsObj = {
        import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
        val skeleton = ScalaTextHelpers.parseObject(methodsSkeletonText)
        if (methodsDefStructTexts.isEmpty) skeleton
        else skeleton.appendDefinitions(methodsDefStructTexts.map(ScalaTextHelpers.parseDefn): _*)
      }
      // NOTE (M8f byte-parity bug-for-bug): the legacy `CogenServiceProduct`
      // positional ctor swapped `serverWrapped` / `clientWrapped` — the old
      // call site passed `(server, client, ServerWrappedPair, ClientWrappedPair, …)`
      // into a case class whose field order is
      // `(…, clientWrapped, serverWrapped, …)`. The golden corpus encodes
      // that swap: `render` emits `List(serverWrapped, clientWrapped)`, so
      // the on-disk goldens print the *client*-wrapped class first (the
      // field labelled `serverWrapped` actually holds the client-wrapped
      // pair). Preserve this for byte parity by swapping the named args.
      CogenServiceProduct(
        server        = ScalaTextHelpers.parseTrait(serverText),
        client        = ScalaTextHelpers.parseTrait(clientText),
        clientWrapped = serverWrapped,
        serverWrapped = clientWrapped,
        methods       = methodsObj,
        marshallers   = ScalaTextHelpers.parseObject(codecsText),
        imports       = imports,
      )
    }

  }

}

/** F-TextTree M8a — String→scala.meta splice helpers shared by every
  * CogenProduct carrier. Each helper short-circuits when the string list
  * is empty so the no-extension-applied case stays a zero-cost identity
  * (preserving the M7 fast path). */
private[products] object CogenProductSplice {
  import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._

  def applyDefnBases[T <: Defn](defn: T, bases: List[String]): T = {
    if (bases.isEmpty) defn
    else defn.prependBase(bases.map(ScalaTextHelpers.parseInit))
  }

  def applyCompanionBases(companion: Defn.Object, bases: List[String]): Defn.Object = {
    if (bases.isEmpty) companion
    else companion.prependBase(bases.map(ScalaTextHelpers.parseInit))
  }

  def applyCompanionStats(companion: Defn.Object, stats: List[String]): Defn.Object = {
    if (stats.isEmpty) companion
    else companion.appendDefinitions(stats.map(ScalaTextHelpers.parseStat))
  }

  def parseSiblings(siblings: List[String]): List[Defn] = {
    if (siblings.isEmpty) List.empty
    else siblings.map(ScalaTextHelpers.parseStat).collect { case d: Defn => d }
  }
}
