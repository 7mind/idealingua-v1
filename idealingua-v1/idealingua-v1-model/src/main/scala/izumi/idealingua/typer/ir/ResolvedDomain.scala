package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.model.il.ast.raw.defns.RawConst
import izumi.idealingua.model.il.ast.typed.DomainMetadata

/** Intermediate IR produced by typer Phases 1-4 (PR-02 IMPL-2).
  *
  * Phase 2 (`NameResolver`) constructs the initial value with all name-resolution
  * data filled in. Phase 3 (`AliasDealiaser`) fills in `aliases`. Phase 4
  * (`KindChecker`) only extends `diagnostics`. Phases 5-11 (deferred to IMPL-3)
  * extend the IR with structural data (`flattenedStructs`, `ephemeralsOf`,
  * `parents`, `roots`, `loops`, `fingerprints`, …) and ultimately yield the
  * frozen `Domain` value at Phase 11 (Assembler).
  *
  * Per the field-ordering invariant (master plan §4 / tasks C12/L3), every
  * field that flows to the wire is a `List`, never a `Set` or `Map`. The
  * `services`/`buzzers`/`streams`/`consts` lists preserve source declaration
  * order.
  *
  * Per the diagnostics-mode invariant (tasks C8/L1), every Phase 1-4 error
  * surfaces as an accumulated `Diagnostic` rather than a thrown exception.
  *
  * ---- F16 deviation note ----
  * `services`/`buzzers`/`streams` retain the IMPL-1 split shape (separate
  * `List[ServiceDef]` etc., not merged into `userTypes` / `members`). After the
  * F16/Option A1 TypeId widening, these IR values could be folded into
  * `Member.User` in IMPL-3; IMPL-2 keeps them split for parity with `Domain`.
  *
  * @param id          Domain identifier.
  * @param meta        Origin file path, direct inclusions/imports, and
  *                    domain-level annotations.
  * @param members     User types + builtins reachable from this domain. Phase 2
  *                    pre-seeds entries for referenced `Primitive` values as
  *                    `Member.Builtin` so that primitive lookups are total.
  * @param userTypes   Projection of `members` onto `Member.User` cases. Phase 2
  *                    populates concurrently with `members`.
  * @param services    RPC service definitions in declaration order.
  * @param buzzers     Buzzer definitions in declaration order.
  * @param streams     Streams definitions in declaration order.
  * @param imports     Transitively-imported types keyed by origin `DomainId`.
  *                    Phase 1 populates.
  * @param aliases     Resolved alias targets (alias-id → first non-alias
  *                    `TypeId` reached by chasing `.target`). Phase 3 populates.
  * @param consts      Raw constant definitions in declaration order. Phase 1
  *                    carries through; Phase 8 (IMPL-3) replaces with typed
  *                    `Const` values.
  * @param diagnostics Accumulated diagnostics from Phases 1-4.
  */
final case class ResolvedDomain(
  id: DomainId,
  meta: DomainMetadata,
  members: Map[TypeId, Member],
  userTypes: Map[TypeId, TypeDef],
  services: List[ServiceDef],
  buzzers: List[BuzzerDef],
  streams: List[StreamsDef],
  imports: Map[DomainId, Set[TypeId]],
  aliases: Map[AliasId, TypeId],
  consts: List[RawConst],
  diagnostics: Diagnostics,
)
