package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{DomainId, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, DomainMeshResolved, Import, ImportedId, SingleImport}
import izumi.idealingua.model.il.ast.raw.models.Inclusion
import izumi.idealingua.model.il.ast.raw.typeid.ParsedId
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class ScopeBuilderSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  describe("ScopeBuilder") {
    it("builds localNames + index for a 2-type domain plus a 1-type imported domain") {
      val (input, importedDtoId) = fixture(
        local = List(dto("A"), enumDef("E")),
        imports = List(SingleImport(domB, ImportedId("B", None))),
        referenced = Map(domB -> resolved(domB, List(dto("B", domB)))),
      )

      val scoped = ScopeBuilder(input)

      scoped.domainId shouldBe domA
      scoped.localNames.keySet shouldBe Set("A", "E")
      scoped.localNames("A") shouldBe DTOId(TypePath(domA, Seq.empty), "A")
      scoped.localNames("E") shouldBe EnumId(TypePath(domA, Seq.empty), "E")
      scoped.importedNames.keySet shouldBe Set("B")
      scoped.importedNames("B") shouldBe importedDtoId
      scoped.index.keySet should contain allOf (
        DTOId(TypePath(domA, Seq.empty), "A"),
        EnumId(TypePath(domA, Seq.empty), "E"),
      )
      scoped.diagnostics.isEmpty shouldBe true
    }

    it("emits ImportNameClashesWithLocal when an imported alias collides with a local name") {
      val (input, _) = fixture(
        local = List(dto("Clash")),
        imports = List(SingleImport(domB, ImportedId("B", Some("Clash")))),
        referenced = Map(domB -> resolved(domB, List(dto("B", domB)))),
      )

      val scoped = ScopeBuilder(input)

      scoped.diagnostics.issues.collect {
        case d: Diagnostic.ImportNameClashesWithLocal => d.name
      } shouldBe Vector("Clash")
    }

    it("emits ScopeCollision for duplicate local names") {
      val (input, _) = fixture(
        local = List(dto("X"), enumDef("X")),
        imports = Nil,
        referenced = Map.empty,
      )

      val scoped = ScopeBuilder(input)

      val collisions = scoped.diagnostics.issues.collect { case d: Diagnostic.ScopeCollision => d.name }
      collisions shouldBe Vector("X")
      scoped.localNames("X") shouldBe DTOId(TypePath(domA, Seq.empty), "X") // first definition wins
    }

    it("emits ForeignTypeUnsupported for foreign type definitions") {
      val foreign = RawTypeDef.ForeignType(
        id      = izumi.idealingua.model.common.IndefiniteId(Seq.empty, "Foreign"),
        mapping = Map.empty,
        meta    = meta,
      )
      val (input, _) = fixture(
        local      = List(dto("A"), foreign),
        imports    = Nil,
        referenced = Map.empty,
      )

      val scoped = ScopeBuilder(input)

      scoped.diagnostics.issues.collect { case d: Diagnostic.ForeignTypeUnsupported => d.name } shouldBe Vector("Foreign")
      scoped.localNames.keySet shouldBe Set("A")
    }
  }
}

object ScopeBuilderSpec {
  val domA: DomainId = DomainId(Seq("test"), "a")
  val domB: DomainId = DomainId(Seq("test"), "b")

  val meta: RawNodeMeta = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)

  def dto(name: String, dom: DomainId = domA): RawTypeDef.DTO =
    RawTypeDef.DTO(DTOId(TypePath(dom, Seq.empty), name), RawStructure(Nil, Nil, Nil, Nil, Nil), meta)

  def enumDef(name: String, dom: DomainId = domA): RawTypeDef.Enumeration =
    RawTypeDef.Enumeration(EnumId(TypePath(dom, Seq.empty), name), RawEnum(Nil, Nil, Nil), meta)

  def aliasDef(name: String, target: izumi.idealingua.model.common.AbstractIndefiniteId, dom: DomainId = domA): RawTypeDef.Alias =
    RawTypeDef.Alias(AliasId(TypePath(dom, Seq.empty), name), target, meta)

  def resolved(
    dom: DomainId,
    types: List[RawTypeDef],
    services: List[RawService] = Nil,
    buzzers: List[RawBuzzer] = Nil,
    streams: List[RawStreams] = Nil,
    referenced: Map[DomainId, DomainMeshResolved] = Map.empty,
    imports: Seq[Import] = Seq.empty,
  ): DomainMeshResolved = {
    val refMap = referenced
    val imp    = imports
    new DomainMeshResolved {
      override def id: DomainId                                  = dom
      override def imports: Seq[Import]                          = imp
      override def members: Seq[RawTopLevelDefn]                 = {
        val baseMembers: Seq[RawTopLevelDefn] = types.map {
          case w: RawTypeDef.WithId       => RawTopLevelDefn.TLDBaseType(w)
          case n: RawTypeDef.NewType      => RawTopLevelDefn.TLDNewtype(n)
          case f: RawTypeDef.ForeignType  => RawTopLevelDefn.TLDForeignType(f)
          case d: RawTypeDef.DeclaredType => RawTopLevelDefn.TLDDeclared(d)
        }
        val svc = services.map(RawTopLevelDefn.TLDService.apply)
        val bz  = buzzers.map(RawTopLevelDefn.TLDBuzzer.apply)
        val st  = streams.map(RawTopLevelDefn.TLDStreams.apply)
        baseMembers ++ svc ++ bz ++ st
      }
      override def referenced: Map[DomainId, DomainMeshResolved] = refMap
      override def origin: FSPath                                = FSPath.Name(dom.id + ".domain")
      override def directInclusions: Seq[Inclusion]              = Seq.empty
      override def meta: RawNodeMeta                             = ScopeBuilderSpec.meta
    }
  }

  def fixture(
    local: List[RawTypeDef],
    imports: List[SingleImport],
    referenced: Map[DomainId, DomainMeshResolved],
  ): (DomainMeshLoaded, TypeId) = {
    val resolvedA = resolved(domA, local, referenced = referenced)
    val importedTid = imports.headOption.flatMap { si =>
      referenced.get(si.domain).flatMap { ref =>
        ref.members.collectFirst {
          case d: RawTopLevelDefn.TLDBaseType if d.v.id.name == si.imported.name => d.v.id: TypeId
        }
      }
    }
    val originalImports: Seq[Import] = imports.groupBy(_.domain).toSeq.map {
      case (dom, group) => Import(dom, group.map(_.imported).toSet)
    }
    val loaded = DomainMeshLoaded(
      id                = domA,
      origin            = FSPath.Name("a.domain"),
      directInclusions  = Seq.empty,
      originalImports   = originalImports,
      meta              = meta,
      types             = local,
      services          = Seq.empty,
      buzzers           = Seq.empty,
      streams           = Seq.empty,
      consts            = Seq.empty,
      imports           = imports,
      defn              = resolvedA,
    )
    (loaded, importedTid.getOrElse(DTOId(TypePath(domA, Seq.empty), "<sentinel>")))
  }

  // ParsedId helper for NewType cases used elsewhere
  def parsedId(name: String, dom: DomainId = domA): ParsedId = ParsedId(dom.pkg :+ dom.id, name)
}
