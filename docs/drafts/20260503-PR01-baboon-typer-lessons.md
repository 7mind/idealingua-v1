# PR-01 — Baboon typer lessons for idealingua-v1 modernization

Audience: an engineer modernizing idealingua-v1 who has *not* read Baboon's source.
Purpose: ground PR-02 (the modernization plan) in concrete Baboon facts and explicit
"keep / modify / drop" verdicts for every micro-phase and surrounding component.

The Baboon repository's default branch is `main`; all citations are taken from
`main` (the meta-plan's incidental references to `develop` are stale and should
be ignored). All file paths in citations are relative to the Baboon repo root.

Actual Baboon files read (paths used; differs from suggested where noted):
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/BaboonModule.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/BaboonTyper.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/ScopeBuilder.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/ScopeSupport.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/BaboonRules.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/BaboonEnquiries.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/RootExtractor.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/AdtInheritanceExpander.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/SymbolNames.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/TypeInfo.scala`
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/typer/BaboonFamilyManager.scala`
  (the meta-plan suggested a different folder; the file lives under `typer/`)
- `baboon-compiler/src/main/scala/io/septimalmind/baboon/validator/BaboonValidator.scala`

idealingua-v1 files read for verdict grounding:
- `idealingua-v1-model/.../il/ast/IDLTyper.scala` — also contains `IDLPretyper`
  (lines 31–75) and `IDLPostTyper` (lines 78–594). No separate `IDLPostTyper.scala`
  exists; the meta-plan reference was to the class, not a file.
- `idealingua-v1-model/.../typespace/TypespaceImpl.scala`
- `idealingua-v1-model/.../typespace/Typespace.scala`
- `idealingua-v1-model/.../typespace/StructuralQueriesImpl.scala`
- `idealingua-v1-model/.../typespace/InheritanceQueriesImpl.scala`
- `idealingua-v1-model/.../typespace/verification/TypespaceVerifier.scala`
- `idealingua-v1-model/.../typespace/verification/rules/` (8 rule files)

> Correction to the meta-plan: the suggested 13-element list (`parsePkg, parseVersion,
> runTyper, toUniqueMap, RootExtractor.roots, buildDependencies, DG.fromPred,
> enquiries.shallowId, computeDeepSchema, enquiries.loopsOf, makeRefMeta,
> computeDerivations, final assembly`) is faithful as a list but understates two
> facts visible in `BaboonTyper.process` (`baboon-compiler/.../typer/BaboonTyper.scala:39`):
> (a) **root extraction is followed by an alias-root pass** (lines 53–60) that
> contributes resolved alias targets to the root set — not a separate phase per se
> but a non-trivial post-step on top of `RootExtractor.roots`; and
> (b) `runTyper` is itself a multi-step compound (`BaboonTyper.scala:389`–`443`)
> with two scope-build passes around `AdtInheritanceExpander` and a separate
> alias-resolution sweep at the end. Both are documented below.

---

## 1. Top-level pipeline overview

Baboon's compilation pipeline, as wired in `baboon-compiler/.../BaboonModule.scala:52–69`,
is **Parser → Validator → Typer → Comparator → Translator → RuntimeCodec**:

- `BaboonParser` consumes raw `.baboon` text into `RawDomain`.
- `BaboonFamilyManager` (`baboon-compiler/.../typer/BaboonFamilyManager.scala:30`)
  orchestrates parsing, cross-domain dependency, per-version caching, and
  hands off batches to the typer.
- `BaboonTyper.process` (`baboon-compiler/.../typer/BaboonTyper.scala:39`) is the
  per-domain pipeline that turns `RawDomain` into a `Domain` (the materialized
  IR, `DG[TypeId, DomainMember]` plus precomputed schema fingerprints, loops,
  ref-metadata, and derivation roots).
- `BaboonValidator` (`baboon-compiler/.../validator/BaboonValidator.scala:13`)
  runs *post-typer*, *over a `BaboonFamily`*: it walks each lineage and each
  domain to enforce the fully-typed-IR invariants. Note: in Baboon the
  validator is family-scoped, not raw-AST-scoped. Lots of name-shape /
  scope checks happen earlier inside the typer micro-phases via `SymbolNames`.
- `BaboonComparator` derives version-to-version diffs across a lineage.
- `BaboonRules` (`baboon-compiler/.../typer/BaboonRules.scala:11`) consumes
  the diff and derives field-level conversion programs.
- Per-language `BaboonAbstractTranslator` consumes the materialized `Domain` plus
  the `BaboonRules` output to emit code; `BaboonRuntimeCodec` bakes the runtime.

**Mapping onto idealingua-v1's existing flow** (parser → IDLPretyper → IDLPostTyper
→ TypespaceImpl → translators):

| Baboon stage | idealingua-v1 analogue | Notes |
|---|---|---|
| `BaboonParser` | `idealingua-v1-core/.../il/parser/IDLParser` + `il/loader/ModelLoader*` | ide-v1 parser also handles imports + overlay merge in the loader, before typing. Baboon does that in `BaboonFamilyManager`. |
| `BaboonValidator` (over typed family) | `typespace/verification/TypespaceVerifier.scala:7` (over `Typespace`) | both validate the typed/post-typer IR, not raw AST. Both ride on top of cycle/duplicate/naming rules. |
| `BaboonTyper` per-domain micro-phases | `IDLTyper.scala:19` → `IDLPretyper` (lines 31–75) → `IDLPostTyper` (lines 78–594) | Baboon: ~13 named phases plus trivial gluing steps, each producing a typed-IR-N+1. idealingua-v1: 1 monolithic post-typer that interleaves resolution, validation, and structural-fact derivation, throwing `IDLException` on user error. |
| `BaboonFamilyManager` (cross-domain index, parallel parsing) | `IDLPostTyper.getDomain` + `TypespaceImpl.transitivelyReferenced` | KEEP shape, drop lineage/reload — see §3 BaboonFamilyManager and Lesson 3. ide-v1 has *no shared* cross-domain cache; each post-typer + each typespace clones a fresh per-domain typer recursively (`IDLPostTyper.getDomain` `IDLTyper.scala:84-89`, `TypespaceImpl.transitivelyReferenced` `TypespaceImpl.scala:14`). This is the load-bearing performance defect. |
| `BaboonComparator` + `BaboonRules` (version diffing + conversion derivation) | (no analogue) | ide-v1 has no version-DAG semantics. **DROP** entirely (see §3 and §6). |
| Per-language `BaboonAbstractTranslator` consumes materialized `Domain` | `idealingua-v1-transpilers/.../*Translator.scala` consumes `Typespace` (a query-API facade) | ide-v1 translators re-derive structural facts on demand via `StructuralQueriesImpl` + `InheritanceQueriesImpl`. Baboon translators consume a finished value. |

---

## 2. Per-domain typer micro-phases

