package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.TypeId
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
