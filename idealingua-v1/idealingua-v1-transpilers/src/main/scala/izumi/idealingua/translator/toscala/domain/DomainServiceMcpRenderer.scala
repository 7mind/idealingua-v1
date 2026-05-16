package izumi.idealingua.translator.toscala.domain

import io.circe.{Json, JsonObject, Printer}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.translator.toschema.domain.{
  SchemaAdtRenderer,
  SchemaDtoRenderer,
  SchemaEnumRenderer,
  SchemaIdentifierRenderer,
  SchemaInterfaceRenderer,
  SchemaMethodOutput,
  SchemaTypeResolver,
}
import izumi.idealingua.typer.ir.{Domain, EphemeralOrigin, Member, TypeDef}

/** MCP / HTTP4s bridge codegen — emits a `<ServiceName>Mcp.scala` source
  * file alongside the existing `<ServiceName>.scala` plus a classpath
  * resource `mcp/<ServiceName>.mcp.json` carrying the MCP `ListToolsResult`
  * envelope.
  *
  * Per plan §3.1, §6, §9 (D9). Gated by `ScalaBuildManifest.emitMcpBridge`
  * (default `false`). Mb2 scope: all 5 `Output` variants (Void / Singular /
  * Struct / Algebraic / Alternative) dispatch uniformly through
  * `mux.invokeMethod` per plan D5; the per-method static `wrap` flag is
  * derived from `OutputWrapPolicy.isWrapped` (§3.2).
  *
  * The bridge:
  *   - Has zero per-method codec references — `IRTServerMultiplexor.invokeMethod`
  *     does all codec work.
  *   - Loads `tools/list` from classpath resource `mcp/<ServiceName>.mcp.json`
  *     at first access (D8(b)).
  *   - Routes via pattern-match on the tool `name`
  *     (`<pkg>.<ServiceName>.<methodName>`, D10/D22).
  *   - Wraps non-object responses in `{"result": <raw>}` per the static
  *     compile-time `wrap` flag derived from `OutputWrapPolicy.isWrapped`.
  *   - Emits MCP-spec error envelopes (`isError: true`) for in-band failures;
  *     HTTP 4xx/5xx reserved for transport-layer failures.
  *
  * Buzzers are skipped per D11(b).
  *
  * @see docs/drafts/20260514-mcp-http4s-bridge-plan.md
  */
final class DomainServiceMcpRenderer(domain: Domain) {

  private val schemaResolver = new SchemaTypeResolver(domain)
  private val methodOutput   = new SchemaMethodOutput(schemaResolver)
  private val dtoRenderer    = new SchemaDtoRenderer(domain, schemaResolver)
  private val enumRenderer   = new SchemaEnumRenderer
  private val idRenderer     = new SchemaIdentifierRenderer(schemaResolver)
  private val adtRenderer    = new SchemaAdtRenderer
  private val ifcRenderer    = new SchemaInterfaceRenderer(domain)
  private val jsonPrinter    = Printer.spaces2.copy(dropNullValues = false)

  // Resolver emits `#/components/schemas/<wireId>` (the OpenAPI convention
  // used by the schema.json doc). MCP `tools/list` payloads are
  // self-contained per JSON Schema 2020-12, so refs get rewritten to
  // `#/$defs/<wireId>` and the transitive closure of referenced
  // user-types is embedded under `$defs` of each tool's
  // inputSchema/outputSchema (R-flag fix: $ref previously resolved to
  // nowhere because `components` was absent from the MCP envelope).
  private val ComponentRefPrefix = "#/components/schemas/"
  private val DefsRefPrefix      = "#/$defs/"

  /** Per-domain components table keyed by `wireId`. Built lazily — every
    * service in the domain shares the same closure dataset, so we pay
    * once even with multi-service domains.
    */
  private lazy val componentsByWireId: Map[String, Json] = buildComponentsTable()

  /** Render the bridge code + the per-service `*.mcp.json` resource for one
    * service. Returns the pair `(scalaSource, resourceJson)`.
    */
  def render(service: TypeDef.Service): (String, String) = {
    val pkg     = domain.id.toPackage.mkString(".")
    val svcName = service.id.name
    val rpcs    = service.methods.collect { case rpc: DefMethod.RPCMethod => rpc }
    val scalaSrc = renderScalaSource(pkg, svcName, rpcs)
    val json     = renderResource(service, rpcs)
    (scalaSrc, json)
  }

