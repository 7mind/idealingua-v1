# idealingua-v1 modernization — Defect Ledger (Implementation phase, M2+)

Adversarial-review findings for the implementation milestone.

The M1 plan-document audit trail (PR-01, PR-02, PR-03) lives in `./defects-m1.md` — that ledger is closed and append-only. Do not extend it.

Status: `[ ]` open · `[~]` under fix · `[x]` resolved

---

## PR-03.1

### T1 (scaffold module)

## [PR-03.1-D01] Placeholder task bodies for `runWireFixtures` / `runCrossLangInterop` deviate from plan §3 wording
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/sbtgen/Deps.scala:481-482
**Description:** Plan `docs/drafts/20260503-2300-PR0301-baseline-harness-impl-plan.md:135, 139` specifies the placeholder bodies print exactly `"runWireFixtures: placeholder — implemented in PR-03.2"` and `"runCrossLangInterop: placeholder — implemented in PR-03.4"`. The shipped strings instead said `"runWireFixtures: placeholder — Layer B = PR-03.2"` and `"runCrossLangInterop: placeholder — Layer C = PR-03.4"`. PR-03 master plan §5 only constrains task *names*, not message content, so this was plan-vs-implementation prose drift.
**Fix:** Updated the two stub strings in `sbtgen/Deps.scala:481-482` to `"runWireFixtures: placeholder — implemented in PR-03.2"` and `"runCrossLangInterop: placeholder — implemented in PR-03.4"`. Re-ran `./sbtgen.sc --js` to regenerate `build.sbt:1695-1696`. Verified the four printed messages now match the plan exactly. `project/plugins.sbt` unchanged (zero diff) — confirmed.

## [PR-03.1-D02] Stub bodies for `regenerateGoldens` / `verifyGoldens` exit-0 silently if T2/T3 forgets to update them
**Status:** resolved (deferred to T2 reviewer; by-design per plan §12 line 448)
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/sbtgen/Deps.scala:479-480
**Description:** After T2/T3 ships, an executor who forgets to update the task body but adds the helper objects (`GoldenGenerator`, `GoldenVerifier`) elsewhere would still see `sbt regenerateGoldens` succeed silently. T1 plan §12 line 448 mandates `prints the no-op message and exits 0`, so T1 honours the contract; the footgun is T2's risk.
**Fix:** Acknowledged, no T1 change. T2 reviewer is responsible for verifying that T2's executor replaced the stub body, and that running `sbt regenerateGoldens` exhibits the new behaviour (writing files to `golden/`) rather than the T1 println.

## [PR-03.1-D03] Dead `Test/*` config in a no-test module is sbtgen-template residue, not executor error
**Status:** resolved (no action; sbtgen template artifact)
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/build.sbt:1590-1599, 1610-1619, 1620
**Description:** Harness has no test sources. The four `Test / unmanagedSourceDirectories ++=` blocks and `Test / testOptions += Tests.Argument("-oDF")` are inert. They are sbtgen-injected defaults — every artifact gets them — so the inertness is not an executor error. Harmless.
**Fix:** Accepted as sbtgen template artifact. No T1 action.

## [PR-03.1-D04] `refreshFlakeTask` whitespace realignment is a deterministic side-effect of the new Import block
**Status:** resolved (no action; deterministic)
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/build.sbt:11
**Description:** Adding 4 new taskKey declarations to the same `Import("…\n…")` block in `sbtgen/Deps.scala:504-509` causes sbtgen to column-align the `=` signs across all 5 lines, including the pre-existing `refreshFlakeTask`. Mechanical realignment, not a behaviour change.
**Fix:** Accepted as autogen drift consequence. No action.

