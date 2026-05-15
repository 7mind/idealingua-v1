# PR-03.2 — Layer B wire-byte fixtures, Scala leg — implementation plan

Plan author: planning subagent (review-loop, 2026-05-04 11:00).
Source briefs: `tasks.md`, `defects.md`, `defects-m1.md`, `docs/drafts/20260503-PR03-backcompat-test-harness-plan.md`, `docs/drafts/20260503-2300-PR0301-baseline-harness-impl-plan.md`.

**Orchestrator decision baked in (2026-05-04, dots-in-dirname)**: fixture path encoding uses `wire-fixtures/scala/<wireId>/<scenario>.json` where `<wireId>` is a single directory whose name contains dots (e.g. `idltest.events.TestBuzzer.EmptyInput`). User-overridable before T2.

---

## §1 Goal & non-goals

**Goal.** Single commit on `wip/necromancy` that wires the body of the existing `runWireFixtures` sbt task to drive a Layer B byte-strict round-trip test for the Scala leg only: load hand-authored JSON fixtures from disk, decode via the legacy compiler's generated Circe codecs, re-encode the resulting Scala value, and assert byte-strict equality against the original fixture. Establishes the Layer B harness pattern that PR-03.3 will mirror for the TS + C# legs and that PR-02 must not regress.

The four contractual sbt task names from PR-03 master plan §5 are already wired (PR-03.1 commit `1b8e1e2`); this PR replaces the placeholder body of `runWireFixtures` with real behaviour.

The harness must compile cross-build Scala 2.13.18 + 3.8.3 (per C3/Q8). All fixtures must round-trip identically on both Scala versions; any Circe-version-driven asymmetry between 2.13 and 3.8.3 surfaces here.

**Non-goals (explicit refusal of scope creep).**
- No Layer B for TypeScript or C# (PR-03.3).
- No Layer C cross-language interop matrix (PR-03.4).
- No negative-test corpus (PR-03.5).
- No `docs/wire-format.md` (PR-03.6).
- No new typer code; no `izumi.idealingua.typer.phase.*`.
- No edits to compiler modules (the F6 deviation in PR-03.1 was a one-shot).
- No edits to `.domain` files (per master plan §6 line 723-727 — patterns whose existing-corpus coverage is missing are deferred).
- No edits to plan docs unless surfacing a contradiction.
- No `git push`, rebase, `reset --hard`, or force-push.
- No CI yaml.
- No fixture authoring for `TBLOB` (vacuous on this corpus per F2; F5 unresolved).
- No `consts`-block fixtures (Q6 deferred).
- No `streams` fixtures (C5 — deprecated-but-keep-working, no behaviour change, no fixtures).

---

## §2 How Layer B Scala works (round-trip identity)

For each fixture file `F.json` with declared `wireId W`, the harness:

1. Reads `F.json` as `Array[Byte]`.
2. Parses bytes as `io.circe.Json` (`J = parse(F.json)`).
3. Looks up `WireDispatch.entries(W)` → `RoundTripEntry(decode, encode, …)`.
4. Decodes: `J.as[T]`. On `Left(DecodingFailure)`, fail with `(fixtureFile, wireId, "decode-failed: <message>")`.
5. Re-encodes: `value.asJson` → `J2`. Compare `J2.noSpaces.getBytes(UTF_8)` byte-for-byte against fixture bytes. **Decision (locked in §11 R-defaults):** fixtures use `Json.noSpaces` (single-line, no whitespace, UTF-8); harness asserts byte equality.
6. Authoring sanity: assert `parse(F.json).noSpaces == J2.noSpaces`. Catches whitespace-bearing fixtures.

**Failure kinds** (each surfaced with `(fixtureFile, wireId, kind)`):
- `decode-failed`: legacy decoder returned `Left(DecodingFailure)`.
- `byte-mismatch`: re-encoded bytes ≠ fixture bytes.
- `whitespace-mismatch`: fixture has whitespace it shouldn't.
- `roundtrip-divergence`: parsed-and-reprinted fixture ≠ re-encoded value.

**Aggregate.** `WireFixtureRunner.runAll` accumulates failures, throws `WireFixtureVerificationFailure(msg)` on any. Task body wraps as `MessageOnlyException` per PR-03.1-D11. **Never `sys.exit`.**