The phases below are taken in source order from `BaboonTyper.process`
(`baboon-compiler/.../typer/BaboonTyper.scala:39`–`90`) plus the `runTyper`
sub-pipeline (`BaboonTyper.scala:389`–`443`). Each entry is grounded in the
file:line where the phase originates. Note that Phase 3 (`runTyper`) is a
private method called from `process` at line 45; its body lives at lines
389–443. The remaining phases are inlined into `process` itself at lines
47–90 — so the cited line numbers are not monotonic when read top-to-bottom
through this section.

### Phase 1 — parsePkg
- **Input IR**: `RawDomain.header` (raw header AST).
- **Output IR**: `Pkg` value (typed package id).
- **What it does**: Extracts the typed package id from the raw header.
  Cite: `BaboonTyper.scala:43` (`id <- componentParsers.parsePkg(model.header)`).
- **idealingua-v1 verdict**: **modified**. ide-v1 already parses package as part of the
  loader; the equivalent typed `DomainId` is *available* on `DomainMeshLoaded` after
  `IDLPretyper.perform` (`IDLTyper.scala:60-74`) but is re-derived inside
  `IDLPostTyper.fixPkg` (`IDLTyper.scala:577`) and `fixServiceId`
  (`IDLTyper.scala:469`), weaving package-id materialization throughout the monolith.
- **Rationale**: Surface the package id as the very first phase of the new typer
  rather than threading it through every subsequent transformer. The analogue
  is `defn.id: DomainId` already available on the `DomainMeshLoaded` input
  (`IDLPretyper.perform`, `IDLTyper.scala:60`–`74`); the new typer can take it
  as a phase-input and stop re-deriving it.

### Phase 2 — parseVersion
- **Input IR**: `RawDomain.version` (raw version literal).
- **Output IR**: `Version` value.
- **What it does**: Parses the per-domain semver/version literal.
  Cite: `BaboonTyper.scala:44` (`version <- componentParsers.parseVersion(model.version)`).
- **idealingua-v1 verdict**: **no**.
- **Rationale**: idealingua-v1 has no per-domain version semantics — `DomainDefinition`
  carries `id: DomainId` and `meta: DomainMetadata` (`IDLTyper.scala:125`–`133`)
  but no version. Versioning was only ever wired through Baboon's lineage / family
  / comparator triad; without those, parsing a version is dead weight.

### Phase 3 — runTyper (compound)
- **Input IR**: `Pkg` + `Seq[RawTLDef]` + `RawNodeMeta`.
- **Output IR**: `TyperOutput(defs: List[DomainMember], renames: Map[TypeId.User, TypeId.User], aliases: List[AliasInfo])`.
- **What it does**: A six-step internal pipeline (`BaboonTyper.scala:389`–`443`):
  (a) seed builtin members (`scala:395`); (b) build the *initial* scope tree over
  pre-expansion raw defns (`scala:403`); (c) flatten + topo-sort that tree
  (`scala:404–405`); (d) run `AdtInheritanceExpander` (`scala:406`) so all
  `+/-/^` ADT inheritance arms are desugared to literal branches; (e) **rebuild**
  the scope tree over the rewritten defns (`scala:413`) so the synthesized
  branches are registered as nested scope entries; (f) compute renames,
  re-toposort, then fold over the ordered scopes calling `BaboonTranslator.translate`
  per definition. Aliases are resolved as a separate sweep at the end (`scala:436`–`439`).
- **idealingua-v1 verdict**: **modified**. The "two-scope-build sandwich around
  expansion" is specific to Baboon's ADT set-algebra; idealingua-v1 has no
  Include/Exclude/Intersect arms. The *shape* — scope tree → topo-sort →
  per-defn fold → aliases — is exactly the inversion idealingua-v1 needs.
- **Rationale**: idealingua-v1's `IDLPostTyper.perform` (`IDLTyper.scala:119`–`134`)
  iterates `defn.types`, `defn.services`, `defn.buzzers`, `defn.streams` in
  source order and translates each via `fixType` / `fixService` / `fixBuzzer` /
  `fixStreams` — without an explicit scope tree, without topo-sort, and with
  cross-domain references resolved on the fly via `lookupAnother`
  (`IDLTyper.scala:404`). The lesson: introduce an explicit `ScopeBuilder` phase
  + an explicit toposort phase, and fold the per-defn translator over the
  ordered output.

### Phase 4 — toUniqueMap (indexedDefs)
- **Input IR**: `List[DomainMember]`.
- **Output IR**: `Map[TypeId, DomainMember]`.
- **What it does**: Indexes typed defs by id, failing on duplicates with
  `TyperIssue.DuplicatedTypedefs`. Cite: `BaboonTyper.scala:47–51`.
- **idealingua-v1 verdict**: **yes**.
- **Rationale**: ide-v1 has `TypeCollection` (`idealingua-v1-model/.../typespace/TypeCollection.scala`)
  doing a similar job lazily on top of `domain.types`. The typer should produce
  the index eagerly and fail fast — uniqueness is a typer-level invariant, not
  a translator-level concern.

### Phase 5 — RootExtractor.roots (+ alias-root contribution)
- **Input IR**: `Map[TypeId, DomainMember]` + (separately) the resolved aliases
  flagged `root`.
- **Output IR**: `Map[TypeId, DomainMember.User]` (the union `directRoots ++ aliasRoots`, `BaboonTyper.scala:60`).
- **What it does**: Selects the user-flagged root types
  (`RootExtractor.scala:11`), then *adds* the resolved targets of any alias
  marked `root` (`BaboonTyper.scala:53–60`). The DI module has two
  implementations — `DeclaredRootExtractor` (only types with `root` modifier)
  and `AllRootsExtractor` (everything user-defined); only the declared variant
  is wired in compiler + explorer modes (`BaboonModule.scala:57–58`).
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: ide-v1 has no `root` keyword. The closest analogue is
  "non-ephemeral, non-anonymous top-level types" — i.e. anything in
  `defn.types` that isn't a synthesized service-method input/output struct.
  ide-v1's translators currently iterate `typespace.domain.types` directly
  (e.g. `ScalaTranslator.translate` per the meta-plan) without distinguishing
  roots from interior types. For the new typer, "roots" should still exist as
  an explicit set so that translators can choose to render only roots (rejecting
  ephemeral DTOs as standalone files), but the membership rule will be
  "everything declared at top level" rather than "everything tagged `root`".

### Phase 6 — buildDependencies
- **Input IR**: `Map[TypeId, DomainMember]` + roots + initial seed predecessors.
- **Output IR**: `Map[TypeId, Set[TypeId]]` (predecessor adjacency).
- **What it does**: Recursive (tail-recursive) BFS over `enquiries.fullDepsOfDefn`
  to build the predecessor map starting from roots; expands the frontier each
  pass and stops when no new types are reachable. Cycles are *retained*, not
  pruned. Cite: `BaboonTyper.scala:355`–`387` (impl), `BaboonTyper.scala:61`–`65`
  (call site).
