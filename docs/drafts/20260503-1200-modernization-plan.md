# idealingua-v1 modernization — meta-plan

## Goal

The existing idealingua-v1 compiler (Scala) parses .domain/.model IDL files into a raw AST, runs a single-pass "post-typer" that resolves identifiers ad-hoc, then hands a `Typespace` facade to per-language translators that re-derive structural facts (field flattening, inheritance, conflict resolution, converter generation) on demand. Typing is split between `IDLPostTyper` (resolves names) and `TypespaceImpl` + `StructuralQueriesImpl` + `InheritanceQueriesImpl` (computes structural truths each time a translator asks). Cross-domain references rebuild full per-domain typers (`IDLPostTyper.getDomain`) and per-domain typespaces (`TypespaceImpl.transitivelyReferenced`) recursively, with no shared cache — the proximate cause of the "insanely slow" behaviour the user described.

The modernization aims to (1) introduce a proper multi-phase typer/IR after the model of Baboon's typer (parser → validator → typer composed of focused micro-passes → translator), (2) keep wire-format byte-stability as a non-negotiable invariant tested via golden corpora plus runtime cross-version interop tests, (3) delete the unused Go and Protobuf backends, and (4) leave a maintainable Scala/TS/C# code-gen path on top of the new IR. Idealingua-v1 has no multi-version evolution semantics, so we explicitly omit the parts of Baboon devoted to version DAGs, derivations, and conversion derivation.

## Findings from exploration

### idealingua-v1 today

Modules under `/home/pavel/work/safe/idealingua-v1/idealingua-v1/`:

- **idealingua-v1-model** — Holds two AST families and the typer entry point. Source layout:
  - Raw AST: `idealingua-v1-model/src/main/scala/izumi/idealingua/model/il/ast/raw/{defns,domains,models,typeid}/*.scala` (roughly 20 case-class files, e.g. `RawTypeDef.scala`, `RawService.scala`).
  - Typed AST: `.../il/ast/typed/{TypeDef,Service,Buzzer,Streams,Structure,DefMethod,...}.scala`.
  - Typer entry: `.../il/ast/IDLTyper.scala` — class `IDLTyper` calls `IDLPretyper` then `IDLPostTyper`. `IDLPostTyper` is a 600-line monolith doing name resolution, alias dealiasing, ID-field type validation, ADT/struct/method/buzzer/stream/const-value transformation, foreign-domain recursion, and meta fixing in a single pass.
  - "Slow typer" hot spot: `IDLPostTyper.getDomain` (line 85) creates a fresh `IDLPretyper` + `IDLPostTyper` per imported domain reference and caches per-instance only; combined with the post-typer's per-call `fixSimpleId` cross-domain delegation (`getDomain(...).fixSimpleId(out)` line 555), and with `TypespaceImpl.transitivelyReferenced` (line 14 of `TypespaceImpl.scala`) which clones a fresh `TypespaceImpl` for every transitively referenced domain (each of which itself recomputes `transitivelyReferenced`) — this gives an effectively quadratic-or-worse blow-up on graphs with N domains.
  - Typespace queries: `.../typespace/Typespace.scala` (interfaces) + `TypespaceImpl.scala` + `StructuralQueriesImpl.scala` + `InheritanceQueriesImpl.scala` + `TypespaceToolsImpl.scala` + `FieldExtractor.scala` + `TypeCollection.scala`. Structural facts (flattened fields, conflict detection, `ConverterDef`s, ephemeral DTOs around services/buzzers/interfaces) are computed lazily from the typed AST at translator request time, not materialized in the IR.
  - Verification: `.../typespace/verification/TypespaceVerifier.scala` runs `DuplicateMemberRule`, `AdtMembersRule`, `BasicNamingConventionsRule`, `AdtConflictsRule`, `CyclicUsageRule`, `CyclicInheritanceRule`, `CyclicImportsRule` against the *typespace* (post-typing) — it cannot reject early because the typer doesn't produce diagnostics; instead it throws via `IDLException` (see `IDLPostTyper.fixType` lines 157, 187, etc.).

