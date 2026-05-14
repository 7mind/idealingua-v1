package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.common.DomainId

/** Assembles the OpenAPI 3.1 envelope around a sorted set of component
  * schemas (one component per user type by wireId).
  *
  * Per plan §3 envelope + D24 version-fallback chain.
  */
final class SchemaDocBuilder {

  /** @param domainId    Domain id (used as `info.title`).
    * @param infoVersion Resolved `info.version` per D24 fallback chain.
    * @param description Optional `info.description` (from `domain.meta.meta.doc`).
    * @param components  Schemas keyed by `wireId`; will be alphabetically
    *                    sorted at emit time for byte-stable goldens.
    */
  def build(
    domainId: DomainId,
    infoVersion: String,
    description: Option[String],
    components: Map[String, Json],
  ): Json = {
    val sorted = components.toSeq.sortBy(_._1)

    val info = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    info += "title"   -> Json.fromString(domainId.toPackage.mkString("."))
    info += "version" -> Json.fromString(infoVersion)
    description.foreach(d => info += "description" -> Json.fromString(d))

    Json.obj(
      "openapi" -> Json.fromString("3.1.0"),
      "info"    -> Json.fromFields(info.toList),
      "components" -> Json.obj(
        "schemas" -> Json.fromFields(sorted)
      ),
    )
  }
}
