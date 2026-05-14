package izumi.idealingua.harness

import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}

/** Ad-hoc dump utility: emits the MCP / http4s bridge into a temp dir for
  * visual inspection or downstream smoke compilation. Not part of any FROZEN
  * harness contract; kept here so the smoke spec class graph picks it up
  * under `Test/runMain`.
  *
  * Default output: `/tmp/mcp-bridge-dump/`.
  */
object McpBridgeDumpMain {
  def main(args: Array[String]): Unit = {
    val outDir     = if (args.nonEmpty) java.nio.file.Paths.get(args(0)) else java.nio.file.Paths.get("/tmp/mcp-bridge-dump")
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val manifest   = HarnessOptions.scala.copy(emitMcpBridge = true)
    val options = UntypedCompilerOptions(
      language           = IDLLanguage.Scala,
      target             = None,
      manifest           = manifest,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )
    val layouted = new TypespaceCompilerBaseFacade(options).compile(loaded)
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
