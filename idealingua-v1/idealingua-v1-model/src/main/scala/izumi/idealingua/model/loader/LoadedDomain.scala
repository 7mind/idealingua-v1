package izumi.idealingua.model.loader

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.problems._

sealed trait LoadedDomain

object LoadedDomain {

  /** Successfully loaded + resolved domain.
    *
    * PR-02 IMPL-10d: the legacy `typespace: Typespace` field is gone — the
    * legacy typer + `TypespaceImpl` + verification subtree have been deleted.
    * Downstream consumers (translators, harness, golden compile) re-run the
    * new phase-based typer pipeline on `parsed` via `NewTyperPipeline.run`.
    */
  final case class Success(path: FSPath, parsed: DomainMeshResolved, warnings: Vector[IDLWarning]) extends LoadedDomain

  sealed trait Failure extends LoadedDomain

  sealed trait DiagnosableFailure extends Failure {
    def failures: Vector[IDLError]
    def warnings: Vector[IDLWarning]
  }

  final case class ParsingFailed(path: FSPath, message: String) extends Failure

  final case class PostVerificationFailure(issues: IDLDiagnostics) extends Failure

  final case class TyperFailed(path: FSPath, domain: DomainId, issues: IDLDiagnostics) extends DiagnosableFailure {
    override def failures: Vector[IDLError] = issues.issues

    override def warnings: Vector[IDLWarning] = issues.warnings
  }

  final case class ResolutionFailed(path: FSPath, domain: DomainId, issues: Vector[RefResolverIssue]) extends DiagnosableFailure {
    override def failures: Vector[IDLError] = issues

    override def warnings: Vector[IDLWarning] = Vector.empty
  }

  final case class VerificationFailed(path: FSPath, domain: DomainId, issues: IDLDiagnostics) extends DiagnosableFailure {
    override def failures: Vector[IDLError] = issues.issues

    override def warnings: Vector[IDLWarning] = issues.warnings
  }

}