- **idealingua-v1 verdict**: **yes**.
- **Rationale**: ide-v1 has no materialized type-dep graph; structural queries
  walk the typed AST live. `StructuralQueriesImpl` (281 lines) and
  `InheritanceQueriesImpl` (100 lines) both do per-call recursion on demand.
  Materializing the graph once at typing time is the single biggest performance
  lever and is what enables phases 7, 9, 10 to be O(N) rather than per-query
  O(traversal).

### Phase 7 — DG.fromPred
- **Input IR**: predecessor adjacency + node metadata.
- **Output IR**: `DG[TypeId, DomainMember]` (Izumi's directed graph).
- **What it does**: Wraps the adjacency + node table into a `DG`, also computing
  the `excludedIds` set (`indexedDefs.keySet \ graph.meta.nodes.keySet`, line 73)
  — types that exist in the index but are unreachable from any root. Cite:
  `BaboonTyper.scala:67–73`.
- **idealingua-v1 verdict**: **yes**.
- **Rationale**: An `excludedIds` set is exactly the diagnostic ide-v1 lacks
  today: ephemeral types created by the typer that aren't reachable from any
  declared root indicate either dead code or a typer bug. ide-v1 currently
  silently materializes such types into `Typespace` and the translator finds
  them via `TypeCollection`.

### Phase 8 — enquiries.shallowId
- **Input IR**: `DomainMember`.
- **Output IR**: `ShallowSchemaId` (SHA-256 of normalized one-level structural
  representation).
- **What it does**: Hashes each type's *immediate* structure — fields + own type
  id, but not transitively expanded references. Sorted-keys form so order
  doesn't matter. Cite: `BaboonEnquiries.scala:309`–`360`.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: ide-v1 has no schema-fingerprinting. In a single-version world
  the use case is *wire-format regression detection* — a stable per-type
  fingerprint that PR-03's wire-format harness can diff against goldens.
  The existing format string at `BaboonEnquiries.scala:319–340` is a plausible
  starting point; ide-v1's variant should drop the foreign-binding shape (line
  341–355) and Baboon-only ADT/contract distinctions, and add an
  identifier-id case (Baboon folds id into the dto/contract distinction;
  ide-v1 has `TypeDef.Identifier` as a separate kind, `IDLTyper.scala:148`).

### Phase 9 — computeDeepSchema
- **Input IR**: `DG[TypeId, DomainMember]`.
- **Output IR**: `Map[TypeId, DeepSchemaId]`.
- **What it does**: SHA-256 of the *transitive* structural representation per
  type; recurses through dto/contract/service/adt/enum/foreign branches, sorting
  recursive content lexicographically and emitting `[recursive:...]` tokens
  on cycles to terminate. Cite: `BaboonTyper.scala:241`–`353`. The bracket-
  framed pseudo-XML `[dto:.../dto:...]` representation is human-debuggable, not
  just a hash input.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: Same justification as Phase 8: in a single-version world the
  deep schema fingerprint is the canonical wire-format invariant. ide-v1's
  variant should align with whatever struct field-ordering rule the new IR
  picks, because Circe's `deriveEncoder` emits keys in Scala field declaration
  order (per the meta-plan §"Wire format" notes), so deep-schema must capture
  field order to be a valid regression signal.

### Phase 10 — enquiries.loopsOf
- **Input IR**: `Map[TypeId, DomainMember]`.
- **Output IR**: `Set[LoopDetector.Cycles[TypeId]]` (witnesses to cycles).
- **What it does**: Builds a dep adjacency list and runs Izumi's
  `LoopDetector.Impl.findCyclesForNodes`. Cite: `BaboonEnquiries.scala:87`–`99`.
  The result is *retained* on the `Domain` (it's a fact, not necessarily an
  error); `BaboonValidator.checkLoops` (`BaboonValidator.scala:69`–`87`) later
  decides whether each cycle is "terminating" (breakable via an enum / built-in
  / non-recursive ADT branch) and only fails on non-terminating cycles.
- **idealingua-v1 verdict**: **yes**.
- **Rationale**: ide-v1 has *both* `CyclicUsageRule` and `CyclicInheritanceRule`
  (`TypespaceVerifier.scala:14–15`) as opaque pass/fail rules. The Baboon split
  — *materialize the cycles, then ask "are they terminating?"* — is a strict
  improvement: the same data can drive cycle diagnostics, the wire-format codec
  generator (cyclic types need lazy decoders), and translator-level decisions.

### Phase 11 — typeMeta assembly
- **Input IR**: per-id shallow + deep schema ids.
- **Output IR**: `Map[TypeId, TypeMeta]`.
- **What it does**: Trivial pair-wise zip — a step worth naming because it's
  the single place where shallow and deep schema fingerprints are joined into
  the per-type metadata view consumed by translators. Cite: `BaboonTyper.scala:79`–`82`.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: ide-v1 needs per-type metadata for codec emission, but the
  fingerprint shape will diverge (see Phases 8 + 9 verdicts). Worth keeping as
  an explicit phase because the join point is where translators bind to the
  metadata contract.

### Phase 12 — makeRefMeta
- **Input IR**: `Map[TypeId, DomainMember]`.
- **Output IR**: `Map[TypeRef, RefMeta]` where `RefMeta` carries `BinReprLen`
  (a Fixed/Range/Alternatives/Unknown classification of the byte length on the
  wire).
- **What it does**: For every reachable `TypeRef`, computes the binary-encoding
  length predicate via `BaboonEnquiries.uebaLen` (`BaboonEnquiries.scala:437`–`565`)
  — fixed sizes for primitives, length-prefix ranges for collections,
  variable-with-known-min for strings/bytes, recursive aggregation across DTO
  fields. Cite: `BaboonTyper.scala:227`–`239`.
- **idealingua-v1 verdict**: **no**.
- **Rationale**: This is exclusively for Baboon's UEBA binary codec. ide-v1
  uses Circe JSON; byte-length classification has no consumer. Drop.

### Phase 13 — computeDerivations
- **Input IR**: `Map[TypeId, DomainMember]`.
- **Output IR**: `Map[RawMemberMeta, Set[TypeId]]` (per-derivation-tag ⇒ types
  that need that derivation).
- **What it does**: Walks user types, collects each type's `derivations` set
  (e.g. `derived[json|ueba]`), then for each tag-rooted set computes the closure
  of dependent types reachable via `recursiveDepsOfDefn`. The result drives
  per-language code-gen ("which types need a JSON codec emitted in language X").
  Cite: `BaboonTyper.scala:104`–`123`, `BaboonTyper.scala:92`–`101`.
- **idealingua-v1 verdict**: **no**.
- **Rationale**: ide-v1 derives codecs unconditionally — every type gets a JSON
  codec in every back-end (this is what makes the runtime-rpc protocol uniform).
  There is no `derived[X]` syntax and no need for closure computation. The
  trivial all-types implementation is the correct one for ide-v1.

### Phase 14 — final assembly (Domain)
- **Input IR**: all of the above.
- **Output IR**: `Domain(id, version, graph, excludedIds, typeMeta, loops,
  refMeta, derivations, roots.keySet, renames, pragmas, aliases)`.