## [PR-03.1-D05] `private[harness] object Placeholder` may trigger CI `-Wunused:all` in Scala 3
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/Placeholder.scala:3
**Description:** Scala 3.8.3 with `-Wunused:all` (build.sbt:1662) plus `-Wconf:any:error` (active when `insideCI.value=true`, build.sbt:1669) MAY flag a top-level `private[harness] object Placeholder` with no callers as an unused private member. Local non-CI compile passed because non-CI demotes to warning. CI mode unverified. T2 makes the question moot by adding helper objects, but the placeholder lingers until then.
**Fix:** Dropped the `private[harness]` access modifier. File content now: `package izumi.idealingua.harness` then `object Placeholder` (no qualifier). Cross-build verified on Scala 3.8.3 and 2.13.18.

## [PR-03.1-D06] T2 footgun: `.raw` Scala strings in `Deps.scala` for task bodies are brittle to refactor
**Status:** resolved (forward-looking guidance for T2; no T1 change)
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/sbtgen/Deps.scala:478-483
**Description:** Plan §3 line 117 sketches T2 wiring as `regenerateGoldens := { … izumi.idealingua.harness.GoldenGenerator.regenerate(corpusRoot, goldenRoot) }`. T1 inlines the body as a `.raw` Scala-source string. T2 will need to either keep the body as a `.raw` string (verbose, hard-to-refactor) or restructure. Inline `.raw` Scala in `Deps.scala` strings is brittle.
**Fix:** No T1 change. T2 should consider extracting the body into a small `harness.TaskBodies` Scala object that lives outside the `.raw` string, with the `.raw` body simply being `izumi.idealingua.harness.TaskBodies.regenerate()`. Recorded as guidance for the T2 executor.

## [PR-03.1-D07] `println` writes to stdout, bypassing sbt's task log
**Status:** resolved (forward-looking guidance for T2; T1 stubs OK)
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/build.sbt:1693-1696
**Description:** `println` writes to JVM stdout. Sbt's `streams.value.log.info(...)` routes via the task log (with task-prefix, level, log-file capture). T1 stubs are acceptable as `println`; T2 production code must use `streams.value.log` so progress and errors are captured by sbt's logging infrastructure.
**Fix:** No T1 change. T2 must replace `println(...)` with `streams.value.log.info(...)` (or `.error(...)` for failure paths) when wiring real bodies.

## [PR-03.1-D08] `pattern var charIn` silencer is fastparse-specific and dead in the harness
**Status:** resolved (no action; sbtgen template artifact)
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/build.sbt:1685-1686
**Description:** `scalacOptions += "-Wconf:msg=pattern var charIn:silent"` is fastparse-specific, injected globally. Harness has no fastparse usage; silencer is dead. Same template artifact as D03.
**Fix:** Accepted as sbtgen template artifact. No action.

### T2 / T3 (regenerateGoldens / verifyGoldens)

