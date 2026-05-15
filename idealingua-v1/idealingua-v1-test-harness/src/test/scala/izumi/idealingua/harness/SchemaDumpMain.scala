package izumi.idealingua.harness

import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade}

/** Ad-hoc dump utility: emits the schema target into a temp dir for visual
  * inspection. Not part of any FROZEN harness contract; kept here so the
  * smoke spec class graph picks it up under `Test/runMain`.
  */
object SchemaDumpMain {
  def main(args: Array[String]): Unit = {
    val outDir = if (args.nonEmpty) java.nio.file.Paths.get(args(0)) else java.nio.file.Paths.get("/tmp/schema-dump")
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val options    = HarnessOptions.optionsFor(IDLLanguage.JsonSchema)
    val layouted   = new TypespaceCompilerBaseFacade(options).compile(loaded)
    java.nio.file.Files.createDirectories(outDir)
    layouted.emodules.foreach {
      case ExtendedModule.DomainModule(_, m) =>
        val full = m.id.path.foldLeft(outDir)(_.resolve(_)).resolve(m.id.name)
        java.nio.file.Files.createDirectories(full.getParent)
        java.nio.file.Files.write(full, m.content.getBytes(java.nio.charset.StandardCharsets.UTF_8))
      case _ => ()
    }
    println(s"dumped to $outDir")
  }
}
