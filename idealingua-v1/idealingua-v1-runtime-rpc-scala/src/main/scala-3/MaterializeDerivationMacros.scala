package izumi.idealingua.runtime.circe

import io.circe.{Codec, Decoder, Encoder, derivation}

import scala.deriving.Mirror

final case class DerivationDerivedEncoder[A](value: Encoder.AsObject[A]) extends AnyVal
object DerivationDerivedEncoder {
  inline implicit def materialize[A: Mirror.Of]: DerivationDerivedEncoder[A] = {
    import _root_.izumi.idealingua.runtime.circe.IRTTimeInstances.{*, given}

    DerivationDerivedEncoder(
      io.circe.generic.auto.deriveEncoder[A].instance
    )
  }
}

final case class DerivationDerivedDecoder[A](value: Decoder[A]) extends AnyVal
object DerivationDerivedDecoder {
  inline implicit def materialize[A: Mirror.Of]: DerivationDerivedDecoder[A] = {
    import _root_.izumi.idealingua.runtime.circe.IRTTimeInstances.{*, given}

    DerivationDerivedDecoder(
      io.circe.generic.auto.deriveDecoder[A].instance
    )
  }
}

final case class DerivationDerivedCodec[A](value: Codec.AsObject[A]) extends AnyVal
object DerivationDerivedCodec {
  inline implicit def materialize[A: Mirror.Of]: DerivationDerivedCodec[A] = {
    import _root_.izumi.idealingua.runtime.circe.IRTTimeInstances.{*, given}

    DerivationDerivedCodec(
      Codec.AsObject.from(
        io.circe.generic.auto.deriveDecoder[A].instance,
        io.circe.generic.auto.deriveEncoder[A].instance,
      )
    )
  }
}
