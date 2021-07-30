package izumi.idealingua.runtime.rpc.http4s.fixtures

import com.comcast.ip4s.IpAddress
import org.http4s.Credentials

final case class DummyRequestContext(ip: IpAddress, credentials: Option[Credentials])