**Why byte-strict raw, not canonicalization.** L4 lock — field-order regressions are wire-affecting; canonicalization would silently accept them. Default: byte-strict raw for ALL payloads (master plan §3 lines 319-322). Canonicalization sanity check deferred to PR-03.3.

---

## §3 Module / file layout

```
./idealingua-v1/idealingua-v1-test-harness/
└── src/main/scala/izumi/idealingua/harness/
    ├── HarnessCorpus.scala          (existing — add `wireFixturesRoot(repoRoot)`)
    ├── HarnessOptions.scala         (existing)
    ├── GoldenCompile.scala          (existing)
    ├── GoldenGenerator.scala        (existing)
    ├── GoldenVerifier.scala         (existing)
    ├── HarnessMain.scala            (extend — add `WireFixturesMain`)
    ├── WireFixtures.scala           (NEW — fixture loader, FixtureFile case class)
    ├── WireFixtureRunner.scala      (NEW — round-trip executor + failure types)
    └── WireDispatch.scala           (NEW — manual Map[wireId, RoundTripEntry])

./idealingua-v1/idealingua-v1-test-defs/
├── golden/scala/                    (existing — promoted to `Compile / unmanagedSourceDirectories`)
│   └── … (PR-03.1's 214 generated Scala files become harness compile sources)
└── wire-fixtures/                   (NEW)
    └── scala/                       (per-language; only Scala in PR-03.2)
        ├── idltest.dtofields.Point/
        │   ├── basic.json
        │   └── …
        ├── idltest.identifiers.UserId/
        │   └── basic.json
        ├── idltest.algebraics.AdtTester/
        │   ├── as-ComplexAdt.json
        │   └── as-ComplexAdt2.json
        └── … (~30 dirs total)
```

**Path encoding rule.** `wire-fixtures/scala/<wireId>/<scenario>.json` — single directory level, dots preserved. Loader: `wireId = parent.fileName`, `scenario = file.fileName.stripSuffix(".json")`.

**Why per-language sub-tree** even though PR-03.2 ships only Scala? Cross-language byte-strict means a fixture byte-identical for Scala may legitimately differ for TS/C#. PR-03.3 mirrors `wire-fixtures/typescript/...` and `wire-fixtures/csharp/...`.

**Why fixtures OUTSIDE `src/main/resources/`?** Same reason as goldens — fixtures are test inputs, not production resources. Putting them on the classpath would compile-load every fixture on every sbt build.

---

## §4 Compiler integration: how generated Scala becomes available to the harness

**Decision: Option 1 + Option 2 combined.**

1. Add `golden/scala/` as `Compile / unmanagedSourceDirectories` on the harness via `sbtgen/Deps.scala`. Goldens compile into the harness's classpath.
2. Hand-write `WireDispatch.scala` listing `(wireId, decode: Json => Decoder.Result[Any], encode: Any => Json)` tuples for fixtures we author. Manual is acceptable for ≈ 30 entries; a `WireDispatchGenerator` source generator is deferred to PR-03.4 where the type matrix grows.

**`WireDispatch.scala` shape:**

```scala
package izumi.idealingua.harness

import io.circe.{Decoder, Json}
import io.circe.syntax._

private[harness] final case class RoundTripEntry(
  wireId: String,
  decode: Json => Decoder.Result[Any],
  encode: Any => Json,
)

private[harness] object WireDispatch {
  val entries: Map[String, RoundTripEntry] = Map(
    "idltest.dtofields.Point" -> RoundTripEntry(
      "idltest.dtofields.Point",
      json => json.as[idltest.dtofields.Point].asInstanceOf[Decoder.Result[Any]],
      v   => v.asInstanceOf[idltest.dtofields.Point].asJson,
    ),
    // … one entry per (wireId) we author fixtures for
  )
}
```

The `Any`-typed dispatch is necessary because the table is heterogeneous; the type info is encoded in `wireId`, not the static type.

**sbt setting (in `sbtgen/Deps.scala`).** Add to harness Artifact's settings:

```scala
"Compile / unmanagedSourceDirectories += (LocalRootProject / baseDirectory).value / \"idealingua-v1\" / \"idealingua-v1-test-defs\" / \"golden\" / \"scala\"".raw,
```

