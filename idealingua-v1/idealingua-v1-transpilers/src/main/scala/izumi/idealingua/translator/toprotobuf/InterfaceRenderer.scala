package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.TypeDef.Interface
import izumi.idealingua.translator.toprotobuf.products.CogenProducts

final class InterfaceRenderer(ctx: PBTContext) {
  def defns(i: Interface): CogenProducts.Message = {
    val self   = ctx.conv.toProtobuf(i.id)
    val fields = ctx.conv.toProtobuf(i.struct.fields)
    ctx.ext.extend(i, CogenProducts.Message(self, fields), _.handleInterface)
  }
}
