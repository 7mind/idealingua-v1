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

    // ---- F2 / IMPL-7a.2: covariant-override semantics ----

    it("accepts covariant field-type override (child refines parent's field type to more specific)") {
      // base <: derived chain: BaseIfc <- DerivedIfc.
      // ParentMixin declares `f: BaseIfc`; ChildMixin extends ParentMixin and
      // re-declares `f: DerivedIfc`. The child's flattened struct should merge
      // both declarations as a soft (covariant) conflict and emit no diagnostic.
      val baseId    = InterfaceId(TypePath(domA, Seq.empty), "BaseIfc")
      val derivedId = InterfaceId(TypePath(domA, Seq.empty), "DerivedIfc")
      val base      = RawTypeDef.Interface(baseId, RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val derived = RawTypeDef.Interface(
        derivedId,
        RawStructure(List(baseId), Nil, Nil, Nil, Nil),
        meta,
      )
      val parentId = InterfaceId(TypePath(domA, Seq.empty), "ParentMixin")
      val parent = RawTypeDef.Interface(
        parentId,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "BaseIfc"), Some("f"), meta)), Nil),
        meta,
      )
      val childId = InterfaceId(TypePath(domA, Seq.empty), "ChildMixin")
      val child = RawTypeDef.Interface(
        childId,
        RawStructure(List(parentId), Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "DerivedIfc"), Some("f"), meta)), Nil),
        meta,
      )

      val (input, _) = fixture(List(base, derived, parent, child), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = StructuralFlattener(CycleDetector(rd0))

      val flat = rd.flattenedStructs(childId)
      flat.conflictsHard shouldBe empty
      flat.conflictsSoft.map(_.name) shouldBe List("f")
      // Soft-conflict fields are sorted by distance ascending: closest first.
      val softFields = flat.conflictsSoft.head.fields
      softFields.head.distance shouldBe 0
      softFields.head.field.typeId shouldBe derivedId
      softFields.last.field.typeId shouldBe baseId
      rd.diagnostics.issues.collect { case d: Diagnostic.FieldNameConflict => d } shouldBe empty
    }

    it("still rejects unrelated field-type conflict (no subtype relation between candidate types)") {
      // Two unrelated mixins (UnrelatedX, UnrelatedY) with no shared parent.
      // Parents declare `f: UnrelatedX` and `f: UnrelatedY`; the child has no
      // override. Neither type is a subtype of the other, so this stays a
      // hard FieldNameConflict.
      val xId = InterfaceId(TypePath(domA, Seq.empty), "UnrelatedX")
      val yId = InterfaceId(TypePath(domA, Seq.empty), "UnrelatedY")
      val x   = RawTypeDef.Interface(xId, RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val y   = RawTypeDef.Interface(yId, RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val p1Id = InterfaceId(TypePath(domA, Seq.empty), "Parent1")
      val p2Id = InterfaceId(TypePath(domA, Seq.empty), "Parent2")
      val p1 = RawTypeDef.Interface(
        p1Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "UnrelatedX"), Some("f"), meta)), Nil),
        meta,
      )
      val p2 = RawTypeDef.Interface(
        p2Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "UnrelatedY"), Some("f"), meta)), Nil),
        meta,
      )
      val dtoId = DTOId(TypePath(domA, Seq.empty), "ChildDTO")
      val dto = RawTypeDef.DTO(
        dtoId,
        RawStructure(List(p1Id, p2Id), Nil, Nil, Nil, Nil),
        meta,
      )
      val (input, _) = fixture(List(x, y, p1, p2, dto), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = StructuralFlattener(CycleDetector(rd0))

      val flat = rd.flattenedStructs(dtoId)
      flat.conflictsHard.map(_.name) shouldBe List("f")
      rd.diagnostics.issues.collect { case d: Diagnostic.FieldNameConflict => d.fieldName } shouldBe Vector("f")
    }

    it("accepts covariant override across diamond inheritance (most-derived type wins)") {
      // Models the `inheritance.domain` CovariantDTO2 pattern:
      //   mixin Covariant {}
      //   mixin CovariantA { & Covariant }
      //   mixin WithCovariance { field: Covariant }
      //   mixin InheritedCovariant { & WithCovariance; field: CovariantA }
      //   data CovariantDTO2 { & InheritedCovariant }
      val covId   = InterfaceId(TypePath(domA, Seq.empty), "Covariant")
      val covAId  = InterfaceId(TypePath(domA, Seq.empty), "CovariantA")
      val withCovId = InterfaceId(TypePath(domA, Seq.empty), "WithCovariance")
      val inhCovId  = InterfaceId(TypePath(domA, Seq.empty), "InheritedCovariant")
      val dtoId   = DTOId(TypePath(domA, Seq.empty), "CovariantDTO2")

      val cov    = RawTypeDef.Interface(covId, RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val covA   = RawTypeDef.Interface(covAId, RawStructure(List(covId), Nil, Nil, Nil, Nil), meta)
      val withCov = RawTypeDef.Interface(
        withCovId,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "Covariant"), Some("field"), meta)), Nil),
        meta,
      )
      val inhCov = RawTypeDef.Interface(
        inhCovId,
        RawStructure(List(withCovId), Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "CovariantA"), Some("field"), meta)), Nil),
        meta,
      )
      val dto = RawTypeDef.DTO(
        dtoId,
        RawStructure(List(inhCovId), Nil, Nil, Nil, Nil),
        meta,
      )

      val (input, _) = fixture(List(cov, covA, withCov, inhCov, dto), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = StructuralFlattener(CycleDetector(rd0))

      val inhFlat = rd.flattenedStructs(inhCovId)
      inhFlat.conflictsHard shouldBe empty
      inhFlat.conflictsSoft.map(_.name) shouldBe List("field")
      inhFlat.conflictsSoft.head.fields.head.field.typeId shouldBe covAId

      val dtoFlat = rd.flattenedStructs(dtoId)
      dtoFlat.conflictsHard shouldBe empty
      dtoFlat.conflictsSoft.map(_.name) shouldBe List("field")
      dtoFlat.conflictsSoft.head.fields.head.field.typeId shouldBe covAId

      rd.diagnostics.issues.collect { case d: Diagnostic.FieldNameConflict => d } shouldBe empty
    }
  }
}
