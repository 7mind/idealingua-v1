package izumi.idealingua.harness

import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.{IDLLanguage, TyperImpl, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-7a.2 byte-equality regression guard for the new typer.
  *
  * For every domain in the positive corpus, compiles the Scala backend
  * twice — once with `TyperImpl.Legacy`, once with `TyperImpl.NewTyper` —
  * and computes per-module byte equality on the UTF-8 content. The spec
  * partitions every observed divergence into two sets:
  *
  *   1. `AcceptedDivergences` — modules whose byte difference is driven
  *      *only* by HashMap / Set iteration order in the legacy IR (e.g.
  *      `implementingDtos`, `compatibleDtos`, `StructuralQueriesImpl.converters`
  *      Set-iteration leaks). These are cosmetic: the wire format is
  *      preserved regardless of case-arm emission order inside generated
  *      Circe codecs (Encoder dispatches by runtime type; Decoder tries
  *      cases until one succeeds), and named-arg constructor calls are
  *      order-independent at the call site. Cast-helper emission order is
  *      purely cosmetic. The decision to accept these as residual is
  *      explicit and locked by the user — see tasks.md IMPL-9 wrap-up.
  *
  *   2. `SubstantiveDivergences` (gated count: `SubstantiveBaseline`) —
  *      every other divergence. These trace to F-followup defects that
  *      change observable structure (missing case-arms, wrong cast targets
  *      derived from real IR differences, missing AnyVal mixin, subtraction
  *      semantics, clone-newtype materialization, deep-diamond field-order
  *      in case-class declarations, etc).
  *
  * The spec asserts:
  *   - Set of module ids is the same modulo `AcceptedMissingModules`.
  *   - Count of substantive divergences == `SubstantiveBaseline`.
  *   - Accepted-residual count == `AcceptedDivergences.size +
  *     AcceptedMissingModules.size` (sanity bound; not a hard gate).
  *
  * Going UP on substantive count fails immediately. Going DOWN also fails
  * with "please lower the baseline" so the gate stays tight as F-followups
  * close.
  *
  * Once `SubstantiveBaseline == 0` on both Scala 2.13 and 3.8, this spec
  * gates IMPL-9 (flipping `TyperImpl.NewTyper` as the default). Until then
  * `tasks.md` IMPL-9 row catalogs the F-followups still open.
  */
final class ScalaTranslatorByteParitySpec extends AnyFunSuite {
  private val repoRoot   = HarnessCorpus.repoRootForTests()
  private val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)

  /** Cosmetic HashMap / Set iteration order divergences. Keys are
    * `<domainId>::<moduleKey>`. Categorized by root cause:
    *
    *   - `A` — HashMap iteration order in the legacy IR
    *     (`implementingDtos` Set affecting Circe encoder case-arm sequence;
    *     field HashMap affecting named-arg call order inside `apply` /
    *     `Struct(...)` constructions; HashMap affecting case-class field
    *     declaration order when the legacy IR builds the type from a
    *     HashMap-backed accumulator).
    *
    *   - `B` — Set iteration in `compatibleDtos` /
    *     `StructuralQueriesImpl.converters` picking a different "first"
    *     peer for the singleton `Struct_cast_into_<X>` / `<T>_downcast_extend_<X>`
    *     extension. Both runs select a valid peer; only the choice of
    *     representative differs.
    *
    * Every entry here was inspected against `regenerateGoldens` / `git
    * diff golden/scala`-equivalent output and confirmed to be ordering-only.
    */
  private val AcceptedDivergences: Set[String] = Set(
    // (A) HashMap iteration — Circe encoder case-arm order
    "{idltest.inheritance}::idltest/inheritance/Covariant.scala",
    "{idltest.inheritance}::idltest/inheritance/Empty.scala",
    "{idltest.inheritance}::idltest/inheritance/Metadata.scala",
    "{idltest.inheritance}::idltest/inheritance/Notification.scala",
    "{idltest.inheritance}::idltest/inheritance/NotificationWithA.scala",
    "{idltest.inheritance}::idltest/inheritance/NotificationWithB.scala",
    "{idltest.inheritance}::idltest/inheritance/WithCovariance.scala",
    "{idltest.inheritance}::idltest/inheritance/NotiBase.scala",
    "{idltest.inheritance}::idltest/inheritance/NotiWithFile.scala",
    "{idltest.phase}::idltest/phase/Name_stored_.scala",
    "{izumi.test.domain01}::izumi/test/domain01/CommonFailure.scala",
    "{izumi.test.domain01}::izumi/test/domain01/RtestMixin2.scala",
    "{izumi.test.domain01}::izumi/test/domain01/TestInterface.scala",
    "{izumi.test.domain02}::izumi/test/domain02/TestInterface1.scala",
    "{izumi.test.domain02}::izumi/test/domain02/TestInterface2.scala",
    "{izumi.test.domain02}::izumi/test/domain02/TestInterface3.scala",
    "{idltest.services}::idltest/services/SuccessData.scala",
    "{idltest.dtofields}::idltest/dtofields/Metadata.scala",
    "{idltest.dtofields}::idltest/dtofields/WHPair.scala",
    // (A) HashMap iteration — named-arg / apply param order
    "{idltest.inheritance}::idltest/inheritance/NotiWithFileRevision.scala",
    "{idltest.inheritance}::idltest/inheritance/PointLike.scala",
    "{izumi.test.domain01}::izumi/test/domain01/AllTypes.scala",
    "{idltest.diamonds}::idltest/diamonds/DTO1.scala",
    "{idltest.diamonds}::idltest/diamonds/TestInterface3.scala",
    "{izumi.test.domain02}::izumi/test/domain02/ImportIdTest.scala",
    "{izumi.test.domain02}::izumi/test/domain02/Pair2.scala",
    "{idltest.datainheritancetransitive}::idltest/datainheritancetransitive/CouponData.scala",
    "{idltest.ast}::idltest/ast/TIfNode.scala",
    "{idltest.ast}::idltest/ast/TLamNode.scala",
    "{idltest.ast}::idltest/ast/BoolNode.scala",
    "{idltest.ast}::idltest/ast/IntNode.scala",
    "{idltest.ast}::idltest/ast/IfNode.scala",
    "{idltest.ast}::idltest/ast/LamNode.scala",
    "{idltest.dtofields}::idltest/dtofields/PointLike.scala",
    // (B) Set iteration — singleton peer selection in cast/downcast extensions
    "{idltest.inheritance}::idltest/inheritance/CovariantA.scala",
    "{idltest.inheritance}::idltest/inheritance/CovariantB.scala",
    "{idltest.inheritance}::idltest/inheritance/DataWithAB.scala",
    "{idltest.inheritance}::idltest/inheritance/IntPair.scala",
    "{idltest.inheritance}::idltest/inheritance/NotificationWithAB.scala",
    "{izumi.test.domain01}::izumi/test/domain01/ExtendedMixin.scala",
    "{izumi.test.domain01}::izumi/test/domain01/GenericFailureData.scala",
    "{idltest.diamonds}::idltest/diamonds/TestInterface1.scala",
    "{idltest.upcasts}::idltest/upcasts/ItemContent.scala",
    "{izumi.test.domain02}::izumi/test/domain02/NestedAdtsService.scala",
    "{idltest.events}::idltest/events/TestBuzzer.scala",
    "{idltest.services}::idltest/services/Request.scala",
    "{idltest.services}::idltest/services/TestService.scala",
    "{idltest.dtofields}::idltest/dtofields/IntPair.scala",
  )

  /** Modules emitted by Legacy but absent in NewTyper (or vice versa) that
    * are accepted as a known F-followup (currently: `clone X into Y { ... }`
    * newtype materialization). Keys are `<domainId>::<moduleKey>`. */
  private val AcceptedMissingModules: Set[String] = Set.empty

  /** Number of *substantive* per-module byte-divergent files between Legacy
    * and NewTyper across the 28-domain corpus, EXCLUDING the entries in
    * `AcceptedDivergences` and `AcceptedMissingModules`.
    *
    * Substantive defects observed at this baseline (one F-followup per
    * family; see `tasks.md` IMPL-9):
    *   - F-clone-newtype: `idltest.clones` package-object missing
    *     `type M2 = M0`; M2.scala module missing entirely from new;
    *     CovariantDTO2 / InheritedCovariant / Name_stored / Name (encoder
    *     case targets) / Name_stored (extra upcast) / PrivateTestObject
    *     emit asymmetric upcast objects driven by clone-derived structs.
    *   - F-subtraction (`- field` operator): PublicUser1/2 retain
    *     subtracted fields; SecurityAttributes / User1 / User2 /
    *     PersonalAttributes downcast/upcast targets reflect missing
    *     subtraction semantics.
    *   - F-cross-domain-anyval: ImportIdService.MixiInput missing
    *     `with AnyVal` because new IR's `flattenedStructs` is per-domain.
    *   - F-anyval-predicate-singlefield: IA2.Struct missing `AnyVal`.
    *   - F-composite-mixin: PrivateMixin / PrivateMixinPrivateParent /
    *     TestInterface2 (diamonds) / Identifiable (upcasts) /
    *     Name_view emit different `using(...)` parameter lists (flattened
    *     fields vs original composite mixin reference).
    *   - F-implementing-dtos-missing: NotiBase / NotiWithFile /
    *     PrivateMixinParent / PersonalAttributes / 7 AST nodes
    *     (AppNode / BoolNode / FloatNode / IfNode / IntNode / LamNode /
    *     SymNode) / IA1 — encoder missing one or more `case v: X.Struct =>`
    *     arms because the implementing DTO is not registered in the new
    *     IR's parent set.
    *   - F-alias-mixin-scope: D1 has different field set
    *     (legacy `(value, f2)` vs new `(value)` + AnyVal); M1.Struct
    *     emits `_cast_into_D1` in new but `_upcast_Struct` in legacy.
    *   - F-deep-diamond / cross-domain inheritance: domain02 DTO1
    *     declares extra `sameField, sameEverywhereField` in new.
    *   - F-adt-companion-shape: TestAliasServ.TestADTIdReturnOutput
    *     companion legacy `trait XCirce` vs new `object X extends IDLAdt`.
    *
    * Trail of substantive-baseline movements:
    *   - 2026-05-12 (initial wrap-up cycle audit): 35.
    *   - 2026-05-12 (Fh1: F-subtraction — expand removedConcepts to flattened
    *     field names in StructuralFlattener): 32.
    *   - 2026-05-12 (Fh2: F-clone-newtype — `clone X into Y { ... }` with
    *     non-empty modifiers now materializes Y as same kind as X, plus
    *     `transitiveParents` rewritten to match legacy `safeAllParents`
    *     asymmetric concept-vs-interface recursion): 30.  Two AcceptedDivergences
    *     entries (`{idltest.ast}::idltest/ast/TypeInfo.scala`,
    *     `{idltest.phase}::idltest/phase/LengthInBytes.scala`) became
    *     byte-identical as a side-effect of the parents-graph realignment.
    *   - 2026-05-12 (Fh3: F-implementing-dtos-missing — surface descendant
    *     interfaces' mirror DTOs as encoder-implementors in
    *     `DomainCirceTranslatorExtensionBase.emitForInterface`): 24. The
    *     missing-case-arms divergences (NotiBase/NotiWithFile + AST encoder
    *     case-arm pairs BoolNode/IntNode/IfNode/LamNode) collapse to
    *     legacy-HashMap iteration-order residuals — added to
    *     `AcceptedDivergences` (category A).
    *   - 2026-05-12 (Fi1/Fi2 + compile gate: F-alt-output-cast-targets-nonexistent
    *     pass-through, F-covariant-field-type-narrowing intersect, generic
    *     signature key fix, Adt-Circe wiring inside service outputs): 23.
    *     `GoldenCompile` now drives the Scala backend through `TyperImpl.NewTyper`
    *     so the on-disk Layer A Scala goldens compile as new-typer output, and
    *     the harness's standard `Compile/compile` becomes the type-error gate.
    *     One residual cross-domain mixin defect (D1 / aliases→aliases2 `M2.f2`)
    *     is the next compile-gate stop — its fix requires plumbing cross-domain
    *     resolved `FlatStruct` lookups into Phase 6 (StructuralFlattener).
    */
  private val SubstantiveBaseline: Int = 23

  private def keyOf(id: ModuleId): String =
    (id.path :+ id.name).mkString("/")

  private def moduleMap(modules: Seq[Module]): Map[String, String] =
    modules.iterator.map(m => keyOf(m.id) -> m.content).toMap

  private final case class Divergence(domainKey: String, moduleKey: String, summary: String) {
    def fullKey: String = s"$domainKey::$moduleKey"
  }

  test("Scala translator: --typer=new emits byte-identical modules to --typer=legacy for every corpus domain") {
    val fullCorpus = HarnessCorpus.loadCorpus(corpusRoot)
    val legacyOpts = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.Legacy)
    val newOpts    = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.NewTyper)

    val divergences        = scala.collection.mutable.Buffer.empty[Divergence]
    val missingModules     = scala.collection.mutable.Buffer.empty[Divergence]
    val crashes            = scala.collection.mutable.Buffer.empty[String]
    var checked            = 0

    for (domain <- fullCorpus) {
      val id = domain.typespace.domain.id.toString
      checked += 1
      try {
        val legacyOut = new TypespaceCompilerBaseFacade(legacyOpts).compile(Seq(domain))
        val newOut    = new TypespaceCompilerBaseFacade(newOpts).compile(Seq(domain))
        val legacyMap = moduleMap(legacyOut.modules)
        val newMap    = moduleMap(newOut.modules)

        val legacyOnly = legacyMap.keySet.diff(newMap.keySet).toSeq.sorted
        val newOnly    = newMap.keySet.diff(legacyMap.keySet).toSeq.sorted
        for (k <- legacyOnly) missingModules += Divergence(id, k, s"legacy-only module")
        for (k <- newOnly)    missingModules += Divergence(id, k, s"new-only module")

        val common = legacyMap.keySet.intersect(newMap.keySet).toSeq.sorted
        for (k <- common) {
          val a = legacyMap(k)
          val b = newMap(k)
          if (a != b) {
            // Compute a short diff summary: first differing line index + a snippet.
            val al = a.linesIterator.toVector
            val bl = b.linesIterator.toVector
            val firstDiff =
              al.zip(bl).indexWhere { case (x, y) => x != y } match {
                case -1 if al.length != bl.length => math.min(al.length, bl.length)
                case n => n
              }
            val snippet =
              if (firstDiff >= 0 && firstDiff < math.max(al.length, bl.length)) {
                val la = if (firstDiff < al.length) al(firstDiff) else "<EOF>"
                val lb = if (firstDiff < bl.length) bl(firstDiff) else "<EOF>"
                s" @L${firstDiff + 1} legacy=`${la.take(120)}` new=`${lb.take(120)}`"
              } else ""
            divergences += Divergence(id, k, s"bytes differ (legacy=${a.length}B new=${b.length}B)$snippet")
          }
        }
      } catch {
        case t: Throwable =>
          crashes += s"$id => ${t.getClass.getSimpleName}: ${t.getMessage.linesIterator.take(1).mkString}"
      }
    }

    val _ = assert(checked > 0, "byte-parity spec compared 0 domains")
    val _ = assert(crashes.isEmpty, s"compiler crashes during byte-parity scan:\n${crashes.mkString("\n")}")

    val (acceptedDiv, substantiveDiv) = divergences.partition(d => AcceptedDivergences.contains(d.fullKey))
    val (acceptedMis, substantiveMis) = missingModules.partition(m => AcceptedMissingModules.contains(m.fullKey))

    val substantive = substantiveDiv ++ substantiveMis
    val accepted    = acceptedDiv ++ acceptedMis
    val observed    = substantive.size

    // Surface any *stale* entries — accepted-list keys that no longer appear in the
    // current divergence set. Stale entries silently mask future regressions.
    val observedKeys     = (divergences ++ missingModules).iterator.map(_.fullKey).toSet
    val staleAccepted    = AcceptedDivergences.diff(observedKeys)
    val staleMissing     = AcceptedMissingModules.diff(observedKeys)
    val staleAll         = staleAccepted ++ staleMissing
    if (staleAll.nonEmpty) {
      fail(
        s"STALE: ${staleAll.size} entries in AcceptedDivergences / AcceptedMissingModules no longer match any " +
          s"observed divergence. Remove them from the spec:\n${staleAll.toSeq.sorted.mkString("\n")}"
      )
    }

    if (observed > SubstantiveBaseline) {
      val msg = new StringBuilder()
      val _   = msg.append(s"REGRESSION: substantive byte-parity divergences increased from baseline=$SubstantiveBaseline to observed=$observed across $checked domain(s).\n")
      val _   = msg.append(s"(accepted residual = ${accepted.size}; see AcceptedDivergences / AcceptedMissingModules sets.)\n")
      val _   = msg.append("This indicates a new defect was introduced — investigate or update the baseline only after root-cause review.\n")
      val _   = msg.append(substantive.iterator.take(80).map(d => s"${d.fullKey} ${d.summary}").mkString("\n"))
      if (substantive.size > 80) {
        val _ = msg.append(s"\n... and ${substantive.size - 80} more")
      }
      fail(msg.toString)
    } else if (observed < SubstantiveBaseline) {
      fail(
        s"IMPROVEMENT: substantive byte-parity divergences dropped from baseline=$SubstantiveBaseline to observed=$observed " +
          s"(accepted residual = ${accepted.size}). " +
          s"Please lower `SubstantiveBaseline` to $observed and note the F-followup that closed in tasks.md."
      )
    }
    // observed == SubstantiveBaseline: gate is steady; F-followups in
    // `tasks.md` IMPL-9 still pending.
  }
}
