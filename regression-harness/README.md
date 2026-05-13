# regression-harness

Wire-format regression harness for idealingua-v1. Given a 3rd-party project
whose `.domain` / `.model` files compile through `idlc`, the harness builds
two versions of `idlc`, generates from both, runs an LLM-authored sample app
under each, and diffs canonical NDJSON streams. Identical output proves
language backends are wire-stable across the two compiler versions.

This is M1 — Scala adapter only, `--old self --new self` only (HEAD-vs-HEAD
sanity), and **no automatic LLM invocation**. The operator runs an LLM
manually using a prompt the harness drops to scratch.

## Status

| Component         | M1 | M2 (planned)                            |
|-------------------|----|-----------------------------------------|
| Scala adapter     | ok | —                                       |
| Typescript adapter| —  | bun + scala-cli style template          |
| C# adapter        | —  | dotnet script template                  |
| `git:<sha>` refs  | —  | git worktree + `sbt stage`, sha-cached  |
| `path:<launcher>` | —  | pre-built launcher, skip build entirely |
| `--regen-sample-app` | partial — exits 3 with prompt | same, plus optional LLM CLI invocation |
| Self-test corpus  | manual hand-roll | full LLM-generated coverage |

## Workflow

Invoke via the `idl-regress` wrapper. Two stages, one per invocation:

### Stage 1 — first run on a fresh project

```bash
./regression-harness/idl-regress \
  --project idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests \
  --old self --new self --lang scala
```

The harness:

1. Stages `idealingua-v1-compiler` via `sbt idealingua-v1-compiler/stage`
   (cached — second invocation skips this).
2. Runs `idlc :scala -d layout=PLAIN` twice (once per side) into
   `target/regression-harness/<runId>/gen-{old,new}/`.
3. Walks the IDL tree, hashes it (`idl_sha256`).
4. Looks for `<project>/.idl-regression/sample_app.scala`. Not present.
5. Renders the prompt template at
   `target/regression-harness/<runId>/sample-app-prompt.scala.md`,
   substituting the IDL tree listing, the generated tree listing, and the
   runtime version.
6. Exits **code 3** with operator instructions printed to stderr.

### Stage 2 — operator action

1. Open `target/regression-harness/<runId>/sample-app-prompt.scala.md`.
2. Feed the entire prompt to your LLM of choice (Claude, GPT-5, etc.). The
   prompt is self-contained — no external context lookups.
3. Save the LLM's output at
   `<project>/.idl-regression/sample_app.scala` (commit it to the target
   project's git history; subsequent runs reuse it verbatim).
4. Write the metadata sidecar:

   ```bash
   echo "idl_sha256=<sha-from-sentinel>" > \
     <project>/.idl-regression/sample_app.scala.meta
   ```

5. Re-run the same `idl-regress` command. This time the sample is found,
   metadata matches, scala-cli builds and runs the driver twice, NDJSON is
   canonicalized, diffed, and a report lands at
   `target/regression-harness/<runId>/out/report.txt`.

## Exit codes

| Code | Meaning                                                    |
|------|------------------------------------------------------------|
| 0    | no divergences                                             |
| 1    | divergences found (and `--fail-on-divergence`, the default) |
| 2    | build / setup failure                                      |
| 3    | sample-app not cached — prompt emitted, run LLM and retry  |
| 126  | CLI usage error                                            |

## Requirements

- `sbt` and `nproc` on PATH (provided by the repo's `nix-shell`).
- `scala-cli` on PATH (provided by `nix-shell`; ~1.10 is the verified version).
- JDK 21 (provided by `nix-shell`).

## Files

| Path                                     | Purpose                                  |
|------------------------------------------|------------------------------------------|
| `Harness.scala`                          | entrypoint, argparse, orchestration      |
| `IdlcResolver.scala`                     | resolves `--old`/`--new` to a launcher   |
| `SampleAppGen.scala`                     | sample-app cache + prompt rendering      |
| `LangAdapter.scala`                      | per-language build/run interface         |
| `adapters/ScalaAdapter.scala`            | scala-cli driven Scala impl              |
| `Canonicalize.scala`                     | NDJSON parser + canonical re-emit        |
| `Diff.scala`                             | line-by-line diff + report               |
| `templates/scala/project.scala.template` | scala-cli using-directives template      |
| `prompts/sample-app-scala.md`            | LLM prompt template                      |
| `idl-regress`                            | bash wrapper around `scala-cli run`      |
| `selftest.sh`                            | M1 sanity self-test                      |

## Roadmap → M2

1. Implement `git:<sha>` / `path:<launcher>` in `IdlcResolver`.
2. Add `TypescriptAdapter`, `CsharpAdapter` (mirror `ScalaAdapter` structure).
3. Lift `--regen-sample-app` into an optional auto-invocation of a configured
   LLM CLI — gated by `--llm-cli claude|codex|…`. The default stays manual.
4. Read circe + runtime versions from a manifest emitted by sbt rather than
   hard-coding `CirceVersion` in `ScalaAdapter`.
5. Full main-tests-corpus sample app (auto-generated, not hand-rolled).
