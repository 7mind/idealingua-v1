package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.toprotobuf.tools.ModuleTools
import izumi.idealingua.translator.toprotobuf.extensions.{ProtobufTranslatorExtension, ProtobufTranslatorExtensions}
import izumi.idealingua.translator.toprotobuf.types.ProtobufTypeConverter

class PBTContext(
  val typespace: Typespace,
  extensions: Seq[ProtobufTranslatorExtension],
  options: Map[String, String],
) {
  final val modules = new ModuleTools(options)
  final val conv    = new ProtobufTypeConverter(typespace.domain.id)
  final val ext = {
    new ProtobufTranslatorExtensions(this, extensions)
  }

  final val interfaceRenderer  = new InterfaceRenderer(this)
  final val compositeRenderer  = new CompositeRenderer(this)
  final val enumRenderer       = new EnumRenderer(this)
  final val adtRenderer        = new AdtRenderer(this)
  final val identifierRenderer = new IdentifierRenderer(this)
  final val aliasRenderer      = new AliasRenderer(this)
  final val serviceRenderer    = new ServiceRenderer(this)
}
