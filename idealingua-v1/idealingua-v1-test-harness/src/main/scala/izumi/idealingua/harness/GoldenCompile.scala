package izumi.idealingua.harness

import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/** Shared compile pipeline for both `GoldenGenerator` and `GoldenVerifier`. */
private[harness] object GoldenCompile {

  val languages: Seq[IDLLanguage] = Seq(IDLLanguage.Scala, IDLLanguage.Typescript, IDLLanguage.CSharp)

  /** Additional virtual target produced by the Scala translator with
    * `emitMcpBridge = true` (PR-04 MCP Mb1). Lives at `<goldenRoot>/scala-mcp/`
    * so the four FROZEN harness contracts (and `GoldenVerifier`) sweep it
    * alongside the regular language goldens.
    */
  val mcpBridgeGoldenSubdir: String = "scala-mcp"

  /** Compiles the loaded corpus for all three target languages.
    *
    * Returns a map from the expected on-disk path (relative to `goldenRoot`)
    * to the UTF-8 content bytes. Only `DomainModule` entries are included —
    * `RuntimeModule` entries are excluded by design (runtime files are not
    * part of the golden corpus).
    *
    * PR-02 IMPL-10c (2026-05-12): the `TyperImpl` enum was deleted and the
    * legacy translator tree was retired; the new-typer pipeline is the sole
    * code path. The per-language `typer` lookup that selected `TyperImpl` is
    * gone — `HarnessOptions.optionsFor(lang)` now produces new-typer options
    * directly.
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

    // PR-04 MCP Mb1: Scala translator with `emitMcpBridge = true` produces a
    // second set of artefacts (per-service `<Name>Mcp.scala` source + a
    // `mcp/<Name>.mcp.json` resource module). These land under the
    // `scala-mcp/` subdir of `goldenRoot`. Mb1 limits scope to `Output.Singular`
    // dispatch — other variants stub with `mcpError(-32601, ...)`.
    val mcpManifest = HarnessOptions.scala.copy(emitMcpBridge = true)
    val mcpOptions = UntypedCompilerOptions(
      language           = IDLLanguage.Scala,
      target             = None,
      manifest           = mcpManifest,
      withBundledRuntime = false, // runtime files are not part of the golden corpus (same as regular passes above)
      providedRuntime    = None,
      zipOutput          = false,
    )
    val mcpLayouted = new TypespaceCompilerBaseFacade(mcpOptions).compile(loaded)
    for (emodule <- mcpLayouted.emodules) {
      emodule match {
        case ExtendedModule.DomainModule(_, module) if isMcpArtifact(module.id.name, module.id.path) =>
          val relPath = module.id.path.foldLeft(goldenRoot.resolve(mcpBridgeGoldenSubdir)) {
            (acc, seg) => acc.resolve(seg)
          }.resolve(module.id.name)
          val bytes = module.content.getBytes(StandardCharsets.UTF_8)
          builder += relPath -> bytes
        case _ =>
          // Skip non-MCP modules from this pass — they are byte-equal to the
          // regular Scala pass under `scala/` (with `emitMcpBridge = false`),
          // and would land on identical paths under the same `scala/` subtree
          // if included.
      }
    }

    builder.result()
  }

  /** Filter: keep only artefacts unique to the MCP bridge emission path —
    * `<ServiceName>Mcp.scala` source files and the `mcp/<ServiceName>.mcp.json`
    * classpath resource modules.
    */
  private def isMcpArtifact(name: String, path: Seq[String]): Boolean =
    name.endsWith("Mcp.scala") || (path == Seq("mcp") && name.endsWith(".mcp.json"))
}