  /** Re-emit the per-service `*.mcp.json` envelope verbatim from the schema
    * renderer so wrap-policy comes from the same compiler pass (R6 mitigation).
    */
  private def renderResource(service: TypeDef.Service, rpcs: List[DefMethod.RPCMethod]): String = {
    val tools = rpcs.map { m =>
      val pkg          = domain.id.toPackage.mkString(".")
      val svcName      = service.id.name
      val methodCap    = m.name.capitalize
      val toolName     = s"$pkg.$svcName.${m.name}"
      val svcWireId    = service.id.wireId
      val inputWireId  = s"$svcWireId.${methodCap}Input"
      val outputWireId = s"$svcWireId.${methodCap}Output"
      val description  = m.meta.doc.getOrElse("")
      val fields       = scala.collection.mutable.LinkedHashMap.empty[String, Json]
      fields += "name"                          -> Json.fromString(toolName)
      fields += "description"                   -> Json.fromString(description)
      fields += "inputSchema"                   -> selfContain(methodOutput.structSchema(m.signature.input))
      fields += "outputSchema"                  -> selfContain(methodOutput.dispatch(m.signature.output))
      fields += "x-idealingua-wire-type-input"  -> Json.fromString(inputWireId)
      fields += "x-idealingua-wire-type-output" -> Json.fromString(outputWireId)
      fields += "x-idealingua-kind"             -> Json.fromString("rpc")
      Json.fromFields(fields.toList)
    }
    jsonPrinter.print(Json.obj("tools" -> Json.fromValues(tools))) + "\n"
  }

  /** Walk the JSON tree of one tool's `inputSchema` / `outputSchema`,
    * gather every `$ref` that points at `#/components/schemas/…`,
    * compute the transitive closure across the per-domain components
    * table, embed the closure as `$defs`, and rewrite every ref to the
    * `#/$defs/…` form. Returns the original schema verbatim if no refs
    * are present.
    */
  private def selfContain(schema: Json): Json = {
    val roots = collectRefs(schema)
    if (roots.isEmpty) {
      schema
    } else {
      val closure = transitiveClosure(roots)
      val rewritten = rewriteRefs(schema)
      if (closure.isEmpty) {
        rewritten
      } else {
        val defs = Json.fromFields(closure.toSeq.sortBy(_._1).map { case (k, v) => k -> rewriteRefs(v) })
        rewritten.asObject match {
          case Some(o) => Json.fromJsonObject(o.add("$defs", defs))
          case None    => rewritten
        }
      }
    }
  }

  /** Collect the wireIds referenced by all `$ref` strings appearing in
    * `j`. Only `$ref`s pointing into `#/components/schemas/` are
    * relevant — primitive schemas have no refs.
    */
  private def collectRefs(j: Json): Set[String] = {
    val acc = scala.collection.mutable.LinkedHashSet.empty[String]
    def walk(node: Json): Unit = {
      node.asObject match {
        case Some(o) =>
          o.toIterable.foreach {
            case ("$ref", v) =>
              v.asString.foreach { s =>
                if (s.startsWith(ComponentRefPrefix)) acc += s.stripPrefix(ComponentRefPrefix)
              }
            case (_, v) =>
              walk(v)
          }
        case None =>
          node.asArray.foreach(_.foreach(walk))
      }
    }
    walk(j)
    acc.toSet
  }

  /** Iterative fixed-point closure over the components table. Returns
    * the map of `wireId -> schema` for every type reachable from
    * `roots`. Cycles are handled via the `visited` set.
    */
  private def transitiveClosure(roots: Set[String]): Map[String, Json] = {
    val visited = scala.collection.mutable.LinkedHashSet.empty[String]
    val queue   = scala.collection.mutable.Queue.empty[String]
    queue.enqueueAll(roots)
    while (queue.nonEmpty) {
      val wireId = queue.dequeue()
      if (!visited.contains(wireId)) {
        visited += wireId
        componentsByWireId.get(wireId).foreach { schema =>
          collectRefs(schema).foreach { ref =>
            if (!visited.contains(ref)) queue.enqueue(ref)
          }
        }
      }
    }
    visited.iterator.flatMap(w => componentsByWireId.get(w).map(w -> _)).toMap
  }

