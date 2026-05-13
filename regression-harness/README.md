# regression-harness

Wire-format regression harness for idealingua-v1. Given a 3rd-party project
whose `.domain` / `.model` files compile through `idlc`, the harness builds
two versions of `idlc`, generates from both, runs a hand-or-LLM-authored
sample app under each, and diffs canonical NDJSON streams. Identical output
proves the language backends are wire-stable across the two compiler versions.

M6 status: Scala + TypeScript + C# adapters. `--old` / `--new` accept
`self`, `git:<sha|tag|branch>`, or `path:<launcher-or-stage-dir>`. Per-sha
cache for staged launchers + locally-published Scala runtime artifacts.
publishM2 short-circuits for non-Scala targets. `matrix` selftest mode.
LLM invocation is still manual: the harness emits a prompt and exits with
code 3 the first time a project is encountered.

## Status

| Component                | M1 | M2 | M4 | M5 | M6 | Beyond            |
|--------------------------|----|----|----|----|----|-------------------|
| Scala adapter            | ok | ok | ok | ok | ok | —                 |
| TypeScript adapter       | —  | —  | ok | ok | ok | —                 |
| C# adapter               | —  | —  | —  | ok | ok | —                 |
| `self` ref               | ok | ok | ok | ok | ok | —                 |
| `git:<sha>` ref          | —  | ok | ok | ok | ok | —                 |
| `path:<launcher>` ref    | —  | —  | —  | —  | ok | —                 |
| publishM2 short-circuit  | —  | —  | —  | —  | ok | —                 |
| `--dry-run`              | —  | —  | —  | —  | ok | —                 |
| `selftest.sh matrix`     | —  | —  | —  | —  | ok | —                 |
| Full-corpus sample apps  | —  | —  | —  | —  | —  | LLM-generated     |
| Automatic LLM invocation | —  | —  | —  | —  | —  | `--llm-cli <cli>` |
| CI integration           | —  | —  | —  | —  | —  | GitHub Actions    |

## Quickstart

Three sample invocations covering the common workflows.

### A. Sanity (HEAD vs HEAD)

Detects non-determinism in the harness, sample app, or canonicalization
itself. Should always print zero divergences.

```bash
./regression-harness/idl-regress \
  --project idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests \
  --old self \
  --new self \
  --lang scala
```

### B. IMPL-9 (frozen baseline) vs HEAD

The committed regression baseline. The `dtofields-only` subcorpus is
deliberately narrow so the IMPL-9 idlc (which precedes F5 / TBLOB
normalization) compiles it cleanly.

```bash
./regression-harness/selftest.sh impl9-vs-head
./regression-harness/selftest.sh impl9-vs-head-ts
./regression-harness/selftest.sh impl9-vs-head-cs
# or all six cells in one go:
./regression-harness/selftest.sh matrix
```

### C. Custom git refs against a third-party project

```bash
./regression-harness/idl-regress \
  --project /path/to/external-project \
  --old git:v1.4.18 \
  --new git:wip/feature-branch \
  --lang typescript \
  --keep-worktrees           # retain per-sha worktree for diagnosis
```

## When to use which `<ref>` form

```
       Where does the idlc binary come from?
                        |
       ┌────────────────┼────────────────────┐
       │                │                    │
   currently        a known            already built
   checked out      commit/tag         somewhere on disk
       │                │                    │
       v                v                    v
     self          git:<ref>           path:<launcher>
       │                │                    │
       │  uses the      │  git worktree      │  no build step;
       │  working tree  │  add --detach +    │  reads version
       │  + sbt stage   │  sbt stage; cached │  from sibling
       │  if needed     │  by short-sha      │  version.txt or
       │                │                    │  --runtime-version
       v                v                    v
```

| Form              | Use when                                       | Cost                                |
|-------------------|------------------------------------------------|-------------------------------------|
| `self`            | Iterating on the current branch                | One `sbt stage` if not already done |
| `git:<ref>`       | Comparing against a historical commit/tag      | One worktree-add + sbt stage per sha (cached) |
| `path:<launcher>` | You have a pre-built launcher (CI artifact, manual build, a cached entry from a prior run) | Free; no sbt invocation             |

`path:` example: the M6 path: form can re-use launchers from a previous
`git:` run without rebuilding —

