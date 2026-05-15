# PR-03.5 — Negative-test corpus — implementation plan

Plan author: planning subagent (review-loop, 2026-05-08).
Source briefs: tasks.md, defects.md, master plan §7, L5 lock.

**Pre-locked decisions (planner-recommended):**
- Negative corpus path: `idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/negative/<topic>/<name>/` (sibling to positive corpus, outside Layer A walk).
- Test harness: `NegativeSpec` ScalaTest under `idealingua-v1-test-harness/src/test/scala/`. Run via `sbt idealingua-v1-test-harness/test`. **No new sbt task** (the four contractual names are locked).
- Assertion shape: `intercept[Throwable]` (per L5 — any exception qualifies; diagnostic-kind assertions deferred to PR-02).
- Per-fixture sub-directory layout (`<topic>/<name>/`) so multi-file negatives (cyclic imports across two `.domain` files) work.
- `.must-reject` zero-byte markers per master-plan §7 line 784. PR-02 will overwrite with expected diagnostic kinds.

---

## §1 Goal & non-goals

**Goal.** Single commit on `wip/necromancy` adding a negative-test corpus and harness assertions. For each malformed `.domain` input, assert that the legacy compiler produces *some* failure (any thrown exception during loader/typer/verifier).

**In scope.**
- Negative `.domain` fixtures (one per category), checked in under a sibling-of-positive corpus root.
- `NegativeSpec` ScalaTest in the harness module that walks the negative corpus and asserts "throws something".
- Per-fixture sub-directory layout supporting multi-file fixtures.
- Zero-byte `.must-reject` markers (PR-02 fills later).