- **idealingua-v1-core** — Misnamed: contains the parser (FastParse) and the loader/resolver, not the typer. Layout:
  - Parser: `.../il/parser/{IDLParser, DefDomain, DefService, DefBuzzer, DefStreams, DefStructure, DefMember, DefConst, DefSignature, DefBuzzer, DefPositions, DefParsers}.scala` plus `parser/structure/{Aggregates,Comments,Identifiers,Keywords,Separators,Symbols,MetaAggregates}.scala` and `parser/structure/syntax/{Basic,Literals}.scala`.
  - Loader: `.../il/loader/{ModelLoader, ModelLoaderImpl, ModelLoaderContext, ModelLoaderContextImpl, ModelParser, ModelParserImpl, ModelResolver, ExternalRefResolver, ExternalRefResolverPass, FilesystemEnumerator, DomainMeshResolvedMutable}.scala`.
  - Renderer (debug): `.../il/renderer/IDLRenderer.scala` etc.

- **idealingua-v1-transpilers** — All language back-ends. Each has the same shape: a `*Translator.scala` driver, a `*TContext.scala` per-language context, an `extensions/` folder of pluggable codec/feature extensions, a `layout/` folder for filesystem layout + naming, a `products/` folder of cogen-product types, a `tools/` folder, and a `types/` folder. Backends:
  - `toscala/` — Scala (uses `scala.meta` quasiquotes; circe codec extension is the wire-format author for Scala). **Keep.**
  - `totypescript/` — TypeScript (string-template code-gen). **Keep.**
  - `tocsharp/` — C# (string-template code-gen, JsonNet codec extension). **Keep.**
  - `togolang/` — Go. **Delete.**
  - `toprotobuf/` — Protobuf. **Delete.**
  - Facade: `.../translator/TypespaceCompilerBaseFacade.scala` registers all five descriptors via `descriptors: Seq[TranslatorDescriptor[?]]`.
  - Each translator does its own "type math": `ScalaTranslator.translate` (line 32) iterates `typespace.domain.types` and asks `typespace.structure.structure(id)` per struct (Circe extension does this in `withDerivedClass`, line 187 of `CirceTranslatorExtensionBase.scala`); `inheritance.implementingDtos` is called per ADT/interface to build the codec (lines 110, 43 of the same file). Because `Typespace` and friends recompute on each call and are constructed per-domain, this is repeated work.

- **idealingua-v1-compiler** — The CLI entry point: `idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/{CommandlineIDLCompiler, IDLCArgs, ManifestReader, ArtifactPublisher, Codecs, CredentialsReader, CompilerLog, ShutdownImpl, Shutdown}.scala`. The test directory tree exists but is empty (`idealingua-v1-compiler/src/test/scala/izumi/` has no .scala files), so there is no end-to-end golden harness today.

- **idealingua-v1-test-defs** — Pure resource module. `.../src/main/resources/defs/main-tests/source/idltest/*.domain` lists 22 example domains: `algebraics`, `aliases`, `aliases2`, `anyvals`, `ast`, `buzzers`, `clones`, `consts`, `datainheritance`, `datainheritancetransitive`, `diamonds`, `dtofields`, `enums`, `identifiers`, `inheritance`, `jsonlike`, `phase`, `services`, `streams`, `substraction`, `syntax`, `upcasts`. Plus cross-package fixtures under `izumi/test/`, an `overlays/` folder demonstrating the overlay merge, and a `subdir/` test. **No generated-code golden files are checked in** — these are inputs only.

