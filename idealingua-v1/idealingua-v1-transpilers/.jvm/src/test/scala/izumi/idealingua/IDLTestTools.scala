package izumi.idealingua

import izumi.fundamentals.platform.build.MacroParameters
import izumi.fundamentals.platform.files.IzFiles
import izumi.fundamentals.platform.jvm.IzJvm
import izumi.fundamentals.platform.language.Quirks
import izumi.fundamentals.platform.resources.IzResourcesDirty
import izumi.fundamentals.platform.strings.IzString.*
import izumi.fundamentals.platform.time.IzTime.*
import izumi.fundamentals.platform.time.Timed
import izumi.idealingua.il.loader.*
import izumi.idealingua.il.renderer.{IDLRenderer, IDLRenderingOptions}
import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.*
import izumi.idealingua.translator.*
import izumi.idealingua.translator.tocsharp.CSharpTranslator
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.layout.CSharpNamingConvention
import izumi.idealingua.translator.togolang.GoLangTranslator
import izumi.idealingua.translator.togolang.extensions.GoLangTranslatorExtension
import izumi.idealingua.translator.toprotobuf.ProtobufTranslator
import izumi.idealingua.translator.toprotobuf.extensions.ProtobufTranslatorExtension
import izumi.idealingua.translator.toscala.ScalaTranslator
import izumi.idealingua.translator.toscala.extensions.ScalaTranslatorExtension
import izumi.idealingua.translator.totypescript.TypeScriptTranslator
import izumi.idealingua.translator.totypescript.extensions.TypeScriptTranslatorExtension

import java.io.File
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.nio.file.*
import scala.sys.process.*
import scala.util.{Properties, Try}

final case class CompilerOutput(targetDir: Path, allFiles: Seq[Path]) {
  def absoluteTargetDir: Path = targetDir.toAbsolutePath

  def phase2: Path = absoluteTargetDir.getParent.resolve("phase2-compiler-output")

  def phase2Relative: Path = absoluteTargetDir.relativize(phase2)

  def relativeOutputs: Seq[String] = allFiles.map(p => absoluteTargetDir.relativize(p.toAbsolutePath).toString)
}

object IDLTestTools {
  def hasDocker: Boolean = IzFiles.haveExecutables("docker")

  def isCI: Boolean = MacroParameters.sbtIsInsideCI().contains(true)

  def loadDefs(): Seq[LoadedDomain.Success] = loadDefs("/defs/any")

  def loadDefs(base: String): Seq[LoadedDomain.Success] = loadDefs(makeLoader(base), makeResolver(base))

  // fixme: workaround for `Error: Could not create the Java Virtual Machine.` (remove if it doesn't reproduce anymore)
  def scalaSysEnv = Map("JAVA_OPTS" -> "")

  def makeLoader(base: String): LocalModelLoaderContext = {
    val src     = new File(getClass.getResource(base).toURI).toPath
    val context = new LocalModelLoaderContext(Seq(src), Seq.empty)
    context
  }

  def makeResolver(base: String): ModelResolver = {
    val last = base.split('/').last
    val rules = Try(TypespaceCompilerBaseFacade.descriptor(IDLLanguage.parse(last)).rules)
      .getOrElse(TypespaceCompilerBaseFacade.descriptors.flatMap(_.rules))
    new ModelResolver(rules)
  }

  def loadDefs(context: LocalModelLoaderContext, resolver: ModelResolver): Seq[LoadedDomain.Success] = {
    val loaded   = context.loader.load()
    val resolved = resolver.resolve(loaded).ifWarnings(w => System.err.println(w)).throwIfFailed()

    val loadable = context.enumerator.enumerate().filter(_._1.name.endsWith(context.domainExt)).keySet
    val good     = resolved.successful.map(_.path).toSet
    val failed   = loadable.diff(good)
    assert(failed.isEmpty, s"domains were not loaded: $failed")

    resolved.successful
  }

