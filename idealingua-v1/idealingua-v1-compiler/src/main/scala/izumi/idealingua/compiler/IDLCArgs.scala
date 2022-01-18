package izumi.idealingua.compiler

import java.io.File
import java.nio.file.{Path, Paths}

import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import izumi.fundamentals.platform.cli.model.schema._
import izumi.fundamentals.platform.cli.{CLIParserImpl, ParserFailureHandler}

case class LanguageOpts(
                         id: String,
                         withRuntime: Boolean,
                         zip: Boolean,
                         target: Option[Path],
                         manifest: Option[File],
                         credentials: Option[File],
                         extensions: List[String],
                         overrides: Map[String, String],
                       )


case class IDLCArgs(
                     root: Path,
                     source: Path,
                     overlay: Path,
                     target: Path,
                     languages: List[LanguageOpts],
                     init: Option[Path],
                     versionOverlay: Option[Path],
                     overrides: Map[String, String],
                     publish: Boolean = false
                   )

object IDLCArgs {
  def default: IDLCArgs = IDLCArgs(
    Paths.get(".")
    , Paths.get("source")
    , Paths.get("overlay")
    , Paths.get("target")
    , List.empty
    , None
    , None
    , Map.empty
  )

  object P extends ParserDef {
    final val rootDir = arg("root", "r", "root directory", "<path>")
    final val sourceDir = arg("source", "s", "source directory", "<path>")
    final val targetDir = arg("target", "t", "target directory", "<path>")
    final val overlayDir = arg("overlay", "o", "overlay directory", "<path>")
    final val overlayVersionFile = arg("overlay-version", "v", "version file", "<path>")
    final val define = arg("define", "d", "define value", "const.name=value")
    final val publish = flag("publish", "p", "build and publish generated code")


  }

  object LP extends ParserDef {
    final val target = arg("target", "t", "lang target directory", "<path>")
    final val manifest = arg("manifest", "m", "manifest file", "<path>")
    final val credentials = arg("credentials", "cr", "credentials file", "<path>")
    final val extensionSpec = arg("extensions", "e", "extensions spec", "{* | -AnyvalExtension;-CirceDerivationTranslatorExtension}")
    final val noRuntime = flag("disable-runtime", "nr", "don't include builtin runtime")
    final val noZip = flag("disable-zip", "nz", "don't zip outputs")
    final val define = arg("define", "d", "define value", "const.name=value")
  }

  object IP extends ParserDef {}

  def parseUnsafe(args: Array[String]): IDLCArgs = {
    val parsed = new CLIParserImpl().parse(args) match {
      case Left(value) =>
        ParserFailureHandler.TerminatingHandler.onParserError(value)
      case Right(value) =>
        value
    }

    val globalArgs = GlobalArgsSchema(P, None, None)

    val roleHelp = ParserSchemaFormatter.makeDocs(ParserSchema(globalArgs, Seq(
      RoleParserSchema("init", IP, Some("setup project template. Invoke as :init <path>"), None, freeArgsAllowed = true),
      RoleParserSchema("scala", LP, Some("scala target"), None, freeArgsAllowed = false),
      RoleParserSchema("go", LP, Some("go target"), None, freeArgsAllowed = false),
      RoleParserSchema("csharp", LP, Some("C#/Unity target"), None, freeArgsAllowed = false),
      RoleParserSchema("typescript", LP, Some("Typescript target"), None, freeArgsAllowed = false),
    )))

    if (parsed.roles.isEmpty || parsed.roles.exists(_.role == "help")) {
      println(roleHelp)
    }

    val init = parsed.roles.find(_.role == "init").map {
      r =>
        new File(r.freeArgs.head).toPath
    }

    val parameters = parsed.globalParameters
    val root = parameters.findValue(P.rootDir).asPath.getOrElse(Paths.get("."))
    val src = parameters.findValue(P.sourceDir).asPath.getOrElse(root.resolve("source"))
    val target = parameters.findValue(P.targetDir).asPath.getOrElse(root.resolve("target"))
    assert(src.toFile.getCanonicalPath != target.toFile.getCanonicalPath)
    val overlay = parameters.findValue(P.overlayDir).asPath.getOrElse(root.resolve("overlay"))
    val overlayVersion = parameters.findValue(P.overlayVersionFile).asPath
    val publish = parameters.hasFlag(P.publish)
    val defines = parseDefs(parameters)

    val internalRoles = Seq("init", "help")

    val languages = parsed.roles.filterNot(r => internalRoles.contains(r.role)).map {
      role =>
        val parameters = role.roleParameters
        val runtime = parameters.hasNoFlag(LP.noRuntime)
        val zip = parameters.hasNoFlag(LP.noZip)
        val target = parameters.findValue(LP.target).asPath
        val manifest = parameters.findValue(LP.manifest).asFile
        val credentials = parameters.findValue(LP.credentials).asFile
        val defines = parseDefs(parameters)
        val extensions = parameters.findValue(LP.extensionSpec).map(_.value.split(',')).toList.flatten

        LanguageOpts(
          id = role.role,
          withRuntime = runtime,
          target = target,
          manifest = manifest,
          credentials = credentials,
          extensions = extensions,
          overrides = defines,
          zip = zip,
        )
    }

    IDLCArgs(
      root,
      src,
      overlay,
      target,
      languages.toList,
      init,
      overlayVersion,
      defines,
      publish
    )
  }

  private def parseDefs(parameters: RawEntrypointParams): Map[String, String] = {
    parameters.findValues(LP.define).map {
      v =>
        val parts = v.value.split('=')
        parts.head -> parts.tail.mkString("=")
    }.toMap
  }

}
