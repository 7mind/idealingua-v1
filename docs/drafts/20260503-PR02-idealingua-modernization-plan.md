# PR-02 — idealingua-v1 modernization plan (master plan)

Audience: a future engineer (could be the user, could be someone else) who must
turn this plan into incremental implementation PRs. Architectural reference:
PR-01 (`docs/drafts/20260503-PR01-baboon-typer-lessons.md`). Grounding facts:
the meta-plan (`docs/drafts/20260503-1200-modernization-plan.md`,
§"Findings from exploration" + §"Cross-cutting decisions"). This document
does not duplicate either; it cites them by section.

All citations to idealingua-v1 source use `path:line` form, anchored to the
worktree root `idealingua-v1/`. All citations to PR-01 use `§N — name` or
`§N.subsection`. Where the meta-plan introduces a fact (e.g. file counts), this
document re-asserts the fact only if PR-02's plan turns on it.

---

## §1. Goal and scope

PR-02 commits to delivering a single, executable plan that future implementation
PRs follow. Concretely: the new typer's phase-by-phase decomposition with named
input/output IR (§3); the materialized IR's data shape and ordering invariants
(§4); the deletion list for the Go and Protobuf back-ends and the
`idealingua-v1-runtime-rpc-go` runtime module (§5); the restructuring rules for
the surviving Scala / TypeScript / C# back-ends (§6); the CLI deprecation
schedule (§7); the performance baseline-and-measurement protocol (§8); the
file-level diff plan (§10); and the residual open architectural questions for
the user (§12). Everything is anchored to file:line citations from the current
codebase.

PR-02 explicitly does NOT cover the wire-format regression test harness — that
is PR-03's deliverable (meta-plan §"Proposed PR breakdown" item 3,
`docs/drafts/20260503-1200-modernization-plan.md:137-148`). PR-02 also does not
contain any implementation code: the only Scala-shaped material is the IR
pseudo-code in §4 used to specify shape, not to be compiled. PR-02 does not
re-derive Baboon's micro-phases — those live in PR-01 §2 — it maps them to
ide-v1 phases. Finally, PR-02 does not lock the codec wire-format spec doc
(also a PR-03 deliverable, meta-plan §"Proposed PR breakdown" item 3 success
criteria).

---

## §2. Overall strategy

### Build alongside vs. in-place — recommendation: alongside (locked default)

