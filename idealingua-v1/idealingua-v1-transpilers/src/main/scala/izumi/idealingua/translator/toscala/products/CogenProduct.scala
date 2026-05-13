package izumi.idealingua.translator.toscala.products

import izumi.idealingua.model.common.TypeName
import izumi.idealingua.translator.toscala.domain.DomainScalaParseBack
import izumi.idealingua.translator.toscala.types.runtime.Import

import scala.meta.{Defn, Term}

/** F-TextTree M8a — CogenProduct carriers expose String splice slots.
  *
  * Extension outputs (AnyVal `Init` bases, Circe sibling `Init` bases,
  * cast-implicit-object stats, sibling traits) reach the carrier as
  * pre-rendered Scala source strings instead of `scala.meta.Defn` / `Init`
  * trees. At carrier render time, each String is parsed back to its
  * expected scala.meta type via `DomainScalaParseBack` and spliced into
  * the inner `Defn` outer shell; the outer shell is then printed via
  * `Defn.syntax` by `ModuleTools.toSource`.
  *
  * **Byte parity by construction**: the parse-back boundary is the same
  * one extensions already crossed before (via `q"..."` quasiquotes inside
  * extension code). M8a consolidates it: extension entry points return
  * String (via `renderS30(_)`) at the call site; carriers route the
  * Strings back through the parse-back stage at render time. The total
  * `scala.meta` reach is identical to M7 — only the boundary moved.
  *
  * Slot semantics:
  *   - `defnAnyvalBases` — prepended to `defn.templ.inits` (case class /
  *     trait header bases). Producer: `DomainAnyvalExtension`.
  *   - `companionCirceBases` — prepended to `companionBase.templ.inits`.
  *     Producer: `DomainCirceDerivationTranslatorExtension` (sibling init).
  *   - `companionCasts` — appended to `companionBase.templ.body.stats`.
  *     Producers: `DomainCast{Similar,Up,DownExpand}Extension`.
  *   - `siblings` — top-level Defn fragments appended to `more` before
  *     printing (extensions like the Circe trait, plus impl-struct splice).
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

  final case class TraitProduct(defn: Defn.Trait, more: List[Defn] = List.empty, preamble: String = "") extends MultipleCogenProduct[Defn.Trait]

  final case class EnumProduct(
    defn: Defn.Trait,
    companionBase: Defn.Object,
    elements: List[(Term.Name, Defn)],
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

  final case class CogenServiceProduct(
    server: Defn.Trait,
    client: Defn.Trait,
    clientWrapped: CogenServiceProduct.Pair[Defn.Class],
    serverWrapped: CogenServiceProduct.Pair[Defn.Class],
    methods: Defn.Object,
    marshallers: Defn.Object,
    imports: List[Import],

    //    service: CogenServiceProduct.Pair[Defn.Trait]
    //    , client: CogenServiceProduct.Pair[Defn.Trait]
    //    , wrapped: CogenServiceProduct.Pair[Defn.Trait]
    //    , defs: CogenServiceProduct.Defs
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

    //    final case class Defs(defs: Defn.Object, in: Pair[Defn.Trait], out: Pair[Defn.Trait]) {
    //      def render: Defn = {
    //        import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
    //        defs.prependDefnitions(in.render ++ out.render)
    //      }
    //    }
    //
    final case class Pair[T <: Defn](defn: T, companion: Defn.Object) {
      def render: List[Defn] = List(defn, companion)
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
    else defn.prependBase(bases.map(DomainScalaParseBack.parseInit))
  }

  def applyCompanionBases(companion: Defn.Object, bases: List[String]): Defn.Object = {
    if (bases.isEmpty) companion
    else companion.prependBase(bases.map(DomainScalaParseBack.parseInit))
  }

  def applyCompanionStats(companion: Defn.Object, stats: List[String]): Defn.Object = {
    if (stats.isEmpty) companion
    else companion.appendDefinitions(stats.map(DomainScalaParseBack.parseStat))
  }

  def parseSiblings(siblings: List[String]): List[Defn] = {
    if (siblings.isEmpty) List.empty
    else siblings.map(DomainScalaParseBack.parseStat).collect { case d: Defn => d }
  }
}
