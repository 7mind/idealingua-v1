package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Algebraic, Alternative, Singular, Struct, Void}
import izumi.idealingua.model.il.ast.typed.{SimpleStructure}

/** Per-method rendering helpers for the TypeScript service / buzzer
  * renderer. Mirrors the inline methods on the legacy `TypeScriptTranslator`
  * that operate on a single `DefMethod`.
  *
  * F-TextTree M2 — ported to the M1.5 typed-renderer protocol. The
  * public methods now return `TextTree[TSRefHandle]`-backed values
  * (rendered via `resolver.resolve` at the boundary), so the parent
  * `DomainTSServiceRenderer` can compose the full service body with the
  * harvest pathway intact. Internal helpers that emit nested ADT /
  * Alternative type unions delegate the alternative-`TypeId` emission to
  * `TextTree.value(TSRefHandle.TypeRef(...))`; everything else
  * (constructor bodies, dispatcher switches, client promise wrappers)
  * stays as String-level composition.
  */
final class DomainTSServiceMethodProduct(ctx: DomainTSContext, adtRenderer: DomainTSAdtRenderer) {

  import ctx._

  private val resolver = new DomainTSTypeResolver(conv)

  // -- Signatures ----------------------------------------------------------

  /** Mirror of legacy `renderRPCMethodSignature`. */
  def renderRPCMethodSignature(method: DefMethod, spread: Boolean = false, forClient: Boolean = true): String = method match {
    case m: DefMethod.RPCMethod =>
      if (spread) {
        val fields = m.signature.input.fields.map(f => conv.safeName(f.name) + s": ${conv.toNativeType(f.typeId)}").mkString(", ")
        if (forClient)
          s"""${m.name}($fields): Promise<${renderServiceMethodOutputSignature(m)}>"""
        else
          s"""${m.name}(context: C${if (m.signature.input.fields.nonEmpty) ", " else ""}$fields): Promise<${renderServiceMethodOutputSignature(m)}>"""
      } else {
        s"""${m.name}(input: In${m.name.capitalize}): Promise<${renderServiceMethodOutputSignature(m)}>"""
      }
  }

  def renderServiceMethodOutputSignature(method: DefMethod.RPCMethod): String =
    renderServiceMethodOutputType(method.signature.output, method)

