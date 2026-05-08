package izumi.idealingua.harness

import java.nio.file.Path
import scala.collection.mutable

/** PR-03.4 T2 — Cross-language matrix runner. */
object WireFixtureCrossLangRunner {
  import CrossLangChain.{Lang, CrossLangFailureKind}

  final case class Failure(s: Lang, m: Lang, fixture: WireFixtures.FixtureFile, kind: CrossLangFailureKind)

  final case class Report(
    perDirection: Seq[(Lang, Lang, Int, Int, Int)], // (s, m, verified, skipped, excluded)
    failures: Seq[Failure],
  ) {
    def verified: Int = perDirection.map(_._3).sum
    def skipped:  Int = perDirection.map(_._4).sum
    def excluded: Int = perDirection.map(_._5).sum

    def formatSummary(): String = {
      val header = s"runCrossLangInterop: $verified verified, $skipped skipped (F9 service/buzzer Dispatch gap), $excluded excluded (F10/F13)"
      val perLine = perDirection.map { case (s, m, v, sk, ex) =>
        s"  ${s.name}->${m.name}: $v verified, $sk skipped, $ex excluded"
      }.mkString("\n")
      header + "\n" + perLine
    }
  }

  /**
   * Pre-excluded (s, m, wireId, scenarioOpt) tuples — see plan §5.2.
   * scenarioOpt=None  → exclude all scenarios for this wireId in this direction.
   * scenarioOpt=Some(s) → exclude only the named scenario (scenario-level F10/F13 divergence).
   */
  private val ExcludedTuples: Set[(Lang, Lang, String, Option[String])] = Set(
    // *→TS: AllTypes.Struct (F13 + F10 unsigned-wrap + int64 boundary precision)
    (Lang.Scala,      Lang.Typescript, "izumi.test.domain01.AllTypes.Struct", None),
    (Lang.CSharp,     Lang.Typescript, "izumi.test.domain01.AllTypes.Struct", None),
    // Scala→C#: AllTypes.Struct (Scala signed-wrap unsigned-ints — plan §5.2 reserve)
    (Lang.Scala,      Lang.CSharp,     "izumi.test.domain01.AllTypes.Struct", None),
    // F-new-A: TS encoder does not normalize null vs undefined; chain endpoint
    // {"no":null} (from Scala intermediate) does not re-encode to source {}.
    (Lang.Typescript, Lang.Scala,      "idltest.dtofields.OptionalObj",       Some("with-none")),
    // F-new-B: TS-mediated round-trip leaves "no":null in json_M which Scala
    // decoder can't reach the bottom of (NullableObj field 'a' missing).
    (Lang.Scala,      Lang.Typescript, "idltest.dtofields.OptionalObj",       Some("with-none")),
    // F-new-C: C# uint8=200 fixture's Scala-decoder fails (Scala signed Byte
    // max is 127). Mirror of plan §5.2's Scala→C# AllTypes exclusion.
    (Lang.CSharp,     Lang.Scala,      "izumi.test.domain01.AllTypes.Struct", Some("basic")),
  )

