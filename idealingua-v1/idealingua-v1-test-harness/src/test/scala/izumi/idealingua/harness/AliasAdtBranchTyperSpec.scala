package izumi.idealingua.harness

import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.common.TypeId.{AliasId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, TypePath}
import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.typer.ir.{Diagnostic, TypeDef}
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.{Files, Path}

/** ADT alternatives and mixins that name an alias declared in another domain
  * (#623).
  */
final class AliasAdtBranchTyperSpec extends AnyFunSuite {

  private val base =
    """domain example.base
      |
      |data Item {
      |  id: str
      |}
      |
      |id ItemId {
      |  value: str
      |}
      |
      |enum Color { RED GREEN }
      |
      |mixin Named {
      |  name: str
      |}
      |
      |alias ItemRef = Item
      |alias ItemIdRef = ItemId
      |alias ColorRef = Color
      |alias NamedRef = Named
      |alias NamedRefRef = NamedRef
      |""".stripMargin

  private val baseId = DomainId(Seq("example"), "base")
  private val apiId  = DomainId(Seq("example"), "api")

  private def load(api: String): LoadedDomain = {
    val root = Files.createTempDirectory("alias-adt-branch")
    write(root.resolve("example/base.domain"), base)
    write(root.resolve("example/api.domain"), api)
    val context = new LocalModelLoaderContext(Seq(root), Seq.empty[File])
    val models  = new ModelResolver().resolve(context.loader.load())
    models.all.collectFirst {
      case s: LoadedDomain.Success if s.parsed.id == apiId          => s
      case f: LoadedDomain.VerificationFailed if f.domain == apiId => f
    }.getOrElse(fail(s"example.api was neither typed nor rejected: ${models.all}"))
  }

  private def write(path: Path, content: String): Unit = {
    Files.createDirectories(path.getParent)
    val _ = Files.writeString(path, content)
  }

  private def accepted(api: String): LoadedDomain.Success = load(api) match {
    case s: LoadedDomain.Success => s
    case other                   => fail(s"Expected example.api to be accepted, got $other")
  }

  private def rejectedWith(api: String): Vector[Diagnostic] = load(api) match {
    case f: LoadedDomain.VerificationFailed => f.diagnostics.issues
    case other                              => fail(s"Expected example.api to be rejected, got $other")
  }

  test("an alternative that names an alias of another domain is accepted") {
    val loaded = accepted(
      """domain example.api
        |
        |import example.base
        |
        |alias LocalRef = example.base#ItemRef
        |
        |adt Choice {
        |  example.base#ItemRef as ref
        |  LocalRef as local
        |  example.base#ItemIdRef as idref
        |  example.base#ColorRef as color
        |}
        |""".stripMargin
    )
    val choice = loaded.domain.userTypes.values.collectFirst { case a: TypeDef.Adt if a.id.name == "Choice" => a }.getOrElse(fail("Choice is missing"))
    assert(choice.alternatives.map(_.typeId).collect { case a: AliasId => a.name } == List("ItemRef", "LocalRef", "ItemIdRef", "ColorRef"))
  }

  test("an alternative named through an import of a foreign alias is accepted") {
    val _ = accepted(
      """domain example.api
        |
        |import example.base.{ ItemRef }
        |
        |adt Choice {
        |  ItemRef as ref
        |}
        |""".stripMargin
    )
  }

  test("a mixin that names an alias of another domain is accepted") {
    val loaded = accepted(
      """domain example.api
        |
        |import example.base
        |
        |alias LocalNamedRef = example.base#NamedRef
        |
        |data ByInterface {
        |  & example.base#NamedRef
        |  own: str
        |}
        |
        |data ByChain {
        |  & example.base#NamedRefRef
        |  own: str
        |}
        |
        |data ByLocalAlias {
        |  & LocalNamedRef
        |  own: str
        |}
        |
        |data ByEmbed {
        |  + example.base#NamedRef
        |  own: str
        |}
        |""".stripMargin
    )
    val named = loaded.domain.userTypes.values.collect {
      case d: TypeDef.Dto if d.id.name.startsWith("By") => d.id.name -> (d.struct.superclasses.interfaces ++ d.struct.superclasses.concepts)
    }.toMap
    val expected = InterfaceId(TypePath(baseId, Seq.empty), "Named")
    assert(named.keySet == Set("ByInterface", "ByChain", "ByLocalAlias", "ByEmbed"))
    named.foreach { case (name, supers) => assert(supers == List(expected), name) }
  }

  test("a mixin that names a foreign alias of a non-structure is rejected") {
    val diags = rejectedWith(
      """domain example.api
        |
        |import example.base
        |
        |data Holder {
        |  & example.base#ColorRef
        |  own: str
        |}
        |""".stripMargin
    )
    assert(diags.collect { case d: Diagnostic.BadMixinTarget => d.badTarget.name } == Vector("Color"), diags)
  }
}