  /** Tree rewrite — only `"$ref"` strings whose value starts with the
    * components prefix get rewritten; arbitrary string values are left
    * alone, even when they happen to look like a path.
    */
  private def rewriteRefs(j: Json): Json = {
    j.asObject match {
      case Some(o) =>
        val rewritten = JsonObject.fromIterable(o.toIterable.map {
          case ("$ref", v) =>
            v.asString match {
              case Some(s) if s.startsWith(ComponentRefPrefix) =>
                "$ref" -> Json.fromString(DefsRefPrefix + s.stripPrefix(ComponentRefPrefix))
              case _ => "$ref" -> v
            }
          case (k, v) => k -> rewriteRefs(v)
        })
        Json.fromJsonObject(rewritten)
      case None =>
        j.asArray match {
          case Some(arr) => Json.fromValues(arr.map(rewriteRefs))
          case None      => j
        }
    }
  }

  /** Build the per-domain `wireId -> schema` table, mirroring
    * `DomainSchemaTranslator`'s components emission (user-types +
    * interface-mirror ephemerals + method-input/output ephemerals +
    * ephemeral ADTs). Result is read-only and used only for the
    * `$defs` closure walks above.
    */
  private def buildComponentsTable(): Map[String, Json] = {
    val out = scala.collection.mutable.LinkedHashMap.empty[String, Json]

    domain.userTypes.foreach {
      case (_, td) => emitTypeDef(td, out)
    }

    val ifcMirrors = domain.members.collect {
      case (_, Member.Ephemeral(eph)) if eph.origin.isInstanceOf[EphemeralOrigin.InterfaceMirror] => eph
    }.toList
    ifcMirrors.foreach { eph =>
      val flatFields = domain.flattenedStructs.get(eph.id).map(_.fields).getOrElse(Nil)
      val _          = out.put(eph.id.wireId, dtoRenderer.renderFromFlat(eph.id, flatFields, None))
    }

    val methodEph = domain.members.collect {
      case (_, Member.Ephemeral(eph)) =>
        eph.origin match {
          case _: EphemeralOrigin.MethodInput  => Some(eph)
          case _: EphemeralOrigin.MethodOutput => Some(eph)
          case _                               => None
        }
    }.flatten.toList
    methodEph.foreach { eph =>
      val flatFields = domain.flattenedStructs.get(eph.id).map(_.fields).getOrElse(Nil)
      val _          = out.put(eph.id.wireId, dtoRenderer.renderFromFlat(eph.id, flatFields, None))
    }

    out.toMap
  }

  private def emitTypeDef(
    td: TypeDef,
    out: scala.collection.mutable.LinkedHashMap[String, Json],
  ): Unit = td match {
    case _: TypeDef.Alias       => ()
    case e: TypeDef.Enum        => val _ = out.put(e.id.wireId, enumRenderer.render(e))
    case i: TypeDef.Identifier  => val _ = out.put(i.id.wireId, idRenderer.render(i))
    case dto: TypeDef.Dto       => val _ = out.put(dto.id.wireId, dtoRenderer.render(dto))
    case adt: TypeDef.Adt       => val _ = out.put(adt.id.wireId, adtRenderer.render(adt))
    case ifc: TypeDef.Interface => val _ = out.put(ifc.id.wireId, ifcRenderer.render(ifc))
    case _                      => ()
  }

