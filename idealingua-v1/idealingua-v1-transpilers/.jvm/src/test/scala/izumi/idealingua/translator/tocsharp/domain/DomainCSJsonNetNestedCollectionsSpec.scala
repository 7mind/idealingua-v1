package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.files.IzFiles
import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.util.matching.Regex

final class DomainCSJsonNetNestedCollectionsSpec extends AnyWordSpec {
  private val domainSource: String =
    """domain repro.nested
      |
      |data NestedOptional {
      |  rows : list[opt[list[str]]]
      |  byKey : map[str, opt[map[str, str]]]
      |}
      |""".stripMargin

  private val foreachDeclaration: Regex = """foreach\s*\(\s*var\s+(\w+)\s+in""".r

  private def renderedNestedOptional(): String = {
    val root = Files.createTempDirectory("cs-nested-collections")
    try {
      val domainFile = root.resolve("repro").resolve("nested.domain")
      Files.createDirectories(domainFile.getParent)
      Files.write(domainFile, domainSource.getBytes(StandardCharsets.UTF_8))
      val context  = new LocalModelLoaderContext(Seq(root), Seq.empty[File])
      val resolved = new ModelResolver().resolve(context.loader.load())
      val options = UntypedCompilerOptions(
        language           = IDLLanguage.CSharp,
        target             = None,
        manifest           = CSharpBuildManifest.example,
        withBundledRuntime = false,
        providedRuntime    = None,
        zipOutput          = false,
      )
      val out = new TypespaceCompilerBaseFacade(options).compile(resolved.successful)
      out.emodules.collectFirst {
        case ExtendedModule.DomainModule(_, m) if m.id.name == "NestedOptional.cs" => m.content
      }.getOrElse(fail("no NestedOptional.cs module emitted"))
    } finally {
      IzFiles.erase(root)
    }
  }

  private def shadowedLoopVariables(source: String): List[String] = {
    final case class Loop(name: String, depth: Int)
    val (_, _, shadowed) = source.linesIterator.foldLeft((0, List.empty[Loop], List.empty[String])) {
      case ((depth, open, found), line) =>
        val active    = open.filter(_.depth <= depth)
        val declared  = foreachDeclaration.findAllMatchIn(line).map(_.group(1)).toList
        val clashes   = declared.filter(name => active.exists(_.name == name))
        val nextDepth = depth + line.count(_ == '{') - line.count(_ == '}')
        (nextDepth, active ++ declared.map(Loop(_, nextDepth)), found ++ clashes)
    }
    shadowed
  }

  "JSON.NET serializer extension" should {
    "give collections nested inside an Option loop variables distinct from the enclosing loop" in {
      val source = renderedNestedOptional()
      assert(foreachDeclaration.findAllMatchIn(source).size >= 4, source)
      assert(shadowedLoopVariables(source).isEmpty, source)
    }
  }
}
