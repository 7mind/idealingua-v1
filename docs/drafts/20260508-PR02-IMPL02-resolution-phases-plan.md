# PR-02 IMPL-2 — Resolution Phases (ScopeBuilder, NameResolver, AliasDealiaser, KindChecker)

Plan author: planning subagent (review-loop, 2026-05-08).
Source briefs: tasks.md (C1/Q6, C3/Q8, C8/L1, C12/L3, F16), master plan §3 Phases 1-4 + §4 + §10, defects-m1.md PR-02-D05.

**Pre-locked decisions (planner-recommended):**
- F16 = Option A1: widen legacy `TypeId` sealed hierarchy + catch-alls in 4 sites; defer `TypeDef`/`ServiceDef` IR consolidation to IMPL-3.
- `ResolvedDomain.scala` lives alongside `Domain.scala` in `izumi.idealingua.typer.ir`.
- Phase tests at `idealingua-v1-model/src/test/scala/izumi/idealingua/typer/phase/`.
- IMPL-2 `ScopeBuilder` consumes `DomainMeshLoaded` as bypass; F18 tracks IMPL-4 reshape to `(DomainId, ParsedDomain, FamilyIndex)`.

---

## §1 Goal & non-goals

**Goal.** Single atomic commit on `wip/necromancy` implementing the first 4 typer phases (master plan §3 Phases 1-4) as pure functions producing a `ResolvedDomain` intermediate IR. Code compiles, is unit-testable in `idealingua-v1-model`'s test source set, and is **not called by the legacy compile path**. Feature-flag wiring deferred to IMPL-6.

**Non-goals.**
- Phases 5-11 (IMPL-3).
- Phase 0 `FamilyIndex` (IMPL-4).
- Phase 12 Validator (IMPL-5).
- Feature-flag wiring (IMPL-6).
- Translator changes (IMPL-7a/b/c).
- Legacy typer deletion (IMPL-10).

---

## §2 `ResolvedDomain` shape

Single new file `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/ResolvedDomain.scala`. 11 fields:

```scala
final case class ResolvedDomain(
  id: DomainId,
  meta: DomainMetadata,
  members: Map[TypeId, Member],          // Phase 2 fills
  userTypes: Map[TypeId, TypeDef],       // Phase 2 projection
  services: List[ServiceDef],            // Phase 2 fills (per F16 split — IMPL-3 absorbs)
  buzzers: List[BuzzerDef],              // Phase 2 fills
  streams: List[StreamsDef],             // Phase 2 fills
  imports: Map[DomainId, Set[TypeId]],   // Phase 1 fills
  aliases: Map[AliasId, TypeId],         // Phase 3 fills
  consts: List[RawConst],                // Phase 1 carry-through (raw; Phase 8 IMPL-3 types)
  diagnostics: Diagnostics,              // Phases 1-4 accumulate
)
```

Fields NOT yet in `ResolvedDomain` (deferred to IMPL-3+):
- `roots` (Phase 10 RootExtractor)
- `ephemeralsOf`/`ephemeralOwner` (Phase 7 EphemeralSynthesizer)
- `flattenedStructs` (Phase 6 StructuralFlattener)
- `parents`/`implementingDtos` (Phase 6)
- `loops` (Phase 5 CycleDetector)
- `fingerprints`/`domainFingerprint` (Phase 9 FingerprintCalculator)

---

## §3 Phase 1 — `ScopeBuilder`

**File**: `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/ScopeBuilder.scala`.

**Input** (IMPL-2 bypass per F18): `DomainMeshLoaded` (output of legacy `IDLPretyper.perform()` at `IDLTyper.scala:60-74`). IMPL-4 reshapes to `(DomainId, ParsedDomain, FamilyIndex)`.

**Output** (internal type, IMPL-2-only):

```scala
final case class ScopedDomain(
  domainId: DomainId,
  localNames: Map[String, TypeId],
  importedNames: Map[String, TypeId],
  index: Map[TypeId, RawTypeDef],
  raw: DomainMeshLoaded,
  diagnostics: Diagnostics,
)
```

**Algorithm**:
1. Materialise `localNames` from `defn.types` walking `RawTypeDef.WithId` / `NewType` / `ForeignType` cases. Emit `ForeignTypeUnsupported` diagnostic (per C7) for `ForeignType` rather than throwing.
2. Resolve `importedNames` via per-domain recursive `ScopeBuilder` call (IMPL-2 bypass; IMPL-4 uses `FamilyIndex`).
3. Check import-name vs local-name clash (legacy `IDLPretyper.perform` at `IDLTyper.scala:56-58`) → `ImportNameClashesWithLocal` diagnostic.
4. Emit `ScopeCollision` for duplicate names within `localNames`.

