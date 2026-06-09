import izumi.sbtgen._
import izumi.sbtgen.model._

object Idealingua {
  def main(args: Array[String]): Unit = {
    entrypoint(args.toSeq)
  }

  object V {
    val izumi = Version.VExpr("Izumi.version")

    val sbtgen = Version.VExpr("V.sbtgen")

    val kind_projector = Version.VExpr("V.kind_projector")
    val scalatest      = Version.VExpr("V.scalatest")

    val cats                 = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.org_typelevel_cats_core_version")
    val cats_effect          = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.org_typelevel_cats_effect_version")
    val circe                = Version.VExpr("Izumi.Deps.fundamentals_json_circeJVM.io_circe_circe_core_version")
    val circe_generic_extras = Version.VExpr("V.circe_generic_extras")
    val circe_derivation     = Version.VExpr("V.circe_derivation")
    val jawn                 = Version.VExpr("Izumi.Deps.fundamentals_json_circeJVM.org_typelevel_jawn_parser_version")
    val zio                  = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.dev_zio_zio_version")
    val zio_interop_cats     = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.dev_zio_zio_interop_cats_version")
    val izumi_reflect        = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.dev_zio_izumi_reflect_version")

    val http4s          = Version.VExpr("V.http4s")
    val http4s_blaze    = Version.VExpr("V.http4s_blaze")
    val scalameta       = Version.VExpr("V.scalameta")
    val fastparse       = Version.VExpr("V.fastparse")
    val scala_xml       = Version.VExpr("V.scala_xml")
    val asynchttpclient = Version.VExpr("V.asynchttpclient")

    val slf4j           = Version.VExpr("V.slf4j")
    val typesafe_config = Version.VExpr("V.typesafe_config")

    val scala_java_time = Version.VExpr("V.scala_java_time")
    val scodec_bits     = Version.VExpr("V.scodec_bits")
    val json_schema_validator = Version.VExpr("V.json_schema_validator")
  }

  object PV {
    val izumi                      = Version.VExpr("PV.izumi")
    val packager                   = Version.VExpr("PV.packager")
    val sbt_mdoc                   = Version.VExpr("PV.sbt_mdoc")
    val sbt_paradox_material_theme = Version.VExpr("PV.sbt_paradox_material_theme")
    val sbt_ghpages                = Version.VExpr("PV.sbt_ghpages")
    val sbt_site                   = Version.VExpr("PV.sbt_site")
    val sbt_unidoc                 = Version.VExpr("PV.sbt_unidoc")
    val sbt_scoverage              = Version.VExpr("PV.sbt_scoverage")
    val sbt_pgp                    = Version.VExpr("PV.sbt_pgp")

    val scala_js_version        = Version.VExpr("PV.scala_js_version")
    val crossproject_version    = Version.VExpr("PV.crossproject_version")
    val scalajs_bundler_version = Version.VExpr("PV.scalajs_bundler_version")
  }

  def entrypoint(args: Seq[String]): Unit = {
    Entrypoint.main(izumi, settings, Seq("-o", ".") ++ args)
  }

  val settings = GlobalSettings(
    groupId        = "io.7mind.izumi",
    sbtVersion     = None,
    scalaJsVersion = PV.scala_js_version,
  )

  object Deps {
    final val fundamentals_collections = Library("io.7mind.izumi", "fundamentals-collections", V.izumi, LibraryType.Auto)
    final val fundamentals_platform    = Library("io.7mind.izumi", "fundamentals-platform", V.izumi, LibraryType.Auto)
    final val fundamentals_functional  = Library("io.7mind.izumi", "fundamentals-functional", V.izumi, LibraryType.Auto)
    final val fundamentals_bio         = Library("io.7mind.izumi", "fundamentals-bio", V.izumi, LibraryType.Auto)
    final val logstage_core            = Library("io.7mind.izumi", "logstage-core", V.izumi, LibraryType.Auto)
    final val logstage_adapter_slf4j   = Library("io.7mind.izumi", "logstage-adapter-slf4j", V.izumi, LibraryType.Auto)

