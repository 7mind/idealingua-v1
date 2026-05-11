# PR-02 IMPL-3 — Structural Phases (5-11) + F16 IR Consolidation

Plan author: planning subagent (review-loop, 2026-05-08).
Source briefs: tasks.md (C1/Q6, C3/Q8, C5/Q1, C6/Q10, C8/L1, C11/L2, C12/L3, F1, F16, F19), master plan §3 Phases 5-11 + §4 + §10, IMPL-1 + IMPL-2 commits (`441a040`, `a55e5a9`).

**Pre-locked decisions (planner-recommended; no user-blockers):**
- Q1 ephemeral ADTs: treat as plain `Member.User(TypeDef.Adt)` + record in `ephemeralsOf` for ownership tracking. Only DTO-shaped ephemerals carry an `EphemeralOrigin`.
- Q2 Phase 8 const-target resolution: implement small in-phase resolver in `ConstValueTyper` (~30 lines, mirrors `NameResolver.resolveRef`). Avoid touching IMPL-2 code.
- Q3 Phase 8 coercion strictness: strict primitive matching (`RawVal.CInt` requires `Primitive.TInt32` declared target). Tighter than legacy; can relax later if needed.
- Q4 Phase 9 cross-domain fingerprint: serialize imported type's `wireId` only (not nested imported-domain fingerprint). Simpler, matches C4/Q12 locked invariant.
- Q5 F16 absorb-now in IMPL-3: ship the IR consolidation now (`ServiceDef`/`BuzzerDef`/`StreamsDef` absorbed into `TypeDef.Service`/`Buzzer`/`Streams`).

---

## §1 Goal & non-goals

**Goal.** Single atomic commit on `wip/necromancy` implementing 7 structural phases (master plan §3 Phases 5-11) as pure functions, plus F16 IR consolidation. After IMPL-3, `Phase 11 Assembler.apply(ResolvedDomain): Domain` yields the frozen `Domain` IR.

**Inter-phase carrier**: extend `ResolvedDomain` with Phase 5-10 annotation fields (`loops`, `flattenedStructs`, `parents`, `implementingDtos`, `ephemeralsOf`, `ephemeralOwner`, `fingerprints`, `domainFingerprint`, `roots`, `typedConsts`). The Assembler projects to `Domain` mechanically.

**Non-goals.**
- Phase 0 `FamilyIndex` (IMPL-4).
- Phase 12 `Validator` (IMPL-5).
- Feature-flag wiring (IMPL-6).
- Translator changes (IMPL-7a/b/c).
- Legacy typer deletion (IMPL-10).

---

## §2 Phase 5 — `CycleDetector`

Tarjan-SCC over the type-reference graph. Edges:
- DTO/Interface: fields + superclasses; container types (TList, TSet, TOption, TMap.valueType) flagged container-broken.
- Identifier: each `IdField` target.
- ADT: each `AdtMember.typeId`.
- Service/Buzzer (post-F16): method signatures into input/output struct fields.

For each non-trivial SCC, emit `Cycle[TypeId](members, terminating)` where `terminating = true` iff every back-edge is container-broken. `terminating == false` → diagnostic:
- `CyclicUsage` — non-broken cycle through field references.
- `CyclicInheritance` — cycle entirely through inheritance edges.

Inheritance-cycle and ADT-membership-cycle from legacy `InheritanceQueriesImpl.checkCycles:103-109` and `StructuralQueriesImpl.adtRecursiveMembers:182-193` are subsumed.

---

## §3 Phase 6 — `StructuralFlattener`

Build:
- `parents: Map[TypeId, Set[InterfaceId]]` via transitive walk of `struct.superclasses.interfaces`.
- `implementingDtos: Map[InterfaceId, Set[DTOId]]` via inversion.
- `flattenedStructs: Map[StructureId, FlatStruct]` via BFS from owner, accumulating `FlatField(field, origin, distance)`. Local fields `distance=0`; ancestors prepended in BFS order.

**Field-order invariant (C12/L3)**: preserve declaration order using `mutable.ListBuffer` → `.toList`. No `.groupBy.toMap` without sort-key restoration. Matches legacy `StructuralQueriesImpl.scala:41` post-sort.

