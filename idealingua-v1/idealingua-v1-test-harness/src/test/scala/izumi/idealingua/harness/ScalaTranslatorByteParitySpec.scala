package izumi.idealingua.harness

import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.{IDLLanguage, TyperImpl, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-7a.2 byte-equality regression guard for the new typer.
  *
  * For every domain in the positive corpus, compiles the Scala backend
  * twice — once with `TyperImpl.Legacy`, once with `TyperImpl.NewTyper` —
  * and computes per-module byte equality on the UTF-8 content. The spec
  * asserts that:
  *
  *   1. Both runs emit the exact same set of module ids (path + name).
  *   2. The number of byte-divergent modules does not exceed a recorded
  *      baseline (`KnownDivergenceBaseline`). Going UP — i.e. a new
  *      defect that introduces additional divergence — fails the spec
  *      immediately. Going DOWN — i.e. an F-followup fix that eliminates
  *      divergence — also fails the spec with a "please lower the
  *      baseline" message, so the gate stays accurate.
  *
  * Originally introduced after the F8 defect (ephemeral DTOs missing from
  * `flattenedStructs` caused every service method to render with zero
  * parameters), which the prior `ScalaTyperParitySpec` parseability-only
  * smoke test failed to detect.
  *
  * Once `KnownDivergenceBaseline == 0` on both Scala 2.13 and 3.8, this
  * spec gates IMPL-9 (flipping `TyperImpl.NewTyper` as the default).
  * Until then the recorded divergence inventory tracks remaining
  * F-followups; see `tasks.md` IMPL-9 row for the per-defect catalog.
  */
final class ScalaTranslatorByteParitySpec extends AnyFunSuite {
  private val repoRoot   = HarnessCorpus.repoRootForTests()
  private val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)

  /** Number of per-module byte-divergent files between Legacy and NewTyper
    * across the 28-domain corpus.
    *
    * Trail of baseline movements:
    *   - 2026-05-12 (F8 fix): 167. Initial inventory after ephemeral DTOs
    *     registered in `flattenedStructs`.
    *   - 2026-05-12 (Fa: defects #5 + #7): 167 (in-place; every previously
    *     affected module still diverged for at least one other defect).
    *   - 2026-05-12 (Fb: defects #1 + #6): 167 (in-place; same reason).
    *   - 2026-05-12 (Fc: defects #3 + #4): 167 → **113** (–54 modules).
    *     Upcast self-target emission + Circe interface mirror inclusion
    *     closed enough catalog entries that 54 modules became byte-identical
    *     end-to-end.
    *   - 2026-05-12 (Fd: defect #2 Circe layout): 113 → **110** (–3 modules).
    *     Interface-companion `StructCirce` trait + `Struct_upcast_*` cast
    *     set on the inner mirror DTO + AnyVal mixin on the impl case class +
    *     parent-iteration field-order on top-level DTO `_upcast_` bodies.
    *     Most remaining divergences are dominated by separate defects
    *     (IRTCast-vs-IRTExtend on the interface-implementor cast, missing
    *     `Struct_cast_into_<peer>` set, parent-listing ordering, clone-newtype
    *     shape) so the byte-count metric moves only when EVERY divergence in
    *     a module is closed.
    *   - 2026-05-12 (Fe1: interface-mirror `downcast_extend` + extension
    *     ordering + impl-struct self-upcast field-order + empty-signature
    *     peer matching): 110 → **96** (–14 modules).  Three coupled fixes:
    *     (1) `StructuralFlattener` now registers `EphemeralOrigin.InterfaceMirror`
    *     entries in `parents`/`implementingDtos` so `DomainCastDownExpandExtension`
    *     emits `<I>_downcast_extend_<I>Struct` mirroring legacy `compatibleDtos`.
    *     (2) `DomainScalaTranslator.emitInterface` companion-stat order
    *     switched to `sims ++ downs ++ ups` matching legacy
    *     `defaultExtensions` (CastSimilar, CastDownExpand, CastUp).
    *     (3) `DomainCastUpExtension.generateUpcastsForImplStruct` now sorts
    *     fields via `DomainScalaStruct.fromFlat` (full legacy sort key with
    *     `-definedWithIndex` tiebreaker), so the `Struct_upcast_Struct`
    *     self-cast body uses declaration order.
    *     (4) `DomainCastSimilarExtension.sameSignature` no longer
    *     early-returns on empty signatures, matching legacy emission of
    *     `Struct_cast_into_<other-empty-DTO>` for empty-fielded mixins.
    *   - 2026-05-12 (Fe3: same-signature cast field sort): 86 → **82**
    *     (–4 modules).  `DomainCastSimilarExtension.mkConverters` now
    *     orders the converter body assignments via
    *     `DomainScalaStruct.fromFlat` (legacy
    *     `(distance, definedBy, -idx).reverse`).  Plain
    *     `sortBy(_.distance)` produced own-fields-first
    *     (`Target(someInt = …, value = …)`) instead of legacy
    *     parents-first (`Target(value = …, someInt = …)`).
    *   - 2026-05-12 (Fe4: same-type dedup picks deepest entry): 82 →
    *     **80** (–2 modules).  `DomainScalaStruct.fromFlat` previously
    *     deduped same-name duplicates by smallest distance unconditionally.
    *     Legacy `StructuralQueriesImpl.NonContradictive` returns
    *     `Some(fields.head)` when all duplicates share the same `Field`
    *     value — and because legacy `FieldExtractor` emits
    *     `superFields ++ embeddedFields ++ thisFields` (parents-first),
    *     `head` is the deepest occurrence.  In the new IR the BFS-flattener
    *     emits self-first, so the deepest entry is `occurrences.maxBy(_.distance)`.
    *     `TestInterface3.scala` no longer reorders the trait body's
    *     `def if1Field_overriden` / `def if1Field_inherited` declarations.
    *   - 2026-05-12 (Fe2: cast-down sort + parent BFS order + impl-struct
    *     peer `cast_into`): 96 → **86** (–10 modules).  Four coupled fixes:
    *     (5) `DomainCastDownExpandExtension.constructorsForInterface` now
    *     orders the `using(...)` assignments via `DomainScalaStruct.fromFlat`,
    *     so multi-distance fields (e.g. `TBoolNode.Struct(tpe = …, lit = …)`)
    *     match the legacy `(distance, definedBy, -idx).reverse` ordering.
    *     (6) `DomainCastUpExtension.generateUpcastsForImplStruct` no longer
    *     alphabetises `qualifiedAncestors`; it keeps BFS-distance order so
    *     `Struct_upcast_<closest>` precedes `Struct_upcast_<deeper>`
    *     (legacy emits parents in declaration order, not alphabetical).
    *     (7) `DomainCastUpExtension.structuralParents` likewise replaces
    *     `sortBy(_.toString)` with a BFS-order traversal over
    *     `struct.superclasses.interfaces ++ concepts`, restoring `Point →
    *     {Metadata, IntPair}` declaration order.
    *     (8) `DomainInterfaceRenderer` now appends
    *     `DomainCastSimilarExtension.mkConvertersForImplStruct` on the
    *     mirror `<I>.Struct` companion, emitting
    *     `Struct_cast_into_<peer-mirror>` entries the legacy
    *     `defaultExtensions` chain produced on the synthesised impl DTO
    *     (Pair1.Struct ↔ Pair2.Struct etc.).
    *
    * Remaining catalog entries (each becomes a future F-followup):
    * `cast_into` vs `downcast_extend_TStruct` naming/IRTCast-vs-IRTExtend on
    * the mirror-as-implementor path; missing `Struct_cast_into_*` peer-mirror
    * set inside interface impl companions; `clone X into Y { ... }` newtype
    * shape; non-matching parent-order (alphabetical vs legacy declaration
    * order); etc.
    *
    * Symmetric on Scala 2.13.18 and 3.8.3.
    */
  private val KnownDivergenceBaseline: Int = 80

  private def keyOf(id: ModuleId): String =
    (id.path :+ id.name).mkString("/")

  private def moduleMap(modules: Seq[Module]): Map[String, String] =
    modules.iterator.map(m => keyOf(m.id) -> m.content).toMap

  test("Scala translator: --typer=new emits byte-identical modules to --typer=legacy for every corpus domain") {
    val fullCorpus = HarnessCorpus.loadCorpus(corpusRoot)
    val legacyOpts = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.Legacy)
    val newOpts    = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.NewTyper)

    val failures = scala.collection.mutable.Buffer.empty[String]
    var checked  = 0

    for (domain <- fullCorpus) {
      val id = domain.typespace.domain.id.toString
      checked += 1
      try {
        val legacyOut = new TypespaceCompilerBaseFacade(legacyOpts).compile(Seq(domain))
        val newOut    = new TypespaceCompilerBaseFacade(newOpts).compile(Seq(domain))
        val legacyMap = moduleMap(legacyOut.modules)
        val newMap    = moduleMap(newOut.modules)

        val legacyOnly = legacyMap.keySet.diff(newMap.keySet)
        val newOnly    = newMap.keySet.diff(legacyMap.keySet)
        if (legacyOnly.nonEmpty || newOnly.nonEmpty) {
          if (legacyOnly.nonEmpty) failures += s"$id: legacy-only modules: ${legacyOnly.toSeq.sorted.take(10).mkString(", ")}"
          if (newOnly.nonEmpty)    failures += s"$id: new-only modules: ${newOnly.toSeq.sorted.take(10).mkString(", ")}"
        }

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
            failures += s"$id::$k bytes differ (legacy=${a.length}B new=${b.length}B)$snippet"
          }
        }
      } catch {
        case t: Throwable =>
          failures += s"$id => ${t.getClass.getSimpleName}: ${t.getMessage.linesIterator.take(1).mkString}"
      }
    }

    val _ = assert(checked > 0, "byte-parity spec compared 0 domains")

    val observed = failures.size
    if (observed > KnownDivergenceBaseline) {
      val msg = new StringBuilder()
      val _   = msg.append(s"REGRESSION: byte-parity divergences increased from baseline=$KnownDivergenceBaseline to observed=$observed across $checked domain(s).\n")
      val _   = msg.append("This indicates a new defect was introduced — investigate or update the baseline only after root-cause review.\n")
      val _   = msg.append(failures.take(80).mkString("\n"))
      if (failures.size > 80) {
        val _ = msg.append(s"\n... and ${failures.size - 80} more")
      }
      fail(msg.toString)
    } else if (observed < KnownDivergenceBaseline) {
      fail(
        s"IMPROVEMENT: byte-parity divergences dropped from baseline=$KnownDivergenceBaseline to observed=$observed. " +
          "Please lower `KnownDivergenceBaseline` in this file to $observed so the gate stays tight, and note the F-followup that closed in tasks.md."
      )
    }
    // observed == KnownDivergenceBaseline: gate is steady; F-followups in
    // `tasks.md` IMPL-9 still pending.
  }
}
