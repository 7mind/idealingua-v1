package izumi.idealingua.runtime.rpc.http4s.context

trait WsIdExtractor[RequestCtx, WsCtx] {
  def extract(ctx: RequestCtx): Option[WsCtx]
}

object WsIdExtractor {
  def id[C]: WsIdExtractor[C, C] = c => Some(c)
}
