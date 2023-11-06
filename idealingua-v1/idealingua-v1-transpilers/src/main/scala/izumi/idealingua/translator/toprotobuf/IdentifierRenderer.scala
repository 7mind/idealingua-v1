package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.TypeDef.Identifier
import izumi.idealingua.translator.toprotobuf.products.CogenProducts

final class IdentifierRenderer(ctx: PBTContext) {
  def defns(i: Identifier): CogenProducts.Message = {
    val self   = ctx.conv.toProtobuf(i.id)
    val fields = ctx.conv.toProtobuf(i.fields)
    ctx.ext.extend(i, CogenProducts.Message(self, fields), _.handleIdentifier)
  }
}
