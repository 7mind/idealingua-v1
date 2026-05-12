package izumi.idealingua.harness

import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TyperImpl, TypespaceCompilerBaseFacade}

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/** Shared compile pipeline for both GoldenGenerator and GoldenVerifier. */
private[harness] object GoldenCompile {

  val languages: Seq[IDLLanguage] = Seq(IDLLanguage.Scala, IDLLanguage.Typescript, IDLLanguage.CSharp)

  /**
    * Compiles the loaded corpus for all three target languages.
    * Returns a map from the expected on-disk path (relative to goldenRoot) to the UTF-8 content bytes.
    * Only DomainModule entries are included — RuntimeModule entries are excluded.
    *
    * PR-02 IMPL-9: `scalaTyper` defaults to `TyperImpl.NewTyper` — the IMPL-9
    * flip makes the new typer the canonical default for the Scala backend.
    * `regenerateGoldens` (via `RegenerateMain`) now produces new-typer Scala
    * goldens; the previously-opt-in `regenerateGoldensNewTyper` sbt task is now
    * redundant but retained for callers that pin the target typer explicitly.
    * The harness's standard `Compile/compile` then transitively type-checks
    * every emitted module across the 28-domain corpus, surfacing any type
    * error in the new-typer Scala backend that bytewise-equality
    * (`ScalaTranslatorByteParitySpec`) alone cannot detect. TS / C# stay on
    * the Legacy typer until their Phase B ports land (IMPL-10 / IMPL-11 gating).
    */
  def compileAll(
    loaded: Seq[LoadedDomain.Success],
    goldenRoot: Path,
    scalaTyper: TyperImpl = TyperImpl.NewTyper,
  ): Map[Path, Array[Byte]] = {
    val builder = Map.newBuilder[Path, Array[Byte]]

    for (lang <- languages) {
      val typer = lang match {
        case IDLLanguage.Scala => scalaTyper
        case _                 => TyperImpl.Legacy
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
