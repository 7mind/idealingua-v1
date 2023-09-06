package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.TypeDef.DTO
import izumi.idealingua.translator.toprotobuf.products.CogenProducts

final class CompositeRenderer(ctx: PBTContext) {
  def defns(i: DTO): CogenProducts.Message = {
    val self   = ctx.conv.toProtobuf(i.id)
    val fields = ctx.conv.toProtobuf(i.struct.fields)
    ctx.ext.extend(i, CogenProducts.Message(self, fields), _.handleDto)
  }
}