    final val fundamentals_basics = Seq(
      fundamentals_collections,
      fundamentals_platform,
      fundamentals_functional,
    )
    final val scalatest = Library("org.scalatest", "scalatest", V.scalatest, LibraryType.Auto) in Scope.Test.all

    final val cats_core   = Library("org.typelevel", "cats-core", V.cats, LibraryType.Auto)
    final val cats_effect = Library("org.typelevel", "cats-effect", V.cats_effect, LibraryType.Auto)
    final val cats_all = Seq(
      cats_core,
      cats_effect,
    )

    final val zio_core         = Library("dev.zio", "zio", V.zio, LibraryType.Auto)
    final val zio_interop_cats = Library("dev.zio", "zio-interop-cats", V.zio_interop_cats, LibraryType.Auto)
    final val izumi_reflect    = Library("dev.zio", "izumi-reflect", V.izumi_reflect, LibraryType.Auto)
    final val zio_all = Seq(
      zio_core,
      zio_interop_cats,
      izumi_reflect,
    )

    final val typesafe_config = Library("com.typesafe", "config", V.typesafe_config, LibraryType.Invariant) in Scope.Compile.all

    final val jawn = Library("org.typelevel", "jawn-parser", V.jawn, LibraryType.AutoJvm)

    final val circe_all = Seq(
      Library("io.circe", "circe-parser", V.circe, LibraryType.Auto) in Scope.Compile.all,
      Library("io.circe", "circe-literal", V.circe, LibraryType.Auto) in Scope.Compile.all,
      Library("io.circe", "circe-generic-extras", V.circe_generic_extras, LibraryType.Auto) in Scope.Compile.all,
      Library("io.circe", "circe-derivation", V.circe_derivation, LibraryType.Auto) in Scope.Compile.all.scalaVersion(ScalaVersionScope.AllScala2),
      Library("io.circe", "circe-generic", V.circe, LibraryType.Auto) in Scope.Compile.all.scalaVersion(ScalaVersionScope.AllScala3),
    )

    final val scala_sbt = Library("org.scala-sbt", "sbt", Version.VExpr("sbtVersion.value"), LibraryType.Invariant)
    final val scala_reflect = Library("org.scala-lang", "scala-reflect", Version.VExpr("scalaVersion.value"), LibraryType.Invariant) in Scope.Provided.all.scalaVersion(
      ScalaVersionScope.AllScala2
    )
    final val scala_xml = Library("org.scala-lang.modules", "scala-xml", V.scala_xml, LibraryType.Auto) in Scope.Compile.all
    final val scalameta = Library("org.scalameta", "scalameta", V.scalameta, LibraryType.Auto) in Scope.Compile.all

    final val projector = Library("org.typelevel", "kind-projector", V.kind_projector, LibraryType.Invariant)
      .more(LibSetting.Raw("cross CrossVersion.full"))

    final val fastparse = Library("com.lihaoyi", "fastparse", V.fastparse, LibraryType.Auto) in Scope.Compile.all

    final val http4s_client = Seq(
      Library("org.http4s", "http4s-blaze-client", V.http4s_blaze, LibraryType.Auto)
    )

    val http4s_server = Seq(
      Library("org.http4s", "http4s-dsl", V.http4s, LibraryType.Auto),
      Library("org.http4s", "http4s-circe", V.http4s, LibraryType.Auto),
      Library("org.http4s", "http4s-blaze-server", V.http4s_blaze, LibraryType.Auto),
    )

    val http4s_all = http4s_server ++ http4s_client

    val asynchttpclient = Library("org.asynchttpclient", "async-http-client", V.asynchttpclient, LibraryType.Invariant)