The new typer lands in a new package tree (`izumi.idealingua.typer.*`) and is
selected by a feature flag at `TypespaceCompilerBaseFacade`
(`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TypespaceCompilerBaseFacade.scala:10`).
The legacy `IDLPostTyper` (`IDLTyper.scala:78-594`) and `TypespaceImpl`
(`typespace/TypespaceImpl.scala:11-64`) remain wired and operational until the
PR-03 harness has proven byte-equality of generated artefacts across the full
corpus for Scala, TypeScript, and C#. Justification: (a) ide-v1's wire format
is operationally defined, not specified — see meta-plan §"Wire format — current
assertions and risks" — so the only safe way to evolve it is differentially;
(b) the meta-plan §"Cross-cutting decisions" item C1 already pre-selected this
approach and we are confirming, not re-deciding; (c) the alongside approach
permits per-language opt-in, which is essential because cross-language drift
may already exist (meta-plan risks #2) and only one back-end may be parity-clean
at a time; (d) tearing down `IDLPostTyper` in place would lose the only working
reference encoder during the period when the harness is being written; (e) the
size of the change (the typer + every translator's consumption surface) makes a
single big-bang PR untestable in isolation.

### Migration order (locked PR sequence)

These are the implementation PRs that follow PR-02. Each line is one PR.

1. **IMPL-1 — IR types + assembler skeleton**, no behaviour change.
   New package `izumi.idealingua.typer.ir` containing the case classes from
   §4. No wiring yet; nothing reads the IR. *Dependency:* none.
2. **IMPL-2 — `ScopeBuilder`, `NameResolver`, `AliasDealiaser`, `KindChecker`**
   (pure resolution + per-defn-kind phases, see §3 Phases 1–4). Output:
   `ResolvedDomain`. *Dependency:* IMPL-1.
3. **IMPL-3 — `CycleDetector`, `StructuralFlattener`, `EphemeralSynthesizer`,
   `ConstValueTyper`, `FingerprintCalculator`, `RootExtractor`, `Assembler`**
   (the structural-fact materialization phases, see §3 Phases 5–11). Output:
   `Domain`. *Dependency:* IMPL-2.
4. **IMPL-4 — `IdealinguaFamilyManager`** (cross-domain index, see §3 Phase 0).
   Replaces the recursive cloning at `IDLTyper.scala:84-89` and
   `TypespaceImpl.scala:14-25`. *Dependency:* IMPL-3.
5. **IMPL-5 — diagnostics-mode rewrite of verifier rules** against `Domain`.
   The 7 rules under
   `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/`
   (cited in `TypespaceVerifier.scala:9-17`) are ported one-by-one to consume
   `Domain`. The legacy verifier remains wired, but the new verifier accumulates
   diagnostics rather than throwing. *Dependency:* IMPL-3.
6. **IMPL-6 — feature-flag plumbing** in
   `TypespaceCompilerBaseFacade.scala`. Adds the dispatch and a `Typespace`
   adapter that wraps the new `Domain` so legacy translators still compile while
   they are being ported. *Dependency:* IMPL-4 + IMPL-5.
7. **IMPL-7a — Scala translator port to consume `Domain` directly.**
   Removes `ctx.typespace.structure.structure(id)` and
   `ctx.typespace.inheritance.implementingDtos(id)` call sites; reads
   pre-materialized fields off `Domain`. The Circe extension
   (`idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/extensions/CirceTranslatorExtensionBase.scala`)
   stops calling `ctx.typespace.structure.structure` (line 187 of that file).
   *Dependency:* IMPL-6 + the PR-03 harness covering Scala.
8. **IMPL-7b — TypeScript translator port.** Same shape as IMPL-7a.
   *Dependency:* IMPL-6 + the PR-03 harness covering TypeScript. Can run in
   parallel with IMPL-7a.
9. **IMPL-7c — C# translator port.** Same shape. Parallel with 7a/7b.
   *Dependency:* IMPL-6 + harness covering C#.
10. **IMPL-8 — Go and Protobuf deletion.** All §5 deletions land atomically in
    one PR. *Dependency:* none of the IMPL-7\* PRs (deletion is independent of
    porting); but blocked behind a clean `develop` baseline.
11. **IMPL-9 — flip the feature-flag default** to "new typer".
    *Dependency:* IMPL-7a + 7b + 7c + IMPL-5 all merged and a green PR-03 CI
    run on the full corpus.
12. **IMPL-10 — delete the legacy typer + `Typespace` query interface.**
    Removes `IDLPostTyper`, `TypespaceImpl`, `StructuralQueriesImpl`,
    `InheritanceQueriesImpl`, `TypeCollection`, `Typespace` trait,
    `TypespaceVerifier`. *Dependency:* IMPL-9 has been live for at least one
    release cycle (per cross-cutting decision C9 — meta-plan
    `:154-167`).
13. **IMPL-11 — remove the `Typespace` adapter** introduced in IMPL-6.
    *Dependency:* IMPL-10.

Parallelizable: IMPL-7a / IMPL-7b / IMPL-7c (three back-ends, three engineers).
Independent: IMPL-8 (deletion).

### Feature-flag mechanism

The flag lives on `TypespaceCompilerBaseFacade`
(`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TypespaceCompilerBaseFacade.scala:10`).
Concretely: `UntypedCompilerOptions` (the type passed to the constructor) gains
a single field — call it `typerImpl: TyperImpl` with `TyperImpl = Legacy |
NewTyper`. The dispatch happens in `compile` (line 11): when `Legacy`, the
pipeline matches the existing flow (loaded.typespace already pre-built by the
loader); when `NewTyper`, the loader emits a `Domain` value via the new
typer, and `descriptor.make` is supplied with a thin `Typespace` adapter that
translates `domain.members.get(id)` lookups (and the related field-read shape
exposed by §4's `Domain` case class) into the legacy query API for translators
not yet ported. The flag is wired through `IDLCArgs`
(`idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/IDLCArgs.scala`)
as a global parameter (e.g. `--typer=new` / `--typer=legacy`, default starts as
`legacy` and flips to `new` at IMPL-9).

Lifecycle:
1. **Introduce** (IMPL-6): flag added, default `legacy`, both paths supported,
   the new path is exercised only by harness CI runs.
2. **Both paths supported** (IMPL-7a/b/c): per-translator switch internally
   reads the flag; default still `legacy`.
3. **Harness proves parity** (gated by PR-03 CI): the harness runs the full
   corpus through both flags and asserts byte-equal generated output for every
   surviving back-end.
4. **Flip default** (IMPL-9): default becomes `new`. The legacy path is still
   selectable for one release.
5. **Delete** (IMPL-10 + IMPL-11): legacy path and its types are removed; the
   flag is hardcoded `new` (or removed, if no value remains).

### Atomicity rules

The following groups MUST land as a single PR — splitting them would leave the
build broken or the wire format inconsistent:

- **Go deletion atomic group**: removing `togolang/` (15 files,
  `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/togolang/...`) +
  removing `idealingua-v1-runtime-rpc-go/` (11 files) + removing
  `IDLLanguage.Go` from `IDLLanguage.scala:10-12, 30-31` + removing
  `GoTranslatorDescriptor` from `TypespaceCompilerBaseFacade.scala:30` + removing
  `GoLangBuildManifest.scala`
  (`idealingua-v1/idealingua-v1-core/src/main/scala/izumi/idealingua/model/publishing/manifests/GoLangBuildManifest.scala`) +
  removing `decGo`/`decGoProjectLayout`/`decGoRepositoryOptions`/`encGo`/`encGoProjectLayout`/`encGoRepositoryOptions` from `Codecs.scala:39, 41, 43, 81, 83, 85` + removing
  `case m: GoLangBuildManifest` from `ManifestReader.scala:22-23` (Writer arm)
  and the Reader arm at `:37-38` + removing `GoCredentials`/Go dispatch from
  `CredentialsReader.scala:18, 27` + removing `IDLLanguage.Go -> "0"` from
  `CommandlineIDLCompiler.scala:283` + removing
  `case (c: GoCredentials, IDLLanguage.Go, ...)` from
  `ArtifactPublisher.scala:22` and `publishGo` body at `:169-280` + removing the
  `RoleParserSchema("go", ...)` line in `IDLCArgs.scala:82` + removing the four
  `idealingua-v1-runtime-rpc-go` references in `build.sbt`
  (`build.sbt:919, 1247, 1564, 1741, 1759`). Anything left half-removed breaks
  the build or the CLI parser.
- **Protobuf deletion atomic group**: same shape, for `toprotobuf/` (21 files),
  `IDLLanguage.Protobuf` at `IDLLanguage.scala:22-24, 36-37`, `ProtobufTranslatorDescriptor` at
  `TypespaceCompilerBaseFacade.scala:33`, `ProtobufBuildManifest.scala`,
  `decProtobuf*` / `encProtobuf*` at `Codecs.scala:5, 47, 49, 93, 95`,
  `IDLLanguage.Protobuf` arm at `ManifestReader.scala:41-42` and
  `ProtobufCredentials`/Protobuf dispatch at `CredentialsReader.scala:20-21, 29`
  and `ArtifactPublisher.scala:24, 282-374`. Note: there is no `protobuf` role in
  `IDLCArgs.scala` today (verified at `:80-86`) so the CLI-side deletion is
  manifest-only.
- **Feature-flag introduction atomic group**: add the flag (in
  `IDLCArgs.scala`) + the field on `UntypedCompilerOptions` + the dispatch in
  `TypespaceCompilerBaseFacade.compile` + the adapter that wraps `Domain` as
  `Typespace`. None of these compile without the others.
- **Per-translator port atomic group** (one per back-end): the translator's
  `Translator.translate()` body switching from `ctx.typespace.*` calls to
  `ctx.domain.*` calls + every extension under `extensions/` of that back-end +
  the back-end's `*TContext` removing the `Typespace` field. Splitting these
  causes mid-PR partial breakage.

---

## §3. The new typer — phase-by-phase design

The phases run **per domain** with one prior global phase (Phase 0) that builds
the cross-domain index. The diagnostic effect type is `Either[NEList[Diagnostic],
A]` (per PR-01 Lesson 5 — `IDLException` becomes a "BUG" assertion only).

> Note on package names: every `izumi.idealingua.typer.phase.*` and
> `izumi.idealingua.typer.ir.*` name below is a *starting point*. The
> implementation PRs may refine. The key invariant is "named single-purpose
> phase, declared input IR, declared output IR" (PR-01 Lesson 1).

### Phase 0 — IdealinguaFamilyManager (cross-domain index)

- **Input IR**: `List[ParsedDomain]` (one per `.domain` file, post-parse +
  post-loader). The existing `ModelLoader` (`idealingua-v1/idealingua-v1-core/src/main/scala/izumi/idealingua/model/il/loader/`)
  already produces this; this phase consumes its output.
- **Output IR**: `FamilyIndex` carrying `Map[DomainId, ParsedDomain]` plus a
  cross-domain dependency DAG `Map[DomainId, Set[DomainId]]`.
- **Responsibility**: One-shot cross-domain parse + index. Detects duplicate
  `DomainId` (per cross-cutting decision C11 in the meta-plan
  `:154-167`); detects cyclic imports *before* any per-domain typing, replacing
  the post-typer `CyclicImportsRule` (cited in `TypespaceVerifier.scala:16`).
- **Source-grounded inspiration**: PR-01 §3 BaboonFamilyManager.
- **Replaces in current code**: `IDLPostTyper.getDomain` recursion at
  `IDLTyper.scala:84-89` (the `domainCache` field at `:83` becomes a no-op /
  unused), `IDLPostTyper.lookupAnother` at `IDLTyper.scala:404-407` (rerouted
  through the family index), and `TypespaceImpl.transitivelyReferenced` at
  `TypespaceImpl.scala:14-25` (deleted; the family index already has the
  transitive set).
- **Diagnostics produced**: `DuplicateDomainId`, `CyclicImports`,
  `MissingImportedDomain`.
- **Failure mode**: halts pipeline if the import graph is cyclic or any imported
  domain is missing; otherwise non-fatal, accumulates diagnostics.

### Phase 1 — ScopeBuilder

- **Input IR**: `(DomainId, ParsedDomain, FamilyIndex)`.
- **Output IR**: `ScopedDomain` (a hierarchical scope tree with parent links + a
  flat name index).
- **Responsibility**: Builds the per-domain scope. Cross-domain references are
  resolved against the family index (no per-domain re-typing). The tree
  pre-computes the locally-visible name → `RawTypeDef` map per scope, plus the
  cross-domain `IndefiniteId` → `TypeId` aliases (the mass that
  `IDLPostTyper.imported` and `IDLPostTyper.mapping` build at
  `IDLTyper.scala:92-104`).
- **Source-grounded inspiration**: PR-01 §3 ScopeBuilder.
- **Replaces in current code**: `IDLPostTyper.imported`
  (`IDLTyper.scala:92-97`), `IDLPostTyper.mapping` (`IDLTyper.scala:99-104`),
  `IDLPostTyper.index` (`IDLTyper.scala:106-117`), and the
  `IDLPretyper.perform` import-name-clash check at
  `IDLTyper.scala:56-58`.
- **Diagnostics produced**: `ImportNameClashesWithLocal`, `ScopeCollision`.
- **Failure mode**: non-fatal; collisions and clashes accumulate.

### Phase 2 — NameResolver

- **Input IR**: `ScopedDomain`.
- **Output IR**: `ResolvedDomain` — every `AbstractIndefiniteId` in the raw AST
  is replaced with a definite `TypeId`.
- **Responsibility**: Resolve each occurrence of a name to its definite
  `TypeId`. Generic arity validation. Builtin recognition (the
  `Primitive.mapping` lookup at `IDLTyper.scala:380`). Cross-domain references
  are looked up against the family index — there is no recursive re-typing.
- **Source-grounded inspiration**: PR-01 §3 ScopeSupport + Phase 3 (runTyper) of
  Baboon's per-domain pipeline.
- **Replaces in current code**: `IDLPostTyper.makeDefinite`
  (`IDLTyper.scala:377-391`), `IDLPostTyper.lookupLocal` (`IDLTyper.scala:393-402`),
  `IDLPostTyper.lookupAnother` (`IDLTyper.scala:404-407`),
  `IDLPostTyper.toGeneric` (`IDLTyper.scala:439-458`),
  `IDLPostTyper.contains` (`IDLTyper.scala:460-467`),
  `IDLPostTyper.fixId` (`IDLTyper.scala:297-305`),
  `IDLPostTyper.transformSimpleId` (`IDLTyper.scala:481-504`),
  `IDLPostTyper.fixSimpleId` (`IDLTyper.scala:506-557`).
- **Diagnostics produced**: `UnknownTypeRef`, `WrongGenericArity`,
  `BadIdentifierFieldType`, `ScalarExpected`.
- **Failure mode**: non-fatal; missing references and arity errors accumulate.

### Phase 3 — AliasDealiaser

- **Input IR**: `ResolvedDomain`.
- **Output IR**: `ResolvedDomain` (annotated; aliases resolved to terminal
  targets).
- **Responsibility**: Walks `TypeDef.Alias` chains, computes terminal targets,
  records the resolved-target table on the IR, **detects cyclic-alias chains as
  a diagnostic, not a stack overflow**.
- **Source-grounded inspiration**: PR-01 §3 (last sweep of Baboon's `runTyper`).
- **Replaces in current code**: `TypespaceImpl.dealias`
  (`TypespaceImpl.scala:41-49`); the alias-chasing logic embedded in
  `IDLPostTyper.fixSimpleId` (`IDLTyper.scala:537-549`) where alias
  contraction is interleaved with name resolution.
- **Diagnostics produced**: `CyclicAlias`, `AliasTargetUnresolved`.
- **Failure mode**: non-fatal; cyclic aliases are diagnosed, the alias
  resolution table contains an explicit `Cyclic` marker for offending entries.

### Phase 4 — KindChecker

- **Input IR**: `ResolvedDomain`.
- **Output IR**: `ResolvedDomain`.
- **Responsibility**: Per-defn kind invariants:
  - **Identifier** fields must be primitive | `IdentifierId` | `EnumId`
    (currently enforced by `IDLPostTyper.fixType` at
    `IDLTyper.scala:148-158`, throwing `IDLException` at line 157).
  - **Mixin** references must resolve to a `DTOId` or `InterfaceId`
    (currently `IDLPostTyper.makeDefiniteMixin` at `IDLTyper.scala:315-324`,
    throwing at `:322`).
  - **ADT branch** references must resolve to user types (the typed
    `RawAdt.Member.NestedDefn` arm at `IDLTyper.scala:267-268` currently throws;
    new behaviour: produce a `NestedAdtMemberUnsupported` diagnostic and
    continue).
  - **Interface**: no additional kind-shape invariant beyond what `NameResolver`
    (Phase 2) and the post-assembly `Validator` (Phase 12) already cover.
    Naming is enforced by `BasicNamingConventionsRule` (lifted into
    `SymbolNames`, see §10's `add` row); duplicate-member detection runs in
    Phase 12. Field-target kind constraints are NOT applied to interfaces (only
    Identifiers have the primitive/IdentifierId/EnumId restriction).
  - **DTO**: same as Interface — no additional kind-shape invariant in Phase 4.
    Mixin-target validity (DTOs may include other DTOs and Interfaces as
    structural mixins) is the *MixinTarget* check above; the DTO itself has no
    further kind-shape rule.
  - **Enum**: no additional kind invariant in Phase 4 beyond non-empty members
    (which is structural — an empty `enum {}` cannot be parsed) and unique
    member identifiers. Duplicate-member detection lives in Phase 12's
    Validator (per `DuplicateMemberRule.scala`); naming lives in `SymbolNames`.
    The enum-value uniqueness check therefore does not appear in Phase 4.
- **Source-grounded inspiration**: PR-01 §3 (kind-shape checks subsumed under
  `runTyper`).
- **Replaces in current code**: the throw sites listed above.
- **Diagnostics produced**: `BadIdentifierFieldType`, `BadMixinTarget`,
  `NestedAdtMemberUnsupported`.
- **Failure mode**: non-fatal.

### Phase 5 — CycleDetector

- **Input IR**: `ResolvedDomain`.
- **Output IR**: `ResolvedDomain` annotated with `loops: Set[Cycle[TypeId]]`.
- **Responsibility**: Materialize three cycle classes in the same pass:
  inheritance cycles (currently `CyclicInheritanceRule` at
  `verification/rules/CyclicInheritanceRule.scala`,
  invoked from `TypespaceVerifier.scala:15`); ADT-membership cycles (currently
  computed lazily by `StructuralQueriesImpl.adtRecursiveMembers` at
  `StructuralQueriesImpl.scala:182-193`); structural-usage cycles
  (currently `CyclicUsageRule.scala`, invoked from `TypespaceVerifier.scala:14`).
  Cycles are *retained as facts*; whether each cycle is fatal is decided in the
  Validator phase (PR-01 Lesson 8).
- **Source-grounded inspiration**: PR-01 §2 Phase 10 (`enquiries.loopsOf`) +
  Lesson 8.
- **Replaces in current code**: `CyclicInheritanceRule`, `CyclicUsageRule`,
  `InheritanceQueriesImpl.checkCycles` (`InheritanceQueriesImpl.scala:93-99`),
  the recursive ADT walker at `StructuralQueriesImpl.scala:182-193`.
- **Diagnostics produced**: `CyclicInheritance`, `NonTerminatingCycle`. The
  *retained* loop set is not itself a diagnostic.
- **Failure mode**: non-fatal; only non-terminating cycles surface as
  diagnostics.

### Phase 6 — StructuralFlattener

- **Input IR**: `ResolvedDomain`.
- **Output IR**: `ResolvedDomain` annotated with
  `flattenedStructs: Map[StructureId, FlatStruct]` and
  `parents: Map[TypeId, Set[InterfaceId]]` and
  `implementingDtos: Map[InterfaceId, Set[DTOId]]`.
- **Responsibility**: Pre-compute, once, the flattened struct fields (with
  declaration-order preserved; see §4 ordering invariant), the soft/hard field
  conflict classification, the parent-set per type, and the inverse
  `interface → implementing DTOs` map. **This is the core performance lever**
  (PR-01 Lesson 4).
- **Source-grounded inspiration**: PR-01 §2 Phase 6 (buildDependencies) + PR-01
  Lesson 4.
- **Replaces in current code**: `StructuralQueriesImpl.structure`
  (`StructuralQueriesImpl.scala:14-45`) and its
  `findConflicts` helper (`:47-70`); `FieldExtractor`
  (`idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/FieldExtractor.scala`,
  92 lines); `InheritanceQueriesImpl.allParents` /
  `parentsInherited` / `safeAllParents` / `safeParentsInherited` /
  `safeParentsConcepts` (`InheritanceQueriesImpl.scala:10-91`);
  `InheritanceQueriesImpl.implementingDtos` and `compatibleDtos`
  (`InheritanceQueriesImpl.scala:14-30`).
- **Diagnostics produced**: `HardFieldConflict` (replacing the throw at
  `StructuralQueriesImpl.scala:67`), `MissingMixin`.
- **Failure mode**: non-fatal; conflicts surface as diagnostics, the IR carries
  a marker for the offending struct.

### Phase 7 — EphemeralSynthesizer

- **Input IR**: `ResolvedDomain` (post-flatten).
- **Output IR**: `ResolvedDomain` annotated with
  `ephemerals: Map[TypeId, EphemeralDto]` and the inverse
  `interfaceEphemeralIndex` / `dtoEphemeralIndex`.
- **Responsibility**: Eagerly synthesize the DTOs for service method
  inputs/outputs, buzzer inputs/outputs, and interface→DTO mirror DTOs.
  **These are wire-visible** — their `wireId`
  (`idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/common/TypeId.scala:31`)
  appears in ADT/interface JSON discriminants per the meta-plan §"Wire format"
  (`docs/drafts/20260503-1200-modernization-plan.md:80-105`). The naming rules
  (`methodInputSuffix`, `methodOutputSuffix`, `goodAltSuffix`, `badAltSuffix`,
  `toPositiveBranchName`, `toNegativeBranchName`, `toDtoName`,
  `toInterfaceName`) defined on `TypespaceTools` (`Typespace.scala:63-97`) and
  implemented in `TypespaceToolsImpl`
  (`idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/TypespaceToolsImpl.scala`)
  must be reproduced *byte-identically* (per meta-plan risk #1 and the
  field-ordering invariant in §4). The ephemeral set is therefore part of the
  IR, not a translator-side derivation.
- **Source-grounded inspiration**: no direct Baboon analogue — driven by ide-v1
  specifics. Baboon has no ephemeral DTO mirrors.
- **Replaces in current code**: `TypeCollection.makeServiceEphemerals`
  (`TypeCollection.scala:101-122`), `TypeCollection.outputEphemeral`
  (`:124-160`), `TypeCollection.toOutDef` (`:162-171`),
  `TypeCollection.interfaceEphemeralIndex` (`:62-68`),
  `TypeCollection.dtoEphemeralIndex` (`:74-81`), `TypeCollection.all` (`:87-97`),
  the duplicate-name check at `TypeCollection.verified` (`:183-198`,
  re-emerges as `DuplicateMemberRule` analogue inside the Validator).
- **Diagnostics produced**: `EphemeralNameCollision`.
- **Failure mode**: non-fatal.

### Phase 8 — ConstValueTyper

- **Input IR**: `ResolvedDomain`.
- **Output IR**: `ResolvedDomain` with const blocks fully type-checked.
- **Responsibility**: Type-check `RawVal` const blocks against the resolved
  type table. Currently, `IDLPostTyper.translateValue` (`IDLTyper.scala:216-253`)
  has three `// TODO: verify structure` comments at lines 240, 245, and 250
  (also flagged in meta-plan cross-cutting decision C6,
  `:154-167`). The new typer fully checks `CTypedList`, `CTyped`, `CTypedObject`
  against the materialized struct shape from Phase 6, and propagates type errors
  as diagnostics rather than ignoring them.
- **Source-grounded inspiration**: no direct Baboon analogue — Baboon has
  per-domain-pragma constants but ide-v1's `consts` block is a different
  surface syntax.
- **Replaces in current code**: `IDLPostTyper.translateValue`
  (`IDLTyper.scala:216-253`).
- **Diagnostics produced**: `ConstTypeMismatch`, `ConstFieldMissing`,
  `ConstFieldUnknown`.
- **Failure mode**: non-fatal; const blocks with errors carry an `Unchecked`
  marker.

### Phase 9 — FingerprintCalculator

- **Input IR**: `ResolvedDomain` (post-flatten + post-ephemeral).
- **Output IR**: `Map[TypeId, Fingerprint]` plus a per-domain rollup
  `Fingerprint` (one hash per top-level type, plus one per domain).
- **Responsibility**: Compute a stable per-type fingerprint covering: type kind
  (DTO/Interface/Identifier/Adt/Enum/Alias/Service/Buzzer/Streams), declared
  field name+type pairs **in declaration order**, parent set, ephemeral DTO
  shape, and (for ADTs and Interfaces) the discriminant strings each
  branch/implementor will emit on the wire (the `wireId` formula at
  `TypeId.scala:31`). The fingerprint is the canonical wire-format invariant
  used by PR-03's harness to detect regressions.
- **Source-grounded inspiration**: PR-01 §2 Phase 8 (`shallowId`) + Phase 9
  (`computeDeepSchema`); both modified per PR-01's verdicts.
- **Replaces in current code**: nothing — this is a new capability.
- **Diagnostics produced**: none. The fingerprint is a fact, not a check.
- **Failure mode**: never fails (impossible to fail on a passing IR).

### Phase 10 — RootExtractor

- **Input IR**: `ResolvedDomain` (post-everything).
- **Output IR**: `Set[TypeId]` of "roots" — all top-level user-declared types
  (i.e., *not* synthesized ephemerals).
- **Responsibility**: Identify the set of types the translator should emit as
  top-level files. Per PR-01 §2 Phase 5 verdict, ide-v1 has no `root` keyword;
  every user-declared top-level type is a root, and synthesized ephemerals are
  not.
- **Source-grounded inspiration**: PR-01 §2 Phase 5 + §3 RootExtractor.
- **Replaces in current code**: the implicit "iterate over `domain.types`"
  pattern in `ScalaTranslator.translate`
  (`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/ScalaTranslator.scala:54`).
- **Diagnostics produced**: none.
- **Failure mode**: never fails.

### Phase 11 — Assembler

- **Input IR**: every annotated artifact from Phases 0–10.
- **Output IR**: a frozen `Domain` value (see §4).
- **Responsibility**: Assemble the final `Domain` value. After this phase, the
  IR is immutable — no later code may mutate. The Validator (Phase 12) only
  reads.
- **Source-grounded inspiration**: PR-01 §2 Phase 14.
- **Replaces in current code**: `IDLPostTyper.perform`'s assembly at
  `IDLTyper.scala:119-134`.
- **Diagnostics produced**: none (assembly is mechanical).
- **Failure mode**: never fails on a passing IR; an assembly failure is a
  compiler bug (`IDLException`/`assert`).

### Phase 12 — Validator (post-assembly diagnostics)

- **Input IR**: `Domain` (frozen).
- **Output IR**: `IDLDiagnostics`.
- **Responsibility**: Run all post-typer rules — duplicate members, naming
  conventions, ADT-conflict detection, terminating-cycle classification — over
  the materialized `Domain`. Rules read pre-computed fields (no on-the-fly
  recursion).
- **Source-grounded inspiration**: PR-01 §3 BaboonValidator.
- **Replaces in current code**: `TypespaceVerifier`
  (`verification/TypespaceVerifier.scala:7-22`) + each of the 7 rule files in
  `verification/rules/`.
- **Diagnostics produced**: the same 7 diagnostic categories the legacy
  `TypespaceVerifier` auto-applies (the rules at
  `verification/rules/AdtConflictsRule.scala`, `AdtMembersRule.scala`,
  `BasicNamingConventionsRule.scala`, `CyclicImportsRule.scala`,
  `CyclicInheritanceRule.scala`, `CyclicUsageRule.scala`,
  `DuplicateMemberRule.scala` — wired in `TypespaceVerifier.scala:9-17`), plus
  the typer-time diagnostics already emitted by Phases 1–8.
  `ReservedKeywordRule.scala` (an eighth file in the same directory) is *not*
  in this set: it is constructed per-translator and runs only when a translator
  descriptor opts in (see PR-01 Lesson 9, §10's `keep` row). The Phase 12
  Validator inherits the same opt-in model.
- **Failure mode**: accumulating; the entry caller decides whether to halt.

### Deprecated-but-supported types: Streams

Per cross-cutting decision **C5 / Q1** (see `tasks.md`), `Streams` are not
in production use and are scheduled for removal in a later release. PR-02
does **not** rewrite anything for Streams: the new IR carries
`TypeDef.Streams` and `TypedStream` with the *same shape and semantics* as
the legacy typed AST, every phase passes them through unchanged, and the
existing Scala/TS/C# emitters continue to produce the same generated code.
Concretely:

- Phase 1 (ScopeBuilder), Phase 2 (NameResolver), Phase 4 (KindChecker)
  treat `Streams` exactly like `Service` — same scoping, same name
  resolution, same kind invariants.
- Phase 7 (EphemeralSynthesizer) does **not** synthesize per-method
  ephemeral DTOs for stream methods; the legacy code does not either, so
  no behaviour change.
- Phase 9 (FingerprintCalculator) hashes `Streams` like any other type;
  fingerprints participate in PR-03's harness on equal footing with
  Services.
- The new `Domain.types`/`Domain.userTypes` projections include `Streams`.
- The wire-format spec doc (PR-03 §4) documents `stream:s2c` /
  `stream:c2s` packet kinds (defined at `packets.scala:43-46`) but flags
  Streams as "deprecated; do not introduce new uses".

What "deprecate-but-keep-working" explicitly does **not** mean: no new
fixtures in PR-03 specifically targeting stream encodings beyond what
the existing `streams.domain` test fixture already exercises; no new
language-level features for streams; no schema-level changes. The path to
removal is a separate PR after M2 lands, gated on confirmation that no
production consumer has emerged in the meantime.

`Buzzers` retain full first-class status — the runtime serves
`buzzer:request`/`buzzer:response`/`buzzer:failure` packet kinds, the
typed AST has `Buzzer` and `BuzzerId`, and existing fixtures (e.g.
`buzzers.domain`) cover them. Phase 7 synthesizes ephemeral input/output
DTOs for buzzer methods exactly as for services.

### Phase dependency DAG

```
Phase 0 (Family)         ── parses, builds index ─────────────────┐
                                                                  │
                                                                  ▼
                                                           ┌──────────────┐
                                                           │ Phase 1      │
                                                           │ ScopeBuilder │
                                                           └──────┬───────┘
                                                                  ▼
                                                           ┌──────────────┐
                                                           │ Phase 2      │
                                                           │ NameResolver │
                                                           └──────┬───────┘
                                                                  ▼
                                                           ┌──────────────┐
                                                           │ Phase 3      │
                                                           │ AliasDealias │
                                                           └──────┬───────┘
                                                                  ▼
                                                           ┌──────────────┐
                                                           │ Phase 4      │
                                                           │ KindChecker  │
                                                           └──────┬───────┘
                                                                  ▼
                                          ┌─── Phase 5 ─── CycleDetector ───┐
                                          ▼                                 ▼
                                  Phase 6                                Phase 8
                            StructuralFlattener                      ConstValueTyper
                                          │                                 │
                                          ▼                                 │
                                  Phase 7                                   │
                            EphemeralSynthesizer                            │
                                          │                                 │
                                          └──────────┬──────────────────────┘
                                                     ▼
                                            Phase 9 — Fingerprint
                                                     │
                                                     ▼
                                           Phase 10 — Roots
                                                     │
                                                     ▼
                                           Phase 11 — Assembler
                                                     │
                                                     ▼
                                           Phase 12 — Validator
```

Phases 5 / 6 / 8 can run in parallel after Phase 4 (no data dependency between
them). Phase 7 depends on Phase 6 (ephemerals reference flattened struct
fields). Phase 9 depends on Phases 6 and 7. The implementation may collapse
parallel runs into a single sequential pass for simplicity; the dependency
edges above are the *correctness* edges, not the performance suggestion.

### Performance argument (order-of-magnitude)

Today's pipeline is dominated by recursive per-domain re-typing. Concretely:

- `IDLPostTyper.getDomain` (`IDLTyper.scala:84-89`) creates a fresh
  `IDLPretyper` + `IDLPostTyper` per imported domain reference. Cache lives
  inside one typer instance only (`domainCache` at `:83`); the *outer* call
  recursively invokes `getDomain` from `lookupAnother` (`:404-407`) and
  `fixSimpleId` (`:555`).
- `TypespaceImpl.transitivelyReferenced` (`TypespaceImpl.scala:14-25`) clones a
  fresh `TypespaceImpl` per transitively-referenced domain, each of which
  recomputes `transitivelyReferenced`.
- Translator-time queries (`StructuralQueriesImpl.structure` at
  `StructuralQueriesImpl.scala:14-45`, `FieldExtractor` at the file-level,
  `InheritanceQueriesImpl.implementingDtos` at
  `InheritanceQueriesImpl.scala:14-19`) walk the typed AST per call.

Letting N be the number of domains and M the average number of types per
domain: the current cost is *at least* O(N²·M) (every domain re-types every
other), often worse on densely cross-referencing graphs because of the typespace
clone amplification. The new pipeline does O(N+E) for parse + family-index
build (E = number of import edges), O(M) for each per-domain pass, and O(M·D)
for `StructuralFlattener` (D = average inheritance depth). Translators consume
materialized maps via O(1) lookups instead of O(M) recursive queries. The
expected speedup on 22 domains × order-of-100 types is at least 10×; on larger
private deployments the speedup is potentially much higher because of the
super-linear blow-up. **This is an algorithmic argument, not a measurement.**
The actual numbers go in §8's measurement record.

---

## §4. The new IR

### Pseudo-code (case classes, sealed traits)

The names below are the starting points for `izumi.idealingua.typer.ir.*`.

```scala
final case class Domain(
  id: DomainId,
  meta: DomainMetadata,
  members: Map[TypeId, Member],     // every TypeDef + every ephemeral
  roots: Set[TypeId],                // user-declared top-level types
  ephemeralsOf: Map[TypeId, Set[TypeId]], // service/buzzer → its ephemerals
  ephemeralOwner: Map[TypeId, TypeId],     // ephemeral → owning service/buzzer
  flattenedStructs: Map[StructureId, FlatStruct],
  parents: Map[TypeId, Set[InterfaceId]],
  implementingDtos: Map[InterfaceId, Set[DTOId]],
  loops: Set[Cycle[TypeId]],
  fingerprints: Map[TypeId, Fingerprint],
  domainFingerprint: Fingerprint,
  imports: Map[DomainId, Set[TypeId]], // transitively-imported types
  consts: List[Const],                 // type-checked
  aliases: Map[AliasId, TypeId],       // resolved alias targets
  userTypes: Map[TypeId, TypeDef],     // projection: members.collect { case (id, Member.User(defn)) => id -> defn }
)

sealed trait Member
object Member {
  final case class User(defn: TypeDef) extends Member
  final case class Ephemeral(defn: EphemeralDto) extends Member
  final case class Builtin(prim: Primitive) extends Member
}

sealed trait TypeDef { def id: TypeId; def meta: NodeMeta }
object TypeDef {
  final case class Dto(id: DTOId, struct: Struct, meta: NodeMeta) extends TypeDef
  final case class Interface(id: InterfaceId, struct: Struct, meta: NodeMeta) extends TypeDef
  final case class Identifier(id: IdentifierId, fields: List[IdField], meta: NodeMeta) extends TypeDef
  final case class Adt(id: AdtId, alternatives: List[AdtMember], meta: NodeMeta) extends TypeDef
  final case class Enum(id: EnumId, members: List[EnumMember], meta: NodeMeta) extends TypeDef
  final case class Alias(id: AliasId, target: TypeId, meta: NodeMeta) extends TypeDef
  final case class Service(id: ServiceId, methods: List[Method], meta: NodeMeta) extends TypeDef
  final case class Buzzer(id: BuzzerId, events: List[Method], meta: NodeMeta) extends TypeDef
  final case class Streams(id: StreamsId, streams: List[TypedStream], meta: NodeMeta) extends TypeDef
}

// IMPORTANT: fields is a *List*, not a Set or Map. Declaration order is
// part of the IR contract — see "Field-ordering invariant" below.
final case class Struct(
  fields: List[Field],            // declared on this type, in source order
  removedFields: List[Field],
  superclasses: Super,
)

final case class FlatStruct(
  ownerId: StructureId,
  fields: List[FlatField],          // *flattened* fields, in resolved order
  conflictsHard: List[FieldConflict],
  conflictsSoft: List[FieldConflict],
)

final case class FlatField(
  field: Field,        // from Struct
  origin: TypeId,      // where the field was declared
  distance: Int,       // inheritance distance from owner
)

final case class EphemeralDto(
  id: DTOId,
  origin: EphemeralOrigin,   // MethodInput | MethodOutput | InterfaceMirror
  struct: Struct,
)

final case class Fingerprint(value: ByteVector)  // SHA-256; scodec.bits.ByteVector for cross-build structural equality

final case class Cycle[T](members: List[T], terminating: Boolean)

sealed trait Diagnostic { def position: Position }
final case class Diagnostics(issues: Vector[Diagnostic]) {
  def isEmpty: Boolean = issues.isEmpty
  def ++(other: Diagnostics): Diagnostics = Diagnostics(issues ++ other.issues)
}
```

### Field-ordering invariant

**Every collection inside `Domain` and its members that maps to a JSON object on
the wire MUST preserve declaration order at the IR level.** Concretely:

- `Struct.fields`, `Struct.removedFields`, `FlatStruct.fields` — `List`,
  not `Set`, not `Map`. Declaration order from the raw AST is preserved
  through all phases. Inheritance flattening prepends inherited fields *in
  ancestor declaration order* before local fields, which matches the legacy
  behaviour observed at `StructuralQueriesImpl.scala:41` (the `sortBy` there
  sorts by `(distance, definedBy.toString, -definedWithIndex)` then reverses;
  `definedWithIndex` is the source-declared index inside its owning struct).
- `TypeDef.Adt.alternatives` — `List`. Branch declaration order survives.
- `TypeDef.Enum.members` — `List`. Member declaration order survives.
- `Service.methods`, `Buzzer.events`, `Streams.streams` — `List`. Method
  declaration order survives.
- `Domain.consts` — `List`. Const block declaration order survives.

Why: per meta-plan risk #1 (`docs/drafts/20260503-1200-modernization-plan.md:170`),
Circe's `deriveEncoder`
emits keys in Scala field declaration order; the generated Scala case classes
take their field order from the IR; therefore the IR's field order is part of
the wire format. Any phase that re-sorts (e.g. for hashing in Phase 9) must do
so on a *copy*, not the IR-level list. The Fingerprint hash deliberately
*includes* declaration order so that re-ordering surfaces as a fingerprint
diff in the PR-03 harness.

Maps for indexing (e.g. `Domain.members: Map[TypeId, Member]`) are unordered by
definition; all *ordered* data lives in `List`s. Where a map's iteration order
matters (e.g. for diagnostic stability), it is `ListMap` or sourced from a
deterministic key sort.

### IR-vs-translator boundary: direct consumption (locked)

Translators consume the IR directly: `Domain` is a value with named fields, not
a query interface. `flattenedStructs.apply(id)` is an O(1) `Map` lookup, not
a method call that triggers recursion. There is no `Typespace`-style
`structure(id: StructureId): Struct` interface in the new path.

Justification: PR-01 Lessons 4 and 6. The whole performance win comes from
materializing facts at typing time so translators read instead of compute. A
query API would let lazy patterns creep back in.

A *thin* `Typespace` adapter is added during IMPL-6 (per §2 migration) so that
translators not yet ported still compile; the adapter is removed in IMPL-11.

### Frozen-after-assembly invariant

After `Phase 11 — Assembler` returns a `Domain`, the value is immutable. No
phase, validator, or translator may mutate. Concretely: every collection inside
`Domain` is `List`/`Set`/`Map` from `scala.collection.immutable`. The Validator
returns `Diagnostics` separately rather than mutating a field on `Domain`.
Fingerprint computation is read-only. Translators never store back. Violations
are caught by code review (no `mutable.*` imports in the IR package) plus a
unit assertion in the Assembler that rejects any `mutable.*` instance reachable
from `Domain` at construction time.

`Fingerprint` wraps `scodec.bits.ByteVector` rather than `Array[Byte]` because
`Array[Byte]` in Scala uses reference equality (`a1 == a2` returns `false`
for two arrays with identical content); `ByteVector` provides structural
equality, immutability, and works on both Scala 2.13 and Scala 3, so
`Map[TypeId, Fingerprint]` lookups, case-class `equals`, and harness
comparisons behave as written. (Earlier drafts proposed Scala 3's `IArray`,
but cross-cutting decision C3 keeps the cross-build, so `IArray` is off the
table — see `tasks.md` cross-cutting note **C3 / Q8**.) If `scodec` is
undesirable as a transitive dependency, the equivalent is a hand-written
`final case class Fingerprint(value: Array[Byte])` with explicit
`equals`/`hashCode` overrides; behaviour is identical.

---

## §5. Backend deletion plan

This section is exhaustive. Each entry: full path + one-line rationale.

### Go deletion list (15 + 11 + glue = 26+ files)

Translator side (15 files under
`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/togolang/`):

| Path | Why safe to delete |
|---|---|
| `togolang/products/CogenProduct.scala` | Go-only cogen products; no cross-language consumers. |
| `togolang/products/RenderableCogenProduct.scala` | Go-only; ditto. |
| `togolang/GoLangTranslationTools.scala` | Go-only translator helpers. |
| `togolang/types/GoLangImports.scala` | Go-only import-tracking type. |
| `togolang/types/GoLangField.scala` | Go-only field-emit type. |
| `togolang/types/GoLangImportRecord.scala` | Go-only. |
| `togolang/types/GoLangStruct.scala` | Go-only struct-emit type. |
| `togolang/types/GoLangType.scala` | Go-only type-emit dispatch. |
| `togolang/GLTContext.scala` | Go-only translator context. |
| `togolang/extensions/GoLangTranslatorExtension.scala` | Go-only extension trait. |
| `togolang/extensions/GoLangTranslatorExtensions.scala` | Go-only extensions container. |
| `togolang/tools/ModuleTools.scala` | Go-only path/module helpers. |
| `togolang/GoLayouter.scala` | Go-only filesystem layout. |
| `togolang/GoLangTranslator.scala` | Go translator entry point — primary kill site. |
| `togolang/GoTranslatorDescriptor.scala` | Registers Go in `TypespaceCompilerBaseFacade.descriptors`. |

Runtime module (11 files under `idealingua-v1/idealingua-v1-runtime-rpc-go/`):

| Path | Why safe to delete |
|---|---|
| `idealingua-v1-runtime-rpc-go/src/main/resources/runtime/go/irt/logger.go` | Go-only runtime resource. |
| `…/transport_http_client.go` | Go-only. |
| `…/auth.go` | Go-only. |
| `…/transport_websocket_server.go` | Go-only. |
| `…/transport_websocket_client.go` | Go-only. |
| `…/transport_http_server.go` | Go-only. |
| `…/transport_websocket.go` | Go-only. |
| `…/dispatcher.go` | Go-only. |
| `…/marshaller.go` | Go-only. |
| `…/formatter.go` | Go-only. |
| `…/transport.go` | Go-only. |
| (and the directory itself) | After files removed, empty parent dirs are deletable. |

Manifest (1 file):

| Path | Why safe to delete |
|---|---|
| `idealingua-v1/idealingua-v1-core/src/main/scala/izumi/idealingua/model/publishing/manifests/GoLangBuildManifest.scala` | Manifest type used only by Go publishing. |

Glue (modify or remove specific lines):

| File | Lines/symbols | Why |
|---|---|---|
| `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/IDLLanguage.scala` | `:10-12` (`case object Go`) and `:30-31` (`Go.toString` arm) | Removes the `Go` case from the language ADT. |
| `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TypespaceCompilerBaseFacade.scala` | `:5` (import) and `:30` (`GoTranslatorDescriptor` registration) | Drops the descriptor. |
| `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/CompilerOptions.scala` | `:5` (`GoLangBuildManifest` import), `:7` (extension import), `:57` (`GoTranslatorOptions` type alias) | Drops Go-typed options. |
| `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/Codecs.scala` | `:39` (`decGo`), `:41` (`decGoProjectLayout`), `:43` (`decGoRepositoryOptions`), `:81` (`encGo`), `:83` (`encGoProjectLayout`), `:85` (`encGoRepositoryOptions`) | Drops Go manifest codecs (the four `*GoProjectLayout`/`*GoRepositoryOptions` defs reference types living inside `GoLangBuildManifest.scala`, so they must be deleted in the same atomic group). |
| `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/ManifestReader.scala` | `:22-23` (Writer arm: `case m: GoLangBuildManifest`) and `:37-38` (Reader arm: `case IDLLanguage.Go => readManifest(GoLangBuildManifest.example)`) | Drops Go manifest reader and writer. |
| `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CredentialsReader.scala` | `:18` (`case class GoCredentials …`) and `:27` (`case IDLLanguage.Go => read[GoCredentials]…`) | Drops Go credentials type and dispatch arm. |
| `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/ArtifactPublisher.scala` | `:5` (import), `:22` (Go publish dispatch), `:169-280` (`publishGo` body) | Drops Go publishing pipeline. |
| `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/IDLCArgs.scala` | `:82` (`RoleParserSchema("go", ...)`) | Removes the `go` CLI role. |
| `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CommandlineIDLCompiler.scala` | `:283` (`IDLLanguage.Go -> "0"` in `VersionOverlay.example`) | Removes the Go default-version map entry. |
| `build.sbt` | `:919` (test->compile dep), `:1247` (project def block), `:1564` (dep), `:1741` (aggregator), `:1759` (aggregator) | Removes the runtime-rpc-go module from sbt graph. |
<!-- IDLTestTools.scala is intentionally NOT listed here: a token-search of the file (60 lines) returned no `Go`/`golang`/`protobuf`/`togolang` references. Listing it would imply spurious cleanup work. -->

### Protobuf deletion list (21 + glue files)

Translator side (21 files under
`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toprotobuf/`):

| Path | Why safe to delete |
|---|---|
| `toprotobuf/extensions/ProtobufTranslatorExtensions.scala` | Protobuf-only extensions container. |
| `toprotobuf/extensions/ProtobufTranslatorExtension.scala` | Protobuf-only extension trait. |
| `toprotobuf/EnumRenderer.scala` | Protobuf enum cogen. |
| `toprotobuf/PBTContext.scala` | Protobuf-only context. |
| `toprotobuf/InterfaceRenderer.scala` | Protobuf interface cogen. |
| `toprotobuf/products/CogenProducts.scala` | Protobuf cogen products. |
| `toprotobuf/products/RenderableCogenProduct.scala` | Protobuf cogen products. |
| `toprotobuf/CompositeRenderer.scala` | Protobuf composite cogen. |
| `toprotobuf/layout/ProtobufLayouter.scala` | Protobuf-only layout. |
| `toprotobuf/ServiceRenderer.scala` | Protobuf-only. |
| `toprotobuf/AdtRenderer.scala` | Protobuf-only. |
| `toprotobuf/tools/ModuleTools.scala` | Protobuf-only helpers. |
| `toprotobuf/ProtobufTranslatorDescriptor.scala` | Registers Protobuf in `TypespaceCompilerBaseFacade.descriptors`. |
| `toprotobuf/ProtobufTranslator.scala` | Protobuf entry point. |
| `toprotobuf/types/ProtobufType.scala` | Protobuf type model. |
| `toprotobuf/types/ProtobufTypeConverter.scala` | Protobuf type model. |
| `toprotobuf/types/ProtobufMethod.scala` | Protobuf type model. |
| `toprotobuf/types/ProtobufField.scala` | Protobuf type model. |
| `toprotobuf/types/ProtobufAdtMember.scala` | Protobuf type model. |
| `toprotobuf/IdentifierRenderer.scala` | Protobuf-only. |
| `toprotobuf/AliasRenderer.scala` | Protobuf-only. |

Manifest (1 file):

| Path | Why safe to delete |
|---|---|
| `idealingua-v1/idealingua-v1-core/src/main/scala/izumi/idealingua/model/publishing/manifests/ProtobufBuildManifest.scala` | Manifest type used only by Protobuf publishing. |

Glue (modify or remove specific lines):

| File | Lines/symbols | Why |
|---|---|---|
| `IDLLanguage.scala` | `:22-24` (`case object Protobuf`) and `:36-37` (`Protobuf.toString` arm) | Removes Protobuf from the language ADT. |
| `TypespaceCompilerBaseFacade.scala` | `:6` (import) and `:33` (`ProtobufTranslatorDescriptor` registration) | Drops the descriptor. |
| `CompilerOptions.scala` | `:5` (manifest import), `:8` (extension import), `:60` (`ProtobufTranslatorOptions` alias) | Drops Protobuf options. |
| `Codecs.scala` | `:5` (import `ProtobufRepositoryOptions`), `:47` (`decProtobufRepo`), `:49` (`decProtobuf`), `:93` (`encProtobufRepo`), `:95` (`encProtobuf`) | Drops Protobuf codecs. |
| `ManifestReader.scala` | `:41-42` (Protobuf reader arm) | Drops the reader. |
| `CredentialsReader.scala` | `:20-21` (`case class ProtobufCredentials …`) and `:29` (`case IDLLanguage.Protobuf => read[ProtobufCredentials]…`) | Drops Protobuf credentials type and dispatch arm. |
| `ArtifactPublisher.scala` | `:5` (import), `:24` (Protobuf dispatch), `:282-374` (`publishProtobuf` body) | Drops the publisher. |
| `IDLCArgs.scala` | (no `protobuf` role exists today; verified at `:80-86`) | No CLI line to delete. |

### Deprecate-before-delete fallback

If, contra meta-plan risk #7 (`docs/drafts/20260503-1200-modernization-plan.md:170-178`),
some external consumer still
depends on the Go or Protobuf back-ends, the fallback path is:

1. **Move (single PR, no behavioural change)**: pull the `togolang/`,
   `toprotobuf/`, `idealingua-v1-runtime-rpc-go/` trees into a new sbt module
   `idealingua-v1-legacy-backends/` (separate `lazy val` in `build.sbt`),
   marked `@deprecated("idealingua-v1-go and idealingua-v1-protobuf are
   unmaintained; migrate to a supported back-end")` at every public entry
   point: `GoTranslatorDescriptor`, `ProtobufTranslatorDescriptor`,
   `GoLangBuildManifest`, `ProtobufBuildManifest`. The default ide-v1 compiler
   distribution stops depending on this module; users who want Go/Protobuf
   add it explicitly. Wire-format-affecting changes are *not* applied to the
   legacy module — it's frozen.
2. **Delete (one release later)**: when no consumers remain (verified by
   internal poll or telemetry), remove the legacy module entirely. This is
   functionally identical to IMPL-8 above, just deferred.

The default plan is direct deletion (IMPL-8). The fallback exists to record
that deletion without a deprecation cycle would be a breaking change if the
user's "no consumers" assertion turns out to be wrong.

---

## §6. Backend restructure for Scala / TS / C#

### Scala (the wire-format reference; load-bearing)

**Where the translator currently does type math:**

- `ScalaTranslator.translate`
  (`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/ScalaTranslator.scala:32`)
  iterates `ctx.typespace.domain.types` (line 54), calls
  `ctx.modules.toModuleId` per def, and dispatches on `TypeDef`.
- `CirceTranslatorExtensionBase.handleAdt`
  (`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/extensions/CirceTranslatorExtensionBase.scala:41-97`)
  consumes `adt.alternatives` directly (line 43) and emits `Map(c.wireId -> v.value).asJsonObject`
  per branch (line 51). The discriminant strings come from the typed AST's
  `wireId` (`TypeId.scala:31`). No call into `Typespace`.
- `CirceTranslatorExtensionBase.handleInterface` (`:106-158`) calls
  `ctx.typespace.inheritance.implementingDtos(interface.id)` (line 110) to find
  the discriminant set. **This is the load-bearing call site.**
- `CirceTranslatorExtensionBase.withDerivedClass` (`:184-`) calls
  `ctx.typespace.structure.structure(id)` (line 187) to obtain the flattened
  struct; reads `struct.all.head.field` for the unwrap case (line 206).
- `ScalaTranslator.translateDef` (`:81-98`) calls
  `ctx.tools.mkStructure(d.id)` (line 92) for DTOs, where `tools` ultimately
  reaches into the Typespace queries.

**What it stops doing under the new IR:** every "look up structural fact"
reduces to a field read on `Domain`. `inheritance.implementingDtos(id)` becomes
`domain.implementingDtos(id)`. `structure.structure(id)` becomes
`domain.flattenedStructs(id)`. The `tools.mkStructure(d.id)` indirection
collapses to direct field access. Ephemeral DTOs are looked up via
`domain.ephemeralsOf(serviceId)` and `domain.members(ephemeralId)`. The
`adt.alternatives` list is read straight off the `TypeDef.Adt` value (no
change there — the IR-level list already has the right shape).

**What it keeps doing:** the actual Scala-meta quasiquote emission lives in
the per-renderer files under `…/toscala/` (e.g.
`InterfaceRenderer.scala:46-50` emits `q"""trait ${t.typeName} extends
..$ifDecls { ..$decls }"""` inside `mkTrait`); these renderers are unchanged
by the new typer. The Circe extension's
choice of envelope (`Map(c.wireId -> v.value).asJsonObject` at `:51`,
`{"type_name": {...fields}}`) is preserved verbatim. Field encoder ordering
follows from Scala field declaration order (Circe behaviour), which follows
from the IR field order (preserved per §4 invariant). The unwrap path for
`Output.Singular` (line 191-196) is preserved.

**Specific renames or removals:**

- `ctx.typespace` field on `STContext` is renamed to `ctx.domain` (typed
  `Domain`, not `Typespace`). All call sites switch.
- `ctx.typespace.inheritance.implementingDtos(id)` → `ctx.domain.implementingDtos(id)`
  (a `Map` lookup; no `withDerivedClass` invocation needed against
  `inheritance.implementingDtos`, see PR-01 Lesson 4).
- `ctx.typespace.structure.structure(id)` → `ctx.domain.flattenedStructs(id)`.
- `ctx.typespace.tools.mkStructure(d.id)` → `ctx.domain.flattenedStructs(d.id)`
  (the legacy `mkStructure` is a thin wrapper that returns the same
  `FlatStruct`-equivalent).
- `ctx.typespace.types.interfaceEphemeralIndex` and `dtoEphemeralIndex` →
  `ctx.domain.ephemeralsOf` / `ctx.domain.ephemeralOwner`.
- `ctx.typespace.dealias` (the recursive walk at `TypespaceImpl.scala:41-49`) →
  `ctx.domain.aliases.getOrElse(t, t)` for `AliasId`, `t` otherwise. The
  current `ctx.typespace(a)` site at
  `CirceTranslatorExtensionBase.scala:214` is *not* dealiasing — it is a
  `TypeDef` lookup; under the new IR it becomes `ctx.domain.userTypes(a)`,
  which returns `TypeDef` directly and preserves the existing
  `case TypeDef.Alias(_, target, _)` match at `:215`.

**Wire-format invariants this back-end must preserve:**

1. **Envelope and discriminants**: ADT JSON has the form
   `{"<wireId>": <branch-fields>}`. `wireId = "${path.toPackage.mkString(".")}.${name}"`
   (`TypeId.scala:31`). Per cross-cutting decision C4
   (`docs/drafts/20260503-1200-modernization-plan.md:154-167`),
   no type may be moved into a different package; the new typer must place
   ephemerals under the same `path` they have today, computed from the same
   suffix rules (`methodInputSuffix`, etc., `Typespace.scala:64-97`).
2. **Interface envelope**: same `{"<wireId>": <fields>}` shape, keyed on the
   *implementing DTO's* wireId (line 114 of `CirceTranslatorExtensionBase`).
3. **`wireId` formula**: forever locked to
   `path.toPackage.mkString(".") + "." + name`. The new typer's
   `EphemeralSynthesizer` (Phase 7) places ephemerals under the same `TypePath`
   as today, so wireIds round-trip.
4. **DTO/Identifier field ordering**: Circe `deriveEncoder` emits keys in Scala
   field declaration order; Scala field order comes from `Struct.fields`
   ordering; `Struct.fields` order comes from the IR. Per §4 invariant.
5. **`idNameFix` rule**: unnamed identifier fields with `fieldsCount == 1`
   become `"value"`; otherwise `<typeName>.uncapitalize` minus a leading `#`
   if present. Per `IDLPostTyper.idNameFix` (`IDLTyper.scala:199-214`). The
   new IR's Identifier construction in Phase 1 (or the equivalent) applies the
   same rule, so generated Scala code matches.
6. **Ephemeral naming**: every existing `methodInputSuffix`,
   `methodOutputSuffix`, `goodAltSuffix`, `badAltSuffix`,
   `toPositiveBranchName`, `toNegativeBranchName`, `toDtoName`,
   `toInterfaceName` stays byte-identical. These are consumed in Phase 7 and
   passed through unchanged.
7. **Singular-output unwrap**: when a method's `Output` is `Singular`, the
   single-field encoding bypasses the wrapper object (line 191 onward). The
   new IR's ephemeral for `Singular` outputs continues to carry a single
   `Field(typeId, "value", ...)` so the encoder branches the same way.
8. **`RpcPacket` envelope** (out of scope for the translator, but affects
   integration): the runtime envelope stays as `RpcPacket(kind, data, id, ref,
   service, method, headers)` per
   `idealingua-v1/idealingua-v1-runtime-rpc-scala/src/main/scala/izumi/idealingua/runtime/rpc/packets.scala`.
   No changes from the new typer reach the runtime.

### TypeScript (shorter restatement)

**Where it currently does type math:** the TS translator under
`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/totypescript/`
is structured the same way as the Scala translator: a `*Translator.scala`
driver, a `TSTContext` per-domain context that holds a `Typespace`, and an
`extensions/` folder. Each translator-side call into `Typespace` is the
analogue of the Scala calls cited above. The deepest type-math is in the
`extensions/` subdir where JSON codec emission is computed; these compute
discriminants and field lists by walking the `Typespace`-exposed structure.

**What it stops doing:** identical to Scala — replaces every `Typespace`
query with a `Domain` field read.

**What it keeps doing:** TS-specific idiom emission (string templates instead
of quasiquotes), `tsconfig` / `package.json` layout, npm-publish manifest
handling.

**Specific renames or removals:** `TSTContext.typespace` → `TSTContext.domain`;
all `inheritance.implementingDtos` and `structure.structure` calls to direct
field reads; ephemeral lookup via `domain.ephemeralsOf` / `domain.ephemeralOwner`.

**Wire-format invariants this back-end must preserve:**

1. ADT envelope: same `{"<wireId>": <fields>}` shape as Scala. The TS encoder
   must emit byte-identical JSON for every test-corpus type (validated by
   PR-03's cross-language harness).
2. Interface envelope: same shape as Scala, keyed on implementing DTO's
   `wireId`.
3. `wireId` formula: same as Scala.
4. DTO field ordering: TS encoders must emit fields in IR declaration order to
   round-trip with Scala's Circe output. The TS extension that emits
   per-field assignments is the load-bearing site; it must iterate
   `flatStruct.fields` in order.
5. `idNameFix` rule: same as Scala.
6. Ephemeral naming: same as Scala (byte-identical suffixes / branch names).

### C# (shorter restatement)

**Where it currently does type math:** the C# translator under
`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/tocsharp/`,
mirror of Scala. The `extensions/JsonNetExtension.scala` is the JSON-codec
author for C#; it walks the `Typespace`-exposed structure to emit
JsonNet-equivalent codecs for ADT/interface envelopes.

**What it stops doing:** identical to Scala — replaces every `Typespace`
query with a `Domain` field read.

**What it keeps doing:** C#-specific idiom emission, JsonNet annotations,
`.csproj` layout.

**Specific renames or removals:** `CSContext.typespace` → `CSContext.domain`;
the `JsonNetExtension` reads `domain.implementingDtos(id)` / `domain.flattenedStructs(id)` directly.

**Wire-format invariants this back-end must preserve:**

1. ADT envelope: byte-equal to Scala.
2. Interface envelope: byte-equal to Scala.
3. `wireId` formula: byte-equal.
4. DTO field ordering: must follow IR order. JsonNet's default behaviour is
   declaration-order; the C# emission must declare fields in IR order to
   preserve this.
5. `idNameFix` rule: byte-equal.
6. Ephemeral naming: byte-equal.

---

## §7. CLI and packaging

### `CommandlineIDLCompiler` and `IDLCArgs.scala` flag changes

**Surviving roles** (reading `IDLCArgs.scala:80-86`):

- `init` — project-template setup.
- `scala` — Scala target. Survives.
- `csharp` — C#/Unity target. Survives.
- `typescript` — TypeScript target. Survives.

**Removed roles**:

- `go` (line 82) — removed in the Go deletion atomic group.

There is no Protobuf role today (verified above), so no Protobuf-side flag
removal is needed.

**New global parameter** (introduced in IMPL-6, default flips in IMPL-9):

- `--typer=<legacy|new>` — selects which typer pipeline runs. Defaults to
  `legacy` until IMPL-9, then `new`. Removed in IMPL-11.

### Deprecation cycle (per cross-cutting decision C9 in meta-plan)

Per `docs/drafts/20260503-1200-modernization-plan.md:154-167` decision C9, any
flag added/removed/renamed gets a deprecation note and one release of overlap.
Concretely:

1. **Release N (= release just before IMPL-8 lands)**: print a deprecation
   warning when the `go` role is invoked (still functional). No schema change.
2. **Release N+1 (containing IMPL-8)**: `go` role is removed. CLI parser
   rejects with a clear error message ("the `go` target was removed in this
   release; use idealingua-v1 < N to generate Go code"). Fail-fast, not
   silently accepted.
3. **Release N (containing IMPL-6)**: `--typer=new` is documented as
   experimental.
4. **Release N+M (containing IMPL-9)**: `--typer` default flips; old default
   is documented as legacy and still selectable.
5. **Release N+M+1 (containing IMPL-10/IMPL-11)**: `--typer` flag is removed
   (or hardcoded `new`); legacy types deleted.

### `ArtifactPublisher` — what changes downstream

`ArtifactPublisher.scala:22-24` dispatches per-language. The
`(c: GoCredentials, IDLLanguage.Go, m: GoLangBuildManifest)` arm at line 22 and
`publishGo` body at `:169-280` are removed in the Go deletion group; the
`(c: ProtobufCredentials, IDLLanguage.Protobuf, m: ProtobufBuildManifest)` arm
at line 24 and `publishProtobuf` body at `:282-374` are removed in the Protobuf
deletion group. No new publisher entries are added by PR-02 — Scala / TS / C#
publishing flow is unchanged.

The `idealingua-v1-runtime-rpc-go` build-product (the Go binary published from
this module) ceases to exist; downstream consumers that pulled it from the
artifact store will see a 404 and must pin to the last release that contained
it.

---

## §8. Performance measurement plan

### Baseline invocation

The baseline target is end-to-end compile time of the test-defs corpus through
the legacy pipeline.

Command (run on the user's `vm` host, from the repo root, with `nproc`-aware
sbt):

```
sbt -batch \
  -Dsbt.semanticdb=false \
  'set ThisBuild / Test / parallelExecution := false' \
  'idealingua-v1-compiler/runMain izumi.idealingua.compiler.CommandlineIDLCompiler \
     --root idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests \
     --src source --target /tmp/idl-bench-out \
     scala --no-runtime --no-zip --target /tmp/idl-bench-out/scala'
```

Variants:
- One **JVM warmup pass** (run the same command, discard timing) before any
  measured run, so HotSpot has compiled the hot loop.
- Three **measured passes** of each variant, report `min`, `median`, `mean`
  wall-clock time.
- `time -v` (or `/usr/bin/time -v`) is captured for each measured pass; the
  `Elapsed (wall clock) time` field is the headline number; `Maximum resident
  set size` is reported alongside.

### New-typer invocation

Same command with `--typer=new` added to the global parameters (per §7). All
other flags identical. Same warmup + 3-pass discipline. Same `time -v` capture.

Single language is enough for the headline number because the typer is
language-agnostic; running across all three confirms no per-language hot path
masks the win.

### Acceptance threshold (proposed)

- **Primary**: ≥10× wall-clock speedup on the median full-corpus compile, on
  the user's `vm` host. The 10× number is the meta-plan's tentative target
  (`docs/drafts/20260503-1200-modernization-plan.md:122-135`) and matches the
  algorithmic argument in §3 (super-linear → linear).
- **Secondary** (absolute, also required): the new pipeline completes the full
  22-domain corpus in ≤500ms on the same host. Justification: a small corpus
  on a modern x86 VM with the JIT warm should be I/O-bound, not CPU-bound;
  500ms is roughly the file-IO budget for 22 files plus a small fixed compile
  cost. If the new typer can't beat 500ms, something is still O(N²).
- If only one threshold is met, IMPL-9 (flag flip) is held until both are met
  *or* the user explicitly accepts the shortfall in writing.

### Where to record the baseline

A new fixture file at
`idealingua-v1/idealingua-v1-test-defs/src/main/resources/perf-baseline.json`,
checked in. Schema:

```json
{
  "host": "user-vm-2026-05-03",
  "scala": "2.13.16",
  "java": "<vendor>-<version>",
  "git_sha": "<sha>",
  "legacy": {
    "wall_clock_ms_min": ...,
    "wall_clock_ms_median": ...,
    "wall_clock_ms_mean": ...,
    "rss_kib": ...
  },
  "new": {...},
  "speedup_median": ...
}
```

The harness PR (PR-03) re-runs this measurement on every release and gates the
release on no regression > 20% from the recorded baseline.

---

## §9. Cross-cutting decisions still open

This section restates each open decision from meta-plan §"Cross-cutting
decisions" (`docs/drafts/20260503-1200-modernization-plan.md:154-167`, items
C1–C11) that affects PR-02, gives PR-02's recommendation, and identifies the
blocker. The user reads this to decide. C12 (added by the orchestrator after
the meta-plan was published) is covered first to make the decision visible.

- **C12 — Field-ordering invariant.** Already locked in `tasks.md`
  cross-cutting notes. The new IR preserves struct field declaration order
  (`List`, not `Set`/`Map`) because Circe `deriveEncoder` emits keys in
  declaration order. See §4 IR for the structural enforcement (the
  "Field-ordering invariant" subsection) and R1 in §11 for the Assembler
  sanity check that defends it. *Recommendation*: locked. *Blocker*: none.

- **C1 — Build alongside vs. in-place.** *Recommendation*: alongside, per §2.
  *Blocker*: none — recommendation is locked in this document, but the user
  may still override.

- **C2 — Which back-ends survive.** *Recommendation*: keep Scala, TypeScript,
  C#; delete Go and Protobuf (translator + manifests + the Go runtime
  module). *Blocker*: meta-plan risk #7 — the user is asserting "no Go/Protobuf
  consumers"; if wrong, the §5 fallback path applies.

- **C3 — Scala version target.** *Recommendation*: drop 2.12 and 2.13 in the
  *compiler* module (`idealingua-v1-model`, `idealingua-v1-core`,
  `idealingua-v1-transpilers`, `idealingua-v1-compiler`) and target Scala
  3.3.6 only there. Keep cross-build for the *runtime* modules
  (`idealingua-v1-runtime-rpc-scala*`, `idealingua-v1-runtime-rpc-http4s`)
  because consumers depend on them (`build.sbt:173-185, 348-359`). *Blocker*:
  user confirmation. The new typer is much cleaner in Scala 3 (sealed-trait
  exhaustivity, opaque types for `TypeId` newtypes), and the compiler is the
  module that benefits most.

- **C4 — ADT discriminant scheme.** *Recommendation*: lock the `wireId =
  path.toPackage.mkString(".") + "." + name` formula
  (`TypeId.scala:31`) forever. Forbid moving any existing user type into a
  different package; the typer's PR-03 harness asserts this. *Blocker*: user
  acknowledgment that this is a forever invariant.

- **C5 — Buzzers and Streams.** **RESOLVED 2026-05-03**: Buzzers stay
  first-class in the new IR (Phase 7 synthesizes ephemeral input/output DTOs
  for buzzer methods on par with services). Streams are deprecated-but-kept-
  working: the IR carries `TypeDef.Streams` with unchanged shape, every
  phase is a pass-through, no new fixtures or language features, removal
  scheduled for a post-M2 release. See "Deprecated-but-supported types:
  Streams" in §3 above and `tasks.md` cross-cutting note **C5 / Q1**.

- **C6 — Constants (`RawVal` / `ConstValue`).** *Recommendation*: Phase 8
  type-checks consts fully (replacing the three `// TODO: verify structure`
  comments at `IDLTyper.scala:240, 245, 250`). *Blocker*: user confirmation
  that consts are in scope for PR-02's IMPL- sequence (vs. deferred to a
  later modernization).

- **C7 — Newtypes and ForeignType.** **RESOLVED 2026-05-03**: `ForeignType`
  is removed from the grammar; existing models that use it stop compiling
  with a hard typer diagnostic. Verified 2026-05-03 that no `.domain`
  fixture under `idealingua-v1-test-defs/` mentions `foreign` (zero matches
  in a case-insensitive grep). The grammar surface to remove: keyword
  `foreign` (`Keywords.scala:25`), parser entry `defStructure.foreignBlock`
  (`DefStructure.scala:131-136` and the `DefMember.scala:27` alternative),
  raw AST nodes `RawTypeDef.ForeignType` (`RawTypeDef.scala:32`) and
  `RawTopLevelDefn.TLDForeignType` (`RawTopLevelDefn.scala:24`). NewType
  support is finished for all type kinds (currently DTO/Interface only at
  `IDLTyper.scala:174-189`); moved to a dedicated `NewTypeExpander` raw-AST
  desugaring pass per PR-01 Lesson 7.

- **C8 — Diagnostics model.** *Recommendation*: every typer phase returns
  `Either[NEList[Diagnostic], A]` (or the `Diagnostics` accumulator from §4).
  `IDLException` is reduced to "BUG" assertions only (PR-01 Lesson 5).
  *Blocker*: none — this is intrinsic to the new typer.

- **C9 — CLI compatibility.** *Recommendation*: per §7, every flag
  added/removed gets a deprecation cycle. No additional question for the user.
  *Blocker*: none.

- **C10 — Compiler-self test corpus.** *Recommendation*: the harness lives in a
  new module `idealingua-v1-test-harness`, separate from `idealingua-v1-compiler/src/test/scala/`,
  to keep TS/dotnet test-time deps out of the compiler module.
  PR-03 owns this; PR-02 only signals the layout choice. *Blocker*: PR-03.

- **C11 — Cross-domain reference graph cycles.** *Recommendation*: cyclic
  imports are detected in Phase 0 (Family build), *before* any per-domain
  typing, replacing the post-typer `CyclicImportsRule` invocation at
  `TypespaceVerifier.scala:16`. *Blocker*: none.

---

## §10. File-level diff plan

This is the implementation map. Each row is a single file (or a small, named
file group). Every entry is grounded in a citation already used above; every
deletion is enumerated; every modification names the entry-point.

| Action | Path | Notes |
|---|---|---|
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/Domain.scala` | The frozen IR type from §4. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/TypeDef.scala` | IR `TypeDef` family. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/Struct.scala` | `Struct`, `FlatStruct`, `Field`, `FlatField`, `Super`. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/Ephemeral.scala` | `EphemeralDto`, `EphemeralOrigin`. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/Fingerprint.scala` | `Fingerprint`, hash representation. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/Cycle.scala` | `Cycle[T]`. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/Diagnostics.scala` | `Diagnostic`, `Diagnostics` accumulator. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/family/FamilyIndex.scala` | `FamilyIndex`. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/family/IdealinguaFamilyManager.scala` | Phase 0. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/ScopeBuilder.scala` | Phase 1. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/NameResolver.scala` | Phase 2. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/AliasDealiaser.scala` | Phase 3. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/KindChecker.scala` | Phase 4. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/CycleDetector.scala` | Phase 5. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/StructuralFlattener.scala` | Phase 6. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/EphemeralSynthesizer.scala` | Phase 7. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/ConstValueTyper.scala` | Phase 8. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/FingerprintCalculator.scala` | Phase 9. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/RootExtractor.scala` | Phase 10. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/Assembler.scala` | Phase 11. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/Validator.scala` | Phase 12. |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/NewTypeExpander.scala` | Per PR-01 Lesson 7 (replaces `IDLTyper.scala:174-189`). |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/util/SymbolNames.scala` | Per PR-01 Lesson 9 (universal name-shape utility lifted into typer). |
| add | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/typer/util/TypeInfo.scala` | Per PR-01 §3 TypeInfo verdict (centralized builtin registry). |
| add | `idealingua-v1/idealingua-v1-test-defs/src/main/resources/perf-baseline.json` | Per §8 — baseline fixture file. |
| modify | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/il/ast/IDLTyper.scala` | IMPL-2/3/6: deprecate `IDLPostTyper`/`IDLPretyper`, add a `NewIDLTyper` adapter that delegates to the new pipeline; full removal in IMPL-10. |
| modify | `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TypespaceCompilerBaseFacade.scala` | IMPL-6: add typer-impl dispatch (`:11`); IMPL-7: per-translator `.make(domain, options)` overload alongside `.make(typespace, options)`; IMPL-8: drop `GoTranslatorDescriptor` (`:30`) and `ProtobufTranslatorDescriptor` (`:33`); IMPL-10: remove the legacy dispatch arm. |
| modify | `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/IDLLanguage.scala` | IMPL-8: remove `Go` (`:10-12`, `:30-31`) and `Protobuf` (`:22-24`, `:36-37`). |
| modify | `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/CompilerOptions.scala` | IMPL-8: remove `GoLangBuildManifest`/`ProtobufBuildManifest` imports (`:5`), remove `GoTranslatorOptions` (`:57`) and `ProtobufTranslatorOptions` (`:60`); IMPL-6: add `typerImpl: TyperImpl` field on `UntypedCompilerOptions`. |
| modify | `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/ScalaTranslator.scala` | IMPL-7a: switch `ctx.typespace.domain.types` (`:54`) to `ctx.domain.roots`-aware iteration; switch `ctx.tools.mkStructure` (`:92`) to `ctx.domain.flattenedStructs` lookup. |
| modify | `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/extensions/CirceTranslatorExtensionBase.scala` | IMPL-7a: replace `ctx.typespace.inheritance.implementingDtos(interface.id)` (`:110`) with `ctx.domain.implementingDtos(interface.id)`; replace `ctx.typespace.structure.structure(id)` (`:187`) with `ctx.domain.flattenedStructs(id)`; replace `ctx.typespace(a)` type-lookup (`:214`) with `ctx.domain.userTypes(a)` so the existing `case TypeDef.Alias(_, target, _) => …` match at `:215` continues to compile (`userTypes` returns `TypeDef` directly; note: `ctx.typespace.apply(id)` returns `TypeDef`, not the dealiased target — calling it "alias dealiasing" was wrong; true dealiasing is `TypespaceImpl.dealias` at `:41-49`, replaced by Phase 3 `AliasDealiaser` exposed as `Domain.aliases`). |
| modify | (per-extension Scala) `…/toscala/extensions/*.scala` | IMPL-7a: same pattern — every `ctx.typespace.*` becomes `ctx.domain.*`. |
| modify | (TypeScript translator + extensions) `…/totypescript/*.scala` and `…/extensions/*.scala` | IMPL-7b: same shape as IMPL-7a. |
| modify | (C# translator + extensions) `…/tocsharp/*.scala` and `…/extensions/*.scala` | IMPL-7c: same shape as IMPL-7a. |
| modify | `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/IDLCArgs.scala` | IMPL-6: add `--typer` global flag; IMPL-8: remove `RoleParserSchema("go", …)` (`:82`); IMPL-9: flip default; IMPL-11: remove the flag. |
| modify | `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CommandlineIDLCompiler.scala` | IMPL-8: remove `IDLLanguage.Go -> "0"` (`:283`) from `VersionOverlay.example`. |
| modify | `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/Codecs.scala` | IMPL-8: drop `decGo`/`decGoProjectLayout`/`decGoRepositoryOptions`/`encGo`/`encGoProjectLayout`/`encGoRepositoryOptions` (`:39`, `:41`, `:43`, `:81`, `:83`, `:85`), drop `ProtobufRepositoryOptions` import (`:5`), drop `decProtobufRepo`/`decProtobuf`/`encProtobufRepo`/`encProtobuf` (`:47`, `:49`, `:93`, `:95`). |
| modify | `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/ManifestReader.scala` | IMPL-8: drop the Go writer arm (`:22-23`), Go reader arm (`:37-38`), and Protobuf reader arm (`:41-42`). |
| modify | `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CredentialsReader.scala` | IMPL-8: drop `GoCredentials` (`:18`) and Go dispatch arm (`:27`); drop `ProtobufCredentials` (`:20-21`) and Protobuf dispatch arm (`:29`). |
| modify | `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/ArtifactPublisher.scala` | IMPL-8: drop imports (`:5`), Go dispatch (`:22`), `publishGo` (`:169-280`), Protobuf dispatch (`:24`), `publishProtobuf` (`:282-374`). |
| modify | `build.sbt` | IMPL-8: drop `idealingua-v1-runtime-rpc-go` references at `:919`, `:1247`, `:1564`, `:1741`, `:1759`. |
| delete | `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/togolang/` | IMPL-8 — full subtree (15 files). |
| delete | `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toprotobuf/` | IMPL-8 — full subtree (21 files). |
| delete | `idealingua-v1/idealingua-v1-runtime-rpc-go/` | IMPL-8 — full subtree (11 .go files + sbt artifacts). |
| delete | `idealingua-v1/idealingua-v1-core/src/main/scala/izumi/idealingua/model/publishing/manifests/GoLangBuildManifest.scala` | IMPL-8. |
| delete | `idealingua-v1/idealingua-v1-core/src/main/scala/izumi/idealingua/model/publishing/manifests/ProtobufBuildManifest.scala` | IMPL-8. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/Typespace.scala` | IMPL-10 — replaced by `Domain` value. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/TypespaceImpl.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/StructuralQueriesImpl.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/InheritanceQueriesImpl.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/TypespaceToolsImpl.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/FieldExtractor.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/structures/AbstractStruct.scala` | IMPL-10 — replaced by §4 `FlatStruct`. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/structures/ConstAbstractStruct.scala` | IMPL-10 — replaced by §4 `FlatStruct`. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/structures/ConverterDef.scala` | IMPL-10 — only consumed by legacy structural-query layer. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/structures/FieldConflicts.scala` | IMPL-10 — replaced by §4 `FlatStruct.conflictsHard`/`conflictsSoft`. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/structures/Struct.scala` | IMPL-10 — replaced by §4 `FlatStruct`. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/TypeCollection.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/TypespaceVerifier.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/AdtConflictsRule.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/AdtMembersRule.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/BasicNamingConventionsRule.scala` | IMPL-10 — replaced by `SymbolNames` lifted into Phase 1. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/CyclicImportsRule.scala` | IMPL-10 — replaced by Phase 0 enforcement. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/CyclicInheritanceRule.scala` | IMPL-10 — replaced by Phase 5. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/CyclicUsageRule.scala` | IMPL-10 — replaced by Phase 5. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/DuplicateMemberRule.scala` | IMPL-10 — replaced by Phase 12. |
| keep | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/rules/ReservedKeywordRule.scala` | NOT deleted — `ReservedKeywordRule` is constructed per-translator (`ScalaTranslatorDescriptor.scala:24`, `CSharpTranslatorDescriptor.scala:24`, `TypescriptTranslatorDescriptor.scala:24`) per PR-01 Lesson 9, not auto-applied by the legacy `TypespaceVerifier`. The Phase 12 Validator inherits the same per-translator model. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/MissingDependency.scala` | IMPL-10. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/verification/VerificationRule.scala` | IMPL-10 — interface gone with the verifier. |
| delete | `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/il/ast/IDLTyper.scala` (legacy classes only) | IMPL-10 — `IDLPretyper` and `IDLPostTyper` are removed. Assumes no external consumer imports these symbols; see §11 (Risks and assumptions) for the rationale. |

(Note on the table above: `ReservedKeywordRule.scala` is *kept* because per
PR-01 Lesson 9 it lives per-translator; its row's Action is `keep`, not
`delete`. The `IDLTyper.scala` row's Action is `delete`; see §11's "no external
consumers" assumption below.)

---

## §11. Risks and assumptions

These are PR-02-specific risks. They build on (rather than copy) the meta-plan
risks at `docs/drafts/20260503-1200-modernization-plan.md:170-178`.

**R1 — Field-ordering invariant is fragile.** The new IR's `List`-based field
ordering is correct only if every phase preserves it. Any phase that
internally `groupBy`-then-flattens (without the same key sort) silently
shuffles fields and breaks Circe byte-output. *Mitigation*: add an Assembler
sanity check that reconstructs the source-order field list from the raw AST
and asserts equality against the IR-level `Struct.fields`. The check fires
during `--typer=new` only, so it has zero cost in legacy mode. Cross-check
against PR-03's harness goldens at IMPL-9.

**R2 — Ephemeral wireId reproducibility.** Phase 7's `EphemeralSynthesizer`
must produce exactly the same `wireId` strings as today's `TypeCollection`.
Even a single-character change in `methodInputSuffix` /
`toPositiveBranchName` rotates every ADT JSON tag and breaks all clients.
*Mitigation*: extract the suffix and naming rules from `TypespaceToolsImpl`
into a small frozen constants module that both the legacy and new pipeline
call into; PR-03's fingerprint goldens detect any drift.

**R3 — `idNameFix` semantics depend on raw-AST inspection.** The unnamed-id
field rule at `IDLPostTyper.idNameFix` (`IDLTyper.scala:199-214`) inspects the
*post-resolution* type name (`fixId[AbstractIndefiniteId, TypeId](f.typeId).name`,
line 207). The new pipeline must run name resolution before idNameFix, so the
phase ordering is load-bearing. *Mitigation*: idNameFix lives in Phase 4
(`KindChecker`), which runs after Phase 2 (`NameResolver`) — codified in §3
DAG.

**R4 — Cross-domain reference shape change risks parser drift.** Phase 0
parses every domain in parallel; if two domains declare the same `DomainId`
with overlapping content (the overlay merge mechanism — meta-plan §"idealingua-v1
today" mentions `DomainMeshLoaded` overlay support), the new manager must
either replicate the merge semantics or fail. *Mitigation*: open question
Q5 below; do not implement until resolved.

**R5 — `Typespace` adapter (IMPL-6) hides incomplete IR.** During the
parallel-port window, the adapter wraps `Domain` as `Typespace`; if the IR is
missing a fact a translator queries, the adapter has to recompute it on the
fly, which can mask a missing IR field that should be added in Phase 6 or 7.
*Mitigation*: the adapter's implementation only ever reads pre-materialized
fields; if a translator-side query has no matching IR field, the adapter
throws "BUG: missing IR field <X>", which surfaces during IMPL-7\* porting.

**R6 — Performance threshold (10×, ≤500ms) may not be met if the
`StructuralFlattener` is naively recursive.** The flattener processes
inheritance graphs; on deep diamond hierarchies (the `diamonds.domain`
fixture) a naive impl is still O(M²). *Mitigation*: implement
`StructuralFlattener` in topological order over the inheritance graph
(materialized in Phase 5's cycle output), so each type's flatten is O(parents
+ local-fields).

**R7 — `Typespace.transitivelyReferenced` is consumed by translators not yet
audited.** A `grep` for `transitivelyReferenced` in the translator tree may
surface call sites; if any translator depends on the legacy clone-per-domain
behaviour, the new IR must expose the equivalent via `Domain.imports`.
*Mitigation*: during IMPL-7\* porting, every legacy-only call path is mapped
to a new IR field; if no field exists, this surfaces as a bug to add to the
Phase 11 Assembler output.

**R8 — Scala 3 migration noise interleaves with typer work.** Per
cross-cutting decision C3, dropping 2.13 in the compiler module means the new
typer code is Scala 3 only. The boundary against legacy `IDLPostTyper` must
still cross-compile until IMPL-10. *Mitigation*: keep the new typer in a
sub-package compiled Scala-3-only; the cross-compile barrier is the
`IDLTyper`-level adapter.

**R9 — Ephemeral DTO fingerprint depends on the synthesizer running before
the fingerprint phase.** If somebody re-orders Phase 7 and Phase 9, the
fingerprint omits ephemerals and the harness misses regressions there.
*Mitigation*: codified in §3 DAG; Phase 9 has an explicit dependency edge
from Phase 7.

**A1 — Assumption: no external consumer imports `IDLPretyper` or
`IDLPostTyper`.** The §10 IDLTyper.scala row is locked as `delete` (rather
than `modify-then-delete-next-release`) on this assumption. If any external
project imports those classes directly, the fallback is to demote IMPL-10's
`IDLTyper.scala` row to `modify` (a re-export shim) for one release, then
delete in IMPL-11. The user's "no external consumers" assertion (Q6 below for
Go/Protobuf is the analogous question; for `IDLPretyper`/`IDLPostTyper` no
explicit user statement exists) means PR-02 ships a single `delete`.

---

## §12. Open questions for the user

These are architectural choices PR-02 could not make from source alone. Each
is a numbered checkbox. The "what changes downstream" line names what
becomes locked once the question is answered.

- [ ] **Q1 — Which Scala version is acceptable for the compiler module
  (cross-cutting C3)?** Options: (a) Scala 3 only; (b) cross-build 3 + 2.13
  (current `build.sbt:173-185` posture); (c) cross-build 3 + 2.13 + 2.12.
  *Downstream*: locks the syntax used in the new typer (e.g. enums, opaque
  types, exhaustivity behaviour). Recommended: (a).

- [x] **Q2 — Are Buzzers and Streams in scope for the new IR (cross-cutting
  C5)?** **RESOLVED 2026-05-03**: Buzzers first-class; Streams deprecated-
  but-kept-working (per `tasks.md` cross-cutting note C5 / Q1 and §3
  "Deprecated-but-supported types: Streams").

- [ ] **Q3 — Are constants in scope for typing (cross-cutting C6)?** Options:
  (a) Phase 8 type-checks fully; (b) Phase 8 carries unchecked constants
  through; (c) constants are dropped from the language. *Downstream*: decides
  whether the three TODOs at `IDLTyper.scala:240, 245, 250` produce
  diagnostics or are silently accepted. Recommended: (a).

- [x] **Q4 — Is `ForeignType` deleted (cross-cutting C7)?** **RESOLVED
  2026-05-03**: Option (b) — delete the syntax and produce a hard
  diagnostic on encounter. Verified zero `.domain` fixtures under
  `idealingua-v1-test-defs/` use the `foreign` keyword, so removal is
  safe. See §9 C7 above for the grammar-surface enumeration.

- [ ] **Q5 — How does the new family manager handle duplicate `DomainId`
  across files (= overlay merge)?** Options: (a) replicate the legacy merge
  behaviour (`DomainMeshLoaded` overlay mechanism); (b) require all overlays
  to be merged at the loader level before the family manager sees them;
  (c) error on duplicates. *Downstream*: decides whether `IdealinguaFamilyManager`
  has a merge step or assumes pre-merged input. PR-01 §7 lists this as an
  open question for Baboon as well; we cannot inherit Baboon's answer.

- [ ] **Q6 — Are Go and Protobuf actually unused (meta-plan risk #7)?**
  Options: (a) yes, delete in IMPL-8 outright; (b) no, fall back to the
  deprecation path in §5. *Downstream*: decides whether IMPL-8 lands as a
  single deletion or a two-step move-then-delete.

- [ ] **Q7 — Is the `wireId` formula `path.toPackage.mkString(".") + "." +
  name` (`TypeId.scala:31`) locked forever (cross-cutting C4)?** Options:
  (a) yes, wire-format invariant; (b) reserve the right to change with a
  two-version migration. *Downstream*: locks whether the EphemeralSynthesizer
  may ever change the path of an ephemeral, which would silently rewrite all
  ADT JSON tags.

- [ ] **Q8 — Where does the IMPL-7\* test harness live (cross-cutting C10)?**
  Options: (a) `idealingua-v1-test-harness` new module; (b) reuse
  `idealingua-v1-compiler/src/test/`. *Downstream*: PR-03's deliverable; this
  is here only because it slightly affects PR-02's `--typer` flag-flip
  prerequisites. Recommended: (a).

- [ ] **Q9 — Does the new typer produce a "BUG" diagnostic class for
  invariant violations, or use `assert`?** Options: (a) typed
  `Diagnostic.InternalError`; (b) `IDLException` reduced to assertion-level;
  (c) `Predef.assert`. PR-01 Lesson 5 prefers (b). *Downstream*: decides
  whether a buggy typer surfaces as a diagnostic to the user or as a
  developer-facing stack trace.

- [ ] **Q10 — Is the TS / C# field-ordering invariant currently honoured by
  those translators?** PR-02 cannot answer this from source alone — the
  harness in PR-03 must measure cross-language byte parity (per meta-plan
  §"Known gaps" item 2 `:179-183`). *Downstream*: decides whether IMPL-7b /
  IMPL-7c need explicit field-order fix work or only the `Domain`-consumption
  switch.

---

*End of PR-02 plan.*
