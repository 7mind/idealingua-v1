package izumi.idealingua.compiler

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

import izumi.idealingua.model.publishing.manifests._
import izumi.idealingua.model.publishing.manifests.ProtobufBuildManifest.ProtobufRepositoryOptions

trait PlatformEnumCodecs {
  implicit def decScalaProjectLayout: Decoder[ScalaProjectLayout] = deriveDecoder
  implicit def decTypeScriptProjectLayout: Decoder[TypeScriptProjectLayout] = deriveDecoder
  implicit def decGoProjectLayout: Decoder[GoProjectLayout] = deriveDecoder
  implicit def decCSharpProjectLayout: Decoder[CSharpProjectLayout] = deriveDecoder

  implicit def encScalaProjectLayout: Encoder[ScalaProjectLayout] = deriveEncoder
  implicit def encTypeScriptProjectLayout: Encoder[TypeScriptProjectLayout] = deriveEncoder
  implicit def encGoProjectLayout: Encoder[GoProjectLayout] = deriveEncoder
  implicit def encCSharpProjectLayout: Encoder[CSharpProjectLayout] = deriveEncoder
}
