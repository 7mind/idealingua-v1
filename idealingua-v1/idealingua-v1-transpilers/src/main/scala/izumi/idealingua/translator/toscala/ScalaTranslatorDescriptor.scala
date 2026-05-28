package izumi.idealingua.translator.toscala

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.translator.CompilerOptions.ScalaTranslatorOptions
import izumi.idealingua.translator._
import izumi.idealingua.translator.toscala.domain.DomainScalaTranslator
import izumi.idealingua.translator.toscala.layout.ScalaLayouter
import izumi.idealingua.util.Parallel

object ScalaTranslatorDescriptor extends TranslatorDescriptor[ScalaTranslatorOptions] {
  override def defaultManifest: BuildManifest = ScalaBuildManifest.example

  override def typedOptions(options: UntypedCompilerOptions): ScalaTranslatorOptions = CompilerOptions.from(options)

  override def language: IDLLanguage = IDLLanguage.Scala

  override def makeDomain(
    domain: izumi.idealingua.typer.ir.Domain,
    parsed: izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved,
    options: UntypedCompilerOptions,
    parallel: Parallel = Parallel.Default,
  ): Translator = new DomainScalaTranslator(domain, parsed, typedOptions(options), parallel)

  override def makeHook(options: UntypedCompilerOptions): TranslationLayouter = new ScalaLayouter(typedOptions(options))

  // https://scala-lang.org/files/archive/spec/2.12/01-lexical-syntax.html
  val keywords: Set[String] = Set(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "forSome",
    "if",
    "implicit",
    "import",
    "lazy",
    "macro",
    "match",
    "new",
    "null",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "this",
    "throw",
    "trait",
    "try",
    "true",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield",
    // Scala 3 restricted keywords: https://docs.scala-lang.org/scala3/guides/migration/incompat-syntactic.html#restricted-keywords
    "export",
    "enum",
    "given",
    "then",
    "=>>",
    "?=>",
  )
}
