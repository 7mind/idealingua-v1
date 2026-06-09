package izumi.idealingua.translator.toscala.domain

import _root_.io.circe.{DecodingFailure, Json}
import izumi.functional.bio.IO2
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod
import izumi.idealingua.translator.toscala.products.CogenProduct.CogenServiceProduct
import izumi.idealingua.translator.toscala.products.RenderableCogenProduct
import izumi.idealingua.translator.toscala.tools.ScalaTextHelpers
import izumi.idealingua.translator.toscala.types.runtime
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Service` (or `TypeDef.Buzzer` via the
  * service-shaped projection) as the same Defns the legacy
  * `ServiceRenderer.renderService` produces (modulo the extension chain).
  *
  * F-TextTree M7..M8f: ported off legacy quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. Each of the 7 top-level outputs (server +
  * client traits, server + client wrapped class/companion pairs, methods
  * + codecs companion objects) is assembled as a `TextTree`, lowered to
  * `String`. The carrier (`CogenServiceProduct.fromTexts`) owns the
  * String → Defn boundary; the methods-object skeleton is parsed and
  * then the per-method defStruct text fragments are appended via
  * `Defn.appendDefinitions` so the printer indent matches the legacy
  * shape exactly.
  *
  * Per-method splice points (server / client decls, wrapped server / client
  * bodies, method-signature objects, codec objects, method registrations,
  * codec registrations) are composed in `DomainServiceMethodProduct`, which
  * exposes each as a `TextTree[ScalaRefHandle]`. Splice ordering matches
  * the legacy renderer byte-for-byte.
  *
  * IMPL-7a.2 Phase B M4 (relaxed parity): emits server / client traits,
  * wrapped client / server classes, methods + codecs objects — same shape
  * the legacy renderer produces and the IRT runtime expects. Per-method
  * codegen is delegated to `DomainServiceMethodProduct`, which resolves
  * pre-synthesized input / output struct ids in `domain.flattenedStructs`
  * and the Output ADTs in `domain.userTypes`.
  *
  * Buzzer dispatch: per F16 absorption (services + buzzers share the same
  * widened ID hierarchy), `renderBuzzer` reuses this renderer body via
  * `DomainServiceContext.forBuzzer`, mirroring legacy `Buzzer.asService`.
  */
final class DomainServiceRenderer(ctx: DomainSTContext) {

  private val resolver = new DomainScalaTextResolver(ctx.conv)

  def renderService(svc: NewTypeDef.Service): RenderableCogenProduct =
    renderImpl(DomainServiceContext.forService(ctx, svc))

  def renderBuzzer(bz: NewTypeDef.Buzzer): RenderableCogenProduct =
    renderImpl(DomainServiceContext.forBuzzer(ctx, bz))

  private def renderImpl(c: DomainServiceContext): CogenServiceProduct = {
    val decls = c.methods.collect { case rpc: RPCMethod => rpc }
      .map(DomainServiceMethodProduct(ctx, c, _))

    val ctxT  = c.Ctx.t
    val ctxP  = c.Ctx.p
    val ft    = c.F.t
    // Pre-rendered scaffolding fragments — the legacy `${...}` interpolations
    // bottom out in scala-source text; we splice the rendered form.
    // F-TextTree M8e..M8f: `c.F.t` is a `String` ("Or"); use the existing
    // `parameterize(List[Type])` API by constructing a single-element type
    // alias holder. Since we only need the rendered text, build the
    // parameterized init once via the ScalaType helper.
    val irtDispatcherText = ctx.rt.IRTDispatcher.parameterize(ft).typeFull.toString
    val irtServiceIdName   = ctx.rt.IRTServiceId.typeName.toString
    val irtServiceIdTerm   = ctx.rt.IRTServiceId.termName.toString
    val irtMethodIdName    = ctx.rt.IRTMethodId.typeName.toString
    val irtWrappedClientInit = ctx.rt.IRTWrappedClient.typeFull.toString
    val svcClientInitFt    = c.svcClientTpe.parameterize(ft).typeFull.toString
    val methodImportText   = c.methodImport

    val svcServer        = c.svcServerTpe.typeName.toString
    val svcClient        = c.svcClientTpe.typeName.toString
    val svcWrappedClient = c.svcWrappedClientTpe.typeName.toString
    val svcWrappedClientTerm = c.svcWrappedClientTpe.termName.toString
    val svcWrappedServer = c.svcWrappedServerTpe.typeName.toString
    val svcWrappedServerTerm = c.svcWrappedServerTpe.termName.toString
    val svcMethodsTerm   = c.svcMethods.termName.toString
    val svcCodecsTerm    = c.svcCodecs.termName.toString
    val typeNameLit      = s""""${c.typeName}""""

    val serverDecls: Seq[TextTree[ScalaRefHandle]] = decls.map(_.defnServer)
    val clientDecls: Seq[TextTree[ScalaRefHandle]] = decls.map(_.defnClient)
    val clientWrappedDecls: Seq[TextTree[ScalaRefHandle]] = decls.map(_.defnClientWrapped)
    val serverWrappedDecls: Seq[TextTree[ScalaRefHandle]] = decls.map(_.defnServerWrapped)
    val methodSigDecls: Seq[TextTree[ScalaRefHandle]] = decls.map(_.defnMethod)
    val codecDecls: Seq[TextTree[ScalaRefHandle]] = decls.map(_.defnCodec)
    val codecRegs: TextTree[ScalaRefHandle] =
      decls.map(_.defnCodecRegistration).join(", ")
    val methodRegs: TextTree[ScalaRefHandle] =
      decls.map(_.defnMethodRegistration).join(", ")

    // The per-method I/O DTOs + their Circe / AnyVal / Cast augmentation
    // come back as pre-rendered Scala-source text from
    // `DomainServiceMethodProduct.defStructsText`. The methods-object skeleton
    // is parsed by `CogenServiceProduct.fromTexts`, then the defStruct
    // strings are appended via `Defn.appendDefinitions` so the printer
    // indent matches the legacy shape exactly. */
    val defStructTexts: List[String] = decls.flatMap(_.defStructsText).toList

    val serverTree: TextTree[ScalaRefHandle] =
      q"""trait $svcServer[$ft[+_, +_], $ctxP] {
         |  type Just[+T] = $ft[Nothing, T]
         |  ${serverDecls.joinN().shift(2).trim}
         |}""".stripMargin

    val clientTree: TextTree[ScalaRefHandle] =
      q"""trait $svcClient[$ft[+_, +_]] {
         |  type Just[+T] = $ft[Nothing, T]
         |  ${clientDecls.joinN().shift(2).trim}
         |}""".stripMargin

    val clientWrappedTree: TextTree[ScalaRefHandle] =
      q"""class $svcWrappedClient[$ft[+_, +_]: IRTIO2](_dispatcher: $irtDispatcherText) extends $svcClientInitFt {
         |  final val _F: IRTIO2[$ft] = implicitly
         |  $methodImportText
         |  ${clientWrappedDecls.joinN().shift(2).trim}
         |}""".stripMargin

    val clientWrappedCompanionTree: TextTree[ScalaRefHandle] =
      q"""object $svcWrappedClientTerm extends $irtWrappedClientInit {
         |  val allCodecs: Map[$irtMethodIdName, IRTCirceMarshaller] = {
         |    Map($codecRegs)
         |  }
         |}""".stripMargin

    // MCP discovery: when the build emits the per-service `<Svc>Mcp` pointer
    // object (same gate as `DomainScalaTranslator.emitService`), the
    // `*WrappedServer` overrides `IRTWrappedService.mcpResource` to point at
    // `<Svc>Mcp.resource`. The reference resolves because the Mcp object is
    // emitted in the same package under the SAME condition. When the flag is
    // off, no override is emitted and the trait's default `None` applies.
    val mcpResourceOverride: TextTree[ScalaRefHandle] =
      if (ctx.options.manifest.emitMcpBridge && c.emitsMcpBridge)
        q"""override def mcpResource: Option[McpServiceResource] = Some(${c.typeName}Mcp.resource)"""
      else q""""""

    val serverWrappedTree: TextTree[ScalaRefHandle] =
      q"""class $svcWrappedServer[$ft[+_, +_]: IRTIO2, $ctxP](_service: $svcServer[$ft, $ctxT]) extends IRTWrappedService[$ft, $ctxT] {
         |  final val _F: IRTIO2[$ft] = implicitly
         |  final val serviceId: $irtServiceIdName = $svcMethodsTerm.serviceId
         |  val allMethods: Map[$irtMethodIdName, IRTMethodWrapper[$ft, $ctxT]] = {
         |    Seq[IRTMethodWrapper[$ft, $ctxT]]($methodRegs).map(m => m.signature.id -> m).toMap
         |  }
         |  $mcpResourceOverride
         |  ${serverWrappedDecls.joinN().shift(2).trim}
         |}""".stripMargin

    // Legacy emits `object $svcWrappedServerTerm { }`. The empty-brace pitfall
    // (M5) applies — parse-back preserves source-level `{}` whereas the legacy
    // printer drops them. Emit the bare object form so the parsed Defn
    // re-prints byte-equal to legacy.
    val serverWrappedCompanionTree: TextTree[ScalaRefHandle] =
      q"""object $svcWrappedServerTerm"""

    val methodsObjTree: TextTree[ScalaRefHandle] =
      q"""object $svcMethodsTerm {
         |  final val serviceId: $irtServiceIdName = $irtServiceIdTerm($typeNameLit)
         |  ${methodSigDecls.joinN().shift(2).trim}
         |}""".stripMargin

    val codecsObjTree: TextTree[ScalaRefHandle] =
      q"""object $svcCodecsTerm {
         |  ${codecDecls.joinN().shift(2).trim}
         |}""".stripMargin

    val clientWrappedPair = CogenServiceProduct.Pair.fromTexts(
      defnClassText = clientWrappedTree.mapRender(resolver.resolve),
      companionText = clientWrappedCompanionTree.mapRender(resolver.resolve),
    )
    val serverWrappedPair = CogenServiceProduct.Pair.fromTexts(
      defnClassText = serverWrappedTree.mapRender(resolver.resolve),
      companionText = serverWrappedCompanionTree.mapRender(resolver.resolve),
    )

    // Note: M8f removed the `fTypeName = Type.Name(ft)` indirection
    // by routing `ft: String` through the existing
    // `ScalaType.parameterize(names: String*)` varargs arm. Both
    // `IRTDispatcher.parameterize(ft).typeFull` and
    // `svcClientTpe.parameterize(ft).typeFull` use that arm.
    val _ = ScalaTextHelpers

    CogenServiceProduct.fromTexts(
      serverText            = serverTree.mapRender(resolver.resolve),
      clientText            = clientTree.mapRender(resolver.resolve),
      serverWrapped         = serverWrappedPair,
      clientWrapped         = clientWrappedPair,
      methodsSkeletonText   = methodsObjTree.mapRender(resolver.resolve),
      methodsDefStructTexts = defStructTexts,
      codecsText            = codecsObjTree.mapRender(resolver.resolve),
      imports               = List(
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
