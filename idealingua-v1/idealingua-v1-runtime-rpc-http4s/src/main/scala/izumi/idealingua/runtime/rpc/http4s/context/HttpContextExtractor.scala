package izumi.idealingua.runtime.rpc.http4s.context

import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import org.http4s.Request
import org.http4s.headers.`X-Forwarded-For`

trait HttpContextExtractor[RequestCtx] {
  def extract[F[_, _]](request: Request[F[Throwable, _]]): RequestCtx
}

object HttpContextExtractor {
  def authContext: HttpContextExtractor[AuthContext] = new HttpContextExtractor[AuthContext] {
    override def extract[F[_, _]](request: Request[F[Throwable, _]]): AuthContext = {
      val networkAddress = request.headers
        .get[`X-Forwarded-For`]
        .flatMap(_.values.head.map(_.toInetAddress))
        .orElse(request.remote.map(_.host.toInetAddress))
      val headers = request.headers
      AuthContext(headers, networkAddress)
    }
  }
}
