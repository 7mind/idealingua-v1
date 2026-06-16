package izumi.idealingua.translator.toschema

import io.circe.{Json, Printer}
import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.model.publishing.manifests.SchemaBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.translator.toschema.domain._
import izumi.idealingua.typer.ir.{Domain, EphemeralOrigin, Member, TypeDef}

/** Domain-IR walker for the JSON Schema + MCP target.
  *
  * Mirrors `DomainScalaTranslator` (Phase B M6) — consumes the new typer's
  * `Domain` IR directly, in `parsed.members` declaration order. Each
  * user-type emit produces one entry into `components.schemas`.
  *
  * At M3 the service/buzzer stubs are replaced with full MCP
  * `ListToolsResult` envelopes, one `.mcp.json` per service or buzzer.
  * Method input ephemeral DTOs and method output ephemerals
  * (`Singular`/`Struct`/`Void` DTOs + `Algebraic`/`Alternative` ADTs) are
  * emitted into `components.schemas` so `x-idealingua-wireId-input` /
  * `x-idealingua-wireId-output` `$ref`s resolve inside the same OpenAPI
  * document.
  */
final class DomainSchemaTranslator(
  domain: Domain,
  parsed: DomainMeshResolved,
  options: CompilerOptions[SchemaBuildManifest],
) extends Translator {

  private val resolver        = new SchemaTypeResolver(domain)
  private val dtoRenderer     = new SchemaDtoRenderer(domain, resolver)
  private val enumRenderer    = new SchemaEnumRenderer
  private val idRenderer      = new SchemaIdentifierRenderer(resolver)
  private val adtRenderer     = new SchemaAdtRenderer
  private val ifcRenderer     = new SchemaInterfaceRenderer(domain, dtoRenderer)
  private val docBuilder      = new SchemaDocBuilder
  private val methodOutput    = new SchemaMethodOutput(resolver)
  private val serviceRenderer = new SchemaServiceRenderer(domain.id, methodOutput)
  private val buzzerRenderer  = new SchemaBuzzerRenderer(domain.id, methodOutput)
  private val printer         = Printer.spaces2.copy(dropNullValues = false)

  override def translate(): Translated = {
    val typesByName: Map[String, TypeDef] =
      domain.userTypes.toSeq.map { case (id, td) => id.name -> td }.toMap

    val components = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    val mcpModules = scala.collection.mutable.ArrayBuffer.empty[Module]

    val pkgPath = domain.id.toPackage.toList

    parsed.members.foreach {
      case RawTopLevelDefn.TLDBaseType(raw) =>
        typesByName.get(raw.id.name).foreach(emitTypeDef(_, components))
      case RawTopLevelDefn.TLDNewtype(raw) =>
        typesByName.get(raw.id.name).foreach(emitTypeDef(_, components))
      case RawTopLevelDefn.TLDService(raw) =>
        typesByName.get(raw.id.name).foreach {
          case svc: TypeDef.Service =>
            val doc      = serviceRenderer.render(svc)
            val rendered = printer.print(doc) + "\n"
            val moduleId = ModuleId(pkgPath, s"${svc.id.name}.mcp.json")
            mcpModules += Module(moduleId, rendered)
          case _ => ()
        }
      case RawTopLevelDefn.TLDBuzzer(raw) =>
        typesByName.get(raw.id.name).foreach {
          case bz: TypeDef.Buzzer =>
            val doc      = buzzerRenderer.render(bz)
            val rendered = printer.print(doc) + "\n"
            val moduleId = ModuleId(pkgPath, s"${bz.id.name}.mcp.json")
            mcpModules += Module(moduleId, rendered)
          case _ => ()
        }
      case _ => ()
    }

    // Emit interface-mirror ephemeral DTOs (`<Iface>.Struct`) so the
    // SchemaInterfaceRenderer `$ref`s into the same document resolve.
    emitInterfaceMirrors(components)

    // Emit method input/output ephemerals (MethodInput/MethodOutput DTOs +
    // ephemeral ADTs for Algebraic/Alternative outputs) so MCP `*-wireId-*`
    // annotations point at concrete schema components.
    emitMethodEphemerals(components)

    val infoVersion = resolveInfoVersion()
    val description = domain.meta.meta.doc

    val doc = docBuilder.build(
      domainId    = domain.id,
      infoVersion = infoVersion,
      description = description,
      components  = components.toMap,
    )

    val rendered = printer.print(doc) + "\n"
    val moduleId = ModuleId(pkgPath, "schema.json")
    val schema   = Module(moduleId, rendered)

    Translated(domain.id, domain.meta, schema +: mcpModules.toSeq)
  }

  private def emitTypeDef(
    td: TypeDef,
    out: scala.collection.mutable.LinkedHashMap[String, Json],
  ): Unit = td match {
    case _: TypeDef.Alias =>
      ()
    case e: TypeDef.Enum =>
      val _ = out.put(e.id.wireId, enumRenderer.render(e))
    case i: TypeDef.Identifier =>
      val _ = out.put(i.id.wireId, idRenderer.render(i))
    case dto: TypeDef.Dto =>
      val _ = out.put(dto.id.wireId, dtoRenderer.render(dto))
    case adt: TypeDef.Adt =>
      val _ = out.put(adt.id.wireId, adtRenderer.render(adt))
    case ifc: TypeDef.Interface =>
      val _ = out.put(ifc.id.wireId, ifcRenderer.render(ifc))
    case _ =>
      ()
  }

  /** Emits interface-mirror ephemerals (`<Iface>.Struct`) as flat-object
    * schemas. The interface's `oneOf` branches `$ref` these by full wireId.
    * Mirror emission order is sorted by wireId for byte-stable output.
    */
  private def emitInterfaceMirrors(
    out: scala.collection.mutable.LinkedHashMap[String, Json],
  ): Unit = {
    val mirrors = domain.members.collect {
      case (_, Member.Ephemeral(eph)) if eph.origin.isInstanceOf[EphemeralOrigin.InterfaceMirror] =>
        eph
    }.toList.sortBy(_.id.wireId)

    mirrors.foreach { eph =>
      val flatFields = domain.flattenedStructs.get(eph.id).map(_.fields).getOrElse(Nil)
      val schema     = dtoRenderer.renderFromFlat(eph.id, flatFields, None)
      val _          = out.put(eph.id.wireId, schema)
    }
  }

  /** Emits method-input + method-output ephemerals. DTO-shaped ephemerals
    * (`MethodInput`, `MethodOutput` for `Singular`/`Struct`/`Void`) are
    * rendered as flat-object schemas via `SchemaDtoRenderer.renderFromFlat`.
    * ADT-shaped output ephemerals (`Algebraic`/`Alternative`) are placed in
    * `Member.User` by `EphemeralSynthesizer.placeEphemeralAdt` and are
    * rendered via `SchemaAdtRenderer`.
    *
    * Emission order: sorted by wireId for byte-stability across runs.
    */
  private def emitMethodEphemerals(
    out: scala.collection.mutable.LinkedHashMap[String, Json],
  ): Unit = {
    // 1. MethodInput / MethodOutput DTO ephemerals.
    val dtoEphemerals = domain.members.collect {
      case (_, Member.Ephemeral(eph)) =>
        eph.origin match {
          case _: EphemeralOrigin.MethodInput  => Some(eph)
          case _: EphemeralOrigin.MethodOutput => Some(eph)
          case _                               => None
        }
    }.flatten.toList.sortBy(_.id.wireId)

    dtoEphemerals.foreach { eph =>
      val flatFields = domain.flattenedStructs.get(eph.id).map(_.fields).getOrElse(Nil)
      val schema     = dtoRenderer.renderFromFlat(eph.id, flatFields, None)
      val _          = out.put(eph.id.wireId, schema)
    }

    // 2. Ephemeral ADTs (Algebraic / Alternative outputs). These are
    //    `Member.User(TypeDef.Adt)` keyed by AdtId whose owner (`ephemeralOwner`)
    //    points at a service/buzzer. Selecting them via `ephemeralOwner`
    //    avoids accidentally re-emitting user-declared ADTs (which the main
    //    `emitTypeDef` walk already handles).
    val ownedAdts = domain.ephemeralOwner.keysIterator.collect {
      case adtId =>
        domain.userTypes.get(adtId) match {
          case Some(a: TypeDef.Adt) if !out.contains(a.id.wireId) => Some(a)
          case _                                                  => None
        }
    }.flatten.toList.sortBy(_.id.wireId)

    ownedAdts.foreach { adt =>
      val _ = out.put(adt.id.wireId, adtRenderer.render(adt))
    }
  }

  /** Per plan D24: domain `meta.version` annotation (future) → BuildManifest
    * `common.version` → compile-time fallback `0.0.0`.
    */
  private def resolveInfoVersion(): String = {
    val annoVersion = domain.meta.meta.annos.iterator
      .find(_.name == "version")
      .flatMap { a =>
        a.values.values.collectFirst {
          case izumi.idealingua.model.il.ast.typed.ConstValue.CString(v) => v
        }
      }

    annoVersion.getOrElse {
      val v = options.manifest.common.version
      if (v.release) v.version else s"${v.version}-${v.snapshotQualifier}"
    }
  }
}
