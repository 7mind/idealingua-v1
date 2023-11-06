package izumi.idealingua.runtime.circe

import io.circe.Codec

/**
  * Provides circe codecs for case classes and sealed traits
  *
  * {{{
  *   final case class Abc(a: String, b: String, c: String)
  *
  *   object Abc extends IRTWithCirce[Abc]
  * }}}
  *
  * To derive codecs for a sealed trait with branches inside its
  * own companion object, use a proxy object - this works around
  * a scala limitation: https://github.com/milessabin/shapeless/issues/837
  *
  * {{{
  *   sealed trait Abc
  *
  *   private abcCodecs extends IRTWithCirce[Abc]
  *
  *   object Abc extends IRTWithCirce(abcCodecs) {
  *     final case class A()
  *     object A extends IRTWithCirce[A]
  *
  *     final case class B()
  *     object B extends IRTWithCirce[B]
  *     final case class C()
  *
  *     object C extends IRTWithCirce[C]
  *   }
  * }}}
  */
abstract class IRTWithCirce[A]()(implicit derivedCodec: => DerivationDerivedCodec[A]) {
  // workaround for https://github.com/milessabin/shapeless/issues/837
  def this(proxy: IRTWithCirce[A]) = this()(DerivationDerivedCodec(proxy.codec))

  implicit lazy val codec: Codec.AsObject[A] = derivedCodec.value
}
