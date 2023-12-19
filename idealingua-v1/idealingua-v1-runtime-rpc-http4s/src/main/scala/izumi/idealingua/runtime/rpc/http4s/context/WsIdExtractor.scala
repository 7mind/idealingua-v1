package izumi.idealingua.runtime.rpc.http4s.context

trait WsIdExtractor[RequestCtx, WsCtx] {
  def extract(ctx: RequestCtx, previous: Option[WsCtx]): Option[WsCtx]
}

object WsIdExtractor {
  def id[C]: WsIdExtractor[C, C] = (c, _) => Some(c)
}
