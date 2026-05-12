package izumi.idealingua.translator.tocsharp

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.translator.CompilerOptions.CSharpTranslatorOptions
import izumi.idealingua.translator._
import izumi.idealingua.translator.tocsharp.domain.{CSharpDefaultExtensions, DomainCSharpTranslator}
import izumi.idealingua.translator.tocsharp.layout.CSharpLayouter

object CSharpTranslatorDescriptor extends TranslatorDescriptor[CSharpTranslatorOptions] {

  override def defaultManifest: BuildManifest = CSharpBuildManifest.example

  override def typedOptions(options: UntypedCompilerOptions): CSharpTranslatorOptions = CompilerOptions.from(options)

  override def language: IDLLanguage = IDLLanguage.CSharp

  override def defaultExtensions: Seq[TranslatorExtension] = CSharpDefaultExtensions.defaultExtensions

  override def makeDomain(
    domain: izumi.idealingua.typer.ir.Domain,
    parsed: izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved,
    options: UntypedCompilerOptions,
  ): Translator = new DomainCSharpTranslator(domain, parsed, typedOptions(options))

  override def makeHook(options: UntypedCompilerOptions): TranslationLayouter = new CSharpLayouter(typedOptions(options))

  // https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/
  val keywords: Set[String] = Set(
    "abstract",
    "as",
    "base",
    "bool",
    "break",
    "byte",
    "case",
    "catch",
    "char",
    "checked",
    "class",
    "const",
    "continue",
    "decimal",
    "default",
    "delegate",
    "do",
    "double",
    "else",
    "enum",
    "event",
    "explicit",
    "extern",
    "false",
    "finally",
    "fixed",
    "float",
    "for",
    "foreach",
    "goto",
    "if",
    "implicit",
    "in",
    "int",
    "interface",
    "internal",
    "is",
    "lock",
    "long",
    "namespace",
    "new",
    "null",
    "object",
    "operator",
    "out",
    "override",
    "params",
    "private",
    "protected",
    "public",
    "readonly",
    "ref",
    "return",
    "sbyte",
    "sealed",
    "short",
    "sizeof",
    "stackalloc",
    "static",
    "string",
    "struct",
    "switch",
    "this",
    "throw",
    "true",
    "try",
    "typeof",
    "uint",
    "ulong",
    "unchecked",
    "unsafe",
    "ushort",
    "using",
    "using",
    "static",
    "virtual",
    "void",
    "volatile",
    "while",
  )

//  val contextualKeywords: Set[String] = Set(
//    "add",
//    "alias",
//    "ascending",
//    "async",
//    "await",
//    "by",
//    "descending",
//    "dynamic",
//    "equals",
//    "from",
//    "get",
//    "global",
//    "group",
//    "into",
//    "join",
//    "let",
//    "nameof",
//    "on",
//    "orderby",
//    "partial",
//    "remove",
//    "select",
//    "set",
//    "value",
//    "var",
//    "when",
//    "where",
//    "yield",
//  )
}