    val scala_java_time = Library("io.github.cquiroz", "scala-java-time", V.scala_java_time, LibraryType.Auto)
    // scodec-bits: used by Fingerprint in izumi.idealingua.typer.ir (IMPL-1).
    // Cross-builds on Scala 2.13 + 3.x (JVM + JS). See tasks.md F1.
    val scodec_bits = Library("org.scodec", "scodec-bits", V.scodec_bits, LibraryType.Auto) in Scope.Compile.all
    // json-schema-validator: used by the harness PR-04 IMPL-MCP-M5 validation specs
    // (Layer A — fixtures vs emitted schemas; Layer B — *.mcp.json vs MCP ListToolsResult schema).
    // 1.5.9 supports JSON Schema 2020-12. Test-scope only.
    val json_schema_validator = Library("com.networknt", "json-schema-validator", V.json_schema_validator, LibraryType.Invariant) in Scope.Test.jvm
  }

  import Deps._

  // DON'T REMOVE, these variables are read from CI build (build.sh)
  final val scala213 = ScalaVersion("2.13.18")
  final val scala300 = ScalaVersion("3.8.3")

  object Groups {
    final val idealingua = Set(Group("idealingua"))
  }

  object Targets {
    val targetScala2 = Seq(scala213)
    val targetScala3 = Seq(scala300, scala213)
    private val jvmPlatform2 = PlatformEnv(
      platform = Platform.Jvm,
      language = targetScala2,
      settings = Seq.empty,
    )
    private val jsPlatform2 = PlatformEnv(
      platform = Platform.Js,
      language = targetScala2,
      settings = Seq(
        "coverageEnabled" := false,
        "scalaJSLinkerConfig" in (SettingScope.Project, Platform.Js) := "{ scalaJSLinkerConfig.value.withModuleKind(ModuleKind.CommonJSModule) }".raw,
      ),
    )
    private val jvmPlatform3 = PlatformEnv(
      platform = Platform.Jvm,
      language = targetScala3,
      settings = Seq.empty,
    )
    private val jsPlatform3 = PlatformEnv(
      platform = Platform.Js,
      language = targetScala3,
      settings = Seq(
        "coverageEnabled" := false,
        "scalaJSLinkerConfig" in (SettingScope.Project, Platform.Js) := "{ scalaJSLinkerConfig.value.withModuleKind(ModuleKind.CommonJSModule) }".raw,
      ),
    )
    final val cross2 = Seq(jvmPlatform2, jsPlatform2)
    final val jvm2   = Seq(jvmPlatform2)

    final val cross3 = Seq(jvmPlatform3, jsPlatform3)
    final val jvm3   = Seq(jvmPlatform3)
  }

  object Projects {

    final val plugins = Plugins(
      Seq(Plugin("IzumiPlugin"))
    )

    implicit class VersionOptionExt(version: Option[Version]) {
      def asExpr = {
        version match {
          case Some(v) => v.asExpr
          case _       => ???
        }
      }
    }

    implicit class VersionExt(version: Version) {
      def asExpr = {
        version match {
          case v: Version.VConst      => s"\"${v.value}\""
          case v: Version.VExpr       => v.value
          case v: Version.SbtGen.type => v.value
        }
      }
    }

    object root {
      final val id = ArtifactId("idealingua-v1")
      final val plugins = Plugins(
        enabled = Seq(Plugin("SbtgenVerificationPlugin"))
      )
      final val settings = Seq(
        "libraryDependencySchemes" in SettingScope.Build += s""""io.circe" %% "circe-core" % VersionScheme.Always""".raw,
        "libraryDependencySchemes" in SettingScope.Build += s""""io.circe" %% "circe-core_sjs1" % VersionScheme.Always""".raw,
      )

      final val sharedAggSettings = Seq(
        "crossScalaVersions" := "Nil".raw
      )

