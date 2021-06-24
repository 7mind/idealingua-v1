package izumi.idealingua.model.publishing.manifests

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.BuildManifest.Common
import izumi.idealingua.model.publishing.manifests.ProtobufBuildManifest.ProtobufRepositoryOptions

case class ProtobufBuildManifest(
                                  common: Common,
                                  options: Map[String, String],
                                  repository: ProtobufRepositoryOptions
                                ) extends BuildManifest


object ProtobufBuildManifest {
  final case class ProtobufRepositoryOptions(repository: String)

  def example: ProtobufBuildManifest = {
    val common = BuildManifest.Common.example
    ProtobufBuildManifest(
      common = common.copy(version = common.version.copy(snapshotQualifier = "SNAPSHOT")),
      options = Map.empty,
      repository = ProtobufRepositoryOptions("test")
    )
  }
}
