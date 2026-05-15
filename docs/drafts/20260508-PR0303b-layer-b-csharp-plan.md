# PR-03.3b — Layer B wire-byte fixtures, C# leg — implementation plan

Plan author: planning subagent (review-loop, 2026-05-08).
Source briefs: tasks.md, defects.md, defects-m1.md (PR-03-D02), `docs/drafts/20260504-1200-PR0303a-layer-b-typescript-plan.md`, PR-03.3a commit `f94e7ca`.

**Pre-locked decisions (planner-recommended):**
- Per-language fixture corpus (`wire-fixtures/csharp/<wireId>/<scenario>.json`); cross-language byte parity is Layer C (PR-03.4).
- One-shot `dotnet bin/Debug/net9.0/Driver.dll` after a one-time `dotnet build`. stdin/stdout JSON batch protocol — same shape as PR-03.3a.
- csproj `<Compile Include>` direct paths for IRT runtime + goldens. NO golden-tree symlink (improvement over PR-03.3a-T2-D01). MSBuild + Roslyn handle cross-tree references natively because C# imports namespaces, not file paths.
- ~14-15 wireIds (PR-03.3a's 17 minus 5 service/buzzer types whose C# RTTI naming diverges from Scala's; AllTypes.Struct re-enabled since C# has no moment+tsx blocker).
- Newtonsoft.Json pinned `13.0.3`.

---

## §1 Goal & non-goals

**Goal.** Single commit on `wip/necromancy` extending `runWireFixtures` to drive a Layer B byte-strict round-trip for the C# leg, sequenced AFTER the TS leg. Scala leg (PR-03.2) and TS leg (PR-03.3a) continue to pass unchanged. Each leg fails fast.

Driver: `dotnet`-hosted C# subprocess that uses Newtonsoft.Json + each golden's `[JsonConverter]`-driven serialization to round-trip JSON fixtures byte-strictly.

**Non-goals (refuse scope creep).**
- Layer C cross-language interop (PR-03.4).
- Negative-test corpus (PR-03.5).
- `docs/wire-format.md` spec (PR-03.6).
- Compiler-side changes; new `.domain` edits.
- TBLOB fixtures (F2 vacuous; verified by grep).
- Multi-element TSet (F8 deferred).
- Daemon-mode driver.
- AllTypes.Struct fixture if C# encoder blocker surfaces (then drop and document, mirroring F13).
- Unification of C# vs Scala/TS wireIds for service/buzzer types (architectural divergence in legacy emitter).
- Cross-build of the C# driver (it's net9.0, not Scala-versioned). Scala-side runner code MUST cross-build 2.13.18 + 3.8.3.

---

## §2 Round-trip identity

Three-process pipeline (mirrors PR-03.3a):

1. `WireFixturesMain` (Scala, in-sbt-JVM) — entry point; existing for Scala+TS legs; extended to invoke C# leg after TS.
2. `WireFixtureCSharpRunner` (Scala, in-sbt-JVM) — orchestrates: walks `wire-fixtures/csharp/`, packages requests as stdin JSON, spawns dotnet subprocess, reads stdout JSON, byte-compares each result, accumulates failures, throws `WireFixtureVerificationFailure`.
3. `Driver.exe` (C#, in dotnet subprocess) — reads stdin JSON, looks up `(deserialize, serialize)` in `Dispatch.cs`, runs round-trip, returns `{ok, reEncodedJson}` per request via stdout JSON.

**Per-fixture verification.** Identical to PR-03.3a §2: stdin batch with `{wireId, fixturePath, fixtureJson}`; driver dispatches via Newtonsoft, returns `{ok, reEncodedJson}` or `{ok:false, kind, detail}`. Scala-side byte-compare with canonical-noSpaces fallback to distinguish `WhitespaceMismatch` vs `RoundtripDivergence`.

**Failure kinds** (reuse PR-03.3a's 6-kind enum verbatim per leg):
- `DecodeFailed`, `WhitespaceMismatch`, `RoundtripDivergence`, `UnknownWireId`, `DriverCrashed`, `DriverTimeout`.
- T2 keeps per-leg `FailureKind` enums; deduplicate in a future PR (F15).

**Why one-shot, not daemon.** Cold `dotnet bin/Debug/net9.0/Driver.dll` ~150-300ms (vs `dotnet run` 1-2s). Daemon mode deferred to PR-03.4.

**Cold-start `dotnet build`.** First run ~10-30s on cold cache, ~2-5s warm. Build is one-time per host (cached in `obj/`). `CSharpDriverBridge` runs `dotnet build` once (idempotent via `bin/Debug/net9.0/Driver.dll` existence check).

---

## §3 Module / file layout

```
./idealingua-v1/idealingua-v1-test-harness/
├── src/main/scala/izumi/idealingua/harness/
│   ├── HarnessCorpus.scala               (extend — add wireFixturesCSharpRoot, harnessCSharpDir)
│   ├── HarnessMain.scala                 (extend — WireFixturesMain invokes C# leg after TS leg)
│   ├── WireFixtures.scala                (existing — generic loader, reused)
│   ├── WireFixtureRunner.scala           (existing)
│   ├── WireDispatch.scala                (existing)
│   ├── FixtureSeeder.scala               (existing)
│   ├── WireFixtureTypescriptRunner.scala (existing)
│   ├── TypescriptDriverBridge.scala      (existing)
│   ├── WireFixtureCSharpRunner.scala     (NEW — subprocess orchestration)
│   └── CSharpDriverBridge.scala          (NEW — dotnet build + subprocess spawn)
└── src/main/csharp/                      (NEW — C# driver project)
    ├── Driver.csproj                     (NEW — net9.0 console exe, Newtonsoft 13.0.3, <Compile Include>)
    ├── Program.cs                        (NEW — main entry: stdin batch reader, dispatch, stdout writer)
    ├── Dispatch.cs                       (NEW — manual wireId → IDispatchEntry map)
    └── (FixtureSeeder.cs)                (OPTIONAL — typed-value seeder for re-baselining)

./idealingua-v1/idealingua-v1-test-defs/wire-fixtures/csharp/
├── idltest.dtofields.Point/{basic,extra}.json
├── idltest.dtofields.OptionalObj/{with-some,with-none}.json
├── idltest.algebraics.AdtTester/{as-ComplexAdt,as-ComplexAdt2}.json
├── … (~14-15 dirs)
```

**Build artifacts** at `idealingua-v1-test-harness/src/main/csharp/{bin,obj}/` (already gitignored via standard sbtgen template).

---

## §4 Compiler integration

C# is structurally easier than TS: `using IRT.Marshaller;` imports a NAMESPACE, not a file path. MSBuild + Roslyn resolve cross-tree references via `<Compile Include>`. No symlink architecture needed.

**`Driver.csproj`** in `idealingua-v1-test-harness/src/main/csharp/`:

```xml
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <OutputType>Exe</OutputType>
    <TargetFramework>net9.0</TargetFramework>
    <Nullable>disable</Nullable>
    <RootNamespace>IdealinguaV1Harness.CSharpDriver</RootNamespace>
    <AssemblyName>Driver</AssemblyName>
    <NoWarn>CS0108;CS0114;CS0612;CS0618;CS8632;CS0162;CS0219</NoWarn>
  </PropertyGroup>
  <ItemGroup>
    <PackageReference Include="Newtonsoft.Json" Version="13.0.3" />
  </ItemGroup>
  <ItemGroup>
    <!-- IRT runtime sources (skip Transport tree). -->
    <Compile Include="..\..\..\..\idealingua-v1-runtime-rpc-csharp\src\main\resources\runtime\csharp\IRT\Marshaller\**\*.cs" />
    <Compile Include="..\..\..\..\idealingua-v1-runtime-rpc-csharp\src\main\resources\runtime\csharp\IRT\Logger\**\*.cs" />
    <Compile Include="..\..\..\..\idealingua-v1-runtime-rpc-csharp\src\main\resources\runtime\csharp\IRT\*.cs" />
    <!-- Goldens. -->
    <Compile Include="..\..\..\..\idealingua-v1-test-defs\golden\csharp\**\*.cs" />
    <!-- Exclude service/buzzer goldens that pull IRT.Transport.Client. -->
    <Compile Remove="..\..\..\..\idealingua-v1-test-defs\golden\csharp\Idltest\Services\TestService.cs" />
    <Compile Remove="..\..\..\..\idealingua-v1-test-defs\golden\csharp\Idltest\Events\TestBuzzer.cs" />
    <Compile Remove="..\..\..\..\idealingua-v1-test-defs\golden\csharp\Izumi\Test\Domain02\TestAliasServ.cs" />
    <Compile Remove="..\..\..\..\idealingua-v1-test-defs\golden\csharp\Izumi\Test\Domain02\NestedAdtsService.cs" />
    <Compile Remove="..\..\..\..\idealingua-v1-test-defs\golden\csharp\Izumi\Test\Domain02\ImportIdService.cs" />
    <Compile Remove="..\..\..\..\idealingua-v1-test-defs\golden\csharp\Izumi\Test\Domain01\OptionalService.cs" />
  </ItemGroup>
</Project>
```

**Why exclude services + buzzers entirely.** Six golden files pull `using IRT.Transport.Client;` which depends on third-party WebSocketSharp. Since C# service/buzzer wireIds diverge from Scala/TS naming anyway, excluding these 6 files removes 5 wireIds we wouldn't fixture and avoids the WebSocketSharp packaging.

**Driver invocation lifecycle (`CSharpDriverBridge`):**
1. **Idempotent build.** Check for `bin/Debug/net9.0/Driver.dll`. Absent → `dotnet build -c Debug` (180s timeout). Present → skip.
2. **Subprocess spawn.** `dotnet bin/Debug/net9.0/Driver.dll` (NOT `dotnet run`). cwd = `harnessCSharpDir`. stdin = batch JSON; stdout = batch results JSON. 60s hard timeout.
3. **Cleanup.** No symlinks. `regenerateGoldens` deleting `golden/csharp/` invalidates the build artifacts; next `runWireFixtures` rebuilds.

`dotnet build` runs `restore` implicitly. NuGet-cache-airgapped CI is a downstream concern (F14).

---

## §5 Coverage matrix (~14-15 wireIds)

PR-03.3a's 17 entries minus 5 service/buzzer (per §4 csproj exclusion). "BC=Scala": Y = identical bytes; N = legitimately diverges; ≈ = depends on values.

| # | Pattern | wireId | C# class | BC=Scala |
|---|---|---|---|---|
| 1 | Plain DTO | `idltest.dtofields.Point` | `Idltest.Dtofields.Point` | ≈ |
| 2 | Identifier multi-field unnamed | `idltest.identifiers.ComplexID` | `Idltest.Identifiers.ComplexID` | Y |
| 3 | Identifier multi-field named | `idltest.identifiers.UserId` | `Idltest.Identifiers.UserId` | Y |
| 3b | Identifier multi-field | `idltest.identifiers.BucketID` | `Idltest.Identifiers.BucketID` | Y |
| 5 | ADT multi-branch | `idltest.algebraics.AdtTester` | abstract `Idltest.Algebraics.AdtTester` | Y |
| 6 | ADT with interface branch | `idltest.algebraics.AdtWithInterface` | abstract `Idltest.Algebraics.AdtWithInterface` | Y |
| 7 | Interface impl DTO | `idltest.inheritance.WithCovariance.Struct` | `Idltest.Inheritance.WithCovarianceStruct` | Y |
| 8 | Optional present | `idltest.dtofields.OptionalObj` | `Idltest.Dtofields.OptionalObj` | Y |
| 9 | Optional absent | `idltest.dtofields.OptionalObj` | (same) | **N** (matches TS: `{}`) |
| 10 | List of structs | `idltest.dtofields.ListObj` | `Idltest.Dtofields.ListObj` | ≈ |
| 11 | Map with string keys | `idltest.identifiers.KVIDGeneric` | `Idltest.Identifiers.KVIDGeneric` | Y |
| 12 | Enum | `idltest.identifiers.DepartmentEnum` | `Idltest.Identifiers.DepartmentEnum` | Y |
| 13 | Enum inside Identifier | `idltest.identifiers.UserWithEnumId` | `Idltest.Identifiers.UserWithEnumId` | Y |
| 14 | Anyval-shaped DTO | `idltest.dtofields.NullableObj` | `Idltest.Dtofields.NullableObj` | Y |
| 15 | Empty struct | `idltest.inheritance.Empty.Struct` | `Idltest.Inheritance.EmptyStruct` | Y |
| 21 | JSONLike ADT | `idltest.json.JSONLike` | abstract `Idltest.Json.JSONLike` | Y |
| 22-28 | Big-types DTO | `izumi.test.domain01.AllTypes.Struct` | `Izumi.Test.Domain01.AllTypesStruct` | **N** (see §8) |
| 29 | Cross-domain reference | `idltest.phase.Name_incoming` | `Idltest.Phase.Name_incoming` | Y |

**Service/buzzer wireIds excluded.** Two architectural reasons:
- (a) Some methods don't synthesize Input/Output classes at all (parameterless / +Mixin / singular-string-output cases).
- (b) The ones that DO synthesize use C# `In<Method>` / `Out<Method>` naming (e.g. `Idltest.Events.TestBuzzer.InEnumInput`), so RTTI strings diverge from Scala's `<Method>Input` (`idltest.events.TestBuzzer.EnumInputInput`).

F9 follow-up updated.

---

## §6 Fixture authoring conventions (C#-specific)

- **Format**: `JsonConvert.SerializeObject(value, Settings)` with `Formatting.None`, `NullValueHandling.Ignore`. Single-line UTF-8.
- **Key order**: per-converter `WritePropertyName` calls in declaration order (e.g. Point.cs:78-94). Matches Scala/TS.
- **Time values**: `ts` (zoned UTC) emits `+00:00` (matches TS, diverges Scala `Z`); `tsuni` emits literal `Z` (matches Scala); `tslocal` no zone (matches Scala).
- **Floats**: Newtonsoft round-trip format produces `0.0` for `0.0f`/`0.0` (matches Scala, diverges TS).
- **Optional-None**: drops key (matches TS, diverges Scala). Per-converter `if (v.X != null)` guards + global `NullValueHandling.Ignore`.
- **Unsigned ints**: native `byte`/`ushort`/`uint`/`ulong`. Fixtures use POSITIVE values (200, 60000, 4000000000). Diverges Scala signed-wrap.
- **Int64**: full 64-bit signed `long`. Matches Scala. Diverges TS 2^53 limit.
- **UInt64**: full 64-bit unsigned `ulong`. Newtonsoft emits as JSON number always (Q4 hybrid string-for-large NOT enforced; defer reconciliation to PR-03.4).
- **UUID**: lowercase canonical. Matches Scala.
- **Enum**: `WriteValue(value.ToString())`. Driver MUST register `StringEnumConverter` in `JsonSerializerSettings` (mirror JsonNetMarshaller.cs:15).
- **Identifier wire form**: `"TypeName#part1:part2"`. Matches Scala/TS byte-for-byte.
- **TSet single-element only** (F8 deferred).

**Sample sketches** (T3 verifies empirically):

```json
// wire-fixtures/csharp/idltest.dtofields.Point/basic.json (matches Scala AND TS)
{"w":10,"h":20,"id":"abc","name":"point-1","x":3,"y":4,"ownfield":"of","export":true}
```

```json
// wire-fixtures/csharp/idltest.dtofields.OptionalObj/with-none.json (matches TS, diverges Scala)
{}
```

```json
// wire-fixtures/csharp/izumi.test.domain01.AllTypes.Struct/basic.json (DIVERGES; ts uses +00:00; uint* native)
{"b":true,"s":"hello","int8":42,"int16":1000,"int32":100000,"int64":-9007199254740993,"f":1.5,"d":-2.25,"uuid":"3a7f0c12-1234-5678-9abc-fedcba987654","ts":"2025-01-15T10:30:45.000+00:00","tslocal":"2025-01-15T10:30:45.000","tsuni":"2025-01-15T10:30:45.000Z","time":"10:30:45.000","date":"2025-01-15","uint8":200,"uint16":60000,"uint32":4000000000,"uint64":9007199254740993,"list":[],"another":[],"selfMap":{},"enumMap":{"k1":"Val1"},"selfSet":[]}
```

---

## §7 `runWireFixtures` task wiring (extension)

Replace task body in `sbtgen/Deps.scala` with three-leg + per-leg-count log:

```scala
"runWireFixtures" := """{
                       |  val log      = streams.value.log
                       |  val repoRoot = (LocalRootProject / baseDirectory).value.toPath
                       |  log.info("runWireFixtures: starting")
                       |  val cp = (Compile / fullClasspath).value.files
                       |  val r  = (Compile / runner).value
                       |  r.run("izumi.idealingua.harness.WireFixturesMain", cp, Seq(repoRoot.toString), log)
                       |    .failed.foreach(e => throw new MessageOnlyException(e.getMessage))
                       |  def countJsons(p: java.nio.file.Path): Long = if (java.nio.file.Files.exists(p)) {
                       |    val s = java.nio.file.Files.walk(p)
                       |    try s.filter(x => java.nio.file.Files.isRegularFile(x) && x.toString.endsWith(".json")).count()
                       |    finally s.close()
                       |  } else 0L
                       |  val sc = countJsons(repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/scala"))
                       |  val tc = countJsons(repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript"))
                       |  val cc = countJsons(repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/csharp"))
                       |  log.info(s"runWireFixtures: all $sc Scala + $tc TypeScript + $cc CSharp fixtures match")
                       |}""".stripMargin.raw,
```

**`HarnessMain.scala`:** `WireFixturesMain.main` runs Scala → TS → C# in order. Each fails fast.

**`HarnessCorpus.scala` additions:**

```scala
def wireFixturesCSharpRoot(repoRoot: Path): Path =
  repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/csharp")

def harnessCSharpDir(repoRoot: Path): Path =
  repoRoot.resolve("idealingua-v1/idealingua-v1-test-harness/src/main/csharp")
```

---

## §8 C# encoder behavior audit

Citations from JsonNetMarshaller.cs and per-type JsonNetConverters under `golden/csharp/`.

- **Optional-None drops key.** Per-converter guards (OptionalObj.cs:41, AllTypes.cs:343, 365, 370). Global `NullValueHandling.Ignore` (JsonNetMarshaller.cs:16).
- **Float `0.0` emits `0.0`.** Newtonsoft round-trip format.
- **Unsigned ints native** in C#.
- **Int64 full 64-bit signed.**
- **UInt64 full 64-bit unsigned**, raw JSON number always (Q4 hybrid not enforced).
- **UUID** lowercase via `Guid.ToString()`.
- **Time `ts` (zoned UTC)** = `"yyyy-MM-ddTHH:mm:ss.fffzzz"` → `+00:00` (JsonNetMarshaller.cs:120).
- **Time `tsuni` (UTC universal)** = `"yyyy-MM-ddTHH:mm:ss.fffZ"` → literal `Z` (JsonNetMarshaller.cs:144).
- **Time `tslocal`** = `"yyyy-MM-ddTHH:mm:ss.fff"` (no zone).
- **Time `time`** = `"HH:mm:ss.fff"` from TimeSpan format.
- **Date** = `"yyyy-MM-dd"`.
- **ADT discriminator** SHORT branch name (matches Scala/TS).
- **ADT-with-interface combined** two-level wrap (matches TS).
- **Field order** via `WritePropertyName` declaration sequence (matches Scala/TS).
- **Enum** `value.ToString()` → enum case name (matches Scala).
- **Identifier wire form** `"TypeName#part1:part2"` (matches Scala/TS byte-for-byte).

**No `???` emissions in C# goldens** — verified: `command grep -rl '???' golden/csharp/` returns ZERO. F2 vacuous on C# leg.

---

## §9 Negative fixtures

Out of scope; defer to PR-03.5. T4 smoke test doubles as in-execution sanity check.

---

## §10 Field-order audit

Same conclusion as Scala/TS: declaration-order via `WritePropertyName` sequencing in custom `WriteJson` methods (Point.cs:78-94 et al.).

---

## §11 Risks & open questions

- **R1 — `dotnet build` cold start** (~10-30s on cold cache, ~2-5s warm). One-time per host.
- **R2 — NuGet airgapped CI** (F14). Out of PR-03.3b scope.
- **R3 — Newtonsoft.Json version drift.** Pinned `13.0.3`.
- **R4 — TBLOB `???`** vacuous on this corpus.
- **R5 — UInt64 hybrid encoding (Q4)** not enforced by Newtonsoft default. Cross-language reconciliation in PR-03.4.
- **R6 — Enum converter registration** load-bearing. T2 verifies via DepartmentEnum fixture.
- **R7 — RTTI registry static-ctor** uses `Assembly.GetExecutingAssembly().GetTypes()` by default; works in single-assembly setup.
- **R8 — IRT.Transport tree** pulls WebSocketSharp; csproj excludes the 6 service/buzzer goldens that import it.
- **R9 — Goldens compile failure (PRIMARY USER-BLOCKER)**. T1 explicit smoke test: `dotnet build`. **If fails, escalate.** Risk likelihood: LOW.
- **R10 — Per-leg FailureKind enum duplication** (F15 cosmetic cleanup deferred).
- **R11 — Abstract ADT classes**: `Idltest.Algebraics.AdtTester` is `abstract`. Dispatch entries for ADTs use `JsonConvert.DeserializeObject<AdtTester>(json, Settings)` (the `[JsonConverter]` on the class drives dispatch). NOT `new AdtTester(json)` (would fail to compile).

---

## §12 Sub-task breakdown

### T1 — C# driver scaffold + smoke-test goldens compile

**Scope:**
- Create `idealingua-v1-test-harness/src/main/csharp/` with `Driver.csproj` (per §4), stub `Program.cs` (returns empty `{"results":[]}`), stub `Dispatch.cs` (empty `Dictionary<string, IDispatchEntry>`).
- Create Scala scaffolds: empty `WireFixtureCSharpRunner.scala`, `CSharpDriverBridge.scala`. Add `wireFixturesCSharpRoot`, `harnessCSharpDir` to `HarnessCorpus`. Extend `WireFixturesMain` to call new runner.
- Run `./sbtgen.sc --js`. Verify `git diff project/plugins.sbt` empty.
- **Smoke 1**: `dotnet build` from harness/csharp dir. Must succeed clean. **If fails: R9 → STOP and escalate.**
- **Smoke 2**: cross-build `sbt + idealingua-v1-test-harness/compile`.
- **Smoke 3**: `sbt -batch runWireFixtures` with 0 C# fixtures. Success log: `"all 25 Scala + 19 TypeScript + 0 CSharp fixtures match"`.

### T2 — Driver protocol + Dispatch (no fixtures yet)

**Scope:**
- Implement `Program.cs`: stdin batch reader (`Console.OpenStandardInput()` + UTF-8), per-request dispatch, stdout batch writer.
- Define `IDispatchEntry` interface + generic `DispatchEntry<T>` helper:

```csharp
public interface IDispatchEntry {
  string Serialize(object value);
  object Deserialize(string json);
}

public sealed class DispatchEntry<T> : IDispatchEntry {
  public string Serialize(object value) => JsonConvert.SerializeObject((T)value, Settings);
  public object Deserialize(string json) => JsonConvert.DeserializeObject<T>(json, Settings);
}
```

`Settings` mirrors JsonNetMarshaller.cs:9-23 (NullValueHandling.Ignore, DateParseHandling.None, Formatting.None, StringEnumConverter registered).

- Populate `Dispatch.cs` with ~14-15 entries from §5. Plain DTOs → `DispatchEntry<TypeName>`. ADTs → `DispatchEntry<AbstractAdtBase>`. Identifiers → `DispatchEntry<IdentifierClass>`. Enums → `DispatchEntry<EnumType>` with manual converter registration.
- Implement `WireFixtureCSharpRunner.runAll` (mirrors `WireFixtureTypescriptRunner.runAll`).
- Implement `CSharpDriverBridge` (mirrors `TypescriptDriverBridge` minus IRT-symlink and npm-install; replace with idempotent `dotnet build` check).
- Run `./sbtgen.sc --js`.

**Success criterion:** with 0 C# fixtures, `sbt runWireFixtures` clean. With 1 hand-authored Point fixture, runs to clean pass.

### T3 — Fixture corpus authoring (~14-15 wireIds, ~17-19 files)

**Scope:** hand-author per §5 + §6, batched. For each fixture: build typed value, encode via driver's Settings, save, run `sbt runWireFixtures`. For BC=Y rows, copy from `wire-fixtures/scala/` and verify. For BC=N, author independently.

Optional `FixtureSeeder.cs` for parity with PR-03.2/03.3a. Recommended.

**Success criterion:** `sbt runWireFixtures` clean; `sbt + runWireFixtures` clean cross-build.

### T4 — Smoke test (negative paths)

WhitespaceMismatch (whitespace insertion); UnknownWireId (Dispatch miss); DriverCrashed (`Environment.Exit(1)` in Program.cs).

### T5 — Final commit

Single commit on `wip/necromancy`. PR-03.1/03.2/03.3a contracts hold; all three legs pass.
