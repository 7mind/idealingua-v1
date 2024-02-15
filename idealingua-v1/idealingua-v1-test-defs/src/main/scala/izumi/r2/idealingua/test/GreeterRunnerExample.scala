package izumi.r2.idealingua.test

import _root_.io.circe.syntax.*
import izumi.idealingua.runtime.rpc.{IRTOutputMiddleware, IRTServerMultiplexor}
import izumi.r2.idealingua.test.generated.GreeterServiceServerWrapped
import zio.*

object GreeterRunnerExample {
  def main(args: Array[String]): Unit = {
    val greeter     = new GreeterServiceServerWrapped[IO, Unit](new impls.AbstractGreeterServer.Impl[IO, Unit]())
    val multiplexor = new IRTServerMultiplexor.FromServices[IO, Unit](Set(greeter), IRTOutputMiddleware.empty)

    val req1  = new greeter.greet.signature.Input("John", "Doe")
    val json1 = req1.asJson
    println(json1)

    val req2  = new greeter.alternative.signature.Input()
    val json2 = req2.asJson
    println(json2)

    val invoked1 = multiplexor.invokeMethod(greeter.greet.signature.id)((), json1)
    val invoked2 = multiplexor.invokeMethod(greeter.alternative.signature.id)((), json1)

    implicit val unsafe: Unsafe = Unsafe.unsafe(identity)
    println(zio.Runtime.default.unsafe.run(invoked1).getOrThrowFiberFailure())
    println(zio.Runtime.default.unsafe.run(invoked2).getOrThrowFiberFailure())
  }
}
