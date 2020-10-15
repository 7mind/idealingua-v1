package izumi.idealingua.model.publishing.manifests

import izumi.idealingua.model.publishing.{BuildManifest, ProjectNamingRule}
import izumi.idealingua.model.publishing.BuildManifest.{Common, ManifestDependency}

sealed trait TypeScriptProjectLayout

object TypeScriptProjectLayout {

  final case object YARN extends TypeScriptProjectLayout

  final case object PLAIN extends TypeScriptProjectLayout

}

case class YarnOptions(
                        projectNaming: ProjectNamingRule,
                        scope: String,
                        devDependencies: List[ManifestDependency],
                        peerDependencies: List[ManifestDependency],
                      )

object YarnOptions {
  def example: YarnOptions = YarnOptions(
    projectNaming = ProjectNamingRule.example,
    devDependencies = List(
      ManifestDependency("typescript", "4.0.1"),
    ),
    peerDependencies = List(
      ManifestDependency("moment", "^2.29.1"),
      ManifestDependency("@types/node", "^14.11.8"),
      ManifestDependency("@types/websocket", "1.0.1"),
    ),
    scope = "@TestScope",
  )
}

// https://docs.npmjs.com/files/package.json
// https://github.com/npm/node-semver#prerelease-tags
case class TypeScriptBuildManifest(
                                    common: Common,
                                    layout: TypeScriptProjectLayout,
                                    yarn: YarnOptions
                                  ) extends BuildManifest

object TypeScriptBuildManifest {
  def example: TypeScriptBuildManifest = {
    val common = BuildManifest.Common.example
    TypeScriptBuildManifest(
      common = common.copy(version = common.version.copy(snapshotQualifier = "build.0")),
      layout = TypeScriptProjectLayout.YARN,
      yarn = YarnOptions.example,
    )
  }
}
