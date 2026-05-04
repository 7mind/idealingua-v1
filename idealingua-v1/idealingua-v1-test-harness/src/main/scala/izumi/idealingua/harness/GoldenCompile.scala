package izumi.idealingua.harness

import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade}

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/** Shared compile pipeline for both GoldenGenerator and GoldenVerifier. */
private[harness] object GoldenCompile {

  val languages: Seq[IDLLanguage] = Seq(IDLLanguage.Scala, IDLLanguage.Typescript, IDLLanguage.CSharp)

  /**
    * Compiles the loaded corpus for all three target languages.
    * Returns a map from the expected on-disk path (relative to goldenRoot) to the UTF-8 content bytes.
    * Only DomainModule entries are included — RuntimeModule entries are excluded.
    */
  def compileAll(loaded: Seq[LoadedDomain.Success], goldenRoot: Path): Map[Path, Array[Byte]] = {
    val builder = Map.newBuilder[Path, Array[Byte]]

    for (lang <- languages) {
      val options  = HarnessOptions.optionsFor(lang)
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
