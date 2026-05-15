# PR-03 — Wire-format back-compat test harness plan

Status: plan only. No implementation in this PR.

This document specifies the regression harness that protects the wire format
during the typer modernization (PR-02). Cross-references:
- Meta-plan: `docs/drafts/20260503-1200-modernization-plan.md`, especially
  §"Wire format — current assertions and risks".
- PR-01: `docs/drafts/20260503-PR01-baboon-typer-lessons.md`, especially
  Phases 8–9 ("shallowId" / "computeDeepSchema") which become this harness's
  fingerprint primitive.

## §1. Goal and non-goals

**Goal.** Prove byte-for-byte stability of the JSON wire format produced by
the Scala/TypeScript/C# generated code as the legacy `IDLPostTyper` plus
`TypespaceImpl` are replaced by the new multi-phase typer (PR-02). Production
RPC clients depend on the exact wire shape today: the envelope strings in
`idealingua-v1/idealingua-v1-runtime-rpc-scala/src/main/scala/izumi/idealingua/runtime/rpc/packets.scala:38–66`
(`rpc:request`, `rpc:response`, `rpc:failure`, `buzzer:request`, `buzzer:response`,
`buzzer:failure`, `stream:s2c`, `stream:c2s`, `?:failure`), the field order of
`RpcPacket` at `packets.scala:86–94`, and the ADT/interface discriminator
formula `s"${path.toPackage.mkString(".")}.$name"` at
`idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/common/TypeId.scala:31–33`.
The new typer must reproduce this output exactly, on every supported back-end,
on every input domain in `idealingua-v1-test-defs`.

**Non-goals.**
- This document is *not* the wire-format spec. §4 specifies the skeleton of
  that spec, which lands in a follow-up PR as `docs/wire-format.md`.
- This document does *not* re-cover the functional integration test surface
  exercised by `idealingua-v1/idealingua-v1-runtime-rpc-http4s/src/test/scala/izumi/idealingua/runtime/rpc/http4s/Http4sTransportTest.scala`
  — that suite verifies an HTTP transport for a single hand-written service
  through Scala generation, not the wire shape across languages.
- We do not propose new IDL syntax, new builtin scalars, or new packet kinds.
- We do not propose changes to runtime modules other than (i) test
  infrastructure additions and (ii) the codec drivers in §3 Layer C.
- We do not aim to fix any pre-existing cross-language drift surfaced by the
  baseline step in §5; the baseline freezes today's behaviour, however ugly.

## §2. Threat model — what can break the wire format

Each entry names a concrete failure mode, the file:line in current code that
authors the affected bytes, and what an undetected regression would look like
to a consumer.

1. **`RpcPacket` field reordering or `RPCPacketKind` literal-string rename.**
   `packets.scala:86–94` declares `RpcPacket(kind, data, id, ref, service,
   method, headers)`; Circe's `deriveEncoder` (via `packets.scala:111`) emits
   keys in declaration order. `packets.scala:37–71` defines the literal
   strings `?:failure`, `rpc:request`, `rpc:response`, `rpc:failure`,
   `buzzer:request`, `buzzer:response`, `buzzer:failure`, `stream:s2c`,
   `stream:c2s` via `toString` overrides — these are the wire values of
   `RPCPacketKind`. Any reorder, rename, or addition without coordinated
   client rollout breaks every RPC framing layer simultaneously. An undetected
   break is a hard "unknown packet kind" exception or a silent decode of
   `kind` from the wrong position, depending on whether the consumer is
   strict-Circe or lenient.

2. **`wireId` formula change.** `TypeId.scala:31–33`:
   `def wireId: String = s"${path.toPackage.mkString(".")}.$name"`. This
   string is the discriminator key for ADT branches (Scala:
   `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/extensions/CirceTranslatorExtensionBase.scala:51`,
   `:57`, `:62–63`), interface implementations (same file, `:114`, `:120`,
   `:125–126`), TypeScript (`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/totypescript/TypeScriptTranslator.scala:85`,
   `:430`), and C# (`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/tocsharp/extensions/JsonNetExtension.scala:445`,
   `:461`). Any change — for example, dropping the empty-package prefix,
   percent-encoding dots, or moving a type into a different package — silently
   produces JSON that decodes-as-default-branch (or fails) on every consumer.

3. **Struct field declaration-order change.** Scala back-end uses Circe
   `deriveEncoder`/`deriveDecoder` for plain DTOs and Identifiers
   (`CirceTranslatorExtensionBase.scala:281–283`); Circe emits keys in Scala
   *field declaration order*. The new IR must preserve the source-order of
   IDL struct fields end-to-end, including through any flattening pass that
   merges parent and child fields. A reordering change is invisible to
   Circe's *decoder* (it accepts any order) but visible to byte-equality and
   to any consumer that reads as a key sequence. PR-01 §"Phase 9 verdict"
   already calls this out: the deep-schema fingerprint must capture field
   order to be a useful regression signal.

4. **`idNameFix` synthetic-field-name rule change.**
   `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/il/ast/IDLTyper.scala:199–214`
   is the canonical rule: if an Identifier field has a name, use it; if there
   is exactly one unnamed field, use the literal `"value"`; otherwise use
   the referenced type's name lowercased via `IzString.uncapitalize` (with a
   leading `#` stripped). Generated DTOs encode JSON with these names. Any
   change re-keys every Identifier in the corpus. An undetected regression
   surfaces as a missing field on decode (the consumer sees `null`) or a
   silent extra key on encode.

5. **Ephemeral-DTO naming change.**
   `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/TypespaceToolsImpl.scala:9–16`
   defines `methodOutputSuffix = "Output"`, `methodInputSuffix = "Input"`,
   `goodAltSuffix = "Success"`, `badAltSuffix = "Failure"`,
   `goodAltBranchName = "Success"`, `badAltBranchName = "Failure"`. The
   ephemeral DTO names appear in `wireId` (`TypeId.scala:31`) and therefore in
   ADT/interface discriminators on the wire. Renaming `Output` to `Result`,
   `Success` to `Ok`, etc., breaks every client that decodes service
   responses.

6. **ADT/interface JSON envelope shape change.** The JSON shape today is
   `{ "<wireId>": { …branchFields } }` for ADTs
   (`CirceTranslatorExtensionBase.scala:51`) and
   `{ "<wireId>": <implementing-DTO-fields> }` for interfaces (same file,
   `:114`). C# emits the same shape with `WriteStartObject`/
   `WritePropertyName(wireId)` (`JsonNetExtension.scala:442–453`). TS uses a
   `{[className]: adt.serialize()}` shape with `case '<wireId>': return …`
   on decode (`TypeScriptTranslator.scala:407`, `:430`). Switching to a
   `{ "@type": "<wireId>", "value": {…} }` discriminator (more conventional
   in some Jackson configurations) breaks every consumer. Switching from
   nested object to top-level `{"_kind": "...", ...flatFields}` likewise.

7. **Singular-output "unwrap" mode change.**
   `CirceTranslatorExtensionBase.scala:191–202` chooses between unwrapped and
   wrapped emission for method outputs: when the method's output is
   `DefMethod.Output.Singular`, the emit path enters the `unwrap` branch
   (`:205–255`) and encodes the single field directly (`v.<fieldName>.asJson`
   or `.asJsonObject`) instead of the usual `{ <fieldName>: <value> }`
   wrapper. Removing this mode wraps every previously-unwrapped output in an
   extra object level. The TypeScript and C# back-ends must agree on which
   methods are Singular and apply unwrap consistently — a discrepancy here
   is a cross-language drift that no single-language test would catch.

8. **Cross-language drift.** Scala (Circe), TS (hand-rolled string templates
   in `TypeScriptTranslator.scala`), and C# (JsonNet via
   `JsonNetExtension.scala`) each implement codecs independently; nothing in
   the build cross-checks them. A change in the Scala back-end that misses
   a parallel change to TS/C# yields one-way encoding compatibility (Scala
   producer → C# consumer fails or silently produces wrong fields) that no
   single-back-end test catches. The meta-plan §"Risks 2" already names this
   risk; this harness's Layer C is the only mitigation.

9. **Interface→ephemeral-DTO mirror change.**
   `TypespaceToolsImpl.scala:20–22` derives `implId(Interface) =
   DTOId(parent = interface, name = "Struct")`. The ephemeral DTO's
   `wireId` is what appears in interface-typed JSON
   (`CirceTranslatorExtensionBase.scala:114`). Restructuring the
   interface→DTO mirror (e.g. dropping the `"Struct"` literal in
   `TypespaceToolsImpl.scala:71–72`) renames every interface payload key.

