package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.{StructureId, TypeId}
import izumi.idealingua.model.common.TypeId.{AliasId, DTOId, InterfaceId}
import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.typed.DomainMetadata

/** The fully-materialised, frozen IR for a single compiled domain.
  *
  * Produced by Phase 11 (Assembler) and consumed read-only by all downstream
  * phases (Validator, translators). Every field is an immutable Scala
  * collection; no phase may mutate this value after construction (see master
  * plan §4 "Frozen-after-assembly invariant").
  *
  * Maps keyed by `TypeId` (`members`, `fingerprints`, etc.) are unordered by
  * definition.  All ordered data lives in `List`-typed fields of the contained
  * `TypeDef`/`Struct` values (see master plan §4 "Field-ordering invariant").
  *
  * After F16 absorption (PR-02 IMPL-3), `TypeDef` covers all user-declared
  * categories (structural + service family), and services/buzzers/streams flow
  * through `members` as `Member.User(TypeDef.Service(...))` etc.  `Domain` no
  * longer carries separate service-family lists.
  *
  * @param id                  Domain identifier.
  * @param meta                Origin file path, direct inclusions/imports, and
  *                            domain-level annotations.
  * @param members             Every `Member` reachable from this domain:
  *                            user structural and service-family types,
  *                            synthesized ephemerals, and builtins. Keyed by
  *                            `TypeId`.
  * @param roots               User-declared top-level `TypeId`s (structural
  *                            types only; excludes ephemerals and builtins).
  * @param ephemeralsOf        Maps each service/buzzer/interface/DTO owner to
  *                            the set of ephemeral `TypeId`s it owns.
  * @param ephemeralOwner      Inverse: maps each ephemeral `TypeId` back to its
  *                            owning user type's `TypeId`.
  * @param flattenedStructs    Pre-materialised flat struct for every
  *                            `StructureId` (DTO + Interface).  O(1) lookup.
  * @param parents             Maps each structural `TypeId` to the set of
  *                            `InterfaceId`s it directly or transitively
  *                            extends.
  * @param implementingDtos    Maps each `InterfaceId` to the set of `DTOId`s
  *                            that (directly or transitively) implement it.
  * @param loops               Detected cycles in the type reference graph.
  * @param fingerprints        SHA-256 fingerprint for each `TypeId`.
  * @param domainFingerprint   SHA-256 fingerprint for the entire domain.
  * @param imports             Transitively-imported types keyed by origin
  *                            `DomainId`.
  * @param consts              Type-checked constant definitions in declaration
  *                            order (`List` — see Field-ordering invariant).
  * @param aliases             Resolved alias targets: maps each `AliasId` to
  *                            the concrete (non-alias) `TypeId` it expands to.
  * @param userTypes           Projection of `members` onto `Member.User` cases.
  */
final case class Domain(
  id: DomainId,
  meta: DomainMetadata,
  members: Map[TypeId, Member],
  roots: Set[TypeId],
  ephemeralsOf: Map[TypeId, Set[TypeId]],
  ephemeralOwner: Map[TypeId, TypeId],
  flattenedStructs: Map[StructureId, FlatStruct],
  parents: Map[TypeId, Set[InterfaceId]],
  implementingDtos: Map[InterfaceId, Set[DTOId]],
  loops: Set[Cycle[TypeId]],
  fingerprints: Map[TypeId, Fingerprint],
  domainFingerprint: Fingerprint,
  imports: Map[DomainId, Set[TypeId]],
  consts: List[Const],
  aliases: Map[AliasId, TypeId],
  userTypes: Map[TypeId, TypeDef],
)
