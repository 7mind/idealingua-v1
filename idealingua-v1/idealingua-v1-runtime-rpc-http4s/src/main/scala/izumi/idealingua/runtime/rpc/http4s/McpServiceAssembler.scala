package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.IO2
import izumi.idealingua.runtime.rpc.{
  IRTMethodId,
  IRTOutputMiddleware,
  IRTServerMultiplexor,
  IRTWrappedService,
  McpServiceMeta,
  McpToolMeta,
}

/** Exception thrown when two services register the same MCP tool name. */
final class McpToolNameCollisionException(val toolName: String)
  extends RuntimeException(s"MCP tool name collision: '$toolName' is registered by more than one service")

/** Exception thrown when a tool name cannot be resolved to an IRTMethodId
  * within its paired service's method set. */
final class McpToolResolutionException(val toolName: String, val suffix: String, val available: Set[String])
  extends RuntimeException(
    s"Cannot resolve MCP tool '$toolName' (suffix '$suffix') in service method set. " +
      s"Available: ${available.mkString(", ")}"
  )

/** Assembled result produced by [[McpServiceAssembler.assemble]].
  *
  * @param mux          combined [[IRTServerMultiplexor]] over all services
  * @param toolsListJson union `tools/list` envelope: `{"tools": [...]}`
  * @param dispatch     fully-qualified MCP tool name → (target [[IRTMethodId]], wrap flag)
  */
final case class McpAssembly[F[+_, +_], C](
  mux: IRTServerMultiplexor[F, C],
  toolsListJson: Json,
  dispatch: Map[String, (IRTMethodId, Boolean)],
)

/** Factory that assembles N `(IRTWrappedService, McpServiceMeta)` pairs into the
  * inputs that [[McpJsonRpcRoutes]] needs.
  *
  * Construction fails-fast (throws) on duplicate tool names across services.
  */
object McpServiceAssembler {

  /** Assemble all services into a [[McpAssembly]].
    *
    * @param services   pairs of (wrapped service, its MCP metadata)
    * @param middleware output middleware applied to every method in the mux
    * @tparam F  bifunctor effect
    * @tparam C  request context
    * @throws McpToolNameCollisionException if any tool name appears in more than one service
    * @throws McpToolResolutionException    if a tool name cannot be resolved to an IRTMethodId
    */
  def assemble[F[+_, +_]: IO2, C](
    services: Seq[(IRTWrappedService[F, C], McpServiceMeta)],
    middleware: IRTOutputMiddleware[F, C],
  ): McpAssembly[F, C] = {
    // Build the combined IRTServerMultiplexor over the supplied services, then
    // share the collision-check + toolsList + dispatch-build with assembleOverMux.
    val mux: IRTServerMultiplexor[F, C] =
      new IRTServerMultiplexor.FromServices[F, C](
        services.map(_._1).toSet,
        middleware,
      )
    assembleOverMux(services.map(_._2), mux)
  }

  /** Assemble metadata over a CALLER-SUPPLIED multiplexor.
    *
    * Unlike [[assemble]] (which builds its own auth-free `FromServices` mux),
    * this consumes an already-built `mux` — e.g. an authenticated/authorized
    * multiplexor — and only adds the MCP layer over it: collision check, union
    * `tools/list`, and the dispatch map resolved against `mux.methods.keySet`.
    *
    * @param metas the per-service MCP metadata to expose
    * @param mux   the pre-built multiplexor holding every service's methods
    * @throws McpToolNameCollisionException if any tool name appears in more than one meta
    * @throws McpToolResolutionException    if a tool name cannot be resolved within `mux.methods`
    */
  def assembleOverMux[F[+_, +_], C](
    metas: Seq[McpServiceMeta],
    mux: IRTServerMultiplexor[F, C],
  ): McpAssembly[F, C] = {

    // 1. Fail-fast on cross-service tool-name collisions.
    val allToolMetas: Seq[McpToolMeta] = metas.flatMap(_.tools)
    allToolMetas.groupBy(_.toolName).foreach {
      case (toolName, pairs) if pairs.size > 1 =>
        throw new McpToolNameCollisionException(toolName)
      case _ => // no collision
    }

    // 2. Build the union tools/list JSON.
    val toolsListJson: Json = Json.obj("tools" -> Json.fromValues(allToolMetas.map(toolMetaToJson)))

    // 3. Build the dispatch map: toolName -> (IRTMethodId, wrap), resolving each
    //    tool name's suffix against the supplied mux's method set.
    val methodKeys = mux.methods.keySet
    val dispatch: Map[String, (IRTMethodId, Boolean)] =
      allToolMetas.map { toolMeta =>
        val methodId = resolveMethodId(toolMeta.toolName, methodKeys)
        toolMeta.toolName -> (methodId, toolMeta.wrap)
      }.toMap

    McpAssembly(mux, toolsListJson, dispatch)
  }

  /** Resolve a tool name to an [[IRTMethodId]] within a service's method set.
    *
    * MCP tool names are fully-qualified: `<pkg>.<Svc>.<method>`.
    * [[IRTMethodId.toString]] == `"<Svc>.<method>"` (the last two dot-segments).
    * We match on that suffix.
    *
    * @throws McpToolResolutionException if no method key matches the suffix
    */
  private def resolveMethodId(toolName: String, allMethodKeys: Set[IRTMethodId]): IRTMethodId = {
    val parts  = toolName.split('.')
    val suffix = parts.takeRight(2).mkString(".")
    allMethodKeys.find(_.toString == suffix) match {
      case Some(id) => id
      case None =>
        throw new McpToolResolutionException(toolName, suffix, allMethodKeys.map(_.toString))
    }
  }

  /** Render a [[McpToolMeta]] to the JSON shape expected in `tools/list`. */
  private def toolMetaToJson(t: McpToolMeta): Json =
    Json.obj(
      "name"         -> Json.fromString(t.toolName),
      "description"  -> Json.fromString(t.description),
      "inputSchema"  -> t.inputSchema,
      "outputSchema" -> t.outputSchema,
    )
}
