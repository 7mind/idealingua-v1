package izumi.idealingua.compiler

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto

import izumi.idealingua.model.publishing.manifests._

trait PlatformEnumCodecs {
  implicit def decScalaProjectLayout: Decoder[ScalaProjectLayout] = semiauto.deriveEnumerationDecoder
  implicit def decTypeScriptProjectLayout: Decoder[TypeScriptProjectLayout] = semiauto.deriveEnumerationDecoder
  implicit def decGoProjectLayout: Decoder[GoProjectLayout] = semiauto.deriveEnumerationDecoder
  implicit def decCSharpProjectLayout: Decoder[CSharpProjectLayout] = semiauto.deriveEnumerationDecoder

  implicit def encScalaProjectLayout: Encoder[ScalaProjectLayout] = semiauto.deriveEnumerationEncoder
  implicit def encTypeScriptProjectLayout: Encoder[TypeScriptProjectLayout] = semiauto.deriveEnumerationEncoder
  implicit def encGoProjectLayout: Encoder[GoProjectLayout] = semiauto.deriveEnumerationEncoder
  implicit def encCSharpProjectLayout: Encoder[CSharpProjectLayout] = semiauto.deriveEnumerationEncoder
}
