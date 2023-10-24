package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.DefMethod.{Output, Signature}
import izumi.idealingua.model.il.ast.typed.{DefMethod, Service}
import izumi.idealingua.translator.toprotobuf.products.CogenProducts
import izumi.idealingua.translator.toprotobuf.types.{ProtobufAdtMember, ProtobufMethod}

final class ServiceRenderer(ctx: PBTContext) {
  def defns(service: Service): CogenProducts.Service = {

    val methods = service.methods.map {
      case DefMethod.RPCMethod(name, Signature(input, na: Output.NonAlternativeOutput), _) =>
        val out = transformNonAlternativeOutput(na)
        ProtobufMethod(name, ctx.conv.toProtobuf(input.fields), out)
      case DefMethod.RPCMethod(name, Signature(input, Output.Alternative(success, failure)), _) =>
        val s = transformNonAlternativeOutput(success)
        val f = transformNonAlternativeOutput(failure)
        ProtobufMethod(name, ctx.conv.toProtobuf(input.fields), ProtobufMethod.Alternative(s, f))
    }

    ctx.ext.extend(service, CogenProducts.Service(service.id.domain, service.id.name, methods), _.handleService)
  }

  private def transformNonAlternativeOutput(na: Output.NonAlternativeOutput): ProtobufMethod.NonAlternativeOutput = na match {
    case Output.Void() =>
      ProtobufMethod.Void
    case Output.Singular(typeId) =>
      ProtobufMethod.Single(ctx.conv.toProtobuf(typeId))
    case Output.Struct(struct) =>
      ProtobufMethod.Structure(ctx.conv.toProtobuf(struct.fields))
    case Output.Algebraic(alternatives) =>
      val alts = alternatives.map(alt => ProtobufAdtMember(alt.memberName, ctx.conv.toProtobuf(alt.typeId)))
      ProtobufMethod.ADT(alts)
  }

}
