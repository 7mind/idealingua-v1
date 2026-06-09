package izumi.idealingua.runtime.rpc

import io.circe.Json

/**
  * Platform-neutral, per-method MCP delta that the interpreter cannot reconstruct
  * from [[IRTMethodSignature]] alone.
  *
  * This is the shared contract: emitted code produces it and the runtime interpreter
  * consumes it. It is pure data — no http4s, no cats-effect — so it cross-compiles on
  * both JVM and JS targets.
  *
  * @param toolName    fully-qualified MCP tool name, `<pkg>.<Svc>.<method>`
  * @param description human-readable tool description
  * @param inputSchema JSON Schema of the tool input
  * @param outputSchema JSON Schema of the tool output
  * @param wireInput   wire-type id of the input
  * @param wireOutput  wire-type id of the output
  * @param kind        method kind discriminator
  * @param wrap        static wrap flag derived from `OutputWrapPolicy.isWrapped`
  */
final case class McpToolMeta(
  toolName: String,
  description: String,
  inputSchema: Json,
  outputSchema: Json,
  wireInput: String,
  wireOutput: String,
  kind: String,
  wrap: Boolean,
)

final case class McpServiceMeta(
  serviceId: String,
  tools: List[McpToolMeta],
)

/**
  * Platform-neutral, lightweight POINTER from a generated `<Svc>Mcp` object to the
  * classpath resource carrying its MCP `ListToolsResult` envelope.
  *
  * It is pure data (no I/O, no circe), so it cross-compiles on both JVM and JS
  * targets — the generated `<Svc>Mcp` object holds ONLY this value. Reading and
  * parsing the named resource into a [[McpServiceMeta]] is a JVM-only concern,
  * performed by `McpServiceLoader` in the http4s runtime.
  *
  * @param serviceId    wire id of the service (`<pkg>.<Svc>`)
  * @param resourcePath classpath path of the `.mcp.json` envelope, e.g. `mcp/<Svc>.mcp.json`
  * @param toolPrefix   fully-qualified tool-name prefix `<pkg>.<Svc>.`, used to
  *                     disambiguate same-simple-name resource copies across modules
  */
final case class McpServiceResource(
  serviceId: String,
  resourcePath: String,
  toolPrefix: String,
)
