package izumi.idealingua.translator.toprotobuf.layout

import izumi.idealingua.translator
import izumi.idealingua.translator.CompilerOptions.ProtobufTranslatorOptions
import izumi.idealingua.translator.{ExtendedModule, Layouted, Translated, TranslationLayouter}

class ProtobufLayouter(options: ProtobufTranslatorOptions) extends TranslationLayouter {
  override def layout(outputs: Seq[Translated]): Layouted = {
    ExtendedModule.DomainModule()
    Layouted()
  }
}

