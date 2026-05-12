package izumi.idealingua.translator.toscala.domain

import _root_.io.circe.{DecodingFailure, Json}
import izumi.functional.bio.IO2
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod
import izumi.idealingua.translator.toscala.products.CogenProduct.CogenServiceProduct
import izumi.idealingua.translator.toscala.products.RenderableCogenProduct
import izumi.idealingua.translator.toscala.types.runtime
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta._

/** Renders a new-IR `TypeDef.Service` (or `TypeDef.Buzzer` via the
  * service-shaped projection) as the same scala.meta `Defn`s the legacy
  * `ServiceRenderer.renderService` produces (modulo the extension chain).
  *
  * IMPL-7a.2 Phase B M4 (relaxed parity): emits server / client traits,
  * wrapped client / server classes, methods + codecs objects — same shape
  * the legacy renderer produces and the IRT runtime expects. Per-method
  * codegen is delegated to `DomainServiceMethodProduct`, which resolves
  * pre-synthesized input / output struct ids in `domain.flattenedStructs`
  * and the Output ADTs in `domain.userTypes` (placed by Phase 7
  * `EphemeralSynthesizer`).
  *
  * Buzzer dispatch: per F16 absorption (services + buzzers share the same
  * widened ID hierarchy), `renderBuzzer` reuses this renderer body via
  * `DomainServiceContext.forBuzzer`, mirroring legacy `Buzzer.asService`.
  */
final class DomainServiceRenderer(ctx: DomainSTContext) {

  def renderService(svc: NewTypeDef.Service): RenderableCogenProduct =
    renderImpl(DomainServiceContext.forService(ctx, svc))

  def renderBuzzer(bz: NewTypeDef.Buzzer): RenderableCogenProduct =
    renderImpl(DomainServiceContext.forBuzzer(ctx, bz))

  private def renderImpl(c: DomainServiceContext): CogenServiceProduct = {
    val decls = c.methods.collect { case rpc: RPCMethod => rpc }
      .map(DomainServiceMethodProduct(ctx, c, _))

    val qqServer =
      q"""trait ${c.svcServerTpe.typeName}[Or[+_, +_], ${c.Ctx.p}] {
            type Just[+T] = Or[Nothing, T]
            ..${decls.map(_.defnServer)}
          }"""

    val qqClient =
      q"""trait ${c.svcClientTpe.typeName}[Or[+_, +_]] {
            type Just[+T] = Or[Nothing, T]
            ..${decls.map(_.defnClient)}
          }"""

    val qqClientWrapped =
      q"""class ${c.svcWrappedClientTpe.typeName}[Or[+_, +_] : IRTIO2](_dispatcher: ${ctx.rt.IRTDispatcher.parameterize(List(c.F.t)).typeFull})
               extends ${c.svcClientTpe.parameterize(List(c.F.t)).init()} {
               final val _F: IRTIO2[${c.F.t}] =  implicitly
               ${c.methodImport}

               ..${decls.map(_.defnClientWrapped)}
          }"""

    val qqClientWrappedCompanion =
      q"""
         object ${c.svcWrappedClientTpe.termName} extends ${ctx.rt.IRTWrappedClient.init()} {
           val allCodecs: Map[${ctx.rt.IRTMethodId.typeName}, IRTCirceMarshaller] = {
             Map(..${decls.map(_.defnCodecRegistration)})
           }
         }
       """

    val qqServerWrapped =
      q"""class ${c.svcWrappedServerTpe.typeName}[Or[+_, +_] : IRTIO2, ${c.Ctx.p}](
              _service: ${c.svcServerTpe.typeName}[${c.F.t}, ${c.Ctx.t}]
            )
               extends IRTWrappedService[${c.F.t}, ${c.Ctx.t}] {
            final val _F: IRTIO2[${c.F.t}] = implicitly

            final val serviceId: ${ctx.rt.IRTServiceId.typeName} = ${c.svcMethods.termName}.serviceId

            val allMethods: Map[${ctx.rt.IRTMethodId.typeName}, IRTMethodWrapper[${c.F.t}, ${c.Ctx.t}]] = {
              Seq[IRTMethodWrapper[${c.F.t}, ${c.Ctx.t}]](..${decls.map(_.defnMethodRegistration)})
                .map(m => m.signature.id -> m)
                .toMap
            }

            ..${decls.map(_.defnServerWrapped)}
          }"""

    val qqServerWrappedCompanion =
      q"""
         object ${c.svcWrappedServerTpe.termName} {
         }
       """

    val qqServiceMethods =
      q"""
         object ${c.svcMethods.termName} {
           final val serviceId: ${ctx.rt.IRTServiceId.typeName} = ${ctx.rt.IRTServiceId.termName}(${Lit.String(c.typeName)})

           ..${decls.map(_.defnMethod)}
           ..${decls.flatMap(_.defStructs)}
         }
       """

    val qqServiceCodecs =
      q"""
         object ${c.svcCodecs.termName} {
          ..${decls.map(_.defnCodec)}
         }
       """

    CogenServiceProduct(
      qqServer,
      qqClient,
      CogenServiceProduct.Pair(qqServerWrapped, qqServerWrappedCompanion),
      CogenServiceProduct.Pair(qqClientWrapped, qqClientWrappedCompanion),
      qqServiceMethods,
      qqServiceCodecs,
      List(
        runtime.Import.from(runtime.Pkg.language, "higherKinds"),
        runtime.Import.from(runtime.Pkg.of[IO2[Nothing]], "IO2", Some("IRTIO2")),
        runtime.Import[Json](Some("IRTJson")),
        runtime.Import[DecodingFailure](Some("IRTDecodingFailure")),
        runtime.Pkg.of[_root_.io.circe.syntax.EncoderOps[Nothing]].`import`,
        ctx.rt.services.`import`,
      ),
    )
  }
}
