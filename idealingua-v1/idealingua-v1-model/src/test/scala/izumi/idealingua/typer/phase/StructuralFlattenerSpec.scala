package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class StructuralFlattenerSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  describe("StructuralFlattener") {
    it("merges fields from two parent interfaces in BFS order, no conflicts") {
      val i1Id = InterfaceId(TypePath(domA, Seq.empty), "I1")
      val i2Id = InterfaceId(TypePath(domA, Seq.empty), "I2")
      val i1 = RawTypeDef.Interface(
        i1Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("a"), meta)), Nil),
        meta,
      )
      val i2 = RawTypeDef.Interface(
        i2Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("b"), meta)), Nil),
        meta,
      )
      val dtoId = DTOId(TypePath(domA, Seq.empty), "D")
      val dto = RawTypeDef.DTO(
        dtoId,
        RawStructure(List(i1Id, i2Id), Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("c"), meta)), Nil),
        meta,
      )
      val (input, _) = fixture(List(i1, i2, dto), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = StructuralFlattener(CycleDetector(rd0))

      val flat = rd.flattenedStructs(dtoId)
      flat.fields.map(_.field.name) shouldBe List("c", "a", "b")
      flat.conflictsHard shouldBe empty
      flat.conflictsSoft shouldBe empty
      rd.parents(dtoId) shouldBe Set(i1Id, i2Id)
      rd.implementingDtos(i1Id) should contain(dtoId)
      rd.diagnostics.issues.collect { case d: Diagnostic.FieldNameConflict => d } shouldBe empty
    }

    it("classifies same-name same-type field on two parents as soft conflict") {
      val i1Id = InterfaceId(TypePath(domA, Seq.empty), "I1")
      val i2Id = InterfaceId(TypePath(domA, Seq.empty), "I2")
      val i1 = RawTypeDef.Interface(
        i1Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("x"), meta)), Nil),
        meta,
      )
      val i2 = RawTypeDef.Interface(
        i2Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("x"), meta)), Nil),
        meta,
      )
      val dtoId = DTOId(TypePath(domA, Seq.empty), "D")
      val dto = RawTypeDef.DTO(
        dtoId,
        RawStructure(List(i1Id, i2Id), Nil, Nil, Nil, Nil),
        meta,
      )
      val (input, _) = fixture(List(i1, i2, dto), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = StructuralFlattener(CycleDetector(rd0))

      val flat = rd.flattenedStructs(dtoId)
      flat.conflictsSoft.map(_.name) shouldBe List("x")
      flat.conflictsHard shouldBe empty
      rd.diagnostics.issues.collect { case d: Diagnostic.FieldNameConflict => d } shouldBe empty
    }

    it("emits FieldNameConflict for same-name incompatible-type fields on two parents") {
      val i1Id = InterfaceId(TypePath(domA, Seq.empty), "I1")
      val i2Id = InterfaceId(TypePath(domA, Seq.empty), "I2")
      val i1 = RawTypeDef.Interface(
        i1Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("x"), meta)), Nil),
        meta,
      )
      val i2 = RawTypeDef.Interface(
        i2Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "i32"), Some("x"), meta)), Nil),
        meta,
      )
      val dtoId = DTOId(TypePath(domA, Seq.empty), "D")
      val dto = RawTypeDef.DTO(
        dtoId,
        RawStructure(List(i1Id, i2Id), Nil, Nil, Nil, Nil),
        meta,
      )
      val (input, _) = fixture(List(i1, i2, dto), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = StructuralFlattener(CycleDetector(rd0))

      val flat = rd.flattenedStructs(dtoId)
      flat.conflictsHard.map(_.name) shouldBe List("x")
      rd.diagnostics.issues.collect { case d: Diagnostic.FieldNameConflict => d.fieldName } shouldBe Vector("x")
    }
  }
}
