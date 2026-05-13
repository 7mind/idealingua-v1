package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.TypeId.{AdtId, DTOId}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod
import izumi.idealingua.translator.toscala.domain.extensions.{
  DomainAnyvalExtension,
  DomainCastSimilarExtension,
  DomainCastUpExtension,
  DomainCirceDerivationTranslatorExtension,
}
import izumi.idealingua.translator.toscala.products.CogenProduct
import izumi.idealingua.translator.toscala.types.{ClassSource, ScalaField, ScalaType}
import izumi.idealingua.typer.ir.{FlatStruct, TypeDef => NewTypeDef}

import scala.meta.Defn

/** Per-method rendering helper for the Domain-IR Service / Buzzer renderer.
  *
  * F-TextTree M7: ported off `scala.meta` quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. Type references travel as
  * `ScalaRefHandle.{TypeFull, TermFull}` value nodes; helpers expose
  * `TextTree[ScalaRefHandle]` for splice points so the parent
  * `DomainServiceRenderer` can assemble the 7 top-level Defns as text and
  * parse them back via `DomainScalaParseBack` at a single boundary.
  *
  * Supporting `scala.meta` scaffolding (`DomainCompositeStructure`'s
  * `Term.Param` / `Defn.Def` fragments, `DomainScalaStruct.scalaStruct`'s
  * field shapes, and the extension family's `Defn` emissions) is spliced
  * as pre-rendered text via `DomainScalaParseBack.renderS30(_)` — the same
  * pattern M6 established for the structural renderers (`Composite`,
  * `Interface`, `Id`). Migrating the scaffolding/extensions off `scala.meta`
  * is M8 scope.
  *
  * **Carrier strategy** (same as M5/M6): `defStructs: List[Defn]` is the
  * only `Defn`-returning method retained — it composes the Circe sibling
  * + AnyVal + Cast* augmented method I/O `CogenProduct[Defn.Class]`
  * surface via `withCirce(...)`, exactly as legacy did. The list is
  * spliced into the parent methods-object as pre-rendered text by the
  * renderer.
  *
  * **Byte parity**: every emitted text fragment is parse-roundtrip-safe
  * (no empty `{}` pitfalls beyond what M5/M6 already characterised).
  * `verifyGoldens` byte-equal across the corpus, both Scala 2.13 and 3.x.
  *
  * IMPL-7a.2 Phase B M4 (relaxed parity): twin of legacy
  * `types.ServiceMethodProduct`. Same code-emission strategy (Input
  * struct + Output struct/ADT + per-method object + Circe codec + server /
  * client wrapped variants), but every typespace lookup is replaced by a
  * direct read off `Domain.flattenedStructs` / `Domain.userTypes` (set up
  * by Phase 7 `EphemeralSynthesizer`).
  *
  * Output struct synthesis: per F5c (commit `1eb4377`), `EphemeralSynthesizer`
  * pre-materializes the input/output ephemeral DTOs and the Output/Alt ADTs,
  * so the renderer resolves them by id lookup in `domain.flattenedStructs`
  * and `domain.userTypes`. Auto-wrapped primitive alternative branches are
  * handled identically.
  */
