package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.common.TypeId.{DTOId, InterfaceId}
import izumi.idealingua.typer.ir.{Domain, FlatField, TypeDef}

/** Emits an Interface JSON Schema as a `oneOf` of single-key wrapper
  * objects keyed by the **full wireId** of each implementing DTO.
  *
  * Wire-format §4: an interface serializes as
  * `{ "<implFullWireId>": { ...impl fields } }`. The implementor set must
  * match the Scala-leg emitter set exactly (the four FROZEN harness
  * contracts ride on byte-stable wire traffic).
  *
  * Implementor set = every `DTOId` key in `Domain.parents` whose transitive
  * interface closure contains the target. This includes (a) user DTOs that
  * extend the interface, and (b) interface-mirror ephemeral DTOs of every
  * descendant interface (the mirrors of the target interface and of any
  * sub-interface), since `StructuralFlattener` populates `parents` for
  * interface-mirror ephemerals as well as for user types.
  *
  * Result sorted by `_.toString` for byte-stable ordering across runs,
  * matching the legacy `DomainCirceTranslatorExtensionBase.emitForInterface`
  * ordering.
  */
final class SchemaInterfaceRenderer(domain: Domain, dtoRenderer: SchemaDtoRenderer) {

  private val InterfaceMirrorSuffix = "Struct"

  def render(ifc: TypeDef.Interface): Json = {
    val implementors = collectImplementors(ifc.id)
    val branches     =
      if (implementors.nonEmpty) implementors.map(branchSchema)
      else List(mirrorBranch(ifc))

    val base = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    base += "title"             -> Json.fromString(ifc.id.wireId)
    ifc.meta.doc.foreach(d => base += "description" -> Json.fromString(d))
    base += "oneOf"             -> Json.fromValues(branches)
    base += "x-idealingua-kind" -> Json.fromString("interface")

    Json.fromFields(base.toList)
  }

  // A foreign interface has no implementors in this domain's `parents`, so it
  // resolves to its own mirror struct instead of a `oneOf` of implementor wrappers.
  private def mirrorBranch(ifc: TypeDef.Interface): Json = {
    val mirror     = DTOId(ifc.id, InterfaceMirrorSuffix)
    val flatFields = domain.findFlatStruct(ifc.id).map(_.fields)
      .getOrElse(ifc.struct.fields.map(FlatField(_, ifc.id, 0)))
    Json.obj(
      "type"                 -> Json.fromString("object"),
      "properties"           -> Json.obj(mirror.wireId -> dtoRenderer.renderFromFlat(mirror, flatFields, None)),
      "required"             -> Json.arr(Json.fromString(mirror.wireId)),
      "additionalProperties" -> Json.False,
    )
  }

  private def branchSchema(impl: DTOId): Json = {
    val discriminator = impl.wireId
    val ref           = Json.obj(
      "$ref" -> Json.fromString(s"#/components/schemas/$discriminator")
    )
    Json.obj(
      "type"                 -> Json.fromString("object"),
      "properties"           -> Json.obj(discriminator -> ref),
      "required"             -> Json.arr(Json.fromString(discriminator)),
      "additionalProperties" -> Json.False,
    )
  }

  /** Collects every DTO whose transitive interface-inheritance closure
    * contains `target`. Three legacy buckets collapse cleanly here because
    * `Domain.parents` is populated for (a) user DTOs, (b) user Interfaces,
    * AND (c) interface-mirror ephemerals (`StructuralFlattener.scala:296-307`).
    *
    * We project to DTO keys only (Interface keys never become wire-format
    * implementors directly — only their mirror DTOs do, which are themselves
    * keys in `parents`).
    *
    * Result sorted by `_.toString` for byte-stable output.
    */
  private def collectImplementors(target: InterfaceId): List[DTOId] = {
    val buf = scala.collection.mutable.LinkedHashSet.empty[DTOId]
    domain.parents.foreach {
      case (id: DTOId, ifaces) if ifaces.contains(target) =>
        val _ = buf.add(id)
      case _ => ()
    }
    buf.toList.sortBy(_.toString)
  }
}