- **What it does**: Assembles the materialized domain value. Cite:
  `BaboonTyper.scala:88`.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: The new ide-v1 final assembly has fewer fields (no `version`,
  no `refMeta`, no `derivations` per Phases 2/12/13 verdicts; no `renames`
  because there is no version DAG, hence no `was[T]` annotations to track
  (cf. non-goal §6 'Version-axis renaming')). Concretely, the new type should be
  something like `Domain(id, graph, excludedIds, typeMeta, loops, roots, aliases,
  pragmas)`. The crucial point is that this is **a value, not a query
  interface** — by the time translators see it, every structural fact they need
  is materialized. This is the single biggest break from
  `TypespaceImpl` (`/idealingua-v1-model/.../typespace/TypespaceImpl.scala:11`)
  which exposes lazy `tools`, `inheritance`, `structure`, `transitivelyReferenced`
  query interfaces.

---

## 3. Surrounding-infrastructure section

### ScopeBuilder
- **Input**: `Pkg` + `Seq[RawTLDef]` + `RawNodeMeta`.
- **Output**: `RootScope[ExtendedRawDefn]` (a hierarchical scope tree with
  parent links and a flat name index).
- **What it does**: Recursively builds `LeafScope`/`SubScope`/`RootScope` nodes;
  ADTs become `SubScope`s when they contain inline branches; namespaces and
  services become `SubScope`s; primitive top-levels become `LeafScope`s. Includes
  one IDL-surface concession (the `// BAB-G01:` block at
  `ScopeBuilder.scala:116-127`, inside the broader `case service: RawService =>`
  span at `:115-141`): synthesises a `data in {}` arg struct for every service
  method, so an `in`-less method signature is still scope-resolvable. Cite:
  `ScopeBuilder.scala:23`–`162`.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: ide-v1 has no scope tree; name resolution is a per-call walk
  through `IDLPostTyper.mapping` (`IDLTyper.scala:99–104`) plus `IDLPostTyper.index`
  (`IDLTyper.scala:106–117`). Lifting to a proper hierarchical scope tree
  means every later phase can ask "is name X in scope from defn Y?" in
  O(depth), not by re-resolving via package-comparison heuristics. The
  service-input synthesis trick is *not* directly applicable (ide-v1 method
  signatures have explicit `RawSimpleStructure` inputs, `IDLTyper.scala:347`)
  but the *pattern* of "do small surface-syntax desugaring once at scope-build
  time" is the lesson: e.g. the `idNameFix` defaulting in
  `IDLPostTyper.idNameFix` (`IDLTyper.scala:199–214`) belongs at scope-build
  time, not field-translation time.

### ScopeSupport
- **Input**: a `Scope`, a `RawTypeName` (or `ScopedRef`), a `Pkg`.
- **Output**: `TypeId` / `TypeId.User` / `Owner` / `Option[RawTypeRef]`.
- **What it does**: Lookup operations *over* the scope tree built by
  `ScopeBuilder`: `resolveScopedRef` (`ScopeSupport.scala:55`–`80`) walks a
  multi-segment path to a user type id; `resolveTypeId` (`ScopeSupport.scala:151`–`180`)
  resolves a single name (with optional prefix), falling back to builtin
  recognition; `resolveAlias` peeks for raw alias targets without dealiasing;
  `ownerOf` reconstructs the declarations's `Owner` (Toplevel / Ns / Adt) by
  walking parent links. The whole module is logic-free of issues unrelated
  to lookup — all defn-shape interpretation lives in `BaboonTyper`.
- **idealingua-v1 verdict**: **yes** (with renamed methods).
- **Rationale**: ide-v1 has a scattered name-resolution subsystem:
  `IDLPostTyper.makeDefinite` (`IDLTyper.scala:377–391`) handles primitives,
  generics, local lookup, and cross-domain lookup in a single match block;
  `IDLPostTyper.lookupLocal` (`IDLTyper.scala:393–402`) and `lookupAnother`
  (`IDLTyper.scala:404–407`) split the "in this domain vs. another" decision
  and do *not* share state with `fixSimpleId` (`IDLTyper.scala:506–557`),
  which independently re-routes via `getDomain(...).fixSimpleId(out)` to a
  fresh per-domain typer. A single `ScopeSupport`-style module that owns
  *every* lookup against a *shared* scope/index removes the duplicated logic
  and the recursive per-domain cloning.

### AdtInheritanceExpander
- **Input**: `Pkg`, `Seq[RawTLDef]`, ordered scope list.
- **Output**: `Seq[RawTLDef]` with all `+ X` / `- X` / `^ X` ADT inheritance
  arms desugared to literal `RawAdtMemberDto`/`RawAdtMemberContract` entries.
- **What it does**: Topo-orders ADT scopes, expands each ADT's inheritance arms
  by resolving the ref (which may be an ADT or a single branch), composing
  Include/Exclude/Intersect by *branch name*, and rewriting the `RawAdt.members`
  list. After this pass the standard typer pipeline runs unchanged. Cite:
  `AdtInheritanceExpander.scala:35`–`309` (whole file).
- **idealingua-v1 verdict**: **no**.
- **Rationale**: ide-v1 has no ADT set algebra. `RawAdt.Member.NestedDefn`
  (`IDLTyper.scala:267`) currently throws at translation time. The Baboon
  expander solves a problem that doesn't exist in ide-v1's grammar. The
  *meta-pattern* — "raw AST → desugaring pass → re-built scope tree → typer
  pipeline" — is reusable for ide-v1's `NewType` desugaring (currently inlined
  into `IDLPostTyper.fixType` at lines 174–189), but that's a different file
  and a different lesson; see Lesson 7.

