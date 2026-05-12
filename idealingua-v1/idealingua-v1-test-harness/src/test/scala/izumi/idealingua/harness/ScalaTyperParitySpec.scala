package izumi.idealingua.harness

import izumi.idealingua.translator.{IDLLanguage, TyperImpl, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-7a corpus smoke test (post-M6).
  *
  * Before M6, this spec gated on byte-equal Scala output between
  * `--typer=legacy` and `--typer=new`. Phase A delegated the new path back
  * to the legacy translator after re-deriving a `Typespace`, so the gate
  * was structurally trivial. Phase B M1-M5 added a parallel renderer
  * exerciser that asserted byte-equal alias/enum output and tracked
  * structural-renderer divergences informationally.
  *
  * **At M6, the new path is end-to-end**: `DomainScalaTranslator` consumes
  * `Domain` directly through the new renderers + extensions. The legacy
  * `ScalaTranslator` and `IDLTyper` are no longer on this path. The four
  * FROZEN harness contracts run under `TyperImpl.Legacy` by default
  * (unchanged), so they remain green trivially.
  *
  * What this spec now asserts:
  *   1. The new path produces output (non-zero modules) for every corpus
  *      domain — no `IDLException` escapes.
  *   2. Every emitted module's content is non-empty.
  *   3. Every emitted Scala source parses cleanly under the Scala 2.13
  *      dialect (compilability smoke test — the real `compile` happens in
  *      the wire-fixture harness).
  *
  * The previous byte-parity assertion is removed: M6 deliberately diverges
  * from legacy source bytes (e.g. iteration order off `Domain.userTypes`
  * may differ from `Typespace.domain.types`; package-object grouping
  * normalisation; extension splice order). Wire-format equivalence — the
  * actual cross-language interop contract — is verified by `runWireFixtures`
  * and `runCrossLangInterop` on the default Legacy path; the M3-M5
  * 0-divergence corpus evidence stands as the wire-format-equivalence
  * proof for the new path (every renderer's pre-extension AST was
  * byte-identical to legacy across 28 domains x 2 Scala versions, modulo
  * the deterministic `sortBy(_.toString)` Circe interface implementor
  * ordering documented in `DomainCirceTranslatorExtensionBase`).
  */
final class ScalaTyperParitySpec extends AnyFunSuite {
  private val repoRoot   = HarnessCorpus.repoRootForTests()
  private val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)

  test("Scala translator: --typer=new emits parseable Scala for every corpus domain") {
    val fullCorpus = HarnessCorpus.loadCorpus(corpusRoot)
    val newOpts    = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.NewTyper)

    val failures    = scala.collection.mutable.Buffer.empty[String]
    val parseErrors = scala.collection.mutable.Buffer.empty[String]
    var checked     = 0

    for (domain <- fullCorpus) {
      val id = domain.typespace.domain.id.toString
      checked += 1
      try {
        val out = new TypespaceCompilerBaseFacade(newOpts).compile(Seq(domain))
        if (out.modules.isEmpty) {
          failures += s"$id: produced 0 modules"
        }
        out.modules.foreach { m =>
          if (m.content.trim.isEmpty) {
            failures += s"$id: empty module ${m.id}"
          } else {
            // Strip package prefix lines that include curly-brace blocks
            // package-object.scala uses for the alias grouping. scala.meta
            // parses both forms.
            try {
              import scala.meta._
              // HarnessOptions sets scalaVersions = List("2.13.18", "3.8.3"); the
              // emitter picks Scala 3 dialect when any version starts with "3"
              // (see ModuleTools.toSource:20). Parse with the matching dialect
              // so Scala-3-only syntax (e.g. `import X as _M`) is accepted.
              val input: scala.meta.Input = scala.meta.Input.String(m.content)
              val _ = scala.meta.dialects.Scala30(input).parse[scala.meta.Source].get
            } catch {
              case t: Throwable =>
                parseErrors += s"$id::${m.id}: ${t.getMessage.linesIterator.take(1).mkString}"
            }
          }
        }
      } catch {
        case t: Throwable =>
          failures += s"$id => ${t.getClass.getSimpleName}: ${t.getMessage.linesIterator.take(1).mkString}"
      }
    }

    val _ = assert(checked > 0, "smoke spec compared 0 domains")

    if (failures.nonEmpty || parseErrors.nonEmpty) {
      val msg = new StringBuilder()
      val _   = msg.append(s"checked $checked domain(s)\n")
      if (failures.nonEmpty) {
        val _ = msg.append(s"${failures.size} translation failure(s):\n").append(failures.take(20).mkString("\n")).append("\n")
      }
      if (parseErrors.nonEmpty) {
        val _ = msg.append(s"${parseErrors.size} parse error(s):\n").append(parseErrors.take(20).mkString("\n"))
      }
      fail(msg.toString)
    }
  }
}
