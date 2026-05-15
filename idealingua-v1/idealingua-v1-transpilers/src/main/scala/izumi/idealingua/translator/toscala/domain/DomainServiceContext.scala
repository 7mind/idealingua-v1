package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId.{BuzzerId, ServiceId}
import izumi.idealingua.model.common.{IndefiniteId, TypeName, TypePath}
import izumi.idealingua.model.il.ast.typed.{DefMethod, NodeMeta}
import izumi.idealingua.translator.toscala.types.ScalaType
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

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
  *
  * F-TextTree M8e..M8f: scaffolder ported off legacy quasiquotes. The
  * renderer-facing splice slots (`IO2.n`, `F.{t, p}`, `Ctx.{t, p}`,
  * `methodImport`) carry pre-rendered Scala 3 source text. Callers
  * splice the strings verbatim (no parse-back boundary call). The
  * legacy `q"_F"` / `t"Or"` / `tparam"Or[?, ?]"` / `Import(...)` shapes
  * rendered to the same literal tokens under the Scala 3 printer;
  * emitting those tokens directly removes the round-trip without
  * behavioural change.
  */
final case class DomainServiceContext(
  ctx: DomainSTContext,
  serviceId: ServiceId,
  methods: List[DefMethod],
  meta: NodeMeta,
) {

  object IO2 {
    val n: String = "_F"
  }

  object F {
    val t: String = "Or"
    // Type-param syntax form for `[Or[+_, +_]]` splice sites. The legacy
    // `tparam"Or[?, ?]"` rendered to `Or[+_, +_]` under Scala 3 dialect
    // because the existential `?` lowers to a variant wildcard in the
    // type-param position.
    val p: String = "Or[+_, +_]"
  }

  object Ctx {
    val t: String = "C"
    val p: String = "C"
  }

  val typeName: TypeName = serviceId.name

  private val pkg: List[String] = "_root_" :: serviceId.domain.toPackage.toList

  /** `import _root_.<pkg>.<TypeName> as _M` (Scala 3 dialect form). The
    * legacy `Import(List(Importer(pkgTerm, List(Importee.Rename(_, _)))))`
    * tree printed identically under the Scala 3 dialect (`as` renaming,
    * no braces for a single rename importee). */
  val methodImport: String =
    s"import ${pkg.mkString(".")}.${typeName} as _M"

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