10. **Builtin-scalar JSON mapping change.**
    `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/common/TypeId.scala:106–238`
    enumerates the builtins (`TBool`, `TString`, `TInt8/16/32/64`,
    `TUInt8/16/32/64`, `TFloat`, `TDouble`, `TUUID`, `TBLOB`, `TTs`, `TTsTz`,
    `TTsU`, `TTime`, `TDate`). The mapping from each builtin to its JSON
    representation (e.g. `TUUID` as canonical 8-4-4-4-12 string,
    `TTsTz` as ISO-8601 with offset, `TBLOB` as base64) is implicit in each
    back-end's serializer choice. A library upgrade that flips RFC 3339
    "Z" to "+00:00" or changes UUID hyphenation breaks parity even though
    no idealingua-v1 source line moved.

11. **Generic `TList`/`TSet`/`TOption`/`TMap` encoding change.**
    `TypeId.scala:251–289` defines the four generics. Today: `TList`/`TSet`
    encode as JSON array; `TOption[T]` is either present-as-`T` or absent
    (Circe `Option` encoder); `TMap[K, V]` encodes as JSON object only when
    `K` is a string-shaped scalar (Circe extension treats `TMap` specially
    in the `unwrap` `isObjectEncoder` decision at
    `CirceTranslatorExtensionBase.scala:212`). An IR refactor that wraps
    `TOption` in `{ "value": T }` or treats `TSet` as `{ "items": [...] }`
    breaks compatibility silently: the consumer's `Option[T]` decoder still
    parses `null`/missing successfully but never sees the new wrapped shape.

12. **Service/method `IRTMethodId` framing.** `RpcPacket.rpcRequest`
    (`packets.scala:121–123`) writes
    `service = method.service.value`, `method = method.methodId.value`. The
    method-id values are the IDL-author-specified service/method names
    untransformed. A new typer that capitalizes, snake-cases, or otherwise
    canonicalizes method names breaks every routing table downstream.

13. **Struct field declaration-order across inheritance flattening.** Today
    flattening is performed on demand by
    `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/StructuralQueriesImpl.scala`
    (called from `CirceTranslatorExtensionBase.scala:187`:
    `ctx.typespace.structure.structure(id)`). The new IR materializes
    flattened structs at typing time. Whatever ordering rule that pass
    chooses (parent-first vs. child-first, alphabetical, declaration-order
    with inheritance interpolation) becomes wire-visible. The harness must
    catch any drift from today's `structure(id)` order.

## §3. Three-layer harness design

Three independent layers, each catching a different failure class. A single
layer is not enough: source goldens are too brittle for whitespace, byte
fixtures cannot detect cross-language drift, and cross-language tests cannot
diff individual generated lines for review.

### Layer A — Generated-source goldens (per-language snapshot tests)

**Where the goldens live.** Proposed:
`idealingua-v1/idealingua-v1-test-defs/src/main/resources/golden/<lang>/<domain>/<file>.<ext>`,
where `<lang>` ∈ {`scala`, `typescript`, `csharp`} and the directory tree
mirrors the back-end's natural per-language layout (Scala packages, TS
modules, C# namespaces). The goldens are checked into the repo as ordinary
files, not embedded resources.