```bash
./regression-harness/idl-regress \
  --project /path/to/external-project \
  --old path:target/regression-harness/cache/idlc/<short-sha>/stage/bin/idealingua-v1-compiler \
  --new self \
  --lang scala \
  --runtime-version 1.4.18
```

If a `version.txt` is present next to the launcher (or at the stage root),
`--runtime-version` is auto-derived. Per-sha caches written by `git:<ref>`
populate `runtimeVersion.txt` alongside the launcher — copy that to
`<stage>/version.txt` to satisfy auto-detect.

## LLM workflow (sample app generation)

Sample-app generation is **not auto-invoked** in M6. The harness emits a
prompt and exits 3; an operator (human or LLM) drives the LLM round-trip.

1. First run on a fresh project prints:

   ```
   [idl-regress] === manual sample-app step required ===
   [idl-regress] prompt written to: target/regression-harness/<runId>/sample-app-prompt.scala.md
   [idl-regress] sentinel:          target/regression-harness/<runId>/PROMPT_READY.txt
   ```

2. Pass the prompt to your LLM of choice. The prompt is self-contained: it
   embeds the IDL tree, the generated source tree, and the pinned runtime
   version.

   **Manual (any LLM)**: feed the prompt file as the entire user message.

   **Claude CLI (if you have `claude` on PATH)**:

   ```bash
   PROMPT="$(cat target/regression-harness/<runId>/sample-app-prompt.scala.md)"
   claude -p "$PROMPT" --output-format text --permission-mode acceptEdits \
     > /tmp/sample_app.scala
   ```

   **Anthropic API directly (curl)**:

   ```bash
   curl https://api.anthropic.com/v1/messages \
     -H "x-api-key: $ANTHROPIC_API_KEY" \
     -H "anthropic-version: 2023-06-01" \
     -H "content-type: application/json" \
     -d @<(jq -n \
       --arg p "$(cat target/regression-harness/<runId>/sample-app-prompt.scala.md)" \
       '{model:"claude-opus-4-7-20260101", max_tokens:8192, messages:[{role:"user", content:$p}]}'
     )
   ```

3. Place the rendered file at `<project>/.idl-regression/sample_app.<lang>`
   and write a metadata sidecar:

   ```bash
   echo "idl_sha256=$(grep idl_sha256 .../<runId>/PROMPT_READY.txt | cut -d= -f2)" \
     > <project>/.idl-regression/sample_app.<lang>.meta
   ```

4. Re-run the same `idl-regress` command. The harness reuses the cached
   sample app verbatim. The IDL sha is checked; if you change the IDL, the
   sidecar's `idl_sha256` mismatches and the harness refuses to use the
   stale sample (re-run with `--regen-sample-app`).

### Sample-app determinism contract

The prompt template instructs the LLM to:

- Use fixed literal values only (no `Instant.now`, `UUID.randomUUID`,
  `Random`, `Math.random`, `new Date()`, `DateTime.Now`, `Guid.NewGuid`).
- Sort output by `(wireId, scenario)` lexicographically before emit.
- Emit `<wireId>\t<scenario>\t<json>` one line per typed instance.

The harness does NOT yet enforce these via a lint pass (planned post-M6);
the sanity selftest (HEAD vs HEAD) is the canary — any non-determinism
shows up as divergences in zero-diff territory.

## Per-sha cache layout

```
target/regression-harness/cache/idlc/<short-sha>/
  stage/                # full sbt-native-packager stage tree
  stage/bin/idealingua-v1-compiler   # launcher (executable)
  runtimeVersion.txt    # version captured at publish time
  m2/                   # local maven repo, populated by publishM2
                        # (Scala only; absent for TS / C# resolutions)
  .ivy2/                # sbt-internal ivy cache (per plan R2)
  .coursier/            # sbt-internal coursier cache (per plan R2)
  .sbt/                 # sbt-internal global base
  worktree/             # ephemeral; removed unless --keep-worktrees
```

Per-run scratch under `target/regression-harness/<runId>/` is never cached.
Safe to delete in bulk:

```bash
rm -rf target/regression-harness/
```

Subsequent runs rebuild the cache from scratch.

## Exit codes

