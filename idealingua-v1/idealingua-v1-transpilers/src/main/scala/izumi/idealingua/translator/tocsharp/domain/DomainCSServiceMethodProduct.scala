package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.common.TypeId.{BuzzerId, DTOId, ServiceId}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Algebraic, Alternative, Singular, Struct, Void}
import izumi.idealingua.model.il.ast.typed.SimpleStructure
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.types.{CSharpClass, CSharpField, CSharpType}

/** Per-method rendering helpers for the C# service / buzzer renderer.
  * Mirrors the inline methods on the legacy `CSharpTranslator` that
  * operate on a single `DefMethod`: `renderRPCMethodSignature`,
  * `renderRPCMethodOutputModel`, `renderRPCMethodOutputSignature`,
  * `renderRPCClientMethod`, `renderRPCDispatcherHandler`,
  * `renderRPCDummyMethod`, `renderServiceMethodInModel`,
  * `renderServiceMethodOutModel`, `renderBuzzerMethodOutModel`,
  * `renderAlternativeImpl`, `renderAlternativeType`,
  * `renderServiceMethodAlternativeOutput`.
  *
  * IMPL-7c Phase B M3: per-method codegen extracted into its own class
  * so the service renderer body composes by mapping over a list of
  * methods. Counterpart of `DomainTSServiceMethodProduct` on the TS
  * side. Wire-format-visible mangling (`In<M>` / `Out<M>` /
  * `<M>Success` / `<M>Failure`) routes through `DomainCSNameMangling`
  * for centralization, mirroring TS.
  *
  * Per-method ephemerals (`In<Method>` / `Out<Method>` classes) emit
  * the input + output struct shapes the legacy translator emits inline.
  * The field set comes off `method.signature.input.fields` and
  * `method.signature.output` — service / buzzer method I/O structs aren't
  * structurally inherited (`SimpleStructure` has no `superclasses`), so
  * no `Domain.flattenedStructs` lookup is needed.
  *
  * `Typespace` + `CSharpImports` are threaded per-call for the type
  * helpers (`CSharpType` constructor, `CSharpField` constructor,
  * `CSharpClass` constructors), same as M2.
  */
final class DomainCSServiceMethodProduct(ctx: DomainCSContext, adtRenderer: DomainCSAdtRenderer) {

  // -- Signatures ----------------------------------------------------------

  /** Mirror of legacy `renderRPCMethodSignature` (lines 397-413). */
  def renderRPCMethodSignature(svcOrBuzzer: String, method: DefMethod, forClient: Boolean)(implicit imports: CSharpImports, ts: Typespace): String = {
    method match {
      case m: DefMethod.RPCMethod =>
        val returnValue =
          if (isServiceMethodReturnExistent(m)) s"<${renderRPCMethodOutputSignature(svcOrBuzzer, m)}>" else ""

        val callback =
          s"${if (m.signature.input.fields.isEmpty) "" else ", "}Action$returnValue onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null"
        val fields  = m.signature.input.fields.map(f => CSharpType(f.typeId).renderType(true) + " " + CSharpField.safeVarName(f.name)).mkString(", ")
        val context = s"C ctx${if (m.signature.input.fields.isEmpty) "" else ", "}"
        if (forClient) {
          s"void ${m.name.capitalize}($fields$callback)"
        } else {
          s"${renderRPCMethodOutputSignature(svcOrBuzzer, m)} ${m.name.capitalize}($context$fields)"
        }
    }
  }

  /** Mirror of legacy `renderRPCMethodOutputModel` (lines 415-422). */
  def renderRPCMethodOutputModel(svcOrBuzzer: String, method: DefMethod.RPCMethod)(implicit imports: CSharpImports, ts: Typespace): String =
    method.signature.output match {
      case _: Struct       => s"$svcOrBuzzer.Out${method.name.capitalize}"
      case _: Algebraic    => s"$svcOrBuzzer.Out${method.name.capitalize}"
      case si: Singular    => s"${CSharpType(si.typeId).renderType(true)}"
      case _: Void         => "void"
      case at: Alternative => renderAlternativeType(s"$svcOrBuzzer.Out${method.name.capitalize}", at)
    }