      final val rootSettings = Defaults.SbtMetaRootOptions ++ Defaults.RootOptions ++ Seq(
        "crossScalaVersions" := "Nil".raw,
        "libraryDependencies" := "Nil".raw,
        "coverageEnabled" := false,
        "organization" in SettingScope.Build := "io.7mind.izumi",
        "publishTo" in SettingScope.Build :=
          """{
            |  // https://github.com/sbt/sbt/issues/8131
            |  if (isSnapshot.value) {
            |    Some(
            |      "central-snapshots" at "https://central.sonatype.com/repository/maven-snapshots/"
            |    )
            |  } else {
            |    localStaging.value
            |  }
            |}
            |""".stripMargin.raw,
        "credentials" in SettingScope.Build ++=
          """{
            |  val credTarget = Path.userHome / ".sbt" / "secrets" / "credentials.sonatype-new.properties"
            |  if (credTarget.exists) {
            |    Seq(Credentials(credTarget))
            |  } else {
            |    Seq.empty
            |  }
            |}""".stripMargin.raw,
        "credentials" in SettingScope.Build ++=
          """{
            |  val credTarget = Path.userHome / ".sbt" / "secrets" / "credentials.sonatype-nexus.properties"
            |  if (credTarget.exists) {
            |    Seq(Credentials(credTarget))
            |  } else {
            |    Seq.empty
            |  }
            |}""".stripMargin.raw,
        "credentials" in SettingScope.Build ++=
          """{
            |  val credTarget = file(".") / ".secrets" / "credentials.sonatype-nexus.properties"
            |  if (credTarget.exists) {
            |    Seq(Credentials(credTarget))
            |  } else {
            |    Seq.empty
            |  }
            |}""".stripMargin.raw,
        "refreshFlakeTask" := """{
                                |  val log = streams.value.log
                                |  val rootDir = (ThisBuild / baseDirectory).value
                                |  val lockfileOutput = rootDir / "deps.lock.json"
                                |  val refreshCommand = Process(
                                |    Seq("nix", "develop", "--command", "mdl", "--verbose", ":flake-refresh"),
                                |    rootDir
                                |  )
                                |  // mdl/nix stream progress on stderr; route both streams to info so it
                                |  // surfaces live in the sbt console instead of as alarming [error] lines.
                                |  val refreshLogger = ProcessLogger(line => log.info(line), line => log.info(line))
                                |  val result = refreshCommand.!(refreshLogger)
                                |  if (result != 0) {
                                |    throw new MessageOnlyException(s"flake.nix update failed: mdl exited with $result")
                                |  }
                                |  val gitAdd = Process(Seq("git", "add", lockfileOutput.getPath), rootDir)
                                |  val gitResult = gitAdd.!(log)
                                |  if (gitResult != 0) {
                                |    throw new MessageOnlyException(s"git add failed with exit code $gitResult")
                                |  }
                                |}""".stripMargin.raw,
        "releaseProcess" := """Seq[ReleaseStep](
                              |  checkSnapshotDependencies,
                              |  inquireVersions,
                              |  runClean,
                              |  runTest,
                              |  setReleaseVersion,
                              |  releaseStepTask(refreshFlakeTask),
                              |  commitReleaseVersion,
                              |  tagRelease,
                              |  //publishArtifacts,
                              |  setNextVersion,
                              |  commitNextVersion,
                              |  pushChanges
                              |)""".stripMargin.raw,
        "homepage" in SettingScope.Build := """Some(url("https://izumi.7mind.io"))""".raw,
        "licenses" in SettingScope.Build := """Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))""".raw,
        "developers" in SettingScope.Build :=
          """List(
          Developer(id = "7mind", name = "Septimal Mind", url = url("https://github.com/7mind"), email = "team@7mind.io"),
        )""".raw,
        "scmInfo" in SettingScope.Build := """Some(ScmInfo(url("https://github.com/7mind/izumi"), "scm:git:https://github.com/7mind/izumi.git"))""".raw,
        "scalacOptions" in SettingScope.Build += s"""s${"\"" * 3}-Xmacro-settings:scalatest-version=$${${V.scalatest.asExpr}}${"\"" * 3}""".raw,
        "scalacOptions" in SettingScope.Build += s"""s${"\"" * 3}-Xmacro-settings:scalajs-version=${PluginVersions.pv.scala_js_version}${"\"" * 3}""".raw,
        "scalacOptions" in SettingScope.Build += s"""s${"\"" * 3}-Xmacro-settings:bundler-version=$${${Idealingua.settings.bundlerVersion.asExpr}}${"\"" * 3}""".raw,
        "scalacOptions" in SettingScope.Build += s"""s${"\"" * 3}-Xmacro-settings:sbt-js-version=$${${Idealingua.settings.sbtJsDependenciesVersion.asExpr}}${"\"" * 3}""".raw,
        "scalacOptions" in SettingScope.Build += s"""s${"\"" * 3}-Xmacro-settings:crossproject-version=$${${Idealingua.settings.crossProjectVersion.asExpr}}${"\"" * 3}""".raw,
        "scalacOptions" in SettingScope.Build += """s"-Xmacro-settings:is-ci=${insideCI.value}"""".raw,
      )

