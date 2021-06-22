package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.toprotobuf.tools.ModuleTools
import izumi.idealingua.translator.toprotobuf.extensions.{ProtobufTranslatorExtension, ProtobufTranslatorExtensions}
import izumi.idealingua.translator.toprotobuf.types.ProtobufTypeConverter

class PBTContext(
                 val typespace: Typespace
                 , extensions: Seq[ProtobufTranslatorExtension]
               ) {
  final val modules = new ModuleTools()
  final val conv = new ProtobufTypeConverter(typespace.domain.id)
  final val ext = {
    new ProtobufTranslatorExtensions(this, extensions)
  }

  final val interfaceRenderer = new InterfaceRenderer(this)
  final val compositeRenderer = new CompositeRenderer(this)
  //  final val adtRenderer = new AdtRenderer(this)
//  final val idRenderer = new IdRenderer(this)
//  final val serviceRenderer = new ServiceRenderer(this)
//  final val enumRenderer = new EnumRenderer(this)
}
