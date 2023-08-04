package izumi.idealingua.runtime.rpc.http4s

import scala.concurrent.ExecutionContext

final case class HttpExecutionContext(clientExecutionContext: ExecutionContext)
