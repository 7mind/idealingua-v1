package izumi.idealingua.harness

import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TyperImpl, TypespaceCompilerBaseFacade}

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/** Shared compile pipeline for both `GoldenGenerator` and `GoldenVerifier`. */
private[harness] object GoldenCompile {

  val languages: Seq[IDLLanguage] = Seq(IDLLanguage.Scala, IDLLanguage.Typescript, IDLLanguage.CSharp)

  /** Compiles the loaded corpus for all three target languages.
    *
    * Returns a map from the expected on-disk path (relative to `goldenRoot`)
    * to the UTF-8 content bytes. Only `DomainModule` entries are included —
    * `RuntimeModule` entries are excluded by design (runtime files are not
    * part of the golden corpus).
    *
    * PR-02 IMPL-10b: the per-language typer dispatch is preserved because
    * `TyperImpl.NewTyper` on the TypeScript backend currently diverges from
    * the on-disk Legacy goldens (latent defect surfaced when the byte-parity
    * apparatus was retired — `D1.M2-slice` drops the `f2` field, plus 17
    * other modules diverge in introspector field-order and other emission
    * shapes). Scala + C# are on `TyperImpl.NewTyper` (IMPL-9 / IMPL-7c.2 M5
    * production-path swaps). TypeScript stays on `TyperImpl.Legacy` until
    * the IMPL-7b NewTyper-TS divergences are diagnosed and reconciled —
    * tracked as a post-IMPL-10b followup so the FROZEN harness contracts
    * stay green.
    */
  def compileAll(loaded: Seq[LoadedDomain.Success], goldenRoot: Path): Map[Path, Array[Byte]] = {
    val builder = Map.newBuilder[Path, Array[Byte]]

    for (lang <- languages) {
      val typer = lang match {
        case IDLLanguage.Scala      => TyperImpl.NewTyper
        case IDLLanguage.CSharp     => TyperImpl.NewTyper
        case IDLLanguage.Typescript => TyperImpl.Legacy
      }
      val options  = HarnessOptions.optionsFor(lang, typer)
      val layouted = new TypespaceCompilerBaseFacade(options).compile(loaded)

      for (emodule <- layouted.emodules) {
        emodule match {
          case ExtendedModule.DomainModule(_, module) =>
            val relPath = module.id.path.foldLeft(goldenRoot.resolve(lang.toString)) {
              (acc, seg) => acc.resolve(seg)
            }.resolve(module.id.name)
            val bytes = module.content.getBytes(StandardCharsets.UTF_8)
            builder += relPath -> bytes
          case _: ExtendedModule.RuntimeModule =>
          // excluded by design: runtime files are not part of the golden corpus
        }
      }
    }

    builder.result()
  }
}
