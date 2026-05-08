package izumi.idealingua.harness

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import io.circe._
import io.circe.parser._

/** PR-03.4 T2 — TS/C# daemon-mode subprocess wrapper. */
private[harness] final class CrossLangDaemon private (
  proc: java.lang.Process,
  stdin: BufferedWriter,
  stdout: BufferedReader,
  langName: String,
) {
  private val nextId = new AtomicInteger(0)

  /** Send a roundtrip request; block until the matching response is read; 10s per-request timeout. */
  def roundtrip(wireId: String, json: String): CrossLangScalaAdapter.RoundtripResponse = {
    val id = s"$langName-${nextId.incrementAndGet()}"
    val request = Json.obj(
      "req"    -> Json.fromString("roundtrip"),
      "id"     -> Json.fromString(id),
      "wireId" -> Json.fromString(wireId),
      "json"   -> Json.fromString(json),
    ).noSpaces
    try {
      stdin.write(request)
      stdin.write('\n')
      stdin.flush()
    } catch {
      case e: Throwable =>
        return CrossLangScalaAdapter.RoundtripResponse(id, ok = false, kind = Some("DriverCrashed"), detail = Some(s"stdin write failed: ${e.getMessage}"))
    }

    readLineWithTimeout(10000L) match {
      case None =>
        CrossLangScalaAdapter.RoundtripResponse(id, ok = false, kind = Some("DriverTimeout"), detail = Some(s"No response within 10s for request $id"))
      case Some(responseLine) =>
        parse(responseLine).toOption match {
          case None =>
            CrossLangScalaAdapter.RoundtripResponse(id, ok = false, kind = Some("DriverCrashed"), detail = Some(s"Unparseable response: $responseLine"))
          case Some(j) =>
            val c = j.hcursor
            CrossLangScalaAdapter.RoundtripResponse(
              id            = c.downField("id").as[String].getOrElse(id),
              ok            = c.downField("ok").as[Boolean].getOrElse(false),
              kind          = c.downField("kind").as[String].toOption,
              detail        = c.downField("detail").as[String].toOption,
              reEncodedJson = c.downField("reEncodedJson").as[String].toOption,
            )
        }
    }
  }

  /** Send shutdown; wait for process exit. */
  def shutdown(): Unit = {
    try {
      stdin.write("""{"req":"shutdown"}""")
      stdin.write('\n')
      stdin.flush()
      stdin.close()
    } catch { case _: Throwable => () }
    if (!proc.waitFor(5, TimeUnit.SECONDS)) {
      val _ = proc.destroyForcibly()
    }
  }

  private def readLineWithTimeout(millis: Long): Option[String] = {
    import scala.concurrent.{Await, Future}
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    try {
      val f = Future(stdout.readLine())
      Await.result(f, millis.millis) match {
        case null => None
        case s    => Some(s)
      }
    } catch {
      case _: java.util.concurrent.TimeoutException => None
      case _: java.io.IOException                   => None
    }
  }
}

private[harness] object CrossLangDaemon {

  /** Spawn the TS daemon. Reuses TypescriptDriverBridge.ensureNpmInstall + ensureIrtSymlink. */
  def spawnTypescript(harnessTsDir: Path): Either[String, CrossLangDaemon] = {
    TypescriptDriverBridge.ensureNpmInstall(harnessTsDir) match {
      case Some(e) => return Left(e)
      case None    => ()
    }
    TypescriptDriverBridge.ensureIrtSymlink(harnessTsDir) match {
      case Some(e) => return Left(e)
      case None    => ()
    }
    val pb = new java.lang.ProcessBuilder("npx", "tsx", "driver.ts", "--daemon")
      .directory(harnessTsDir.toFile)
      .redirectErrorStream(false)
    spawn(pb, "ts")
  }

  /** Spawn the C# daemon. Reuses CSharpDriverBridge.ensureBuild. */
  def spawnCSharp(harnessCSharpDir: Path): Either[String, CrossLangDaemon] = {
    CSharpDriverBridge.ensureBuild(harnessCSharpDir) match {
      case Some(e) => return Left(e)
      case None    => ()
    }
    val driverDll = harnessCSharpDir.resolve("bin/Debug/net9.0/Driver.dll")
    val pb = new java.lang.ProcessBuilder("dotnet", driverDll.toString, "daemon")
      .directory(harnessCSharpDir.toFile)
      .redirectErrorStream(false)
    spawn(pb, "cs")
  }

  private def spawn(pb: java.lang.ProcessBuilder, langName: String): Either[String, CrossLangDaemon] = {
    try {
      val proc   = pb.start()
      val stdin  = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream, StandardCharsets.UTF_8))
      val stdout = new BufferedReader(new InputStreamReader(proc.getInputStream, StandardCharsets.UTF_8))
      Right(new CrossLangDaemon(proc, stdin, stdout, langName))
    } catch {
      case e: Throwable => Left(s"Failed to spawn $langName daemon: ${e.getMessage}")
    }
  }
}
