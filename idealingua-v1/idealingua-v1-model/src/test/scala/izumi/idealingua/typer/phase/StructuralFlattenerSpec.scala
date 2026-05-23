package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{DomainId, IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, ImportedId, SingleImport}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, FamilyIndex}
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

    // ---- PR-01-D07: write-side harvest test ----
    //
    // Verifies that `StructuralFlattener.apply(local, family)` populates
    // `crossDomainUserTypes` and `crossDomainFlattenedStructs` when the
    // local domain references foreign types only through field positions
    // (not as mixin supers).  This exercises the write-side of the harvest
    // so a regression that leaves those maps empty is caught at the unit
    // level rather than only through full-pipeline IDL fixtures.

    it("populates crossDomainUserTypes and crossDomainFlattenedStructs from field-type references") {
      // --- foreign domain: idltest.foreign_ids ---
      //   identifier ItemID { f1: str; f2: str }
      //   data MultiFieldDto { x: str; y: str }
      val domForeign  = DomainId(Seq("idltest"), "foreign_ids")
      val itemIdId    = IdentifierId(TypePath(domForeign, Seq.empty), "ItemID")
      val foreignDtoId = DTOId(TypePath(domForeign, Seq.empty), "MultiFieldDto")

      val foreignIdentDef = RawTypeDef.Identifier(
        itemIdId,
        List(
          RawField(IndefiniteId(Seq.empty, "str"), Some("f1"), meta),
          RawField(IndefiniteId(Seq.empty, "str"), Some("f2"), meta),
        ),
        meta,
      )
      val foreignDtoDef = RawTypeDef.DTO(
        foreignDtoId,
        RawStructure(Nil, Nil, Nil, List(
          RawField(IndefiniteId(Seq.empty, "str"), Some("x"), meta),
          RawField(IndefiniteId(Seq.empty, "str"), Some("y"), meta),
        ), Nil),
        meta,
      )

      val foreignDefn  = resolved(domForeign, List(foreignIdentDef, foreignDtoDef))
      val foreignMesh = DomainMeshLoaded(
        id               = domForeign,
        origin           = FSPath.Name("foreign_ids.domain"),
        directInclusions = Seq.empty,
        originalImports  = Seq.empty,
        meta             = meta,
        types            = List(foreignIdentDef, foreignDtoDef),
        services         = Seq.empty,
        buzzers          = Seq.empty,
        streams          = Seq.empty,
        consts           = Seq.empty,
        imports          = Seq.empty,
        defn             = foreignDefn,
      )

      // --- local domain: idltest.a (= domA) ---
      //   imports ItemID and MultiFieldDto from domForeign
      //   data D    { val: ItemID      }
      //   data D2   { val: MultiFieldDto }
      val localImports = List(
        SingleImport(domForeign, ImportedId("ItemID",       None)),
        SingleImport(domForeign, ImportedId("MultiFieldDto", None)),
      )
      val dDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "D"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "ItemID"),       Some("val"), meta)), Nil),
        meta,
      )
      val d2Def = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "D2"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "MultiFieldDto"), Some("val"), meta)), Nil),
        meta,
      )

      val (localMesh, _) = fixture(
        local      = List(dDef, d2Def),
        imports    = localImports,
        referenced = Map(domForeign -> foreignDefn),
      )

      // Build a FamilyIndex containing both domains so StructuralFlattener can
      // resolve the foreign mesh when it follows field-type references.
      val family = FamilyIndex(
        domains     = Map(domA -> localMesh, domForeign -> foreignMesh),
        importGraph = Map(domA -> Set(domForeign), domForeign -> Set.empty[DomainId]),
        loadOrder   = List(domForeign, domA),
        diagnostics = Diagnostics.empty,
      )

      // Run local domain through the typer pipeline up to (but not including)
      // StructuralFlattener, then apply StructuralFlattener with the family.
      val localRd0 = AliasDealiaser(KindChecker(NameResolver(ScopeBuilder(domA, localMesh, family), family)))
      val localRd  = StructuralFlattener(CycleDetector(localRd0), family)

      // Write-side assertions: the harvest must have populated the maps.
      import izumi.idealingua.typer.ir.TypeDef
      localRd.crossDomainUserTypes.get(itemIdId) match {
        case Some(td: TypeDef.Identifier) => td.fields.size shouldBe 2
        case other                        => fail(s"expected TypeDef.Identifier, got $other")
      }

      localRd.crossDomainFlattenedStructs should contain key foreignDtoId
      localRd.crossDomainFlattenedStructs(foreignDtoId).fields.size shouldBe 2

      // PR-02-D02: the widening to all TypeDef categories means the foreign DTO
      // must also appear in crossDomainUserTypes as a TypeDef.Dto.
      localRd.crossDomainUserTypes.get(foreignDtoId) match {
        case Some(_: TypeDef.Dto) => // expected
        case other                => fail(s"expected TypeDef.Dto for foreignDtoId, got $other")
      }
    }
  }
}
