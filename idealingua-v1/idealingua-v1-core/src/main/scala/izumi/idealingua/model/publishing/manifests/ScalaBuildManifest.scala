package izumi.idealingua.model.publishing.manifests

import izumi.idealingua.model.publishing.BuildManifest.Common
import izumi.idealingua.model.publishing.{BuildManifest, ProjectNamingRule}

case class ScalaBuildManifest(
  common: Common,
  layout: ScalaProjectLayout,
  sbt: SbtOptions,
  emitMcpBridge: Boolean = false,
) extends BuildManifest

case class SbtOptions(
  projectNaming: ProjectNamingRule,
  enableScalaJs: Boolean,
  scalaVersions: List[String],
  sbtVersion: Option[String],
  enableDocs: Option[Boolean],
) {
  def isCrossBuild: Boolean = scalaVersions.size > 1
}

object SbtOptions {
  def example: SbtOptions = {
    SbtOptions(
      projectNaming = ProjectNamingRule.example,
      enableScalaJs = true,
      scalaVersions = Nil,
      sbtVersion    = None,
      enableDocs    = None,
    )
  }
}

object ScalaBuildManifest {
  def example: ScalaBuildManifest = {
    val common = BuildManifest.Common.example
    ScalaBuildManifest(
      common = common.copy(version = common.version.copy(snapshotQualifier = "SNAPSHOT")),
      layout = ScalaProjectLayout.SBT,
      sbt    = SbtOptions.example,
    )
  }
}

sealed trait ScalaProjectLayout

object ScalaProjectLayout {

  final case object PLAIN extends ScalaProjectLayout

  final case object SBT extends ScalaProjectLayout

}