**Diagnostics** (sealed sub-types of `Diagnostic`):
- `ImportNameClashesWithLocal`
- `ScopeCollision`
- `ForeignTypeUnsupported`

**Replaces** (read-only inspiration; NO edits in IMPL-2): `IDLPretyper.perform` clash check; `IDLPostTyper.imported`/`mapping`/`index`.

---

## §4 Phase 2 — `NameResolver`

**File**: `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/NameResolver.scala`.

**Input/Output**: `ScopedDomain → ResolvedDomain` (first construction; alias/diagnostics extended later).

**Responsibility**: replace every `AbstractIndefiniteId` in raw types/services/buzzers/streams with a definite `TypeId`. Build typed IR values:
- `RawTypeDef.Enumeration` → `TypeDef.Enum`
- `RawTypeDef.Alias` → `TypeDef.Alias`
- `RawTypeDef.Identifier` → `TypeDef.Identifier`
- `RawTypeDef.Interface` → `TypeDef.Interface`
- `RawTypeDef.DTO` → `TypeDef.Dto`
- `RawTypeDef.Adt` → `TypeDef.Adt`
- `RawService` → `ServiceDef`
- `RawBuzzer` → `BuzzerDef`
- `RawStreams` → `StreamsDef`

Builtins (`Primitive.mapping` at legacy `IDLTyper.scala:380`) added to `members` lazily as `Member.Builtin(prim)` only when referenced.

**Diagnostics**:
- `UnknownTypeRef`
- `WrongGenericArity`

**Replaces**: `IDLPostTyper.makeDefinite`/`lookupLocal`/`lookupAnother`/`toGeneric`/`fixId`/`fixSimpleId`/`transformSimpleId`.

**Non-fatal**: unresolved references produce diagnostic + placeholder; downstream phases skip those entries.

---

## §5 Phase 3 — `AliasDealiaser`

**File**: `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/AliasDealiaser.scala`.

**Input/Output**: `ResolvedDomain → ResolvedDomain` (annotated — `aliases` map filled).

**Algorithm**:
1. Collect `TypeDef.Alias` entries from `userTypes`.
2. For each `AliasId`, walk `.target` until non-alias or previously-visited (cycle).
3. On cycle: emit `CyclicAlias(cycle, position)` diagnostic; never throw.
4. Build `aliases: Map[AliasId, TypeId]`.

**Diagnostics**:
- `CyclicAlias`
- `AliasTargetUnresolved`

**Replaces**: `TypespaceImpl.dealias` at `TypespaceImpl.scala:41-49`; alias-chasing in `IDLPostTyper.fixSimpleId` at `IDLTyper.scala:537-549`.

---

## §6 Phase 4 — `KindChecker`

**File**: `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/KindChecker.scala`.

**Input/Output**: `ResolvedDomain → ResolvedDomain` (diagnostics extended).

**Per-defn-kind invariants** (master plan §3 lines 293-318; defects-m1 PR-02-D05):
1. **Identifier fields**: each `IdField` must be `Primitive` | `IdentifierId` | `EnumId`. Replaces throw at `IDLTyper.scala:157`. Diagnostic: `BadIdentifierFieldType`.
2. **Mixin targets**: every `Struct.superclasses.concepts` entry must resolve to `DTOId` | `InterfaceId`. Replaces throw at `IDLTyper.scala:322`. Diagnostic: `BadMixinTarget`.
3. **ADT branches**: must be known user types (DTO / Identifier / Interface); must NOT be `AdtId` (no nesting). Replaces throw at `IDLTyper.scala:267-268`. Diagnostic: `NestedAdtMemberUnsupported`.
4. **Interface / DTO / Enum**: no additional Phase-4 invariants (per defects-m1 PR-02-D05). Duplicate-member, naming-convention, empty-enum checks belong to Phase 12 Validator (IMPL-5).

---

## §7 F16 reconciliation: Option A1 (widen `TypeId` + minimal catch-alls)

Four legacy edits (precisely):

1. **`TypeId.scala`**: make `ServiceId`, `BuzzerId`, `StreamsId` extend the sealed `TypeId` trait.
2. **`IDLTyper.scala:481-504, 506-530`** (`transformSimpleId`, `fixSimpleId`): add `case _: ServiceId | _: BuzzerId | _: StreamsId => throw new IllegalStateException(...)` catch-arms. Legacy code never feeds these ids into `fixSimpleId` (dedicated `fixServiceId`/`fixBuzzerId`/`fixStreamsId` at `:469`, `:473`, `:477` handle them), so catch-all is unreachable.
3. **`InheritanceQueriesImpl.scala:37-64, 68-91`** (`safeParentsInherited`, `safeParentsConcepts`): add `case _: ServiceId | _: BuzzerId | _: StreamsId => List()` (services/buzzers/streams have no inheritance).

