package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.TypeDef.Enumeration
import izumi.idealingua.translator.toprotobuf.products.CogenProducts

final class EnumRenderer(ctx: PBTContext) {
  def defns(i: Enumeration): CogenProducts.Enum = {
    val self = ctx.conv.toProtobuf(i.id)
    ctx.ext.extend(i, CogenProducts.Enum(self, i.members.map(_.value)), _.handleEnum)
  }
}
