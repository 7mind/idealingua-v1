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
  * ---- Deviation from master plan §4 pseudocode ----
  * The master plan shows `TypeDef` containing `Service`, `Buzzer`, `Streams`
  * cases and uses them in `Domain.members: Map[TypeId, Member]`.  In the legacy
  * model, `ServiceId`, `BuzzerId`, `StreamsId` are plain case classes that do
  * NOT extend the sealed `TypeId` trait.  Adding new `TypeId` subtypes in
  * `TypeId.scala` would break exhaustive pattern matches in `IDLTyper.scala`
  * and `InheritanceQueriesImpl.scala` (match-analysis warnings promoted to
  * errors via `-Wconf:cat=other-match-analysis:error` in the Scala 2.13 build),
  * and those files are off-limits in IMPL-1.
  *
  * Resolution (IMPL-1): `TypeDef` covers only structural types (DTO, Interface,
  * Identifier, Adt, Enum, Alias); services, buzzers, and streams live in
  * dedicated `List[ServiceDef]`, `List[BuzzerDef]`, `List[StreamsDef]` fields.
  * The corresponding `ephemeralsOf`, `ephemeralOwner`, and `members` entries for
  * service-owned ephemerals still key on synthesized `DTOId` values (which ARE
  * `TypeId` subtypes), so the `Map[TypeId, Member]` type is preserved.  IMPL-2+
  * will revisit first-class service membership once the sealed-TypeId tension
  * is resolved (e.g. by introducing IR-level `TypeId` subtypes for services in a
  * coordinated change that also updates the pattern-match sites).
  * --------------------------------------------------
  *
  * @param id                  Domain identifier.
  * @param meta                Origin file path, direct inclusions/imports, and
  *                            domain-level annotations.
  * @param members             Every `Member` reachable from this domain:
  *                            user structural types, synthesized ephemerals,
  *                            and builtins.  Keyed by `TypeId`.  Does NOT
  *                            include service/buzzer/streams entries (see
  *                            deviation note above).
  * @param roots               User-declared top-level `TypeId`s (structural
  *                            types only; excludes ephemerals and builtins).
  * @param ephemeralsOf        Maps each service/buzzer `DTOId` owner key to
  *                            the set of ephemeral `DTOId`s it owns.  (Using
  *                            `DTOId` as the owner key is the practical
  *                            workaround for the sealed-TypeId issue; IMPL-3
  *                            will refine.)
  * @param ephemeralOwner      Inverse: maps each ephemeral `DTOId` back to its
  *                            owning service/buzzer's representative `DTOId`.
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
  * @param services            RPC service definitions in declaration order.
  * @param buzzers             Buzzer definitions in declaration order.
  * @param streams             Streams definitions in declaration order
  *                            (deprecated-but-kept-working per C5/Q1).
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
  services: List[ServiceDef],
  buzzers: List[BuzzerDef],
  streams: List[StreamsDef],
)