## [PR-03.1-D09] Legacy `ScalaTranslator.translate()` emits package-object aliases in HashMap-iteration order — non-deterministic across JVM instances
**Status:** resolved (user-approved deviation 2026-05-04: keep the fix; "purely additive" constraint from job.md relaxed for this minimal architecturally-justified change)
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/ScalaTranslator.scala:33-38 (pre-fix). Affects goldens for alias-declaring domains: `idltest/aliases`, `idltest/aliases2`, `izumi/test/domain01`, `izumi/test/domain02`, `izumi/test/domain03recursive01` (and any future `.domain` declaring `alias`).
**Description:** `ScalaTranslator.translate()` collected aliases into `.toMultimap` (returns `Map[ModuleId, Seq[Option[Member]]]`, i.e. an immutable HashMap), then iterated the HashMap to emit one `package-object.scala` per `ModuleId`. HashMap iteration order is determined by key hashCode buckets, which can vary across JVM instances when `String.hashCode` happens to be sensitive to JIT-side micro-state. Empirically, two sequential `sbt -batch regenerateGoldens` invocations produced package-object.scala files whose `type AliasName = …` lines were ordered differently — confirming the iteration order is not stable cross-JVM. With non-stable goldens, Layer A's byte-strict contract is unenforceable: the freeze tag would baseline whichever ordering the regen-machine's JVM happened to emit, and PR-02's typer rewrite cannot meaningfully target "the same legacy ordering".
**Root cause:** `ScalaTranslator.scala:33` `.toMultimap.view.mapValues(_.flatten.toSeq)` — `.toMultimap` is a `IzCollections` extension that returns an immutable HashMap, not a deterministic linear order. `.view.mapValues(...)` does not reorder, so the iteration order leaks to the `aliases.map { case (id, content) => ... }` consumer at `:43`.
**Fix:** T2/T3 executor patched the function (13-line surgical change) to replace `.toMultimap.view.mapValues(_.flatten.toSeq)` with `.groupBy(_._1).toSeq.sortBy(_._1.toString).map { case (id, pairs) => id -> pairs.flatMap(_._2) }`. Sorts the distinct ModuleIds by `toString` (stable across JVMs), preserves declaration order within each group via `flatMap` over the original pair-sequence. Generated content is unchanged byte-by-byte for the per-group emission; only cross-group ordering becomes deterministic. Verification: 3 sequential `sbt -batch verifyGoldens` runs from cold sbt-JVM start, byte-strict pass on all 700 generated files. Two `sbt regenerateGoldens` runs followed by sha256-set diff — zero diff lines across 700 files. User approved the deviation 2026-05-04 ("Keep the fix. Forget 'purely additive' - that's acceptable").

## [PR-03.1-D10] Architecture decision: `runner.value.run(mainClass, …)` task-body pattern instead of direct method calls
**Status:** resolved (acceptable; documented for future PR-03.x work)
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/sbtgen/Deps.scala:479-480 (taskKey bodies); /home/pavel/work/safe/idealingua-v1/idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/HarnessMain.scala (entry points)
**Description:** Plan §3 sketched the task body as a direct call: `regenerateGoldens := { ... izumi.idealingua.harness.GoldenGenerator.regenerate(...) }`. Direct calls fail at sbt load time because the task body is compiled against the meta-build classpath (in `project/`), which does not contain the harness module's compiled classes. The executor used the idiomatic sbt pattern: define `RegenerateMain` and `VerifyMain` as `def main(args: Array[String]): Unit` entry points in the harness module, and invoke via `(Compile / runner).value.run(mainClass, (Compile / fullClasspath).value.files, Seq(repoRoot), log)` from the task body.
**Fix:** Accepted. The harness module's main sources stay sbt-library-free (no `import sbt._`); sbt's logging happens via `streams.value.log.info(...)` in the task body; the `runner.value.run` machinery passes the harness's `Compile / fullClasspath` to a fresh classloader (does NOT fork by default — same sbt JVM, fresh classloader scope). This pattern is stable, well-supported, and idiomatic. Recorded for T4 commit prep. Plan §3's direct-call sketch should be considered superseded by this architecture.

### Final review pass (full PR-03.1 diff)

## [PR-03.1-D11] `VerifyMain.main` calls `sys.exit(1)` on verification failure, terminating the sbt JVM
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/HarnessMain.scala:29-32
**Description:** sbt 1.12 removed `TrapExit` (JDK 17+ deprecates `SecurityManager`); `Run$.executeTrapExit` is now an alias for `executeSuccess`. The harness's `try { ... } catch { case e: GoldenVerificationFailure => System.err.println(e.getMessage); sys.exit(1) }` therefore called `java.lang.System.exit(1)` directly, terminating the sbt daemon/CLI JVM rather than failing the task cleanly.
**Fix:** Removed the `try/catch` from `VerifyMain.main`. `GoldenVerificationFailure` now propagates to `runner.run`'s `Try.apply` wrapper, returning `Try.Failure`; the task body's `.failed.foreach(e => throw new MessageOnlyException(e.getMessage))` converts it to a clean sbt task failure with the structured diff text. Smoke test verified: corrupt a golden → `sbt -batch verifyGoldens` exits 1 with `[error] (idealingua-v1-test-harness / verifyGoldens) ... Golden verification failed. ... Mismatched goldens (1 files — run regenerateGoldens to update them): Golden mismatch: scala/idltest/aliases/package-object.scala` in the error log; sbt daemon stays live.

