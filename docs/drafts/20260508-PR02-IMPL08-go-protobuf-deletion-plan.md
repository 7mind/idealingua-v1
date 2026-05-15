# PR-02 IMPL-8 — Delete Go + Protobuf transpilers + `idealingua-v1-runtime-rpc-go`

Plan author: planning subagent (review-loop, 2026-05-08).
Source briefs: tasks.md (C2/Q2, C9), defects-m1.md (PR-02-D01..D04, D08, D11, D14), master plan §5/§10.

**Pre-locked decisions (planner-recommended):**
- Atomic single-commit deletion (C2/Q2 lock).
- Hard-removal of `--lang go`/`--lang protobuf` CLI flags (C9 lock; no deprecation).
- B2: drop `HarnessOptions.scala:53` `case other => throw` catch-all (becomes unreachable post-enum-shrink; mechanical fixup).

---

## §1 Goal & non-goals

**Goal.** Single atomic commit on `wip/necromancy` removing all Go and Protobuf code paths from idealingua-v1: the `idealingua-v1-runtime-rpc-go` sbt module, the `togolang/` and `toprotobuf/` transpiler trees, the `IDLLanguage.Go` / `IDLLanguage.Protobuf` enum cases, and every glue site that pattern-matches or imports those symbols.

**Post-deletion invariant.** All four harness contracts (`verifyGoldens`, `runWireFixtures`, `runCrossLangInterop`, `idealingua-v1-test-harness/test`) pass on Scala 2.13.18 and Scala 3.8.3.

**Non-goals.**
- Typer rewrite (PR-02 IMPL-1..7, 9..N).
- Any change to PR-03 outputs (harness, goldens, wire-fixtures, NegativeSpec, `wire-format.md`).
- Refactoring surviving Scala/TS/C# code beyond removing imports/dispatch.

---

## §2 Module deletion: `idealingua-v1-runtime-rpc-go`

### 2.1 Filesystem
Recursively delete `idealingua-v1/idealingua-v1-runtime-rpc-go/` (11 `.go` files + module structure).

### 2.2 sbtgen edits (`sbtgen/Deps.scala`)
- Remove ArtifactId at line 358.
- Remove `Projects.idealingua.runtimeRpcGo` from line 423 (transpilers test deps tuple).
- Remove the `Artifact(...)` block at lines 440-445.
- Remove from line 459 (compiler `depends` list).

### 2.3 build.sbt regeneration
Run `./sbtgen.sc --js`. Auto-removes 5 references (lines 837, 1126, 1409, 1766, 1788 at HEAD).

---

## §3 Transpiler tree deletion

### 3.1 togolang/ tree
Delete `idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/togolang/` (15 .scala files: GoLangTranslator, GoTranslatorDescriptor, GoLayouter, products/, types/, extensions/, tools/, GLTContext, GoLangTranslationTools).

### 3.2 toprotobuf/ tree
Delete `idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toprotobuf/` (21 .scala files: ProtobufTranslator, ProtobufTranslatorDescriptor, renderers, products/, types/, extensions/, layout/, tools/).

### 3.3 Descriptor registry
Edit `TypespaceCompilerBaseFacade.scala`:
- Remove import lines 5-6 (`GoTranslatorDescriptor`, `ProtobufTranslatorDescriptor`).
- Remove `GoTranslatorDescriptor,` (line 30) and `ProtobufTranslatorDescriptor,` (line 33) from `descriptors` Seq.
- Result: descriptors shrinks from 5 to 3 (Scala/Typescript/CSharp).

---

## §4 Glue-code edits

### 4.1 IDLLanguage enum (`idealingua-v1-transpilers/.../translator/IDLLanguage.scala`)
- Lines 10-12: `case object Go` block → delete.
- Lines 22-24: `case object Protobuf` block → delete.
- Lines 30-31: `case Go.toString => Go` arm → delete.
- Lines 36-37: `case Protobuf.toString => Protobuf` arm → delete.
- The `parse` body's `(s.trim.toLowerCase: @unchecked)` wrapper means surviving 3-arm match is fine; unknown args throw `MatchError` (acceptable per C9).

### 4.2 CompilerOptions.scala
- Line 5 imports: drop `GoLangBuildManifest, ProtobufBuildManifest`.
- Line 7-8: delete extension imports.
- Line 57: `type GoTranslatorOptions = ...` → delete.
- Line 60: `type ProtobufTranslatorOptions = ...` → delete.

### 4.3 Codecs.scala (`idealingua-v1-compiler/.../compiler/Codecs.scala`)
**At HEAD: 4 Go defs + 4 Protobuf defs (not 6 Go per stale defects-m1 PR-02-D01).**
- Line 5: import `ProtobufRepositoryOptions` → delete.
- Line 33: `decGo` → delete.
- Line 35: `decGoRepositoryOptions` → delete.
- Line 39: `decProtobufRepo` → delete.
- Line 41: `decProtobuf` → delete.
- Line 65: `encGo` → delete.
- Line 67: `encGoRepositoryOptions` → delete.
- Line 73: `encProtobufRepo` → delete.
- Line 75: `encProtobuf` → delete.

`decGoProjectLayout` / `encGoProjectLayout` are NOT in this file (they live in `PlatformEnumCodecs.scala` — see §4.4).

### 4.4 PlatformEnumCodecs.scala (NEW — missed by master plan §5/§10)
`idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/PlatformEnumCodecs.scala`:
- Line 11: `decGoProjectLayout` → delete.
- Line 16: `encGoProjectLayout` → delete.

