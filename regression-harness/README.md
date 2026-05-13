# regression-harness

Wire-format regression harness for idealingua-v1. Given a 3rd-party project
whose `.domain` / `.model` files compile through `idlc`, the harness builds
two versions of `idlc`, generates from both, runs an LLM-authored sample app
under each, and diffs canonical NDJSON streams. Identical output proves
language backends are wire-stable across the two compiler versions.

M2 status: Scala adapter, `--old`/`--new` accept `self` or `git:<sha|tag|branch>`.
Per-sha cache for staged launchers + locally-published runtime artifacts.
Automatic LLM invocation is still deferred (M3).

## Status

| Component            | M1 | M2  | M3+ (planned)                           |
|----------------------|----|-----|-----------------------------------------|
| Scala adapter        | ok | ok  | —                                       |
| Typescript adapter   | —  | —   | bun + scala-cli-style template          |
| C# adapter           | —  | —   | dotnet script template                  |
| `self` ref           | ok | ok  | —                                       |
| `git:<sha>` ref      | —  | ok  | —                                       |
| `path:<launcher>`    | —  | —   | pre-built launcher, skip build entirely |
| `--regen-sample-app` | partial — exits 3 with prompt | same | optional LLM CLI invocation |
| Self-test corpus     | manual hand-roll | manual hand-roll | full LLM-generated coverage |

## Workflow

Invoke via the `idl-regress` wrapper. Per-side build artifacts are cached
under `target/regression-harness/cache/idlc/<short-sha>/`; subsequent runs at
the same sha skip the build.

### Stage 1 — first run on a fresh project

```bash
./regression-harness/idl-regress \
  --project idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests \
  --old git:ea697f5 --new self --lang scala
```

The harness:

1. Resolves each side to an `IdlcResolution` (launcher + local runtime
   repository + runtime Maven version).
   - `self`: stages `idealingua-v1-compiler` via `sbt idealingua-v1-compiler/stage`
     in the current worktree; `publishLocal`s `idealingua-v1-model` and
     `idealingua-v1-runtime-rpc-scala` to `~/.ivy2/local` (skipped if already
     present at the matching version).
   - `git:<ref>`: `git worktree add --detach <perSha>/worktree <sha>`, then
     `sbt -batch -Dsbt.global.base=<perSha>/.sbt -Dsbt.ivy.home=<perSha>/.ivy2
     -Dcoursier.cache=<perSha>/.coursier -Dmaven.repo.local=<perSha>/m2
     idealingua-v1-compiler/stage <runtime-modules>/publishM2`.
     The launcher is copied to `<perSha>/launcher` and the worktree is
     removed (unless `--keep-worktrees`). The `m2/`, `.ivy2/`, `.coursier/`
     dirs survive for future runs.
2. Runs `idlc :scala --define=layout=PLAIN --disable-zip` twice (once per side)
   into `target/regression-harness/<runId>/gen-{old,new}/`.
3. Walks the IDL tree, hashes it (`idl_sha256`).
4. Looks for `<project>/.idl-regression/sample_app.scala`. If absent, renders
   the prompt template, exits **code 3** with operator instructions.
5. If sample present, builds two scala-cli projects (one per side) using
   each side's runtime repository + version (via the `{{RUNTIME_REPOSITORY}}`
   and `{{RUNTIME_VERSION}}` template substitutions). Stdout is captured,
   canonicalized to NDJSON, and diffed.

### Stage 2 — operator action

If the harness exits 3:

1. Open `target/regression-harness/<runId>/sample-app-prompt.scala.md`.
2. Feed the entire prompt to your LLM of choice. The prompt is
   self-contained — no external context lookups.
3. Save the LLM's output at `<project>/.idl-regression/sample_app.scala`
   (commit it to the target project's git history).
4. Write the metadata sidecar:

   ```bash
   echo "idl_sha256=<sha-from-sentinel>" > \
     <project>/.idl-regression/sample_app.scala.meta
   ```

5. Re-run the same `idl-regress` command.

## Per-sha cache layout

```
target/regression-harness/cache/idlc/<short-sha>/
  launcher              # copy of the staged idlc binary
  runtimeVersion.txt    # version that was publishM2-ed
  m2/                   # local maven repo (-Dmaven.repo.local)
  .ivy2/                # sbt-internal ivy cache (per plan R2)
  .coursier/            # sbt-internal coursier cache (per plan R2)
  .sbt/                 # sbt-internal global base
  worktree/             # ephemeral; removed unless --keep-worktrees
```

The cache is keyed by full 12-char sha prefix. Different tags/branches that
resolve to the same sha share the cache. The cache is safe to delete in
bulk; the next invocation will rebuild.

## Exit codes

| Code | Meaning                                                    |
|------|------------------------------------------------------------|
| 0    | no divergences                                             |
| 1    | divergences found (and `--fail-on-divergence`, the default) |
| 2    | build / setup failure                                      |
| 3    | sample-app not cached — prompt emitted, run LLM and retry  |
| 126  | CLI usage error                                            |

## Requirements

- `sbt` and `git` on PATH (`nix-shell` provides them).
- `scala-cli` on PATH (`nix-shell` provides ~1.10).
- JDK 21 (`nix-shell`).
- A clean working tree if you intend to `git worktree add` from the same repo;
  the harness will refuse to add a worktree at an already-occupied path.

## Files

| Path                                          | Purpose                                  |
|-----------------------------------------------|------------------------------------------|
| `Harness.scala`                               | entrypoint, argparse, orchestration      |
| `IdlcResolution.scala`                        | resolved per-side triplet                |
| `IdlcResolver.scala`                          | self + git-ref resolution + per-sha cache|
| `SampleAppGen.scala`                          | sample-app cache + prompt rendering      |
| `LangAdapter.scala`                           | per-language build/run interface         |
| `adapters/ScalaAdapter.scala`                 | scala-cli driven Scala impl              |
| `Canonicalize.scala`                          | NDJSON parser + canonical re-emit        |
| `Diff.scala`                                  | line-by-line diff + report               |
| `templates/scala/project.scala.template`      | scala-cli using-directives template      |
| `prompts/sample-app-scala.md`                 | LLM prompt template                      |
| `idl-regress`                                 | bash wrapper around `scala-cli run`      |
| `selftest.sh`                                 | `sanity` + `impl9-vs-head` self-tests    |
| `selftest-expectations/impl9-vs-head.scala.json` | committed baseline divergence set     |

## Roadmap → M3

1. `path:<launcher>` form in `IdlcResolver`.
2. Add `TypescriptAdapter`, `CsharpAdapter` (mirror `ScalaAdapter` structure).
3. Lift `--regen-sample-app` into an optional auto-invocation of a configured
   LLM CLI — gated by `--llm-cli claude|codex|…`. The default stays manual.
4. Read circe + runtime versions from a manifest emitted by sbt rather than
   hard-coding `CirceVersion` in `ScalaAdapter`.
5. Full main-tests-corpus sample app (auto-generated, not hand-rolled).