`unmanagedSourceDirectories` verified at `build.sbt:1580-1619` (existing harness module).

**Cross-build risk.** Goldens were generated by the legacy compiler with `sbt = SbtOptions.example.copy(scalaVersions = List("2.13.18", "3.8.3"))`. Scala-version-aware imports: Scala 3 uses `io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}` (`CirceTranslatorExtensionBase.scala:305`); Scala 2 uses `io.circe.derivation.{deriveDecoder, deriveEncoder}` (`CirceTranslatorExtensionBase.scala:304`). Both must compile via `+ idealingua-v1-test-harness/compile`.

---

## §5 Coverage matrix (FIRST-round fixtures, ~30)

| # | Pattern | (Domain, Type) | Why load-bearing | §4 spec topic |
|---|---|---|---|---|
| 1 | Plain DTO mixed scalars | `idltest.dtofields.Point` | Field declaration order | §4.3 |
| 2 | Identifier multi-field unnamed | `idltest.identifiers.ComplexID` | `idNameFix` with `_:` | §4.6+§4.7 |
| 3 | Identifier multi-field named | `idltest.identifiers.UserId`, `BucketID` | sorted-by-name serialization | §4.6 |
| 4 | Identifier with sub-Identifier | `idltest.identifiers.ComplexID` (nested) | parts.split parses sub-id | §4.6 |
| 5 | ADT multi-branch | `idltest.algebraics.AdtTester` (2 fixtures) | discriminator key, per-branch shape | §4.4 |
| 6 | ADT with interface branch | `idltest.algebraics.AdtWithInterface` | mixed branch shapes | §4.4 |
| 7 | Interface w/ implementing DTO | `idltest.inheritance.WithCovariance` | wireId-keyed encoding | §4.4 |
| 8 | Optional present | `idltest.dtofields.OptionalObj` (Some) | `Encoder[Option].Some` | §4.10 |
| 9 | Optional absent | `idltest.dtofields.OptionalObj` (None) | Circe omits key on None | §4.10 |
| 10 | List of structs | `idltest.dtofields.ListObj` | `Encoder[List]` | §4.10 |
| 11 | Map with string keys | `idltest.identifiers.KVIDGeneric` | `Encoder[Map[String, V]]` | §4.10 |
| 12 | Enum | `idltest.identifiers.DepartmentEnum` (Engineering, Sales) | enum string encoding | §4.5 |
| 13 | Enum inside Identifier | `idltest.identifiers.UserWithEnumId` | enum-as-id-component | §4.5+§4.6 |
| 14 | Anyval-shaped DTO | `idltest.dtofields.NullableObj` | `forProduct1` path | §4.3 (anyval) |
| 15 | Empty struct | `idltest.inheritance.Empty` | empty object | §4.3 |
| 16 | Service method input wrapper | `idltest.services.GreeterService.greet.Input` | `methodInputSuffix` | §4.7 |
| 17 | Service method singular output (unwrap) | `idltest.services.GreeterService.hello.Output` | unwrap branch | §4.8 |
| 18 | Buzzer method input wrapper | `idltest.events.TestBuzzer.empty.Input` | C5 — Buzzers first-class | §4.7 |
| 19 | Buzzer enum input | `idltest.events.TestBuzzer.enumInput.Input` | enum-in-buzzer | §4.7 |
| 20 | Buzzer ADT input | `idltest.events.TestBuzzer.adtInput.Input` | ADT-in-buzzer | §4.7 |
| 21 | jsonlike | `idltest.json.<...>` | raw `Json` field | §4 jsonlike |
| 22 | TUInt64 in struct | `izumi.test.domain01.AllTypes` | u64 → Long → JSON number | §4.9+Q4 |
| 23 | TInt64 in struct | `AllTypes` | i64 → Long | §4.9 |
| 24 | TFloat / TDouble | `AllTypes` | float/double JSON | §4.9 |
| 25 | TUUID in struct | `AllTypes` | java.util.UUID | §4.9 |
| 26 | TTsTz (UTC, ms-precision) | `AllTypes` | `IRTTimeInstances` | §4.9 |
| 27 | TList of structs (recursive) | `AllTypes.list` | nested list, self-ref | §4.10 |
| 28 | Single-element TSet | `AllTypes.selfSet` (one elem) | Set1 stable order | §4.10+Q5 |
| 29 | Cross-domain reference | `idltest.phase.<…>` → `idltest.aliases` | multi-domain wireId | §4.2 |
| 30 | Inheritance flattening (extra coverage) | `Point` extra fixture | flattening order | §4.3 |

