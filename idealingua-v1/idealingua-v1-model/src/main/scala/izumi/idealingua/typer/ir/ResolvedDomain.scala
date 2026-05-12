package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.{DomainId, StructureId, TypeId}
import izumi.idealingua.model.common.TypeId.{AliasId, DTOId, InterfaceId}
import izumi.idealingua.model.il.ast.raw.defns.RawConst
import izumi.idealingua.model.il.ast.typed.DomainMetadata
import scodec.bits.ByteVector

/** Intermediate IR carrier produced by typer Phases 1-10.
  *
  * Phase 2 (`NameResolver`) constructs the initial value with all name-resolution
  * data filled in. Phase 3 (`AliasDealiaser`) fills in `aliases`. Phase 4
  * (`KindChecker`) only extends `diagnostics`. Phases 5-10 progressively annotate
  * structural data (`loops`, `flattenedStructs`, `parents`, `implementingDtos`,
  * `ephemeralsOf`, `ephemeralOwner`, `fingerprints`, `domainFingerprint`,
  * `roots`, `typedConsts`). Phase 11 (`Assembler`) projects to the frozen
  * `Domain` IR.
  *
  * Per the field-ordering invariant (master plan §4 / tasks C12/L3), every
  * field that flows to the wire is a `List`, never a `Set` or `Map`.
  *
  * Per the diagnostics-mode invariant (tasks C8/L1), every Phase 1-10 error
  * surfaces as an accumulated `Diagnostic` rather than a thrown exception.
  *
  * After F16 absorption (PR-02 IMPL-3): service-family types live in
  * `members`/`userTypes` as `Member.User(TypeDef.Service(...))`, etc. — no
  * separate lists.
  *
  * @param id                  Domain identifier.
  * @param meta                Origin file path, direct inclusions/imports, and
  *                            domain-level annotations.
  * @param members             User types + builtins + ephemerals reachable from
  *                            this domain. Phase 2 pre-seeds entries for
  *                            referenced `Primitive` values as `Member.Builtin`
  *                            so primitive lookups are total.
  * @param userTypes           Projection of `members` onto `Member.User` cases.
  *                            Phase 2 populates concurrently with `members`.
  * @param imports             Transitively-imported types keyed by origin
  *                            `DomainId`. Phase 1 populates.
  * @param aliases             Resolved alias targets (alias-id → first non-alias
  *                            `TypeId` reached by chasing `.target`). Phase 3
  *                            populates.
  * @param consts              Raw constant definitions in declaration order.
  *                            Phase 1 carries through; Phase 8 produces the
  *                            typed counterpart in `typedConsts`.
  * @param loops               Detected cycles in the type reference graph.
  *                            Phase 5 populates.
  * @param flattenedStructs    Pre-materialised flat struct for every
  *                            `StructureId`. Phase 6 populates.
  * @param parents             Maps each structural `TypeId` to the set of
  *                            `InterfaceId`s it directly or transitively
  *                            extends. Phase 6 populates.
  * @param implementingDtos    Maps each `InterfaceId` to the set of `DTOId`s
  *                            that implement it. Phase 6 populates.
  * @param ephemeralsOf        Maps each owner `TypeId` to the set of ephemeral
  *                            `TypeId`s it owns. Phase 7 populates.
  * @param ephemeralOwner      Inverse map (ephemeral → owner). Phase 7 populates.
  * @param fingerprints        Per-type SHA-256 fingerprints. Phase 9 populates.
  * @param domainFingerprint   Domain-wide SHA-256 fingerprint. Phase 9 populates.
  * @param roots               User-declared top-level `TypeId`s. Phase 10
  *                            populates.
  * @param typedConsts         Type-checked constants in declaration order.
  *                            Phase 8 populates.
  * @param diagnostics         Accumulated diagnostics from Phases 1-10.
  */
final case class ResolvedDomain(
  id: DomainId,
  meta: DomainMetadata,
  members: Map[TypeId, Member],
  userTypes: Map[TypeId, TypeDef],
  imports: Map[DomainId, Set[TypeId]],
  aliases: Map[AliasId, TypeId],
  consts: List[RawConst],
  loops: Set[Cycle[TypeId]] = Set.empty,
  flattenedStructs: Map[StructureId, FlatStruct] = Map.empty,
  crossDomainFlattenedStructs: Map[StructureId, FlatStruct] = Map.empty,
  parents: Map[TypeId, Set[InterfaceId]] = Map.empty,
  implementingDtos: Map[InterfaceId, Set[DTOId]] = Map.empty,
  ephemeralsOf: Map[TypeId, Set[TypeId]] = Map.empty,
  ephemeralOwner: Map[TypeId, TypeId] = Map.empty,
  fingerprints: Map[TypeId, Fingerprint] = Map.empty,
  domainFingerprint: Fingerprint = Fingerprint(ByteVector.empty),
  roots: Set[TypeId] = Set.empty,
  typedConsts: List[Const] = List.empty,
  diagnostics: Diagnostics = Diagnostics.empty,
)
