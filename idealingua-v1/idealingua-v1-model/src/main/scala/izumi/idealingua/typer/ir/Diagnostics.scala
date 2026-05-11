package izumi.idealingua.typer.ir

import izumi.idealingua.model.il.ast.InputPosition

/** A single diagnostic message emitted by a typer phase.
  *
  * Every diagnostic carries a source position so that error messages can be
  * anchored to the originating `.domain` file. Subtypes are defined by each
  * phase; this sealed trait is the common accumulation target (see
  * `Diagnostics`).
  *
  * Corresponds to PR-01 Lesson 5: typer phases return `Either[NEList[Diagnostic],
  * A]` rather than throwing `IDLException` on user-visible errors.
  */
sealed trait Diagnostic {
  def position: InputPosition
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
