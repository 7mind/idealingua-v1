# PR-03.3a — Layer B wire-byte fixtures, TypeScript leg — implementation plan

Plan author: planning subagent (review-loop, 2026-05-04 12:00).
Source briefs: tasks.md, defects.md, defects-m1.md, master plan §3 lines 232-391, §11 Q8, §12 PR-03.4 lines 1199-1210; PR-03.2 plan; PR-03.2 commit `e8425fb`.

**Orchestrator decisions baked in (2026-05-04, planner-recommended):**
- Per-language fixture corpus: `wire-fixtures/typescript/<wireId>/<scenario>.json`, authored independently from Scala. Cross-language byte parity is Layer C (PR-03.4) territory; PR-03.3a only requires TS round-trip on TS.
- One-shot `tsx` driver with stdin batch protocol. Daemon mode deferred to PR-03.4.
- IRT runtime resolution via tsconfig `paths` + `rootDirs`; fallback to scratch-tree copy if tsc resolution proves brittle.
- ~17 wireIds in TS dispatch (PR-03.2's 22 minus 5 service/buzzer types whose TS classes are module-private). Tracked as F9 follow-up.

---

## §1 Goal & non-goals

**Goal.** Single commit on `wip/necromancy` extending the existing `runWireFixtures` sbt task to ALSO drive a Layer B byte-strict round-trip test for the TypeScript leg: load JSON fixtures from `wire-fixtures/typescript/`, run them through a node-hosted TS driver that uses the legacy compiler's generated TS classes via `serialize()` / constructor pairs, capture re-encoded JSON bytes, assert byte-strict equality.

The Scala leg from PR-03.2 must continue to pass unchanged. `runWireFixtures` extends, doesn't replace.

**Non-goals.**
- C# Layer B (PR-03.3b — sequential).
- Layer C cross-language interop (PR-03.4).
- Negative-test corpus (PR-03.5).
- `docs/wire-format.md` (PR-03.6).
- TBLOB fixtures (F2 vacuous; F5 unresolved).
- Multi-element TSet (F8 deferred).
- consts/streams (Q6/C5 deferred).
- Service/Buzzer method I/O on TS leg (F9 follow-up — TS goldens have module-private classes).
- Daemon-mode driver (master plan §11 Q8 deferred to PR-03.4).
- `flake.nix` updates beyond confirming nodejs/tsc/npm presence.
- Compiler-side changes; new `.domain` edits.
- Cross-build of the TS driver (driver is JS/TS, not Scala-versioned). Scala-side runner code MUST cross-build.

---

## §2 How Layer B TypeScript works (round-trip identity)

**Architecture: three-process pipeline.**
1. `WireFixturesMain` (Scala, in-sbt-JVM) — entry point. Existing for Scala leg. Extended to also invoke TS leg after Scala leg passes.
2. `WireFixtureTypescriptRunner` (Scala, in-sbt-JVM) — orchestrates: walks `wire-fixtures/typescript/<wireId>/<scenario>.json`, packages requests as stdin JSON, spawns `tsx driver.ts` subprocess, reads stdout JSON, byte-compares each result to its fixture, accumulates failures, throws `WireFixtureVerificationFailure` on any.
3. `driver.ts` (TS, in node subprocess) — reads stdin JSON, looks up `(deserialize, serialize)` in `Dispatch.ts`, runs round-trip, returns `{ok, reEncodedJson}` per request via stdout JSON. Exits 0 on any structural error (per-fixture failures via JSON); exits non-zero only on driver-internal crash.

**Per-fixture verification.**
1. Read fixture bytes (Scala-side).
2. Send to driver: `{wireId, fixturePath, fixtureJson: <text>}`.
3. Driver: `JSON.parse(fixtureJson)`. On parse error → `{ok:false, kind:"DecodeFailed", detail}`.
4. Driver: lookup `Dispatch[wireId]`. On miss → `{ok:false, kind:"UnknownWireId"}`.
5. Driver: `entry.deserialize(parsedObj) → typedValue`. On exception → `{ok:false, kind:"DecodeFailed", detail}`.
6. Driver: `entry.serialize(typedValue) → reEncodedObj`; `JSON.stringify(reEncodedObj) → reEncodedJson`. Send `{ok:true, reEncodedJson}`.
7. Scala-side byte-compare. On match: success. On mismatch: parse both, compare canonical noSpaces. Match → `WhitespaceMismatch`. Differ → `RoundtripDivergence`.
8. Aggregate. Throw `WireFixtureVerificationFailure(report)` on any failure. Task body re-wraps as `MessageOnlyException` per PR-03.1-D11.

**Failure kinds** (extends Scala leg's):
- `DecodeFailed`, `WhitespaceMismatch`, `RoundtripDivergence`, `UnknownWireId` — same as Scala leg.
- **NEW**: `DriverCrashed` (driver exited non-zero or didn't return one result per request), `DriverTimeout` (hard 60s).

**Why one-shot, not daemon.** Master plan §11 Q8 left this open. ~17 fixtures × one node startup ≈ 200ms total. Daemon adds line-oriented protocol + lifecycle complexity for marginal speedup. Defer daemon to PR-03.4 where Layer C 6-direction matrix amortizes startup.

**Why byte-strict raw.** L4 lock — field-order regressions are wire-affecting. Same justification as PR-03.2 §2.

---

## §3 Module / file layout

```
./idealingua-v1/idealingua-v1-test-harness/
├── src/main/scala/izumi/idealingua/harness/
│   ├── HarnessCorpus.scala          (extend — add wireFixturesTypescriptRoot, harnessTypescriptDir)
│   ├── HarnessMain.scala            (extend — WireFixturesMain invokes TS leg after Scala leg)
│   ├── WireFixtures.scala           (existing — generic, no edits needed)
│   ├── WireFixtureRunner.scala      (existing — Scala-leg only; unchanged)
│   ├── WireDispatch.scala           (existing — Scala-leg only; unchanged)
│   ├── FixtureSeeder.scala          (existing — Scala-leg only; unchanged)
│   ├── WireFixtureTypescriptRunner.scala  (NEW — subprocess orchestration + result parsing)
│   └── TypescriptDriverBridge.scala       (NEW — node/tsx + npm install lifecycle helper)
└── src/main/typescript/                   (NEW — TS driver project)
    ├── package.json                       (NEW — tsx, typescript, moment, websocket)
    ├── tsconfig.json                      (NEW — rootDirs+paths)
    ├── package-lock.json                  (NEW — committed)
    ├── driver.ts                          (NEW — main entry)
    └── Dispatch.ts                        (NEW — manual wireId → {deserialize, serialize})

./idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript/
├── idltest.dtofields.Point/{basic,extra}.json
├── idltest.dtofields.OptionalObj/{with-some,with-none}.json   (with-none diverges from Scala)
├── idltest.algebraics.AdtTester/{as-ComplexAdt,as-ComplexAdt2}.json
├── … (~17 dirs total)
```

**Path encoding rule.** Mirror PR-03.2: `wire-fixtures/typescript/<wireId>/<scenario>.json`, dots-in-dirname, `.notes.md` siblings allowed (ignored by loader).

---

## §4 Compiler integration: how generated TypeScript becomes available

**Three import roots needed:**
1. `irt` runtime at `idealingua-v1-runtime-rpc-typescript/src/main/resources/runtime/typescript/irt/`.
2. Generated goldens at `idealingua-v1-test-defs/golden/typescript/<modulePath>/`.
3. Driver's own sources.

**`tsconfig.json` (in `idealingua-v1-test-harness/src/main/typescript/`):**

```json
{
  "compilerOptions": {
    "module": "commonjs",
    "target": "es2020",
    "lib": ["es2020", "dom"],
    "moduleResolution": "node",
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "experimentalDecorators": true,
    "skipLibCheck": true,
    "strictNullChecks": false,
    "noImplicitAny": false,
    "noEmit": true,
    "rootDirs": [
      ".",
      "../../../../idealingua-v1-test-defs/golden/typescript",
      "../../../../idealingua-v1-runtime-rpc-typescript/src/main/resources/runtime/typescript"
    ],
    "baseUrl": ".",
    "paths": {
      "irt": ["../../../../idealingua-v1-runtime-rpc-typescript/src/main/resources/runtime/typescript/irt"],
      "*": ["../../../../idealingua-v1-test-defs/golden/typescript/*"]
    }
  },
  "include": [
    "driver.ts",
    "Dispatch.ts",
    "../../../../idealingua-v1-test-defs/golden/typescript/**/*.ts",
    "../../../../idealingua-v1-runtime-rpc-typescript/src/main/resources/runtime/typescript/irt/**/*.ts"
  ]
}
```

**Why `strictNullChecks: false`?** TS goldens use undeclared-init patterns (`private _w: number; this._w = value;`). The legacy emitter targets `strictNullChecks: false` per `TypescriptLayouter.scala:86`. We match.

**Why `paths` + `rootDirs`?** Goldens import via relative paths `'../../irt'`, `'../../../irt'`, etc. depending on depth. `paths` only intercepts BARE specifiers (`from 'irt'`); relative imports are resolved by filesystem. **Mitigation strategy in priority order:**
1. **`rootDirs` virtual-merge** — TS treats listed dirs as a single virtual tree; `'../../irt'` from a golden may resolve to the IRT source root. T1 evaluates empirically.
2. **Symlink** in T1 bootstrap: `ln -s <abs-irt> idealingua-v1-test-defs/golden/typescript/irt`. Symlink under git-ignored scratch (NOT committed).
3. **Scratch tree copy**: T1 bootstrap step copies IRT + goldens into `target/typescript/scratch/` so `'../../irt'` resolves naturally. Self-contained, slowest.

Pick the lowest-overhead working approach at T1. Document the choice.

**`package.json`:**

```json
{
  "name": "idealingua-v1-harness-driver",
  "private": true,
  "version": "0.0.0",
  "scripts": {
    "driver": "tsx driver.ts"
  },
  "devDependencies": {
    "tsx": "^4.20.0",
    "typescript": "5.9.3",
    "moment": "^2.29.4",
    "websocket": "^1.0.34",
    "@types/node": "^22.7.6",
    "@types/websocket": "^1.0.10"
  }
}
```

`package-lock.json` committed for reproducible installs. `npm install --prefer-offline` once per host (idempotent via `node_modules/` exists check).

**Build artifact location.** `idealingua-v1-test-harness/target/typescript/` for emitted artifacts. Already `.gitignore`d via `target/`.

**Driver invocation.** `WireFixtureTypescriptRunner` spawns `npm run driver` (= `tsx driver.ts`) with cwd = harness's TS dir, pipes stdin batch, captures stdout. First invocation runs `npm install` first (idempotent).

---

## §5 Coverage matrix (TypeScript leg, ~17 wireIds)

PR-03.2's 22 wireIds minus 5 service/buzzer (B3 — module-private TS classes). "BS=Scala" column: Y = bytes identical to Scala fixture; N = legitimately diverges; ≈ = depends on fixture values.

| # | Pattern | wireId | BS=Scala | Notes |
|---|---|---|---|---|
| 1 | Plain DTO | `idltest.dtofields.Point` | ≈ | Same bytes if no 0.0/null edge cases |
| 2 | Identifier multi-field unnamed | `idltest.identifiers.ComplexID` | Y | String-form, both encode same spec |
| 3 | Identifier multi-field named (UserId) | `idltest.identifiers.UserId` | Y | Both alphabetical-by-field |
| 3b | Identifier (BucketID) | `idltest.identifiers.BucketID` | Y | Same |
| 5 | ADT multi-branch (×2) | `idltest.algebraics.AdtTester` | Y | `{"ComplexAdt":{…}}` mirror |
| 6 | ADT with interface branch | `idltest.algebraics.AdtWithInterface` | Y | Two-level wrap matches |
| 7 | Interface w/ implementing DTO | `idltest.inheritance.WithCovariance.Struct` | Y | wireId-keyed encoding matches |
| 8 | Optional present | `idltest.dtofields.OptionalObj` | Y | `{"no":{"a":42}}` mirror |
| 9 | Optional absent | `idltest.dtofields.OptionalObj` | **N** | Scala `{"no":null}`; TS `{}` |
| 10 | List of structs | `idltest.dtofields.ListObj` | ≈ | Empty inner DTOs identical |
| 11 | Map with string keys | `idltest.identifiers.KVIDGeneric` | Y | Insertion-order in both |
| 12 | Enum | `idltest.identifiers.DepartmentEnum` | Y | String-encoded |
| 13 | Enum inside Identifier | `idltest.identifiers.UserWithEnumId` | Y | Same |
| 14 | Anyval-shaped DTO | `idltest.dtofields.NullableObj` | Y | Single-field |
| 15 | Empty struct | `idltest.inheritance.Empty.Struct` | Y | `{}` |
| 21 | Jsonlike | `idltest.json.JSONLike` | Y | Discriminator-keyed matches |
| 22-28 | Big-types DTO | `izumi.test.domain01.AllTypes.Struct` | **N** | Many divergences (see §8) |
| 29 | Cross-domain reference | `idltest.phase.Name_incoming` | Y | AnyVal-shaped |

**Out of TS leg:**
- `idltest.services.TestService.SimpleInput` — private class.
- `idltest.services.TestService.GreetSingularOutOutput` — out class is exported but Scala fixture is a JSON STRING; TS encodes `{}`. Skip.
- `idltest.events.TestBuzzer.{EmptyInput, EnumInputInput, AdtInputInput}` — private classes.

**Recommended fixture order** (T3 batches, ~5 each):
- B1: rows 1, 2, 3, 3b, 14, 15.
- B2: rows 5, 6, 7, 21.
- B3: rows 8, 9, 10, 11, 12, 13, 29.
- B4: rows 22-28 (AllTypes — most encoder-surprise risk).

Each batch ends with `sbt runWireFixtures` confirming green before continuing.

---

## §6 Fixture authoring conventions (TS-specific)

- **Format**: `JSON.stringify(value)` with no second arg. Single-line, UTF-8, no trailing newline.
- **No in-file metadata**.
- **Naming**: same as Scala leg.
- **Key order**: TS object literal declaration order; ECMAScript guarantees insertion order for string-keyed properties; `JSON.stringify` preserves it. Goldens emit in declaration order (verified `Point.ts:236-247` matches Scala's case-class declaration).
- **Time values**: TS uses moment-format `YYYY-MM-DDTHH:mm:ss.SSSZ` where `Z` is `+00:00` for UTC (NOT `Z` literal). Scala emits literal `Z`. **Divergent.** TS fixtures use `+00:00`. Verify empirically at T3.
- **Floats**: avoid `0.0` (TS emits `0`). Use `1.5`, `-2.25`.
- **Optional-`None`**: TS drops the key (`undefined` → omitted by `JSON.stringify`). Scala emits `"key":null`. **Divergent.**
- **Unsigned ints**: TS validators throw on negative. Use POSITIVE values (`200`, `60000`, `4000000000`). Scala fixtures use signed-wrap. **Divergent.**
- **Int64/UInt64**: ≤ `Number.MAX_SAFE_INTEGER = 2^53 - 1`. Scala fixture `int64=-9007199254740993` (= -2^53 - 1) becomes `-9007199254740991` on TS. **Divergent.**

**Sample sketches:**

```json
// wire-fixtures/typescript/idltest.dtofields.Point/basic.json (IDENTICAL to Scala)
{"w":10,"h":20,"id":"abc","name":"point-1","x":3,"y":4,"ownfield":"of","export":true}
```

```json
// wire-fixtures/typescript/idltest.dtofields.OptionalObj/with-none.json (DIVERGES; Scala = {"no":null})
{}
```

```json
// wire-fixtures/typescript/izumi.test.domain01.AllTypes.Struct/basic.json (DIVERGES)
{"b":true,"s":"hello","int8":42,"int16":1000,"int32":100000,"int64":9007199254740991,"f":1.5,"d":-2.25,"uuid":"3a7f0c12-1234-5678-9abc-fedcba987654","ts":"2025-01-15T10:30:45.000+00:00","tslocal":"2025-01-15T10:30:45.000","tsuni":"2025-01-15T10:30:45.000+00:00","time":"10:30:45.000","date":"2025-01-15","uint8":200,"uint16":60000,"uint32":4000000000,"uint64":9007199254740991,"list":[],"another":[],"selfMap":{},"enumMap":{"k1":"Val1"},"selfSet":[]}
```

(Verify at T3 — empirical bytes from the encoder are authoritative.)

---

## §7 `runWireFixtures` task wiring (extension)

**Task body** (sbtgen/Deps.scala):

```scala
"runWireFixtures" := """{
                       |  val log      = streams.value.log
                       |  val repoRoot = (LocalRootProject / baseDirectory).value.toPath
                       |  log.info("runWireFixtures: starting")
                       |  val cp = (Compile / fullClasspath).value.files
                       |  val r  = (Compile / runner).value
                       |  r.run("izumi.idealingua.harness.WireFixturesMain", cp, Seq(repoRoot.toString), log)
                       |    .failed.foreach(e => throw new MessageOnlyException(e.getMessage))
                       |  val scalaDir = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/scala")
                       |  val tsDir    = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript")
                       |  def countJsons(p: java.nio.file.Path): Long = if (java.nio.file.Files.exists(p)) {
                       |    val s = java.nio.file.Files.walk(p)
                       |    try s.filter(x => java.nio.file.Files.isRegularFile(x) && x.toString.endsWith(".json")).count()
                       |    finally s.close()
                       |  } else 0L
                       |  val sc = countJsons(scalaDir)
                       |  val tc = countJsons(tsDir)
                       |  log.info(s"runWireFixtures: all $sc Scala + $tc TypeScript fixtures match")
                       |}""".stripMargin.raw,
```

**`HarnessMain.scala` extension:** `WireFixturesMain.main` runs Scala leg first (existing); then runs TS leg via `WireFixtureTypescriptRunner.runAll(repoRoot, fixturesRoot, harnessTypescriptDir)`. On Scala-leg failure, TS leg never runs.

**`HarnessCorpus.scala` additions:**

```scala
def wireFixturesTypescriptRoot(repoRoot: Path): Path =
  repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript")

def harnessTypescriptDir(repoRoot: Path): Path =
  repoRoot.resolve("idealingua-v1/idealingua-v1-test-harness/src/main/typescript")
```

---

## §8 TS encoder behavior audit (divergences from Scala)

Documented per §5 column "BS=Scala = N". Summary:
- Optional-None → key dropped (vs Scala null).
- Float 0.0 → emitted as `0` (vs Scala `0.0`).
- Unsigned ints → throw on negative (vs Scala signed-wrap).
- Int64 ≥ 2^53 → truncates (vs Scala 64-bit Long).
- Time UTC → `+00:00` (vs Scala `Z`).
- ADT discriminator key SHORT name; Interface FULL class name; ADT-with-interface combined: `{branchName: {fullClassName: data}}`. Scala matches.
- Field order, enum encoding, identifier serialization: match Scala.

T3 authors confirm each divergence empirically when encountered.

---

## §9 Negative fixtures

Out of scope; defer to PR-03.5. T4 smoke test (corrupt + revert) doubles as in-execution negative-path sanity check.

---

## §10 Field-order audit

TS preserves declaration order via object-literal insertion order (ES guarantees). Same as Scala via Circe `deriveEncoder`. Verified at goldens spot-check (`Point.ts:236-247`).

---

## §11 Risks & open questions

- **R1 — `tsx` startup time** (~600ms cold, ~200ms warm). Acceptable for one-shot batched protocol.
- **R2 — `npm install` flake** in airgapped CI. Mitigation: `package-lock.json` committed; CI provisioning is downstream concern.
- **R3 — `moment` deprecation** (long-term concern, F11 follow-up).
- **R4 — `tsconfig paths` semantics** — only intercepts BARE specifiers; relative imports resolved by filesystem. T1 evaluates `rootDirs` first; falls back to symlink or scratch-tree copy.
- **R5 — Driver crash semantics**: per-fixture try/catch in driver; structured `{ok:false}` for expected errors; non-zero exit only for driver-internal crash.
- **R6 — Goldens compile failure** (B1). T1 explicit smoke test catches; escalate as user-blocker if surfaces.
- **R7 — IRT transitive `websocket` import**: `package.json` includes `websocket` as devDep so the import resolves; tree-shaking irrelevant since driver only uses `Formatter` / `Introspector` symbols.

---

## §12 Sub-task breakdown

### T1 — TS driver scaffold + smoke-test goldens compile

**Scope:**
- Create `idealingua-v1-test-harness/src/main/typescript/` with: `package.json`, `tsconfig.json`, stub `driver.ts` (returns empty `results`), stub `Dispatch.ts` (empty map), `package-lock.json` (after `npm install`).
- Create Scala scaffolds: empty `WireFixtureTypescriptRunner.scala`, `TypescriptDriverBridge.scala`. Add `wireFixturesTypescriptRoot` and `harnessTypescriptDir` methods on `HarnessCorpus.scala`. Extend `WireFixturesMain` to call the new runner.
- Run `./sbtgen.sc --js`. Verify `git diff project/plugins.sbt` empty.
- Run `npm install --prefer-offline --no-audit --no-fund` from harness's TS dir; commit resulting `package-lock.json`.
- **Smoke 1**: `npx tsc --noEmit` against the harness TS project. Must succeed. **If fails: B1 escalates — STOP.**
- **Smoke 2**: cross-build `sbt + idealingua-v1-test-harness/compile`. Must succeed both Scala 2.13 + 3.8.3.
- **Smoke 3**: `sbt -batch runWireFixtures` with no TS fixtures. Scala leg passes (existing 25 fixtures); TS leg trivially passes with 0 fixtures.

**Success criterion:** harness compiles cross-build; `tsc --noEmit` passes; `runWireFixtures` runs end-to-end.

### T2 — Driver protocol + Dispatch (no fixtures yet)

**Scope:**
- Implement `driver.ts`: stdin batch reader, per-request dispatch, stdout batch writer.
- Populate `Dispatch.ts` with ~17 entries from §5. For each: `{deserialize: (json) => new <Type>(json), serialize: (val) => JSON.stringify(val.serialize())}`. ADTs use the corresponding helper functions (e.g. `AdtTesterHelpers.serialize`).
- Implement `WireFixtureTypescriptRunner.runAll`: walk fixtures, build batch, spawn driver, parse results, byte-compare, aggregate failures.
- Implement `TypescriptDriverBridge`: locate `node`/`npm`, idempotent npm-install, subprocess spawn with stdin/stdout JSON framing, 60s timeout.
- Run sbtgen --js. Update task body to log `"all $sc Scala + $tc TypeScript fixtures match"`.

**Success criterion:** with 0 TS fixtures, `sbt runWireFixtures` clean (Scala 25 + TS 0). With 1 hand-authored test fixture, runs to clean pass (Scala 25 + TS 1).

### T3 — Fixture corpus authoring (~17 fixtures)

**Scope:** hand-author per §5 + §6. For each:
1. Build typed value via golden constructor.
2. Compute encoded bytes (`JSON.stringify(value.serialize())` or `<Type>Helpers.serialize`).
3. Save to `wire-fixtures/typescript/<wireId>/<scenario>.json`.
4. Run `sbt runWireFixtures` after each batch.

For BS=Y rows: copy from `wire-fixtures/scala/`. For BS=N: author independently per §6 conventions. For BS=≈: try copy first; branch if mismatch.

**Success criterion:** `sbt runWireFixtures` clean; `sbt + runWireFixtures` clean cross-build. Output: `"all 25 Scala + <N> TypeScript fixtures match"`.

### T4 — Smoke test (negative paths)

**Scope:**
- Corrupt one byte of a TS fixture; expect `RoundtripDivergence`; revert.
- Drop a `Dispatch[wireId]` entry; expect `UnknownWireId`; revert.
- Force driver crash (`process.exit(1)`); expect `DriverCrashed`; revert.

**Success criterion:** all three failure modes produce structured output; sbt daemon survives.

### T5 — Final commit

Single commit on `wip/necromancy`. PR-03.1 contracts hold; both legs pass; cross-build green.