- **Runtime modules** — `idealingua-v1-runtime-rpc-{scala,typescript,csharp,go,http4s}` plus a tests-only suite at `idealingua-v1-runtime-rpc-http4s/src/test/scala/izumi/idealingua/runtime/rpc/http4s/Http4sTransportTest.scala`. Wire envelope is defined in `idealingua-v1-runtime-rpc-scala/src/main/scala/izumi/idealingua/runtime/rpc/packets.scala`: `RpcPacket(kind, data, id, ref, service, method, headers)` encoded via Circe `deriveDecoder`/`deriveEncoder`. `RPCPacketKind` constants are the literal strings `rpc:request`, `rpc:response`, `rpc:failure`, `buzzer:request`, `buzzer:response`, `buzzer:failure`, `stream:s2c`, `stream:c2s`, `?:failure`. **These strings are part of the wire format** — any rename breaks all clients. The Scala-only marshaller abstraction is `IRTCirceMarshaller` (decode/encode partial functions keyed by `IRTMethodId`).

### Baboon typer at a glance

Top-level compilation pipeline (from `baboon-compiler/src/main/scala/io/septimalmind/baboon/BaboonModule.scala`): **Parser → Validator → Typer → Comparator (version evolution) → Translator (per language) → RuntimeCodec**.

