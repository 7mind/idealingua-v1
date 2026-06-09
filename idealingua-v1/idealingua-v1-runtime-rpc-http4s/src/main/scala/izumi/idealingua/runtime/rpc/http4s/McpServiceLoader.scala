package izumi.idealingua.runtime.rpc.http4s

import io.circe.{Json, parser}
import izumi.idealingua.runtime.rpc.{McpServiceMeta, McpServiceResource, McpToolMeta}

/** JVM-only reader that turns a platform-neutral [[McpServiceResource]] pointer
  * (emitted by `DomainServiceMcpRenderer` into the generated `<Svc>Mcp` object)
  * into a full [[McpServiceMeta]] by reading and parsing the `.mcp.json`
  * classpath resource the pointer names.
  *
  * This is the JVM half of the JS-linkable split: the generated source holds
  * only the pointer (it cross-compiles to Scala.js); the resource read+parse —
  * which relies on `getResources` / `Source.fromURL` and is therefore JVM-only —
  * lives here.
  */
object McpServiceLoader {

  /** Read the `.mcp.json` the pointer names and build the full [[McpServiceMeta]].
    *
    * Collision-safety: the resource is keyed by SIMPLE service name and can
    * collide across modules sharing a classpath (e.g. two `MatchmakingService`
    * envelopes). Enumerate ALL copies via `getClassLoader.getResources` and
    * select the UNIQUE copy whose tools all live under `r.toolPrefix`. Fail fast
    * on zero/ambiguous matches; a single classpath copy is used directly even if
    * its tools[] is empty (no prefix to match against).
    */
  def load(r: McpServiceResource): McpServiceMeta = {
    val tools     = loadEnvelope(r).hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(Vector.empty)
    val toolMetas = tools.toList.map { t =>
      val c            = t.hcursor
      val outputSchema = c.downField("outputSchema").focus.getOrElse(Json.obj())
      McpToolMeta(
        toolName     = stringField(c, "name"),
        description  = stringField(c, "description"),
        inputSchema  = c.downField("inputSchema").focus.getOrElse(Json.obj()),
        outputSchema = outputSchema,
        wireInput    = stringField(c, "x-idealingua-wire-type-input"),
        wireOutput   = stringField(c, "x-idealingua-wire-type-output"),
        kind         = stringField(c, "x-idealingua-kind"),
        wrap         = outputSchema.hcursor.get[Boolean]("x-idealingua-wrapped").toOption.getOrElse(false),
      )
    }
    McpServiceMeta(serviceId = r.serviceId, tools = toolMetas)
  }

  private def loadEnvelope(r: McpServiceResource): Json = {
    val e    = getClass.getClassLoader.getResources(r.resourcePath)
    val urls = {
      val b = List.newBuilder[java.net.URL]
      while (e.hasMoreElements) { val _ = b += e.nextElement() }
      b.result()
    }
    require(urls.nonEmpty, s"Missing classpath resource: ${r.resourcePath}")
    val parsed = urls.map(read)
    val matches = parsed.filter { j =>
      j.hcursor.downField("tools").focus.flatMap(_.asArray).exists { ts =>
        ts.nonEmpty && ts.forall(_.hcursor.get[String]("name").toOption.exists(_.startsWith(r.toolPrefix)))
      }
    }
    matches match {
      case one :: Nil => one
      case Nil        =>
        if (parsed.size == 1) parsed.head
        else throw new IllegalStateException(s"No copy of ${r.resourcePath} has tools under '${r.toolPrefix}' (found ${parsed.size})")
      case many       =>
        throw new IllegalStateException(s"Ambiguous ${r.resourcePath}: ${many.size} copies match '${r.toolPrefix}'")
    }
  }

  private def read(u: java.net.URL): Json = {
    val s = scala.io.Source.fromURL(u, "UTF-8")
    try parser.parse(s.mkString).fold(throw _, identity)
    finally s.close()
  }

  private def stringField(c: io.circe.HCursor, name: String): String =
    c.get[String](name).toOption.getOrElse("")
}