      final val sharedSettings = Defaults.SbtMetaSharedOptions ++ Defaults.CrossScalaPlusSources ++ Seq(
        "testOptions" in SettingScope.Test += """Tests.Argument("-oDF")""".raw,
        // "testOptions" in (SettingScope.Test, Platform.Jvm) ++= s"""Seq(Tests.Argument("-u"), Tests.Argument(s"$${target.value}/junit-xml-$${scalaVersion.value}"))""".raw,
        "scalacOptions" ++= Seq(
          SettingKey(Some(scala213), None) := Defaults.Scala213Options,
          SettingKey(Some(scala300), None) := Defaults.Scala3Options,
          SettingKey.Default := Const.EmptySeq,
        ),
        "scalacOptions" ++= Seq(
          SettingKey(Some(scala213), Some(true)) := Seq(
            "-opt:l:inline",
            "-opt-inline-from:izumi.**",
          ),
          SettingKey(Some(scala300), Some(true)) := Seq(
            "-opt",
            "-opt-inline:izumi.**",
          ),
          SettingKey.Default := Const.EmptySeq,
        ),
        "scalacOptions" -= "-Wconf:any:error",
        "scalacOptions" += "-Wconf:msg=nowarn:silent",
        "scalacOptions" += "-Wconf:msg=pattern var charIn:silent",
        // scalameta's parsers_3 transitively depends on trees_2.13 → sourcecode_2.13,
        // conflicting with sourcecode_3 from fastparse_3
        "excludeDependencies" ++= Seq(
          SettingKey(Some(scala300), None) := Seq(""""com.lihaoyi" % "sourcecode_2.13"""".raw),
          SettingKey.Default := Const.EmptySeq,
        ),
      )

    }

    object idealingua {
      final val id       = ArtifactId("idealingua")
      final val basePath = Seq("idealingua-v1")

      final val model                = ArtifactId("idealingua-v1-model")
      final val core                 = ArtifactId("idealingua-v1-core")
      final val runtimeRpcScala      = ArtifactId("idealingua-v1-runtime-rpc-scala")
      final val testDefs             = ArtifactId("idealingua-v1-test-defs")
      final val transpilers          = ArtifactId("idealingua-v1-transpilers")
      final val runtimeRpcHttp4s     = ArtifactId("idealingua-v1-runtime-rpc-http4s")
      final val runtimeRpcTypescript = ArtifactId("idealingua-v1-runtime-rpc-typescript")
      final val runtimeRpcCSharp     = ArtifactId("idealingua-v1-runtime-rpc-csharp")
      final val compiler             = ArtifactId("idealingua-v1-compiler")
      final val testHarness          = ArtifactId("idealingua-v1-test-harness")
    }

    object docs {
      final val id       = ArtifactId("doc")
      final val basePath = Seq("doc")

      final lazy val microsite = ArtifactId("microsite")
    }

  }

  final val forkTests = Seq(
    "fork" in (SettingScope.Test, Platform.Jvm) := true
  )


