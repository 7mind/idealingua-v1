package izumi.idealingua.runtime.rpc

import io.circe.Json
import org.scalatest.wordspec.AnyWordSpec

final class McpServiceMetaTest extends AnyWordSpec {

  "McpServiceMeta" should {
    "round-trip name/wrap/inputSchema unchanged through an in-memory construction" in {
      val inputSchema  = Json.obj("type" -> Json.fromString("object"))
      val outputSchema = Json.obj("type" -> Json.fromString("string"))

      val tool = McpToolMeta(
        toolName = "pkg.Svc.method",
        description = "a tool",
        inputSchema = inputSchema,
        outputSchema = outputSchema,
        wireInput = "pkg.MethodInput",
        wireOutput = "pkg.MethodOutput",
        kind = "query",
        wrap = true,
      )

      val meta = McpServiceMeta(serviceId = "pkg.Svc", tools = List(tool))

      assert(meta.tools.size == 1)
      val read = meta.tools.head
      assert(read.toolName == "pkg.Svc.method")
      assert(read.wrap)
      assert(read.inputSchema == inputSchema)
      assert(read.outputSchema == outputSchema)
      assert(meta.serviceId == "pkg.Svc")
    }
  }
}
