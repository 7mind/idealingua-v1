package izumi.idealingua.translator.toprotobuf.extensions

import izumi.idealingua.model.il.ast.typed.TypeDef.{Adt, Alias, DTO, Enumeration, Interface, Identifier}
import izumi.idealingua.translator.TranslatorExtension
import izumi.idealingua.translator.toprotobuf.PBTContext
import izumi.idealingua.translator.toprotobuf.products.CogenProducts

trait ProtobufTranslatorExtension extends TranslatorExtension {
  import izumi.fundamentals.platform.language.Quirks._

  def handleInterface(ctx: PBTContext, interface: Interface, product: CogenProducts.Message): CogenProducts.Message = {
    discard(ctx, interface, manifest)
    product
  }

  def handleDto(ctx: PBTContext, dto: DTO, product: CogenProducts.Message): CogenProducts.Message = {
    discard(ctx, dto, manifest)
    product
  }

  def handleAlias(ctx: PBTContext, alias: Alias, product: CogenProducts.Message): CogenProducts.Message = {
    discard(ctx, alias, manifest)
    product
  }

  def handleIdentifier(ctx: PBTContext, id: Identifier, product: CogenProducts.Message): CogenProducts.Message = {
    discard(ctx, id, manifest)
    product
  }

  def handleEnum(ctx: PBTContext, enumeration: Enumeration, product: CogenProducts.Enum): CogenProducts.Enum = {
    discard(ctx, enumeration, manifest)
    product
  }

  def handleAdt(ctx: PBTContext, adt: Adt, product: CogenProducts.ADT): CogenProducts.ADT = {
    discard(ctx, adt, manifest)
    product
  }
}


