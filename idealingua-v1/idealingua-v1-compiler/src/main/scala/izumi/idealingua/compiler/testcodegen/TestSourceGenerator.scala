package izumi.idealingua.compiler.testcodegen

import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.BuildManifest.Common
import izumi.idealingua.model.publishing.manifests.{CSharpBuildManifest, CSharpProjectLayout, NugetOptions, SbtOptions, ScalaBuildManifest, ScalaProjectLayout, TypeScriptBuildManifest, TypeScriptProjectLayout, YarnOptions}
import izumi.idealingua.model.publishing.ProjectVersion
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}

/** Build-time source generator for the idealingua-v1 test harness corpus.
  *
  * Runs the in-process translator over the `defs/main-tests` corpus and writes
  * per-language output trees under `<genRoot>/`:
  *
  *   - `scala/`              — Scala translator output (Compile sources).
  *   - `scala-mcp/`          — Scala translator output with
  *                             `emitMcpBridge = true` (per-service
  *                             `<Name>Mcp.scala`; non-MCP modules from this
  *                             pass are byte-equal to `scala/` and skipped).
  *   - `scala-mcp-resources/mcp/` `*.mcp.json` files — bridge classpath
  *     resources (Compile resources).
  *   - `typescript/`         — TS translator output (TS driver consumes).
  *   - `csharp/`             — C# translator output (C# driver consumes).
  *
  * Also (re-)creates `<genRoot>/typescript/irt` as a symlink into the
  * runtime-rpc-typescript source tree so the TS driver can resolve
  * `import {...} from 'irt'`. If symlink creation fails (filesystem refuses
  * symlinks), the IRT tree is copied instead.
  *
  * Lives in the `idealingua-v1-compiler` module so it sits below
  * `idealingua-v1-test-harness` in the dependency graph — the harness
  * module's `Compile / sourceGenerators` invokes this via a forked JVM, so
  * generation does not depend on the harness module's own compile output
  * (which is the bootstrap defeater the legacy `GoldenCompile` pattern hit
  * once the committed goldens were removed).
  *
  * Generation is deterministic — same input corpus, same output bytes —
  * making the build reproducible.
  */
object TestSourceGenerator {

  val ScalaDir: String          = "scala"
  val ScalaMcpDir: String       = "scala-mcp"
  val ScalaMcpResources: String = "scala-mcp-resources"
  val TypescriptDir: String     = "typescript"
  val CsharpDir: String         = "csharp"

  /** Pinned manifest values used for fixture generation. Inlined here (rather
    * than imported from `idealingua-v1-test-harness`'s `HarnessOptions`) to
    * keep this module independent of the harness — otherwise the
    * sourceGenerators chain re-enters the cycle the codegen split was
    * designed to break.
    */
  private val pinnedVersion: ProjectVersion =
    ProjectVersion(version = "0.0.0", release = true, snapshotQualifier = "test")

  private val pinnedCommon: Common =
    BuildManifest.Common.example.copy(
      izumiVersion = "test-harness",
      version      = pinnedVersion,
    )

  private val scalaManifest: ScalaBuildManifest = ScalaBuildManifest(
    common = pinnedCommon,
    layout = ScalaProjectLayout.PLAIN,
    sbt    = SbtOptions.example.copy(scalaVersions = List("3.9.0", "3.8.3")),
  )

  private val typescriptManifest: TypeScriptBuildManifest = TypeScriptBuildManifest(
    common = pinnedCommon,
    layout = TypeScriptProjectLayout.PLAIN,
    yarn   = YarnOptions.example,
  )

  private val csharpManifest: CSharpBuildManifest = CSharpBuildManifest(
    common      = pinnedCommon,
    nuget       = NugetOptions.example,
    layout      = CSharpProjectLayout.PLAIN,
    enableNUnit = false,
  )

  private def manifestFor(lang: IDLLanguage): BuildManifest = lang match {
    case IDLLanguage.Scala      => scalaManifest
    case IDLLanguage.Typescript => typescriptManifest
    case IDLLanguage.CSharp     => csharpManifest
    case IDLLanguage.JsonSchema =>
      throw new IllegalArgumentException("JsonSchema is not part of the test source generation corpus")
  }

  private def optionsFor(lang: IDLLanguage): UntypedCompilerOptions =
    UntypedCompilerOptions(
      language           = lang,
      target             = None,
      manifest           = manifestFor(lang),
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )

  private val languages: Seq[IDLLanguage] =
    Seq(IDLLanguage.Scala, IDLLanguage.Typescript, IDLLanguage.CSharp)