### BaboonValidator
- **Input**: `BaboonFamily` (typed, family-scoped IR after typer).
- **Output**: `F[NEList[BaboonIssue], Unit]` (issues, not exceptions).
- **What it does**: Walks each lineage and each domain; for each domain runs
  ten checks (`BaboonValidator.scala:54`–`65`): `checkMissingTypes`,
  `checkConventions`, `checkLoops`, `checkUniqueness`, `checkShape`,
  `checkPathologicGenerics`, `checkAnyFields`, `checkIdentifierFields`,
  `checkUserMapKeysEligibility`, `checkRoots`. Then runs `validateEvolution`
  on the lineage's evolution (cross-version checks). All errors are
  accumulated, not thrown.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: `TypespaceVerifier` (`typespace/verification/TypespaceVerifier.scala:7`)
  already has the right shape — accumulating diagnostics into `IDLDiagnostics`
  via 7 + N rules — but the rules currently target the legacy `Typespace`
  query API. Porting to the new IR means rewriting each rule against the
  materialized `Domain`. The Baboon checks that *do* apply to ide-v1:
  `checkMissingTypes`, `checkConventions`, `checkLoops`, `checkUniqueness`,
  `checkShape`, `checkIdentifierFields`. The checks that don't:
  `checkPathologicGenerics` (Baboon-specific generic restrictions),
  `checkAnyFields` (Baboon `any` builtin doesn't exist in ide-v1),
  `checkUserMapKeysEligibility` (Baboon-specific UEBA constraint). Most
  importantly: ide-v1 must drop `IDLException` from the typer call path
  (see Lesson 5 below).

### BaboonFamilyManager
- **Input**: `List[BaboonParser.Input]` (file paths + content).
- **Output**: `BaboonFamily` (lineage of versions, with shared cache).
- **What it does**: Parses every input in parallel, builds a `DomainKey` index,
  expands the file-content map, then iteratively builds families
  (`BaboonFamilyManager.scala:52–69`); on `reload`, computes a minimal change
  set and re-parses only the dirty files (`BaboonFamilyManager.scala:71`+). The
  cache layer (`BaboonFamilyCache`) is the cross-domain shared state that
  Baboon uses to make incremental compilation cheap.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: This is *the* component with the most leverage for ide-v1's
  performance complaint. ide-v1's `IDLPostTyper.getDomain` (`IDLTyper.scala:84–89`)
  creates a fresh `IDLPretyper` + `IDLPostTyper` *per import*, with caching
  *only inside one typer instance*; combined with `TypespaceImpl.transitivelyReferenced`
  (`TypespaceImpl.scala:14–18`) which itself materializes a fresh `TypespaceImpl`
  per referenced domain (each of which recomputes `transitivelyReferenced` on
  its own), the behaviour is exponential on densely cross-referencing graphs.
  ide-v1 doesn't need lineages or reload (single-version), but it does need a
  single shared cross-domain cache built at family-load time and threaded
  through every typer phase. The new `IdealinguaFamilyManager` (or whatever
  we name it) should: (a) parse every domain in parallel; (b) build the
  cross-domain dep DAG once; (c) typecheck domains in topo order, sharing
  resolved type indices via a single `Map[DomainId, Domain]`.

### BaboonEnquiries
- **Input**: `DomainMember`/`TypeRef`/`Domain`.
- **Output**: schema-id, dep set, foreign-resolution, cycle witnesses, byte-len
  classification, parent set, etc.
- **What it does**: A grab-bag of pure queries against the typed model
  (`BaboonEnquiries.scala:23`–`43`, trait surface). Concrete impls
  (`BaboonEnquiries.scala:77`–`609`) are pure functions over the typed AST and
  are reused by the typer (Phases 6/8/9/10/12), the validator, the comparator,
  and the rules engine.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: This is the *contract surface* the new ide-v1 IR should
  expose: structural queries (transitive deps, ref enumeration, schema id,
  loop detection) but no decisions. ide-v1's `StructuralQueriesImpl`
  (281 lines) and `InheritanceQueriesImpl` (100 lines) currently exist as a
  *query interface on top of an unfinished IR* — the new model inverts
  this: queries become utility functions over a *finished* IR (the materialized
  `Domain`). Drop the foreign-binding bits (`hasForeignType`,
  `resolveForeignBinding`) and the UEBA bits (`uebaLen`).

### BaboonRules
- **Input**: `Domain` × `Domain` (prev, last) × `BaboonDiff`.
- **Output**: `BaboonRuleset` (per-type conversion programs).
- **What it does**: For each type in the previous version, decides how to
  convert it to the new version: copy-by-name, transfer-fields, expand
  precision, wrap-into-collection, swap-collection-type, or escalate to
  `CustomConversionRequired` when no automatic derivation applies. Cite:
  `BaboonRules.scala:26`–`324`.
- **idealingua-v1 verdict**: **no**.
- **Rationale**: ide-v1 has no version-DAG, no diff, no conversions. Drop entirely.
  See §6 non-goals.

### RootExtractor
- **Input**: `Map[TypeId, DomainMember]`.
- **Output**: `Map[TypeId, DomainMember.User]` (root subset).
- **What it does**: Filters to user types tagged `root` (DeclaredRootExtractor)
  or all user types (AllRootsExtractor). Cite: `RootExtractor.scala:9`–`27`.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: As covered under Phase 5: ide-v1's analogue is "all top-level,
  non-ephemeral declared types". The implementation is a single line.
  The lesson is that *root selection is a separate, named, replaceable
  policy*, not a hard-coded loop in the translator.

### SymbolNames
- **Input**: a name + meta.
- **Output**: validation effect (issue or unit).
- **What it does**: Validates type/field/enum names against three rules:
  alphanumeric+underscore, valid first character (lowercase for fields), and
  no `baboon` prefix (reserved). Cite: `SymbolNames.scala:9`–`48`.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: ide-v1 has `BasicNamingConventionsRule.scala` and
  `ReservedKeywordRule.scala` doing equivalent work but only in the
  *post-typer verifier*. Lifting these into a `SymbolNames`-style utility
  called *during* typer construction (e.g. inside the equivalent of
  `convertTypename`, `ScopeSupport.scala:139`) means the typer produces
  better-localized issues. Replace `baboon` with `idealingua` (or whatever
  the reserved namespace prefix is) in the prefix check.

### TypeInfo
- **Input**: builtin id / type-ref / type-id.
- **Output**: scalar/collection classification, comparator type, evolution
  compatibility, default-value presence.
- **What it does**: Holds the builtin registry (`scalars`, `collections`,
  `varlens`, `timestamps`, `floats`, `integers`, `seqCollections`,
  `safeSources`) and answers questions about each: is this scalar? is this
  collection? what comparator semantics? is `T → U` a precision-expansion?
  can `T` be wrapped into collection `U`? Cite: `TypeInfo.scala:32`–`188`.
- **idealingua-v1 verdict**: **modified**.
- **Rationale**: ide-v1 has the same data scattered across `Primitive`
  (`idealingua-v1-model/.../common/Primitive.scala`), `Generic`
  (`idealingua-v1-model/.../common/Generic.scala`), and ad-hoc handling in
  `IDLPostTyper.toGeneric` (`IDLTyper.scala:439`–`458`). Centralizing into a
  `TypeInfo`-style registry buys the same uniformity. The evolution / precex /
  collection-swap predicates are all version-DAG-related and **drop**;
  the comparator-type classification is also Baboon-specific (it serves UEBA
  codec gen) and drops. What remains is roughly: "is this a builtin scalar?",
  "is this a builtin collection?", "what arity does this generic take?".
  The set of builtins itself differs from Baboon (ide-v1 has fewer numeric
  types — see `Primitive` enumeration).

### BaboonComparator — explicitly DROP
The `BaboonComparator` component (referenced in `BaboonModule.scala:59` as
`make[BaboonComparator[F]]...`; entry-point trait method
`BaboonComparator.evolve(pkg, versions): F[NEList[BaboonIssue], BaboonEvolution]`
at `BaboonComparator.scala:15`) implements per-domain-pair
diff computation: it consumes two consecutive `Domain` versions and emits a
`BaboonDiff` describing per-type structural changes (added/removed branches,
field changes, renames, collection swaps, precision expansions). This entire
component is the foundation of Baboon's multi-version evolution semantics, on
which `BaboonRules` and `BaboonValidator.validateEvolution` rest. **idealingua-v1
has no version-DAG.** Each `.domain` file declares one `DomainId` and there is
no concept of comparing successive versions of the same domain — there is no
`Family`, no lineage, no evolution. The entire diff/conversion apparatus is
load-bearing for nothing in ide-v1 and should be dropped wholesale, not adapted.
This drop is what enables most of the simplification described in §6.

---

## 4. IR-level table

The table below names each IR level the new ide-v1 typer should expose, the
Baboon source it traces to, and the materialized facts that level holds.

| IR level | Source | Produced by | Consumed by | Materialized facts | Notes |
|---|---|---|---|---|---|
| **Raw AST** | `Seq[RawTLDef]` (or ide-v1's existing `RawTopLevelDefn`/`DomainMeshLoaded` from `IDLPretyper.perform` `IDLTyper.scala:31`–`74`) | Parser | ScopeBuilder | source-level structure only | Already exists in ide-v1; reuse. |
| **Scoped AST** | `RootScope[ExtendedRawDefn]` (Baboon: `ScopeBuilder.scala:23`–`46`) | ScopeBuilder | BaboonTranslator (`BaboonTyper.scala:418-430`) + ScopeSupport (`ScopeSupport.scala:55-180`) | hierarchical scope tree, parent links, flat-name index | New for ide-v1; replaces the on-the-fly resolution in `IDLPostTyper.makeDefinite` (`IDLTyper.scala:377`–`391`). |
| **Resolved AST** | typed defs + indexed map (Baboon: `Map[TypeId, DomainMember]` from `BaboonTyper.scala:47`–`51`) | BaboonTranslator (`BaboonTyper.scala:418-430`) + ScopeSupport (`ScopeSupport.scala:55-180`) | Structural / Validation | every TypeRef resolved to a TypeId, aliases resolved | Replaces the lazy `dealias` walk in `TypespaceImpl.dealias` (`TypespaceImpl.scala:41`–`49`). |
| **Structural AST** | `DG[TypeId, DomainMember]` + `roots` + `excludedIds` + `loops` (Baboon: `BaboonTyper.scala:67`–`78`) | StructuralPhase (graph + cycle detection) | Validation / Translator | dep graph, root set, excluded set, cycle witnesses, *flattened struct fields* (this is the lesson — see Lesson 4) | Replaces ide-v1's runtime queries in `StructuralQueriesImpl` and `InheritanceQueriesImpl`. |
| **Validated AST** | structural AST tagged "checks passed" (Baboon's view is implicit; the validator returns `Unit` on success: `BaboonValidator.scala:14`) | Validator (the new ide-v1 equivalent of `TypespaceVerifier`) | Translator | identical type to Structural; the level boundary is the *passing* of validation, not a transformation | Lets translators consume an "innocent until proven guilty" IR safely. |
| **Final domain** | `Domain` value (Baboon: `BaboonTyper.scala:88`) | Typer assembly (Phase 14) | Translator + RuntimeCodec | full materialized IR: graph, type-meta (shallow + deep schema id), roots, aliases, loops, pragmas | This is the boundary across which translators consume *no query interface* — only fields. |

Notes for PR-02 reading this table: each IR level corresponds to (at least) one
package in the proposed new typer layout. The "produced by" cell names the phase
class. The "materialized facts" cell is the *contract* — adding new translators
must not require new query-interface methods over a previous IR level; if a
translator needs a fact, it should be materialized into the IR at the earliest
level that can compute it.

---

## 5. Lessons applied

1. **Decompose `IDLPostTyper` into named single-purpose phases, each with a
   declared input IR and output IR.** Baboon proves this works at scale: 14
   phases (Phases 1–14 above), each independently testable, each grounded in
   a typed-IR-N → typed-IR-N+1 boundary. ide-v1's monolithic
   `IDLPostTyper.perform` (`IDLTyper.scala:119`–`134`) hides four conceptually
   independent concerns (name resolution, structural derivation, validation,
   const-value typing) behind a single `try/catch` wrapper in
   `IDLTyper.perform` (`IDLTyper.scala:20`–`28`). Concretely changes:
   `idealingua-v1-model/.../il/ast/IDLTyper.scala` — split into a phase pipeline
   under a new package `izumi.idealingua.typer.phase.*`.

2. **Materialize the type-dependency graph once.** Baboon's `DG.fromPred`
   (`BaboonTyper.scala:67`) gives every later phase O(1) graph access. ide-v1
   currently rebuilds the equivalent on every call: `StructuralQueriesImpl`
   walks the typed AST per query (281 lines of recursive walks), and
   `InheritanceQueriesImpl.implementingDtos` is called per
   ADT/interface during code-gen (per the meta-plan §3, line 35). Concretely
   changes: introduce a `TypeGraph` value at typer assembly time;
   `idealingua-v1-model/.../typespace/StructuralQueriesImpl.scala` becomes
   pure utility functions over the materialized graph instead of an
   `extends StructuralQueries` query class.

3. **Build cross-domain reference resolution against a *single shared* index.**
   Baboon's `BaboonFamilyManager` (`BaboonFamilyManager.scala:52`–`69`) parses
   all inputs in parallel, builds a `DomainKey` index, then typecheckings are
   threaded through a single shared structure. ide-v1's
   `IDLPostTyper.getDomain` (`IDLTyper.scala:84`–`89`) creates a fresh typer
   per import and is called transitively from `lookupAnother`
   (`IDLTyper.scala:404`); `TypespaceImpl.transitivelyReferenced`
   (`TypespaceImpl.scala:14`–`18`) does the same recursive cloning at the
   typespace layer. This is the load-bearing performance defect. Concretely
   changes: introduce a top-level `IdealinguaFamilyManager` (or similar);
   delete the `domainCache: mutable.HashMap[DomainId, IDLPostTyper]` field on
   `IDLPostTyper` (`IDLTyper.scala:83`); rewrite `lookupAnother` to consult
   the shared family-level index.

4. **Bake structural facts into the IR at typing time.** Baboon's `Domain`
   value (`BaboonTyper.scala:88`) carries `graph`, `excludedIds`, `typeMeta`,
   `loops`, `refMeta`, `derivations`, `roots`, `renames`, `pragmas`, `aliases`
   — every fact a translator needs, materialized once. Translators consume
   the value, not a query API. ide-v1's translators consume `Typespace` (an
   interface, `Typespace.scala`) via methods like
   `typespace.structure.structure(id)` and `typespace.inheritance.implementingDtos(id)`
   — both of which trigger lazy on-demand walks. Concretely changes: in the
   new `Domain`, add `flattenedStructs: Map[TypeId, Structure]` so
   `Typespace.structure.structure` becomes a `domain.flattenedStructs(id)`
   lookup, and `implementingDtos` becomes a precomputed `Map[TypeId, Set[TypeId]]`.

5. **No exceptions for user errors anywhere in the typer.** Baboon's effect type
   `F[NEList[BaboonIssue], A]` accumulates issues per phase
   (`BaboonValidator.scala:24`–`31`); the only `assert` calls in
   `BaboonTyper.scala` are for invariants (e.g. `assert(maybedef.nonEmpty,
   s"BUG: $id not found")` at line 249), not user-facing errors. ide-v1's
   `IDLPostTyper` throws `IDLException` from at least nine places
   (`IDLTyper.scala:58, 157, 188, 192, 268, 322, 400, 547, 552`; the
   `IllegalArgumentException` sites at `:115` and `:195` are intentionally
   preserved as invariant assertions per the prescription below);
   the verifier (`TypespaceVerifier.scala`) returns diagnostics but only
   *after* the typer has had a chance to throw. Concretely changes: every
   typer phase returns `Either[NEList[Diagnostic], A]` (or the chosen effect
   wrapper); `IDLException` becomes a "BUG" assertion only.

6. **Replace lazy structural queries with pure utility functions over a
   materialized IR.** Baboon's `BaboonEnquiries` (`BaboonEnquiries.scala:77`–
   `609`) is a 600-line module of pure functions, called by the typer/validator
   /comparator/translator alike. There is no `BaboonEnquiriesImpl extends
   ...` view of the IR; the IR is the value, the enquiries are static.
   ide-v1's `StructuralQueriesImpl` (281 lines) and `InheritanceQueriesImpl`
   (100 lines) extend trait interfaces (`StructuralQueries`,
   `InheritanceQueries`) and are constructed per-typespace in
   `TypespaceImpl` (`TypespaceImpl.scala:33–35`). Concretely changes: collapse
   the two `*QueriesImpl` classes into a static utility module; `Typespace`
   shrinks to a thin wrapper around the materialized `Domain`.

7. **Apply the "raw AST → desugar pass → re-build → typer" pattern to ide-v1's
   `NewType` (and any future surface-syntax sugar).** Baboon's
   `AdtInheritanceExpander` is a textbook example of this pattern
   (`AdtInheritanceExpander.scala:35`–`82`): rewrite raw AST in topo order,
   re-build scopes, then run the standard typer over the rewritten input.
   ide-v1 currently inlines `NewType` desugaring into the middle of
   `IDLPostTyper.fixType` (`IDLTyper.scala:174`–`189`), which interleaves
   desugaring with name resolution and makes both harder to understand.
   Concretely changes: introduce a `NewTypeExpander` phase that produces a
   `RawTLDef` list with no `NewType` entries; everything downstream stops
   needing the `case RawTypeDef.NewType(...) =>` arm.

8. **Treat cycle detection as fact-materialization, not pass/fail.** Baboon
   *retains* the cycles in the `Domain` value (`BaboonTyper.scala:78`,
   `loops = enquiries.loopsOf(graph.meta.nodes)`), then the validator
   (`BaboonValidator.scala:69`–`87`) decides whether each cycle is
   "terminating" — meaning some path through the cycle hits an enum / builtin
   / non-recursive ADT branch. ide-v1's `CyclicUsageRule` and
   `CyclicInheritanceRule` (`TypespaceVerifier.scala:14`–`15`) are opaque
   pass/fail rules; the cycles themselves never escape. Concretely changes:
   the new `Domain` carries a `loops: Set[Cycle[TypeId]]` field; codec
   generation can consult it to decide where to emit lazy decoders;
   diagnostics consult it to emit clearer error messages with cycle witnesses.

9. **Lift surface-name validation into the typer (not the post-typer
   verifier).** Baboon's `SymbolNames` (`SymbolNames.scala:9`–`48`) is called
   from inside `ScopeSupport.convertTypename` (`ScopeSupport.scala:139`–`149`)
   and so symbol-name failures are reported at the moment the name first
   surfaces. ide-v1's two name-shape rules play different roles and must not
   be conflated: `BasicNamingConventionsRule` is a *universal* verifier rule
   wired into `TypespaceVerifier.basicRules` (`TypespaceVerifier.scala:12`),
   so it runs for every domain; `ReservedKeywordRule`, by contrast, is *not*
   universal — it is constructed *per translator* and passed through each
   translator descriptor's `rules` list (`ScalaTranslatorDescriptor.scala:24`,
   `CSharpTranslatorDescriptor.scala:24`, `TypescriptTranslatorDescriptor.scala:24`),
   because each target language has different reserved keywords. Both run
   only after the typer has succeeded, meaning a single bad name shadows
   other cascade errors. Concretely changes: move the universal
   `BasicNamingConventionsRule`-equivalent into the new typer's
   name-construction site (the analogue of `convertTypename`) so it becomes
   a typer-time invariant; keep reserved-keyword checking *as a translator
   concern* (per-language, not domain-global), wired into each translator's
   verification pass — do not promote it to a typer-global rule.

