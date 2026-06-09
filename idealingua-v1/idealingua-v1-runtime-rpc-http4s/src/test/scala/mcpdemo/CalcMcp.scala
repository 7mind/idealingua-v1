package mcpdemo

import izumi.idealingua.runtime.rpc.McpServiceResource

/** Platform-neutral MCP pointer for `Calc`, mirroring what
  * `DomainServiceMcpRenderer` now emits: a lightweight
  * [[McpServiceResource]] naming the committed `mcp/Calc.mcp.json` classpath
  * resource. The full `McpServiceMeta` is obtained on the JVM via
  * `McpServiceLoader.load(CalcMcp.resource)`.
  */
object CalcMcp {
  val resource: McpServiceResource = McpServiceResource(
    serviceId    = "mcpdemo.Calc",
    resourcePath = "mcp/Calc.mcp.json",
    toolPrefix   = "mcpdemo.Calc.",
  )
}
