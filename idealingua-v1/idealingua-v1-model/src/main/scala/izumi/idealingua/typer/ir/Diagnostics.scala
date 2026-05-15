package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.{DomainId, TypeId}
import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.model.il.ast.InputPosition

/** A single diagnostic message emitted by a typer phase.
  *
  * Every diagnostic carries a source position so that error messages can be
  * anchored to the originating `.domain` file. Subtypes for Phases 1-4 live
  * in this file because the trait is sealed (Scala disallows extending a
  * sealed trait from a different compilation unit).
  *
  * Corresponds to PR-01 Lesson 5: typer phases return `Either[NEList[Diagnostic],
  * A]` rather than throwing `IDLException` on user-visible errors.
  */
sealed trait Diagnostic {
  def position: InputPosition
}

object Diagnostic {

  // --- Phase 0 (IdealinguaFamilyManager) ------------------------------------

  /** A cycle was detected in the domain import graph.
    *
    * Per C11/L2: emitted as a diagnostic (non-fatal); `FamilyIndex.loadOrder`
    * still contains the cycling domains in a deterministic (alphabetic) order.
    */
  final case class CyclicDomainImport(cycle: List[DomainId], position: InputPosition) extends Diagnostic

  // --- Phase 1 (ScopeBuilder) -----------------------------------------------

  /** A local name is declared more than once in the same domain. */
  final case class ScopeCollision(name: String, existing: TypeId, duplicate: TypeId, position: InputPosition) extends Diagnostic

  /** An imported alias collides with a locally-declared simple name. Mirrors
    * the legacy `IDLPretyper` clash check at `IDLTyper.scala:56-58`.
    */
  final case class ImportNameClashesWithLocal(name: String, local: TypeId, imported: TypeId, position: InputPosition) extends Diagnostic

  /** A `foreign` type was encountered. Per C7 the legacy typer throws; the new
    * IR records a non-fatal diagnostic and skips the entry.
    */
  final case class ForeignTypeUnsupported(name: String, position: InputPosition) extends Diagnostic

  // --- Phase 2 (NameResolver) -----------------------------------------------

  /** A type reference does not resolve to any local, imported, or builtin type. */
  final case class UnknownTypeRef(reference: String, position: InputPosition) extends Diagnostic

  /** A generic type was instantiated with the wrong number of type arguments. */
  final case class WrongGenericArity(name: String, expected: Int, actual: Int, position: InputPosition) extends Diagnostic

  // --- Phase 3 (AliasDealiaser) ---------------------------------------------

  /** A cycle was detected in the alias graph. */
  final case class CyclicAlias(cycle: List[AliasId], position: InputPosition) extends Diagnostic

  /** An alias's `target` did not resolve to a known type (Phase 2 should have
    * already emitted `UnknownTypeRef`; this is the alias-graph view of the
    * same condition).
    */
  final case class AliasTargetUnresolved(alias: AliasId, position: InputPosition) extends Diagnostic

  // --- Phase 4 (KindChecker) ------------------------------------------------

  /** An `Identifier` field is typed as something other than `Primitive`,
    * `IdentifierId`, or `EnumId`.
    */
  final case class BadIdentifierFieldType(identifier: TypeId, field: String, badType: TypeId, position: InputPosition) extends Diagnostic

  /** A mixin (concept) target resolves to a non-structural type (i.e. not
    * `DTOId` / `InterfaceId`).
    */
  final case class BadMixinTarget(owner: TypeId, badTarget: TypeId, position: InputPosition) extends Diagnostic

  /** An ADT branch references either an unknown type or another ADT (nested
    * ADTs are not supported).
    */
  final case class NestedAdtMemberUnsupported(adt: TypeId, branch: TypeId, position: InputPosition) extends Diagnostic

  // --- Phase 5 (CycleDetector) ----------------------------------------------

  /** Non-broken cycle through field/member references (no `Option`/`List`/etc.
    * container indirection in the cycle path).
    */
  final case class CyclicUsage(members: List[TypeId], position: InputPosition) extends Diagnostic

