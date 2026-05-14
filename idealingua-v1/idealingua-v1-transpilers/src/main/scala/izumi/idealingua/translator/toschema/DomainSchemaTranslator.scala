package izumi.idealingua.translator.toschema

import io.circe.{Json, Printer}
import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.model.publishing.manifests.SchemaBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.translator.toschema.domain._
import izumi.idealingua.typer.ir.{Domain, TypeDef}

/** Domain-IR walker for the JSON Schema + MCP target.
  *
  * Mirrors `DomainScalaTranslator` (Phase B M6) — consumes the new typer's
  * `Domain` IR directly, in `parsed.members` declaration order. Each
  * user-type emit produces one entry into `components.schemas`. Aliases are
  * collapsed (no slot); ADTs / Interfaces / Services / Buzzers are stubbed
  * with `{"x-idealingua-todo": "M2"}` at M1.
  */
final class DomainSchemaTranslator(
  domain: Domain,
  parsed: DomainMeshResolved,
  options: CompilerOptions[SchemaBuildManifest],
) extends Translator {

  private val resolver       = new SchemaTypeResolver(domain)
  private val dtoRenderer    = new SchemaDtoRenderer(domain, resolver)
  private val enumRenderer   = new SchemaEnumRenderer
  private val idRenderer     = new SchemaIdentifierRenderer(resolver)
  private val docBuilder     = new SchemaDocBuilder
  private val printer        = Printer.spaces2.copy(dropNullValues = false)

  private val todoStub: Json =
    Json.obj("x-idealingua-todo" -> Json.fromString("M2"))

  override def translate(): Translated = {
    val typesByName: Map[String, TypeDef] =
      domain.userTypes.toSeq.map { case (id, td) => id.name -> td }.toMap

    val components = scala.collection.mutable.LinkedHashMap.empty[String, Json]

    parsed.members.foreach {
      case RawTopLevelDefn.TLDBaseType(raw) =>
        typesByName.get(raw.id.name).foreach(emitTypeDef(_, components))
      case RawTopLevelDefn.TLDNewtype(raw) =>
        typesByName.get(raw.id.name).foreach(emitTypeDef(_, components))
      case RawTopLevelDefn.TLDService(raw) =>
        typesByName.get(raw.id.name).foreach { td =>
          val _ = components.put(td.id.wireId, todoStub)
        }
      case RawTopLevelDefn.TLDBuzzer(raw) =>
        typesByName.get(raw.id.name).foreach { td =>
          val _ = components.put(td.id.wireId, todoStub)
        }
      case _ => ()
    }

    val infoVersion = resolveInfoVersion()
    val description = domain.meta.meta.doc

    val doc = docBuilder.build(
      domainId    = domain.id,
      infoVersion = infoVersion,
      description = description,
      components  = components.toMap,
    )

    val rendered = printer.print(doc) + "\n"

    val pkgPath  = domain.id.toPackage.toList
    val moduleId = ModuleId(pkgPath, "schema.json")

    Translated(domain.id, domain.meta, Seq(Module(moduleId, rendered)))
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
      val _ = out.put(adt.id.wireId, todoStub)
    case ifc: TypeDef.Interface =>
      val _ = out.put(ifc.id.wireId, todoStub)
    case _ =>
      ()
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