  final lazy val idealingua = Aggregate(
    name = Projects.idealingua.id,
    artifacts = Seq(
      Artifact(
        name    = Projects.idealingua.model,
        libs    = Seq(scala_reflect, Deps.scodec_bits) ++ Deps.fundamentals_basics.map(_ in Scope.Compile.all),
        depends = Seq.empty,
      ),
      Artifact(
        name      = Projects.idealingua.core,
        libs      = Seq(fastparse) ++ Seq(Deps.fundamentals_platform in Scope.Compile.all),
        depends   = Seq(Projects.idealingua.model).map(_ in Scope.Compile.all),
        platforms = Targets.cross3,
      ),
      Artifact(
        name = Projects.idealingua.runtimeRpcScala,
        libs = Seq(
          scala_reflect,
          Deps.fundamentals_bio in Scope.Compile.all,
          Deps.fundamentals_platform in Scope.Compile.all,
          scala_java_time in Scope.Test.js,
        ) ++
          cats_all.map(_ in Scope.Compile.all) ++
          circe_all ++
          zio_all.map(_ in Scope.Test.all),
        depends = Seq.empty,
      ),
      Artifact(
        name = Projects.idealingua.runtimeRpcHttp4s,
        libs = (http4s_all ++ Seq(asynchttpclient, Deps.logstage_core, Deps.logstage_adapter_slf4j)).map(_ in Scope.Compile.all),
        depends = Seq(Projects.idealingua.runtimeRpcScala).map(_ in Scope.Compile.all) ++
          // `idealingua-v1-model` carries `izumi.idealingua.runtime.model.IDL*`
          // base traits (`IDLGeneratedType`, `IDLEnumElement`, `IDLAdtElement`,
          // ...). The runtimeRpcScala compile classpath transitively re-exports
          // them in some builds but not all, and the test classpath here needs
          // direct visibility for the freshly-generated `mcpdemo/*.scala`
          // fixtures consumed by `McpBridgeRealServerSpec` (per-corpus codegen
          // committed under `src/test/scala/mcpdemo/`).
          Seq(Projects.idealingua.model).map(_ in Scope.Test.jvm) ++
          Seq(Projects.idealingua.testDefs).map(_ in Scope.Test.jvm),
        platforms = Targets.jvm3,
      ),
      Artifact(
        name = Projects.idealingua.transpilers,
        libs = Seq(
          scala_xml,
          scalameta,
          Deps.fundamentals_bio in Scope.Compile.all,
        ) ++
          circe_all,
        depends = Seq(
          Projects.idealingua.core,
          Projects.idealingua.runtimeRpcScala,
        ).map(_ in Scope.Compile.all) ++
          Seq(Projects.idealingua.testDefs, Projects.idealingua.runtimeRpcTypescript, Projects.idealingua.runtimeRpcCSharp)
            .map(_ in Scope.Test.jvm),
        settings  = forkTests,
        platforms = Targets.cross3,
      ),
      Artifact(
        name      = Projects.idealingua.testDefs,
        libs      = zio_all,
        depends   = Seq(Projects.idealingua.runtimeRpcScala).map(_ in Scope.Compile.all),
        platforms = Targets.jvm3,
      ),
      Artifact(
        name      = Projects.idealingua.runtimeRpcTypescript,
        libs      = Seq.empty,
        depends   = Seq.empty,
        platforms = Targets.jvm3,
      ),
      Artifact(
        name      = Projects.idealingua.runtimeRpcCSharp,
        libs      = Seq.empty,
        depends   = Seq.empty,
        platforms = Targets.jvm3,
      ),
      Artifact(
        name = Projects.idealingua.compiler,
        libs = Seq(typesafe_config),
        depends = Seq(
          Projects.idealingua.transpilers,
          Projects.idealingua.runtimeRpcScala,
          Projects.idealingua.runtimeRpcTypescript,
          Projects.idealingua.runtimeRpcCSharp,
          Projects.idealingua.testDefs,
        ).map(_ in Scope.Compile.all),
        platforms = Targets.jvm3,
        settings  = Seq(
          // R1a added `testcodegen.TestCodegenMain` alongside the public
          // `CommandlineIDLCompiler.main`. Without an explicit `mainClass`,
          // sbt-native-packager's staged launcher prompts `-main <class>`
          // instead of running the compiler directly — which breaks
          // `regression-harness/idl-regress` (it invokes the launcher
          // positionally with `--root=…`). Pin the entry point to the
          // public compiler. (Was added to build.sbt directly in X2; moved
          // into sbtgen here so `mdl :gen` doesn't regress it.)
          "mainClass" in SettingScope.Compile := """Some("izumi.idealingua.compiler.CommandlineIDLCompiler")""".raw,
        ),
        plugins = Plugins(
          Seq(Plugin("JavaAppPackaging"))
        ),
      ),
      Artifact(
        name      = Projects.idealingua.testHarness,
        libs      = Seq(Deps.json_schema_validator),
        depends   = Seq(
          Projects.idealingua.transpilers,
          Projects.idealingua.compiler,
          Projects.idealingua.testDefs,
        ).map(_ in Scope.Compile.all),
        platforms = Targets.jvm3,
        settings  = Seq(
          // R1: test sources are generated at build time (under
          // `<harnessTarget>/generated-sources/test-harness/`) by the
          // `idealingua-v1-compiler` module's `TestCodegenMain` entrypoint.
          // Generation is wired as a `Compile / sourceGenerators` task so
          // sbt invokes it before `Compile / compile`. The MCP bridge
          // classpath resources land under `scala-mcp-resources/` and are
          // exposed via `unmanagedResourceDirectories`.
          //
          // The `scala/` subtree is the Scala-translator output and feeds
          // compilation. `scala-mcp/` (per-service bridge sources) is NOT
          // compiled — those modules depend on http4s symbols that aren't on
          // this module's classpath; they exist as artefacts the
          // `McpBridgeConsistencySpec` reads at test time. `scala-mcp-resources/`
          // is wired below as a `Compile` resource directory.
          "sourceGenerators" in SettingScope.Compile += """Def.task[Seq[File]] {
                                   |  val log         = streams.value.log
                                   |  val repoRoot    = (LocalRootProject / baseDirectory).value.toPath.toAbsolutePath
                                   |  val genRoot     = (Compile / target).value.toPath.resolve("generated-sources/test-harness").toAbsolutePath
                                   |  val codegenCp   = (`idealingua-v1-compiler` / Compile / fullClasspath).value.files
                                   |  val codegenRun  = (`idealingua-v1-compiler` / Compile / runner).value
                                   |  log.info(s"test-harness codegen: generating into $genRoot")
                                   |  codegenRun.run(
                                   |    "izumi.idealingua.compiler.testcodegen.TestCodegenMain",
                                   |    codegenCp,
                                   |    Seq(repoRoot.toString, genRoot.toString),
                                   |    log,
                                   |  ).failed.foreach(e => throw new MessageOnlyException(e.getMessage))
                                   |  val scalaSubdir = genRoot.resolve("scala")
                                   |  if (java.nio.file.Files.exists(scalaSubdir)) {
                                   |    val s = java.nio.file.Files.walk(scalaSubdir)
                                   |    try {
                                   |      val it  = s.iterator()
                                   |      val buf = scala.collection.mutable.ArrayBuffer.empty[java.io.File]
                                   |      while (it.hasNext) {
                                   |        val p = it.next()
                                   |        if (java.nio.file.Files.isRegularFile(p) && p.getFileName.toString.endsWith(".scala")) buf += p.toFile
                                   |      }
                                   |      buf.toSeq
                                   |    } finally s.close()
                                   |  } else Seq.empty[java.io.File]
                                   |}.taskValue""".stripMargin.raw,
          "unmanagedResourceDirectories" in SettingScope.Compile += """(Compile / target).value / "generated-sources" / "test-harness" / "scala-mcp-resources"""".raw,
          // The sourceGenerator above emits ~150 IDL-generated `.scala`
          // files under `target/generated-sources/test-harness/scala/...`.
          // scoverage instruments all `Compile / sources` (managed +
          // unmanaged), then in `coverageReport` tries to match each
          // instrumented file to a declared source root. Generated files
          // live outside `src/main/scala`, so scoverage throws
          // `RuntimeException: No source root found for .../generated-sources/.../<File>.scala`.
          // Disabling instrumentation on the test-harness mirrors the
          // policy already in place for the runtime/transpiler modules
          // (test scaffolding is not the unit under measurement).
          "coverageEnabled" := false,
          "runWireFixtures" := """{
                               |  val log      = streams.value.log
                               |  val repoRoot = (LocalRootProject / baseDirectory).value.toPath
                               |  log.info("runWireFixtures: starting")
                               |  val cp = (Compile / fullClasspath).value.files
                               |  val r  = (Compile / runner).value
                               |  r.run("izumi.idealingua.harness.WireFixturesMain", cp, Seq(repoRoot.toString), log)
                               |    .failed.foreach(e => throw new MessageOnlyException(e.getMessage))
                               |  def countJsons(p: java.nio.file.Path): Long = if (java.nio.file.Files.exists(p)) {
                               |    val s = java.nio.file.Files.walk(p)
                               |    try s.filter(x => java.nio.file.Files.isRegularFile(x) && x.toString.endsWith(".json")).count()
                               |    finally s.close()
                               |  } else 0L
                               |  val sc = countJsons(repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/scala"))
                               |  val tc = countJsons(repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript"))
                               |  val cc = countJsons(repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/csharp"))
                               |  log.info(s"runWireFixtures: all $sc Scala + $tc TypeScript + $cc CSharp fixtures match")
                               |}""".stripMargin.raw,
          "runCrossLangInterop" := """{
                            |  val log      = streams.value.log
                            |  val repoRoot = (LocalRootProject / baseDirectory).value.toPath
                            |  log.info("runCrossLangInterop: starting cross-language matrix")
                            |  val cp = (Compile / fullClasspath).value.files
                            |  val r  = (Compile / runner).value
                            |  r.run("izumi.idealingua.harness.CrossLangMain", cp, Seq(repoRoot.toString), log)
                            |    .failed.foreach(e => throw new MessageOnlyException(e.getMessage))
                            |  log.info("runCrossLangInterop: matrix verified")
                            |}""".stripMargin.raw,
        ),
      ),
    ),
    pathPrefix       = Projects.idealingua.basePath,
    groups           = Groups.idealingua,
    defaultPlatforms = Targets.cross3,
  )