  def compilesScala(
    id: String,
    domains: Seq[LoadedDomain.Success],
    layout: ScalaProjectLayout,
    useDockerForLocalTest: Boolean,
    extensions: Seq[ScalaTranslatorExtension] = ScalaTranslator.defaultExtensions,
  ): Boolean = {
    val mf                = ScalaBuildManifest.example
    val manifest          = mf.copy(layout = ScalaProjectLayout.SBT, sbt = mf.sbt.copy(projectNaming = mf.sbt.projectNaming.copy(dropFQNSegments = Some(1))))
    val out               = compiles(id, domains, CompilerOptions(IDLLanguage.Scala, extensions, manifest))
    val classpath: String = IzJvm.safeClasspath()

    val cmd = layout match {
      case ScalaProjectLayout.PLAIN =>
        // it's hard to map volumes on CI agent bcs our build runs in docker but all the mounts happens on the docker host
        if (useDockerForLocalTest && hasDocker && !isCI) {
          dockerRun(out, classpath, scala213 = true)
        } else {
          directRun(out, classpath)
        }

      case ScalaProjectLayout.SBT =>
        Seq("sbt", "clean", "compile")
    }

    val exitCode = run(out.absoluteTargetDir, cmd, scalaSysEnv, "scalac")
    exitCode == 0
  }

  private def virtualiseFs(v: Iterable[String], prefix: String): Iterable[(Seq[String], String)] = {
    v.map {
      cpe =>
        val p      = Paths.get(cpe)
        val target = s"/$prefix/${p.getParent.toString.hashCode().toLong + Int.MaxValue}/${p.getFileName.toString}"
        (Seq("-v", s"'$cpe:$target:ro'"), target)
    }
  }

  private def dockerRun(out: CompilerOutput, classpath: String, scala213: Boolean): Seq[String] = {
    val v  = classpath.split(':')
    val cp = virtualiseFs(v, "cp")

    val cpe = cp.flatMap(_._1)
    val scp = cp.map(_._2).mkString(":")

    val flags = if (scala213) {
      Seq(
        "-Wunused:_",
        "-Werror",
      )
    } else {
      Seq(
        "-Ywarn-unused:_",
        "-Xfatal-warnings",
      )
    }

    val dcp = Seq(
      "docker",
      "run",
      "--rm",
    ) ++ cpe ++
      Seq(
        "-v",
        s"'${out.absoluteTargetDir}:/work:Z'",
        "septimalmind/izumi-env:latest",
        "scalac",
        "-J-Xmx2g",
        "-language:higherKinds",
        "-unchecked",
        "-feature",
        "-deprecation",
        "-Xlint:_",
      ) ++
      flags ++
      Seq(
        "-classpath",
        scp,
      ) ++ out.relativeOutputs.filter(_.endsWith(".scala")).map(t => s"'$t'")

    dcp
  }

  private def directRun(out: CompilerOutput, classpath: String) = {
    val currentScalaVersion = Properties.versionNumberString
    Seq(
      "cs",
      "launch",
      s"scalac:$currentScalaVersion",
      "--",
      "-deprecation",
      "-opt-warnings:_",
      "-d",
      out.phase2Relative.toString,
      "-classpath",
      classpath,
    ) ++ out.relativeOutputs.filter(_.endsWith(".scala"))
  }

