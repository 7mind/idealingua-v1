package izumi.idealingua.translator.toscala.domain

import io.circe.{Json, JsonObject, Printer}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.translator.toschema.domain.{SchemaAdtRenderer, SchemaDtoRenderer, SchemaEnumRenderer, SchemaIdentifierRenderer, SchemaInterfaceRenderer, SchemaMethodOutput, SchemaTypeResolver}
import izumi.idealingua.typer.ir.{Domain, EphemeralOrigin, Member, TypeDef}

/** MCP bridge codegen — emits a platform-neutral `<ServiceName>Mcp.scala`
  * source file alongside the existing `<ServiceName>.scala` plus a classpath
  * resource `mcp/<ServiceName>.mcp.json` carrying the MCP `ListToolsResult`
  * envelope.
  *
  * Gated by `ScalaBuildManifest.emitMcpBridge` (default `false`). All five
  * `Output` variants (Void / Singular / Struct / Algebraic / Alternative) carry
  * a per-method static `wrap` flag derived from `OutputWrapPolicy.isWrapped`.
  *
  * The emitted Scala source is pure data: a `<ServiceName>Mcp` object holding a
  * lightweight `McpServiceResource` pointer to the `.mcp.json` resource — no
  * schemas, no resource I/O, no transport bindings, so it cross-compiles to
  * Scala.js. The full `McpServiceMeta` is read from the resource at runtime by
  * `McpServiceLoader` and consumed by the http4s interpreter. The `.mcp.json`
  * carries the per-method data (toolName, description, input/output schemas,
  * wire-type ids, kind, wrap).
  *
  * Buzzers are skipped.
  */
final class DomainServiceMcpRenderer(domain: Domain) {

  private val schemaResolver = new SchemaTypeResolver(domain)
  private val methodOutput   = new SchemaMethodOutput(schemaResolver)
  private val dtoRenderer    = new SchemaDtoRenderer(domain, schemaResolver)
  private val enumRenderer   = new SchemaEnumRenderer
  private val idRenderer     = new SchemaIdentifierRenderer(schemaResolver)
  private val adtRenderer    = new SchemaAdtRenderer
  private val ifcRenderer    = new SchemaInterfaceRenderer(domain, dtoRenderer)
  private val jsonPrinter    = Printer.spaces2.copy(dropNullValues = false)

  // Resolver emits `#/components/schemas/<wireId>` (the OpenAPI convention
  // used by the schema.json doc). MCP `tools/list` payloads are
  // self-contained per JSON Schema 2020-12, so refs get rewritten to
  // `#/$defs/<wireId>` and the transitive closure of referenced user-types is
  // embedded under `$defs` of each tool's inputSchema/outputSchema — otherwise
  // a `$ref` resolves to nowhere, since `components` is absent from the MCP
  // envelope.
  private val ComponentRefPrefix = "#/components/schemas/"
  private val DefsRefPrefix      = "#/$defs/"

  /** Per-domain components table keyed by `wireId`. Built lazily — every
    * service in the domain shares the same closure dataset, so we pay
    * once even with multi-service domains.
    */
  private lazy val componentsByWireId: Map[String, Json] = buildComponentsTable()

  /** Per-method MCP data feeding the `*.mcp.json` resource emitter.
    *
    * `kind` is the MCP method-kind discriminator (currently always `"rpc"`).
    */
  private final case class ToolData(
    toolName: String,
    description: String,
    inputSchema: Json,
    outputSchema: Json,
    wireInput: String,
    wireOutput: String,
    kind: String,
    wrap: Boolean,
  )

  /** Render the `<Svc>Mcp` pointer source + the per-service `*.mcp.json`
    * resource for one service. Returns the pair `(scalaSource, resourceJson)`.
    */
  def render(service: TypeDef.Service): (String, String) = {
    val pkg      = domain.id.toPackage.mkString(".")
    val svcName  = service.id.name
    val rpcs     = service.methods.collect { case rpc: DefMethod.RPCMethod => rpc }
    val tools    = rpcs.map(m => toolData(service, m))
    val scalaSrc = renderScalaSource(pkg, svcName, service.id.wireId)
    val json     = renderResource(tools)
    (scalaSrc, json)
  }

