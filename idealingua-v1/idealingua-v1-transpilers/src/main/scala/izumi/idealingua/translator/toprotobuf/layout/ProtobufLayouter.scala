package izumi.idealingua.translator.toprotobuf.layout

import izumi.idealingua.translator.CompilerOptions.ProtobufTranslatorOptions
import izumi.idealingua.translator.{ExtendedModule, Layouted, Translated, TranslationLayouter}

class ProtobufLayouter(options: ProtobufTranslatorOptions) extends TranslationLayouter {
  override def layout(outputs: Seq[Translated]): Layouted = {
    val extended = outputs.flatMap {
      translated =>
        translated.modules.map(ExtendedModule.DomainModule(translated.typespace.domain.id, _))
    }
    Layouted(extended)
  }
}