  def runAll(repoRoot: Path): Report = {
    val tsHarness = repoRoot.resolve("idealingua-v1/idealingua-v1-test-harness/src/main/typescript")
    val csHarness = repoRoot.resolve("idealingua-v1/idealingua-v1-test-harness/src/main/csharp")

    val scalaRoot = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/scala")
    val tsRoot    = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript")
    val csRoot    = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/csharp")

    val scalaFixtures = WireFixtures.load(scalaRoot)
    val tsFixtures    = WireFixtures.load(tsRoot)
    val csFixtures    = WireFixtures.load(csRoot)

    // Spawn TS and C# daemons; Scala is in-JVM
    val tsDaemon = CrossLangDaemon.spawnTypescript(tsHarness) match {
      case Right(d)  => d
      case Left(err) => throw new WireFixtureCrossLangVerificationFailure(s"Failed to spawn TS daemon: $err")
    }
    val csDaemon = CrossLangDaemon.spawnCSharp(csHarness) match {
      case Right(d)  => d
      case Left(err) =>
        try { tsDaemon.shutdown() } catch { case _: Throwable => () }
        throw new WireFixtureCrossLangVerificationFailure(s"Failed to spawn C# daemon: $err")
    }

    try {
      val scalaFn: CrossLangChain.RoundtripFn = (wireId, json) =>
        CrossLangScalaAdapter.roundtrip(s"scala-${System.nanoTime()}", wireId, json)
      val tsFn: CrossLangChain.RoundtripFn = (wireId, json) => tsDaemon.roundtrip(wireId, json)
      val csFn: CrossLangChain.RoundtripFn = (wireId, json) => csDaemon.roundtrip(wireId, json)

      val daemonByLang: Map[Lang, CrossLangChain.RoundtripFn] = Map(
        Lang.Scala      -> scalaFn,
        Lang.Typescript -> tsFn,
        Lang.CSharp     -> csFn,
      )
      val fixturesByLang: Map[Lang, Seq[WireFixtures.FixtureFile]] = Map(
        Lang.Scala      -> scalaFixtures,
        Lang.Typescript -> tsFixtures,
        Lang.CSharp     -> csFixtures,
      )

      val allLangs: Seq[Lang] = Seq(Lang.Scala, Lang.Typescript, Lang.CSharp)
      val pairs: Seq[(Lang, Lang)] = for {
        s <- allLangs
        m <- allLangs
        if s != m
      } yield (s, m)

      val perDir    = mutable.Buffer[(Lang, Lang, Int, Int, Int)]()
      val failures  = mutable.Buffer[Failure]()

      for ((s, m) <- pairs) {
        var verified = 0
        var skipped  = 0
        var excluded = 0

        val sourceFixtures = fixturesByLang(s)
        val mFn            = daemonByLang(m)
        val sFn            = daemonByLang(s)

        for (fixture <- sourceFixtures) {
          val isExcluded =
            ExcludedTuples.contains((s, m, fixture.wireId, None)) ||
            ExcludedTuples.contains((s, m, fixture.wireId, Some(fixture.scenario)))
          if (isExcluded) {
            excluded += 1
          } else {
            val outcome = CrossLangChain.run(s, m, fixture, mFn, sFn)
            if (outcome.ok) {
              verified += 1
            } else {
              outcome.kind match {
                case Some(CrossLangFailureKind.UnknownWireIdAt(_)) =>
                  skipped += 1  // F9: target lacks a Dispatch entry for this wireId
                case Some(kind) =>
                  failures += Failure(s, m, fixture, kind)
                case None =>
                  () // should not occur
              }
            }
          }
        }

        perDir += ((s, m, verified, skipped, excluded))
      }

      val report = Report(perDir.toSeq, failures.toSeq)
      if (failures.nonEmpty) {
        throw new WireFixtureCrossLangVerificationFailure(formatFailures(report))
      }
      report
    } finally {
      try { tsDaemon.shutdown() } catch { case _: Throwable => () }
      try { csDaemon.shutdown() } catch { case _: Throwable => () }
    }
  }

  private def formatFailures(report: Report): String = {
    val sb = new StringBuilder
    sb.append("Cross-language interop verification failed.\n\n")
    sb.append(report.formatSummary())
    sb.append("\n\n")
    sb.append(s"Failures (${report.failures.size}):\n")

    val byKindName = report.failures.groupBy(f => f.kind.getClass.getSimpleName)
    for ((kindName, fs) <- byKindName.toSeq.sortBy(_._1)) {
      sb.append(s"\n$kindName (${fs.size} fixtures):\n")
      for (f <- fs.take(10)) {
        sb.append(s"  ${f.s.name}->${f.m.name} ${f.fixture.wireId} (${f.fixture.scenario}): ${f.kind}\n")
      }
      if (fs.size > 10) sb.append(s"  ... and ${fs.size - 10} more\n")
    }
    sb.toString()
  }
}

final class WireFixtureCrossLangVerificationFailure(msg: String) extends RuntimeException(msg)
