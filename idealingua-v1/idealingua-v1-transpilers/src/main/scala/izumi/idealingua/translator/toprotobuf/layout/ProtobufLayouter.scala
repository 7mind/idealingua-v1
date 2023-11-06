package izumi.idealingua.translator.toprotobuf.layout

import izumi.fundamentals.platform.language.Quirks
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.CompilerOptions.ProtobufTranslatorOptions
import izumi.idealingua.translator.{ExtendedModule, Layouted, Translated, TranslationLayouter}

class ProtobufLayouter(options: ProtobufTranslatorOptions) extends TranslationLayouter {
  Quirks.discard(options)
  override def layout(outputs: Seq[Translated]): Layouted = {
    val extended = outputs.flatMap {
      translated =>
        translated.modules.map(ExtendedModule.DomainModule(translated.typespace.domain.id, _))
    }
    Layouted(ProtobufLayouter.uuidModule +: extended)
  }
}

object ProtobufLayouter {
  val uuidModule: ExtendedModule.RuntimeModule = ExtendedModule.RuntimeModule(
    Module(
      ModuleId(Seq("idl"), "types.proto"),
      """syntax="proto2";
        |
        |package idl.types;
        |
        |message PUUID {
        |  required uint64 most = 1;
        |  required uint64 least = 2;
        |}
        |
        |message PTimestamp {
        |  required int64 seconds = 1;
        |  required int32 nanos = 2;
        |  optional string timezone = 3;
        |}
        |""".stripMargin,
    )
  )
}
