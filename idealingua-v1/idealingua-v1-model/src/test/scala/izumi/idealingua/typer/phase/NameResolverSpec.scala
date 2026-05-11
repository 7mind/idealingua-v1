package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Generic, IndefiniteGeneric, IndefiniteId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.{Diagnostic, Member, TypeDef => IRTypeDef}
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

      val scoped   = ScopeBuilder(input)
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
      val r = NameResolver(ScopeBuilder(input))
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
      val r          = NameResolver(ScopeBuilder(input))

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
      val r          = NameResolver(ScopeBuilder(input))

      r.diagnostics.issues.collect { case d: Diagnostic.UnknownTypeRef => d } should have size 1
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
      val r          = NameResolver(ScopeBuilder(input))

      r.diagnostics.issues.collect { case d: Diagnostic.WrongGenericArity => d } should have size 1
    }
  }
}
