package izumi.idealingua.runtime.rpc.http4s

import izumi.idealingua.runtime.rpc.IRTServiceId

final class IRTContextServicesMuxer[F[+_, +_]](
  val contextServices: Set[IRTContextServices[F, ?]]
) {
  private[this] val serviceToContext: Map[IRTServiceId, IRTContextServices[F, ?]] = {
    contextServices.flatMap(m => m.serverMuxer.services.map(s => s.serviceId -> m)).toMap
  }

  def getContextService(id: IRTServiceId): Option[IRTContextServices[F, ?]] = {
    serviceToContext.get(id)
  }
}
