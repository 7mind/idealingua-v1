# PR-03.4 — Layer C cross-language interop matrix — implementation plan

Plan author: planning subagent (review-loop, 2026-05-08).
Source briefs: tasks.md, defects.md, defects-m1.md, master plan §3 lines 391-451, §11 Q8, §12; PR-03.2/03.3a/03.3b plans + commits.

**Pre-locked decisions (planner-recommended, orchestrator-accepted 2026-05-08):**
- **Round-trip semantics**: Interpretation C (full-loop byte-strict). Chain `S → M → S` ends at S's encoder; F10 divergences wash out at the chain endpoint provided both decoders accept both forms semantically.
- **Daemon protocol**: line-oriented JSON-per-line `roundtrip` request (decode + re-encode in one call). Per-request 10s timeout; daemon-startup 60s grace.
- **Daemon mode is OPT-IN** via `--daemon` flag (TS) / `daemon` subcommand (C#). Layer B batch mode preserved unchanged.
- **AllTypes.Struct *→TS direction excluded** at orchestration time (F13 + F10 unsigned-wrap + int64 boundary).
- **F10 → `[~]`** post-merge: PR-03.4 verifies divergences don't propagate beyond chain endpoint; final resolution awaits PR-02 typer rewrite.

---

## §1 Goal & non-goals

**Goal.** Single commit on `wip/necromancy` implementing the body of `runCrossLangInterop` (currently a placeholder at `sbtgen/Deps.scala:518`). Verifies cross-language interop between Scala/TS/C# via the EXISTING per-language Dispatch tables and subprocess drivers from PR-03.2/03.3a/03.3b — no new fixtures, no new compiler changes.

**Non-goals.**
- Negative-test corpus (PR-03.5).
- `docs/wire-format.md` (PR-03.6).
- Compiler / typer / emitter changes.
- New `.domain` edits.
- New TBLOB / multi-element TSet / consts / streams fixtures.
- Replacing Layer B batch protocol with daemon (Layer B keeps batch).
- Adding fixtures to per-language corpora.
- Exporting service/buzzer input classes (F9 future).
- Refactoring per-leg `FailureKind` enums (F15 cosmetic).
- 3-language cycles (only 2-language chains per master plan §3).
- Envelope (`RpcPacket`) cross-language tests (deferred).

---

## §2 Round-trip identity (Interpretation C — full-loop byte-strict)

For each ordered pair of distinct languages (S, M):

```
fixture_bytes (in S's canonical form)
  → decode_M(parse_json(fixture_bytes))         // M consumes
  → encode_M(typed_value_M)                     // M reproduces (may diverge byte-wise from fixture!)
  → parse_json(_)
  → decode_S(_)                                 // S re-consumes M's output
  → encode_S(typed_value_S)                     // S emits
  → byte-strict-compare against fixture_bytes
```

The chain ends at S's encoder; bytes-on-the-wire after the final stage are S's canonical form. F10 divergences (Optional-None `null` vs key-drop, time UTC `Z` vs `+00:00`, URL-escape case, etc.) **wash out at the chain endpoint** as long as both decoders accept both forms semantically.

**Failure kinds** (operationally distinct from Layer B's; per F15 not unified):
- `DecodeFailedAt(M)`: M rejected source-language fixture bytes.
- `DecodeFailedAt(S)`: S rejected M's re-encoded JSON.
- `EncodeFailedAt(M)` / `EncodeFailedAt(S)`: structural; rare.
- `ChainEndpointMismatch`: loop completed but final bytes ≠ source fixture (e.g., precision loss through TS intermediate).
- `WhitespaceMismatch`: bytes differ but parsed JSON identical (unexpected; investigate).
- `UnknownWireIdAt(lang)`: skipped, recorded.
- `DriverCrashed(lang)` / `DriverTimeout(lang)`: subprocess.
- `Excluded(reason)`: pre-excluded per §5.2 (NOT a failure).

---

## §3 Module / file layout

**New harness sources** (Scala 2.13.18 + 3.8.3 cross-compatible):

```
./idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/
├── HarnessMain.scala            (extend — add CrossLangMain)
├── CrossLangChain.scala         (NEW — per-tuple chain orchestrator)
├── CrossLangDaemon.scala        (NEW — TS/C# subprocess wrapper, daemon protocol)
├── CrossLangScalaAdapter.scala  (NEW — in-JVM Scala "daemon" using WireDispatch.entries)
└── WireFixtureCrossLangRunner.scala  (NEW — top-level runner: load corpora, compute matrix, aggregate)
```

**Driver-mode additions** (additive):
- `idealingua-v1-test-harness/src/main/typescript/driver.ts` — new `--daemon` argv branch using `readline`. Existing batch path UNCHANGED.
- `idealingua-v1-test-harness/src/main/csharp/Program.cs` — new `daemon` subcommand. Existing `seed` and batch paths UNCHANGED.

**sbt task body** (`sbtgen/Deps.scala:518`):

```scala
"runCrossLangInterop" := """{
  val log      = streams.value.log
  val repoRoot = (LocalRootProject / baseDirectory).value.toPath
  log.info("runCrossLangInterop: starting cross-language matrix")
  val cp = (Compile / fullClasspath).value.files
  val r  = (Compile / runner).value
  r.run("izumi.idealingua.harness.CrossLangMain", cp, Seq(repoRoot.toString), log)
   .failed.foreach(e => throw new MessageOnlyException(e.getMessage))
  log.info("runCrossLangInterop: matrix verified")
}""".stripMargin.raw,
```

---

## §4 Daemon-mode protocol (line-oriented JSON-per-line)

**Stdin** (Scala harness → driver subprocess), one request per line:

```
{"req":"roundtrip","id":"r-001","wireId":"...","json":"<input json>"}\n
{"req":"shutdown"}\n
```

**Stdout** (driver subprocess → Scala harness), one response per line:

```
{"id":"r-001","ok":true,"reEncodedJson":"<output json>"}\n
{"id":"r-001","ok":false,"kind":"DecodeFailed","detail":"..."}\n
```

**Why `roundtrip` (single-request) instead of `decode`/`encode` separated.** Per Interpretation C, the chain orchestrator transports JSON between daemons (not typed-value handles); each driver remains stateless across requests. Matches master plan §3 lines 399-417.

**Buffering**:
- TS: `process.stdout.write(line + '\n')`. Pipe is block-buffered; the trailing `\n` plus `process.stdout` flush behavior on linefeed is sufficient empirically (T1 verifies).
- C#: `Console.Out.WriteLine(line)` followed by `Console.Out.Flush()`.

**Timeouts**: per-request 10s; first-request-after-spawn 60s grace (TS tsx warmup, dotnet cold start).

**Daemon shutdown**: send `{"req":"shutdown"}`, drain stdout, expect process exit within 5s; `destroyForcibly` if not.

**Failure modes**: daemon mid-run crash → EOF on read → `DriverCrashed` for in-flight tuple AND every subsequent tuple in the matrix (don't auto-restart for PR-03.4; re-running is cheap).

---

## §5 Cross-language matrix coverage

Six ordered cross-language pairs: (Scala→TS), (Scala→C#), (TS→Scala), (TS→C#), (C#→Scala), (C#→TS). Self-pairs are Layer B; out of scope.

**Eligibility per (S, M, wireId)**: tuple exercised iff S has a fixture, S has a Dispatch entry, M has a Dispatch entry.

**Empirical Dispatch coverage**:
- Scala: 22 wireIds (`WireDispatch.scala:56-221`).
- TS: 17 wireIds (`Dispatch.ts:25-128`).
- C#: 17 wireIds (`Dispatch.cs:32-59`).

**Empirical fixture coverage**:
- Scala: 22 wireIds × 25 files.
- TS: 16 wireIds × 19 files (AllTypes.Struct dropped per F13).
- C#: 17 wireIds × 20 files.

**Triple-coverage (eligible across all three)**: 17 wireIds (TS+C# Dispatch identical; Scala superset by 5 service/buzzer types).

**Per-direction matrix** (≈ counts; exact emerges at T3):

| Direction | Eligible wireIds | Total fixtures | Notes |
|---|---|---|---|
| Scala → TS | 17 | ~20 | 5 service/buzzer skipped; AllTypes.Struct excluded (§5.2) |
| Scala → C# | 17 | ~20 | 5 service/buzzer skipped; AllTypes.Struct excluded (§5.2) |
| TS → Scala | 16 | 19 | TS has 16 wireIds with fixtures (AllTypes dropped) |
| TS → C# | 16 | 19 | Same |
| C# → Scala | 17 | 20 | C# has all 17 |
| C# → TS | 16 | 17–20 | AllTypes.Struct excluded (§5.2) |

**Total cross-language tuples** ≈ 6 × ~17 ≈ 102 tuples; with §5.2 exclusions ≈ **96 verified tuples**.

### §5.2 Pre-excluded tuples (NOT failures; recorded)

| Source | Target | Excluded wireIds | Reason |
|---|---|---|---|
| (any) | TS | `izumi.test.domain01.AllTypes.Struct` | F13: TS IRT formatter incompatible with tsx + Node 24 + esbuild CJS interop |
| Scala | TS | (above) | Additional: Scala fixture's signed-wrap `uint8:-56` would fail TS validators on negative-int |
| Scala | C# | `izumi.test.domain01.AllTypes.Struct` | Scala fixture's signed-wrap unsigned-ints — verify whether C# decoder coerces or throws (T3) |
| C# | TS | (above) | Additional: C# fixture's `uint64:9007199254740993` (>2^53) precision loss in TS Number; `int64:-9007199254740993` same |

Effectively: AllTypes.Struct excluded from 4 directions (`*→TS` × 2 + `Scala→C#` + nothing else if T3 confirms C# accepts signed-wrap). Plan reserves AllTypes.Struct for T3 empirical validation; if the (Scala→C#) chain works, the exclusion shrinks.

### §5.3 Skipped tuples (target lacks Dispatch entry)

- (Scala → TS) / (Scala → C#) for 5 service/buzzer wireIds: `idltest.services.TestService.SimpleInput`, `…GreetSingularOutOutput`, `idltest.events.TestBuzzer.{EmptyInput, EnumInputInput, AdtInputInput}`. Total skipped: 10. Reported as `Skipped(F9)`.

---

## §6 Chain protocol (operational detail)

For each tuple `(S, M, wireId, scenario, fixture_bytes)`:

```
1. resp_M = daemon_M.roundtrip(wireId, fixture_bytes_utf8_string)
   if !resp_M.ok: fail per resp_M.kind (DecodeFailedAt(M), etc.)
   json_M = resp_M.reEncodedJson

2. resp_S = daemon_S.roundtrip(wireId, json_M)
   if !resp_S.ok: fail per resp_S.kind (DecodeFailedAt(S), etc.)
   final_bytes_S = resp_S.reEncodedJson.getBytes(UTF_8)

3. Compare against source fixture
   if Arrays.equals(fixture_bytes, final_bytes_S): OK
   else if parse(fixture_bytes).noSpaces == parse(final_bytes_S).noSpaces: WhitespaceMismatch (unexpected)
   else: ChainEndpointMismatch
```

**Failure attribution**: `DecodeFailedAt(L)` names which language failed. `ChainEndpointMismatch` indicates a semantic round-trip failure (e.g., precision loss through TS intermediate).

**Reporting**: mirror Layer B `formatReport` pattern (group by (sourceLang, targetLang, kind); enumerate failing wireIds; show fixture path + failed stage + detail).

---

## §7 `runCrossLangInterop` task wiring

**`HarnessMain.scala` extension:**

```scala
object CrossLangMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: CrossLangMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = java.nio.file.Paths.get(args(0))
    val report = WireFixtureCrossLangRunner.runAll(repoRoot)
    System.out.println(report.formatSummary())
  }
}
```

`WireFixtureCrossLangRunner.runAll` returns `Report(verified, skipped, excluded)` on success or throws `WireFixtureCrossLangVerificationFailure` on any failure (no `sys.exit`; per PR-03.1-D11).

**Success log** (printed to stdout; exact counts emerge at T3):

```
runCrossLangInterop: <V> verified, <S> skipped (F9 service/buzzer Dispatch gap), <X> excluded (F10/F13)
  Scala→TS: ...
  Scala→C#: ...
  TS→Scala: ...
  TS→C#: ...
  C#→Scala: ...
  C#→TS: ...
```

---

## §8 Per-language daemon-mode driver extensions

### §8.1 TypeScript: `driver.ts` additions

Top-of-file argv branch:

```typescript
if (process.argv.includes('--daemon')) {
  const rl = readline.createInterface({ input: process.stdin });
  rl.on('line', (line) => {
    const req = JSON.parse(line);
    if (req.req === 'shutdown') { rl.close(); process.exit(0); }
    if (req.req === 'roundtrip') {
      const result = doRoundtrip(req.wireId, req.json);
      process.stdout.write(JSON.stringify({ id: req.id, ...result }) + '\n');
    }
  });
} else {
  // existing batch-mode path (PR-03.3a, untouched)
}
```

### §8.2 C#: `Program.cs` additions

```csharp
if (args.Length == 1 && args[0] == "daemon") {
  using var reader = new StreamReader(Console.OpenStandardInput(), Encoding.UTF8);
  string line;
  while ((line = reader.ReadLine()) != null) {
    var req = JObject.Parse(line);
    if ((string)req["req"] == "shutdown") return 0;
    if ((string)req["req"] == "roundtrip") {
      var resp = DoRoundtrip((string)req["wireId"], (string)req["json"]);
      Console.Out.WriteLine(new JObject {
        ["id"] = (string)req["id"], /* ok, kind, detail, reEncodedJson */
      }.ToString(Formatting.None));
      Console.Out.Flush();
    }
  }
  return 0;
}
```

Existing batch + `seed` paths UNCHANGED.

### §8.3 Scala: in-JVM "daemon"

`CrossLangScalaAdapter.roundtrip(wireId, json)` directly uses `WireDispatch.entries` — no subprocess.

---

## §9 Failure-kind enumeration

Per §2 and §15. Sealed-trait `CrossLangFailureKind` with `DecodeFailedAt`, `EncodeFailedAt`, `ChainEndpointMismatch`, `WhitespaceMismatch`, `UnknownWireIdAt`, `DriverCrashed`, `DriverTimeout`, `Excluded`. Operationally distinct from Layer B's enum (F15 cosmetic deferred).

---

## §10 Risks & open questions (T3-empirical)

The following are validated at T3:

1. **R10.1** Does Scala decode TS's missing-key for Optional-None? Circe's default for `Option` fields IS lenient; expected to work.
2. **R10.2** Does TS decode Scala's `null` for Optional-None? Expected: legacy IRT TS classes test `data.no` → both null/undefined map to None. Round-trip works.
3. **R10.3** Does Scala decode C#'s `+00:00` for `ts`? Expected: legacy `IRTTimeInstances.tsTzDecoder` accepts ISO-8601-with-offset.
4. **R10.4** Does TS decode Scala's signed-wrapped `uint8:-56`? **Hard structural blocker** — TS validators throw on negative. Mitigated via §5.2 exclusion.
5. **R10.5** Does Scala decode TS's truncated `int64`? **ChainEndpointMismatch** for AllTypes.Struct. Mitigated via §5.2 exclusion.
6. **R10.6** URL-escape case for Identifiers (C# %3a vs Scala/TS %3A): expected to work because all three decoders are case-insensitive; idempotent re-encode at chain endpoint.
7. **R10.7** TS daemon survives Node 24 IPC pipe quirks? T1 5-fixture smoke test verifies.
8. **R10.8** dotnet `Console.Out.WriteLine` flush under daemon? Mitigated via explicit `Console.Out.Flush()`.

---

## §11 Sub-task breakdown

### T1 — Daemon-mode scaffolds + smoke

Scope:
- Add `--daemon` flag to `driver.ts`. Existing batch-mode path UNCHANGED.
- Add `daemon` subcommand to `Program.cs`. Existing batch + `seed` paths UNCHANGED.
- Add `CrossLangScalaAdapter` (in-JVM Scala roundtrip).
- Add skeleton `CrossLangDaemon` (subprocess wrapper for TS/C# daemon mode), `CrossLangChain` (placeholder), `WireFixtureCrossLangRunner` (placeholder runs to clean exit with empty matrix).
- Add `CrossLangMain` entry point in `HarnessMain.scala`.
- Wire `runCrossLangInterop` task body to call `CrossLangMain`.

Acceptance:
- `sbt runCrossLangInterop` runs without error against placeholder Runner.
- Daemon-mode 5-fixture smoke test (manual): spawn TS daemon, send 5 `roundtrip` requests for Point/basic.json, verify byte-strict responses. Same for C# daemon.
- Layer B batch mode UNCHANGED (`sbt runWireFixtures` → still 25+19+20).

### T2 — Chain protocol + matrix orchestration

Scope:
- Implement `CrossLangChain.run(s, m, fixture)` — issues roundtrip via two daemons, byte-compares.
- Implement `WireFixtureCrossLangRunner.runAll`:
  - Spawn 3 daemons (Scala in-JVM, TS subprocess, C# subprocess).
  - Compute matrix, apply §5.2 exclusions.
  - For each tuple, call `CrossLangChain.run`.
  - Aggregate + format report.
  - Terminate daemons cleanly.

Acceptance:
- `sbt runCrossLangInterop` reports matrix size and verifies all-green for un-excluded tuples.
- Cross-build verifies (Scala 2.13 + 3).

### T3 — Empirical validation of §10

Scope:
- Run the matrix; classify any unexpected failures.
- For each unexpected failure, open a follow-up F-row in `tasks.md`.
- Confirm §10.1-§10.6 expected-to-work cases pass empirically.
- Confirm §10.4-§10.5 expected-to-fail cases hit the §5.2 exclusions cleanly.

Acceptance:
- Report shows verified count, skipped count, excluded count exactly per §7 success-log shape.
- No unexpected failures; every failure has a documented reason or a new F-followup.

### T4 — Smoke tests

Mirror Layer B's smoke convention (per `tasks.md` PR-03.3b T4):
1. **DriverCrashed**: `Environment.Exit(1)` early in C# daemon. Verify `DriverCrashed(CSharp)` for all C#-involving tuples; sbt daemon survives.
2. **DriverTimeout**: `while(true) {}` loop in TS daemon. Verify timeout fires; sbt daemon survives.
3. **ChainEndpointMismatch**: corrupt one byte of Scala fixture. Verify failure with source-language fixture path.
4. **DecodeFailedAt(M)**: corrupt JSON syntactically (stray `,`). Verify `DecodeFailedAt(M)`.

### T5 — Final commit prep

- Re-run `./sbtgen.sc --js`. Verify `git diff project/plugins.sbt` empty.
- Cross-build: `sbt -batch + verifyGoldens`, `+ runWireFixtures` (still green), `+ runCrossLangInterop` (new).
- Single commit on `wip/necromancy`.