  /** Mirror of legacy `renderRPCMethodOutputSignature` (line 424-426). */
  def renderRPCMethodOutputSignature(svcOrBuzzer: String, method: DefMethod.RPCMethod)(implicit imports: CSharpImports, ts: Typespace): String =
    s"${renderRPCMethodOutputModel(svcOrBuzzer, method)}"

  /** Mirror of legacy `renderAlternativeType` (lines 210-215). */
  private def renderAlternativeType(name: String, alternative: Alternative)(implicit im: CSharpImports, ts: Typespace): String = {
    val leftType  = renderServiceMethodAlternativeOutput(name, alternative, success = false)
    val rightType = renderServiceMethodAlternativeOutput(name, alternative, success = true)

    s"Either<$leftType, $rightType> "
  }

  /** Mirror of legacy `renderServiceMethodAlternativeOutput` (lines 176-191). */
  private def renderServiceMethodAlternativeOutput(name: String, at: Alternative, success: Boolean)(implicit im: CSharpImports, ts: Typespace): String = {
    if (success)
      at.success match {
        case _: Algebraic => s"${name}Success"
        case _: Struct    => s"${name}Success"
        case si: Singular => CSharpType(si.typeId).renderType(true)
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.success.toString)
      }
    else
      at.failure match {
        case _: Algebraic => s"${name}Failure"
        case _: Struct    => s"${name}Failure"
        case si: Singular => CSharpType(si.typeId).renderType(true)
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.failure.toString)
      }
  }

  def isServiceMethodReturnExistent(method: DefMethod.RPCMethod): Boolean = method.signature.output match {
    case _: Void => false
    case _       => true
  }

  // -- Models --------------------------------------------------------------

  /** Mirror of legacy `renderServiceMethodInModel(i: DTOId, ...)` (lines 625-631).
    *
    * M5 production-swap: splices `JsonNetExtension.preModelEmit(ctx, name, struct)`
    * / `postModelEmit(ctx, name, struct)` (legacy `:76-79` + `:99-127`) into
    * the per-method I/O DTO emission. The default empty-extension call
    * (no `withExtensions` overload invoked) preserves the M3 byte-parity
    * contract.
    */
  def renderServiceMethodInModel(i: DTOId, structure: SimpleStructure)(implicit imports: CSharpImports, ts: Typespace): String =
    renderServiceMethodInModel(i, structure, withExtensions = false)

  def renderServiceMethodInModel(i: DTOId, structure: SimpleStructure, withExtensions: Boolean)(implicit imports: CSharpImports, ts: Typespace): String = {
    val csClass = CSharpClass(i, structure)

    if (!withExtensions) {
      s"""
         |${csClass.render(withWrapper = true, withSlices = false, withRTTI = true)}
         |""".stripMargin
    } else {
      val pre  = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.preStruct(csClass.id.name)
      val post = izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.postStruct(ctx.domain, csClass.id.name, csClass)
      // Legacy `:628-630` shape:
      //   ${ext.preModelEmit(ctx, csClass.id.name, csClass)}
      //   ${csClass.render(...)}
      //   ${ext.postModelEmit(ctx, csClass.id.name, csClass)}
      s"""$pre
         |${csClass.render(withWrapper = true, withSlices = false, withRTTI = true)}
         |$post""".stripMargin
    }
  }

  /** Mirror of legacy `renderServiceMethodOutModel` for a service-scoped
    * synthetic `DTOId(svcId, name)` (lines 601-607). */
  def renderServiceMethodOutModel(serviceId: ServiceId, name: String, out: DefMethod.Output)(
    implicit imports: CSharpImports,
    ts: Typespace,
  ): String = renderServiceMethodOutModel(serviceId, name, out, withExtensions = false)

  def renderServiceMethodOutModel(serviceId: ServiceId, name: String, out: DefMethod.Output, withExtensions: Boolean)(
    implicit imports: CSharpImports,
    ts: Typespace,
  ): String = renderMethodOutModelImpl(DTOId(serviceId, name), name, out, withExtensions)

  /** Mirror of legacy `renderBuzzerMethodOutModel` for a buzzer-scoped
    * synthetic `DTOId(bzId, name)` (lines 609-615). */
  def renderBuzzerMethodOutModel(buzzerId: BuzzerId, name: String, out: DefMethod.Output)(
    implicit imports: CSharpImports,
    ts: Typespace,
  ): String = renderBuzzerMethodOutModel(buzzerId, name, out, withExtensions = false)

  def renderBuzzerMethodOutModel(buzzerId: BuzzerId, name: String, out: DefMethod.Output, withExtensions: Boolean)(
    implicit imports: CSharpImports,
    ts: Typespace,
  ): String = renderMethodOutModelImpl(DTOId(buzzerId, name), name, out, withExtensions)

  private def renderMethodOutModelImpl(dtoId: DTOId, name: String, out: DefMethod.Output, withExtensions: Boolean)(implicit imports: CSharpImports, ts: Typespace): String = out match {
    case st: Struct => renderServiceMethodInModel(dtoId, st.struct, withExtensions)
    case al: Algebraic =>
      // Legacy `renderAdtImpl` (`:156-174`) splices `ext.preModelEmit(ctx, adt)` /
      // `ext.postModelEmit(ctx, adt)` for a synthetic `Adt(AdtId(TypePath(...
      // DomainId.Undefined, Seq.empty), name), members, NodeMeta.empty)`. The
      // JsonNet converter target uses this synthetic name; for the new
      // emitter we construct the same synthetic `TypeDef.Adt` and pass it
      // to `DomainCSJsonNetExtension.preAdt` / `postAdt`.
      if (!withExtensions) adtRenderer.renderAdtImpl(name, al.alternatives, renderUsings = false)
      else {
        val syntheticAdt = izumi.idealingua.typer.ir.TypeDef.Adt(
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
    case at: Alternative => renderAlternativeImpl(dtoId, name, at, withExtensions)
  }

  /** Mirror of legacy `renderAlternativeImpl` (lines 217-246). */
  private def renderAlternativeImpl(structId: DTOId, name: String, alternative: Alternative, withExtensions: Boolean)(implicit im: CSharpImports, ts: Typespace): String = {
    val left = alternative.failure match {
      case al: Algebraic => adtRenderer.renderAdtImpl(renderServiceMethodAlternativeOutput(name, alternative, success = false), al.alternatives, renderUsings = false)
      case st: Struct    => renderServiceMethodInModel(DTOId(structId.path, structId.name + "Failure"), st.struct, withExtensions)
      case _             => ""
    }

    val right = alternative.success match {
      case al: Algebraic => adtRenderer.renderAdtImpl(renderServiceMethodAlternativeOutput(name, alternative, success = true), al.alternatives, renderUsings = false)
      case st: Struct    => renderServiceMethodInModel(DTOId(structId.path, structId.name + "Success"), st.struct, withExtensions)
      case _             => ""
    }

    s"""$left
       |$right
     """.stripMargin
  }

  // -- Client method body --------------------------------------------------

  /** Mirror of legacy `renderRPCClientMethod` (lines 428-472). */
  def renderRPCClientMethod(svcOrBuzzer: String, method: DefMethod)(implicit imports: CSharpImports, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      m.signature.output match {
        case _: Struct | _: Algebraic | _: Alternative =>
          s"""public ${renderRPCMethodSignature(svcOrBuzzer, method, forClient = true)} {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => CSharpField.safeVarName(ff.name)).mkString(", ")});"
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
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => CSharpField.safeVarName(ff.name)).mkString(", ")});"
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
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => CSharpField.safeVarName(ff.name)).mkString(", ")});"
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

  /** Mirror of legacy `renderRPCDispatcherHandler` (lines 504-529). */
  def renderRPCDispatcherHandler(svcOrBuzzer: String, method: DefMethod, server: String)(implicit imports: CSharpImports, ts: Typespace): String =
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

  /** Mirror of legacy `renderRPCDummyMethod` (lines 566-580). */
  def renderRPCDummyMethod(svcOrBuzzer: String, member: DefMethod, virtual: Boolean)(implicit imports: CSharpImports, ts: Typespace): String = {
    val retValue = member match {
      case m: DefMethod.RPCMethod =>
        m.signature.output match {
          case _: Struct | _: Algebraic | _: Alternative => "return null;"
          case s: Singular                               => "return " + CSharpType(s.typeId).defaultValue + ";";
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
