package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.TypeId.{BuzzerId, DTOId, ServiceId}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Algebraic, Alternative, Singular, Struct, Void}
import izumi.idealingua.model.il.ast.typed.SimpleStructure
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Per-method rendering helpers for the C# service / buzzer renderer.
  *
  * F-TextTree M3 — the outer output-model envelopes (`renderServiceMethodInModel`,
  * `renderMethodOutModelImpl`) are composed as `TextTree[CSRefHandle]` and
  * rendered at the helper return boundary. Per-method signature and
  * dispatcher helpers continue to return `String`: they emit short
  * inline fragments (single switch arms, comma-separated argument lists,
  * etc.) whose `s"..."` shape exactly matches what gets spliced into the
  * enclosing service envelope. Porting those to TextTree would add
  * indentation noise without functional improvement; the byte-parity
  * contract is what `verifyGoldens` enforces.
  */
final class DomainCSServiceMethodProduct(ctx: DomainCSContext, adtRenderer: DomainCSAdtRenderer) {

  // -- Signatures ----------------------------------------------------------

  def renderRPCMethodSignature(svcOrBuzzer: String, method: DefMethod, forClient: Boolean)(implicit imports: CSharpImports, domain: Domain): String = {
    method match {
      case m: DefMethod.RPCMethod =>
        val returnValue =
          if (isServiceMethodReturnExistent(m)) s"<${renderRPCMethodOutputSignature(svcOrBuzzer, m)}>" else ""

        val callback =
          s"${if (m.signature.input.fields.isEmpty) "" else ", "}Action$returnValue onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null"
        val fields  = m.signature.input.fields.map(f => DomainCSharpType(f.typeId).renderType(true) + " " + DomainCSField.safeVarName(f.name)).mkString(", ")
        val context = s"C ctx${if (m.signature.input.fields.isEmpty) "" else ", "}"
        if (forClient) {
          s"void ${m.name.capitalize}($fields$callback)"
        } else {
          s"${renderRPCMethodOutputSignature(svcOrBuzzer, m)} ${m.name.capitalize}($context$fields)"
        }
    }
  }

  def renderRPCMethodOutputModel(svcOrBuzzer: String, method: DefMethod.RPCMethod)(implicit imports: CSharpImports, domain: Domain): String =
    method.signature.output match {
      case _: Struct       => s"$svcOrBuzzer.Out${method.name.capitalize}"
      case _: Algebraic    => s"$svcOrBuzzer.Out${method.name.capitalize}"
      case si: Singular    => s"${DomainCSharpType(si.typeId).renderType(true)}"
      case _: Void         => "void"
      case at: Alternative => renderAlternativeType(s"$svcOrBuzzer.Out${method.name.capitalize}", at)
    }

  def renderRPCMethodOutputSignature(svcOrBuzzer: String, method: DefMethod.RPCMethod)(implicit imports: CSharpImports, domain: Domain): String =
    s"${renderRPCMethodOutputModel(svcOrBuzzer, method)}"

  private def renderAlternativeType(name: String, alternative: Alternative)(implicit im: CSharpImports, domain: Domain): String = {
    val leftType  = renderServiceMethodAlternativeOutput(name, alternative, success = false)
    val rightType = renderServiceMethodAlternativeOutput(name, alternative, success = true)

    s"Either<$leftType, $rightType> "
  }

