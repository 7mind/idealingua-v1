package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.common.TypeId.{BuzzerId, DTOId, ServiceId}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Algebraic, Alternative, Singular, Struct, Void}
import izumi.idealingua.model.il.ast.typed.SimpleStructure
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.types.{CSharpClass => LegacyCSharpClass}
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Per-method rendering helpers for the C# service / buzzer renderer.
  *
  * IMPL-10-prep-Cs1: body uses `DomainCSharpType` / `DomainCSClass` /
  * `DomainCSField` (Domain-backed) for renderer-internal codegen. The
  * `Typespace` parameter is retained on the JsonNet-touching call paths
  * (`renderServiceMethodInModel(withExtensions=true)`, `renderAlternativeImpl`,
  * `renderMethodOutModelImpl`) because `DomainCSJsonNetExtension.post*`
  * still operates on the legacy `CSharpClass` and reads `Typespace` for
  * `CSharpType` predicate chains. The JsonNet port is deferred to Cs2.
  *
  * The legacy `CSharpClass` is constructed at the JsonNet splice site
  * only — the renderer's own emit body uses `DomainCSClass`.
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
    renderServiceMethodInModel(i, structure, withExtensions = false, tsForJsonNet = None)

  /** When `withExtensions = true`, splices the JsonNet pre/post around the
    * per-method I/O DTO emission. The JsonNet extension still consumes
    * legacy `CSharpClass` + `Typespace` (Cs2 scope), so the caller threads
    * `tsForJsonNet`.
    */
  def renderServiceMethodInModel(i: DTOId, structure: SimpleStructure, withExtensions: Boolean, tsForJsonNet: Option[Typespace])(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = {
    val csClass = DomainCSClass(i, structure)

    if (!withExtensions) {
      s"""
         |${csClass.render(withWrapper = true, withSlices = false, withRTTI = true)}
         |""".stripMargin
    } else {
      val ts = tsForJsonNet.getOrElse(
        throw new IllegalStateException("JsonNet splice requested without Typespace (Cs2 will eliminate this dependency)")
      )
      // The JsonNet extension still consumes legacy CSharpClass / Typespace.
      // Build a legacy CSharpClass with the same shape as the Domain one for
      // the JsonNet splice. This is the only place the legacy converter
      // family is touched in the renderer body; Cs2 will eliminate it.
      implicit val _ts: Typespace = ts
      val legacyCsClass = LegacyCSharpClass(i, structure)
      val pre  = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.preStruct(legacyCsClass.id.name)
      val post = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.postStruct(ctx.domain, legacyCsClass.id.name, legacyCsClass)
      s"""$pre
         |${csClass.render(withWrapper = true, withSlices = false, withRTTI = true)}
         |$post""".stripMargin
    }
  }

  def renderServiceMethodOutModel(serviceId: ServiceId, name: String, out: DefMethod.Output)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderServiceMethodOutModel(serviceId, name, out, withExtensions = false, tsForJsonNet = None)

  def renderServiceMethodOutModel(serviceId: ServiceId, name: String, out: DefMethod.Output, withExtensions: Boolean, tsForJsonNet: Option[Typespace])(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderMethodOutModelImpl(DTOId(serviceId, name), name, out, withExtensions, tsForJsonNet)

  def renderBuzzerMethodOutModel(buzzerId: BuzzerId, name: String, out: DefMethod.Output)(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderBuzzerMethodOutModel(buzzerId, name, out, withExtensions = false, tsForJsonNet = None)

  def renderBuzzerMethodOutModel(buzzerId: BuzzerId, name: String, out: DefMethod.Output, withExtensions: Boolean, tsForJsonNet: Option[Typespace])(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = renderMethodOutModelImpl(DTOId(buzzerId, name), name, out, withExtensions, tsForJsonNet)

  private def renderMethodOutModelImpl(dtoId: DTOId, name: String, out: DefMethod.Output, withExtensions: Boolean, tsForJsonNet: Option[Typespace])(
    implicit imports: CSharpImports,
    domain: Domain,
  ): String = out match {
    case st: Struct => renderServiceMethodInModel(dtoId, st.struct, withExtensions, tsForJsonNet)
    case al: Algebraic =>
      if (!withExtensions) adtRenderer.renderAdtImpl(name, al.alternatives, renderUsings = false)
      else {
        val ts = tsForJsonNet.getOrElse(
          throw new IllegalStateException("JsonNet splice requested without Typespace (Cs2 will eliminate this dependency)")
        )
        val syntheticAdt = NewTypeDef.Adt(
          izumi.idealingua.model.common.TypeId.AdtId(
            izumi.idealingua.model.common.TypePath(izumi.idealingua.model.common.DomainId.Undefined, Seq.empty),
            name,
          ),
          al.alternatives,
          izumi.idealingua.model.il.ast.typed.NodeMeta.empty,
        )
        val pre  = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.preAdt(syntheticAdt)
        val post = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.postAdt(syntheticAdt, ts, imports)
        adtRenderer.renderAdtImpl(name, al.alternatives, renderUsings = false, preSplice = pre, postSplice = post)
      }
    case si: Singular    => s"// ${si.typeId}"
    case _: Void         => ""
    case at: Alternative => renderAlternativeImpl(dtoId, name, at, withExtensions, tsForJsonNet)
  }

  private def renderAlternativeImpl(structId: DTOId, name: String, alternative: Alternative, withExtensions: Boolean, tsForJsonNet: Option[Typespace])(
    implicit im: CSharpImports,
    domain: Domain,
  ): String = {
    val left = alternative.failure match {
      case al: Algebraic => adtRenderer.renderAdtImpl(renderServiceMethodAlternativeOutput(name, alternative, success = false), al.alternatives, renderUsings = false)
      case st: Struct    => renderServiceMethodInModel(DTOId(structId.path, structId.name + "Failure"), st.struct, withExtensions, tsForJsonNet)
      case _             => ""
    }

    val right = alternative.success match {
      case al: Algebraic => adtRenderer.renderAdtImpl(renderServiceMethodAlternativeOutput(name, alternative, success = true), al.alternatives, renderUsings = false)
      case st: Struct    => renderServiceMethodInModel(DTOId(structId.path, structId.name + "Success"), st.struct, withExtensions, tsForJsonNet)
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