  private def renderScalaSource(pkg: String, svcName: String, rpcs: List[DefMethod.RPCMethod]): String = {
    val pkgDecl = if (pkg.isEmpty) "" else s"package $pkg\n\n"

    val perMethodConsts = rpcs.map(rpc => renderMethodConsts(pkg, svcName, rpc)).mkString("\n")

    val matchArms = rpcs.map(renderMatchArm).mkString("\n")

    val fallthroughPrefix = s""""$pkg.$svcName.""""

    s"""${pkgDecl}import _root_.cats.effect.Async
       |import _root_.io.circe.{Json, parser}
       |import izumi.functional.bio.{Error2, Exit, IO2}
       |import izumi.idealingua.runtime.rpc.{
       |  IRTDecodingException,
       |  IRTGenericFailure,
       |  IRTLimitReachedException,
       |  IRTMethodId,
       |  IRTMethodName,
       |  IRTMissingHandlerException,
       |  IRTServerMultiplexor,
       |  IRTServiceId,
       |  IRTTypeMismatchException,
       |  IRTUnathorizedRequestContextException,
       |  IRTUnparseableDataException,
       |}
       |import org.http4s.{HttpRoutes, Request, Response}
       |import org.http4s.circe._
       |import org.http4s.dsl.Http4sDsl
       |
       |/** MCP / HTTP4s bridge for `$svcName`. Generated by
       |  * `DomainServiceMcpRenderer` per plan §3.1. Mb2 scope: all 5
       |  * `Output` variants (Void / Singular / Struct / Algebraic /
       |  * Alternative) dispatch uniformly through `mux.invokeMethod` per
       |  * plan D5; the static `wrap` flag is derived from
       |  * `OutputWrapPolicy.isWrapped` (§3.2).
       |  *
       |  * Effect typeclasses: bifunctor `F[+_, +_]: IO2: Error2` is the
       |  * user-facing surface (mirrors the IRT runtime + plan D3); http4s
       |  * routes additionally need `cats.effect.Async[F[Throwable, _]]` at the
       |  * edge for its DSL (`Ok(...)`, `req.as[Json]`, etc.) — same pattern
       |  * `HttpServer.scala` uses.
       |  */
       |object ${svcName}McpRoutes {
       |
       |  /** tools/list envelope loaded from classpath resource at first access
       |    * (D8(b)). The translator emits `mcp/$svcName.mcp.json` alongside
       |    * this source file when `emitMcpBridge = true`.
       |    */
       |  private lazy val toolsListJson: Json = {
       |    val stream = getClass.getClassLoader.getResourceAsStream("mcp/$svcName.mcp.json")
       |    require(stream != null, "Missing classpath resource: mcp/$svcName.mcp.json")
       |    try parser.parse(scala.io.Source.fromInputStream(stream, "UTF-8").mkString).toTry.get
       |    finally stream.close()
       |  }
       |
       |$perMethodConsts
       |
       |  def routes[F[+_, +_]: IO2: Error2, C](
       |    mux: IRTServerMultiplexor[F, C],
       |    extractCtx: Request[F[Throwable, _]] => F[Throwable, C],
       |    dsl: Http4sDsl[F[Throwable, _]],
       |  )(implicit AT: Async[F[Throwable, _]]
       |  ): HttpRoutes[F[Throwable, _]] = {
       |    import dsl._
       |    HttpRoutes.of[F[Throwable, _]] {
       |      case GET -> Root / "mcp" / "tools" / "list" =>
       |        Ok(toolsListJson)
       |
       |      case req @ POST -> Root / "mcp" / "tools" / "call" =>
       |        req.as[Json].flatMap { body =>
       |          val nameOpt  = body.hcursor.downField("name").as[String].toOption
       |          val argsJson = body.hcursor.downField("arguments").as[Json].getOrElse(Json.obj())
       |          nameOpt match {
       |$matchArms
       |            case Some(other) if other.startsWith($fallthroughPrefix) =>
       |              Ok(mcpError(-32601, s"Method not found: $$other"))
       |            case _ =>
       |              NotFound()
       |          }
       |        }
       |    }
       |  }
       |
       |  private def call[F[+_, +_]: IO2: Error2, C](
       |    req: Request[F[Throwable, _]],
       |    args: Json,
       |    methodId: IRTMethodId,
       |    wrap: Boolean,
       |    extractCtx: Request[F[Throwable, _]] => F[Throwable, C],
       |    mux: IRTServerMultiplexor[F, C],
       |    dsl: Http4sDsl[F[Throwable, _]],
       |  )(implicit AT: Async[F[Throwable, _]]
       |  ): F[Throwable, Response[F[Throwable, _]]] = {
       |    import dsl._
       |    (for {
       |      ctx <- extractCtx(req)
       |      raw <- mux.invokeMethod(methodId)(ctx, args)
       |    } yield {
       |      val shaped = if (wrap) Json.obj("result" -> raw) else raw
       |      Json.obj(
       |        "content" -> Json.arr(Json.obj(
       |          "type" -> Json.fromString("text"),
       |          "text" -> Json.fromString(shaped.noSpaces),
       |        )),
       |        "structuredContent" -> shaped,
       |        "isError"           -> Json.False,
       |      )
       |    }).sandboxExit.flatMap {
       |      // Plan §9 mapping: IRT semantic failures surface in-band as MCP
       |      // `isError:true` envelopes (HTTP 200). Out-of-band HTTP statuses
       |      // are reserved for transport-layer failures (malformed body,
       |      // route mismatch, unhandled middleware exception) — those are
       |      // already produced by http4s before this `call` runs.
       |      // Codes follow JSON-RPC 2.0 reserved range: -32700 parse,
       |      // -32601 method-not-found, -32602 invalid-params, -32603 internal,
       |      // -32000..-32099 server-defined (rate-limit, unauthorized).
       |      // Diverges from `HttpServer.handleHttpResult` (HTTP 401, 429) per
       |      // plan §9: MCP semantics treat auth + rate-limit as tool-level
       |      // failures, not transport-level.
       |      // Underlying `getMessage` is surfaced for decoding errors; we
       |      // deliberately do NOT surface stack traces or causes (avoid
       |      // leaking server internals).
       |      case Exit.Success(j) => Ok(j)
       |      case Exit.Error(_: IRTMissingHandlerException, _) =>
       |        Ok(mcpError(-32601, s"Method not found: $${methodId.service.value}.$${methodId.methodId.value}"))
       |      case Exit.Error(e: IRTUnparseableDataException, _) =>
       |        Ok(mcpError(-32700, s"Parse error: $${safeMsg(e)}"))
       |      case Exit.Error(e: IRTTypeMismatchException, _) =>
       |        Ok(mcpError(-32602, s"Invalid arguments (type mismatch): $${safeMsg(e)}"))
       |      case Exit.Error(e: IRTDecodingException, _) =>
       |        Ok(mcpError(-32602, s"Invalid arguments: $${safeMsg(e)}"))
       |      case Exit.Error(e: _root_.io.circe.Error, _) =>
       |        Ok(mcpError(-32602, s"Invalid arguments: $${safeMsg(e)}"))
       |      case Exit.Error(e: IRTLimitReachedException, _) =>
       |        Ok(mcpError(-32000, s"Rate limit exceeded: $${safeMsg(e)}"))
       |      case Exit.Error(_: IRTUnathorizedRequestContextException, _) =>
       |        Ok(mcpError(-32001, "Unauthorized"))
       |      case Exit.Error(_: IRTGenericFailure, _) =>
       |        Ok(mcpError(-32603, "Internal error"))
       |      case Exit.Error(t, _) =>
       |        // Surface the exception class name only — never the message
       |        // body or cause chain — so clients can distinguish e.g.
       |        // ArithmeticException from NullPointerException without
       |        // leaking server internals (paths, hostnames, library
       |        // version strings often embedded in `getMessage`).
       |        Ok(mcpError(-32603, s"Internal error ($${t.getClass.getSimpleName})"))
       |      case _ =>
       |        Ok(mcpError(-32603, "Internal error"))
       |    }
       |  }
       |
       |  /** Surface the exception's own message only — never the cause chain
       |    * or stack trace — and fall back to the class name if `getMessage`
       |    * is `null`. Prevents leaking server internals (file paths,
       |    * library-version strings, host names) into the MCP envelope.
       |    *
       |    * `IRTServerMethod.invoke` appends the BIO trace to its decoder
       |    * exception message via `s"…\\nTrace: $$trace"` — strip that
       |    * suffix here so the wire payload carries only the structured
       |    * error (no stack frames).
       |    */
       |  private def safeMsg(t: Throwable): String = {
       |    val m = t.getMessage
       |    val raw = if (m == null) t.getClass.getSimpleName else m
       |    val idx = raw.indexOf("\\nTrace:")
       |    if (idx >= 0) raw.substring(0, idx) else raw
       |  }
       |
       |  private def mcpError(code: Int, msg: String): Json = Json.obj(
       |    "content"           -> Json.arr(Json.obj(
       |      "type" -> Json.fromString("text"),
       |      "text" -> Json.fromString(msg),
       |    )),
       |    "structuredContent" -> Json.obj(
       |      "error" -> Json.obj(
       |        "code"    -> Json.fromInt(code),
       |        "message" -> Json.fromString(msg),
       |      )
       |    ),
       |    "isError"           -> Json.True,
       |  )
       |}
       |""".stripMargin
  }

  private def renderMethodConsts(pkg: String, svcName: String, rpc: DefMethod.RPCMethod): String = {
    val mName = rpc.name
    s"""  private val toolName_$mName = "$pkg.$svcName.$mName"""" + "\n" +
      s"""  private val methodId_$mName = IRTMethodId(IRTServiceId("$svcName"), IRTMethodName("$mName"))"""
  }

  /** Pattern-match arm. Mb2: all 5 `Output` variants dispatch uniformly
    * through `call(...)` -> `mux.invokeMethod` (D5). The static `wrap` flag
    * is derived from `OutputWrapPolicy.isWrapped` (D6 / §3.2) and determines
    * whether the raw response gets envelope'd into `{"result": <raw>}`.
    */
  private def renderMatchArm(rpc: DefMethod.RPCMethod): String = {
    val mName   = rpc.name
    val wrap    = OutputWrapPolicy.isWrapped(rpc.signature.output)
    val wrapLit = if (wrap) "true" else "false"
    s"""            case Some(`toolName_$mName`) =>
       |              call(req, argsJson, methodId_$mName, wrap = $wrapLit, extractCtx, mux, dsl)""".stripMargin
  }
}
