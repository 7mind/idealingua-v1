package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, IndefiniteMixin, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class KindCheckerSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  describe("KindChecker") {
    it("accepts an identifier with primitive fields") {
      val idDef = RawTypeDef.Identifier(
        IdentifierId(TypePath(domA, Seq.empty), "K"),
        List(RawField(IndefiniteId(Seq.empty, "str"), Some("k"), meta)),
        meta,
      )
      val (input, _) = fixture(List(idDef), Nil, Map.empty)
      val r          = KindChecker(AliasDealiaser(NameResolver(scopeFor(input))))
      r.diagnostics.issues.collect { case d: Diagnostic.BadIdentifierFieldType => d } shouldBe empty
      r.diagnostics.issues.collect { case d: Diagnostic.BadMixinTarget         => d } shouldBe empty
    }

    it("accepts a DTO mixed from another DTO") {
      val base = RawTypeDef.DTO(DTOId(TypePath(domA, Seq.empty), "Base"), RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val child = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Child"),
        RawStructure(Nil, List(IndefiniteMixin(Seq.empty, "Base")), Nil, Nil, Nil),
        meta,
      )
      val (input, _) = fixture(List(base, child), Nil, Map.empty)
      val r          = KindChecker(AliasDealiaser(NameResolver(scopeFor(input))))
      r.diagnostics.issues.collect { case d: Diagnostic.BadMixinTarget => d } shouldBe empty
    }

    it("accepts an ADT with a DTO branch") {
      val dtoDef = dto("Branch")
      val adtDef = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "A"),
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "Branch"), None, meta)),
        meta,
      )
      val (input, _) = fixture(List(dtoDef, adtDef), Nil, Map.empty)
      val r          = KindChecker(AliasDealiaser(NameResolver(scopeFor(input))))
      r.diagnostics.issues.collect { case d: Diagnostic.NestedAdtMemberUnsupported => d } shouldBe empty
    }

    it("emits BadIdentifierFieldType when an identifier field is typed as a DTO") {
      val refDto = dto("X")
      val idDef = RawTypeDef.Identifier(
        IdentifierId(TypePath(domA, Seq.empty), "K"),
        List(RawField(IndefiniteId(Seq.empty, "X"), Some("k"), meta)),
        meta,
      )
      val (input, _) = fixture(List(refDto, idDef), Nil, Map.empty)
      val r          = KindChecker(AliasDealiaser(NameResolver(scopeFor(input))))
      r.diagnostics.issues.collect { case d: Diagnostic.BadIdentifierFieldType => d } should not be empty
    }

    it("emits BadMixinTarget when a mixin target is an Enum") {
      val en = enumDef("E")
      val child = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Child"),
        RawStructure(Nil, List(IndefiniteMixin(Seq.empty, "E")), Nil, Nil, Nil),
        meta,
      )
      val (input, _) = fixture(List(en, child), Nil, Map.empty)
      val r          = KindChecker(AliasDealiaser(NameResolver(scopeFor(input))))
      r.diagnostics.issues.collect { case d: Diagnostic.BadMixinTarget => d } should not be empty
    }

    it("emits NestedAdtMemberUnsupported when an ADT branch references another ADT") {
      val nested = RawTypeDef.Adt(AdtId(TypePath(domA, Seq.empty), "Inner"), Nil, meta)
      val outer = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "Outer"),
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "Inner"), None, meta)),
        meta,
      )
      val (input, _) = fixture(List(nested, outer), Nil, Map.empty)
      val r          = KindChecker(AliasDealiaser(NameResolver(scopeFor(input))))
      r.diagnostics.issues.collect { case d: Diagnostic.NestedAdtMemberUnsupported => d } should not be empty
    }
  }
}