**How they're regenerated.** A new SBT task `regenerateGoldens` invokes
`TypespaceCompilerBaseFacade.compile`
(`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TypespaceCompilerBaseFacade.scala`)
once per (language × domain) pair against every domain in
`idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/idltest/`
and overwrites the golden tree. The CI test runs the same compiler in
read-only mode and asserts `git diff --exit-code` (or the equivalent
in-test file-tree comparison if we don't want CI shelling out to git). Any
non-empty diff fails the test.

**Diff policy.** A non-empty diff is *always* a generated-code change.
Every PR that produces a Layer A diff must:
1. List the diff in the PR description.
2. Tag the diff as either a wire-affecting change (requires §4 spec doc
   update + cross-team announcement) or a non-wire change (whitespace,
   comments, generated `import` reordering, identifier-of-symbol-rename).
3. Get reviewer sign-off acknowledging the classification, *even if the
   classification is "comment-only"*. Comment-only changes still flag as
   typer changes that the reviewer should examine — better to over-review
   than to miss a wire change disguised as a comment-only one.
4. If wire-affecting: update §4's spec doc and bump a wire-format version
   marker (see §11 open question on fingerprinting).

**Pre-modernization baseline step.** Before any PR-02 typer change lands,
run `regenerateGoldens` against the *current* legacy compiler at the
freeze-checkpoint commit (§5), and check the goldens into the repo. PR-02's
incremental commits then cannot ship without `git diff --exit-code` clean —
i.e., the new typer must reproduce the legacy goldens with character-for-character
source equality unless the diff has been classified per the policy above.
("byte-for-byte" is reserved in this document for the wire-format invariant in
Layer B; Layer A is a *source-text* equality check on generated code.) Without this baseline,
PR-02 has nothing to compare against, and "no regression" becomes
unprovable.

**Acceptance criteria for Layer A.**
- Every domain under
  `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/idltest/`
  produces a complete golden tree for every surviving language (Scala, TS,
  C#).
- `sbt regenerateGoldens` is idempotent on a clean tree (running it twice
  in a row produces no diff).
- CI fails on any diff. Failure output names the (lang, domain, file)
  triple of the first divergent file.
- Goldens are reviewed as part of the baseline PR: each language lead
  signs off that today's output is what their consumers actually
  receive.

**Open questions for Layer A.**
- Do we golden the entire generated tree (build files, manifests, runtime
  glue) or only the per-type sources? Recommendation: only per-type
  sources, plus the per-domain manifest, to keep the diff signal-to-noise
  high.
- Do we strip `// Generated at <timestamp>` comments before comparing?
  Recommendation: yes, normalize timestamps and version literals to a
  fixed token before checking in.

### Layer B — Wire-byte fixtures (per-language runtime decode/re-encode tests)

**Why generated-source goldens are not enough.** Layer A is a *source*-level
diff; it is simultaneously too narrow (a whitespace tweak in the Scala
back-end's quasiquote layout flips the diff red without changing a single
emitted JSON byte) and too broad (a wire-affecting change that happens to
preserve the surface form of the generated codec — e.g. swapping
`deriveEncoder` for an explicit `Encoder.forProductN` with the same field
order — would slip through). Wire-byte fixtures verify the *runtime*
serialization behaviour independent of source layout.

**Format.** One JSON file per (domain, type, scenario) tuple, at
`idealingua-v1/idealingua-v1-test-defs/src/main/resources/wire-fixtures/<domain>/<type>/<scenario>.json`.
Each fixture is a single representative payload. Multiple scenarios per
type are supported (e.g. `identifiers/UserId/minimal.json`,
`identifiers/UserId/with-special-chars.json`). The path encodes everything
the test loader needs.

**The runtime test.** Each language's runtime test suite depends on the
generated output of `idealingua-v1-test-defs` and runs:
1. Load fixture from disk.
2. Decode JSON into the generated type using the back-end's runtime
   deserializer (Circe in Scala, the hand-rolled `serialize`/`deserialize`
   in TS, JsonNet in C#).
3. Re-encode the decoded value.
4. Byte-compare against the fixture.

Per-language entry points:
- Scala: a new test in `idealingua-v1-test-harness` (see §9), depending
  on `idealingua-v1-test-defs % "compile->compile"` so the generated
  Scala types are on the classpath.
- TS: a Node test runner driven from SBT (see "Per-language build
  pipeline" below).
- C#: a `dotnet test` invocation driven from SBT (see "Per-language
  build pipeline" below).

**Per-language build pipeline.** Generated TS from idealingua-v1 is
not a published npm package, and generated C# is not a published
NuGet package; both must be made runnable inside the harness without
relying on a registry. The harness pipeline is:

- *Scala*: the generated `.scala` files compile as part of the
  `idealingua-v1-test-defs` SBT project (today's setup); the
  harness's Scala test code depends on it via the standard SBT
  classpath. No extra step.
- *TypeScript*: the harness emits a `tsconfig.json` and a
  hand-written test driver into a per-test working directory next
  to the generated `.ts` files. SBT shells out to `tsc --noEmit`
  (type-check the generated TS plus the driver against each other),
  then runs the driver via `node` (or `tsx`/`ts-node` if we want
  to skip a separate emit step). Runtime dependencies of generated
  TS — i.e. the TS runtime fragments the back-end emits alongside
  the per-type files — are vendored into the same working directory;
  no `npm install` against a registry occurs in the test path.
- *C#*: the harness writes a small `.csproj` that `<Compile Include=
  "...generated...*.cs" />`s the generated sources alongside the
  hand-written test driver, then SBT shells out to `dotnet build`
  followed by `dotnet test` (or `dotnet run` for the codec driver
  in Layer C). The runtime dependency is JsonNet (`Newtonsoft.Json`),
  pinned to a specific version in the `.csproj` and restored from
  the local NuGet cache.

The Node and dotnet binaries are provided through `flake.nix` (§8).
No "npm install in a temp dir" step: the harness controls the build
inputs end-to-end.

**Comparison rule.** The default for Layer B is **byte-strict raw
comparison for ALL payloads** — both envelope and inner. The harness
loads the fixture, decodes it, re-encodes it through the back-end's
runtime, and asserts byte-equality against the original fixture.

Canonicalization is run as a *parallel sanity check*, not the primary
assertion: the harness also computes the canonical form of (a) the
fixture, (b) the round-tripped Scala output, (c) the round-tripped
TS output, and (d) the round-tripped C# output, and asserts that all
four canonical forms agree. This catches cross-language drift even in
the cases where raw bytes legitimately differ between languages (e.g.
because one language's runtime emits `1.0` and another emits `1`).

The canonicalization pass — recursive key sort, whitespace
normalization, drop trailing zeros for floats, write integers without
exponent notation, normalize string escapes — was the *primary* check
in an earlier draft of this plan, and has been **demoted** to a parallel
sanity check. Reason: a canonicalization-as-primary policy silently
accepts field-order changes inside payloads, which the user explicitly
called out as wire-affecting. Raw byte equality keeps Layer B honest
about field order.

The harness ships a single canonicalizer used by all three language
test runners (one Scala implementation; TS and C# call into it via
the codec drivers in Layer C, OR each language ships its own
implementation that the baseline step verifies emits the same canonical
form). The canonicalization helper lives in the harness module (§9),
not inside the runtime modules — it must not be a dependency of
production code. Invariant: applying canonicalize twice equals
applying it once.

Trade-off: byte-strict raw comparison requires that any legitimate,
non-wire-affecting per-language difference (e.g. `1.0` vs `1`, key
ordering of a `Map` whose key set is logically equivalent across
languages) be normalized at *fixture authoring time* — i.e., the
fixture's bytes must already be in the form each language emits, or
the test fails. This pushes work onto fixture authors; the parallel
canonicalized sanity check is what catches the cases where the bytes
diverge for reasons that are not actually wire-affecting, so authors
get an actionable signal ("your raw fixture is wrong; canonical
forms agree") instead of a silent miss. If a language genuinely
needs an asymmetric raw fixture, the fixture file may carry a
sibling `<scenario>.<lang>.json` override — opt-in, not the default.

**How fixtures are authored.**
1. The harness includes a `scaffold` mode: given a typed value (built in
   Scala directly against the generated types), emit a default fixture
   to disk. The author then trims, renames, and edits to make the
   fixture maximally informative (including edge cases, e.g.
   max-int64, empty strings, deeply nested optionals).
2. Hand-authored fixtures are reviewed in the same PR that introduces
   them. Diff hygiene: one fixture per file, files ordered by
   convention (domain/type/scenario), no trailing newline normalization
   in CI (use a `.gitattributes` rule to keep diff noise out).

**Acceptance criteria for Layer B.**
- Every test domain in `idealingua-v1-test-defs` has at least one
  fixture covering each of: a struct (DTO), an ADT (where the domain
  has one), an interface (where present), an enum (where present), an
  identifier (where present), a map-typed field (where present), a
  list-typed field (where present), an optional-typed field (where
  present).
- Every fixture round-trips in all three languages.
- A failure surfaces with `(domain, type, scenario, language)` in the
  test name.

**Open questions for Layer B.**
- Do we include `null` vs missing-key fixtures for `TOption`?
  Recommendation: yes, both, in separate scenarios.
- For `TBLOB`, do we fixture small (10-byte) and large (1 MiB) blobs?
  Recommendation: small only — large blobs blow the diff size and don't
  exercise codec logic differently from small ones.

### Layer C — Cross-language interop tests

**Why.** A struct that round-trips inside Scala but breaks when emitted
by TS and decoded by C# is the *exact* failure mode the user fears.
Cross-encode tests catch drift between back-ends independent of any
single-language Layer A or B test.

**Mechanism.** A small CLI test driver per language, exposing:
- `encode <typeId> <jsonInput>` — read a JSON value typed against the
  *language-native* shape (e.g. a Scala case class instance constructed
  from the input), encode through the back-end's runtime, write JSON to
  stdout.
- `decode <typeId> <jsonInput>` — reverse.

The drivers live alongside the language runtimes:
- `scala-codec-driver`: a tiny `App` in the harness module, depending on
  `idealingua-v1-runtime-rpc-scala` and the generated Scala for the test
  corpus.
- `ts-codec-driver`: a Node script bundled with the generated TS.
- `csharp-codec-driver`: a `dotnet` console app bundled with the
  generated C#.

The harness orchestrates them via subprocess from a Scala test runner.
Drivers communicate over stdin/stdout in a fixed line-oriented protocol
(one request per line, one response per line) — *not* via temp files,
to keep test latency low.

**Generated dispatch tables.** TypeScript erases types at runtime,
and C#'s `JsonConvert.DeserializeObject<T>` requires a compile-time
`T`; neither current back-end emits a runtime registry that maps
`wireId` to a typed encode/decode call. The harness therefore
generates, as part of the harness build, a per-language *dispatch
table* — one entry per type in the test corpus — that maps the
type's `wireId` (the same string formula as `TypeId.scala:31–33`)
to a concrete typed `encode`/`decode` invocation against the
back-end's runtime. The dispatch table is produced by a small
codegen step in PR-03.4 that walks the test corpus, asks the
typespace for each declared type, and emits the typed call sites
into a `Dispatch.ts` / `Dispatch.cs` file living next to the driver
source. The Scala driver does not need a generated dispatch table:
the typespace is already a Scala value and reflection-by-class-name
suffices. Generating the dispatch table — rather than parsing types
out of the JSON — keeps the driver out of the dynamic-typing
business and makes the cross-language test a true codec test, not
a reflection test.

**Test matrix.** For each (A, B) ∈ {Scala, TS, C#}² with A ≠ B:
1. Driver A: encode fixture F to bytes, write to stdout.
2. Driver B: read those bytes from stdin, decode, re-encode to bytes.
3. Compare canonicalized output to F's canonicalized form.

The matrix is dense: 6 cross-language directions plus 3 same-language
sanity checks (which already exist as Layer B). Coverage minimum at
least one fixture in each of these categories: a struct, an ADT, an
interface, an identifier, an enum, a map, a list, an optional, a
service-method-input wrapper, a service-method-output wrapper
(unwrap-mode).

**CI runtime requirements.**
- Existing: nix-managed JVM (already in `flake.nix`).
- Add to `flake.nix`:
  - `nodejs` (Node 22 LTS or current — match what TS consumers use in
    production).
  - `dotnet-sdk` (8.0 LTS or current).
- The SBT test task spawns the per-language driver via the path
  resolved through `nix shell`-style env, not assuming user-installed
  Node/dotnet.

**Acceptance criteria for Layer C.**
- Every (A, B) direction passes for every cross-language fixture.
- A failure surfaces as `(srcLang, dstLang, fixture, byteIndexOfFirstDiff)`
  so the diagnostician immediately sees which back-end's output differs.
- Driver overhead is under 50 ms per invocation (warm JVM not reachable;
  but TS and C# driver process startup is the binding constraint —
  consider keeping drivers alive between fixtures to amortize startup).

**Open questions for Layer C.**
- Daemon mode for drivers (one process per language, multiple requests)
  vs one process per fixture? Recommendation: daemon, with a kill switch
  in the harness for hung drivers.
- Do we test the *envelope* layer (`RpcPacket` framing) in addition to
  payload codecs? Recommendation: yes — a separate envelope-only test
  matrix that wraps each fixture in an `RpcPacket` and verifies the
  envelope round-trips. This catches packet-kind literal-string drift
  (threat model item 1).

## §4. Wire-format spec doc skeleton

The full spec lands in `docs/wire-format.md` in a follow-up PR.
Skeleton, with each section pointing at the file:line authoring the rule
today:

1. **Envelope.** `RpcPacket` field set, declaration order, optionality.
   Source of truth: `packets.scala:86–94`. Each `RPCPacketKind` value
   and its literal-string `toString` form: `packets.scala:37–71`.
   Encoder: `packets.scala:111` (`deriveEncoder`).

2. **`wireId` formula.**
   `s"${path.toPackage.mkString(".")}.$name"`. Source of truth:
   `TypeId.scala:31–33`. Used as discriminant in §4.4.

3. **Struct (DTO/Identifier) encoding.** Field name source: IDL field
   name, with the synthetic-name rule in §4.7 for unnamed Identifier
   fields. Field order: declaration order in the IDL file, transitively
   flattened through inheritance per `StructuralQueriesImpl.structure`
   (called from `CirceTranslatorExtensionBase.scala:187`). Authoring
   files: Scala — `CirceTranslatorExtensionBase.scala:281–283`
   (`deriveEncoder`/`deriveDecoder` for the wrapped form), `:225–241`
   (unwrap form); C# — `JsonNetExtension.scala:104–119`; TS —
   per-DTO `serialize()` template at
   `TypeScriptTranslator.scala:205–209` (inside `renderDto`), with
   the surrounding constructor/deserialize block at `:190–201`
   and the field emit helpers `renderSerializedObject` /
   `renderDeserializeObject` at `:515–523`.

4. **ADT / interface encoding.** Shape:
   `{ "<wireId>": { …branchOrImplFields } }`. Singleton vs multi-branch:
   identical shape; the encoder dispatches on runtime class. Sources:
   - Scala ADT encode/decode:
     `CirceTranslatorExtensionBase.scala:49–58`.
   - Scala interface encode/decode:
     `CirceTranslatorExtensionBase.scala:112–121`.
   - C# ADT: `JsonNetExtension.scala:441–470`.
   - TS ADT: `TypeScriptTranslator.scala:404–434`.

5. **Enum encoding.** Plain JSON string, value equal to the enum
   member's IDL name. Scala: `CirceTranslatorExtensionBase.scala:99–104`
   (uses `withParseable`, which produces an `Encoder.encodeString` chain
   at `:173`). C#: `JsonNetExtension.scala:51–69` (`writer.WriteValue
   (value.ToString())`). TS: enum as TypeScript `enum`; the
   per-member format string `s"$m = '$m'"` is at
   `TypeScriptTranslator.scala:443`, inside `renderEnumeration`
   (the `export enum ${i.id.name} {` declaration line is at `:447`).

6. **Identifier encoding.** Identifiers serialize as a string with a
   formatted body that combines the field values; the field name source
   is the `idNameFix` rule in §4.7. Single-field Identifier with
   unnamed field uses the literal `"value"` for the field name.
   Authoring file: `IDLTyper.scala:199–214` (`idNameFix`). Scala
   Identifier codec is the `Encoder.encodeString.contramap(_.toString)`
   at `CirceTranslatorExtensionBase.scala:173`. C#:
   `JsonNetExtension.scala:29–37`. TS: per-Identifier
   `serialize()`/`toString()` template at
   `TypeScriptTranslator.scala:500–508` (inside `renderIdentifier`),
   with the string-form parser at `:484–491`.

7. **Ephemeral DTO naming (method I/O, branch DTOs).** Suffixes from
   `TypespaceToolsImpl.scala:9–16`:
   - `methodInputSuffix = "Input"`
   - `methodOutputSuffix = "Output"`
   - `goodAltSuffix = "Success"`
   - `badAltSuffix = "Failure"`
   - `goodAltBranchName = "Success"`
   - `badAltBranchName = "Failure"`
   And the interface→DTO mirror: `implId(I) = DTOId(I, "Struct")`
   (`TypespaceToolsImpl.scala:20–22`, `:71–72`).

8. **Singular "unwrap" mode for method outputs.** A method whose
   output is `DefMethod.Output.Singular` encodes the single field's
   value directly as the body, without the wrapper object.
   Authoring: `CirceTranslatorExtensionBase.scala:191–202` (the
   `unwrap` decision), `:205–255` (the unwrap branch). C# and TS
   must agree on this dispatch.

9. **Builtin scalars.** From `TypeId.scala:106–238`:
   - `TBool` → JSON boolean.
   - `TString` → JSON string.
   - `TInt8`/`TInt16`/`TInt32`/`TInt64` → JSON integer; range
     constraints honoured by codec.
   - `TUInt8`/`TUInt16`/`TUInt32`/`TUInt64` → JSON integer; the
     encoder MUST emit unsigned-correct values. (Open question for
     spec: do we send `TUInt64` near `2^63` as JSON number — risking
     loss in JS — or as JSON string? Today's TS back-end behaviour
     must be documented in the spec, not changed.)
   - `TFloat`, `TDouble` → JSON number; document NaN/Infinity policy
     (Circe emits `null` by default; verify TS/C#).
   - `TUUID` → JSON string in canonical 8-4-4-4-12 form.
   - `TBLOB` → JSON string, base64-encoded (verify each back-end).
     **Note:** C# does not currently emit `TBLOB` at all —
     `JsonNetExtension.scala:188` is `case Primitive.TBLOB => ???`,
     a Scala `???` placeholder that throws `NotImplementedError`
     when the transpiler encounters a `TBLOB` field. There is no
     C# baseline to record. See §11 Q12.
   - `TTs`, `TTsTz`, `TTsU`, `TTime`, `TDate` — JSON string in
     ISO-8601 form; precise format per back-end runtime is what the
     spec must capture. The Scala runtime helper is
     `idealingua-v1/idealingua-v1-runtime-rpc-scala/.../runtime/circe/IRTTimeInstances.scala`
     (cited indirectly via `CirceTranslatorExtensionBase.scala:25`).

10. **Optional / list / set / map encoding.** From `TypeId.scala:251–289`:
    - `TOption[T]` — `null` or absent for None; the value of `T` for
      Some. (Document the Circe `Encoder[Option]` default, which omits
      the key on None.)
    - `TList[T]`, `TSet[T]` — JSON array; preserve order for `TList`,
      indeterminate-but-stable order for `TSet` (today's Scala runtime
      iterates `Set` in insertion order for `LinkedHashSet`-typed
      generated fields; verify and document).
    - `TMap[K, V]` — JSON object when `K` is string-shaped; otherwise
      the back-end has language-specific behaviour (Circe rejects
      non-string keys at compile time, see
      `CirceTranslatorExtensionBase.scala:212`'s
      `isObjectEncoder` test for the unwrap path's TMap special-case).
      The spec must document precisely which `K` types are allowed.

For each section, name the file:line authoring the rule. PR-03's harness
trips when this code changes; the spec doc forces every change to be
intentional.

## §5. Pre-modernization baseline workflow

This is the single most important step. Without a baseline, PR-02's
"no regression" claim is unverifiable.

**Order of operations.**

1. Land this plan (PR-03 docs) on `develop`.
2. Resolve the open questions in §11 (specifically: module layout, strict
   vs canonical, fingerprint-now vs defer).
3. **Land a single one-shot baseline PR** that:
   - Adds the harness module skeleton (Layer A only initially).
   - Runs `regenerateGoldens` against the *current* legacy compiler.
   - Checks in the baseline goldens.
   - Adds the CI step that diffs goldens.
4. Cut and push the freeze tag `wire-format-baseline-2026-05-03`
   on the commit produced by step 3. The tag is the immutable
   reference point future drift diffs against; the date in the
   tag string is fixed and does not move.
5. **Add Layer B fixtures** in a follow-up PR, again against the legacy
   compiler. Run the round-trip tests, verify all three languages pass
   on the baseline.
6. **Add Layer C** in a third follow-up PR. Verify cross-language
   parity on the baseline. Any pre-existing drift surfaced here is
   filed as a known-issue ticket, NOT fixed in this baseline (per §1
   non-goals); the harness records today's behaviour as the
   point-of-comparison.
7. PR-02 begins.

**Exact SBT commands.** PR-03.1 must add SBT tasks named exactly
as below. These names are part of the harness contract; later PRs
that need to rename them must do so through a deprecation cycle and
update every cross-reference in this document. The task surface:

- `sbt regenerateGoldens` — produces the Layer A goldens for the
  whole corpus (writes into
  `idealingua-v1-test-defs/src/main/resources/golden/`).
- `sbt verifyGoldens` — read-only check; same compilation pass as
  `regenerateGoldens`, but compares against the checked-in goldens
  and fails on diff. This is what CI runs.
- `sbt runWireFixtures` — runs Layer B (`WireFixtureSpec`) for all
  three languages. Aliases: `sbt "testOnly
  izumi.idealingua.harness.WireFixtureSpec"`.
- `sbt runCrossLangInterop` — runs Layer C (`CrossLangSpec`).
  Aliases: `sbt "testOnly izumi.idealingua.harness.CrossLangSpec"`.

These four task names — `regenerateGoldens`, `verifyGoldens`,
`runWireFixtures`, `runCrossLangInterop` — are the contract.
PR-03.1's success criterion includes: each task is invocable from
the project root and prints a non-empty result.

**Verifying the baseline is complete.**
- Layer A: every domain in
  `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/idltest/`
  has goldens for `scala`, `typescript`, `csharp`. Use a coverage
  assertion at the start of `GoldenSpec` that scans the input
  domain list and fails if any are missing.
- Layer B: every (struct, ADT, interface, enum, identifier, map,
  list, optional) instance in the corpus has at least one fixture.
  Coverage report emitted at end of `WireFixtureSpec`.
- Layer C: every fixture from Layer B has been exercised in every
  (A, B) direction.

**Freeze checkpoint.**
The git tag `wire-format-baseline-2026-05-03` is the immutable
reference. Any future drift PR-02 introduces against that tag MUST
be either (i) classified as not-wire-affecting and signed off, or (ii)
classified as wire-affecting, with a spec-doc update and a downstream
consumer-rollout plan attached.

## §6. Test-corpus coverage audit

The 22 `.domain` files in
`idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/idltest/`
(`algebraics`, `aliases`, `aliases2`, `anyvals`, `ast`, `buzzers`,
`clones`, `consts`, `datainheritance`, `datainheritancetransitive`,
`diamonds`, `dtofields`, `enums`, `identifiers`, `inheritance`,
`jsonlike`, `phase`, `services`, `streams`, `substraction`, `syntax`,
`upcasts`) plus `izumi/` cross-package fixtures and
`overlaytest/` provide rough coverage. This audit names the patterns the
harness must exercise and recommends fixture additions, prefering to
attach to existing domains rather than introducing new `.domain`
files.

The list is organized by pattern. Each entry: pattern name, why it's
load-bearing for the wire format, the existing domain that probably
covers it (verify during fixture authoring), whether a fixture is
needed.

| # | Pattern | Why load-bearing | Likely existing domain | Fixture needed? |
|---|---------|------------------|------------------------|-----------------|
| 1 | Plain DTO with mixed scalar fields | Field declaration order via `deriveEncoder`; baseline shape | `dtofields` | Yes (one per scalar combination of interest) |
| 2 | Identifier with single unnamed scalar | The `"value"` fallback in `IDLTyper.scala:204` | `identifiers` | Yes — explicitly named-scenario fixture |
| 3 | Identifier with multiple unnamed scalars | The `<typeName>.uncapitalize` rule in `IDLTyper.scala:207` | `identifiers` | Yes |
| 4 | ADT with multiple branches | Discriminant key via `wireId`; per-branch shape | `algebraics` | Yes — fixture for each branch |
| 5 | ADT with a single branch | Common bug class: collapse to non-ADT shape | None obvious; verify in `algebraics` or add | Yes; if absent, add a one-branch ADT to `algebraics` |
| 6 | Interface with implementing DTOs | `wireId`-keyed encoding via `CirceTranslatorExtensionBase.scala:114` | `inheritance`, `diamonds`, `datainheritance` | Yes |
| 7 | Interface with NO implementing DTOs | Degenerate case — does the encoder emit anything decodeable? | Check `inheritance`; likely none — needs new minimal model | Yes; add a one-line interface-with-no-impl to an existing domain |
| 8 | Nested ADT inside a struct field | Recursive encoding correctness | Likely `algebraics` | Yes |
| 9 | Optional inside an Optional | Edge case for Circe `Option[Option[T]]` | Likely none — needs synthesis | Yes; add one field to `dtofields` |
| 10 | List of Optionals | Edge case for `Encoder[List[Option[T]]]` collapsing absent | Likely `dtofields` | Yes |
| 11 | Map with optional values | Edge case for `Encoder[Map[K, Option[V]]]` | Likely none — synthesize | Yes; add a field to `dtofields` |
| 12 | Service method with no input, no output | Singular vs Void distinction | `services` | Yes |
| 13 | Service method with `goodAlt` / `badAlt` outputs | `Success`/`Failure` ephemeral DTO names | `services` | Yes |
| 14 | Service method input that is itself an Identifier | Method-input ephemeral wrapper around an Identifier | `services` (verify) | Yes |
| 15 | Buzzer (if Buzzer survives per cross-cutting decision C5) | Buzzer-kind packet wire shape | `buzzers` | Yes if C5 keeps Buzzers |
| 16 | Stream (if Stream survives) | `stream:s2c`/`stream:c2s` packet wire shape | `streams` | Yes if streams are kept |
| 17 | Cross-domain reference (one type imports from another) | Multi-domain `wireId` correctness | `phase`, `clones`, `aliases2`, `izumi/*` | Yes |
| 18 | Const block | `RawVal`/`ConstValue` JSON form (per C6 of meta-plan, currently incompletely typed) | `consts` | Yes (deferred until C6 resolved) |
| 19 | Long Unicode string in string field | UTF-8/UTF-16 escape correctness | None — synthesize | Yes; add a `name: str` field to a `dtofields` DTO and fixture with `"мудыла 🙂"` |
| 20 | Large integer at int64 boundary | `TInt64` near `Long.MaxValue` / `Long.MinValue` | None — synthesize | Yes; add fields to `dtofields` |
| 21 | Very-deep recursion of optional/list | Stack-safety of encoder | None — synthesize | Yes; one fixture exercising 10-level nesting |
| 22 | Inheritance flattening with field-name collision | `idNameFix` interaction with parent fields; declaration order across flattening | `diamonds`, `clones`, `substraction`, `upcasts` | Yes |
| 23 | Empty struct (no fields) | Encoder for the empty object case | Verify `syntax`/`ast`; likely synthesize | Yes |
| 24 | Enum with single member | Degenerate enum | `enums` (verify) | Yes |
| 25 | jsonlike (open-typed JSON field) | `jsonlike` encodes raw `Json` per `RpcPacket.data` precedent at `packets.scala:88` | `jsonlike` | Yes — explicitly fixture with arbitrary JSON value |
| 26 | Anyval-shaped struct (single-field DTO) | The `AnyvalExtension` path at `CirceTranslatorExtensionBase.scala:258–273` | `anyvals` | Yes |
| 27 | `TBLOB`-typed field in a DTO | Builtin scalar parity across all back-ends; **non-negotiable coverage gap** because C# emits `???` (`JsonNetExtension.scala:188`) for `TBLOB` and has no baseline. See §11 Q12. | None — synthesize a `blob: blb` field on `dtofields` | Yes for Scala+TS; **excluded from C# leg** until Q12 resolved |

Total fixture count estimate: ~80–120 small JSON files across the 22
domains, weighted toward `dtofields`, `algebraics`, `services`, and
`identifiers` which carry the most wire-format edge cases.

Recommendation: do NOT propose new `.domain` files for items #5, #7,
#9, #11, #19, #20, #21, #23. Instead, add one or two minimal type
declarations to `dtofields.domain` and `algebraics.domain` to cover
the synthetic cases. New `.domain` files would expand the legacy
compiler's input surface in a way that complicates the baseline.

## §7. Negative tests (rejected models)

**Recommendation: yes, in scope, but as a separate small corpus.**

Argument: the new typer's diagnostics are a user-facing surface (per
meta-plan cross-cutting decision C8: "new typer must produce
diagnostics through every phase, no exceptions for user errors"). If
PR-02 silently changes which inputs are accepted vs rejected — for
example, by being more permissive about a previously-rejected model
— consumers will start checking in models that the legacy compiler
would have rejected, and a future revert is impossible. The baseline
must therefore include a "broken model" corpus that asserts each
intended diagnostic.

Counter-argument: the legacy compiler today reports errors via
`IDLException`, which is a thrown exception with a free-form string
(see `IDLPostTyper.fixType:157`, `:187`, `:192` per the meta-plan).
The diagnostic *messages* are not part of any spec and will change in
PR-02 by design. Asserting on exact message strings would create
constant churn.

Synthesis: the negative-test *fixtures* are checked in now (each
broken model under
`idealingua-v1-test-defs/src/main/resources/defs/negative/<topic>/<name>.domain`
plus a sibling marker file declaring "the legacy compiler must
reject this input"). The *kind*-of-diagnostic assertion is
**deferred to PR-02**. Reasoning: the legacy compiler reports
diagnostics by throwing `IDLException` with a free-form message;
bridging "thrown string" to a structured `kind` enum requires
either (i) shipping a parallel structured-diagnostics type
alongside PR-02 or (ii) brittle substring-matching against
`IDLException` messages. Both are out of PR-03 scope.

What PR-03's harness asserts today is therefore just:

> for each negative fixture, the legacy compiler throws *something*.

That is, `NegativeSpec` invokes `TypespaceCompilerBaseFacade.compile`
on each negative input and asserts that the call raises an
exception (the legacy `IDLException` family). The kind of error
is not asserted; the test is purely "does it reject?".

PR-02 will tighten this. When the new typer ships structured
diagnostics, `NegativeSpec` will be extended to assert specific
`kind`s — at that point each negative fixture's marker file will
list the expected diagnostic kind(s) (e.g. `DuplicateMember`,
`AdtMembers`, `BasicNamingConventions`, `AdtConflicts`,
`CyclicUsage`, `CyclicInheritance`, `CyclicImports` — the rule
kinds enumerated by `TypespaceVerifier.scala`). Until PR-02 ships
structured diagnostics, the marker file is empty — only the
fixture's existence and the "must reject" assertion are
load-bearing.

Directory layout:
`idealingua-v1-test-defs/src/main/resources/defs/negative/<topic>/<name>.domain`,
with a sibling `<name>.must-reject` marker file (zero bytes; its
presence is what `NegativeSpec` reads). Example skeleton:

- `negative/cyclic-imports/two-domains-import-each-other/a.domain`
  + `b.domain`
  + `a.must-reject`
- `negative/duplicate-member/two-fields-same-name/m.domain`
  + `m.must-reject`

Two example broken inputs to seed the corpus:
1. `negative/duplicate-member/dto-with-two-x-fields/m.domain`:
   ```
   domain test.dup
   data X { x: i32; x: str }
   ```
2. `negative/cyclic-inheritance/a-extends-b-extends-a/m.domain`:
   ```
   domain test.cyc
   mixin A: B {}
   mixin B: A {}
   ```

The negative corpus is independent of Layers A/B/C — those run on the
positive corpus. Negative tests run as a fourth layer, smaller and
faster, but on the same critical path.

## §8. CI integration

**Where it runs.** The harness lives in a single test source tree (see
§9 for module choice). It runs as part of the existing SBT
`test` step in CI — i.e., adding a project to `aggregate` in
`build.sbt` near the existing aggregations at `build.sbt:1739`,
`:1743`, `:1757`, `:1761`. No new GHA workflow file; instead the
existing `.github/workflows/` test job picks it up via SBT.

**Cost (order of magnitude).**
- Layer A: regenerate-and-diff. Compiling 22 domains × 3 languages
  through `TypespaceCompilerBaseFacade` is the bulk. Empirically
  (per the meta-plan §"Performance assumption"), the legacy typer is
  slow on the corpus; conservatively budget 60–120 s for Layer A
  in CI.
- Layer B: per-language runtime round-trip. Scala suite is on the
  same JVM (negligible overhead, ~10 s for ~100 fixtures). TS via
  Node: cold-start Node + load generated module + decode 100
  fixtures ≈ 30–60 s. C# via dotnet: similar, 30–60 s, mostly
  startup.
- Layer C: cross-language matrix (~6 directions × ~10 representative
  fixtures × driver round-trip). With drivers in daemon mode,
  ~30–60 s. Without daemon mode, multiplies by per-fixture process
  startup cost — could be 5+ minutes. Daemon mode is required.
- Negative corpus: ~10 s.
- **Total budget**: 3–6 minutes added to CI on a cold cache; less on
  warm. Acceptable as one-time cost at PR submission.

**Local regenerate-goldens.**
- `sbt regenerateGoldens` writes `idealingua-v1-test-defs/src/main/resources/golden/` in place.
- On CI, the same task is run with `--check` semantics (compare and
  fail) by a wrapper test that uses an in-memory file tree comparison.
- A maintainer who deliberately changes a wire-affecting rule runs
  `regenerateGoldens` locally, inspects the diff, classifies it per
  the §3 Layer A diff policy, and includes the regenerated goldens in
  their PR.

**Environment additions to `flake.nix`.** The current flake (per
`/home/pavel/work/safe/idealingua-v1/flake.nix`) already provides JVM
+ SBT. Add:
- `nodejs_22` (or current LTS) for the TS driver.
- `dotnet-sdk_8` (or current LTS) for the C# driver.
- The driver invocation paths read from env vars (e.g.
  `IDL_NODE_BIN`, `IDL_DOTNET_BIN`) so the harness does not assume
  a fixed `nix store` path.

**How a fixture-byte-mismatch failure surfaces.**

Test name format: `<Layer>Spec.<domain>.<type>.<scenario>.<lang or langPair>`.
Example: `WireFixtureSpec.algebraics.MyAdt.minimal.scala` or
`CrossLangSpec.algebraics.MyAdt.minimal.scala→typescript`. The failure
log includes:
- The file path of the fixture.
- The first diverging byte index.
- A unified diff of the canonicalized expected vs actual JSON (limited
  to ~20 lines around the diff).
- The wire-format-fingerprint hash (if §11 question on fingerprinting
  is resolved as "yes, now") for both expected and actual, to
  fast-classify whether the regression is structural vs cosmetic.

## §9. Module layout

**Two options.**

**Option A — Inside `idealingua-v1-compiler/src/test/scala/`** (the
currently-empty test tree per the meta-plan §"Findings, idealingua-v1
today" line on `idealingua-v1-compiler`). Pros: zero new build
configuration; the harness sits next to its compiler; existing
SBT cross-aggregate already includes the compiler. Cons: pulling the
TS/C# tooling and Node/dotnet binaries into the compiler module's
test scope makes the compiler module non-buildable without those
external tools — a regression for any developer who only wants to
build the compiler. Also conflates "compiler self-test" with
"runtime cross-language regression test".

**Option B — New `idealingua-v1-test-harness` module.** Pros: clean
separation. The compiler module remains buildable in isolation; the
harness module is opt-in for full-test runs. The harness can depend
on all three runtime modules
(`idealingua-v1-runtime-rpc-{scala,typescript,csharp}`) plus
`idealingua-v1-test-defs % "test->compile;compile->compile"` plus
the drivers. Cons: one more module to maintain; needs a new
`build.sbt` entry near the existing test-defs entry at
`build.sbt:929`.

**Recommendation: Option B (new `idealingua-v1-test-harness` module).**

Justification:
1. The harness depends on Node and dotnet at test time. Putting that
   dependency on the critical path of "build the compiler" is a
   regression for casual contributors.
2. Cross-cutting decision C10 in the meta-plan already left this
   open and explicitly named "new module" as a candidate.
3. The harness is conceptually a test-time artifact, not a compiler
   sub-component. Mixing the two encourages future drift (a
   compiler-internal helper getting reused in the harness, then
   becoming load-bearing in the harness, then being hard to refactor).
4. Naming: `idealingua-v1-test-harness` is consistent with the
   existing `idealingua-v1-test-defs` resource module convention
   (the `-test-` infix).
5. Opt-out path: if a contributor doesn't have Node or dotnet,
   they can run `sbt "project idealingua-v1-test-harness; testOnly
   izumi.idealingua.harness.scala.*"` to run only the Scala-pure
   subset.
6. Migration: nothing forbids moving the Layer A goldens *resources*
   into `idealingua-v1-test-defs` (which already has `src/main/
   resources/`), while the test code itself lives in
   `idealingua-v1-test-harness`. This split co-locates the
   golden inputs with the rest of the test corpus while keeping
   external-tool dependencies out of `-test-defs`'s build graph.

## §10. Risks and assumptions (specific to PR-03)

These build on, and do not duplicate, the meta-plan's risk list.

1. **Cross-language pre-existing drift surfaced by the baseline step.**
   Per meta-plan §"Risks 2", the three back-ends may already disagree
   on edge cases. The baseline step in §5 is the first time anyone
   has cross-checked them. **Mitigation:** treat any failure of the
   baseline cross-language check as a known-issue ticket, not a
   blocker. The baseline records *today's* asymmetries as the
   point-of-comparison; the harness asserts that PR-02 does not
   make them worse. A future PR can fix the asymmetries one at a
   time. If a baseline cross-language check finds mass divergence
   (>20% of fixtures fail), revisit Layer C scope: cover only those
   types where parity is currently good and file the rest as
   not-yet-covered.

2. **Circe field-ordering coupling that any IR refactor could break
   invisibly.** The new typer's struct-flattening pass (PR-02) will
   compute a new ordering for every flattened struct. Even if the
   IR carries a "preserve declaration order" annotation, the
   *flattening rule* itself (parent-fields-first vs.
   child-fields-first vs. interleaved) is a wire-affecting choice.
   **Mitigation:** make the flattening rule explicit and tested by
   Layer A (the source diff catches it) and document it in §4.3 of
   the spec. PR-02 must include a "field-ordering invariant"
   sub-section in its plan.

3. **Test-corpus gaps.** Per §6, several wire-format edges have no
   coverage in today's `.domain` files (single-branch ADT, empty
   interface, optional-of-optional, large integers, deep
   nesting). **Mitigation:** the §6 audit explicitly enumerates the
   gaps and proposes adding minimal type declarations to existing
   domains. The audit is a one-time cost during the baseline PR;
   future contributors who add new wire-format-affecting features
   are expected to extend the audit.

4. **CI cost growth from cross-language tests.** §8 budgets 3–6
   minutes. If the matrix grows (more languages, more fixtures)
   the budget grows linearly. **Mitigation:** Layer C runs on a
   subset of representative fixtures, not the full corpus. The
   subset is named explicitly in the harness configuration
   (`harness.config.crossLangFixtures = […]`). Adding a new
   cross-lang fixture is a deliberate decision, not implicit.

5. **Authorship overhead of fixtures.** §6 estimates 80–120 fixture
   files. Each is small but each requires reviewer attention.
   **Mitigation:** the scaffold mode (§3 Layer B) cuts authorship
   cost by ~10×. Reviewer cost is one-time during the baseline PR.
   A "fixture-only" PR template is added that highlights the
   (domain, type, scenario) triple in the description, and code
   owners for the harness module review fixture PRs preferentially.

6. **Driver-process lifecycle complexity.** §3 Layer C's
   recommendation of daemon-mode drivers introduces process
   management, kill-switches, and potential test-harness flake.
   **Mitigation:** the driver protocol is line-oriented (one
   request per line of stdin, one response per line of stdout)
   with a hard 30-second timeout per request and a 5-second
   shutdown timeout. The harness restarts a driver between
   non-trivially-failing fixtures to avoid cascading state. If
   daemon mode proves flaky in the first month, the fallback is
   one-process-per-fixture at the cost of ~5× slower CI.

7. **Fixture review fatigue.** Hundreds of small JSON files become
   "noise" in PR diffs after the baseline lands. **Mitigation:** a
   `.gitattributes` rule marks `wire-fixtures/**/*.json` and
   `golden/**/*` as `linguist-generated`, so GitHub collapses them
   by default in PR review. Reviewers explicitly expand only the
   fixtures relevant to the PR's scope.

8. **The `wireId` formula assumes `path.toPackage.mkString(".")` is
   stable across legacy and new IR.** The new IR may model packages
   differently (e.g., `Vector[String]` vs `Seq[String]`,
   normalized vs raw). **Mitigation:** §4.2 of the spec freezes
   the formula as `<dot-joined>.<name>`; the new IR provides a
   stable accessor that produces the same string for the same IDL
   input. Layer A would catch any drift on the corpus, but the
   spec doc is the contract.

9. **Time-format stability.** The `TTs`/`TTsTz`/`TTsU`/`TTime`/`TDate`
   builtins delegate to `IRTTimeInstances` (referenced at
   `CirceTranslatorExtensionBase.scala:25`); a JDK or library upgrade
   that shifts ISO-8601 formatting (e.g. drops the trailing `Z`
   in favour of `+00:00`) breaks the wire format invisibly to the
   typer. **Mitigation:** Layer B fixtures include time-typed
   values in canonical-form representations; the canonicalizer
   normalizes equivalent ISO-8601 strings to a single form, but
   the *fixture* is checked against the *non-canonicalized* output
   to catch library-level drift. This implies an extra "raw"
   compare pass on time-typed fixtures, separate from the
   canonicalized round-trip.

## §11. Open questions for the user

- [ ] **Q1: Strict vs canonicalized JSON byte comparison.** §3 Layer B
      now recommends **byte-strict raw comparison for all payloads**
      (both envelope and inner), with a parallel canonicalization
      sanity check. An earlier draft demoted inner payloads to
      canonical-only; that policy was rejected because it silently
      accepted field-order changes inside payloads, contradicting
      the stated wire-format invariant. Confirm the new default?
      Downstream consequence: the new typer must preserve Scala
      field declaration order through every IR pass; the baseline
      locks a specific field order forever; fixture authors must
      pre-normalize per-language asymmetries (with `<scenario>.<lang>.json`
      overrides as the opt-in escape hatch).
- [ ] **Q2: Module layout.** §9 recommends Option B (new
      `idealingua-v1-test-harness` module). Confirm? Downstream
      consequence: a new entry in `build.sbt`, new aggregate references
      at `build.sbt:1739–1761`. Cross-cutting decision C10 of the
      meta-plan blocks on this answer.
- [ ] **Q3: Negative tests in scope.** §7 recommends yes; PR-03
      ships fixtures plus a "legacy throws something" assertion,
      with kind-of-diagnostic assertions deferred to PR-02 once
      structured diagnostics ship. Confirm? Downstream consequence:
      new directory
      `idealingua-v1-test-defs/src/main/resources/defs/negative/`,
      and the new typer (PR-02) must expose a structured diagnostics
      type that the harness can pattern-match on.
- [ ] **Q4: Wire-format fingerprint-hash now or later.** PR-01 §"Phase
      9 — computeDeepSchema" makes the case for a per-type SHA-256
      fingerprint as a wire-format invariant. Should the fingerprint
      be implemented (a) as part of this harness from PR-03, or (b)
      deferred to PR-02 alongside the new typer? Recommendation: (b)
      — the fingerprint is naturally a typer-pass output, not a
      harness output, so it belongs in PR-02. The harness consumes
      the fingerprint when it lands. Until then, Layer A goldens are
      the regression signal.
- [ ] **Q5: Baseline branch.** §5 step 4 proposes tagging the
      baseline on `wip/mudyla` (the branch hosting the modernization
      per `git log` at the time of writing). Alternative: tag on
      `develop` at a fresh `vX.Y.Z` release commit. Downstream
      consequence: if tagged on `wip/mudyla`, any pre-modernization
      drift on `develop` after the tag must be back-ported to the
      baseline; if tagged on a `develop` release commit, the baseline
      is more stable but PR-02's first commit may already drift from
      it because of unrelated `develop`→`wip/mudyla` differences.
      Recommendation: `develop` at the next minor release commit,
      after merging any other in-flight `develop` work, then merge
      `develop` into `wip/mudyla` once.
- [ ] **Q6: What to do about `RawVal`/`ConstValue`.** Per meta-plan
      cross-cutting decision C6, the legacy typer has TODOs in
      `IDLPostTyper.translateValue`. Should the const-block fixture
      coverage (§6 row #18) be deferred until C6 is resolved?
      Recommendation: yes — defer until PR-02 has a decision on
      consts.
- [ ] **Q7: Buzzers and Streams scope.** Per meta-plan cross-cutting
      decision C5, the survival of `Buzzer`/`Streams` is open.
      Should §6 rows #15 and #16 be in PR-03 scope, or deferred until
      C5 is resolved? Recommendation: the harness *plan* covers
      both; the *baseline* covers them only if they survive C5.
- [ ] **Q8: Daemon mode for codec drivers.** §3 Layer C and §10
      risk #6 propose daemon-mode drivers for performance.
      Confirm scope, or revert to one-process-per-fixture for
      simplicity at the cost of slower CI? Recommendation: daemon
      mode, with the documented kill-switch fallback.
- [x] **Q9: Coverage of `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/izumi/`
      and `overlaytest/` corpora.** Resolved 2026-05-04 by user — broader scope, all 28 `.domain`
      files under `main-tests/source/` (22 in `idltest/`, 5 in `izumi/test/`, 1 in `overlaytest/`).
      Tracked as `tasks.md` C13. PR-03.1 implementation plan
      `docs/drafts/20260503-2300-PR0301-baseline-harness-impl-plan.md` §5 enumerates the files.
- [ ] **Q10: `TUInt64` representation near `2^63`.** §4 topic 9
      flags this as a parenthetical: when an unsigned 64-bit value
      exceeds `2^53`, encoding it as a JSON *number* loses
      precision in any consumer that parses through the IEEE-754
      double path (notably JavaScript's built-in `JSON.parse`).
      The choice is between (a) preserving today's "JSON number"
      encoding and accepting silent JS truncation as a documented
      limitation, (b) encoding `TUInt64` as a JSON string when
      the value exceeds `2^53`, or (c) encoding it as a string
      always. This is a *policy* decision, not a verification.
      Recommendation: capture today's behaviour in the baseline
      and propose option (b) as a follow-up; do not make a wire
      change in PR-03 or PR-02.
- [ ] **Q11: `TSet` iteration order on the wire.** §4 topic 10
      flags `TSet` as "indeterminate-but-stable order; today's
      Scala runtime iterates `Set` in insertion order for
      `LinkedHashSet`-typed generated fields". This is also a
      policy decision: (a) document insertion order as the wire
      contract and require all back-ends to honour it, or (b)
      canonicalize `TSet` payloads to sorted order at encode
      time across all back-ends, breaking insertion-order
      semantics for any consumer that relied on them.
      Recommendation: (a) — preserve today's behaviour, codify
      insertion order as the wire contract, and have Layer B
      assert it with insertion-ordered fixtures.
- [ ] **Q12: `TBLOB` encoding policy, in particular for C#.**
      §4 topic 9 documents `TBLOB` as base64-encoded JSON string,
      but the C# back-end at
      `JsonNetExtension.scala:188` is `case Primitive.TBLOB =>
      ???` — a Scala `???` placeholder that throws
      `NotImplementedError` whenever the transpiler hits a
      `TBLOB`-typed field. There is therefore no current C#
      baseline for `TBLOB`. Consequence: PR-02 cannot ship a
      `TBLOB`-encoding change because there is nothing to
      regress against; and the §6 audit must record `TBLOB-in-DTO`
      as a non-negotiable coverage gap until this is resolved.
      Three options: (a) implement `TBLOB` in C# as part of PR-03
      (out of scope per §1 non-goals), (b) implement it as a
      separate pre-PR-02 task and then add it to the baseline,
      or (c) freeze the corpus without `TBLOB` fields exercised
      in C# and forbid PR-02 from changing `TBLOB` encoding in
      Scala/TS. Recommendation: (b) — small, scoped pre-PR-02
      task; the harness then has a real baseline. Pending a
      decision, the §6 audit row for `TBLOB`-in-DTO is marked as
      a coverage gap and any fixture that would exercise C#
      `TBLOB` is excluded from Layer B's C# leg.

## §12. Implementation PR breakdown

The harness build-out is six incremental PRs. Each lists scope,
dependencies, success criteria, and tree-touch.

**PR-03.1: Module skeleton + Layer A scaffold.**
- Scope: create `idealingua-v1-test-harness` module per §9. Add the
  module via `sbtgen/Deps.scala` (the authoritative build source;
  `build.sbt` is autogenerated). Wire the four contractual SBT tasks
  per §5 (`regenerateGoldens`, `verifyGoldens`, `runWireFixtures`,
  `runCrossLangInterop`); the latter two are no-op placeholders for
  PR-03.2 / PR-03.4. `regenerateGoldens` walks every `.domain` file
  under `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/`
  (broader scope per Q9 / `tasks.md` C13: 22 in `idltest/` + 5 in
  `izumi/test/` + 1 in `overlaytest/` = 28 files) and invokes
  `TypespaceCompilerBaseFacade.compile` for each (lang ∈
  {Scala, Typescript, CSharp}, domain), writing in-memory
  `DomainModule.content` into
  `idealingua-v1-test-defs/golden/<lang>/<modulePath>/<moduleName>`
  (off the classpath). `verifyGoldens` runs the same compile
  pipeline and asserts byte-strict equality against the on-disk
  goldens.
- Dependencies: §11 Q2 resolved; §11 Q9 resolved.
- Success: `sbt regenerateGoldens` runs end-to-end without error and
  produces a non-empty `golden/` tree under
  `idealingua-v1-test-defs/golden/{scala,typescript,csharp}/`. `sbt
  verifyGoldens` exits 0 against the freshly-checked-in goldens
  and exits non-zero if any byte mutates.
- Tree: new module under
  `idealingua-v1/idealingua-v1-test-harness/`; edits to
  `sbtgen/Deps.scala` (regenerated `build.sbt` and
  `project/plugins.sbt` committed alongside).
- See implementation plan
  `docs/drafts/20260503-2300-PR0301-baseline-harness-impl-plan.md`
  for source-grounded T1–T6 sub-tasks.

**PR-03.2: Layer A goldens checked in (BASELINE).**
- Scope: run `regenerateGoldens` against the legacy compiler,
  classify and check in the resulting golden tree. Add the
  full `GoldenSpec` body that does the in-memory file-tree
  comparison and fails on diff. Tag the commit
  `wire-format-baseline-2026-05-03`. This PR is the baseline:
  every subsequent change diffs against this tag.
- Dependencies: PR-03.1; §11 Q5 resolved.
- Success: CI runs `GoldenSpec` clean. The baseline tag is pushed.
  All 22 (or 22 + `izumi/`) domains × 3 languages have goldens.
- Tree: many new files under
  `idealingua-v1/idealingua-v1-test-defs/src/main/resources/golden/`,
  one git tag.

**PR-03.3: Layer B fixtures + Scala round-trip test.**
- Scope: add `WireFixtureSpec` (Scala) that loads each fixture
  from
  `idealingua-v1/idealingua-v1-test-defs/src/main/resources/wire-fixtures/`,
  decodes via the generated Scala types, re-encodes, and
  byte-compares (envelope: strict; payload: canonicalized via the
  helper in the harness). Add the canonicalizer
  (`izumi.idealingua.harness.canonical.Canonicalizer`). Add the
  initial fixture corpus per §6 (start with rows 1, 2, 3, 4, 6,
  10, 12, 14 — the highest-leverage cases). Provide the
  `scaffold` mode for fixture authoring.
- Dependencies: PR-03.2; §11 Q1 resolved.
- Success: `WireFixtureSpec` passes for all fixtures in the
  initial corpus.
- Tree: new fixtures under
  `idealingua-v1/idealingua-v1-test-defs/src/main/resources/wire-fixtures/`,
  new test code in
  `idealingua-v1/idealingua-v1-test-harness/src/test/scala/`.

**PR-03.4: Layer B for TS and C#.**
- Scope: add the `ts-codec-driver` and `csharp-codec-driver`
  console apps. Add `flake.nix` entries for `nodejs_22` and
  `dotnet-sdk_8`. Add `WireFixtureSpec.typescript` and
  `WireFixtureSpec.csharp` SBT tests that subprocess the drivers
  in daemon mode and run the same round-trip on each fixture.
- Dependencies: PR-03.3; §11 Q8 resolved.
- Success: all three languages pass `WireFixtureSpec` on the
  initial fixture corpus. CI runs all three.
- Tree: new
  `idealingua-v1/idealingua-v1-test-harness/src/main/typescript/`
  and `.../src/main/csharp/` driver code; edits to `flake.nix`.

**PR-03.5: Layer C — cross-language interop matrix.**
- Scope: add `CrossLangSpec` that, for each fixture in the
  cross-lang subset, runs the (A, B) matrix per §3 Layer C.
  Daemon-mode drivers, line-oriented protocol, hard timeouts.
  Configure the cross-lang subset explicitly
  (`harness.config.crossLangFixtures`).
- Dependencies: PR-03.4.
- Success: `CrossLangSpec` passes for all configured fixtures
  in all 6 directions. Failures (if any) recorded as
  known-issue tickets per §10 risk #1.
- Tree: extends
  `idealingua-v1/idealingua-v1-test-harness/`.

**PR-03.6: Negative-test corpus.**
- Scope: add `negative/<topic>/<name>.domain` directory and
  `NegativeSpec` test that asserts the legacy typer (via
  `TypespaceCompilerBaseFacade.compile`) **rejects each broken
  model with *some* exception** (the legacy `IDLException`
  family). Per §7, the *kind*-of-diagnostic assertion is
  deferred to PR-02; this PR ships only the corpus and the
  "throws something" assertion plus the empty `<name>.must-reject`
  marker files. PR-02 will tighten the assertion when the new
  typer ships structured diagnostics.
- Dependencies: PR-03.5; §11 Q3 resolved.
- Success: every fixture in the corpus causes
  `TypespaceCompilerBaseFacade.compile` to throw, and
  `NegativeSpec` passes. (Coverage of the
  `TypespaceVerifier.scala` rule set —
  `DuplicateMember`, `AdtMembers`, `BasicNamingConventions`,
  `AdtConflicts`, `CyclicUsage`, `CyclicInheritance`,
  `CyclicImports` — is achieved by including at least one
  fixture per topic; the *test assertion* does not yet
  distinguish them.)
- Tree:
  `idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/negative/`
  + `idealingua-v1/idealingua-v1-test-harness/src/test/scala/.../NegativeSpec.scala`.

**PR-03.7: Spec doc (`docs/wire-format.md`).**
- Scope: write the full spec per the §4 skeleton. Cite each
  rule's authoring file:line. Cross-link to the harness layers
  that protect it.
- Dependencies: PR-03.5 (so the spec authors can verify against
  observed behaviour, not assumptions).
- Success: every rule in §4 has a corresponding section with
  precise wording and a concrete example. The spec is reviewed
  by the language-back-end leads.
- Tree: new file `docs/wire-format.md`.

**PR-03.8 (optional, deferred): wire-format fingerprint hashing.**
- Scope: only opens after §11 Q4 is resolved as "yes, do it".
  Adds a per-type fingerprint computation to the harness
  (initially) or the typer (preferred — see meta-plan PR-02
  scope). Layer A includes the fingerprint as a sentinel file
  per type; a fingerprint diff classifies regressions as
  structural (likely wire-affecting) vs cosmetic.
- Dependencies: PR-03.7; §11 Q4 resolved.
- Success: fingerprint files checked in, present in the baseline
  golden tree, and any change to a fingerprint blocks PR
  merge until reviewed.
- Tree:
  `idealingua-v1/idealingua-v1-test-defs/src/main/resources/golden/fingerprint/<domain>/<type>.sha256`.

End of plan.