Inside `typer/BaboonTyper.process` (per-domain pipeline) the micro-phases are:
1. `parsePkg` — extract package id from header.
2. `parseVersion` — parse semver/version literal (idealingua-v1 has no versions; **drop**).
3. `runTyper` — core resolution from raw defs to typed defs, returning members + renames + aliases.
4. `toUniqueMap` — index by id, fail on duplicates.
5. `RootExtractor.roots` — identify root types (for code-gen entry points; idealingua-v1 doesn't have explicit roots but the analogue is "non-ephemeral top-level definitions").
6. `buildDependencies` — recursively build a predecessor graph of type→type references.
7. `DG.fromPred` — materialize a directed graph of type dependencies.
8. `enquiries.shallowId` — hash each type's *immediate* structure.
9. `computeDeepSchema` — recursively hash full transitive structure per type (used for cross-version equivalence; for idealingua-v1 this would be the basis of the wire-format fingerprint).
10. `enquiries.loopsOf` — cycle detection on the type graph.
11. `makeRefMeta` — distance/reference metrics.
12. `computeDerivations` — propagate derivation annotations (e.g. "this type needs a JSON codec") through dependency graph.
13. Assemble final `Domain` value with all metadata baked in.

Surrounding the per-domain typer:
- `ScopeBuilder` constructs a hierarchical scope tree from raw `RawTLDef`s, returning a `RootScope` with parent links + a flat name index. **This is a separate phase before name resolution and is what idealingua-v1 collapses into the monolithic `IDLPostTyper`.**
- `AdtInheritanceExpander` desugars Include/Exclude/Intersect ADT branches in topological order. (Idealingua-v1 has no ADT set algebra, so this phase is a no-op for us — but the pattern of "per-pass on the IR, topo-ordered" is the lesson.)
- `BaboonValidator` runs separately (post-parsing, pre-typer) over the raw AST.
- `BaboonFamilyManager` orchestrates parsing, importing, and per-version caching; this is the level at which idealingua-v1's `IDLPostTyper.getDomain` recursion belongs (one shared cross-domain index, not one typer per recursive visit).

**Lessons that transfer:**
- Split typing into named, single-purpose passes, each of which takes a typed-IR-N-1 and emits typed-IR-N. Each pass is independently testable.
- Materialize structural facts (flattened struct, parent set, cycle witness, deep schema hash) **into the IR** at typing time, so translators consume a finished value rather than calling back into a `Typespace` query interface.
- Build the cross-domain index **once**, share it across all typer phases.
- Use a `DG`-style explicit dependency graph so that any phase that needs topological order is trivial.
- Keep the validator as a phase that produces diagnostics, not exceptions — `IDLException` should disappear from the typer's call path.

**Lessons that do NOT transfer:**
- Multi-version evolution (`BaboonComparator`, `BaboonFamily`, conversion derivation, `--omit-most-recent-version-suffix-from-paths`, etc.) — idealingua-v1 has a single version per domain. We keep the *single-version* typer + structural-fact pipeline and drop the `Comparator` and version-axis machinery.
- Derivation annotations as IR primitives — idealingua-v1 derives codecs unconditionally (always JSON for runtime-rpc), so we skip phase 12.
- Baboon's wire-format generators (UEBA binary codecs) — idealingua-v1 wire format is JSON via Circe and is fixed.

### Wire format — current assertions and risks

**No wire-format spec doc exists.** Wire format is defined operationally by:
1. `idealingua-v1-runtime-rpc-scala/src/main/scala/izumi/idealingua/runtime/rpc/packets.scala` — the envelope (`RpcPacket` field order, `RPCPacketKind` literal strings).
2. The `wireId` method on `TypeId` (`idealingua-v1-model/.../common/TypeId.scala` line 31): `s"${path.toPackage.mkString(".")}.$name"`. **This is the discriminant** for ADT branches and interface-implementing DTOs.
3. The Circe extension `idealingua-v1-transpilers/.../toscala/extensions/CirceTranslatorExtensionBase.scala`:
   - ADT encoding (line 49): `Map(<wireId> -> v.value).asJsonObject`, i.e. `{ "<fully.qualified.TypeName>": { …branchFields } }`.
   - Interface encoding (line 110): same shape, keyed on the implementing DTO's wireId.
   - DTO/Identifier encoding: Circe `deriveEncoder`/`deriveDecoder`, so field names equal Scala field names equal IDL field names (after idealingua-v1's `idNameFix` rules in `IDLPostTyper.idNameFix`, line 199 — note the special `"value"` fallback for single-field unnamed identifier fields).
   - "Unwrap" mode (line 191) for method outputs of `Singular` form: encodes the single field directly, no wrapper object.
4. The TypeScript backend (`totypescript/`) and C# backend (`tocsharp/JsonNetExtension.scala`) — they emit codecs for the same JSON shape, but each backend re-derives the structure independently. **Drift between backends is currently undetected.**

**Current testing of wire format:** essentially none for the *generated* code. The only end-to-end test is `idealingua-v1-runtime-rpc-http4s/src/test/scala/.../Http4sTransportTest.scala`, which exercises a single hand-built service definition through Scala generation + http4s transport. There are no:
- Generated-source golden files (the `idealingua-v1-test-defs` resources are inputs only).
- Cross-language interop tests (does Scala-encoded JSON round-trip through a TS or C# decoder?).
- Wire-format fingerprint or schema-hash artefacts.
- Property tests or fuzz tests over codec round-tripping.

**Risks to the wire format under modernization:**
- Renaming `TypeId.path` semantics, fully-qualified package construction, or `wireId` formula — silently breaks ADT/interface JSON.
- Reordering `RpcPacket` fields or renaming `RPCPacketKind` cases.
- Changing `idNameFix` (the synthetic name rule for unnamed identifier fields) — currently `"value"` for the 1-field case, `<typeName>.uncapitalize` otherwise. Generated DTOs currently assume this exact rule.
- Changing field order for derivation-based encoders — Circe `deriveEncoder` is order-stable on Scala field declaration order; if the new typer reorders struct fields (e.g. when flattening inheritance), encoders emit JSON keys in a different order. Order doesn't affect *decode*, but it affects byte-for-byte equality and any consumer that reads as a sequence.
- Changing the "ephemeral DTO" naming for service inputs/outputs (`TypespaceTools.methodInputSuffix`, `methodOutputSuffix`, `goodAltSuffix`, `badAltSuffix`, `toPositiveBranchName`, `toNegativeBranchName`) — these names appear in `wireId` for method I/O types and so are wire-visible.
- Removing or restructuring the interface→DTO ephemeral mirror (`TypeCollection.interfaceEphemeralIndex`, `dtoEphemeralIndex`) — these synthesise DTOs whose `wireId` appears as ADT/interface discriminants.
- Drift between languages: if we change Scala but not TS/C#, an unsuspecting consumer continues to deserialize successfully but with a different field ordering or missing default; a positive integration test is the only safety net.

## Proposed PR breakdown

The deliverable of *this* session is plan documents only; each PR below produces one document. Implementation comes later.

1. **PR1 — Baboon-typer-lessons.md** *(this drives PR2)*
   Scope: A focused write-up of the relevant subset of Baboon's compiler architecture, distilled for an audience that has not read Baboon's source. Captures the per-domain typer micro-phases (1–13 listed above), the surrounding infrastructure (`ScopeBuilder`, `Validator`, `FamilyManager`), and explicitly maps each Baboon concept to its idealingua-v1 counterpart or "drop" verdict. Acts as the architectural reference for PR2.
   Success criteria:
   - Each Baboon phase listed with: name, input IR, output IR, one-paragraph "what it does", "applicable to idealingua-v1: yes/no/modified" verdict.
   - Diagram or table of the IR levels (Raw → Scoped → Resolved → Structural → Validated → Final).
   - Explicit non-goals section listing Baboon features we will not port.
   Questions the doc must answer:
   - Which Baboon phases collapse together for idealingua-v1, and which stay separate?
   - What does "deep schema fingerprint" buy us in a single-version world (answer: wire-format fingerprint, regression detection)?
   - Where does cross-domain caching live, and is the cross-domain reference graph cyclic-safe?

2. **PR2 — idealingua-modernization-plan.md** *(this is the master plan)*
   Scope: The concrete, step-by-step plan for replacing `IDLPostTyper`/`TypespaceImpl` with a multi-phase typer + materialized IR; deleting Go and Protobuf; restructuring the Scala/TS/C# back-ends to consume the new IR; and the migration sequence (parallel new typer → swap-over → delete old). Names every file to add, modify, and delete. Includes the proposed package layout for the new typer (e.g. `izumi.idealingua.typer.phase.{ScopeBuilder, NameResolver, AdtChecker, StructuralFlattener, CycleChecker, Fingerprint, RootExtractor}`).
   Success criteria:
   - Phase-by-phase definition of the new typer with named input/output IR types.
   - File-level diff plan: list of files to add, files to modify, files to delete.
   - Delete-plan for `togolang/`, `toprotobuf/`, `idealingua-v1-runtime-rpc-go/` and the corresponding manifests/`TypespaceCompilerBaseFacade` registrations.
   - Migration ordering: which PRs can land independently, which must be atomic.
   - Performance success criterion: a baseline measurement plan (compile the 22 test domains under the current typer, then under the new typer, target ≥10× speedup or specify acceptable factor).
   Questions the doc must answer:
   - Build alongside vs. in-place? (Recommended: alongside, behind a feature flag in `TypespaceCompilerBaseFacade`, until back-end parity is proven by the harness from PR3.)
   - What is the new IR's stability boundary — do back-ends consume it directly, or via a query API?
   - Scala 3 only, or keep cross-build? (Look at current `build.sbt` cross-build settings.)
   - Do we keep `Buzzer`/`Streams` first-class in the new IR? (They exist in raw + typed AST; cost of preserving them is small.)

3. **PR3 — backcompat-test-harness-plan.md** *(this is the safety net for PR2)*
   Scope: The plan for a wire-format regression harness that lets us refactor the typer with confidence. Three layers: (a) generated-source golden files committed under `idealingua-v1-test-defs/golden/<lang>/`, regenerated and diffed in CI; (b) a JSON-corpus fixture: for each test domain, a small set of hand-authored `(typeId, wireJsonSample)` pairs that the *runtime* (not just the codegen) decodes, re-encodes, and asserts byte-equality on, run against Scala / TS / C# runtimes; (c) cross-language interop tests: encode in language A, decode in language B, for at least one ADT, one interface, one nested struct, one identifier, one enum, one map. Plus a derivation-of-`wireId` audit and the proposed wire-format spec doc.
   Success criteria:
   - Concrete corpus design: which 22 domains contribute, which type/JSON pairs per domain.
   - CI integration plan: how golden files are regenerated, how diffs surface as PR comments.
   - Cross-language harness design: what runtime, what test runner, how it gets installed in CI (TS via npm, C# via dotnet).
   - A wire-format spec doc skeleton (envelope + ADT shape + interface shape + identifier rules + enum rules + ephemeral naming rules).
   - "Pre-modernization baseline" step: capture the goldens **from the current compiler** before any typer change lands, so PR2's typer must reproduce them byte-for-byte.
   Questions the doc must answer:
   - Where do the golden files live (`idealingua-v1-test-defs/golden/<lang>/<domain>/<file>`)?
   - How do we test the *negative* case — do we need a "broken model" corpus that asserts diagnostics?
   - Do we test JSON byte-equality strictly, or canonicalize first? (Strict for envelope, canonicalized for codec output, because Circe ordering is currently coupled to Scala field order.)
   - How do we run TS/C# in CI without expanding the developer setup? (Probably nix + sbt-managed test scripts.)

I considered splitting PR2 further into "PR2a: new typer" and "PR2b: backend cleanup", but the file-level dependencies between the typer's IR shape and the back-ends' consumption are tight enough that one cohesive plan doc is clearer. The implementation phase that follows will obviously land as multiple smaller PRs.

## Cross-cutting decisions

The following architectural questions need user resolution before implementation begins. Each is an open checkbox.

1. [ ] **Build alongside vs. in-place.** Recommended: build the new typer in a new package, route through it via a feature flag in `TypespaceCompilerBaseFacade`, gate per-language switchover by the harness from PR3, then delete `IDLPostTyper`/`TypespaceImpl`. Alternative: tear-down-and-rebuild in-place (faster but no safety net during transition).
2. [ ] **Which back-ends survive.** Confirmed-keep: Scala, TypeScript, C#. Confirmed-delete: Go, Protobuf (per user). Question: do we also delete `idealingua-v1-runtime-rpc-go/`, the Go manifest types in `idealingua-v1-core/.../publishing/manifests/GoLangBuildManifest.scala` and `ProtobufBuildManifest.scala`, and Go-only test domains? Recommended: yes, fully.
3. [ ] **Scala version target.** Current build cross-compiles Scala 2 + Scala 3 (visible in `idealingua-v1-runtime-rpc-scala/src/main/scala-2/` and `scala-3/`). Do we drop 2.13 for the modernized compiler? The runtime modules separately may still support 2.13 because consumers depend on them. Question for user: is dropping 2.13 in the *compiler* but keeping it in the *runtime* acceptable?
4. [ ] **ADT discriminant scheme.** The wire format uses `wireId = <pkg>.<name>` as discriminator. Confirm we lock this exact formula forever (we cannot change it without breaking clients). The plan must explicitly forbid moving any existing type into a different package.
5. [ ] **Buzzers and Streams.** Both are first-class in the typed AST today (`Buzzer`, `Streams`, `TypedStream`). The runtime supports them (`BuzzRequest`, `S2CStream`, `C2SStream` packet kinds in `packets.scala`). Question: do production consumers actually use them? If not, deprecate but keep working; if yes, the new typer must treat them on par with services.
6. [ ] **Constants (`RawVal`/`ConstValue`).** The current typer has `// TODO: verify structure` comments (`IDLPostTyper.translateValue`, lines 240, 245, 250). The new typer should fully type-check const blocks. Confirm consts are in scope for the modernization.
7. [ ] **Newtypes and ForeignType.** Current code throws `s"TODO: foreign type isn't supported yet"` (`IDLPostTyper.fixType`, line 192) and partially supports newtypes only for DTO/Interface (lines 175–187). Confirm: is ForeignType in scope for "make it work" or "delete the syntax"?
8. [ ] **Diagnostics model.** Current typer throws `IDLException`; the verifier returns `IDLDiagnostics`. Decision: new typer must produce diagnostics through every phase (no exceptions for user errors).
9. [ ] **CLI compatibility.** `CommandlineIDLCompiler` is the published binary. Decision: any flag added/removed/renamed needs a deprecation note and minimum one release of overlap.
10. [ ] **Compiler-self test corpus.** The `idealingua-v1-compiler/src/test/scala/izumi/` directory exists but is empty. Decision: the new test harness from PR3 lives here? Or is it added to a new `idealingua-v1-test-harness` module to keep test-time deps (TS, dotnet) out of the compiler module?
11. [ ] **Cross-domain reference graph cycles.** `CyclicImportsRule` exists. Confirm: the new typer enforces this *before* dependency-graph construction, not after.

## Risks & assumptions

1. **Wire-format byte-equality is not actually byte-equality today.** Circe's `deriveEncoder` emits keys in Scala field declaration order; if the new typer reorders fields (e.g. when flattening inheritance), JSON byte output changes even though semantics don't. The harness must canonicalize before comparing, AND we must explicitly preserve struct field ordering in the new IR. **Action**: PR2 must include a "field-ordering invariant" section.
2. **Cross-language drift may already exist.** Scala, TS, and C# back-ends each implement codecs separately. We may discover during the harness work (PR3) that they already disagree on edge cases. **Action**: PR3's "pre-modernization baseline" step must capture all three languages and surface any pre-existing divergence as a known issue, not a regression of our work.
3. **Test-domain coverage may be incomplete.** The 22 `.domain` files in `idealingua-v1-test-defs` may not exercise every wire-format case (e.g. nested ADTs in interfaces, ADT-of-ADT, deeply optional maps). **Action**: PR3 plan must include a coverage audit step and add fixtures where needed.
4. **`getDomain` recursion may hide subtle name-resolution semantics.** The current code re-types referenced domains lazily. If the new typer pre-computes a global index, we must reproduce the current resolution rules exactly (especially clash detection: `IDLPretyper.perform` line 56 throws on import vs. local-type name clash).
5. **Performance assumption.** The user says the typer is "insanely slow." We should *measure* before claiming a multiplier. The proposed measurement: time `TypespaceCompilerBaseFacade.compile` over the full `defs/main-tests/source/` corpus, current vs. new typer, on a fixed machine. PR2 should include the actual numbers, not estimates.
6. **Scala-meta dependency.** The Scala back-end uses `scala.meta` quasiquotes (`q"…"`). The new IR should be representable via plain Scala data types — quasiquotes are emit-time only. No risk to the typer here, but the renderer/translator restructure in PR2 must preserve the `scala.meta` boundary.
7. **Assumption that nobody uses Go/Protobuf.** The user asserted this. If wrong, deletion is a breaking change. **Action**: PR2 plan should include a "deprecate before delete" path as a fallback, even if the user is confident.
8. **Assumption that `idealingua-v1-test-defs` resources are committed inputs only.** Verified by inspection; no generated outputs are checked in. The new harness invents the golden-output convention.

## Known gaps in this meta-plan

1. **Baboon source coverage.** Only `BaboonModule.scala`, `BaboonTyper.scala`, `AdtInheritanceExpander.scala`, `ScopeBuilder.scala`, and `BaboonFamilyManager.scala` were read. PR1 must additionally read `BaboonRules.scala`, `BaboonEnquiries.scala`, `RootExtractor.scala`, `ScopeSupport.scala`, `SymbolNames.scala`, `TypeInfo.scala`, `BaboonValidator.scala`, and (briefly) `BaboonComparator.scala` (to confirm what we drop). Raw GitHub URLs work.
2. **TS and C# wire-format byte rules confirmed only by inference from the Scala Circe extension.** PR3 must trace TS (`totypescript/extensions/`) and C# (`tocsharp/extensions/JsonNetExtension.scala`) codec emission for ADTs/interfaces and confirm byte parity.
3. **No automated wire-format tests visible today.** Only `Http4sTransportTest.scala` exists; PR3 invents the harness from scratch.
