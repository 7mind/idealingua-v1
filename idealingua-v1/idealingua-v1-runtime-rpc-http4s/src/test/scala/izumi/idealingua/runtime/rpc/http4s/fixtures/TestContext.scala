package izumi.idealingua.runtime.rpc.http4s.fixtures

sealed trait TestContext {
  def user: String
}

final case class PrivateContext(user: String) extends TestContext {
  override def toString: String = s"private: $user"
}

final case class ProtectedContext(user: String) extends TestContext {
  override def toString: String = s"protected: $user"
}

final case class PublicContext(user: String) extends TestContext {
  override def toString: String = s"public: $user"
}
