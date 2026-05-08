package izumi.idealingua.harness

import io.circe._
import io.circe.parser._
import java.io.{BufferedWriter, ByteArrayOutputStream, InputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/** PR-03.3b — dotnet build lifecycle + subprocess spawn. T2 implementation. */
private[harness] object CSharpDriverBridge {

  final case class DriverResult(
    wireId: String,
    scenario: String,
    ok: Boolean,
    kind: Option[String],
    detail: Option[String],
    reEncodedJson: Option[String],
  )

  /** Spawn the C# driver as a subprocess; pipe fixtures over stdin; collect stdout. */
  def runDriver(harnessCSharpDir: Path, fixtures: Seq[WireFixtures.FixtureFile]): Either[String, Seq[DriverResult]] = {
    ensureBuild(harnessCSharpDir) match {
      case Some(error) => return Left(error)
      case None        => ()
    }

    val batch = buildBatch(fixtures)

    val driverDll = harnessCSharpDir.resolve("bin/Debug/net9.0/Driver.dll")
    val pb = new java.lang.ProcessBuilder("dotnet", driverDll.toString)
      .directory(harnessCSharpDir.toFile)
      .redirectErrorStream(false)
    val proc = pb.start()

    val stdinWriter = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream, StandardCharsets.UTF_8))
    stdinWriter.write(batch)
    stdinWriter.close()

    val finished = proc.waitFor(60, java.util.concurrent.TimeUnit.SECONDS)
    if (!finished) {
      proc.destroyForcibly()
      return Left("C# driver subprocess timed out after 60s")
    }

    val stderr = new String(readAllBytes(proc.getErrorStream), StandardCharsets.UTF_8)
    if (proc.exitValue() != 0) {
      return Left(s"C# driver exited ${proc.exitValue()}: $stderr")
    }

    val stdout = new String(readAllBytes(proc.getInputStream), StandardCharsets.UTF_8)
    Right(parseDriverResults(stdout, fixtures))
  }

  private[harness] def ensureBuild(dir: Path): Option[String] = {
    val driverDll = dir.resolve("bin/Debug/net9.0/Driver.dll")
    if (Files.exists(driverDll)) None
    else {
      val pb = new java.lang.ProcessBuilder("dotnet", "build", "-c", "Debug")
        .directory(dir.toFile)
        .redirectErrorStream(true)
      val proc = pb.start()
      val finished = proc.waitFor(180, java.util.concurrent.TimeUnit.SECONDS)
      if (!finished) {
        proc.destroyForcibly()
        Some("dotnet build timed out after 180s")
      } else if (proc.exitValue() != 0) {
        val out = new String(readAllBytes(proc.getInputStream), StandardCharsets.UTF_8)
        Some(s"dotnet build exited ${proc.exitValue()}: $out")
      } else None
    }
  }

  private def buildBatch(fixtures: Seq[WireFixtures.FixtureFile]): String = {
    val requestsJson: Seq[Json] = fixtures.map { f =>
      Json.obj(
        "wireId"      -> Json.fromString(f.wireId),
        "fixturePath" -> Json.fromString(f.file.toString),
        "fixtureJson" -> Json.fromString(new String(f.bytes, StandardCharsets.UTF_8)),
      )
    }
    Json.obj("requests" -> Json.fromValues(requestsJson)).noSpaces
  }

  private def parseDriverResults(stdout: String, fixtures: Seq[WireFixtures.FixtureFile]): Seq[DriverResult] = {
    val pathToScenario: Map[String, String] = fixtures.map(f => f.file.toString -> f.scenario).toMap

    val results: Seq[Json] = parse(stdout)
      .toOption
      .flatMap(_.hcursor.downField("results").as[Seq[Json]].toOption)
      .getOrElse(Seq.empty)

    results.map { j =>
      val c           = j.hcursor
      val fixturePath = c.downField("fixturePath").as[String].getOrElse("")
      val scenario    = pathToScenario.getOrElse(fixturePath, scenarioFromPath(fixturePath))
      DriverResult(
        wireId        = c.downField("wireId").as[String].getOrElse(""),
        scenario      = scenario,
        ok            = c.downField("ok").as[Boolean].getOrElse(false),
        kind          = c.downField("kind").as[String].toOption,
        detail        = c.downField("detail").as[String].toOption,
        reEncodedJson = c.downField("reEncodedJson").as[String].toOption,
      )
    }
  }

  /** Extracts the scenario name from a fixture path like `.../wireId/scenario.json`. */
  private def scenarioFromPath(path: String): String = {
    val lastSlash = path.lastIndexOf('/')
    val filename  = if (lastSlash >= 0) path.substring(lastSlash + 1) else path
    if (filename.endsWith(".json")) filename.dropRight(5) else filename
  }

  /** Drains an InputStream into a byte array. Compatible with Java 8 / `-release:8`. */
  private def readAllBytes(is: InputStream): Array[Byte] = {
    val buf   = new ByteArrayOutputStream()
    val chunk = new Array[Byte](8192)
    var n     = is.read(chunk)
    while (n > 0) {
      buf.write(chunk, 0, n)
      n = is.read(chunk)
    }
    buf.toByteArray
  }
}
