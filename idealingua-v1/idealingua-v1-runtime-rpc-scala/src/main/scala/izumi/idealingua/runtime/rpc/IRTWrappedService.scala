package izumi.idealingua.runtime.rpc

trait IRTWrappedService[F[_, _], -C] { self =>
  def serviceId: IRTServiceId

  def allMethods: Map[IRTMethodId, IRTMethodWrapper[F, C]]

  /** Optional pointer to this service's generated MCP bridge resource.
    *
    * Defaults to `None`. The code generator overrides this with
    * `Some(<Svc>Mcp.resource)` for every `*WrappedServer` it emits when
    * `emitMcpBridge = true`, making the pointer discoverable from the
    * DI-wired service set instead of an explicit hand-maintained list.
    */
  def mcpResource: Option[McpServiceResource] = None

  final def contramap[D](f: D => C): IRTWrappedService[F, D] = {
    new IRTWrappedService[F, D] {
      override final val serviceId: IRTServiceId = self.serviceId
      override final val allMethods: Map[IRTMethodId, IRTMethodWrapper[F, D]] = {
        self.allMethods.map { case (k, v) => k -> v.contramap(f) }
      }
      override final def mcpResource: Option[McpServiceResource] = self.mcpResource
    }
  }
}
