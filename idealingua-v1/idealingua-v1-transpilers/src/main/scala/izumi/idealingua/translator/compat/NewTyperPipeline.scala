package izumi.idealingua.translator.compat

import izumi.idealingua.model.il.ast.IDLPretyper
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.typer.ir.{Diagnostics, Domain}
import izumi.idealingua.typer.phase._

/** Wires the new phase-based typer (`izumi.idealingua.typer.phase.*` Phases 0-12)
  * end-to-end from a `DomainMeshResolved` to a frozen `Domain`.
  *
  * Bridge to the legacy throw-on-error contract: Phase 0-12 accumulate
  * `Diagnostics` rather than throw, but the legacy facade caller
  * (`TypespaceCompilerBaseFacade.compile`) expects exceptions.  When any
  * phase emits a non-empty `Diagnostics`, the pipeline raises a single
  * `IDLException` aggregating the diagnostic descriptions.
  *
  * TODO PR-02 IMPL-9: replace this throw bridge with routing through the
  * loader's `LoadedDomain.VerificationFailed` channel so the new typer's
  * structured diagnostics survive to the CLI.
  */
object NewTyperPipeline {

  /** Run all 13 phases (0 → 12) on a single domain's parsed AST and return
    * the frozen `Domain` IR.
    *
    * Throws `IDLException` aggregating all accumulated `Diagnostics` from any
    * phase if the pipeline encountered user-visible errors.
    */
  def run(parsed: DomainMeshResolved): Domain = {
    // Phase 0 takes DomainMeshLoaded (post-pretyper).  Reuse the legacy
    // pretyper to perform the raw→loaded conversion; it is a pure structural
    // projection (collect TLDs by category + clash check on imports vs locals).
    val loaded = new IDLPretyper(parsed).perform()

    val family   = IdealinguaFamilyManager(loaded)
    val scoped   = ScopeBuilder(parsed.id, loaded, family)
    val resolved = NameResolver(scoped)
    val dealiased    = AliasDealiaser(resolved)
    val kindChecked  = KindChecker(dealiased)
    val withCycles   = CycleDetector(kindChecked)
    val flattened    = StructuralFlattener(withCycles)
    val withEphem    = EphemeralSynthesizer(flattened)
    val withConsts   = ConstValueTyper(withEphem)
    val withFinger   = FingerprintCalculator(withConsts)
    val rooted       = RootExtractor(withFinger)
    val domain       = Assembler(rooted)

    val validatorDiags = Validator(domain)

    // `rooted.diagnostics` already accumulates Phases 1-10 inclusive: Phase 2
    // `NameResolver` initialises with `scoped.diagnostics ++ ...` and every
    // subsequent ResolvedDomain phase appends with `resolved.diagnostics ++ …`.
    // So we only add Phase 0 (family) and Phase 12 (validator) separately.
    val allDiags: Diagnostics =
      family.diagnostics ++
        rooted.diagnostics ++
        validatorDiags

    if (allDiags.issues.nonEmpty) {
      throw new IDLException(s"New typer diagnostics for ${parsed.id}:\n${allDiags.issues.mkString("\n  - ", "\n  - ", "")}")
    }

    domain
  }
}