### 4.5 PlatformEnumCodecsTest.scala (NEW — missed by master plan; test-source kill)
`idealingua-v1-compiler/src/test/scala/izumi/idealingua/compiler/PlatformEnumCodecsTest.scala`:
- Lines 31-34: "encode GoProjectLayout as string" block → delete.
- Lines 36-39: "decode GoProjectLayout from string" block → delete.
- Lines 61-62: `roundtrip[GoProjectLayout]` x2 → delete.

Without §4.4/§4.5 the build won't compile.

### 4.6 CredentialsReader.scala
- Line 18: `case class GoCredentials` → delete.
- Lines 20-21: `case class ProtobufCredentials` → delete.
- Line 27: `case IDLLanguage.Go => read[GoCredentials]` → delete.
- Line 29: `case IDLLanguage.Protobuf => read[ProtobufCredentials]` → delete.
- Result: 3-arm exhaustive match.

### 4.7 ManifestReader.scala
- Lines 22-23: `case m: GoLangBuildManifest` (Writer) → delete.
- Lines 37-38: `case IDLLanguage.Go` (Reader) → delete.
- Lines 41-42: `case IDLLanguage.Protobuf` (Reader) → delete.

### 4.8 ArtifactPublisher.scala
- Line 5: drop `GoLangBuildManifest, ProtobufBuildManifest` imports.
- Lines 22, 24: drop `publishGo`/`publishProtobuf` dispatch arms.
- Lines 169-280: `publishGo` body → delete.
- Lines 282-374: `publishProtobuf` body → delete.

### 4.9 IDLCArgs.scala
- Line 82: `RoleParserSchema("go", ...)` entry → delete. After deletion `--lang go` causes parser-level "unknown role" error.

### 4.10 CommandlineIDLCompiler.scala
- Line 289: `IDLLanguage.Go -> "0"` map entry → delete. (Was `:283` per defects-m1 — drift to `:289` at HEAD.)

### 4.11 Manifest deletions
- `GoLangBuildManifest.scala` → delete file.
- `ProtobufBuildManifest.scala` → delete file.

### 4.12 HarnessOptions.scala (B2 — post-enum-shrink mechanical fixup)
After §4.1, the `case other => throw …` catch-all at line ~53 becomes unreachable. Drop the case → match becomes pattern-exhaustive over Scala/TS/CSharp. Mechanical fixup, not new test coverage.

---

## §5 Pattern-match exhaustiveness audit

After §4.1 enum-value removal, every `IDLLanguage` match site:

| Site | Behavior post-deletion |
|---|---|
| `IDLLanguage.scala:27` parse | `@unchecked`, unchanged behavior; deleted-language args → MatchError |
| `CredentialsReader.scala:24` | 3-arm exhaustive — clean |
| `ManifestReader.scala:32` Reader | 3-arm exhaustive — clean |
| `ManifestReader.scala:17` Writer | `@unchecked` — clean |
| `ArtifactPublisher.scala:19` | `@unchecked` + catch-all — clean |
| `HarnessOptions.scala:53` | catch-all unreachable; drop per §4.12 |
| `IDLTestTools.scala:43` | `parse(last)` — same MatchError-on-unknown |

---

## §6 Resource files

Only Go runtime files at HEAD are under `idealingua-v1-runtime-rpc-go/.../runtime/go/irt/` (11 files), deleted via §2.1. **No** `idealingua-v1-transpilers/.../resources/runtime/go/` directory exists at HEAD (defects-m1 PR-02-D14 was speculative).

---

## §7 CLI surface (per C9 hard-removal)

- `IDLCArgs.scala:82` go role removed.
- `IDLLanguage.parse("go")` / `parse("protobuf")` throw `MatchError`.
- `defaultsByLang` Go entry removed.
- `GoCredentials` / `ProtobufCredentials` types gone.
- `init` action emits only `scala.json`, `typescript.json`, `csharp.json`.
- Users passing `--lang go` get parser-level "unknown role" error or runtime MatchError. Documented in commit message.

---

## §8 Verification protocol

1. `./sbtgen.sc --js`. Verify `git diff project/plugins.sbt` empty.
2. Cross-build: `sbt -batch '++ 2.13.18; idealingua-v1-test-harness/compile'` and `'++ 3.8.3; …'`.
3. Four harness contracts: `verifyGoldens`, `runWireFixtures`, `runCrossLangInterop`, `idealingua-v1-test-harness/test`.
4. Wider compile: `sbt -batch compile` and cross-build variants.
5. `sbt -batch idealingua-v1-compiler/test` (catches §4.5 deletions).

---

## §9 Risks

- **R1**: Scala 3 unreachable-case warning in `HarnessOptions.scala:53`. Mitigated via §4.12 (drop catch-all).
- **R2**: `IDLTestTools.scala:43` `descriptors.flatMap(_.rules)` — descriptors shrinks 5→3; harness behavior unchanged because Layer A/B/C/Negative target Scala/TS/CSharp only.
- **R3**: `PlatformEnumCodecsTest` is the only Go-mentioning test source. §4.5 + step 5 verification catches.
- **R4**: `Codecs.scala` line numbers diverge from defects-m1 PR-02-D01. Plan §4.3 uses HEAD numbers.
- **R5**: `CommandlineIDLCompiler.scala` line drift. Plan §4.10 uses HEAD `:289`.
- **R6**: defects-m1 PR-02-D14 path doesn't exist at HEAD. §6 verified.

---

## §10 Sub-task breakdown

- **T1 — Punch list (read-only, planner already done).** Skip; planner produced the file list above.
- **T2 — Filesystem deletions.** §2.1, §3.1, §3.2, §4.11.
- **T3 — Source edits.** §3.3, §4.1-4.12. Edit `sbtgen/Deps.scala` per §2.2.
- **T4 — Regenerate + verify.** Run §8 sequence.
- **T5 — Final atomic commit.** Single commit on `wip/necromancy`.