| Code | Meaning                                                       | When                                          |
|------|---------------------------------------------------------------|-----------------------------------------------|
| 0    | Identical wire output (or `--no-fail-on-divergence`)          | Success                                       |
| 1    | Wire-format divergences detected                              | `--fail-on-divergence` (default)              |
| 2    | Build / setup failure (sbt, dotnet, scala-cli, idlc, etc.)    | Any infrastructure-level failure              |
| 3    | Sample-app missing or stale; prompt emitted                   | Operator must run the LLM and re-invoke       |
| 126  | CLI usage error                                               | Unknown flag, malformed `--lang`, etc.        |

`--dry-run` prints the plan and exits 0 regardless of state.

## Troubleshooting (surprises encountered during M1-M6)

### Scala adapter

- **`sbt-native-packager` eats short flags.** The staged launcher's bash
  wrapper consumes `-d`, `-v`, `-h`, `-nz` for its own debug/verbose/help/
  no-zip toggles before passing args to the Java application. The harness
  uses long-form options exclusively (`--root`, `--source`, `--target`,
  `--disable-zip`, `--define=key=value`). If you craft a manual invocation,
  use the same long forms.
- **`scala-cli` syntax: `using repository`, not `using resolver`.** The
  earlier scala-cli releases used `//> using resolver`; M2+ standardized on
  `//> using repository`. The Scala template (`templates/scala/project.scala.template`)
  uses the modern form.
- **`circeVersion` is currently pinned in `ScalaAdapter.CirceVersion`.**
  Reading it from a generated sbt manifest is deferred — see "Known
  follow-ups" below.
- **`++ 2.13.18` after `stage`.** The generated Scala depends on circe-
  derivation (Scala-2-only); the publishM2 step crosses to 2.13.18 explicitly.
  The `stage` step runs at the project's default Scala (3.8.3); the launcher
  itself is dialect-agnostic.

### TypeScript adapter

- **`tsx` is required.** The TS runner probe is `bun` → `npm`. With `npm`
  the adapter assumes `tsx` is installed locally via `package.json`'s
  `devDependencies` — a globally-installed `tsx` is NOT consulted. The
  template pins `tsx`.
- **ESM `strip-types` issue (Node 22.6+).** Native `node --experimental-strip-types`
  is finicky with the generated TS output's relative-import resolution.
  Prefer `bun` if you have it, otherwise let the adapter use `npm` + the
  locally-installed `tsx`.
- **`withRuntime=true` inlines IRT.** TypeScript does not consume a per-sha
  m2 dir — the idlc emits the entire IRT runtime tree alongside the
  generated modules. The TS adapter does NOT trigger publishM2 in M6
  (publishM2 short-circuit, Item A).

### C# adapter

- **`EnableDefaultCompileItems=false` in the csproj.** The .NET SDK's
  default glob picks up `bin/`, `obj/`, and stale artifacts that collide
  with the harness's explicit `<Compile>` items. Disabling default items
  is the only reliable fix.
- **IRT/Transport excluded.** The transport subtree pulls
  `WebSocketSharp` which is not on the default nuget feed for `net9.0`.
  The adapter's csproj template excludes the entire `IRT/Transport/` subtree
  and `IRT/UrlEscaper.cs` (depends on `System.Web`, not auto-referenced by
  `net9.0`). Sample apps must not call into those.
- **`Newtonsoft.Json` 13.0.3 pinned.** This matches the FROZEN test-harness
  Driver.csproj. The harness does NOT consume a per-sha NuGet feed — no
  publishM2 triggered for C# resolutions.
- **`withRuntime=true` for C# too.** Like TypeScript, idlc emits the IRT
  runtime under `<target>/csharp/IRT/`. C# resolutions skip publishM2.

### Stale agent worktrees

The `.claude/worktrees/agent-*` directories are created by Claude Code
subagents and can accumulate. Prune them periodically:

```bash
# Inspect:
git worktree list
# Remove stale entries:
git worktree prune
# Force-remove a specific worktree:
git worktree remove --force .claude/worktrees/agent-<id>
```

The harness's own per-sha cache directories under
`target/regression-harness/cache/idlc/<sha>/worktree/` are physical-only
(no git registration after the build phase) — they live or die with
`rm -rf target/regression-harness/`.

### Recovering from a failed `git worktree add`

If the cache dir already exists but the worktree was never registered
(e.g. a previous run crashed mid-way), the resolver will:

1. `git worktree remove --force <path>` (no-op if already gone)
2. `git worktree prune` (no-op if no stale entries)
3. `rm -rf <path>` physically
4. `git worktree add --detach <path> <sha>` fresh

If that still fails, the resolver prints the exact `git worktree remove`
command to run by hand and exits 2.

## CI integration sketch (deferred)

GitHub Actions wiring is intentionally out of scope for M6. A future PR
should add a workflow like:

```yaml
# .github/workflows/regression-harness.yaml (sketch — not landed)
name: regression-harness
on: [pull_request]
jobs:
  matrix:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 0 }   # need history for git: refs
      - uses: cachix/install-nix-action@v27
      - run: nix develop --command ./regression-harness/selftest.sh matrix
        env:
          SBT_OPTS: "-Xmx4g"
```

The cache directory `target/regression-harness/cache/idlc/` is a natural
candidate for `actions/cache` keyed on the relevant shas.

## Requirements

- `sbt` and `git` on PATH (`nix-shell` provides them).
- `scala-cli` on PATH for `--lang scala` (`nix-shell` provides ~1.10).
- JDK 21 (`nix-shell`).
- For `--lang typescript`: `bun` OR `npm` + `node`. `nix-shell` ships
  `nodejs_24`; the adapter pins `tsx` via the package.json template so
  no global install is needed when running through `npm`.
- For `--lang csharp`: `dotnet` (≥ 8.0). `nix-shell` provides 9.0.
- A clean working tree if you intend `git worktree add` from this repo;
  the harness refuses to add a worktree at an already-occupied path.

## Files

| Path                                          | Purpose                                  |
|-----------------------------------------------|------------------------------------------|
| `Harness.scala`                               | entrypoint, argparse, orchestration      |
| `IdlcResolution.scala`                        | resolved per-side triplet                |
| `IdlcResolver.scala`                          | self/git/path resolution + per-sha cache |
| `SampleAppGen.scala`                          | sample-app cache + prompt rendering      |
| `LangAdapter.scala`                           | per-language build/run interface         |
| `adapters/ScalaAdapter.scala`                 | scala-cli driven Scala impl              |
| `adapters/TypescriptAdapter.scala`            | bun / npm+tsx TS impl                    |
| `adapters/CsharpAdapter.scala`                | dotnet-driven C# impl                    |
| `Canonicalize.scala`                          | NDJSON parser + canonical re-emit        |
| `Diff.scala`                                  | line-by-line diff + report               |
| `templates/scala/project.scala.template`      | scala-cli using-directives template      |
| `templates/typescript/package.json`           | TS package template (deps for IRT)       |
| `templates/typescript/tsconfig.json`          | TS compiler config template              |
| `templates/csharp/Driver.csproj`              | dotnet project template                  |
| `prompts/sample-app-{scala,typescript,csharp}.md` | LLM prompt templates                 |
| `idl-regress`                                 | bash wrapper around `scala-cli run`      |
| `selftest.sh`                                 | self-tests + matrix mode                 |
| `selftest-corpus/dtofields-only/`             | minimal in-tree IMPL-9-compatible corpus |
| `selftest-expectations/impl9-vs-head.*.json`  | committed baseline divergence sets       |

## Known follow-ups (post-M6)

1. **sbt-emitted version manifest** — replace pinned `CirceVersion` in
   `ScalaAdapter` with a read from a per-sha `runtime.properties` file
   emitted during publishM2. Deferred from M6 Item C as it requires a new
   sbt task (out of constraint #3).
2. **Automatic LLM invocation** — gated `--llm-cli claude|codex|…` flag
   that auto-runs the prompt and writes the candidate. The manual
   workflow stays the default.
3. **Lint pass for sample-app determinism** — grep the LLM candidate for
   forbidden non-deterministic APIs (`randomUUID`, `Instant.now`, etc.)
   before accepting it. Today only the sanity selftest catches it.
4. **Full main-tests-corpus sample apps** — the existing in-tree sample
   apps cover only `dtofields-only`. A full LLM round-trip against the
   28-file `main-tests/source/` corpus is the next coverage expansion.
5. **CI integration** — GitHub Actions wiring (see sketch above).
6. **Cross-machine cache portability** — the per-sha cache embeds
   absolute paths in some artifacts; relocating it across machines is not
   yet tested. Within a single host, deletion + rebuild is the workflow.
