package izumi.idealingua.compiler

import izumi.fundamentals.platform.files.IzFiles
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.translator.IDLLanguage

import java.nio.file.*
import scala.annotation.nowarn
import scala.jdk.CollectionConverters.*
import scala.sys.process.*
import scala.util.Try

@nowarn("msg=lazyLines")
class ArtifactPublisher(targetDir: Path, lang: IDLLanguage, creds: Credentials, manifest: BuildManifest) {
  private val log: CompilerLog = CompilerLog.Default

  def publish(): Either[Throwable, Unit] = ((creds, lang, manifest): @unchecked) match {
    case (c: ScalaCredentials, IDLLanguage.Scala, _)         => publishScala(targetDir, c)
    case (c: TypescriptCredentials, IDLLanguage.Typescript, _) => publishTypescript(targetDir, c)
    case (c: CsharpCredentials, IDLLanguage.CSharp, _)       => publishCsharp(targetDir, c)
    case (c, l, _) if c.lang != l =>
      Left(
        new IllegalArgumentException(
          "Language and credentials type didn't match. " +
          s"Got credentials for $l, expect for ${c.lang}"
        )
      )
  }

  private def publishScala(targetDir: Path, creds: ScalaCredentials): Either[Throwable, Unit] = Try {
    log.log("Prepare to package Scala sources")
    Process(
      "sbt clean package",
      targetDir.toFile,
    ).lineStream.foreach(log.log)

    log.log("Writing credentials file to ")
    val buildFile    = targetDir.toAbsolutePath.resolve("build.sbt")
    val sbtCredsFile = targetDir.toAbsolutePath.resolve(".credentials")

    val credsLines = Seq(
      "\n",
      // TODO: Gigahorse apears to be cause of `Too many follow-up requests: 21` exception during publishing
      "ThisBuild / updateOptions := updateOptions.value.withGigahorse(false)",
      "\n",
      s"""credentials += Credentials(Path("${sbtCredsFile.toAbsolutePath.toString}").asFile)""",
      "\n",
      s"""
         |ThisBuild / publishTo := {
         |  if (isSnapshot.value)
         |    Some("snapshots" at "${creds.sbtSnapshotsRepo}")
         |  else
         |    Some("releases"  at "${creds.sbtReleasesRepo}")
         |}
      """.stripMargin,
    )

    Files.write(buildFile, credsLines.asJava, StandardOpenOption.WRITE, StandardOpenOption.APPEND)

    Files.write(
      sbtCredsFile,
      Seq[String](
        s"realm=${creds.sbtRealm}",
        s"host=${creds.sbtHost}",
        s"user=${creds.sbtUser}",
        s"password=${creds.sbtPassword}",
      ).asJava,
    )

    Process(
      "sbt publish",
      targetDir.toFile,
    ).lineStream.foreach(log.log)
  }.toEither

  private def publishTypescript(targetDir: Path, creds: TypescriptCredentials): Either[Throwable, Unit] = Try {
    log.log("Prepare to package Typescript sources")

    val packagesDir = Files.list(targetDir.resolve("packages")).filter(_.toFile.isDirectory).iterator().asScala.toSeq.head
    val credsFile   = Paths.get(System.getProperty("user.home")).resolve("~/.npmrc")
    val repoName    = creds.npmRepo.replaceAll("http://", "").replaceAll("https://", "")
    val scope       = packagesDir.getFileName
    val processDir  = targetDir.toFile

    log.log(s"Writing credentials in ${credsFile.toAbsolutePath.getFileName}")
    val scriptLines = List(
      Seq("echo", s"Setting NPM registry for scope $scope to $repoName using user & _password method..."),
      Seq("npm", "config", "set", s"$scope:registry", s"${creds.npmRepo}"),
      Seq("npm", "config", "set", s"//$repoName:email", s"${creds.npmEmail}"),
      Seq("npm", "config", "set", s"//$repoName:username", s"${creds.npmUser}"),
      Seq("npm", "config", "set", s"//$repoName:_password", (Seq("echo", "-n", s"${creds.npmPassword}") #| Seq("openssl", "base64")).!!),
    )

    scriptLines.foreach(s => Process(s, processDir).lineStream.foreach(log.log))

    log.log("Publishing NPM packages")

    log.log("Yarn installing")
    Process("yarn install", processDir).lineStream.foreach(log.log)

    log.log("Yarn building ES5")
    Process("yarn build", processDir).lineStream.foreach(log.log)

    log.log("Yarn building ESNext")
    Process("yarn build-es", processDir).lineStream.foreach(log.log)

    def publishToNpm(dir: Path, packageFileName: String): Unit = {
      Files.list(dir).filter(_.toFile.isDirectory).iterator().asScala.foreach {
        module =>
          val cmd = s"npm publish --force --registry ${creds.npmRepo} ${module.toAbsolutePath.toString}"
          log.log(s"Publish ${module.getFileName}. Cmd: `$cmd`")
          Files.copy(packagesDir.resolve(s"${module.getFileName}/$packageFileName"), module.resolve("package.json"))
          Process(cmd, processDir).lineStream.foreach(log.log)
      }
    }

    publishToNpm(targetDir.resolve("dist"), "package.json")
    publishToNpm(targetDir.resolve("dist-es"), "package.es.json")
  }.toEither

  private def publishCsharp(targetDir: Path, creds: CsharpCredentials): Either[Throwable, Unit] = Try {
//    val nuspecDir  = targetDir.resolve("nuspec")
//    val nuspecDirAsFile = nuspecDir.toFile

    val targetDirAsFile = targetDir.toFile

    log.log("Publishing C#...")

    log.log("Preparing credentials")
    Process(
      s"dotnet nuget add source ${creds.nugetRepo} --name IzumiPublishSource --username ${creds.nugetUser} --password ${creds.nugetPassword} --store-password-in-clear-text",
      targetDirAsFile,
    ).#||("true").lineStream.foreach(log.log)

//    Process(
//      s"nuget setapikey ${creds.nugetUser}:${creds.nugetPassword} -Source IzumiPublishSource",
//      nuspecDirAsFile,
//    ).lineStream.foreach(log.log)
//
//    log.log("Publishing")
//    Files.list(nuspecDir).filter(_.getFileName.toString.endsWith(".nuspec")).iterator().asScala.foreach {
//      module =>
//        Try(
//          Process(
//            s"nuget pack ${module.getFileName.toString}",
//            nuspecDirAsFile,
//          ).lineStream.foreach(log.log)
//        )
//    }

    Process(
      s"dotnet build -c Release",
      targetDirAsFile,
    ).lineStream.foreach(log.log)

    IzFiles.walk(targetDirAsFile).filter(_.getFileName.toString.endsWith(".nupkg")).foreach {
      pack =>
        Process(
          s"dotnet nuget push ${pack.toString} -s ${creds.nugetRepo}",
          targetDirAsFile,
        ).lineStream.foreach(log.log)
    }
  }.toEither

}