  /** Cycle entirely through inheritance edges (interface extends + DTO mixin). */
  final case class CyclicInheritance(members: List[TypeId], position: InputPosition) extends Diagnostic

  /** Marker emitted alongside `CyclicUsage`/`CyclicInheritance` when no
    * container-typed back-edge breaks the cycle (i.e. all back-edges are
    * direct references).  Distinct from `CyclicUsage` for downstream
    * consumers that need to distinguish "terminating recursion" (legal) from
    * "hard cycle" (illegal).
    */
  final case class NonTerminatingCycle(members: List[TypeId], position: InputPosition) extends Diagnostic

  // --- Phase 6 (StructuralFlattener) ----------------------------------------

  /** Two fields of the same name on the same flattened struct have
    * incompatible types (hard conflict).
    */
  final case class FieldNameConflict(owner: TypeId, fieldName: String, candidates: List[TypeId], position: InputPosition) extends Diagnostic

  /** A structural mixin reference points at a type that is not registered as
    * a user type.  (Distinct from `BadMixinTarget`, which fires when the
    * reference resolves to the wrong KIND of type.)
    */
  final case class MissingMixin(owner: TypeId, missing: TypeId, position: InputPosition) extends Diagnostic

  // --- Phase 7 (EphemeralSynthesizer) ---------------------------------------

  /** A synthesized ephemeral name collides with a user-declared type id or
    * with a previously-synthesized ephemeral id.
    */
  final case class EphemeralNameCollision(synthesized: TypeId, existing: TypeId, position: InputPosition) extends Diagnostic

  // --- Phase 8 (ConstValueTyper) --------------------------------------------

  /** A const's declared `RawVal` shape does not match its declared target
    * `TypeId` (e.g. `CInt` assigned to a `TString` target).
    */
  final case class ConstTypeMismatch(constName: String, expected: TypeId, actualKind: String, position: InputPosition) extends Diagnostic

  /** A const initializer for a DTO/Interface target is missing a required
    * field.
    */
  final case class ConstFieldMissing(constName: String, owner: TypeId, fieldName: String, position: InputPosition) extends Diagnostic

  /** A const initializer for a DTO/Interface target supplies an extra field
    * that is not declared on the target.
    */
  final case class ConstFieldUnknown(constName: String, owner: TypeId, fieldName: String, position: InputPosition) extends Diagnostic

  /** A const value is structurally malformed (e.g. could not resolve target
    * type-id, or the value's nested structure is not a const-value shape).
    */
  final case class BadConstValue(constName: String, description: String, position: InputPosition) extends Diagnostic

  // --- Phase 12 (Validator) -------------------------------------------------

  /** A type name violates naming conventions (lowercase first char, too short,
    * or uses a reserved prefix such as "Iz", "IRT", "IDL").
    */
  final case class BadNamingConvention(typeId: TypeId, reason: String, position: InputPosition) extends Diagnostic

  /** An enum declares two or more members with the same value string. */
  final case class DuplicateEnumMember(enumId: TypeId, memberName: String, position: InputPosition) extends Diagnostic

  /** An ADT declares two or more branches with the same wire name. */
  final case class DuplicateAdtBranch(adtId: TypeId, branchName: String, position: InputPosition) extends Diagnostic

  /** An ADT branch resolves to a primitive (builtin) type rather than a user
    * type (DTO, Interface, or Identifier).
    */
  final case class PrimitiveAdtMember(adtId: TypeId, branchTypeId: TypeId, position: InputPosition) extends Diagnostic
}

/** Accumulator for zero or more `Diagnostic` values.
  *
  * Designed for monoid-style aggregation across phases: `++` merges two
  * accumulators, `isEmpty` guards short-circuit paths.
  */
final case class Diagnostics(issues: Vector[Diagnostic]) {
  def isEmpty: Boolean = issues.isEmpty

  def ++(other: Diagnostics): Diagnostics = Diagnostics(issues ++ other.issues)
}

object Diagnostics {
  val empty: Diagnostics = Diagnostics(Vector.empty)
}