  /** Generate every output tree under `genRoot`. Idempotent: clears each
    * per-output subdir before writing so stale entries from a prior run with
    * a different corpus do not persist. The `typescript/irt` symlink is
    * recreated after the directory clear.
    *
    * `corpusRoot` should point at
    * `<repoRoot>/idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source`.
    *
    * `repoRoot` is used solely to resolve the IRT runtime symlink target.
    */
  def generate(corpusRoot: Path, genRoot: Path, repoRoot: Path): Unit = {
    val loaded = loadCorpus(corpusRoot)

    val scalaOut          = genRoot.resolve(ScalaDir)
    val scalaMcpOut       = genRoot.resolve(ScalaMcpDir)
    val scalaMcpResources = genRoot.resolve(ScalaMcpResources)
    val typescriptOut     = genRoot.resolve(TypescriptDir)
    val csharpOut         = genRoot.resolve(CsharpDir)

    for (d <- Seq(scalaOut, scalaMcpOut, scalaMcpResources, typescriptOut, csharpOut)) {
      deleteRecursively(d)
    }

    val perLangRoots: Map[IDLLanguage, Path] = Map(
      IDLLanguage.Scala      -> scalaOut,
      IDLLanguage.Typescript -> typescriptOut,
      IDLLanguage.CSharp     -> csharpOut,
    )
    for (lang <- languages) {
      val layouted = new TypespaceCompilerBaseFacade(optionsFor(lang)).compile(loaded)
      val outRoot  = perLangRoots(lang)
      for (emodule <- layouted.emodules) emodule match {
        case ExtendedModule.DomainModule(_, module) =>
          val rel = module.id.path.foldLeft(outRoot)((acc, seg) => acc.resolve(seg)).resolve(module.id.name)
          Files.createDirectories(rel.getParent)
          Files.write(rel, module.content.getBytes(StandardCharsets.UTF_8))
        case _: ExtendedModule.RuntimeModule => ()
      }
    }

    // MCP bridge pass: Scala translator with `emitMcpBridge = true`.
    val mcpOptions = UntypedCompilerOptions(
      language           = IDLLanguage.Scala,
      target             = None,
      manifest           = scalaManifest.copy(emitMcpBridge = true),
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )
    val mcpLayouted = new TypespaceCompilerBaseFacade(mcpOptions).compile(loaded)
    for (emodule <- mcpLayouted.emodules) emodule match {
      case ExtendedModule.DomainModule(_, module) =>
        val name = module.id.name
        if (name.endsWith("Mcp.scala")) {
          val rel = module.id.path.foldLeft(scalaMcpOut)((acc, seg) => acc.resolve(seg)).resolve(name)
          Files.createDirectories(rel.getParent)
          Files.write(rel, module.content.getBytes(StandardCharsets.UTF_8))
        } else if (module.id.path == Seq("mcp") && name.endsWith(".mcp.json")) {
          val rel = scalaMcpResources.resolve("mcp").resolve(name)
          Files.createDirectories(rel.getParent)
          Files.write(rel, module.content.getBytes(StandardCharsets.UTF_8))
        }
      case _: ExtendedModule.RuntimeModule => ()
    }

    ensureIrtSymlink(genRoot, repoRoot)
  }

  /** Collect every emitted Scala source under `<genRoot>/{scala,scala-mcp}/`. */
  def collectScalaSources(genRoot: Path): Seq[Path] = {
    Seq(genRoot.resolve(ScalaDir), genRoot.resolve(ScalaMcpDir))
      .flatMap(walkSuffix(_, ".scala"))
      .sortBy(_.toString)
  }

  private def walkSuffix(root: Path, suffix: String): Seq[Path] = {
    if (!Files.exists(root)) return Seq.empty
    val stream = Files.walk(root)
    try {
      val buf = scala.collection.mutable.ArrayBuffer.empty[Path]
      val it  = stream.iterator()
      while (it.hasNext) {
        val p = it.next()
        if (Files.isRegularFile(p) && p.getFileName.toString.endsWith(suffix)) buf += p
      }
      buf.toSeq
    } finally stream.close()
  }

  private def loadCorpus(corpusRoot: Path): Seq[LoadedDomain.Success] = {
    val context  = new LocalModelLoaderContext(Seq(corpusRoot), Seq.empty[File])
    val resolver = new ModelResolver()
    val loaded   = context.loader.load()
    resolver.resolve(loaded).throwIfFailed().successful
  }

  private def ensureIrtSymlink(genRoot: Path, repoRoot: Path): Unit = {
    val target = repoRoot
      .resolve("idealingua-v1/idealingua-v1-runtime-rpc-typescript/src/main/resources/runtime/typescript/irt")
      .toAbsolutePath
    val linkDir = genRoot.resolve(TypescriptDir)
    Files.createDirectories(linkDir)
    val link = linkDir.resolve("irt")
    if (Files.isSymbolicLink(link)) {
      val existing = Files.readSymbolicLink(link)
      if (existing == target) return
      Files.delete(link)
    } else if (Files.exists(link)) {
      deleteRecursively(link)
    }
    try {
      Files.createSymbolicLink(link, target)
    } catch {
      case _: java.io.IOException =>
        copyTree(target, link)
    }
  }

  private def copyTree(src: Path, dst: Path): Unit = {
    if (!Files.exists(src)) return
    Files.createDirectories(dst)
    val stream = Files.walk(src)
    try {
      val it = stream.iterator()
      while (it.hasNext) {
        val p   = it.next()
        val rel = src.relativize(p)
        val q   = dst.resolve(rel.toString)
        if (Files.isDirectory(p)) {
          Files.createDirectories(q)
        } else {
          Files.createDirectories(q.getParent)
          Files.copy(p, q, StandardCopyOption.REPLACE_EXISTING)
        }
      }
    } finally stream.close()
  }

  /** Recursively delete files and directories under `path`. Symbolic links
    * are skipped — see `ensureIrtSymlink` for why the irt symlink must
    * survive a clear. Safe to call when `path` doesn't exist.
    */
  private def deleteRecursively(path: Path): Unit = {
    if (!Files.exists(path)) return
    val stream = Files.walk(path)
    try {
      stream
        .sorted(java.util.Comparator.reverseOrder[Path])
        .forEach {
          p =>
            val isRoot = p == path
            if (!isRoot && !Files.isSymbolicLink(p)) {
              val _ = Files.deleteIfExists(p)
            }
        }
    } finally stream.close()
  }
}