  /** Compute the per-method MCP data. The schemas come from the same
    * `methodOutput`/`selfContain` machinery the schema renderer uses, and the
    * `wrap` flag from `OutputWrapPolicy.isWrapped`.
    */
  private def toolData(service: TypeDef.Service, m: DefMethod.RPCMethod): ToolData = {
    val pkg       = domain.id.toPackage.mkString(".")
    val svcName   = service.id.name
    val methodCap = m.name.capitalize
    val svcWireId = service.id.wireId
    ToolData(
      toolName     = s"$pkg.$svcName.${m.name}",
      description  = m.meta.doc.getOrElse(""),
      inputSchema  = selfContain(methodOutput.structSchema(m.signature.input)),
      outputSchema = selfContain(methodOutput.dispatch(m.signature.output)),
      wireInput    = s"$svcWireId.${methodCap}Input",
      wireOutput   = s"$svcWireId.${methodCap}Output",
      kind         = "rpc",
      wrap         = OutputWrapPolicy.isWrapped(m.signature.output),
    )
  }

  /** Emit the per-service `*.mcp.json` envelope (the `tools/list` payload). */
  private def renderResource(tools: List[ToolData]): String = {
    val toolJsons = tools.map {
      t =>
        val fields = scala.collection.mutable.LinkedHashMap.empty[String, Json]
        fields += "name"                          -> Json.fromString(t.toolName)
        fields += "description"                   -> Json.fromString(t.description)
        fields += "inputSchema"                   -> t.inputSchema
        fields += "outputSchema"                  -> t.outputSchema
        fields += "x-idealingua-wire-type-input"  -> Json.fromString(t.wireInput)
        fields += "x-idealingua-wire-type-output" -> Json.fromString(t.wireOutput)
        fields += "x-idealingua-kind"             -> Json.fromString(t.kind)
        Json.fromFields(fields.toList)
    }
    jsonPrinter.print(Json.obj("tools" -> Json.fromValues(toolJsons))) + "\n"
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
      val closure   = transitiveClosure(roots)
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
              v.asString.foreach {
                s =>
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
        componentsByWireId.get(wireId).foreach {
          schema =>
            collectRefs(schema).foreach {
              ref =>
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

    // Types imported from other domains are referenced by method signatures
    // but are NOT in `userTypes`. Without them the `$defs` closure resolves to
    // nothing for cross-domain `$ref`s and object-typed tool parameters arrive as
    // opaque strings. `crossDomainUserTypes` carries the transitive import set.
    domain.crossDomainUserTypes.foreach {
      case (_, td) => emitTypeDef(td, out)
    }

    val ifcMirrors = domain.members.collect {
      case (_, Member.Ephemeral(eph)) if eph.origin.isInstanceOf[EphemeralOrigin.InterfaceMirror] => eph
    }.toList
    ifcMirrors.foreach {
      eph =>
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
    methodEph.foreach {
      eph =>
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

  /** Emit the platform-neutral pointer source: a `<svcName>Mcp` object exposing
    * `val resource: McpServiceResource` — a lightweight POINTER to the
    * `mcp/<svcName>.mcp.json` classpath resource the renderer emits alongside.
    *
    * The object holds NO schemas, NO string literals beyond the pointer fields,
    * and NO resource-reading code — so it is tiny and links on Scala.js. Reading
    * and parsing the named resource into a [[McpServiceMeta]] (including the
    * collision-safe copy selection across modules) is a JVM-only concern handled
    * by `McpServiceLoader` in the http4s runtime; JS users who need the meta can
    * apply their own resource read (e.g. `PortableResource`) to the pointer.
    */
  private def renderScalaSource(pkg: String, svcName: String, svcWireId: String): String = {
    val pkgDecl    = if (pkg.isEmpty) "" else s"package $pkg\n\n"
    val toolPrefix = if (pkg.isEmpty) s"$svcName." else s"$pkg.$svcName."

    s"""${pkgDecl}import izumi.idealingua.runtime.rpc.McpServiceResource
       |
       |object ${svcName}Mcp {
       |  val resource: McpServiceResource = McpServiceResource(
       |    serviceId    = "${scalaEscape(svcWireId)}",
       |    resourcePath = "mcp/${scalaEscape(svcName)}.mcp.json",
       |    toolPrefix   = "${scalaEscape(toolPrefix)}",
       |  )
       |}
       |""".stripMargin
  }

  /** Escape a string for embedding inside a Scala double-quoted string literal.
    * Handles backslash, double-quote, and the control characters that may
    * appear in JSON Schema content (newline, carriage return, tab).
    */
  private def scalaEscape(s: String): String = {
    val sb = new StringBuilder(s.length + 16)
    s.foreach {
      case '\\' => sb.append("\\\\")
      case '"'  => sb.append("\\\"")
      case '\n' => sb.append("\\n")
      case '\r' => sb.append("\\r")
      case '\t' => sb.append("\\t")
      case c    => sb.append(c)
    }
    sb.toString
  }
}