`removedFields` from any level propagates (matches `InheritanceQueriesImpl:77-78`).

**Conflict classification**:
- Same `(name, typeId)` + same meta → `conflictsSoft`.
- Same `name` + incompatible `typeId` → `conflictsHard` + `FieldNameConflict` diagnostic.
- Covariance: use `parents` map.

Diagnostics: `FieldNameConflict`, `MissingMixin`.

---

## §4 Phase 7 — `EphemeralSynthesizer`

Naming rules from `TypespaceToolsImpl.scala:9-89` (literal constants — wire-format invariant):
- `methodInputSuffix = "Input"`, `methodOutputSuffix = "Output"`
- `goodAltSuffix = "Success"`, `badAltSuffix = "Failure"`, `goodAltBranchName = "Success"`, `badAltBranchName = "Failure"`
- `toDtoName(InterfaceId) = "Struct"` (literal)
- `toInterfaceName(DTOId) = "Defn"` (literal)

Replicate these as `private val` in `EphemeralSynthesizer` (don't depend on `TypespaceToolsImpl` — legacy code stays until IMPL-10).

**Synthesized ephemerals**:
1. Service method inputs/outputs (per `Output.{Singular, Struct, Void, Algebraic, Alternative}` cases).
2. Buzzer method inputs/outputs (same pattern; no legacy adapter).
3. Interface mirror DTOs (`DTOId(I, "Struct")`).
4. DTO→Interface mirror (`InterfaceId(D, "Defn")`) per legacy `TypeCollection.dtoEphemeralIndex:74-81`.

Streams: per C5/Q1, no per-method ephemerals (matches legacy).

**Ephemeral ADTs** (Q1 resolution): emit as `Member.User(TypeDef.Adt)` directly + record in `ephemeralsOf`/`ephemeralOwner`. No new `EphemeralOrigin` case. DTO-shaped ephemerals retain `EphemeralOrigin.{MethodInput, MethodOutput, InterfaceMirror, DtoMirror}`.

Diagnostics: `EphemeralNameCollision`.

---

## §5 Phase 8 — `ConstValueTyper`

Walk `rd.consts: List[RawConst]`; for each:
1. Resolve `RawTypeRef` target via in-phase resolver (~30 lines mirroring `NameResolver.resolveRef`).
2. Type-check `RawVal` against resolved target (strict primitive matching per Q3):
   - `CInt ↔ TInt32` (declared); `CLong ↔ TInt64`/`TUInt64`; `CFloat ↔ TFloat`/`TDouble`; `CString ↔ TString`/`TUUID`/`TBLOB`/time types; `CBool ↔ TBool`.
   - `CList ↔ Generic.TList(elem)`; `CMap ↔ Generic.TMap(k, v)`; `CTyped`/`CTypedList`/`CTypedObject` recurse.
   - DTO target: `CTypedObject` required; typecheck against `rd.flattenedStructs(target)`.

Output: `typedConsts: List[Const]` annotation on `ResolvedDomain` (preserves declaration order). Failed type-checks still emit a `Const` (sentinel) + diagnostic. Closes F19, C6/Q10 (`IDLTyper.scala:240,245,250` TODOs superseded).

Diagnostics: `ConstTypeMismatch`, `ConstFieldMissing`, `ConstFieldUnknown`, `BadConstValue`.

---

## §6 Phase 9 — `FingerprintCalculator`

Per-type canonical serialization → SHA-256 → `Fingerprint(ByteVector)`:
1. Type tag (1 byte) + `id.wireId` (UTF-8).
2. Structural types: `flattenedStructs(id).fields` in IR-list order, `(name, typeId.wireId, distance, origin.wireId)`.
3. ADTs: `alternatives` in declaration order.
4. Enums: `members` in declaration order.
5. Aliases: `target.wireId`.
6. Services/Buzzers (post-F16): `methods`/`events` in declaration order + signature-fingerprint.
7. Cross-domain refs: imported `typeId.wireId` only (Q4; not nested imported-fingerprint; matches C4/Q12 lock).

Domain fingerprint: SHA-256 over `domainId.wireBytes ++ sortedConcat(fingerprints.toSeq.sortBy(_._1.wireId).map(_._2.value))`. Sort on serialization is OK because per-type fingerprints encode declaration order.

JDK `MessageDigest.getInstance("SHA-256")`; no new dep. `ByteVector` already in deps (F1).

No diagnostics; pure computation.

---

## §7 Phase 10 — `RootExtractor`

`roots = rd.userTypes.keySet`. Ephemerals/builtins/imported types excluded. Aliases included via `userTypes`.

No diagnostics.

---

## §8 Phase 11 — `Assembler`

Pure projection from `ResolvedDomain` (with all annotations) to `Domain`:

```scala
Domain(
  id = rd.id, meta = rd.meta,
  members = rd.members,
  roots = rd.roots,
  ephemeralsOf = rd.ephemeralsOf,
  ephemeralOwner = rd.ephemeralOwner,
  flattenedStructs = rd.flattenedStructs,
  parents = rd.parents,
  implementingDtos = rd.implementingDtos,
  loops = rd.loops,
  fingerprints = rd.fingerprints,
  domainFingerprint = rd.domainFingerprint,
  imports = rd.imports,
  consts = rd.typedConsts,
  aliases = rd.aliases,
  userTypes = rd.userTypes,
)
```

After F16 consolidation: `services`/`buzzers`/`streams` flow through `members` as `Member.User(TypeDef.Service(…))` etc.; `Domain` no longer has separate service-family fields.

**Frozen-after-assembly invariant**: case-class immutability + immutable collections enforce. No mutation after Phase 11.

No diagnostics.

---

## §9 F16 IR consolidation

1. Edit `TypeDef.scala`: delete `ServiceDef`/`BuzzerDef`/`StreamsDef` standalone case classes; add `TypeDef.Service`/`Buzzer`/`Streams` cases. Update scaladoc.
2. Edit `Domain.scala`: drop separate `services`/`buzzers`/`streams` lists; update scaladoc.
3. Edit `ResolvedDomain.scala`: drop separate lists; add Phase 5-10 annotation fields with empty defaults.
4. Edit `NameResolver.scala`: `fixService`/`fixBuzzer`/`fixStreams` emit `Member.User(TypeDef.Service(…))` via `placeUserType`; drop separate-list construction.
5. Edit IMPL-2 specs that reference `services`/`buzzers`/`streams`: replace with `userTypes(serviceId)` pattern matches.
6. Legacy catch-alls (`IDLTyper.scala`, `InheritanceQueriesImpl.scala`, `AnyvalExtension.scala`): unchanged — operate at `TypeId` level, not `TypeDef`.

---

## §10 Module + package layout

**New phase files** under `idealingua-v1-model/src/main/scala/izumi/idealingua/typer/phase/`:
- `CycleDetector.scala`, `StructuralFlattener.scala`, `EphemeralSynthesizer.scala`, `ConstValueTyper.scala`, `FingerprintCalculator.scala`, `RootExtractor.scala`, `Assembler.scala`.

**New specs** under `idealingua-v1-model/src/test/scala/izumi/idealingua/typer/phase/`:
- `CycleDetectorSpec.scala`, `StructuralFlattenerSpec.scala`, `EphemeralSynthesizerSpec.scala`, `ConstValueTyperSpec.scala`, `FingerprintCalculatorSpec.scala`, `RootExtractorSpec.scala`, `AssemblerSpec.scala`.

**Modified files** (IR consolidation):
- `TypeDef.scala`, `Domain.scala`, `ResolvedDomain.scala`, `Diagnostics.scala` (+11 sub-types), `NameResolver.scala`, IMPL-2 specs.

Cross-build: Scala 2.13.18 + 3.8.3, JVM + JS. No new deps.

---

## §11 New diagnostic sub-types (added to `Diagnostics.scala`)

```scala
// Phase 5
CyclicUsage, CyclicInheritance, NonTerminatingCycle
// Phase 6
FieldNameConflict, MissingMixin
// Phase 7
EphemeralNameCollision
// Phase 8
ConstTypeMismatch, ConstFieldMissing, ConstFieldUnknown, BadConstValue
```

11 new sub-types. Total post-IMPL-3: 21 (10 IMPL-2 + 11 IMPL-3).

---

## §12 Unit-test plan (~17 cases)

- **CycleDetectorSpec** (3): terminating recursion / DTO self-ref / cyclic interface inheritance.
- **StructuralFlattenerSpec** (3): multi-parent merge / covariant shadow → `conflictsSoft` / incompatible-type → `FieldNameConflict`.
- **EphemeralSynthesizerSpec** (3): `Output.Singular` / `Output.Alternative` (Success+Failure+ADT) / interface mirror.
- **ConstValueTyperSpec** (3): primitive const / DTO const / type-mismatch.
- **FingerprintCalculatorSpec** (2): stability across rebuild / field-reorder changes fingerprint (C12 enforcement).
- **RootExtractorSpec** (1): `roots == userTypes.keySet`.
- **AssemblerSpec** (2): end-to-end positive / multi-violation diagnostics accumulation.

Run targets: Scala 2.13.18 + 3.8.3, JVM + JS.

---

## §13 Risks

1. **F16 consolidation churn affecting IMPL-2 specs** (medium): `NameResolverSpec.scala` references `resolved.services`/`.buzzers`/`.streams`. Mitigation: explicit T1 audit step.
2. **Field-order regression** (high impact, low probability): C12/L3 wire-format invariant. Mitigation: `mutable.ListBuffer` → `.toList`; Phase 9 fingerprint encodes order so regression surfaces in spec.
3. **Ephemeral ADT representation** (medium): Q1 resolution = plain `Member.User(TypeDef.Adt)` + record in `ephemeralsOf`. If wrong, IMPL-7 translator port may diverge.
4. **Const value coercion strictness** (low-medium): Q3 strict. Easy to relax later.
5. **Cross-domain fingerprint** (low): Q4 wireId-only. Simpler, matches C4/Q12.
6. **`ResolvedDomain` field bloat** (low): ~25 fields. Manageable.
7. **No legacy compile-path wiring**: spec coverage is the only protection. Mitigation: `AssemblerSpec` end-to-end.

---

## §14 Sub-task breakdown (T1-T8)

Single atomic commit; T1-T8 are review checkpoints.

- **T1 — F16 consolidation** (prerequisite): edit `TypeDef.scala`, `Domain.scala`, `ResolvedDomain.scala`, `NameResolver.scala`; update IMPL-2 specs. Verify cross-build + IMPL-2 specs green WITHOUT Phase 5-11 code yet.
- **T2 — Phase 5 + spec** + Phase 5 diagnostics.
- **T3 — Phase 6 + spec** + Phase 6 diagnostics.
- **T4 — Phase 7 + spec** + `EphemeralNameCollision`.
- **T5 — Phase 8 + spec** + 4 const diagnostics.
- **T6 — Phase 9 + spec**.
- **T7 — Phase 10 + spec** + Phase 11 + `AssemblerSpec` (end-to-end pipeline).
- **T8 — Final verification**: 4 cross-build legs + 4 harness contracts + ~34 total typer specs. F16 ledger flip in `tasks.md`.

---

## §15 Ledger update brief (`tasks.md` after IMPL-3)

PR-02 IMPL-3 row flips `[~] → [x]` with summary:

```
- [x] **PR-02 IMPL-3** — Phases 5-11 + F16 IR consolidation. 7 phase files + 7 specs (~17 test cases) + 11 new Diagnostic sub-types. F16 absorbed: ServiceDef/BuzzerDef/StreamsDef → TypeDef.Service/Buzzer/Streams; Domain/ResolvedDomain no longer carry separate lists; service-family flows through `members` as Member.User. NameResolver + IMPL-2 specs updated. All 4 cross-build legs green; all 4 harness contracts unchanged. F19 closed (typed Const through Phase 8); C6/Q10 IDLTyper TODOs at :240,245,250 superseded.
```

F16 status: `[ ] → [x]` (full resolution: legacy widening + IR consolidation shipped).
F19 status: `[ ] → [x]` (typed Const through Phase 8).

Add Completed entry mirroring IMPL-2 format.
