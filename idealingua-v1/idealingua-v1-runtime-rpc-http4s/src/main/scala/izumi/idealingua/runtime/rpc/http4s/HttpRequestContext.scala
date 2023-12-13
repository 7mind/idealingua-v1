package izumi.idealingua.runtime.rpc.http4s

import org.http4s.Request

// we can't make it a case class, see https://github.com/scala/bug/issues/11239
class HttpRequestContext[F[_]](val request: Request[F], val context: Any)
