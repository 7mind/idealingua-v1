# Regression Harness for idealingua-v1 — Plan (2026-05-12)

## 1. Goal & non-goals

### Goal
A reusable harness invoked as a one-shot CLI that, given:
- a target 3rd-party project root holding `.domain`/`.model` files in idealingua-v1 syntax;
- two `idlc` references (commit/tag or pre-built launcher);
- a target language (Scala, Typescript, or C#);

emits two streams of canonical wire-serialized samples (one per compiler reference) and diffs them. Identical output proves the language backend is wire-stable across the two compiler versions. Diverging output prints a per-wireId diff.

### Non-goals
- Not a unit-test framework for the compiler. The four FROZEN harness contracts (`regenerateGoldens` / `verifyGoldens` / `runWireFixtures` / `runCrossLangInterop`) remain untouched.
- Not a fuzzer. Sample app is deterministic.
- Not a runtime-RPC verifier. Only serialization codecs are exercised.
- No protobuf/Go (deleted in IMPL-8).
- No cross-language interop in this tool — that lives in the existing harness's `runCrossLangInterop`.

## 2. Pre-locked decisions

### D1 — Location: top-level `regression-harness/` directory, **not** an sbt subproject
- Rationale: the tool is a *meta-tool* that builds the compiler twice (at different commits). If it were an sbt subproject of the very repo it's checking out, every `sbt regression-harness/run` would intersect the worktree the harness manipulates. A standalone tree avoids reentrancy.
- The harness itself is a Scala 3 **scala-cli** script (`regression-harness/Harness.scala`) — single-file, no sbt build, runs via `scala-cli run regression-harness/Harness.scala -- <args>`. Falls back to plain JVM with `coursier launch` if scala-cli is unavailable in nix shell.
- The cached LLM-generated sample apps live with the **project being tested**, under `<project>/.idl-regression/`.
- Per-run scratch under `target/regression-harness/<runId>/` in the harness repo root.

### D2 — Compiler-version selection: Option A (clone-to-worktree + sbt stage), with Option B as opt-in
- Rationale: most general. Accepts `git:<sha>`, `git:<tag>`, or `path:/abs/path/to/idlc-launcher` for each side.
- For `git:` refs the harness uses **`git worktree add`** (not `git clone`) on the current repo, then runs `sbt idealingua-v1-compiler/stage` inside the worktree. `JavaAppPackaging` is already enabled (build.sbt:1410), producing `target/universal/stage/bin/idealingua-v1-compiler`. Worktrees share `.git/` so this is fast and storage-cheap.
- Caching: worktree builds are cached by short-sha under `target/regression-harness/cache/idlc/<sha>/`. Second invocation against the same sha skips the rebuild.
- Cleanup is opt-in (`--keep-worktrees` default off); `git worktree remove` after the run.

### D3 — LLM-generated sample app: **claude CLI** subprocess, cached per project
- Rationale: this very harness is a Claude project; relying on the `claude` CLI piggybacks on existing user auth, no extra API key plumbing. The harness shells out as `claude -p <prompt> --output-format text --permission-mode acceptEdits` capturing stdout.
- Fallback: if `claude` is not on PATH, harness exits with a clear message and emits the prompt + context tarball to `target/regression-harness/<runId>/llm-request.tar` so the user can hand-roll the sample app and place it at the cache path.
- The generated sample app source lives at `<project>/.idl-regression/sample_app.<lang>` and is **committed to the target project's repo** (`.gitignore` exempts only `.idl-regression/scratch/`). Subsequent runs reuse it verbatim, no LLM round-trip. Regeneration is explicit (`--regen-sample-app`).
- Cache key includes: SHA-256 of all `.domain`/`.model` files + the target language. If the input IDL changes, the harness refuses to use the stale cached sample and exits asking for `--regen-sample-app`.

### D4 — Per-language build/run: out-of-tree per-lang templates under `regression-harness/templates/<lang>/`
- **Scala**: scala-cli single-file project. The harness writes a top-level `project.scala` (using-directives for scala 3, jvm 21, `//> using lib "io.7mind.izumi::idealingua-v1-runtime-rpc-scala_3:<runtimeVer>"`, etc.), drops the generated sources, drops `sample_app.scala`. Run: `scala-cli run <dir>`.
- **Typescript**: bun-based runner (already a project dependency hint based on `dts/bun` traces in `wip/necromancy` follow-up plans; if unavailable, falls back to `tsx`). The template is `package.json` + `tsconfig.json`. Run: `bun run sample_app.ts` (with the generated TS package as a local file: dep).
- **C#**: `dotnet` script. Template is a `Driver.csproj` mirroring `idealingua-v1-test-harness/src/main/csharp/Driver.csproj`. Run: `dotnet run --project <dir>`.
- The runtime version is bound to the compiler-version commit via the existing `version.sbt` of the harness's `idealingua-v1-runtime-rpc-*` projects. For the "current" idlc the harness publishes the runtime artifacts **locally** to a per-run ivy/maven cache (`sbt 'publishLocal'` inside the worktree). For pre-built `idlc` launchers (Option B), the harness reads a sibling `version.txt` to know what runtime to depend on; if absent, requires `--runtime-version <ver>`.

### D5 — Output canonicalization: one line per (wireId, scenario), sorted, NDJSON
- Wire format: `<wireId>\t<scenario>\t<canonical-json-noSpaces>\n`.
- `wireId` is the fully-qualified IDL type name (e.g. `idltest.dtofields.Point`), matching the existing `WireFixtures.FixtureFile.wireId` convention (`WireFixtures.scala:18`).
- Lines sorted lexicographically by `(wireId, scenario)`. The sample app emits sorted; the harness verifies via post-sort.
- JSON whitespace: `noSpaces` everywhere; the harness re-parses each line and reserializes with a canonical encoder (circe `Json.printWith(Printer.noSpaces.copy(sortKeys=true))`) before diffing so cosmetic key-order/whitespace cannot mask matches.
- Why two columns (wireId + scenario): one `wireId` can have multiple deterministic scenarios; the existing `FixtureSeeder.scala:67` already follows this convention with `(wireId, scenario, encodedJson)` tuples — we adopt the exact same shape so an existing harness type can be reused by the new tool if it ever moves in-tree.

### D6 — Reusability: project path is a CLI argument, not built-in
- The harness embeds no test corpus. Its self-test uses `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source` (already known canonical IDL) as a sample 3rd-party target.

### D7 — Self-test corpus: `main-tests/source` against itself (sanity) + IMPL-9 vs HEAD (regression baseline)
- Self-test #1 (`./regression-harness/selftest.sh sanity`): run HEAD vs HEAD with Scala. Expected: zero divergences. This catches non-determinism inside the harness itself (e.g. random UUIDs in the sample-app LLM output that snuck past the determinism check).
- Self-test #2 (`./regression-harness/selftest.sh impl9-vs-head`): run `git:ea697f5` (IMPL-9 default-flip commit) vs HEAD with Scala. Expected: a *known* set of divergences (or zero — depending on whether IMPL-9..IMPL-13 changed wire output). The accepted set is committed at `regression-harness/selftest-expectations/impl9-vs-head.scala.json`; CI diff against this artifact is how we know the harness itself didn't regress.

### D8 — CI integration: out of scope for v1, designed-for-future
- The harness exits with code 0 (no divergences) or 1 (divergences found) or 2 (build/setup failure). CI wiring is a one-liner in a future PR — not blocking this plan.

## 3. Architecture overview

```
                +-------------------- harness ----------------------------+
                |                                                         |
   inputs:      |  1. argparse:                                           |
   - project    |     --project <path>                                    |
   - "old" ref  |     --old git:<sha>|path:<launcher>                     |
   - "new" ref  |     --new git:<sha>|path:<launcher>                     |
   - lang       |     --lang scala|typescript|csharp                      |
                |                                                         |
                |  2. resolve idlc(old) -> launcher path                  |
                |     resolve idlc(new) -> launcher path                  |
                |     (each: worktree add + sbt stage, cached by sha)     |
                |                                                         |
                |  3. for each side {old, new}:                           |
                |        run idlc -- generate <lang> into scratch/<side>/ |
                |        publishLocal runtime for that side               |
                |                                                         |
                |  4. obtain sample-app:                                  |
                |        if <project>/.idl-regression/sample_app.<lang>:  |
                |            reuse                                        |
                |        else:                                            |
                |            invoke `claude -p` with prompt + context     |
                |            -> write sample_app.<lang>                   |
                |                                                         |
                |  5. for each side {old, new}:                           |
                |        materialize template + generated + sample_app    |
                |        build + run -> capture stdout                    |
                |        canonicalize -> wire/<side>.ndjson               |
                |                                                         |
                |  6. diff wire/old.ndjson vs wire/new.ndjson:            |
                |        - per-(wireId, scenario) compare                 |
                |        - emit human report + machine report             |
                |        - exit 0 / 1                                     |
                +---------------------------------------------------------+
```

## 4. CLI surface

```
idl-regress --project <path>
            --old <ref>
            --new <ref>
            --lang scala|typescript|csharp
            [--out <dir>]
            [--regen-sample-app]
            [--keep-worktrees]
            [--runtime-version <ver>]           # only with --old/--new path:...
            [--llm-cli <claude|stub|none>]      # default: claude
            [--format human|json|both]          # default: both
            [--fail-on-divergence]              # default: true
```

`<ref>` syntax:
- `git:<sha-or-tag-or-branch>` — git worktree + sbt stage path.
- `path:<absolute-path-to-launcher-or-jar>` — pre-built idlc.
- `self` — current working tree (no worktree, direct `sbt idealingua-v1-compiler/stage`).

Exit codes: `0` = identical, `1` = divergence, `2` = setup error, `3` = LLM unavailable + no cache.

## 5. File-by-file deliverables

| Path | LOC est | Purpose |
|---|---|---|
| `regression-harness/Harness.scala` | 600 | main entrypoint, argparse, orchestration |
| `regression-harness/IdlcResolver.scala` | 180 | git-worktree + sbt-stage caching, path resolution |
| `regression-harness/SampleAppGen.scala` | 200 | claude CLI invocation, prompt construction, caching |
| `regression-harness/LangAdapter.scala` | 60 | sealed trait + Scala/TS/C# impls dispatch |
| `regression-harness/adapters/ScalaAdapter.scala` | 220 | scala-cli template, build, run, capture |
| `regression-harness/adapters/TypescriptAdapter.scala` | 180 | bun/tsx template, build, run, capture |
| `regression-harness/adapters/CsharpAdapter.scala` | 200 | dotnet template, build, run, capture |
| `regression-harness/Canonicalize.scala` | 120 | NDJSON canonical-form parser + sort + circe re-emit |
| `regression-harness/Diff.scala` | 140 | line-by-line diff, per-wireId report, human + machine output |
| `regression-harness/templates/scala/project.scala` | 30 | scala-cli using-directives template |
| `regression-harness/templates/typescript/package.json` | 20 | bun config template |
| `regression-harness/templates/typescript/tsconfig.json` | 30 | TS compiler config |
| `regression-harness/templates/csharp/Driver.csproj` | 25 | dotnet project template |
| `regression-harness/prompts/sample-app-scala.md` | 80 | LLM prompt template, Scala |
| `regression-harness/prompts/sample-app-typescript.md` | 80 | LLM prompt template, TS |
| `regression-harness/prompts/sample-app-csharp.md` | 80 | LLM prompt template, C# |
| `regression-harness/selftest.sh` | 60 | sanity + impl9-vs-head invocations |
| `regression-harness/selftest-expectations/impl9-vs-head.scala.json` | committed artifact | known-good divergence set |
| `regression-harness/README.md` | 200 | usage + design summary |
| `docs/drafts/20260512-regression-harness-plan.md` | this doc | spec |

**Total new code: ~2100 LOC of Scala, ~500 LOC of templates/prompts/docs.** Single-language (Scala 3 scala-cli) keeps it inspectable. No new sbt subproject means no integration cost with the build aggregate.

## 6. LLM invocation contract

### 6.1 Prompt template (sketch, per language)

The Scala prompt (`prompts/sample-app-scala.md`) is rendered with three substitutions: `{{IDL_TREE}}`, `{{GENERATED_TREE}}`, `{{RUNTIME_VERSION}}`. Pseudocode of the rendered prompt:

```
You are generating ONE Scala 3 file: sample_app.scala.

Inputs:
  - The IDL source tree below (.domain / .model files).
  - The Scala source tree generated by the idealingua-v1 compiler from those IDL files.
  - A pinned runtime version: {{RUNTIME_VERSION}}.

Task:
  1. Enumerate every top-level type in the IDL: DTO, identifier, ADT, interface, enum, alias.
  2. For each type, construct ONE deterministic instance using fixed literal values
     (no random, no time-of-day, no UUID.randomUUID).
  3. Use the SAME fixed literal scheme as
     idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/FixtureSeeder.scala
     so cross-checking against existing fixtures is possible. Specifically:
       - integers: 1, 2, 3, ...
       - strings: "s1", "s2", ...
       - UUID #1 = 3a7f0c12-1234-5678-9abc-fedcba987654 (FixtureSeeder.scala:53)
       - timestamp = 2025-01-15T10:30:45.000Z
  4. Encode each instance to JSON via the generated circe encoder (.asJson.noSpaces).
  5. Print a line per instance in the form:
       <wireId>\t<scenario>\t<json>
     where wireId is the fully-qualified type name and scenario is "basic" for the
     first instance of each type. For ADT branches, emit one line per branch with
     scenario = branch name.
  6. Sort lines by (wireId, scenario) before printing.
  7. Top-level: `@main def run(): Unit = { ... }`.
  8. NO imports beyond the generated package and circe syntax. NO build files
     (the harness supplies project.scala).
  9. Output ONLY the source of sample_app.scala. No markdown fence. No commentary.

IDL_TREE:
{{IDL_TREE}}

GENERATED_TREE:
{{GENERATED_TREE}}
```

`{{IDL_TREE}}` is the concatenation of every `.domain`/`.model` file with `=== <relpath> ===` headers (sorted). Capped at 200 KB; if larger, the harness errors with a clear message — out of scope to summarize.

`{{GENERATED_TREE}}` is the **smaller** of the two sides (we have generated for both — they should be near-identical; we pick the "new" side for the prompt). Capped at 800 KB.

### 6.2 Invocation

```
claude -p "$(cat prompt.txt)" \
       --output-format text \
       --permission-mode acceptEdits \
       --model claude-opus-4-7 \
       > sample_app.<lang>.candidate
```

Then a **lint pass**: harness checks the candidate file with a quick grep for forbidden non-determinism:
- `randomUUID`, `Random(`, `Instant.now`, `System.currentTimeMillis`, `Math.random`, `new Date()` (TS), `DateTime.Now` (C#), `Guid.NewGuid` (C#). Any hit → exit 3 with the offending line.

If lint passes, the candidate is moved to `<project>/.idl-regression/sample_app.<lang>` and committed by the user (harness doesn't auto-commit).

### 6.3 Caching strategy

Cache file: `<project>/.idl-regression/sample_app.<lang>`. Cache metadata: `<project>/.idl-regression/sample_app.<lang>.meta.json`:
```json
{
  "idl_sha256": "abc123...",   // sha256 over (sorted-rel-path + sha256 of contents) of every .domain/.model
  "lang": "scala",
  "generated_by": "claude-opus-4-7",
  "generated_at": "2026-05-12T10:00:00Z",
  "prompt_template_sha": "..."  // sha of prompts/sample-app-scala.md
}
```

Cache hit requires: `idl_sha256` match AND `prompt_template_sha` match. Either mismatch ⇒ refuse stale cache; print exact instruction to re-run with `--regen-sample-app`.

### 6.4 Error handling

- `claude` binary missing → exit 2 with instructions to install or supply `--llm-cli stub` for a manual loop.
- LLM output non-empty but fails lint → write to `.candidate` + exit 3, ask user to inspect.
- LLM output empty → exit 3 with full stderr.
- LLM output compiles but `sample_app` exits non-zero / produces no lines → exit 3 (this is the "LLM produced syntactically valid but wrong" case); user inspects the captured stderr.

## 7. Per-language build templates

### 7.1 Scala (scala-cli)

`templates/scala/project.scala`:
```scala
//> using scala 3.8.3
//> using jvm 21
//> using dep "io.7mind.izumi::idealingua-v1-runtime-rpc-scala:{{RUNTIME_VERSION}}"
//> using dep "io.circe::circe-core:{{CIRCE_VERSION}}"
//> using dep "io.circe::circe-generic:{{CIRCE_VERSION}}"
//> using dep "io.circe::circe-parser:{{CIRCE_VERSION}}"
//> using resolver "ivy2Local"
```

The harness substitutes `{{RUNTIME_VERSION}}` (from `version.sbt` of the worktree at build time) and `{{CIRCE_VERSION}}` (from `project/Versions.scala`). Generated sources from the idlc go in `src/main/scala/`. `sample_app.scala` goes in the project root.

Build+run: `scala-cli run <projectDir>` — stdout captured.

### 7.2 Typescript (bun preferred, tsx fallback)

`templates/typescript/package.json`:
```json
{
  "name": "idl-regression-sample",
  "type": "module",
  "dependencies": {
    "idealingua-v1-runtime-rpc-typescript": "file:./generated/typescript"
  },
  "devDependencies": {
    "typescript": "^5.6.0"
  }
}
```

The idlc-generated TS already bundles its runtime under `with-runtime`; the harness chooses `--withRuntime` invocation. `sample_app.ts` imports from `./generated/typescript/<package>`.

Build+run: `bun install --no-save && bun run sample_app.ts` — stdout captured.

### 7.3 C# (dotnet)

`templates/csharp/Driver.csproj`: mirrors `idealingua-v1-test-harness/src/main/csharp/Driver.csproj` exactly, including the `Newtonsoft.Json` dependency the existing harness uses (so wire-format codec is identical to the FROZEN harness's C# driver).

Build+run: `dotnet run --project <projectDir>` — stdout captured.

## 8. Output canonicalization spec

Each sample-app run emits raw lines `<wireId>\t<scenario>\t<json>` to stdout. The harness post-processes:

1. Discard blank lines & lines starting with `#`.
2. Validate every remaining line matches the regex `^[A-Za-z0-9_.]+\t[A-Za-z0-9_-]+\t.+\n?$`.
3. Parse `<json>` with circe; on failure → record a parse error for this side at this `(wireId, scenario)` (do NOT abort; the diff phase will show the parse-error as a divergence kind).
4. Re-emit with `Printer.noSpaces.copy(sortKeys = true).print(json)`. This neutralizes whitespace + key-ordering differences that aren't true wire divergences.
5. Sort all lines by `(wireId, scenario)` lexicographically.
6. Write to `wire/<side>.ndjson`.

The diff is then a strict line-by-line compare of `wire/old.ndjson` vs `wire/new.ndjson`. Three divergence kinds:
- **MISSING**: `(wireId, scenario)` present on one side, absent on the other (sample-app might emit extra types if the new compiler exposes new derived types).
- **CONTENT**: same `(wireId, scenario)` on both sides, different canonical JSON.
- **PARSE-ERROR**: one side produced unparseable JSON; the other side parsed.

Human report (stdout):
```
==============================================
idl-regression: scala
old: git:ea697f5 (IMPL-9)
new: self (PR-02 IMPL-13 ⇨ HEAD)
==============================================
3 divergences across 2 wireIds:

[CONTENT] idltest.dtofields.Point / basic
  old: {"export":true,"h":20,"id":"abc","name":"point-1","ownfield":"of","w":10,"x":3,"y":4}
  new: {"h":20,"id":"abc","name":"point-1","ownfield":"of","w":10,"x":3,"y":4,"export":true}

[MISSING in old] idltest.algebraics.NewBranch / branch
  new: {...}
...
```

Machine report (`<out>/report.json`):
```json
{
  "lang": "scala",
  "old": "git:ea697f5",
  "new": "self",
  "divergences": [
    {"kind":"CONTENT","wireId":"idltest.dtofields.Point","scenario":"basic","old":"...","new":"..."},
    {"kind":"MISSING_IN_OLD","wireId":"idltest.algebraics.NewBranch","scenario":"branch","new":"..."}
  ]
}
```

## 9. Test plan for the harness itself

### 9.1 Selftest sanity (HEAD vs HEAD)
Target: `idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source`. Build idlc once at HEAD, generate against both sides, compare. Must produce zero divergences. Catches: non-determinism in sample app, non-determinism in canonicalization, missing sort.

### 9.2 Selftest IMPL-9 vs HEAD
Same target. Old: `git:ea697f5`. New: `self`. Run produces some divergence set; compare against the committed expectation `regression-harness/selftest-expectations/impl9-vs-head.scala.json`. The expectation file is **committed** so future PRs that change wire output have to update it consciously.

Initially the expectation is empty — IMPL-9 through IMPL-13 are typer modernization with the explicit invariant "wire format preserved byte-strict" (per modernization plan M2 description in `tasks.md`). If non-empty divergences appear, that's a real finding the user needs to evaluate.

### 9.3 Manual rotation matrix (one-off, not in CI)
Add a `selftest.sh matrix` mode that runs all three languages against `main-tests`, both sanity and impl9-vs-head. Expected runtime ~10 min total. Not gating; just a sanity sweep before tagging.

### 9.4 Negative testing
A deliberate-divergence test: temporarily patch the LLM-cached sample app to emit a wrong JSON value, run sanity. The harness must exit code 1 and report the divergence at the patched type. Documented in README as "if you want to test the harness's diff path, here is how".

## 10. Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | LLM-generated sample app uses non-determinism (UUID.random, Instant.now, etc.) → false positives every run. | Lint pass blocks known non-deterministic APIs. Self-test sanity (HEAD vs HEAD) catches anything the lint misses. |
| R2 | `sbt stage` inside a checked-out worktree depends on snapshot artifacts only present in the **current** repo's ivy cache. | Each worktree gets a private ivy resolver `target/regression-harness/cache/idlc/<sha>/.ivy2`. Snapshot pollution avoided. |
| R3 | The "current" idlc's generated TS / C# code might depend on a runtime version that doesn't exist on Maven Central / npm yet. | Harness runs `sbt publishLocal` for the runtime modules inside each worktree, and the templates use `resolvers += Resolver.mavenLocal` / `file:` deps. |
| R4 | `claude` CLI rate limits or network failures mid-run. | Cache is keyed deterministically. After first successful sample-app generation, no further LLM calls. CI never invokes claude (only re-uses cached sample). |
| R5 | An IDL with cyclic / recursive structures may make the LLM-generated sample app non-terminating or stack-overflow. | Prompt explicitly says "construct ONE instance per type using NULL/None/empty-list for nested references where the same type appears in its own field graph". Lint can't catch this — only the runtime would. The runtime times out (5 min default, `--run-timeout` flag); timeout is a divergence. |
| R6 | `bun` not available on the target machine. | Adapter probes; falls back to `tsx`; clear error if neither is on PATH. |
| R7 | `git worktree add` on a dirty branch is annoying for the user. | Harness refuses if the worktree dir already exists, requires `--keep-worktrees=clean` to overwrite, prints precise cleanup command on exit. |
| R8 | Reentrancy: harness is in repo X, "old" ref is also from repo X. If the user runs sbt on X concurrently, ivy resolvers may collide. | Use per-sha ivy cache (see R2). Document "don't run other sbt against this repo while the harness is running" in README. |
| R9 | The four FROZEN harness contracts share infrastructure with the new harness (e.g. `WireFixtures.scala`, `FixtureSeeder.scala`). Accidental imports / drift may break them. | Strict policy: new harness does NOT depend on `idealingua-v1-test-harness` sources; it only mirrors the format conventions (wireId/scenario tab-separated NDJSON). The conventions are *documented* in the new harness's README so they don't drift silently. |
| R10 | LLM cost: regenerating sample app on every IDL edit is expensive for a fast-iterating 3rd-party project. | Cache by `idl_sha256` — only re-runs LLM when the IDL actually changed. The user can manually edit the cached sample app to handle small IDL deltas without LLM. |

## 11. Milestone breakdown

### M1 — Scaffold + Scala adapter end-to-end (~3 sessions)
- `Harness.scala` argparse, exit codes.
- `IdlcResolver.scala` git-worktree + sbt-stage caching, `self` mode only (no commit refs yet).
- `Canonicalize.scala`, `Diff.scala`.
- `LangAdapter.scala` + `adapters/ScalaAdapter.scala` (scala-cli template + capture).
- `prompts/sample-app-scala.md`.
- `SampleAppGen.scala` with `--llm-cli stub` (writes the prompt to disk, expects user to drop a sample_app.scala into the cache path).
- Selftest #1 (sanity HEAD-vs-HEAD with scala against `main-tests`, manual sample_app).
- Acceptance: `idl-regress --project .../main-tests --old self --new self --lang scala --llm-cli stub` → zero divergences after the user hand-drops a sample_app.

### M2 — Git-ref resolution + worktree cache (~1 session)
- Extend `IdlcResolver` to `git:<ref>` form.
- Per-sha ivy cache.
- `publishLocal` of runtime modules inside the worktree.
- Selftest #2 (IMPL-9 vs HEAD scala); commit empty expectation; verify zero divergences (or document what we find).

### M3 — LLM integration (claude CLI) (~1 session)
- `SampleAppGen.scala` real `claude -p` invocation.
- Lint pass for non-determinism.
- Cache-metadata file + invalidation logic.
- Acceptance: deleting `<project>/.idl-regression/sample_app.scala`, re-running, gets a freshly generated app, lint-clean, that reproduces zero divergences against itself.

### M4 — Typescript adapter (~2 sessions)
- `adapters/TypescriptAdapter.scala`, `templates/typescript/`, `prompts/sample-app-typescript.md`.
- Bun probe + tsx fallback.
- Selftest expansion: `main-tests` × typescript.
- Acceptance: TS HEAD-vs-HEAD sanity is clean.

### M5 — C# adapter (~2 sessions)
- `adapters/CsharpAdapter.scala`, `templates/csharp/`, `prompts/sample-app-csharp.md`.
- `dotnet run` invocation.
- Selftest expansion: `main-tests` × csharp.
- Acceptance: C# HEAD-vs-HEAD sanity is clean.

### M6 — Polish + docs (~1 session)
- `README.md` with quickstart + decision tree.
- `selftest.sh matrix` mode.
- Exit-code semantics smoke test.
- Optional: dry-run mode (`--dry-run` prints the plan without executing).

**Total estimated effort: 10 sessions of implementation. M1 is the largest single chunk (~3 sessions) because it lays all the infra; M2-M6 are incremental.**