final case class DomainServiceMethodProduct(
  ctx: DomainSTContext,
  sp: DomainServiceContext,
  method: RPCMethod,
) {

  import ctx.conv._

  // -------- Splice helpers (TextTree[ScalaRefHandle]) ------------------

  /** Methods-object body — the per-method `object $name extends IRTMethodSignature { ... }` shape. */
  def defnMethod: TextTree[ScalaRefHandle] = {
    val nameLit       = q""""$name""""
    val inputTypeName = TextTree.value[ScalaRefHandle](ScalaRefHandle.TypeName(Input.typespaceId))
    val outputTypeName = TextTree.value[ScalaRefHandle](ScalaRefHandle.TypeName(Output.wrappedTypespaceTypeId))

    val irtMethodSignatureInit = ctx.rt.IRTMethodSignature.typeFull.toString
    val irtMethodId            = ctx.rt.IRTMethodId.typeName.toString
    val irtMethodIdTerm        = ctx.rt.IRTMethodId.termName.toString
    val irtMethodNameTerm      = ctx.rt.IRTMethodName.termName.toString

    q"""object $name extends $irtMethodSignatureInit {
       |  final val id: $irtMethodId = $irtMethodIdTerm(serviceId, $irtMethodNameTerm($nameLit))
       |  type Input = $inputTypeName
       |  type Output = $outputTypeName
       |}""".stripMargin
  }

  /** Bare method-name term reference for the `Seq[IRTMethodWrapper[...]]( ... )` list. */
  def defnMethodRegistration: TextTree[ScalaRefHandle] = q"$name"

  /** `TestService.<method>.id -> TestServiceCodecs.<method>` pair. */
  def defnCodecRegistration: TextTree[ScalaRefHandle] = {
    val methodsTerm = sp.svcMethods.termName.toString
    val codecsTerm  = sp.svcCodecs.termName.toString
    q"$methodsTerm.$name.id -> $codecsTerm.$name"
  }

  /** Codecs object per-method body. */
  def defnCodec: TextTree[ScalaRefHandle] = {
    val methodsTerm = sp.svcMethods.termName.toString
    q"""object $name extends IRTCirceMarshaller {
       |  import $methodsTerm.$name.*
       |  ${Input.defnEncoder.shift(2).trim}
       |  ${Input.defnDecoder.shift(2).trim}
       |  ${Output.defnEncoder.shift(2).trim}
       |  ${Output.defnDecoder.shift(2).trim}
       |}""".stripMargin
  }

  /** Server-side wrapped method body. */
  def defnServerWrapped: TextTree[ScalaRefHandle] = {
    val ctxT       = DomainScalaParseBack.renderS30(sp.Ctx.t)
    val ioN        = DomainScalaParseBack.renderS30(sp.IO2.n)
    val methodsTerm = sp.svcMethods.termName.toString
    val codecsTerm  = sp.svcCodecs.termName.toString
    val assertionLine =
      "assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)"

    val invoke: TextTree[ScalaRefHandle] = method.signature.output match {
      case DefMethod.Output.Singular(_) =>
        q"""def invoke(ctx: $ctxT, input: Input): Just[Output] = {
           |  $assertionLine
           |  $ioN.map(_service.$name(ctx${Input.sigCallPrefix}))(v => new Output(v))
           |}""".stripMargin

      case DefMethod.Output.Void() =>
        q"""def invoke(ctx: $ctxT, input: Input): Just[Output] = {
           |  $assertionLine
           |  $ioN.map(_service.$name(ctx${Input.sigCallPrefix}))(_ => new Output())
           |}""".stripMargin

      case DefMethod.Output.Algebraic(_) | DefMethod.Output.Struct(_) =>
        q"""def invoke(ctx: $ctxT, input: Input): Just[Output] = {
           |  $assertionLine
           |  _service.$name(ctx${Input.sigCallPrefix})
           |}""".stripMargin

      case DefMethod.Output.Alternative(_, _) =>
        // Negative/positive *branch* types live inside the Output wrapper
        // (e.g. `TestService.AlternativeOutput.Failure`). The wrapper type
        // path is rooted at `svcBaseTpe.within(<OutputName>)` — emit fully
        // pre-rendered via `ScalaType.within(_).typeFull.toString`.
        val wrapTpe = sp.svcBaseTpe.within(Output.wrappedTypespaceName)
        val negBranch = wrapTpe.within(Output.negativeBranchTypeName).typeFull.toString
        val posBranch = wrapTpe.within(Output.positiveBranchTypeName).typeFull.toString
        q"""def invoke(ctx: $ctxT, input: Input): Just[Output] = {
           |  $ioN.redeem(_service.$name(ctx${Input.sigCallPrefix}))(err => _F.pure(new $negBranch(err)), succ => _F.pure(new $posBranch(succ)))
           |}""".stripMargin
    }

    q"""object $name extends IRTMethodWrapper[${DomainScalaParseBack.renderS30(sp.F.t)}, $ctxT] {
       |  import $methodsTerm.$name.*
       |  val signature: $methodsTerm.$name.type = $methodsTerm.$name
       |  val marshaller: $codecsTerm.$name.type = $codecsTerm.$name
       |  ${invoke.shift(2).trim}
       |}""".stripMargin
  }

  /** Client-side wrapped method body. */
  def defnClientWrapped: TextTree[ScalaRefHandle] = {
    val ioN     = DomainScalaParseBack.renderS30(sp.IO2.n)
    val wrapped = sp.svcWrappedClientTpe.termName.toString
    val exception: TextTree[ScalaRefHandle] = {
      val idLit = q""""${sp.typeName}.$wrapped.$name""""
      // The legacy quasiquote produced an interpolated-string AST where
      // `${v.getClass}` is a `Term.Block(Term.Select(v, getClass))`; the
      // printer's canonical form for a Block-arg inside an `s"…"` is the
      // multi-line `s"… ${\n  v.getClass\n}"` shape — match it explicitly
      // so parse-back round-trips byte-equal to golden.
      q"""val id = $idLit
         |val expected = classOf[_M.$name.Input].toString
         |$ioN.terminate(new IRTTypeMismatchException(s"Unexpected type in $$id: $$v, expected $$expected got $${
         |  v.getClass
         |}", v, None))""".stripMargin
    }

    // Body shape mirrors the golden's printer output verbatim — scalameta's
    // Scala 3 printer is idempotent on its own output, so feeding the
    // canonical layout into parse-back round-trips to byte-equal text.
    // Reproducing the *legacy quasiquote source* layout doesn't work
    // because `q"…".syntax` reformats to the printer's canonical shape.
    method.signature.output match {
      case DefMethod.Output.Singular(_) =>
        q"""def $name(${Input.signatureText}): ${Output.outputType} = {
           |  $ioN.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.$name.Input(${Input.sigDirectCallText})), _M.$name.id)))({
           |    err => $ioN.terminate(err)
           |  }, {
           |    case IRTMuxResponse(IRTResBody(v: _M.$name.Output), method) if method == _M.$name.id =>
           |      $ioN.pure(v.value)
           |    case v =>
           |      ${exception.shift(6).trim}
           |  })
           |}""".stripMargin

      case DefMethod.Output.Void() =>
        q"""def $name(${Input.signatureText}): ${Output.outputType} = {
           |  $ioN.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.$name.Input(${Input.sigDirectCallText})), _M.$name.id)))({
           |    err => $ioN.terminate(err)
           |  }, {
           |    case IRTMuxResponse(IRTResBody(_: _M.$name.Output), method) if method == _M.$name.id =>
           |      $ioN.pure(())
           |    case v =>
           |      ${exception.shift(6).trim}
           |  })
           |}""".stripMargin

      case DefMethod.Output.Algebraic(_) | DefMethod.Output.Struct(_) =>
        q"""def $name(${Input.signatureText}): ${Output.outputType} = {
           |  $ioN.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.$name.Input(${Input.sigDirectCallText})), _M.$name.id)))({
           |    err => $ioN.terminate(err)
           |  }, {
           |    case IRTMuxResponse(IRTResBody(v: _M.$name.Output), method) if method == _M.$name.id =>
           |      $ioN.pure(v)
           |    case v =>
           |      ${exception.shift(6).trim}
           |  })
           |}""".stripMargin

      case DefMethod.Output.Alternative(_, _) =>
        val wrapTpe = sp.svcBaseTpe.within(Output.wrappedTypespaceName)
        val negBranch = wrapTpe.within(Output.negativeBranchTypeName).typeFull.toString
        val posBranch = wrapTpe.within(Output.positiveBranchTypeName).typeFull.toString
        q"""def $name(${Input.signatureText}): ${Output.outputType} = {
           |  $ioN.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.$name.Input(${Input.sigDirectCallText})), _M.$name.id)))({
           |    err => $ioN.terminate(err)
           |  }, {
           |    case IRTMuxResponse(IRTResBody(r), method) if method == _M.$name.id =>
           |      r match {
           |        case va: $negBranch =>
           |          $ioN.fail(va.value)
           |        case va: $posBranch =>
           |          $ioN.pure(va.value)
           |        case v =>
           |          ${exception.shift(10).trim}
           |      }
           |    case v =>
           |      ${exception.shift(6).trim}
           |  })
           |}""".stripMargin
    }
  }

  def defnServer: TextTree[ScalaRefHandle] =
    q"def $name(ctx: ${DomainScalaParseBack.renderS30(sp.Ctx.t)}${Input.signaturePrefix}): ${Output.outputType}"

  def defnClient: TextTree[ScalaRefHandle] =
    q"def $name(${Input.signatureText}): ${Output.outputType}"

  /** Per-method `defStructs` — emitted into the methods-object body alongside
    * the per-method signature objects. Returns Defn list (a List of Defn
    * to be spliced as rendered text by the parent renderer).
    */
  def defStructs: List[Defn] = Input.inputDefn ++ Output.outputDefn

  // -------- Internal accessors -----------------------------------------

  protected def name: String = method.name

  // -------- Input rendering --------------------------------------------
  protected object Input {
    def typespaceId: DTOId =
      DTOId(sp.basePath, DomainNameMangling.methodToInputName(method))

    def typespaceType: ScalaType = ctx.conv.toScala(typespaceId)

    private def fields: List[ScalaField] = {
      val flat = ctx.domain.flattenedStructs.getOrElse(
        typespaceId,
        FlatStruct(typespaceId, List.empty, List.empty, List.empty),
      )
      val supers = izumi.idealingua.model.il.ast.typed.Super(
        interfaces      = List.empty,
        concepts        = method.signature.input.concepts,
        removedConcepts = List.empty,
      )
      val scalaStruct = DomainScalaStruct.scalaStruct(typespaceId, flat, supers, ctx.conv, ctx.domain)
      scalaStruct.all
    }

    /** Comma-separated rendered `Term.Param` list — e.g. `firstName: String, secondName: String`. */
    def signatureText: String = {
      import izumi.idealingua.translator.toscala.types.ScalaField._
      val params = fields.toParams
      params.map(DomainScalaParseBack.renderS30(_)).mkString(", ")
    }

    /** `, $signatureText` when non-empty, else "". Used at the
      * `def $name(ctx: $ctxT$signaturePrefix)` server-side splice site so
      * `(ctx: C)` does not pick up a trailing `, ` for zero-arg methods. */
    def signaturePrefix: String = {
      val s = signatureText
      if (s.isEmpty) "" else s", $s"
    }

    /** Comma-separated `input.<field>` accessor list — server call site. */
    def sigCallText: String =
      fields.map(f => s"input.${DomainScalaParseBack.renderS30(f.name)}").mkString(", ")

    /** `, $sigCallText` when non-empty, else "". Used at the
      * `_service.$name(ctx$sigCallPrefix)` server-side wrapped call site
      * so `(ctx)` does not pick up a trailing `, ` for zero-arg methods. */
    def sigCallPrefix: String = {
      val s = sigCallText
      if (s.isEmpty) "" else s", $s"
    }

    /** Comma-separated bare field-name list — client constructor call site. */
    def sigDirectCallText: String =
      fields.map(f => DomainScalaParseBack.renderS30(f.name)).mkString(", ")

    def inputDefn: List[Defn] = {
      val flat = ctx.domain.flattenedStructs.getOrElse(
        typespaceId,
        FlatStruct(typespaceId, List.empty, List.empty, List.empty),
      )
      val supers = izumi.idealingua.model.il.ast.typed.Super(
        interfaces      = List.empty,
        concepts        = method.signature.input.concepts,
        removedConcepts = List.empty,
      )
      val scalaStruct = DomainScalaStruct.scalaStruct(typespaceId, flat, supers, ctx.conv, ctx.domain)
      val composite   = new DomainCompositeStructure(ctx, scalaStruct)
      val stub = stubDto(typespaceId)
      val base = ctx.compositeRenderer.defns(composite, ClassSource.CsDTO(stub)).asInstanceOf[CogenProduct[Defn.Class]]
      withCirce(base, typespaceId, flat, unwrap = false)
    }

    def defnEncoder: TextTree[ScalaRefHandle] =
      q"""def encodeRequest: PartialFunction[IRTReqBody, IRTJson] = {
         |  case IRTReqBody(value: Input) =>
         |    value.asJson
         |}""".stripMargin

    def defnDecoder: TextTree[ScalaRefHandle] =
      q"""def decodeRequest[Or[+_, +_]: IRTIO2]: PartialFunction[IRTJsonBody, Or[IRTDecodingFailure, IRTReqBody]] = {
         |  case IRTJsonBody(m, packet) if m == id =>
         |    this.decoded[Or, IRTReqBody](packet.as[Input].map(v => IRTReqBody(v)))
         |}""".stripMargin
  }

  // -------- Output rendering -------------------------------------------
  protected object Output {
    private def typename: String = DomainNameMangling.methodToOutputName(method)

    /** Bare name of the wrapped output type (DTO or ADT) for use as the
      * suffix in `<ServiceBase>.<OutputName>`. */
    def wrappedTypespaceName: String = typename

    def outputType: TextTree[ScalaRefHandle] = method.signature.output match {
      case DefMethod.Output.Void() =>
        q"Just[Unit]"
      case DefMethod.Output.Singular(tid) =>
        val target = TextTree.value[ScalaRefHandle](ScalaRefHandle.TypeFull(tid))
        q"Just[$target]"
      case DefMethod.Output.Struct(_) | DefMethod.Output.Algebraic(_) =>
        val aliasType = sp.svcMethods.within(name).within("Output").typeFull.toString
        q"Just[$aliasType]"
      case DefMethod.Output.Alternative(s, f) =>
        val negTpe = renderIdShimNeg(f)
        val posTpe = renderIdShimPos(s)
        q"Or[$negTpe, $posTpe]"
    }

    private def renderIdShimNeg(out: DefMethod.Output.NonAlternativeOutput): TextTree[ScalaRefHandle] = out match {
      case o: DefMethod.Output.Singular =>
        TextTree.value[ScalaRefHandle](ScalaRefHandle.TypeFull(o.typeId))
      case _ =>
        val tpe = ctx.conv.toScala(wrappedTypespaceTypeId).within(negativeBranchTypeName).typeFull.toString
        q"$tpe"
    }

    private def renderIdShimPos(out: DefMethod.Output.NonAlternativeOutput): TextTree[ScalaRefHandle] = out match {
      case o: DefMethod.Output.Singular =>
        TextTree.value[ScalaRefHandle](ScalaRefHandle.TypeFull(o.typeId))
      case _ =>
        val tpe = ctx.conv.toScala(wrappedTypespaceTypeId).within(positiveBranchTypeName).typeFull.toString
        q"$tpe"
    }

    private def positiveId: String = DomainNameMangling.methodToPositiveTypeName(method)
    private def negativeId: String = DomainNameMangling.methodToNegativeTypeName(method)

    def positiveType: ScalaType = sp.svcMethods.within(positiveId)
    def negativeType: ScalaType = sp.svcMethods.within(negativeId)

    private def adtId: AdtId = AdtId(sp.basePath, typename)
    private def dtoId: DTOId = DTOId(sp.basePath, typename)

    def positiveBranchTypeName: String = DomainNameMangling.toPositiveBranchName(adtId)
    def negativeBranchTypeName: String = DomainNameMangling.toNegativeBranchName(adtId)

    def wrappedTypespaceTypeId: izumi.idealingua.model.common.TypeId = method.signature.output match {
      case DefMethod.Output.Struct(_) | DefMethod.Output.Void() | DefMethod.Output.Singular(_) =>
        dtoId
      case DefMethod.Output.Algebraic(_) | DefMethod.Output.Alternative(_, _) =>
        adtId
    }

    def outputDefn: List[Defn] = renderOutput(typename, method.signature.output)

    def defnEncoder: TextTree[ScalaRefHandle] =
      q"""def encodeResponse: PartialFunction[IRTResBody, IRTJson] = {
         |  case IRTResBody(value: Output) =>
         |    value.asJson
         |}""".stripMargin

    def defnDecoder: TextTree[ScalaRefHandle] =
      q"""def decodeResponse[Or[+_, +_]: IRTIO2]: PartialFunction[IRTJsonBody, Or[IRTDecodingFailure, IRTResBody]] = {
         |  case IRTJsonBody(m, packet) if m == id =>
         |    decoded[Or, IRTResBody](packet.as[Output].map(v => IRTResBody(v)))
         |}""".stripMargin

    private def renderOutput(typename: String, out: DefMethod.Output): List[Defn] = out match {
      case DefMethod.Output.Struct(_) | DefMethod.Output.Void() | DefMethod.Output.Singular(_) =>
        val outId: DTOId = DTOId(sp.basePath, typename)
        val flat = ctx.domain.flattenedStructs.getOrElse(
          outId,
          FlatStruct(outId, List.empty, List.empty, List.empty),
        )
        val supers = izumi.idealingua.model.il.ast.typed.Super.empty
        val scalaStruct = DomainScalaStruct.scalaStruct(outId, flat, supers, ctx.conv, ctx.domain)
        val composite   = new DomainCompositeStructure(ctx, scalaStruct)
        val stub = stubDto(outId)
        val base = ctx.compositeRenderer.defns(composite, ClassSource.CsDTO(stub)).asInstanceOf[CogenProduct[Defn.Class]]
        // Legacy unwrap branch: a Singular method output yields a synthetic
        // wrapper DTO with a single field — the Circe codec must encode the
        // inner value directly (`encodeUnwrapped<Name>`).
        val unwrap = out match {
          case _: DefMethod.Output.Singular => true
          case _                            => false
        }
        withCirce(base, outId, flat, unwrap)

      case DefMethod.Output.Algebraic(_) =>
        val outAdtId: AdtId = AdtId(sp.basePath, typename)
        ctx.domain.userTypes.get(outAdtId) match {
          case Some(adt: NewTypeDef.Adt) => withAdtCirce(adt)
          case _                          => List.empty
        }

      case DefMethod.Output.Alternative(success, failure) =>
        val topAdt = ctx.domain.userTypes.get(adtId) match {
          case Some(adt: NewTypeDef.Adt) => withAdtCirce(adt)
          case _                          => List.empty
        }
        topAdt ++ renderShim(positiveId, success) ++ renderShim(negativeId, failure)
    }

    /** IMPL-7a.2-Fi1: service-output ADTs (`Output.Algebraic` and
      * `Output.Alternative`) live inside the service object rather than at
      * top level, so the per-domain `DomainScalaTranslator.emitAdt` Circe
      * wiring does not reach them. Legacy `CirceTranslatorExtensionBase.handleAdt`
      * runs over every ADT in the typespace (service-output ADTs are placed
      * in the legacy `Typespace.types`), emitting the tagged-union codec
      * `<Name>Circe` trait + companion-base `extends`. Replicate that here so
      * service codecs' `value.asJson` and `packet.as[Output]` resolve at the
      * call site. */
    private def withAdtCirce(adt: NewTypeDef.Adt): List[Defn] = {
      val baseAdt = ctx.adtRenderer.renderAdt(adt, List.empty).asInstanceOf[izumi.idealingua.translator.toscala.products.CogenProduct.AdtProduct]
      if (adt.alternatives.nonEmpty) {
        val circe = DomainCirceDerivationTranslatorExtension.emitForAdt(ctx, adt)
        val product = izumi.idealingua.translator.toscala.products.CogenProduct.AdtProduct(
          defn                = baseAdt.defn,
          companionBase       = baseAdt.companionBase,
          elements            = baseAdt.elements,
          more                = baseAdt.more,
          preamble            = baseAdt.preamble,
          companionCirceBases = List(circe.initText),
          siblings            = List(circe.defnText),
        )
        product.render
      } else {
        baseAdt.render
      }
    }

    private def renderShim(typename: String, out: DefMethod.Output): List[Defn] = out match {
      case _: DefMethod.Output.Singular => List.empty
      case o                            => renderOutput(typename, o)
    }
  }

  /** Augment a method Input/Output DTO `CogenProduct` with the full
    * extension chain that legacy `CompositeRenderer.defns(_, CsMethodInput
    * | CsMethodOutput)` runs through `ext.extend(...)`:
    *
    *   - AnyVal mixin on the case class (single-scalar-field wrappers).
    *   - `_cast_into_<peer>` (CastSimilar) and `_upcast_<parent>` (CastUp)
    *     implicit objects appended to the companion.
    *   - `<Name>Circe` trait sibling + companion-base `extends`.
    *
    * The Circe `unwrap` flag corresponds to legacy
    * `ClassSource.CsMethodOutput` with `DefMethod.Output.Singular(_)` (the
    * unwrap branch emits `encodeUnwrapped<Name>` codecs).
    */
  private def withCirce(
    base: CogenProduct[Defn.Class],
    dtoId: DTOId,
    flat: FlatStruct,
    unwrap: Boolean,
  ): List[Defn] = {
    val anyvalBases = DomainAnyvalExtension.withAnyvalForMethodStruct(ctx, flat)
    val sims        = DomainCastSimilarExtension.mkConvertersForMethodStruct(ctx, dtoId)
    val ups         = DomainCastUpExtension.generateUpcastsForMethodStruct(ctx, dtoId)
    val circe       = DomainCirceDerivationTranslatorExtension.emitForMethodStruct(
      ctx           = ctx,
      dtoId         = dtoId,
      flat          = flat,
      unwrap        = unwrap,
      scalaVersions = ctx.options.manifest.sbt.scalaVersions,
    )

    val augmented = CogenProduct[Defn.Class](
      defn                = base.defn,
      companionBase       = base.companionBase,
      tools               = base.tools,
      more                = base.more,
      preamble            = base.preamble,
      defnAnyvalBases     = anyvalBases,
      companionCirceBases = List(circe.initText),
      companionCasts      = sims ++ ups,
      siblings            = List(circe.defnText),
    )
    augmented.render
  }

  private def stubDto(id: DTOId): izumi.idealingua.model.il.ast.typed.TypeDef.DTO =
    izumi.idealingua.model.il.ast.typed.TypeDef.DTO(
      id     = id,
      struct = izumi.idealingua.model.il.ast.typed.Structure(
        fields        = List.empty,
        removedFields = List.empty,
        superclasses  = izumi.idealingua.model.il.ast.typed.Super.empty,
      ),
      meta = izumi.idealingua.model.il.ast.typed.NodeMeta.empty,
    )
}