  private def renderServiceMethodAlternativeOutput(name: String, at: Alternative, success: Boolean)(implicit im: CSharpImports, domain: Domain): String = {
    if (success)
      at.success match {
        case _: Algebraic => s"${name}Success"
        case _: Struct    => s"${name}Success"
        case si: Singular => DomainCSharpType(si.typeId).renderType(true)
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.success.toString)
      }
    else
      at.failure match {
        case _: Algebraic => s"${name}Failure"
        case _: Struct    => s"${name}Failure"
        case si: Singular => DomainCSharpType(si.typeId).renderType(true)
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.failure.toString)
      }
  }

  def isServiceMethodReturnExistent(method: DefMethod.RPCMethod): Boolean = method.signature.output match {
    case _: Void => false
    case _       => true
  }

  // -- Models --------------------------------------------------------------

  def renderServiceMethodInModel(i: DTOId, structure: SimpleStructure)(implicit imports: CSharpImports, domain: Domain): String =
    renderServiceMethodInModel(i, structure, withExtensions = false)

  def renderServiceMethodInModel(i: DTOId, structure: SimpleStructure, withExtensions: Boolean)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = {
    val resolver = new DomainCSTypeResolver()
    val csClass  = DomainCSClass(i, structure)

    if (!withExtensions) {
      val tree: TextTree[CSRefHandle] =
        q"""
           |${csClass.render(withWrapper = true, withSlices = false, withRTTI = true)}
           |""".stripMargin
      tree.mapRender(resolver.resolve)
    } else {
      val pre  = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.preStruct(csClass.id.name)
      val post = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.postStruct(ctx.domain, csClass.id.name, csClass)
      val tree: TextTree[CSRefHandle] =
        q"""$pre
           |${csClass.render(withWrapper = true, withSlices = false, withRTTI = true)}
           |$post""".stripMargin
      tree.mapRender(resolver.resolve)
    }
  }

  def renderServiceMethodOutModel(serviceId: ServiceId, name: String, out: DefMethod.Output)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderServiceMethodOutModel(serviceId, name, out, withExtensions = false)

  def renderServiceMethodOutModel(serviceId: ServiceId, name: String, out: DefMethod.Output, withExtensions: Boolean)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderMethodOutModelImpl(DTOId(serviceId, name), name, out, withExtensions)

  def renderBuzzerMethodOutModel(buzzerId: BuzzerId, name: String, out: DefMethod.Output)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderBuzzerMethodOutModel(buzzerId, name, out, withExtensions = false)

  def renderBuzzerMethodOutModel(buzzerId: BuzzerId, name: String, out: DefMethod.Output, withExtensions: Boolean)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderMethodOutModelImpl(DTOId(buzzerId, name), name, out, withExtensions)

  private def renderMethodOutModelImpl(dtoId: DTOId, name: String, out: DefMethod.Output, withExtensions: Boolean)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = out match {
    case st: Struct => renderServiceMethodInModel(dtoId, st.struct, withExtensions)
    case al: Algebraic =>
      if (!withExtensions) adtRenderer.renderAdtImpl(name, al.alternatives, renderUsings = false)
      else {
        val syntheticAdt = NewTypeDef.Adt(
          izumi.idealingua.model.common.TypeId.AdtId(
            izumi.idealingua.model.common.TypePath(izumi.idealingua.model.common.DomainId.Undefined, Seq.empty),
            name,
          ),
          al.alternatives,
          izumi.idealingua.model.il.ast.typed.NodeMeta.empty,
        )
        val pre  = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.preAdt(syntheticAdt)
        val post = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.postAdt(syntheticAdt, imports)
        adtRenderer.renderAdtImpl(name, al.alternatives, renderUsings = false, preSplice = pre, postSplice = post)
      }
    case si: Singular    => s"// ${si.typeId}"
    case _: Void         => ""
    case at: Alternative => renderAlternativeImpl(dtoId, name, at, withExtensions)
  }

  private def renderAlternativeImpl(structId: DTOId, name: String, alternative: Alternative, withExtensions: Boolean)(
    implicit im: CSharpImports,
    domain: Domain,
  ): String = {
    // Synthesize the JsonNet pre/post converter splices for an Algebraic
    // success or failure side. Without these the abstract Out<Method><Side>
    // ADT serializes via Newtonsoft's default sealed-subclass discriminator,
    // which keys each branch on the literal `"Value"` instead of the branch
    // name — diverging from the legacy emission (where every Algebraic ADT
    // carries `[JsonConverter(typeof(<Name>_JsonNetConverter))]` + the
    // accompanying converter class). Matches `renderMethodOutModelImpl`'s
    // top-level `Algebraic` arm under `withExtensions=true`.
    def algebraicSplices(adtName: String, alts: List[izumi.idealingua.model.il.ast.typed.AdtMember]): (String, String) = {
      val syntheticAdt = NewTypeDef.Adt(
        izumi.idealingua.model.common.TypeId.AdtId(
          izumi.idealingua.model.common.TypePath(izumi.idealingua.model.common.DomainId.Undefined, Seq.empty),
          adtName,
        ),
        alts,
        izumi.idealingua.model.il.ast.typed.NodeMeta.empty,
      )
      val pre  = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.preAdt(syntheticAdt)
      val post = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.postAdt(syntheticAdt, im)
      (pre, post)
    }

    val left = alternative.failure match {
      case al: Algebraic =>
        val adtName     = renderServiceMethodAlternativeOutput(name, alternative, success = false)
        val (pre, post) = algebraicSplices(adtName, al.alternatives)
        adtRenderer.renderAdtImpl(adtName, al.alternatives, renderUsings = false, preSplice = pre, postSplice = post)
      case st: Struct    => renderServiceMethodInModel(DTOId(structId.path, structId.name + "Failure"), st.struct, withExtensions)
      case _             => ""
    }

    val right = alternative.success match {
      case al: Algebraic =>
        val adtName     = renderServiceMethodAlternativeOutput(name, alternative, success = true)
        val (pre, post) = algebraicSplices(adtName, al.alternatives)
        adtRenderer.renderAdtImpl(adtName, al.alternatives, renderUsings = false, preSplice = pre, postSplice = post)
      case st: Struct    => renderServiceMethodInModel(DTOId(structId.path, structId.name + "Success"), st.struct, withExtensions)
      case _             => ""
    }

    s"""$left
       |$right
     """.stripMargin
  }

  // -- Client method body --------------------------------------------------

  def renderRPCClientMethod(svcOrBuzzer: String, method: DefMethod)(implicit imports: CSharpImports, domain: Domain): String = method match {
    case m: DefMethod.RPCMethod =>
      m.signature.output match {
        case _: Struct | _: Algebraic | _: Alternative =>
          s"""public ${renderRPCMethodSignature(svcOrBuzzer, method, forClient = true)} {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => DomainCSField.safeVarName(ff.name)).mkString(", ")});"
            }
             |    Transport.Send<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}, ${renderRPCMethodOutputModel(
              svcOrBuzzer,
              m,
            )}>("$svcOrBuzzer", "${m.name}", ${if (m.signature.input.fields.isEmpty) "null" else "inData"},
             |        new ClientTransportCallback<${renderRPCMethodOutputModel(svcOrBuzzer, m)}>(onSuccess, onFailure, onAny), ctx);
             |}
       """.stripMargin

        case _: Singular =>
          s"""public ${renderRPCMethodSignature(svcOrBuzzer, method, forClient = true)} {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => DomainCSField.safeVarName(ff.name)).mkString(", ")});"
            }
             |    Transport.Send<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}, ${renderRPCMethodOutputModel(
              svcOrBuzzer,
              m,
            )}>("$svcOrBuzzer", "${m.name}", ${if (m.signature.input.fields.isEmpty) "null" else "inData"},
             |        new ClientTransportCallback<${renderRPCMethodOutputModel(svcOrBuzzer, m)}>(onSuccess, onFailure, onAny), ctx);
             |}
       """.stripMargin

        case _: Void =>
          s"""public ${renderRPCMethodSignature(svcOrBuzzer, method, forClient = true)} {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => DomainCSField.safeVarName(ff.name)).mkString(", ")});"
            }
             |    Transport.Send<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}, IRT.Void>("$svcOrBuzzer", "${m.name}", ${
              if (m.signature.input.fields.isEmpty) "null" else "inData"
            },
             |        new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
             |}
       """.stripMargin
      }
  }

  // -- Dispatcher handler --------------------------------------------------

  def renderRPCDispatcherHandler(svcOrBuzzer: String, method: DefMethod, server: String)(implicit imports: CSharpImports, domain: Domain): String =
    method match {
      case m: DefMethod.RPCMethod =>
        if (isServiceMethodReturnExistent(m))
          s"""case "${m.name}": {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var obj = marshaller.Unmarshal<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}>(data);"
            }
             |    return marshaller.Marshal<${renderRPCMethodOutputModel(svcOrBuzzer, m)}>(\n        $server.${m.name.capitalize}(ctx${
              if (m.signature.input.fields.isEmpty) "" else ", "
            }${m.signature.input.fields.map(f => s"obj.${f.name.capitalize}").mkString(", ")})\n    );
             |}
         """.stripMargin
        else
          s"""case "${m.name}": {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var obj = marshaller.Unmarshal<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}>(data);"
            }
             |    $server.${m.name.capitalize}(ctx${if (m.signature.input.fields.isEmpty) "" else ", "}${m.signature.input.fields
              .map(f => s"obj.${f.name.capitalize}").mkString(", ")});
             |    return marshaller.Marshal<IRT.Void>(null);
             |}
       """.stripMargin
    }

  def renderRPCDummyMethod(svcOrBuzzer: String, member: DefMethod, virtual: Boolean)(implicit imports: CSharpImports, domain: Domain): String = {
    val retValue = member match {
      case m: DefMethod.RPCMethod =>
        m.signature.output match {
          case _: Struct | _: Algebraic | _: Alternative => "return null;"
          case s: Singular                               => "return " + DomainCSharpType(s.typeId).defaultValue + ";";
          case _: Void                                   => "// Nothing to return"
        }
      case _ => throw new Exception("Unsupported renderServiceServerDummyMethod case.")
    }
    s"""public ${if (virtual) "virtual " else ""}${renderRPCMethodSignature(svcOrBuzzer, member, forClient = false)} {
       |    $retValue
       |}
     """.stripMargin
  }
}