**Out of PR-03.2 scope** (deferred or N/A):
- Single-branch ADT (#5): no corpus example.
- Interface w/ no impl (#7): no example.
- Optional-of-optional (#9), Map-of-optional (#11), long Unicode (#19), large i64/u64 boundary (#20), deep recursion (#21), single-member enum (#24): require synthesis, low priority. Defer to PR-03.2.5.
- TBLOB (#27): F2 vacuous, F5 unresolved.
- Const block (#18): Q6 deferred.
- Stream method I/O (#16): C5 deprecated.

---

## §6 Fixture authoring conventions

- **Format**: single JSON value, `Json.noSpaces` form, UTF-8, single-line, no trailing newline.
- **No in-file metadata**: `wireId` from path, `scenario` from filename. Optional sibling `<scenario>.notes.md` for human notes.
- **Naming**: lowercase with hyphens. Common: `basic.json`, `empty.json`, `with-<edge>.json`. ADT: `as-<BranchType>.json`. Optional: `with-some.json`, `with-none.json`.
- **Key order**: case-class declaration order (per `CirceTranslatorExtensionBase.scala:281` `deriveEncoder` + `StructuralQueriesImpl.structure(id)` field flattening). For `Point`: `(w, h, id, name, x, y, ownfield, export)`.
- **Time values**: UTC zone, ms-precision (`"2025-01-15T10:30:45.000Z"`).
- **Floats**: avoid NaN/Infinity (Circe emits `null` → round-trip fails). Use exact-representable IEEE-754 (`1.5`, `0.0`, `-2.25`). Avoid `1.0` (Circe ambiguity).

**Sample sketches:**

```json
// wire-fixtures/scala/idltest.dtofields.Point/basic.json
{"w":10,"h":20,"id":"abc","name":"point-1","x":3,"y":4,"ownfield":"of","export":true}
```
```json
// wire-fixtures/scala/idltest.identifiers.UserId/basic.json
"UserId#3a7f0c12-1234-5678-9abc-fedcba987654:0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9"
```
```json
// wire-fixtures/scala/idltest.algebraics.AdtTester/as-ComplexAdt.json
{"ComplexAdt":{"id":"AdtTestID#alpha"}}
```
```json
// wire-fixtures/scala/idltest.dtofields.OptionalObj/with-none.json
{}
```
```json
// wire-fixtures/scala/idltest.dtofields.OptionalObj/with-some.json
{"no":{"a":42}}
```

---

## §7 `runWireFixtures` task wiring

**Task body** (replaces placeholder in `sbtgen/Deps.scala`):

```scala
"runWireFixtures" := """{
                       |  val log      = streams.value.log
                       |  val repoRoot = (LocalRootProject / baseDirectory).value.getAbsolutePath
                       |  log.info("runWireFixtures: starting")
                       |  val cp = (Compile / fullClasspath).value.files
                       |  val r  = (Compile / runner).value
                       |  r.run("izumi.idealingua.harness.WireFixturesMain", cp, Seq(repoRoot), log)
                       |    .failed.foreach(e => throw new MessageOnlyException(e.getMessage))
                       |  log.info("runWireFixtures: all fixtures match")
                       |}""".stripMargin.raw,
```

**`HarnessMain.scala` extension:**

```scala
object WireFixturesMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: WireFixturesMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = Paths.get(args(0))
    WireFixtureRunner.runAll(HarnessCorpus.wireFixturesRoot(repoRoot))
  }
}
```

`HarnessCorpus.wireFixturesRoot(repoRoot) = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures")`.

**Coverage cross-check** (Layer A ↔ Layer B): DEFERRED. The two layers are independent (Layer A = source-text equality; Layer B = JSON round-trip). Compile-time reference inside `WireDispatch.scala` already requires the type to exist in goldens.

---

## §8 TUInt64 / TSet / TBLOB audit results

**TUInt64.** Two corpus uses: `izumi/test/domain01.domain:86` (`uint64: u64` on `AllTypes`) and `izumi/test/domain02.domain:131` (service output). Scala mapping: `Long`. Circe writes JSON number. Scala leg: `u64 = 1234567890L` round-trips byte-identically. Q4 boundary fixture (`uint64 = 9007199254740993L = 2^53 + 1`) included to lock Scala behaviour; Q4 cross-language policy is PR-03.4's concern.

**TSet.** `izumi/test/domain01.domain:93` (`selfSet: set[AllTypes]`) and `idltest/services.domain:104` (`!! set[ErrorData]`). Scala: `scala.collection.immutable.Set[T]`. `Set1`/`Set2`/`Set3`/`Set4` iterate in insertion order; `HashSet` does not (cross-platform). **PR-03.2 restricts to one-element TSet fixtures.** Multi-element deferred to PR-03.4 / Q11.

**TBLOB.** Zero matches in `main-tests/source/`. F2 vacuous. **Excluded from PR-03.2.**

---

## §9 Negative fixtures

**Recommendation: out of scope; defer to PR-03.5.**

- Master plan §12 PR-03.6 (= our PR-03.5) is the dedicated negative-test PR.
- L5 locks "legacy throws something" — binary outcome, low authoring leverage.
- 30+ positive fixtures already, doubling for negatives fragments review.

Exception: T4 smoke test (corrupt + revert) doubles as a single in-execution negative-path sanity check. No checked-in negative fixture file.

---

## §10 Field-order audit (legacy `deriveEncoder` preserves declaration order)

**Verification chain:**
1. Case-class field order: `extractor.extractFields(defn)` post-sorted by `(distance, definedBy.toString, -definedWithIndex)` then `.reverse` at `StructuralQueriesImpl.scala:41`. Deterministic post-F6 fix.
2. Scala translator emits fields in `output.all` order: `golden/scala/idltest/dtofields/Point.scala:5` shows `(w, h, id, name, x, y, ownfield, export)` — matches master plan §6 row 1.
3. Circe `deriveEncoder` (Scala 2 `io.circe.derivation`; Scala 3 `io.circe.generic.semiauto`) emits keys in case-class declaration order — well-documented Circe semantics.
4. Empirical: PR-03.1's 700-file sha256 reproducibility audit confirmed encoder output stable.

**Conclusion:** field order = case-class declaration order = `Struct.all`. Fixtures matching this order round-trip byte-identically.

**ZonedDateTime sub-issue.** `IRTTimeInstances.scala:135-136` encoder converts to UTC before emission. Fixtures MUST use UTC zone.

---

## §11 Risks & open questions

- **R1 — Goldens-on-classpath cross-build cost.** 214 files added to harness compile path on both Scala versions. Compile time grows; not a blocker. Incremental compile mitigates re-run cost. **Unblocks**: T1 first compile.

- **R2 — Scala 2.13 vs 3.8.3 Circe asymmetry.** Different `import` for Circe derivation. Empirically `asJson.noSpaces` is byte-identical, but a future Circe upgrade could break this. **Unblocks**: T3 fixture author runs `+ runWireFixtures` to confirm.

- **R3 — Buzzer / Service ephemeral wireIds.** Verify `wireId` for ephemeral DTOs at fixture-authoring time using `golden/scala/.../<TypeName>.scala`'s package. Example: `idltest.events.TestBuzzer.EmptyInput`.

- **R4 — Set iteration order.** Empirically `Set2`/`Set3`/`Set4` iterate in insertion order; not guaranteed. PR-03.2 restricts to one-element sets.

- **R5 — Anyval Scala 3 emit path.** `golden/scala/idltest/dtofields/NullableObj.scala:9` uses `Encoder.forProduct1` — single-key `Encoder.AsObject`. Should round-trip; empirical confirm at T2.

- **R6 — Sealed-trait subtype encoder imports.** WireDispatch needs `import idltest.algebraics.AdtTester.given`/`._` to bring sealed-trait encoder into scope. Use Scala-2-form (`._`) for cross-build.

- **R-defaults (locked at T1 by orchestrator unless user objects):**
  - Fixture path encoding: dots-in-dirname.
  - TUInt64 boundary fixture: include `9007199254740993L` against `AllTypes`.
  - Cross-build T5 criterion: `+ runWireFixtures` must pass on both Scala versions.
  - Negative fixtures: deferred to PR-03.5.
  - Output wording: mirror `verifyGoldens` (`"runWireFixtures: all <N> fixtures match"`).

---

## §12 Sub-task breakdown

### T1 — Scaffold WireDispatch + classpath integration

**Scope:**
- Edit `sbtgen/Deps.scala`: add `Compile / unmanagedSourceDirectories +=` for `idealingua-v1-test-defs/golden/scala`. Modify `runWireFixtures` task body — but keep placeholder for now; T1 only adds source root.
- Run `./sbtgen.sc --js`. Verify `git diff project/plugins.sbt` empty.
- Add empty skeletons for `WireFixtures.scala`, `WireFixtureRunner.scala`, `WireDispatch.scala` (package decl + empty object) so harness compiles.
- Verify `sbt -batch idealingua-v1-test-harness/compile` (Scala 3) and `sbt -batch '++ 2.13.18 idealingua-v1-test-harness/compile'` (Scala 2.13) succeed with goldens on classpath.

**Success criterion:** harness compiles cross-build with goldens on classpath. No golden file emits a compile error.

**Files touched:** `sbtgen/Deps.scala`, `build.sbt`, `project/plugins.sbt` (regenerated), three new harness skeleton files.

### T2 — WireFixtures + WireFixtureRunner + WireDispatch + WireFixturesMain

**Scope:**
- Implement `WireFixtures.load(root: Path): Seq[FixtureFile]` — walks the tree, returns `Seq(FixtureFile(wireId, scenario, bytes))`.
- Implement `WireFixtureRunner.runAll(root: Path): Unit` — for each fixture, look up `WireDispatch.entries(wireId)`, decode + re-encode, accumulate failures, throw `WireFixtureVerificationFailure` if any.
- Add `WireFixturesMain` entry point in `HarnessMain.scala`.
- Wire `runWireFixtures` task body to invoke `WireFixturesMain` (replace placeholder).
- Populate `WireDispatch.entries` with the ~30 entries from §5.
- Run sbtgen --js; regenerate build.sbt.

**Success criterion:** with no fixtures on disk, `sbt runWireFixtures` runs to clean exit (zero fixtures = trivially successful). Compile passes both Scala versions.

**Files touched:** all of §3's harness Scala files filled in. `sbtgen/Deps.scala`, `build.sbt` (task body update).

### T3 — Fixture corpus authoring

**Scope:** hand-author the ~30 fixtures listed in §5. For each:
1. Construct typed Scala value (use `sbt console` or a one-shot main class to discover canonical encoded JSON).
2. Save to `wire-fixtures/scala/<wireId>/<scenario>.json` as `Json.noSpaces` form.
3. Run `sbt runWireFixtures` after each batch (~5-10 fixtures) to confirm round-trip.

**Discipline:**
- Use §5 row-by-row checklist.
- For Identifier fixtures, double-check `parts` order matches alphabetical-by-field-name (e.g. `BucketID#<app>:<bucket>:<user>`).
- For `Point`-style structs, verify field-order matches case-class declaration.

**Success criterion:** `sbt runWireFixtures` clean; `+ runWireFixtures` clean (cross-build).

**Files touched:** ~30 JSON files under `idealingua-v1/idealingua-v1-test-defs/wire-fixtures/scala/<wireId>/<scenario>.json`.

### T4 — Smoke test

**Scope:** corrupt one byte of one fixture (e.g. `Point/basic.json`), run `sbt runWireFixtures`, confirm `byte-mismatch` failure with structured message naming the file. Revert.

**Success criterion:** failure mode demonstrated; `git status` clean after revert.

**Files touched:** none committed.

### T5 — Final commit

**Scope:** single commit on `wip/necromancy` containing T1+T2+T3 changes. Commit message references PR-03.2.

**Success criterion:**
- `sbt regenerateGoldens && sbt verifyGoldens` (PR-03.1 contracts) still pass.
- `sbt runWireFixtures` passes.
- `sbt + runWireFixtures` (cross-build) passes.
- `sbt + idealingua-v1-test-harness/compile` (cross-build) passes.

**Files touched:** all from T1-T3 plus `tasks.md` (mark PR-03.2 `[x]`, add Completed entry).
