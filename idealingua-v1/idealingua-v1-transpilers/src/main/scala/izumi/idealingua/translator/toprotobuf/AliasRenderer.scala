package izumi.idealingua.translator.toprotobuf

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.il.ast.typed.TypeDef.Alias
import izumi.idealingua.translator.toprotobuf.products.CogenProducts
import izumi.idealingua.translator.toprotobuf.types.ProtobufField

final class AliasRenderer(ctx: PBTContext) {
  def defns(i: Alias): CogenProducts.Message = {
    val self = ctx.conv.toProtobuf(i.id)
    val field = ProtobufField(i.id.name.uncapitalize, ctx.conv.toProtobuf(i.target))
    ctx.ext.extend(i, CogenProducts.Message(self, List(field)), _.handleAlias)
  }
}