  private def renderServiceMethodOutputType(output: DefMethod.Output, method: DefMethod.RPCMethod): String = output match {
    case _: Struct     => s"Out${method.name.capitalize}"
    case al: Algebraic => al.alternatives.map(alt => conv.toNativeType(alt.typeId)).mkString(" | ")
    case si: Singular  => conv.toNativeType(si.typeId)
    case _: Void       => "void"
    case at: Alternative =>
      s"Either<${renderServiceMethodAlternativeOutput("Out" + method.name.capitalize, at, success = false)}, ${renderServiceMethodAlternativeOutput(
          "Out" + method.name.capitalize,
          at,
          success = true,
        )}>"
  }

  private def renderServiceMethodAlternativeOutput(method: String, at: Alternative, success: Boolean): String = {
    if (success)
      at.success match {
        case _: Algebraic => method + "Success"
        case _: Struct    => method + "Success"
        case si: Singular => conv.toNativeType(si.typeId)
        case _: Void      => "Void"
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.success.toString)
      }
    else
      at.failure match {
        case _: Algebraic => method + "Failure"
        case _: Struct    => method + "Failure"
        case si: Singular => conv.toNativeType(si.typeId)
        case _: Void      => "Void"
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.failure.toString)
      }
  }

  // -- Models --------------------------------------------------------------

  /** Mirror of legacy `renderRPCMethodModels`. */
  def renderRPCMethodModels(method: DefMethod): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${renderServiceMethodInModel(s"In${m.name.capitalize}", "IncomingData", m.signature.input, exported = false)}
         |${renderServiceMethodOutModel(s"Out${m.name.capitalize}", "OutgoingData", m.signature.output)}
       """.stripMargin
  }

  private def renderServiceMethodInModel(name: String, implements: String, structure: SimpleStructure, exported: Boolean): String = {
    val exp = if (exported) "export " else ""

    val membersBlock = structure.fields.map(f => conv.toFieldMember(f)).mkString("\n").shift(4)
    val methodsBlock = structure.fields.map(f => conv.toFieldMethods(f)).mkString("\n").shift(4)
    val fromObject   = structure.fields
      .map(f => s"${conv.deserializeName("this." + conv.safeName(f.name), f.typeId)} = ${conv.deserializeType("data." + f.name, f.typeId)};").mkString("\n").shift(8)
    val serializedBody = renderSerializedObject(structure.fields).shift(12)

    val serializedFields: TextTree[TSRefHandle] =
      structure.fields.map { f =>
        val nm = conv.toNativeTypeName(f.name, f.typeId)
        q"$nm: ${TextTree.value[TSRefHandle](TSRefHandle.SerializedTypeRef(f.typeId))};"
      }.joinN()

    val tree: TextTree[TSRefHandle] =
      q"""${exp}class $name implements $implements {
         |$membersBlock
         |$methodsBlock
         |    constructor(data: ${name}Serialized = undefined) {
         |        if (typeof data === 'undefined' || data === null) {
         |            return;
         |        }
         |
         |$fromObject
         |    }
         |
         |    public serialize(): ${name}Serialized {
         |        return {
         |$serializedBody
         |        };
         |    }
         |}
         |
         |${exp}interface ${name}Serialized {
         |${serializedFields.shift(4)}
         |}
       """.stripMargin

    tree.mapRender(resolver.resolve)
  }

  private def renderServiceMethodOutModel(name: String, implements: String, out: DefMethod.Output): String = out match {
    case st: Struct      => renderServiceMethodInModel(name, implements, st.struct, exported = true)
    case al: Algebraic   => adtRenderer.renderAdtImpl(name, al.alternatives, exported = false)
    case at: Alternative => renderAlternative(name, at, exported = false)
    case _               => ""
  }

  private def renderAlternative(method: String, alternative: Alternative, exported: Boolean = true): String = {
    val leftTypeName = renderServiceMethodAlternativeOutputForName(method, alternative, success = false)

    val left = alternative.failure match {
      case al: Algebraic => adtRenderer.renderAdtImpl(leftTypeName, al.alternatives, exported = true)
      case st: Struct    => renderServiceMethodInModel(leftTypeName, "OutgoingData", st.struct, exported = true)
      case _             => ""
    }

    val leftTypeSerialize = alternative.failure match {
      case _: Algebraic => leftTypeName + "Helpers.serialize(either.value)"
      case _: Void      => "{}"
      case _: Struct    => "(either as any).value.serialize() /* TS will report an error value does not exist on type never, though this is not right. */"
      case si: Singular => conv.serializeValue("(either as any).value", si.typeId, asAny = true)
    }

    val leftTypeDeserialize = alternative.failure match {
      case _: Algebraic => leftTypeName + "Helpers.deserialize(content)"
      case _: Void      => "{}"
      case _: Struct    => s"new $leftTypeName(content)"
      case si: Singular => conv.deserializeType("content", si.typeId, asAny = true)
    }

    val rightTypeName = renderServiceMethodAlternativeOutputForName(method, alternative, success = true)

    val right = alternative.success match {
      case al: Algebraic => adtRenderer.renderAdtImpl(rightTypeName, al.alternatives, exported = true)
      case st: Struct    => renderServiceMethodInModel(rightTypeName, "OutgoingData", st.struct, exported = true)
      case _             => ""
    }

    val rightTypeSerialize = alternative.success match {
      case _: Algebraic => rightTypeName + "Helpers.serialize(either.value)"
      case _: Void      => "{}"
      case _: Struct    => "either.value.serialize()"
      case si: Singular => conv.serializeValue("either.value", si.typeId, asAny = true)
    }

    val rightTypeDeserialize = alternative.success match {
      case _: Algebraic => rightTypeName + "Helpers.deserialize(content)"
      case _: Void      => "{}"
      case _: Struct    => s"new $rightTypeName(content)"
      case si: Singular => conv.deserializeType("content", si.typeId, asAny = true)
    }

    val name = s"$method"
    val exp  = if (exported) "export " else ""

    s"""$left
       |$right
       |${exp}type $name = Either<$leftTypeName, $rightTypeName>;
       |${exp}type ${name}Serialized = {[key in 'Success' | 'Failure']?: any};
       |
       |${exp}class ${name}Helpers {
       |    public static serialize(either: $name): ${name}Serialized {
       |        return either.isRight() ? {
       |            'Success': $rightTypeSerialize
       |        } : {
       |            'Failure': $leftTypeSerialize
       |        };
       |    }
       |
       |    public static deserialize(data: ${name}Serialized): $name {
       |        const id = Object.keys(data)[0];
       |        const content = (data as any)[id];
       |        switch (id) {
       |            case 'Success': return new EitherRight<$leftTypeName, $rightTypeName>($rightTypeDeserialize);
       |            case 'Failure': return new EitherLeft<$leftTypeName, $rightTypeName>($leftTypeDeserialize);
       |            default: throw new Error(`Unexpected key $${id} in either object.`);
       |        }
       |    }
       |}
     """.stripMargin
  }

  private def renderServiceMethodAlternativeOutputForName(method: String, at: Alternative, success: Boolean): String = {
    if (success)
      at.success match {
        case _: Algebraic => method + "Success"
        case _: Struct    => method + "Success"
        case si: Singular => conv.toNativeType(si.typeId)
        case _: Void      => "Void"
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.success.toString)
      }
    else
      at.failure match {
        case _: Algebraic => method + "Failure"
        case _: Struct    => method + "Failure"
        case si: Singular => conv.toNativeType(si.typeId)
        case _: Void      => "Void"
        case _            => throw new Exception("Not supported alternative non singular or algebraic " + at.failure.toString)
      }
  }

  // -- Client / dispatcher -------------------------------------------------

  /** Mirror of legacy `renderRPCClientMethod`. */
  def renderRPCClientMethod(service: String, method: DefMethod): String = method match {
    case m: DefMethod.RPCMethod =>
      m.signature.output match {
        case _: Struct =>
          s"""public ${renderRPCMethodSignature(method, spread = true)} {
             |    const __data = new In${m.name.capitalize}();
             |${m.signature.input.fields.map(f => s"__data.${conv.safeName(f.name)} = ${conv.safeName(f.name)};").mkString("\n").shift(4)}
             |    return this.send('${m.name}', __data, In${m.name.capitalize}, ${renderServiceMethodOutputSignature(m)});
             |}
       """.stripMargin

        case _: Algebraic | _: Alternative =>
          s"""public ${renderRPCMethodSignature(method, spread = true)} {
             |    const __data = new In${m.name.capitalize}();
             |${m.signature.input.fields.map(f => s"__data.${conv.safeName(f.name)} = ${conv.safeName(f.name)};").mkString("\n").shift(4)}
             |    return new Promise((resolve, reject) => {
             |        this._transport.send(${service}Client.ClassName, '${m.name}', __data)
             |            .then((data: any) => {
             |                try {
             |                    resolve(Out${m.name.capitalize}Helpers.deserialize(data));
             |                } catch(err) {
             |                    reject(err);
             |                }
             |             })
             |            .catch((err: any) => {
             |                reject(err);
             |            });
             |    });
             |}
         """.stripMargin

        case si: Singular =>
          s"""public ${renderRPCMethodSignature(method, spread = true)} {
             |    const __data = new In${m.name.capitalize}();
             |${m.signature.input.fields.map(f => s"__data.${conv.safeName(f.name)} = ${conv.safeName(f.name)};").mkString("\n").shift(4)}
             |    return new Promise((resolve, reject) => {
             |        this._transport.send(${service}Client.ClassName, '${m.name}', __data)
             |            .then((data: any) => {
             |                try {
             |                    const output = ${conv.deserializeType("data", si.typeId, asAny = true)};
             |                    resolve(output);
             |                }
             |                catch(err) {
             |                    reject(err);
             |                }
             |            })
             |            .catch((err: any) => {
             |                reject(err);
             |            });
             |        });
             |}
         """.stripMargin

        case _: Void =>
          s"""public ${renderRPCMethodSignature(method, spread = true)} {
             |    const __data = new In${m.name.capitalize}();
             |${m.signature.input.fields.map(f => s"__data.${conv.safeName(f.name)} = ${conv.safeName(f.name)};").mkString("\n").shift(4)}
             |    return new Promise((resolve, reject) => {
             |        this._transport.send(${service}Client.ClassName, '${m.name}', __data)
             |            .then(() => {
             |              resolve();
             |            })
             |            .catch((err: any) => {
             |                reject(err);
             |            });
             |        });
             |}
         """.stripMargin
      }
  }

  /** Mirror of legacy `renderServiceDispatcherHandler`. */
  def renderServiceDispatcherHandler(method: DefMethod, impl: String, useRawMarshaller: Boolean = false): String = {
    val useRawParam = if (useRawMarshaller) ", true" else ""
    method match {
      case m: DefMethod.RPCMethod =>
        val resolveCode =
          if (isServiceMethodReturnExistent(m))
            s"""${renderServiceReturnSerialization(m, useRawMarshaller = useRawMarshaller).shift(20)}
               |                    resolve(serialized);""".stripMargin
          else
            s"                    resolve(this.marshaller.Marshal<Void>(Void.instance$useRawParam));"

        s"""case "${m.name}": {
           |    ${
            if (m.signature.input.fields.isEmpty) "// No input params for this method"
            else
              s"const obj = ${if (m.signature.input.fields.nonEmpty) s"new In${m.name.capitalize}(" else ""}this.marshaller.Unmarshal<${
                  if (m.signature.input.fields.nonEmpty) s"In${m.name.capitalize}Serialized" else "object"
                }>(data$useRawParam)${if (m.signature.input.fields.nonEmpty) ")" else ""};"
          }
           |    return new Promise((resolve, reject) => {
           |        try {
           |            this.$impl.${m.name}(context${if (m.signature.input.fields.isEmpty) "" else ", "}${m.signature.input.fields
            .map(f => s"obj.${conv.safeName(f.name)}").mkString(", ")})
           |                .then((res: ${renderServiceMethodOutputSignature(m)}) => {
           |$resolveCode
           |                })
           |                .catch((err) => {
           |                    reject(err);
           |                });
           |        } catch (err) {
           |            reject(err);
           |        }
           |    });
           |}
         """.stripMargin
    }
  }

  private def isServiceMethodReturnExistent(method: DefMethod.RPCMethod): Boolean = method.signature.output match {
    case _: Void => false
    case _       => true
  }

  private def renderServiceReturnSerialization(method: DefMethod.RPCMethod, useRawMarshaller: Boolean = false): String = {
    val useRawParam = if (useRawMarshaller) ", true" else ""
    method.signature.output match {
      case _: Algebraic | _: Alternative =>
        s"const serialized = this.marshaller.Marshal<object>(Out${method.name.capitalize}Helpers.serialize(res)$useRawParam);"
      case _ => s"const serialized = this.marshaller.Marshal<${renderServiceMethodOutputSignature(method)}>(res$useRawParam);"
    }
  }

  // -- Helpers -------------------------------------------------------------

  private def renderSerializedObject(fields: List[izumi.idealingua.model.il.ast.typed.Field]): String = {
    val serialized = fields.map(f => conv.serializeField(f))
    val it         = serialized.iterator
    it.map(m => s"$m${if (it.hasNext) "," else ""}").mkString("\n")
  }
}
