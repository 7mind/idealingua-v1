package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.ProtobufBuildManifest
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.model.typespace.verification.VerificationRule
import izumi.idealingua.model.typespace.verification.rules.ReservedKeywordRule
import izumi.idealingua.translator.CompilerOptions.ProtobufTranslatorOptions
import izumi.idealingua.translator.toprotobuf.layout.ProtobufLayouter
import izumi.idealingua.translator._

object ProtobufTranslatorDescriptor extends TranslatorDescriptor[ProtobufTranslatorOptions] {
  override def defaultManifest: BuildManifest = ProtobufBuildManifest.example

  override def typedOptions(options: UntypedCompilerOptions): ProtobufTranslatorOptions = CompilerOptions.from(options)

  override def language: IDLLanguage = IDLLanguage.Protobuf

  override def defaultExtensions: Seq[TranslatorExtension] = ProtobufTranslator.defaultExtensions

  override def make(typespace: Typespace, options: UntypedCompilerOptions): Translator = new ProtobufTranslator(typespace, typedOptions(options))

  override def rules: Seq[VerificationRule] = Seq(
    ReservedKeywordRule.warning("protobuf", keywords)
  )

  override def makeHook(options: UntypedCompilerOptions): TranslationLayouter = new ProtobufLayouter(typedOptions(options))

  // https://scala-lang.org/files/archive/spec/2.12/01-lexical-syntax.html
  val keywords: Set[String] = Set(
    "message",
    "service",
    "oneof",
    "enum",
    "reserved",
    "import",
    "required",
    "repeated",
    "double",
    "float",
    "int32",
    "int64",
    "uint32",
    "uint64",
    "sint32",
    "sint64",
    "fixed32",
    "fixed64",
    "sfixed32",
    "sfixed64",
    "bool",
    "string",
    "bytes",
  )
}
