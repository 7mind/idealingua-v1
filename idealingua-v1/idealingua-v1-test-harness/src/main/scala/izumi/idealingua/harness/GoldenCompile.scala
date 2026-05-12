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
    * PR-02 IMPL-10b-fix (2026-05-12): all three languages are now on
    * `TyperImpl.NewTyper`. The cross-domain mixin defect that pinned
    * TypeScript on `TyperImpl.Legacy` (latent `D1.toM2Serialized` body
    * dropping the foreign `f2` field) was closed by surfacing foreign
    * harvested flat structs through `Domain.crossDomainFlattenedStructs`
    * and consulting them from `DomainTSStruct.structureOf`. The 17 other
    * TS divergences (introspector field-order + module-export-order) are
    * absorbed into regenerated goldens; `verifyGoldens`, `runWireFixtures`,
    * and `runCrossLangInterop` all stay green on both Scala 2.13.18 and
    * 3.8.3.
    */
  def compileAll(loaded: Seq[LoadedDomain.Success], goldenRoot: Path): Map[Path, Array[Byte]] = {
    val builder = Map.newBuilder[Path, Array[Byte]]

    for (lang <- languages) {
      val typer = lang match {
        case IDLLanguage.Scala      => TyperImpl.NewTyper
        case IDLLanguage.CSharp     => TyperImpl.NewTyper
        case IDLLanguage.Typescript => TyperImpl.NewTyper
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
