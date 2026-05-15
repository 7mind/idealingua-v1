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
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, FamilyIndex}
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

      val family = singleEntryFamily(input)
      val scoped = ScopeBuilder(input.id, input, family)

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

      val family = singleEntryFamily(input)
      val scoped = ScopeBuilder(input.id, input, family)

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

      val family = singleEntryFamily(input)
      val scoped = ScopeBuilder(input.id, input, family)

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

      val family = singleEntryFamily(input)
      val scoped = ScopeBuilder(input.id, input, family)

      scoped.diagnostics.issues.collect { case d: Diagnostic.ForeignTypeUnsupported => d.name } shouldBe Vector("Foreign")
      scoped.localNames.keySet shouldBe Set("A")
    }

    it("resolves transitively re-exported imports (A imports X from B, B imports X from C)") {
      // Mirrors the legacy IDLPostTyper.makeDefinite path that walks the
      // imported domain's `mapping` (= local types ++ its own imports). Real
      // codebases use hub-style domains that import a name from one place and
      // re-export it to downstream domains; without transitive walking, the
      // downstream domain never sees a resolved TypeId for the re-exported
      // name.
      val domC = DomainId(Seq("test"), "c")
      val cDto = dto("X", domC)
      val resolvedC = resolved(domC, List(cDto))

      // Domain B has no local types — it just re-exports X via an import from C.
      val bImports: Seq[SingleImport] = List(SingleImport(domC, ImportedId("X", None)))
      val resolvedB = resolved(domB, Nil)

      val (inputA, _) = fixture(
        local      = List(dto("A")),
        imports    = List(SingleImport(domB, ImportedId("X", None))),
        referenced = Map.empty,
      )

      val stubB = DomainMeshLoaded(
        id               = domB,
        origin           = FSPath.Name("b.domain"),
        directInclusions = Seq.empty,
        originalImports  = Seq.empty,
        meta             = meta,
        types            = Nil,
        services         = Seq.empty,
        buzzers          = Seq.empty,
        streams          = Seq.empty,
        consts           = Seq.empty,
        imports          = bImports,
        defn             = resolvedB,
      )
      val stubC = DomainMeshLoaded(
        id               = domC,
        origin           = FSPath.Name("c.domain"),
        directInclusions = Seq.empty,
        originalImports  = Seq.empty,
        meta             = meta,
        types            = List(cDto),
        services         = Seq.empty,
        buzzers          = Seq.empty,
        streams          = Seq.empty,
        consts           = Seq.empty,
        imports          = Seq.empty,
        defn             = resolvedC,
      )
      val family = FamilyIndex(
        domains     = Map(domA -> inputA, domB -> stubB, domC -> stubC),
        importGraph = Map(domA -> Set(domB), domB -> Set(domC), domC -> Set.empty),
        loadOrder   = List(domC, domB, domA),
        diagnostics = Diagnostics.empty,
      )

      val scoped = ScopeBuilder(inputA.id, inputA, family)
      scoped.importedNames.keySet shouldBe Set("X")
      // The TypeId points to the original declaration site in domC, not domB.
      scoped.importedNames("X") shouldBe DTOId(TypePath(domC, Seq.empty), "X")
      scoped.diagnostics.isEmpty shouldBe true
    }

    it("resolves imported type from family.domains (cross-domain family lookup)") {
      // Domain A imports type C from domain C.  C is not in A's defn.referenced
      // but IS in the family index — verifies that ScopeBuilder uses family, not
      // the embedded defn.referenced map.
      val domC = DomainId(Seq("test"), "c")
      val cDto = dto("C", domC)
      val resolvedC = resolved(domC, List(cDto))

      val (inputA, _) = fixture(
        local      = List(dto("A")),
        imports    = List(SingleImport(domC, ImportedId("C", None))),
        referenced = Map(domC -> resolvedC),
      )

      // Build a family that has both A and a stub for C.
      val stubC = DomainMeshLoaded(
        id               = domC,
        origin           = FSPath.Name("c.domain"),
        directInclusions = Seq.empty,
        originalImports  = Seq.empty,
        meta             = meta,
        types            = List(cDto),
        services         = Seq.empty,
        buzzers          = Seq.empty,
        streams          = Seq.empty,
        consts           = Seq.empty,
        imports          = Seq.empty,
        defn             = resolvedC,
      )
      val family = FamilyIndex(
        domains     = Map(domA -> inputA, domC -> stubC),
        importGraph = Map(domA -> Set(domC), domC -> Set.empty),
        loadOrder   = List(domC, domA),
        diagnostics = Diagnostics.empty,
      )

      val scoped = ScopeBuilder(inputA.id, inputA, family)
      scoped.importedNames.keySet shouldBe Set("C")
      scoped.importedNames("C") shouldBe DTOId(TypePath(domC, Seq.empty), "C")
      scoped.diagnostics.isEmpty shouldBe true
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

  /** Build a single-domain `FamilyIndex` wrapping `root`, plus any imported
    * domains found in `root.defn.referenced` (promoted to minimal stubs).
    *
    * Simulates what `IdealinguaFamilyManager` would produce for a single root.
    */
  def singleEntryFamily(root: DomainMeshLoaded): FamilyIndex = {
    // For tests where the root carries referenced domains in defn.referenced,
    // promote them to DomainMeshLoaded stubs so ScopeBuilder can resolve imports.
    val stubs: Map[DomainId, DomainMeshLoaded] = root.defn.referenced.map {
      case (id, mesh) =>
        val types = mesh.members.iterator.collect {
          case d: RawTopLevelDefn.TLDBaseType    => d.v: RawTypeDef
          case d: RawTopLevelDefn.TLDNewtype     => d.v: RawTypeDef
          case d: RawTopLevelDefn.TLDForeignType => d.v: RawTypeDef
        }.toSeq
        val stub = DomainMeshLoaded(
          id               = mesh.id,
          origin           = mesh.origin,
          directInclusions = mesh.directInclusions,
          originalImports  = mesh.imports,
          meta             = mesh.meta,
          types            = types,
          services         = Seq.empty,
          buzzers          = Seq.empty,
          streams          = Seq.empty,
          consts           = Seq.empty,
          imports          = mesh.imports.flatMap { imp =>
            imp.identifiers.map(iid => SingleImport(imp.id, iid))
          }.toSeq,
          defn             = mesh,
        )
        id -> stub
    }
    FamilyIndex(
      domains     = stubs + (root.id -> root),
      importGraph = Map(root.id -> root.defn.referenced.keys.toSet) ++
                    stubs.map { case (id, _) => id -> Set.empty[DomainId] },
      loadOrder   = stubs.keys.toList.sortBy(_.toString) :+ root.id,
      diagnostics = Diagnostics.empty,
    )
  }

  /** Convenience: build ScopeBuilder output for a single-domain fixture.
    *
    * Constructs a single-entry `FamilyIndex` via `IdealinguaFamilyManager` and
    * delegates to `ScopeBuilder.apply(id, parsed, family)`.  All IMPL-2/3 specs
    * that previously called `ScopeBuilder(input)` directly use this helper to
    * adapt to the IMPL-4 signature without changing the test logic.
    */
  def scopeFor(input: DomainMeshLoaded): ScopeBuilder.ScopedDomain = {
    val family = IdealinguaFamilyManager(input)
    ScopeBuilder(input.id, input, family)
  }

  // ParsedId helper for NewType cases used elsewhere
  def parsedId(name: String, dom: DomainId = domA): ParsedId = ParsedId(dom.pkg :+ dom.id, name)
}
