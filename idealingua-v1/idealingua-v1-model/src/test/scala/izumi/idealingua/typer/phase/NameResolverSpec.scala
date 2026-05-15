package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{DomainId, Generic, IndefiniteGeneric, IndefiniteId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, SingleImport}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, FamilyIndex, Member, TypeDef => IRTypeDef}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class NameResolverSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  describe("NameResolver") {
    it("resolves a primitive-keyed identifier and a DTO with a primitive field") {
      val idDef = RawTypeDef.Identifier(
        IdentifierId(TypePath(domA, Seq.empty), "Key"),
        List(RawField(IndefiniteId(Seq.empty, "str"), Some("k"), meta)),
        meta,
      )
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "D"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("v"), meta)), Nil),
        meta,
      )
      val (input, _) = fixture(List(idDef, dtoDef), Nil, Map.empty)

      val scoped   = scopeFor(input)
      val resolved = NameResolver(scoped)

      resolved.userTypes.keySet should contain allOf (idDef.id, dtoDef.id)
      resolved.members(idDef.id) shouldBe a[Member.User]
      resolved.members.get(Primitive.TString) shouldBe a[Some[?]]
      resolved.diagnostics.isEmpty shouldBe true
    }

    it("resolves an alias to an imported type") {
      val aliasDef = RawTypeDef.Alias(
        AliasId(TypePath(domA, Seq.empty), "AliasB"),
        IndefiniteId(Seq.empty, "B"),
        meta,
      )
      val refDto = dto("B", domB)
      val (input, _) = fixture(
        local      = List(aliasDef),
        imports    = List(izumi.idealingua.model.il.ast.raw.domains.SingleImport(domB, izumi.idealingua.model.il.ast.raw.domains.ImportedId("B", None))),
        referenced = Map(domB -> resolved(domB, List(refDto))),
      )
      val r = NameResolver(scopeFor(input))
      val a = r.userTypes(aliasDef.id).asInstanceOf[IRTypeDef.Alias]
      a.target shouldBe refDto.id
      r.diagnostics.isEmpty shouldBe true
    }

    it("resolves a generic List<str> field on a DTO") {
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "G"),
        RawStructure(Nil, Nil, Nil, List(RawField(
          IndefiniteGeneric(Seq.empty, "list", List(IndefiniteId(Seq.empty, "str"))),
          Some("items"),
          meta,
        )), Nil),
        meta,
      )
      val (input, _) = fixture(List(dtoDef), Nil, Map.empty)
      val r          = NameResolver(scopeFor(input))

      r.userTypes(dtoDef.id).asInstanceOf[IRTypeDef.Dto].struct.fields.head.typeId shouldBe a[Generic.TList]
      r.diagnostics.isEmpty shouldBe true
    }

    it("emits UnknownTypeRef for a missing reference") {
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Bad"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "DoesNotExist"), Some("v"), meta)), Nil),
        meta,
      )
      val (input, _) = fixture(List(dtoDef), Nil, Map.empty)
      val r          = NameResolver(scopeFor(input))

      r.diagnostics.issues.collect { case d: Diagnostic.UnknownTypeRef => d } should have size 1
    }

    it("resolves a cross-domain ref via family index (qualified by #)") {
      // Domain A has `alias X = test.b#T`; T lives in domain B and is NOT in
      // A's local scope or import-alias map.  The resolver must consult the
      // FamilyIndex to find T in B.  Mirrors F4/F5a/F5b — closes them.
      val tDef = dto("T", domB)
      val aliasDef = RawTypeDef.Alias(
        AliasId(TypePath(domA, Seq.empty), "X"),
        IndefiniteId(domB.toPackage, "T"),
        meta,
      )
      val (inputA, _) = fixture(
        local      = List(aliasDef),
        imports    = Nil, // no explicit import — only the cross-domain `#`-qualified ref
        referenced = Map.empty,
      )

      // Build a family that has both A and a stub for B exposing T.
      val stubB = DomainMeshLoaded(
        id               = domB,
        origin           = FSPath.Name("b.domain"),
        directInclusions = Seq.empty,
        originalImports  = Seq.empty,
        meta             = meta,
        types            = List(tDef),
        services         = Seq.empty,
        buzzers          = Seq.empty,
        streams          = Seq.empty,
        consts           = Seq.empty,
        imports          = Seq.empty,
        defn             = resolved(domB, List(tDef)),
      )
      val family = FamilyIndex(
        domains     = Map(domA -> inputA, domB -> stubB),
        importGraph = Map(domA -> Set.empty, domB -> Set.empty),
        loadOrder   = List(domB, domA),
        diagnostics = Diagnostics.empty,
      )

      val scoped = ScopeBuilder(inputA.id, inputA, family)
      val r      = NameResolver(scoped, family)
      val a      = r.userTypes(aliasDef.id).asInstanceOf[IRTypeDef.Alias]
      a.target shouldBe DTOId(TypePath(domB, Seq.empty), "T")
      r.diagnostics.issues.collect { case d: Diagnostic.UnknownTypeRef => d } shouldBe empty
    }

    it("emits UnknownTypeRef when the referenced domain isn't in family") {
      val aliasDef = RawTypeDef.Alias(
        AliasId(TypePath(domA, Seq.empty), "X"),
        IndefiniteId(Seq("test", "missing"), "T"),
        meta,
      )
      val (inputA, _) = fixture(List(aliasDef), Nil, Map.empty)
      val r           = NameResolver(scopeFor(inputA))

      r.diagnostics.issues.collect { case d: Diagnostic.UnknownTypeRef => d } should have size 1
    }

    it("resolves a cross-domain ref via import alias (selective import shadowing)") {
      // `import B.{ T as Renamed }` — when domain A uses `Renamed` unqualified,
      // ScopeBuilder's importedNames carries the alias, and NameResolver picks
      // it up via the local-candidate branch.
      val tDef = dto("T", domB)
      val aliasDef = RawTypeDef.Alias(
        AliasId(TypePath(domA, Seq.empty), "Local"),
        IndefiniteId(Seq.empty, "Renamed"),
        meta,
      )
      val (inputA, _) = fixture(
        local      = List(aliasDef),
        imports    = List(SingleImport(domB, izumi.idealingua.model.il.ast.raw.domains.ImportedId("T", Some("Renamed")))),
        referenced = Map(domB -> resolved(domB, List(tDef))),
      )
      val r = NameResolver(scopeFor(inputA))
      val a = r.userTypes(aliasDef.id).asInstanceOf[IRTypeDef.Alias]
      a.target shouldBe tDef.id
      r.diagnostics.issues.collect { case d: Diagnostic.UnknownTypeRef => d } shouldBe empty
    }

    it("emits WrongGenericArity for a List<a,b>") {
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Bad"),
        RawStructure(Nil, Nil, Nil, List(RawField(
          IndefiniteGeneric(Seq.empty, "list", List(IndefiniteId(Seq.empty, "str"), IndefiniteId(Seq.empty, "str"))),
          Some("v"),
          meta,
        )), Nil),
        meta,
      )
      val (input, _) = fixture(List(dtoDef), Nil, Map.empty)
      val r          = NameResolver(scopeFor(input))

      r.diagnostics.issues.collect { case d: Diagnostic.WrongGenericArity => d } should have size 1
    }
  }
}
