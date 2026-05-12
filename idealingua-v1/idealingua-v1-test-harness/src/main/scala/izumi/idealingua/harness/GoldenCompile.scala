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
    * PR-02 IMPL-7a.2 IMPL-9 compile gate (opt-in): `scalaTyper` is `TyperImpl.Legacy`
    * by default (preserves the stable legacy goldens that sbt compiles via
    * `Compile / unmanagedSourceDirectories += golden/scala`). The
    * `regenerateGoldensNewTyper` sbt task passes `TyperImpl.NewTyper`,
    * regenerating the goldens as the new-typer output. The harness's standard
    * `Compile/compile` then transitively type-checks every emitted module
    * across the 28-domain corpus, surfacing any type error in the new-typer
    * Scala backend that bytewise-equality (`ScalaTranslatorByteParitySpec`)
    * alone cannot detect. TS / C# stay on the Legacy typer in both modes —
    * IMPL-7b / IMPL-7c only land Phase A delegation (TS and C# already
    * re-derive a legacy Typespace internally), so the Layer B fixtures keep
    * exercising the legacy frontend on those languages until Phase B lands.
    */
  def compileAll(
    loaded: Seq[LoadedDomain.Success],
    goldenRoot: Path,
    scalaTyper: TyperImpl = TyperImpl.Legacy,
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