**Out of scope (locked).**
- Diagnostic-kind assertions (L5; PR-02 tightens).
- Substring-matching against `IDLException` messages.
- New sbt task names (locked: only `regenerateGoldens`, `verifyGoldens`, `runWireFixtures`, `runCrossLangInterop`).
- Negatives that exercise transpiler-only paths (TBLOB-on-C#).
- Cross-language negative parity (Layer C is positive-only).
- Service/buzzer-only negatives that are transpiler-level (`???` stubs).

---

## §2 Where negatives live

`idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/negative/<topic>/<name>/<...>.domain` (+ `<name>.must-reject` marker).

**Rationale:**
- Sibling to positive corpus at `defs/main-tests/source/idltest/`.
- OUTSIDE Layer A's `regenerateGoldens` walk (`HarnessCorpus.corpusRoot` resolves only `defs/main-tests/source`).
- Resource module already on harness classpath.

**Per-fixture sub-directory** (not flat) for multi-file negatives:
```
defs/negative/cyclic-imports/two-domains-import-each-other/a.domain
defs/negative/cyclic-imports/two-domains-import-each-other/b.domain
defs/negative/cyclic-imports/two-domains-import-each-other/two-domains-import-each-other.must-reject
```

The marker file is content-irrelevant in PR-03.5 (its presence is the assertion that "this corpus expects rejection"). PR-02 will overwrite it with expected diagnostic kinds.

---

## §3 Negative corpus enumeration (~12 fixtures)

| # | Topic | Fixture | Expected legacy reject site |
|---|-------|---------|---|
| 1 | `syntax-malformed` | `unclosed-brace` | Parser |
| 2 | `syntax-malformed` | `bad-token` | Parser |
| 3 | `duplicate-member` | `dto-with-two-x-fields` | `DuplicateMemberRule` (`TypespaceVerifier.scala:10`) |
| 4 | `duplicate-member` | `enum-with-two-same-members` | `DuplicateMemberRule` |
| 5 | `cyclic-inheritance` | `mixin-a-extends-b-extends-a` | `CyclicInheritanceRule` (`TypespaceVerifier.scala:15`) |
| 6 | `cyclic-imports` | `two-domains-import-each-other` | `CyclicImportsRule.auto` (`TypespaceVerifier.scala:16`) |
| 7 | `cyclic-usage` | `dto-references-itself-non-optional` | `CyclicUsageRule` (`TypespaceVerifier.scala:14`) |
| 8 | `adt-conflicts` | `two-branches-same-name` | `AdtConflictsRule` (`TypespaceVerifier.scala:13`) |
| 9 | `adt-members` | `adt-with-no-members` | `AdtMembersRule` (`TypespaceVerifier.scala:11`) |
| 10 | `naming-conventions` | `lowercase-type-name` | `BasicNamingConventionsRule` (`TypespaceVerifier.scala:12`) |
| 11 | `undefined-reference` | `field-uses-unknown-type` | `IDLTyper` (`IDLTyper.scala:400`) |
| 12 | `id-with-non-scalar` | `id-field-typed-as-list` | `IDLTyper` (`IDLTyper.scala:157`) |

**Verification protocol.** Before committing each fixture, the implementer manually invokes `loadAndCompile(<negativeDir>)` against the legacy compiler and confirms it throws. Any fixture that does NOT throw is excluded and surfaces as a follow-up.

---

## §4 Test harness shape

**ScalaTest spec** under `idealingua-v1-test-harness/src/test/scala/izumi/idealingua/harness/NegativeSpec.scala`:

```scala
package izumi.idealingua.harness

import org.scalatest.funsuite.AnyFunSuite

final class NegativeSpec extends AnyFunSuite {
  private val repoRoot     = HarnessCorpus.repoRootForTests()
  private val negativeRoot = HarnessCorpus.negativeRoot(repoRoot)

  for (caseDir <- HarnessCorpus.listNegativeCases(negativeRoot)) {
    val displayName = negativeRoot.relativize(caseDir).toString
    test(s"legacy compiler rejects $displayName") {
      intercept[Throwable] {
        HarnessCorpus.loadCorpus(caseDir)
      }
    }
  }
}
```

**Per L5**: `intercept[Throwable]` matches any thrown exception. No subclass / message / phase assertions.

Run via `sbt idealingua-v1-test-harness/test`. Does NOT add a 5th sbt task.

---

## §5 Loader/typer entry point

The chain that must throw:
1. `LocalModelLoaderContext.loader.load()` — parser failures.
2. `ModelResolver.resolve(loaded)` — typer/verifier failures surface as `LoadedDomain.{TyperFailed, VerificationFailed}`.
3. `.throwIfFailed()` — converts any `Failure` to `IDLException`.

The existing `HarnessCorpus.loadCorpus` does exactly this. Reuse it. The negative spec invokes `loadCorpus(caseDir)` and asserts it throws.

For multi-`.domain`-file negatives, the case dir IS the corpus root for that fixture. `LocalModelLoaderContext` walks recursively. One case-dir = one isolated load.

---

## §6 sbtgen / Deps / build.sbt changes

**Test-source-root.** sbt's default convention provides `src/test/scala` automatically. No Deps.scala change.

**ScalaTest dependency.** Already declared (`build.sbt:1560`). No change.

**`HarnessCorpus` extensions** (in `src/main/scala/...`):
- `negativeRoot(repoRoot: Path): Path` — resolves `…/defs/negative`.
- `listNegativeCases(negativeRoot: Path): Seq[Path]` — enumerate by scanning for `.must-reject` markers.
- `repoRootForTests(): Path` — derives repo root from `user.dir` (sbt default cwd).

**No changes** to `regenerateGoldens` / `verifyGoldens` / `runWireFixtures` / `runCrossLangInterop` task bodies.

---

## §7 Risks

- **R1**: A negative fixture unexpectedly succeeds. Mitigation: hand-verify each before commit.
- **R2**: Parser more permissive than expected for naming-conventions. L5-neutral; replace if needed.
- **R3**: Multi-file fixtures (cyclic-imports). Verify both `.domain` declarations import each other and both target `CyclicImportsRule`.
- **R4**: `LocalModelLoaderContext` shared classpath. Verified safe — explicit `Seq[Path]`.
- **R5**: Other code scanning `defs/**` recursively. Quick T1 audit.
- **R6**: ScalaTest test-name uniqueness. Filesystem enforces `<topic>/<name>` uniqueness.

---

## §8 Sub-task breakdown

### T1 — Scaffold + sample (1 negative, 1 spec)
- Create `idealingua-v1-test-harness/src/test/scala/izumi/idealingua/harness/NegativeSpec.scala`.
- Add 1 sample fixture: `defs/negative/duplicate-member/dto-with-two-x-fields/m.domain` + `.must-reject` marker.
- Extend `HarnessCorpus` with `negativeRoot`, `listNegativeCases`, `repoRootForTests`.
- Audit (R5): `command grep -rn "resources/defs" idealingua-v1/` to confirm no positive code walks `defs/**`.
- Run `sbt idealingua-v1-test-harness/test` — observe one passing test.
- Smoke-flip: temporarily mutate fixture to be valid → test fails. Revert.

### T2 — Author full corpus
- Author remaining ~11 fixtures per §3.
- Hand-verify each throws on legacy compiler before commit.
- Drop any that don't throw (surface as follow-up for PR-02).
- Run `sbt idealingua-v1-test-harness/test`; confirm all pass.

### T3 — Cross-build verification
- `sbt ++2.13.18 idealingua-v1-test-harness/test` — passes.
- `sbt ++3.8.3 idealingua-v1-test-harness/test` — passes.

### T4 — Smoke
- Mutate one fixture to be valid; observe corresponding test fails. Revert.
- Delete a `.must-reject` marker; observe `listNegativeCases` excludes that case. Re-add.

### T5 — Final commit
- Single commit. Update `tasks.md` PR-03.5 → `[x]` + Completed entry.
