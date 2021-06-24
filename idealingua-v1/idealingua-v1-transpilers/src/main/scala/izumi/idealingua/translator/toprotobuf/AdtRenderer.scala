package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.TypeDef.Adt
import izumi.idealingua.translator.toprotobuf.products.CogenProducts
import izumi.idealingua.translator.toprotobuf.types.ProtobufAdtMember

final class AdtRenderer(ctx: PBTContext) {
  def defns(i: Adt): CogenProducts.ADT = {
    val self = ctx.conv.toProtobuf(i.id)
    val members = i.alternatives.map {
      alt =>
        ProtobufAdtMember(alt.memberName, ctx.conv.toProtobuf(alt.typeId))
    }
    ctx.ext.extend(i, CogenProducts.ADT(self, members), _.handleAdt)
  }
}
