package izumi.idealingua.translator.toprotobuf.extensions

import izumi.idealingua.model.il.ast.typed.TypeDef.Interface
import izumi.idealingua.translator.TranslatorExtension
import izumi.idealingua.translator.toprotobuf.PBTContext
import izumi.idealingua.translator.toprotobuf.products.CogenProducts

trait ProtobufTranslatorExtension extends TranslatorExtension {
  import izumi.fundamentals.platform.language.Quirks._

  def handleMessage(ctx: PBTContext, interface: Interface, product: CogenProducts.Message): CogenProducts.Message = {
    discard(ctx, interface, manifest)
    product
  }
}


