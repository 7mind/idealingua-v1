package izumi.idealingua.model.loader

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.problems._
import izumi.idealingua.typer.ir.{Diagnostics, Domain}

sealed trait LoadedDomain

object LoadedDomain {

  /** Successfully loaded + resolved + typed domain.
    *
    * PR-02 IMPL-10d: the legacy `typespace: Typespace` field is gone — the
    * legacy typer + `TypespaceImpl` + verification subtree have been deleted.
    *
    * PR-02 IMPL-13: `domain: Domain` is materialised at load time by the
    * resolver invoking `NewTyperPipeline.run` on each successfully-resolved
    * mesh.  Translators read this field directly; the pipeline is no longer
    * re-run lazily per translator invocation.
    */
  final case class Success(path: FSPath, parsed: DomainMeshResolved, domain: Domain, warnings: Vector[IDLWarning]) extends LoadedDomain

  sealed trait Failure extends LoadedDomain

  sealed trait DiagnosableFailure extends Failure {
    def failures: Vector[IDLError]
    def warnings: Vector[IDLWarning]
  }

  final case class ParsingFailed(path: FSPath, message: String) extends Failure

  final case class PostVerificationFailure(issues: IDLDiagnostics) extends Failure

  final case class ResolutionFailed(path: FSPath, domain: DomainId, issues: Vector[RefResolverIssue]) extends DiagnosableFailure {
    override def failures: Vector[IDLError] = issues

    override def warnings: Vector[IDLWarning] = Vector.empty
  }

  /** New-typer pipeline rejection.
    *
    * PR-02 IMPL-13: replaces the IMPL-6 C8 throw-bridge.  When any of the
    * 13 typer phases (0-12) emits a non-empty `Diagnostics`, the resolver
    * produces this value instead of letting `NewTyperPipeline.run` raise
    * `IDLException`.  Structured diagnostics — each anchored to an
    * `InputPosition` — survive to the CLI through `LoadedModels.collectFailures`.
    */
  final case class VerificationFailed(path: FSPath, domain: DomainId, diagnostics: Diagnostics, warnings: Vector[IDLWarning]) extends Failure

}