  def compilesTypeScript(
    id: String,
    domains: Seq[LoadedDomain.Success],
    layout: TypeScriptProjectLayout,
    extensions: Seq[TypeScriptTranslatorExtension] = TypeScriptTranslator.defaultExtensions,
  ): Boolean = {
    val manifest = TypeScriptBuildManifest.example.copy(layout = layout)
    val out      = compiles(id, domains, CompilerOptions(IDLLanguage.Typescript, extensions, manifest))

    val outputTsconfigPath = out.targetDir.resolve("tsconfig.json")
    val tsconfigBytes = new String(Files.readAllBytes(outputTsconfigPath), StandardCharsets.UTF_8)
      .replace("\"dist\"", s""""${out.phase2.toString}"""")
      .getBytes
    Files.write(outputTsconfigPath, tsconfigBytes)

    if (run(out.absoluteTargetDir, Seq("yarn", "install"), Map.empty, "yarn") != 0) {
      return false
    }

    val tscCmd = layout match {
      case TypeScriptProjectLayout.YARN =>
        Seq("yarn", "build")
      case TypeScriptProjectLayout.PLAIN =>
        Seq("tsc", "-p", "tsconfig.json")
    }

    val exitCode = run(out.absoluteTargetDir, tscCmd, Map.empty, "tsc")
    exitCode == 0
  }

  def compilesCSharp(
    id: String,
    domains: Seq[LoadedDomain.Success],
    layout: CSharpProjectLayout,
    extensions: Seq[CSharpTranslatorExtension] = CSharpTranslator.defaultExtensions,
  ): Boolean = {
    val mf       = CSharpBuildManifest.example
    val manifest = mf.copy(layout = layout)

    val lang = IDLLanguage.CSharp
    val out  = compiles(id, domains, CompilerOptions(lang, extensions, manifest))

    layout match {
      case CSharpProjectLayout.NUGET =>
        val conv            = new CSharpNamingConvention(manifest.nuget.projectNaming)
        val cmdNuget        = Seq("nuget", "pack", s"nuspec/${conv.nuspecName(conv.pkgId)}")
        val exitCodeBuild   = run(out.targetDir, cmdNuget, Map.empty, "cs-nuget")
        val cmdMsbuild      = Seq("msbuild", "/t:Restore", "/t:Rebuild")
        val exitCodeMsBuild = run(out.targetDir, cmdMsbuild, Map.empty, "cs-msbuild")
        exitCodeBuild == 0 && exitCodeMsBuild == 0

      case CSharpProjectLayout.PLAIN =>
        val refsDir = out.absoluteTargetDir.resolve("refs")

        IzFiles.recreateDirs(refsDir)

        val refsSrc = s"refs/${lang.toString.toLowerCase()}"
        val refDlls = IzResourcesDirty
          .copyFromClasspath(refsSrc, refsDir).files
          .filter(f => f.toFile.isFile && f.toString.endsWith(".dll")).map(f => out.absoluteTargetDir.relativize(f.toAbsolutePath))
        IzResourcesDirty.copyFromClasspath(refsSrc, out.phase2)

        val outname       = "test-output.dll"
        val refs          = s"/reference:${refDlls.mkString(",")}"
        val cmdBuild      = Seq("csc", "-target:library", s"-out:${out.phase2Relative}/$outname", "-recurse:\\*.cs", refs)
        val exitCodeBuild = run(out.absoluteTargetDir, cmdBuild, Map.empty, "cs-build")

        val cmdTest      = Seq("nunit-console", outname)
        val exitCodeTest = run(out.phase2, cmdTest, Map.empty, "cs-test")

        exitCodeBuild == 0 && exitCodeTest == 0

    }
  }

  def compilesGolang(
    id: String,
    domains: Seq[LoadedDomain.Success],
    layout: GoProjectLayout,
    extensions: Seq[GoLangTranslatorExtension] = GoLangTranslator.defaultExtensions,
  ): Boolean = {
    val mf       = GoLangBuildManifest.example
    val manifest = mf.copy(layout = layout)
    val out      = compiles(id, domains, CompilerOptions(IDLLanguage.Go, extensions, manifest))
    val outDir   = out.absoluteTargetDir

    val tmp = outDir.getParent.resolve("phase1-compiler-tmp")
    tmp.toFile.mkdirs()
    Files.move(outDir, tmp.resolve("src"))
    Files.move(tmp, outDir)

    val env = Map(
      "GOPATH"      -> out.absoluteTargetDir.toString,
      "GO111MODULE" -> "off",
    )
    val goSrc = out.absoluteTargetDir.resolve("src")
    if (manifest.repository.dependencies.nonEmpty) {
      manifest.repository.dependencies.foreach(
        md => {
          run(goSrc, Seq("go", "get", md.module), env, "go-dep-install")
        }
      )
    }

    val cmdBuild = Seq("go", "install", "-pkgdir", out.phase2.toString, "./...")
    val cmdTest  = Seq("go", "test", "./...")

    val exitCodeBuild = run(goSrc, cmdBuild, env, "go-build")
    val exitCodeTest  = run(goSrc, cmdTest, env, "go-test")

    exitCodeBuild == 0 && exitCodeTest == 0
  }

  def compilesProtobuf(
    id: String,
    domains: Seq[LoadedDomain.Success],
    options: Map[String, String],
    extensions: Seq[ProtobufTranslatorExtension] = ProtobufTranslator.defaultExtensions,
  ): Boolean = {
    val manifest = ProtobufBuildManifest.example.copy(options = options)
    val out      = compiles(id, domains, CompilerOptions(IDLLanguage.Protobuf, extensions, manifest))
    val outDir   = out.absoluteTargetDir

    val tmp = outDir.getParent.resolve("phase1-compiler-tmp")
    tmp.toFile.mkdirs()
    Files.move(outDir, tmp.resolve("src"))
    Files.move(tmp, outDir)

    val protoSrc = out.absoluteTargetDir.resolve("src")
    val jOut     = out.absoluteTargetDir.resolve("java_output")
    jOut.toFile.mkdirs()

    val cmdBuild      = Seq("protoc", s"--java_out=$jOut", "$(find ./ -iname \"*.proto\")")
    val exitCodeBuild = run(protoSrc, cmdBuild, Map.empty, "proto-build")

    exitCodeBuild == 0
  }

  private def compiles[E <: TranslatorExtension, M <: BuildManifest](id: String, domains: Seq[LoadedDomain.Success], options: CompilerOptions[E, M]): CompilerOutput = {
    val targetDir = Paths.get("target")
    val tmpdir    = targetDir.resolve("idl-output")

    Quirks.discard(tmpdir.toFile.mkdirs())

    // TODO: clashes still may happen in case of parallel runs with the same ID
    val stablePrefix = s"$id-${options.language.toString}"
    val vmPrefix     = s"$stablePrefix-u${ManagementFactory.getRuntimeMXBean.getStartTime}"
    val dirPrefix    = s"$vmPrefix-ts${System.currentTimeMillis()}"

    dropOldRunsData(tmpdir, stablePrefix, vmPrefix)

    val runDir      = tmpdir.resolve(dirPrefix)
    val domainsDir  = runDir.resolve("phase0-rerender")
    val compilerDir = runDir.resolve("phase1-compiler-input")

    IzFiles.recreateDirs(runDir, domainsDir, compilerDir)
    IzFiles.refreshSymlink(targetDir.resolve(stablePrefix), runDir)

    val products = new TypespaceCompilerFSFacade(domains)
      .compile(compilerDir, UntypedCompilerOptions(options.language, options.extensions, None, options.manifest, options.withBundledRuntime))
      .compilationProducts
    assert(products.paths.toSet.size == products.paths.size)

    rerenderDomains(domainsDir, domains)

    val out = CompilerOutput(compilerDir, products.paths)
    out.phase2.toFile.mkdirs()
    out
  }

  private def rerenderDomains(domainsDir: Path, domains: Seq[LoadedDomain.Success]): Unit = {
    domains.foreach {
      d =>
        val rendered = new IDLRenderer(d.typespace.domain, IDLRenderingOptions(expandIncludes = false)).render()
        Files.write(domainsDir.resolve(s"${d.typespace.domain.id.id}.domain"), rendered.utf8)
    }
  }

  private def dropOldRunsData(tmpdir: Path, stablePrefix: String, vmPrefix: String): Unit = {
    tmpdir.toFile
      .listFiles()
      .toList
      .filter(f => f.isDirectory && f.getName.startsWith(stablePrefix) && !f.getName.startsWith(vmPrefix))
      .foreach {
        f =>
          Quirks.discard(Try(IzFiles.erase(f.toPath)))
      }
  }

  protected def run(workDir: Path, cmd: Seq[String], env: Map[String, String], cname: String): Int = {
    val cmdscript = workDir.getParent.resolve(s"$cname.sh").toAbsolutePath
    val commands = Seq(
      "#!/usr/bin/env bash",
      "set -xe",
      s"cd ${workDir.toAbsolutePath}",
    ) ++ env.map(kv => s"export ${kv._1}=${kv._2}") ++ Seq("env") ++ Seq(cmd.mkString("", " \\\n  ", "\n"))

    val cmdSh = commands.mkString("\n")

    Files.write(cmdscript, cmdSh.getBytes)

    val log = workDir.getParent.resolve(s"$cname.log").toFile
    val Timed(exitCode, duration) = Timed {
      val logger = ProcessLogger(log)
      try {
        Process(Seq("/bin/sh", cmdscript.toString), Some(workDir.toFile), env.toSeq: _*)
          .run(logger)
          .exitValue()
      } finally {
        logger.close()
      }
    }

    System.out.println(s"Done in $duration ${duration.readable}")
    System.out.flush()

    if (exitCode != 0) {
      System.out.println(cmdSh)
      System.out.flush()

      System.err.flush()
      System.err.println(s"Process failed for $cname: exitCode=$exitCode in $duration ${duration.readable}")
      System.err.flush()
      System.err.println(s"""
                            |Failure log (${log.getAbsolutePath}):
                            |${IzFiles.readString(log)}""".stripMargin)
      System.err.flush()
    }
    exitCode
  }
}