**IR consequence for IMPL-2**: IMPL-1 split `TypeDef`/`ServiceDef`/`BuzzerDef`/`StreamsDef` is retained; `ResolvedDomain.services`/`buzzers`/`streams` stay as separate `List` fields. F16 marked `[~]` post-IMPL-2 (legacy widening shipped; IR consolidation pending). Full `[x]` after IMPL-3 absorbs the split.

---

## §8 Module + package layout

- New phase files under `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/`: `ScopeBuilder.scala`, `NameResolver.scala`, `AliasDealiaser.scala`, `KindChecker.scala`.
- New IR type: `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/ir/ResolvedDomain.scala`.
- Diagnostics sub-types: co-located in each phase file's companion object (or grouped under `izumi.idealingua.typer.ir.diagnostics.*` if any becomes shared).
- Cross-build Scala 2.13.18 + 3.8.3.
- Legacy edits per F16/Option A1: `TypeId.scala`, `IDLTyper.scala` (2 sites), `InheritanceQueriesImpl.scala` (2 sites). NO edits to `TypespaceCompilerBaseFacade`, `IDLPretyper`, or any translator.

---

## §9 Unit-test plan

**Location**: `idealingua-v1-model/src/test/scala/izumi/idealingua/typer/phase/`.

**Test files**:
- `ScopeBuilderSpec.scala` — positive: simple 2-type + 1-imported-type domain; negative: import-name clash; duplicate local name; `foreign` type.
- `NameResolverSpec.scala` — positive: primitive/local/imported/generic resolution; negative: unknown ref; bad arity.
- `AliasDealiaserSpec.scala` — positive: A→B→primitive; negative: A→B→A cycle.
- `KindCheckerSpec.scala` — positive: identifier-with-primitive-fields; mixin-from-DTO; ADT-with-DTO-branches. Negative: identifier-with-DTO-field; mixin-to-enum; ADT-with-nested-ADT.

**Fixtures**: inline raw `RawTypeDef`/`DomainMeshLoaded` construction (no `.domain` source files). ScalaTest `AnyFunSpec` style matching `NegativeSpec`.

**Verification gates** (end of T5):
- `sbt idealingua-v1-model/test` — green on Scala 2.13.18 + 3.8.3, JVM + JS.
- `sbt verifyGoldens` — unchanged (new phases never invoked by legacy path).
- `sbt runWireFixtures` — unchanged.
- `sbt runCrossLangInterop` — unchanged.
- `sbt idealingua-v1-test-harness/test` — `NegativeSpec` 11/11.
- `sbt compile` — wider build green.

---

## §10 Risks

1. **`DomainMeshLoaded` bypass.** IMPL-2 substitutes `DomainMeshLoaded` for the master-plan-spec `(DomainId, ParsedDomain, FamilyIndex)`. F18 tracks IMPL-4's reshape. Mitigation: `private type Input = DomainMeshLoaded` so the IMPL-4 rewrite is local.
2. **F16 Option A1 may produce dead-code in IMPL-3.** IMPL-3 may choose to absorb `ServiceDef` into `TypeDef`; `ResolvedDomain.services` etc. would become redundant. Not a regression — that's IMPL-3's absorption work.
3. **Cross-domain alias targets.** AliasDealiaser walks only local aliases; cross-domain alias references already resolved by Phase 2.
4. **Scala 3 `@unchecked` subtleties.** Fall back to `@nowarn` annotation if needed (consistent with `IDLPostTyper` line 77).
5. **`members` includes builtins.** Pre-seed with `Primitive.mapping.values` to make `members.apply(primId)` total downstream.
6. **`RawConst` carry-through.** Phase 8 IMPL-3 converts to typed `Const`.

---

## §11 Sub-task breakdown

- **T1**: F16 reconciliation — widen `TypeId` + 4 catch-alls. Verify cross-build + 4 harness contracts unchanged.
- **T2**: `ResolvedDomain` + diagnostic sub-types. Cross-build green.
- **T3**: `ScopeBuilder` + `ScopeBuilderSpec`. Cross-build green.
- **T4**: `NameResolver` + `AliasDealiaser` + specs.
- **T5**: `KindChecker` + spec + final verification (unit tests + harness contracts + wider compile + cross-build).

Single atomic commit on `wip/necromancy` after T5.
