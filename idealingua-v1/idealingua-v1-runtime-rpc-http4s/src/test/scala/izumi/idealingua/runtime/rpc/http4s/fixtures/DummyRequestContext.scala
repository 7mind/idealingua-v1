package izumi.idealingua.runtime.rpc.http4s.fixtures

import org.http4s.Credentials

import java.net.InetAddress

final case class DummyRequestContext(ip: Option[InetAddress], credentials: Option[Credentials])