10. **Make codec/derivation policy explicit, not implicit.** Baboon's
    `RootExtractor` has *two* implementations selected via DI tag
    (`BaboonModule.scala:57`–`58`). The "extract-roots" decision is named,
    swappable, and easy to change without touching translators. ide-v1's
    "what gets a codec / what gets emitted as a top-level file" decision
    is currently scattered across translator-side iteration of
    `typespace.domain.types` (e.g.
    `idealingua-v1-transpilers/.../toscala/ScalaTranslator.scala:54`,
    `ctx.typespace.domain.types.flatMap(translateDef)`) and
    translator-extension hooks (per the meta-plan §"transpilers"). Concretely
    changes: introduce a single named `EmissionPolicy` (or similar) in the
    new typer that decides "is this type emitted as a top-level file? does
    it get a codec?" — translators consult this decision, they don't make it.

---

## 6. Non-goals

The following Baboon features will explicitly NOT be ported to idealingua-v1.

- **Multi-version evolution / `BaboonComparator`.** Lives in
  `baboon-compiler/.../BaboonModule.scala:59` (`make[BaboonComparator[F]]...`)
  and `baboon-compiler/.../typer/BaboonComparator.scala` (entry-point trait
  method `evolve(pkg, versions): F[NEList[BaboonIssue], BaboonEvolution]` at
  `BaboonComparator.scala:15`). ide-v1 has no
  per-domain version literal, no lineage, no `BaboonFamily` of versions of
  the same package — each `.domain` file declares one `DomainId` and that's
  it. Dropping this drops everything reachable from it: comparator, diff
  computation, conversion derivation, version-axis renaming, evolution
  validation.

