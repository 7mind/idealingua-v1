package izumi.idealingua.translator.toscala.domain

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
import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
import izumi.idealingua.translator.toscala.types.{ClassSource, ScalaField, ScalaType}
import izumi.idealingua.typer.ir.{FlatStruct, TypeDef => NewTypeDef}

import scala.meta._

/** Per-method rendering helper for the Domain-IR Service / Buzzer renderer.
  *
  * IMPL-7a.2 Phase B M4 (relaxed parity): twin of legacy
  * `types.ServiceMethodProduct`. Same code-emission strategy (Input
  * struct + Output struct/ADT + per-method object + Circe codec + server /
  * client wrapped variants), but every typespace lookup is replaced by a
  * direct read off `Domain.flattenedStructs` / `Domain.userTypes` (set up
  * by Phase 7 `EphemeralSynthesizer`).
  *
  * The renderer produces structurally-correct Scala — `compiles + emits the
  * IRT runtime calls the legacy generates`. Byte parity with legacy is NOT
  * a hard gate at M4; the contract is wire-format JSON equality
  * (`runWireFixtures` + `runCrossLangInterop`), which M5 supplies.
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

  def defStructs: List[Stat] =
    Input.inputDefn ++ Output.outputDefn

  def defnMethod: Stat = {
    q"""object $nameTerm extends ${ctx.rt.IRTMethodSignature.init()} {
         final val id: ${ctx.rt.IRTMethodId.typeName} = ${ctx.rt.IRTMethodId.termName}(serviceId, ${ctx.rt.IRTMethodName.termName}(${Lit.String(name)}))
         type Input = ${Input.typespaceType.typeName}
         type Output = ${Output.wrappedTypespaceType.typeName}
       }
     """
  }

  def defnMethodRegistration: Term = nameTerm

  def defnCodecRegistration: Term.ApplyInfix =
    q""" ${sp.svcMethods.termName}.$nameTerm.id -> ${sp.svcCodecs.termName}.$nameTerm """

  def defnCodec: Stat = {
    val methods = List(Input.defnEncoder, Input.defnDecoder, Output.defnEncoder, Output.defnDecoder)

    q"""object $nameTerm extends IRTCirceMarshaller {
          import ${sp.svcMethods.termName}.$nameTerm._
          ..$methods
       }
     """
  }

  def defnServerWrapped: Stat = {
    val invoke = method.signature.output match {
      case DefMethod.Output.Singular(_) =>
        q"""def invoke(ctx: ${sp.Ctx.t}, input: Input): Just[Output] = {
              assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
            ${sp.IO2.n}.map(_service.$nameTerm(ctx, ..${Input.sigCall}))(v => new Output(v))
           }"""

      case DefMethod.Output.Void() =>
        q"""def invoke(ctx: ${sp.Ctx.t}, input: Input): Just[Output] = {
              assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
              ${sp.IO2.n}.map(_service.$nameTerm(ctx, ..${Input.sigCall}))(_ => new Output())
           }"""

      case DefMethod.Output.Algebraic(_) | DefMethod.Output.Struct(_) =>
        q"""def invoke(ctx: ${sp.Ctx.t}, input: Input): Just[Output] = {
              assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
              _service.$nameTerm(ctx, ..${Input.sigCall})
           }"""

      case DefMethod.Output.Alternative(_, _) =>
        q"""
           def invoke(ctx: ${sp.Ctx.t}, input: Input): Just[Output] = {
              ${sp.IO2.n}.redeem(_service.$nameTerm(ctx, ..${Input.sigCall}))(
                      err => _F.pure(new ${Output.negativeBranchType.typeFull}(err))
                      , succ => _F.pure(new ${Output.positiveBranchType.typeFull}(succ))
                   )
           }"""
    }

    q"""object $nameTerm extends IRTMethodWrapper[${sp.F.t}, ${sp.Ctx.t}] {
          import ${sp.svcMethods.termName}.$nameTerm._

          val signature: ${sp.svcMethods.termName}.$nameTerm.type = ${sp.svcMethods.termName}.$nameTerm
          val marshaller: ${sp.svcCodecs.termName}.$nameTerm.type = ${sp.svcCodecs.termName}.$nameTerm

          $invoke
       }
     """
  }

  def defnClientWrapped: Stat = {
    val exception =
      q"""
          val id = ${Lit.String(s"${sp.typeName}.${sp.svcWrappedClientTpe.termName.value}.$nameTerm")}
          val expected = classOf[_M.$nameTerm.Input].toString
          ${sp.IO2.n}.terminate(new IRTTypeMismatchException(s"Unexpected type in $$id: $$v, expected $$expected got $${v.getClass}", v, None))
       """

    method.signature.output match {
      case DefMethod.Output.Singular(_) =>
        q"""def $nameTerm(..${Input.signature}): ${Output.outputType} = {
               ${sp.IO2.n}.redeem(_dispatcher
                 .dispatch(IRTMuxRequest(IRTReqBody(new ${init"_M.$nameTerm.Input(..${Input.sigDirectCall})"}), _M.$nameTerm.id))
               )(
                  { err => ${sp.IO2.n}.terminate(err) },
                  {
                    case IRTMuxResponse(IRTResBody(v: _M.$nameTerm.Output), method) if method == _M.$nameTerm.id =>
                      ${sp.IO2.n}.pure(v.value)
                    case v => $exception
                  })
           }"""

      case DefMethod.Output.Void() =>
        q"""def $nameTerm(..${Input.signature}): ${Output.outputType} = {
               ${sp.IO2.n}.redeem(_dispatcher
                 .dispatch(IRTMuxRequest(IRTReqBody(new ${init"_M.$nameTerm.Input(..${Input.sigDirectCall})"}), _M.$nameTerm.id))
               )(
                   { err => ${sp.IO2.n}.terminate(err) },
                   {
                     case IRTMuxResponse(IRTResBody(_: _M.$nameTerm.Output), method) if method == _M.$nameTerm.id =>
                       ${sp.IO2.n}.pure(())
                     case v => $exception
            })
           }"""

      case DefMethod.Output.Algebraic(_) | DefMethod.Output.Struct(_) =>
        q"""def $nameTerm(..${Input.signature}): ${Output.outputType} = {
            ${sp.IO2.n}.redeem(_dispatcher
                 .dispatch(IRTMuxRequest(IRTReqBody(new ${init"_M.$nameTerm.Input(..${Input.sigDirectCall})"}), _M.$nameTerm.id))
               )(
                    { err => ${sp.IO2.n}.terminate(err) },
                    {
                      case IRTMuxResponse(IRTResBody(v: _M.$nameTerm.Output), method) if method == _M.$nameTerm.id =>
                        ${sp.IO2.n}.pure(v)
                      case v => $exception
                    })
           }"""

      case DefMethod.Output.Alternative(_, _) =>
        q"""def $nameTerm(..${Input.signature}): ${Output.outputType} = {
           ${sp.IO2.n}.redeem(_dispatcher
                 .dispatch(IRTMuxRequest(IRTReqBody(new ${init"_M.$nameTerm.Input(..${Input.sigDirectCall})"}), _M.$nameTerm.id))
               )(
                    { err => ${sp.IO2.n}.terminate(err) },
                    {
                       case IRTMuxResponse(IRTResBody(r), method) if method == _M.$nameTerm.id =>
                         r match {
                           case va : ${Output.negativeBranchType.typeFull} =>
                             ${sp.IO2.n}.fail(va.value)

                           case va : ${Output.positiveBranchType.typeFull} =>
                             ${sp.IO2.n}.pure(va.value)

                           case v =>
                             $exception
                         }
                       case v =>
                         $exception
                    })
           }"""
    }
  }

  def defnServer: Stat =
    q"def $nameTerm(ctx: ${sp.Ctx.t}, ..${Input.signature}): ${Output.outputType}"

  def defnClient: Stat =
    q"def $nameTerm(..${Input.signature}): ${Output.outputType}"

  protected def name: String           = method.name
  protected def nameTerm: Term.Name    = Term.Name(name)

  // ---- Input rendering ----
  protected object Input {
    private def typespaceId: DTOId =
      DTOId(sp.basePath, DomainNameMangling.methodToInputName(method))

    def typespaceType: ScalaType = ctx.conv.toScala(typespaceId)

    private def fields: List[ScalaField] = {
      // Resolve the pre-synthesized input ephemeral DTO from flattenedStructs.
      // EphemeralSynthesizer (Phase 7) creates a DTOId(svc, "<Name>Input") for
      // every RPCMethod and registers a FlatStruct under that id.
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

    def signature: List[Term.Param] = {
      import izumi.idealingua.translator.toscala.types.ScalaField._
      fields.toParams
    }

    def sigCall: List[Term.Select]      = fields.map(f => q"input.${f.name}")
    def sigDirectCall: List[Term.Name]  = fields.map(_.name)

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
      // Use a stub legacy DTO via ClassSource.CsDTO — the composite renderer
      // only matches on type, never reads the inner field, at the
      // pre-extension layer.
      val stub = stubDto(typespaceId)
      val base = ctx.compositeRenderer.defns(composite, ClassSource.CsDTO(stub)).asInstanceOf[CogenProduct[Defn.Class]]
      withCirce(base, typespaceId, flat, unwrap = false)
    }

    def defnEncoder: Defn.Def =
      q"""def encodeRequest: PartialFunction[IRTReqBody, IRTJson] = {
            case IRTReqBody(value: Input) => value.asJson
          }"""

    def defnDecoder: Defn.Def =
      q"""def decodeRequest[Or[+_, +_] : IRTIO2]: PartialFunction[IRTJsonBody, Or[IRTDecodingFailure, IRTReqBody]] = {
            case IRTJsonBody(m, packet) if m == id => this.decoded[Or, IRTReqBody](packet.as[Input].map(v => IRTReqBody(v)))
          }
       """
  }

  // ---- Output rendering ----
  protected object Output {
    private def typename: String = DomainNameMangling.methodToOutputName(method)

    def outputType: Type = method.signature.output match {
      case DefMethod.Output.Void() =>
        t"Just[Unit]"
      case DefMethod.Output.Singular(tid) =>
        val scalaType = ctx.conv.toScala(tid)
        t"Just[${scalaType.typeFull}]"
      case DefMethod.Output.Struct(_) | DefMethod.Output.Algebraic(_) =>
        val aliasType: ScalaType = sp.svcMethods.within(name).within("Output")
        t"Just[${aliasType.typeFull}]"
      case DefMethod.Output.Alternative(s, f) =>
        t"Or[${render_Id_SHIM(f, negativeType.typeFull)}, ${render_Id_SHIM(s, positiveType.typeFull)}]"
    }

    private def positiveId: String = DomainNameMangling.methodToPositiveTypeName(method)
    private def negativeId: String = DomainNameMangling.methodToNegativeTypeName(method)

    def positiveType: ScalaType = sp.svcMethods.within(positiveId)
    def negativeType: ScalaType = sp.svcMethods.within(negativeId)

    private def adtId: AdtId = AdtId(sp.basePath, typename)
    private def dtoId: DTOId = DTOId(sp.basePath, typename)

    def positiveBranchType: ScalaType =
      wrappedTypespaceType.within(DomainNameMangling.toPositiveBranchName(adtId))

    def negativeBranchType: ScalaType =
      wrappedTypespaceType.within(DomainNameMangling.toNegativeBranchName(adtId))

    def wrappedTypespaceType: ScalaType = {
      val id = method.signature.output match {
        case DefMethod.Output.Struct(_) | DefMethod.Output.Void() | DefMethod.Output.Singular(_) =>
          dtoId
        case DefMethod.Output.Algebraic(_) | DefMethod.Output.Alternative(_, _) =>
          adtId
      }
      ctx.conv.toScala(id)
    }

    def outputDefn: List[Defn] = renderOutput(typename, method.signature.output)

    def defnEncoder: Defn.Def =
      q"""def encodeResponse: PartialFunction[IRTResBody, IRTJson] = {
            case IRTResBody(value: Output) => value.asJson
          }"""

    def defnDecoder: Defn.Def =
      q"""def decodeResponse[Or[+_, +_] : IRTIO2]: PartialFunction[IRTJsonBody, Or[IRTDecodingFailure, IRTResBody]] = {
            case IRTJsonBody(m, packet) if m == id =>
              decoded[Or, IRTResBody](packet.as[Output].map(v => IRTResBody(v)))
          }"""

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
          case Some(adt: NewTypeDef.Adt) => ctx.adtRenderer.renderAdt(adt, List.empty).render
          case _                          => List.empty
        }

      case DefMethod.Output.Alternative(success, failure) =>
        val topAdt = ctx.domain.userTypes.get(adtId) match {
          case Some(adt: NewTypeDef.Adt) => ctx.adtRenderer.renderAdt(adt, List.empty).render
          case _                          => List.empty
        }
        topAdt ++ render_SHIM(positiveId, success) ++ render_SHIM(negativeId, failure)
    }

    private def render_SHIM(typename: String, out: DefMethod.Output): scala.collection.immutable.Seq[Defn] = out match {
      case _: DefMethod.Output.Singular => List.empty
      case o                            => renderOutput(typename, o)
    }

    def render_Id_SHIM(s: DefMethod.Output.NonAlternativeOutput, typeFull: Type): Type = s match {
      case o: DefMethod.Output.Singular => ctx.conv.toScala(o.typeId).typeFull
      case _                            => typeFull
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
    val anyvalBases       = DomainAnyvalExtension.withAnyvalForMethodStruct(ctx, flat)
    val withAnyVal        = base.defn.prependBase(anyvalBases)

    val sims              = DomainCastSimilarExtension.mkConvertersForMethodStruct(ctx, dtoId)
    val ups               = DomainCastUpExtension.generateUpcastsForMethodStruct(ctx, dtoId)
    val companionWithCasts = base.companionBase.appendDefinitions(sims ++ ups)

    val circe              = DomainCirceDerivationTranslatorExtension.emitForMethodStruct(
      ctx           = ctx,
      dtoId         = dtoId,
      flat          = flat,
      unwrap        = unwrap,
      scalaVersions = ctx.options.manifest.sbt.scalaVersions,
    )
    val siblingInit        = ctx.conv.toScala(dtoId).sibling(circe.name).init()
    val companionFinal     = companionWithCasts.prependBase(siblingInit)

    val augmented = CogenProduct[Defn.Class](
      defn          = withAnyVal,
      companionBase = companionFinal,
      tools         = base.tools,
      more          = base.more :+ circe.defn,
      preamble      = base.preamble,
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
