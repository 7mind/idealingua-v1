package izumi.idealingua.runtime.rpc.http4s.fixtures

sealed trait TestContext

final case class PrivateContext(user: String) extends TestContext {
  override def toString: String = "private"
}

final case class ProtectedContext(user: String) extends TestContext {
  override def toString: String = "protected"
}

final case class PublicContext(user: String) extends TestContext {
  override def toString: String = "public"
}
