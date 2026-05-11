package izumi.idealingua.translator

/** Selects between the legacy IDL typer (`IDLPostTyper`) and the new
  * phase-based typer pipeline (`izumi.idealingua.typer.phase.*`).
  *
  * Threaded through `UntypedCompilerOptions.typerImpl` and dispatched in
  * `TypespaceCompilerBaseFacade.compile`.  Default is `Legacy` everywhere
  * (CLI, harness, fixtures) until the new pipeline reaches byte-parity in
  * a later PR-02 implementation step.
  */
sealed trait TyperImpl

object TyperImpl {
  case object Legacy extends TyperImpl {
    override val toString: String = "legacy"
  }

  case object NewTyper extends TyperImpl {
    override val toString: String = "new"
  }

  def parse(s: String): TyperImpl = {
    (s.trim.toLowerCase: @unchecked) match {
      case Legacy.toString   => Legacy
      case NewTyper.toString => NewTyper
    }
  }
}