  val izumi: Project = Project(
    name = Projects.root.id,
    aggregates = Seq(
      idealingua
    ),
    topLevelSettings  = Projects.root.settings,
    sharedSettings    = Projects.root.sharedSettings,
    sharedAggSettings = Projects.root.sharedAggSettings,
    rootSettings      = Projects.root.rootSettings,
    imports = Seq(
      Import("sbtrelease.ReleaseStateTransformations._"),
      Import("""scala.sys.process._
               |
               |lazy val refreshFlakeTask    = taskKey[Unit]("Refresh flake.nix")
               |lazy val runWireFixtures     = taskKey[Unit]("Run Layer B wire-byte fixtures")
               |lazy val runCrossLangInterop = taskKey[Unit]("Run Layer C cross-language interop")
               |""".stripMargin),
    ),
    globalLibs = Seq(
      ScopedLibrary(projector, FullDependencyScope(Scope.Compile, Platform.All, ScalaVersionScope.AllScala2), compilerPlugin = true),
      scalatest,
    ),
    rootPlugins   = Projects.root.plugins,
    globalPlugins = Projects.plugins,
    appendPlugins = Defaults.SbtGenPlugins ++ Seq(
      SbtPlugin("com.github.sbt", "sbt-pgp", PV.sbt_pgp),
      SbtPlugin("org.scoverage", "sbt-scoverage", PV.sbt_scoverage),
      SbtPlugin("io.7mind.izumi", "sbt-izumi-deps", PV.izumi),
      SbtPlugin("com.github.sbt", "sbt-native-packager", PV.packager),
    ),
  )
}
