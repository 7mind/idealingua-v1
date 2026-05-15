package izumi.idealingua.translator.toschema.products

import io.circe.Json

/** Internal product produced by `DomainSchemaTranslator` for each emitted
  * MCP `<Service>.mcp.json` / `<Buzzer>.mcp.json` document before the
  * `SchemaLayouter` materialises it as a `Module`.
  *
  * @param path     Relative path components of the emitted file (joined with `/`).
  * @param fileName Bare filename (e.g. `TestService.mcp.json`).
  * @param doc      Top-level MCP `ListToolsResult` JSON envelope.
  */
final case class MCPProduct(path: Seq[String], fileName: String, doc: Json)
