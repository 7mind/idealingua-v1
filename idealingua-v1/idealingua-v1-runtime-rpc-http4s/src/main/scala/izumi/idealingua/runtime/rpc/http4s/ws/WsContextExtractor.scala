package izumi.idealingua.runtime.rpc.http4s.ws

trait WsContextExtractor[RequestCtx, WsCtx] {
  def extract(ctx: RequestCtx): Option[WsCtx]
}

object WsContextExtractor {
  def id[Ctx]: WsContextExtractor[Ctx, Ctx] = new WsContextExtractor[Ctx, Ctx] {
    override def extract(ctx: Ctx): Option[Ctx] = Some(ctx)
  }
}