- **Conversion derivation / `BaboonRules`.** `baboon-compiler/.../typer/BaboonRules.scala`
  (whole file, 328 lines). Computes per-type field-level conversion programs
  between consecutive versions: copy-by-name, transfer, expand-precision,
  wrap-into-collection, swap-collection-type, etc. With no version DAG,
  there are no conversions to derive.

- **UEBA binary codecs.** Hooked in `BaboonModule.scala:69`
  (`make[BaboonRuntimeCodec[F]]...`) and `make[CSUEBACodecGenerator]`,
  `make[ScUEBACodecGenerator]`, and equivalents per-language inside each
  `BaboonCommonXXModule` (e.g. `BaboonModule.scala:91, 119, 147, 173, 201,
  227, 254`). The actual generators live at
  `baboon-compiler/.../translator/csharp/CSUEBACodecGenerator.scala`,
  `baboon-compiler/.../translator/scl/ScUEBACodecGenerator.scala`, and
  `baboon-compiler/.../translator/typescript/TsUEBACodecGenerator.scala`;
  the runtime contract is `BaboonRuntimeCodec.scala`. UEBA is Baboon's
  variable-length binary wire format. ide-v1's
  wire format is JSON (Circe in Scala, JsonNet in C#, plain JS in TS); the
  byte-length classification (`BaboonEnquiries.uebaLen`,
  `BaboonEnquiries.scala:437`–`565`) and its consumer (Phase 12, makeRefMeta)
  serve no purpose.

- **Version-axis renaming (`Domain.renames`).** Reachable in
  `BaboonTyper.scala:85` and computed by `BaboonTyper.computeRenames`
  (`BaboonTyper.scala:125`–`225`). Tracks "type X in v1 became type Y in v2"
  via `was[T]` annotations on derivations. With no version DAG, no `was[T]`,
  no rename map.

- **Derivation closure (`computeDerivations`).** `BaboonTyper.scala:104`–`123`.
  Per-tag closure of the dep graph, used to drive selective per-tag code-gen
  ("only emit a JSON codec for types reachable from a `derived[json]` root").
  ide-v1 emits codecs for everything in every back-end; the closure
  computation simplifies to a single map.

- **`any` builtin and its evolution rules.** `TypeInfo.scala:51`
  (`val all... ++ Set(TypeId.Builtins.any)`), and the `TypeRef.Any` cases
  scattered through `BaboonRules.scala` and `BaboonEnquiries.scala`. ide-v1
  has no `any` first-class type; ide-v1's "Anyval" concept is a different
  thing entirely — a translator-side optimization that detects single-field
  DTOs and emits them as Scala `AnyVal` value classes, implemented in
  `idealingua-v1-transpilers/.../toscala/extensions/AnyvalExtension.scala:18`
  with test fixtures at
  `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/idltest/anyvals.domain`.
  No `RawTypeDef.Anyvals` case exists in the raw AST.

- **Foreign-type bindings as per-language registrations
  (`Typedef.Foreign`).** Carried via `RawForeign` and `Typedef.Foreign` in
  Baboon (`BaboonEnquiries.scala:101`–`109` for the resolver,
  `BaboonRules.scala:103`–`105` for the conversion fallback). ide-v1's own
  `RawTypeDef.ForeignType` is currently a TODO that throws in
  `IDLPostTyper.fixType` (`IDLTyper.scala:191`–`192`); modernization keeps
  it as a TODO or deletes the syntax (open question per meta-plan
  cross-cutting decision #7).

- **Multi-language code generators for Rust, Kotlin, Java, Dart, Swift,
  GraphQL, OpenAPI, Python.** All wired in `BaboonModule.scala:161`–`340`.
  ide-v1's confirmed-keep set is Scala / TypeScript / C# only; Go and
  Protobuf are confirmed-delete (per meta-plan).

- **LSP server.** `baboon-compiler/.../lsp/*` (visible in the GitHub tree
  listing earlier). ide-v1 has no LSP today; building one is a separate
  project, not part of typer modernization.

- **Incremental reload.** `BaboonFamilyManager.reload`
  (`BaboonFamilyManager.scala:71`+). Computes a minimal change set on
  successive parse calls — only meaningful when paired with the LSP; without
  LSP, full reparse-on-edit is fine.

---

## 7. Open questions about Baboon

These are points where the source did not give me a confident answer, and
PR-02 (or a future round) needs to resolve them before locking the new typer's
design.

- [ ] **Does Baboon emit struct fields in source declaration order, or in some
      sorted order?** `BaboonEnquiries.shallowId` sorts field names
      (`BaboonEnquiries.scala:319`–`322`) for the *hash*, but the
      `Typedef.Dto` value stores `fields: List[Field]` and I did not trace the
      translator to confirm whether code-gen preserves source order or
      re-sorts. ide-v1's wire format is currently order-coupled to Scala
      field declaration (per meta-plan §"Wire format" risk #4). This needs
      confirming because the new IR's struct-field ordering decision is wire-
      format-load-bearing.

- [ ] **What does `BaboonTyper.computeRenames` produce when there are no `was`
      annotations?** I read the function body
      (`BaboonTyper.scala:125`–`225`) but can't tell from source alone whether
      the resulting `Map[TypeId.User, TypeId.User]` is empty or contains
      identity entries when no renames are declared. ide-v1 may not need this
      map at all (per non-goals), but if any other phase keys on a non-empty
      renames map, we need to know.

- [ ] **What is the contract of `enquiries.fullDepsOfDefn` for foreign types
      with `runtimeMapping` set?** `BaboonEnquiries.scala:271`–`284` shows
      that for `Typedef.Foreign`, deps include both binding-derived deps and
      runtime-mapping-derived deps. ide-v1's foreign types don't currently
      have a runtime-mapping concept; the new IR may need to introduce one
      or drop the foreign-type handling entirely.

- [ ] **Is `BaboonValidator.checkRoots` a soundness check or a policy check?**
      I read up to `BaboonValidator.scala:67` and saw the call but did not
      drill into its body. The distinction matters: if it's a soundness
      check (every reachable type has a root path), ide-v1 needs the
      analogue; if it's policy (e.g. "all top-level types must be roots"),
      ide-v1 may not.

- [ ] **How does Baboon handle duplicate `DomainId` across files? Does the
      family manager merge or fail?** `BaboonFamilyManager.scala:30`+ has
      `load`/`reload` but I only read the first ~120 lines; the dedup /
      merge policy is not pinned down. ide-v1 currently allows
      `DomainMeshLoaded` to merge multiple files into one domain (overlay
      mechanism); the new family manager needs to know whether to mirror this
      or fail.

- [ ] **Do Baboon translators ever loop back into `BaboonEnquiries` queries
      that walk the typed AST recursively, or is everything they need in
      `Domain`?** I did not read any translator. If translators *do* still
      call recursive enquiries, then the "materialized IR" lesson is weaker
      than it looks. PR-02 should confirm by reading at least one translator
      (e.g. `CSDefnTranslator`) before claiming "translators consume a
      finished value".

- [ ] **What is the exact ordering contract of `BaboonTyper.runTyper`'s
      second toposort (`BaboonTyper.scala:416`)?** It runs *after*
      `AdtInheritanceExpander` has rewritten members; the topo result is then
      fed into the per-defn translate-fold (`BaboonTyper.scala:418`–`430`).
      Does the order matter for the translator's correctness, or only for
      its convenience? ide-v1 doesn't currently topo-sort; if topo-sort is a
      correctness invariant in Baboon (e.g. dependent types must be translated
      first to be in the `acc` map), ide-v1 must preserve it.
