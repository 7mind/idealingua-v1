package izumi.idealingua.typer

import izumi.idealingua.model.il.ast.IDLPretyper
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.typer.ir.{Diagnostics, Domain}
import izumi.idealingua.typer.phase._

/** Wires the new phase-based typer (`izumi.idealingua.typer.phase.*` Phases 0-12)
  * end-to-end from a `DomainMeshResolved` to a frozen `Domain`.
  *
  * PR-02 IMPL-13:
  *   - Moved out of `translator.compat` into the typer module so the loader
  *     (`idealingua-v1-core` `ModelResolver`) can invoke it without an inverse
  *     module dependency on `idealingua-v1-transpilers`.
  *   - The IMPL-6 C8 throw-bridge is retired.  When any phase emits a
  *     non-empty `Diagnostics`, this returns `Left(diagnostics)` so the loader
  *     can route the rejection through `LoadedDomain.VerificationFailed`
  *     rather than raising `IDLException`.  Structured diagnostics (each
  *     anchored to an `InputPosition`) survive to the CLI through
  *     `LoadedModels.collectFailures`.
  *
  * Non-fatal diagnostics: per `Diagnostic.CyclicDomainImport` scaladoc, cycles
  * in the cross-domain import graph are advisory — `FamilyIndex.loadOrder`
  * still produces a deterministic, complete ordering and per-domain phases
  * operate independently. The legacy typer accepted cyclic cross-domain
  * imports (the `defs-special/scala-only/idltest/crossimports{1,2,3}`
  * integration fixture is the regression oracle), so this pipeline filters
  * `CyclicDomainImport` out of the fatal-issue set.
  */
object NewTyperPipeline {

  /** Run all 13 phases (0 → 12) on a single domain's parsed AST.
    *
    * @return `Right(domain)` on success; `Left(diagnostics)` if any phase
    *         reported user-visible errors.
    */
  def run(parsed: DomainMeshResolved): Either[Diagnostics, Domain] = {
    // Phase 0 takes DomainMeshLoaded (post-pretyper).  Reuse the legacy
    // pretyper to perform the raw→loaded conversion; it is a pure structural
    // projection (collect TLDs by category + clash check on imports vs locals).
    val loaded = new IDLPretyper(parsed).perform()

    val family   = IdealinguaFamilyManager(loaded)
    val scoped   = ScopeBuilder(parsed.id, loaded, family)
    val resolved = NameResolver(scoped, family)
    val dealiased    = AliasDealiaser(resolved)
    val kindChecked  = KindChecker(dealiased)
    val withCycles   = CycleDetector(kindChecked)
    // F8 fix (IMPL-7a.2): EphemeralSynthesizer must run before
    // StructuralFlattener so that synthesized input/output/mirror DTOs are
    // visible to the flattener and receive a `FlatStruct` entry. The
    // flattener now sources structs from both `userTypes` and
    // `members.collect{ case Ephemeral(...) }` in one pass.
    val withEphem    = EphemeralSynthesizer(withCycles)
    // PR-02 IMPL-7a.2-Fj: pass the cross-domain `FamilyIndex` so the flattener
    // can resolve foreign mixin parents (e.g. `& otherDomain#M`) and include
    // their fields in the local type's `FlatStruct`. Without this the renderer
    // emits `case class D(local_only)` for a `data D { & local; & foreign#M }`
    // declaration, dropping `M`'s fields — see compile-gate error on
    // `idltest/aliases/D1.scala` (1 → 0 errors).
    val flattened    = StructuralFlattener(withEphem, family)
    val withConsts   = ConstValueTyper(flattened)
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

    // Filter out non-fatal diagnostics (see scaladoc): `CyclicDomainImport`
    // is advisory; downstream phases still produce a usable `Domain`.
    val fatal = allDiags.issues.filterNot {
      case _: izumi.idealingua.typer.ir.Diagnostic.CyclicDomainImport => true
      case _                                                          => false
    }

    if (fatal.nonEmpty) {
      Left(izumi.idealingua.typer.ir.Diagnostics(fatal))
    } else {
      Right(domain)
    }
  }
}
