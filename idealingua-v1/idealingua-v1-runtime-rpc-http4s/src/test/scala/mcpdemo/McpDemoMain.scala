package mcpdemo

import scala.io.Source

import io.circe.{Json, parser}
import izumi.functional.bio.Exit
import izumi.idealingua.runtime.rpc.{IRTMethodId, IRTMethodName, IRTOutputMiddleware, IRTServerMultiplexor, IRTServiceId}
import izumi.idealingua.runtime.rpc.http4s.{Http4sTransportTest, McpJsonRpcRoutes}
import org.http4s.{HttpRoutes, Request}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl

/** Standalone runner for the MCP demo. Boots Blaze on a fixed port,
  * mounts the JSON-RPC adapter at `/mcp`, and the legacy REST routes at
  * their canonical paths (so both transports are reachable for ad-hoc
  * curl probes).
  *
  * Usage:
  *
  *   sbt '++ 2.13.18' 'idealingua-v1-runtime-rpc-http4s/Test/runMain mcpdemo.McpDemoMain [port]'
  *
  * Default port: 18080. Blocks until SIGINT.
  */
object McpDemoMain {

  type BIO[+E, +A] = zio.IO[E, A]

  def main(args: Array[String]): Unit = {
    val port = args.headOption.map(_.toInt).getOrElse(18080)

    implicit val unsafeRun: izumi.functional.bio.UnsafeRun2[BIO]      = Http4sTransportTest.IO2R
    implicit val async2: izumi.functional.bio.Async2[BIO]             = izumi.functional.bio.impl.AsyncZio
    implicit val asyncThrowable: cats.effect.Async[BIO[Throwable, *]] = zio.interop.catz.asyncInstance

    val dsl: Http4sDsl[BIO[Throwable, *]] = Http4sDsl.apply[BIO[Throwable, *]]

    val service: ShapesServer[BIO, Unit] = new ShapesServerImpl[BIO, Unit]
    val mux: IRTServerMultiplexor[BIO, Unit] =
      new IRTServerMultiplexor.FromServices[BIO, Unit](
        Set(new ShapesWrappedServer[BIO, Unit](service)),
        IRTOutputMiddleware.empty[BIO, Unit],
      )

    val extractUnit: Request[BIO[Throwable, *]] => BIO[Throwable, Unit] =
      _ => izumi.functional.bio.F.pure(())

    val restRoutes: HttpRoutes[BIO[Throwable, *]] =
      ShapesMcpRoutes.routes[BIO, Unit](mux, extractUnit, dsl)

    // Load the same tools/list envelope the per-service routes load from
    // classpath, so the JSON-RPC `tools/list` returns byte-equal bytes.
    val toolsListJson: Json = {
      val s = getClass.getClassLoader.getResourceAsStream("mcp/Shapes.mcp.json")
      require(s != null, "missing mcp/Shapes.mcp.json on classpath")
      try parser.parse(Source.fromInputStream(s, "UTF-8").mkString).toTry.get finally s.close()
    }

    // Hand-assembled dispatch map (FQ tool name → (methodId, wrap)) wiring the
    // single Shapes service inline; `McpServiceAssembler` derives this from
    // `McpServiceMeta` deltas for the multi-service case.
    def mid(name: String): IRTMethodId = IRTMethodId(IRTServiceId("Shapes"), IRTMethodName(name))
    val dispatch: Map[String, (IRTMethodId, Boolean)] = Map(
      "mcpdemo.Shapes.ping"        -> (mid("ping"),        false),
      "mcpdemo.Shapes.upper"       -> (mid("upper"),       true),
      "mcpdemo.Shapes.add"         -> (mid("add"),         true),
      "mcpdemo.Shapes.echo"        -> (mid("echo"),        true),
      "mcpdemo.Shapes.divmod"      -> (mid("divmod"),      false),
      "mcpdemo.Shapes.reverse"     -> (mid("reverse"),     true),
      "mcpdemo.Shapes.invertMap"   -> (mid("invertMap"),   true),
      "mcpdemo.Shapes.maybeUpper"  -> (mid("maybeUpper"),  true),
      "mcpdemo.Shapes.nextColor"   -> (mid("nextColor"),   true),
      "mcpdemo.Shapes.makeProfile" -> (mid("makeProfile"), true),
      "mcpdemo.Shapes.pay"         -> (mid("pay"),         true),
      "mcpdemo.Shapes.divideSafe"  -> (mid("divideSafe"),  true),
      "mcpdemo.Shapes.noteValue"   -> (mid("noteValue"),   true),
    )

    val jsonRpc = new McpJsonRpcRoutes[BIO, Unit](
      mux              = mux,
      toolsListJson    = toolsListJson,
      dispatch         = dispatch,
      extractCtx       = extractUnit,
      serverName       = "mcpdemo-shapes",
      serverVersion    = "0.1.0",
      protocolVersion  = McpJsonRpcRoutes.DefaultProtocolVersion,
      mountPath        = "/mcp",
      dsl              = dsl,
    )

    // Compose JSON-RPC adapter (single POST /mcp) with the legacy REST routes
    // (GET /mcp/tools/list, POST /mcp/tools/call). Both transports reachable
    // on the same socket — handy for curl probes.
    import cats.implicits._
    val combined: HttpRoutes[BIO[Throwable, *]] = jsonRpc.routes <+> restRoutes

    val resource = BlazeServerBuilder[BIO[Throwable, *]]
      .bindHttp(port, "127.0.0.1")
      .withHttpApp(combined.orNotFound)
      .resource

    val run: BIO[Throwable, Unit] = resource.use { srv =>
      val msg = s"mcpdemo-shapes MCP server listening on http://127.0.0.1:${srv.address.getPort}/mcp (POST JSON-RPC) + legacy REST"
      izumi.functional.bio.F.syncThrowable {
        println(msg)
        println("Ready. Press Ctrl-C to stop.")
      }.flatMap(_ => izumi.functional.bio.F.never)
    }

    unsafeRun.unsafeRunSync(run) match {
      case Exit.Success(_)                 => ()
      case f: Exit.Failure[?] @unchecked => throw f.trace.toThrowable
    }
  }
}
