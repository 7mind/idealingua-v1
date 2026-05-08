package izumi.idealingua.compiler

import izumi.idealingua.model.publishing.BuildManifest.{Common, License, MFUrl, ManifestDependency}
import izumi.idealingua.model.publishing.manifests._
import izumi.idealingua.model.publishing.{ProjectNamingRule, ProjectVersion, Publisher}

trait Codecs extends PlatformEnumCodecs {

  import _root_.io.circe._
  import _root_.io.circe.generic.semiauto._

  implicit def decMFUrl: Decoder[MFUrl] = deriveDecoder

  implicit def decLicense: Decoder[License] = deriveDecoder

  implicit def decCommon: Decoder[Common] = deriveDecoder

  implicit def decMdep: Decoder[ManifestDependency] = deriveDecoder

  implicit def decPublisher: Decoder[Publisher] = deriveDecoder

  implicit def decProjectNamingRule: Decoder[ProjectNamingRule] = deriveDecoder

  implicit def decSbtOptions: Decoder[SbtOptions] = deriveDecoder

  implicit def decScalaBuildManifest: Decoder[ScalaBuildManifest] = deriveDecoder

  implicit def decTs: Decoder[TypeScriptBuildManifest] = deriveDecoder

  implicit def decYarnOptions: Decoder[YarnOptions] = deriveDecoder

  implicit def decCs: Decoder[CSharpBuildManifest] = deriveDecoder

  implicit def decNugetOptions: Decoder[NugetOptions] = deriveDecoder

  implicit def encMFUrl: Encoder[MFUrl] = deriveEncoder

  implicit def encLicense: Encoder[License] = deriveEncoder

  implicit def encCommon: Encoder[Common] = deriveEncoder

  implicit def encMdep: Encoder[ManifestDependency] = deriveEncoder

  implicit def encPublisher: Encoder[Publisher] = deriveEncoder

  implicit def encProjectNamingRule: Encoder[ProjectNamingRule] = deriveEncoder

  implicit def encSbtOptions: Encoder[SbtOptions] = deriveEncoder

  implicit def encScalaBuildManifest: Encoder[ScalaBuildManifest] = deriveEncoder

  implicit def encTs: Encoder[TypeScriptBuildManifest] = deriveEncoder

  implicit def encYarnOptions: Encoder[YarnOptions] = deriveEncoder

  implicit def encCs: Encoder[CSharpBuildManifest] = deriveEncoder

  implicit def encNugetOptions: Encoder[NugetOptions] = deriveEncoder
  //

  implicit def decProjectVersion: Decoder[ProjectVersion] = deriveDecoder

  implicit def encProjectVersion: Encoder[ProjectVersion] = deriveEncoder

  implicit def decVersionOverlay: Decoder[VersionOverlay] = deriveDecoder

  implicit def encVersionOverlay: Encoder[VersionOverlay] = deriveEncoder
}

object Codecs extends Codecs {}
