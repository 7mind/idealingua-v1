package izumi.idealingua

import izumi.fundamentals.platform.files.IzFiles
import izumi.idealingua.model.publishing.manifests.{CSharpProjectLayout, GoProjectLayout, ScalaProjectLayout, TypeScriptProjectLayout}
import org.scalatest.wordspec.AnyWordSpec

import scala.sys.process.Process
import scala.util.Properties

class CompilerTest extends AnyWordSpec {

  final val useDockerForLocalScalaTest = false

  import IDLTestTools._

  "IDL compiler" should {
    val id = getClass.getSimpleName

    "be able to compile into scala" in {
      if (!useDockerForLocalScalaTest) {
        require("scalac")
        val systemScalaVersion = Process(Seq("scalac", "-version"), None, scalaSysEnv.toSeq: _*)
          .#|(Process(Seq("sed", "-r", """s/.*version (.*) --.*/\1/"""))).!!.split("\\.").take(2).mkString(".")
        assume(systemScalaVersion.nonEmpty)
        assume(
          Properties.versionNumberString.startsWith(systemScalaVersion),
          s"compiler test can run on systemScalaVersion=$systemScalaVersion only (local compiler used for test should be the same as build compiler)",
        )
      }

      assert(compilesScala(s"$id-plain", loadDefs(), ScalaProjectLayout.PLAIN, useDockerForLocalScalaTest))
      assert(compilesScala(s"$id-plain-nested", loadDefs("/defs/nested/test"), ScalaProjectLayout.PLAIN, useDockerForLocalScalaTest))
      assert(compilesScala(s"$id-plain-nonportable", loadDefs("/defs/scala"), ScalaProjectLayout.PLAIN, useDockerForLocalScalaTest))
    }

    "be able to compile into scala with SBT" ignore {
      require("sbt")
      // we can't test sbt build: it depends on artifacts which may not exist yet
      assert(compilesScala(s"$id-sbt", loadDefs(), ScalaProjectLayout.SBT, useDockerForLocalScalaTest))
      // circular sbt projects are broken in V1
      // assert(compilesScala(s"$id-sbt-nonportable", loadDefs("/defs/scala"), ScalaProjectLayout.SBT))
    }

    "be able to compile into typescript" in {
      require("tsc", "npm", "yarn")
      assert(compilesTypeScript(s"$id-plain", loadDefs(), TypeScriptProjectLayout.PLAIN))
    }

    "be able to compile into typescript with yarn" in {
      // TODO: once we switch to published runtime there may be an issue with this test same as with sbt one
      require("tsc", "npm", "yarn")
      assert(compilesTypeScript(s"$id-yarn", loadDefs(), TypeScriptProjectLayout.YARN))
      assert(compilesTypeScript(s"$id-yarn-nested", loadDefs("/defs/nested/test"), TypeScriptProjectLayout.YARN))
    }

    "be able to compile into golang" in {
      require("go")
      assert(compilesGolang(s"$id-repository", loadDefs(), GoProjectLayout.REPOSITORY))
      assert(compilesGolang(s"$id-plain", loadDefs(), GoProjectLayout.PLAIN))
      assert(compilesGolang(s"$id-plain-nested", loadDefs("/defs/nested/test"), GoProjectLayout.PLAIN))
    }

    "be able to compile into csharp" in {
      require("csc", "nunit-console", "nuget", "msbuild")
      assert(compilesCSharp(s"$id-plain", loadDefs(), CSharpProjectLayout.PLAIN))
    }

    "be able to compile into csharp with nuget layout" in {
      require("csc", "nuget", "msbuild")
      assert(compilesCSharp(s"$id-nuget", loadDefs(), CSharpProjectLayout.NUGET))
    }

    "be able to compile into protobuf" in {
      require("protoc")
      assert(compilesProtobuf(s"$id-plain", loadDefs(), Map("optimize_for" -> "CODE_SIZE")))
    }
  }

  private def require(tools: String*) = {
    assume(IzFiles.haveExecutables(tools: _*), s"One of required tools is not available: $tools")
  }
}
