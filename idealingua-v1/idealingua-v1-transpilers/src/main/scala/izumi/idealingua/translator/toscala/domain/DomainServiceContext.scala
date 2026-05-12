package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId.{BuzzerId, ServiceId}
import izumi.idealingua.model.common.{DomainId, IndefiniteId, TypeName, TypePath}
import izumi.idealingua.model.il.ast.typed.{DefMethod, NodeMeta}
import izumi.idealingua.translator.toscala.types.ScalaType
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta._

/** Per-service rendering context for the new-IR Service/Buzzer renderer.
  *
  * IMPL-7a.2 Phase B M4: twin of legacy `types.ServiceContext`. Pre-computes
  * the family of ScalaTypes (server / client / wrapped / methods / codecs)
  * that the renderer body references, plus the method-import term and base
  * `TypePath`.
  *
  * Buzzer support: `TypeDef.Buzzer` is widened to a `Service`-shaped view via
  * `DomainServiceContext.forBuzzer`, mirroring the legacy
  * `Buzzer.asService` bridge. Per F16, `Buzzer` and `Service` share the same
  * structural shape (id + method list), so the renderer body is identical;
  * only the `TypeId` constructor differs.
  */
final case class DomainServiceContext(
  ctx: DomainSTContext,
  serviceId: ServiceId,
  methods: List[DefMethod],
  meta: NodeMeta,
) {

  object IO2 {
    val n: Term.Name = q"_F"
  }

  object F {
    val t: Type.Name      = t"Or"
    val p: Type.Param     = tparam"Or[?, ?]"
  }

  object Ctx {
    val t: Type.Name  = t"C"
    val p: Type.Param = tparam"C"
  }

  val typeName: TypeName = serviceId.name

  private val pkg: Term.Ref = serviceId.domain.toPackage.foldLeft(Term.Name("_root_"): Term.Ref) {
    case (acc, v) => Term.Select(acc, Term.Name(v))
  }

  val methodImport: Import =
    Import(List(Importer(pkg, List(Importee.Rename(Name(typeName), Name("_M"))))))

  val basePath: TypePath = TypePath(serviceId.domain, Seq(typeName))

  val svcBaseTpe: ScalaType =
    ctx.conv.toScala(IndefiniteId(serviceId.domain.toPackage, typeName))

  private def typeId(name: String): ScalaType =
    ctx.conv.toScala(IndefiniteId(serviceId.domain.toPackage, name))

  val svcServerTpe: ScalaType        = typeId(s"${typeName}Server")
  val svcClientTpe: ScalaType        = typeId(s"${typeName}Client")
  val svcWrappedServerTpe: ScalaType = typeId(s"${typeName}WrappedServer")
  val svcWrappedClientTpe: ScalaType = typeId(s"${typeName}WrappedClient")
  val svcMethods: ScalaType          = svcBaseTpe
  val svcCodecs: ScalaType           = typeId(s"${typeName}Codecs")
}

object DomainServiceContext {
  def forService(ctx: DomainSTContext, svc: NewTypeDef.Service): DomainServiceContext =
    DomainServiceContext(ctx, svc.id, svc.methods, svc.meta)

  /** Buzzer → Service-shaped projection (mirror of legacy `Buzzer.asService`). */
  def forBuzzer(ctx: DomainSTContext, bz: NewTypeDef.Buzzer): DomainServiceContext = {
    val asServiceId = mkServiceIdFromBuzzer(bz.id)
    DomainServiceContext(ctx, asServiceId, bz.events, bz.meta)
  }

  private def mkServiceIdFromBuzzer(b: BuzzerId): ServiceId =
    ServiceId(b.domain, b.name)
}
