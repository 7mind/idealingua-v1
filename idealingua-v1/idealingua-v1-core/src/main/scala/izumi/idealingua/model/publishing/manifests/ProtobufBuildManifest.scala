package izumi.idealingua.model.publishing.manifests

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.BuildManifest.Common

case class ProtobufBuildManifest(
                                  common: Common,
                                ) extends BuildManifest


object ProtobufBuildManifest {
  def example: ProtobufBuildManifest = {
    val common = BuildManifest.Common.example
    ProtobufBuildManifest(
      common = common.copy(version = common.version.copy(snapshotQualifier = "SNAPSHOT")),
    )
  }
}