## [PR-03.1-D12] `GoldenGenerator.deleteRecursively` deletes entries during `Files.list().forEach()` traversal — implementation-defined behavior
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/GoldenGenerator.scala:35-45
**Description:** `Files.list(path).forEach(deleteRecursively)` deleted children while the parent's `DirectoryStream` was still being iterated — implementation-defined per POSIX; brittle on tmpfs / NFS / ZipFileSystem / Windows.
**Fix:** Replaced with `Files.walk(root).sorted(java.util.Comparator.reverseOrder()).forEach(Files.delete)` — materializes the entire path tree first, deletes in post-order via reverse path sort. Stream closed in `finally`. Cross-build verified on Scala 2.13.18 + 3.8.3.

## [PR-03.1-D12] `GoldenGenerator.deleteRecursively` deletes entries during `Files.list().forEach()` traversal — implementation-defined behavior
**Status:** under fix
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/GoldenGenerator.scala:35-45
**Description:** `Files.list(path)` opens a `DirectoryStream` (on Linux backed by `readdir(2)`). Deleting entries while the stream is iterating is implementation-defined per POSIX: glibc may revisit a deleted entry, or skip a sibling appended after `opendir`. For ext4 + the harness's use (no concurrent writes) this is empirically benign — the 700-file two-run sha256 audit confirms it works. But the pattern is brittle on tmpfs, NFS, ZipFileSystem, or future Windows port; failure mode is `NoSuchFileException` from a re-emitted deleted entry, leaving the parent half-deleted.
**Suggested fix:** Replace the recursive `Files.list().forEach(deleteRecursively)` pattern with the standard idiom `Files.walk(path).sorted(java.util.Comparator.reverseOrder()).forEach(Files.delete)` — collects the path tree first (closing the stream before mutation), then deletes in post-order via reverse path sort. Or use `Files.walkFileTree` with a `SimpleFileVisitor` that overrides `visitFile` and `postVisitDirectory` to call `Files.delete`.

## [PR-03.1-D13] Other transpiler `groupBy` / `toMap` iteration patterns parallel F6/D09 — residual cross-machine determinism risk
**Status:** resolved (deferred to follow-up F7; out of PR-03.1 scope per user's per-case approval boundary)
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/totypescript/TypeScriptImports.scala:41-50 (primary); also `…/totypescript/TypeScriptImports.scala:212`, `…/tocsharp/CSharpImports.scala:112`, `…/toscala/ServiceRenderer.scala:65`, `…/toscala/layout/ScalaLayouter.scala:52`.
**Description:** Same architectural pattern as F6/D09: `imports.filterNot(...).groupBy(_.pkg).map(...).mkString("\n")` and similar produce TS/C#/Scala output in `immutable.HashMap` iteration order. Empirical 700-file sha256 audit on this machine across two cold sbt-JVM invocations shows determinism, but that does not prove cross-machine determinism. The freeze-tag baseline contract rests on the goldens being byte-reproducible from any JVM. If a second host produces different bucket placements (different stdlib version, different JIT, different host hash randomization), `verifyGoldens` could flake on CI or downstream contributors.
**Fix:** Out of PR-03.1 scope. F6/D09's per-case user approval was specifically for the empirically-reproduced ScalaTranslator non-determinism. The TS/C#/ServiceRenderer/ScalaLayouter sites have NOT been observed to flake. Tracked as new follow-up F7 in `tasks.md`; if CI flakes on a different host, we extend the F6-style sort fix to these sites. No PR-03.1 change.
