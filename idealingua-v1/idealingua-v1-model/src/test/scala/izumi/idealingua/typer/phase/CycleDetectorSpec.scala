package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class CycleDetectorSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  describe("CycleDetector") {
    it("treats terminating recursion (List<Self>) as terminating, no diagnostic") {
      // DTO A { items: list<A> }  — container-broken self-loop
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "A"),
        RawStructure(
          interfaces      = Nil,
          concepts        = Nil,
          removedConcepts = Nil,
          fields          = List(RawField(
            izumi.idealingua.model.common.IndefiniteGeneric(Seq.empty, "list", List(IndefiniteId(Seq.empty, "A"))),
            Some("items"),
            meta,
          )),
          removedFields   = Nil,
        ),
        meta,
      )
      val (input, _) = fixture(List(dtoDef), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = CycleDetector(rd0)

      val cyclicUsage = rd.diagnostics.issues.collect { case d: Diagnostic.CyclicUsage => d }
      cyclicUsage shouldBe empty
      rd.loops.exists(_.terminating) shouldBe true
    }

    it("flags DTO self-reference (direct field) as CyclicUsage + NonTerminating") {
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "A"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "A"), Some("self"), meta)), Nil),
        meta,
      )
      val (input, _) = fixture(List(dtoDef), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = CycleDetector(rd0)

      rd.diagnostics.issues.collect { case d: Diagnostic.CyclicUsage => d } should have size 1
      rd.diagnostics.issues.collect { case d: Diagnostic.NonTerminatingCycle => d } should have size 1
      rd.loops.exists(c => !c.terminating) shouldBe true
    }

    it("accepts recursive ADT through container element type (opt)") {
      // adt A = B; data B { f: opt[A] }  — cycle through opt is terminating
      val adtId  = AdtId(TypePath(domA, Seq.empty), "A")
      val dtoId  = DTOId(TypePath(domA, Seq.empty), "B")
      val adtDef = RawTypeDef.Adt(
        adtId,
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "B"), None, meta)),
        meta,
      )
      val dtoDef = RawTypeDef.DTO(
        dtoId,
        RawStructure(
          Nil,
          Nil,
          Nil,
          List(RawField(
            izumi.idealingua.model.common.IndefiniteGeneric(Seq.empty, "opt", List(IndefiniteId(Seq.empty, "A"))),
            Some("f"),
            meta,
          )),
          Nil,
        ),
        meta,
      )
      val (input, _) = fixture(List(adtDef, dtoDef), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = CycleDetector(rd0)

      rd.diagnostics.issues.collect { case d: Diagnostic.CyclicUsage => d } shouldBe empty
      rd.diagnostics.issues.collect { case d: Diagnostic.NonTerminatingCycle => d } shouldBe empty
      rd.loops.exists(_.terminating) shouldBe true
    }

    it("accepts recursive ADT through container element type (list)") {
      // adt A = B; data B { f: list[A] }
      val adtId  = AdtId(TypePath(domA, Seq.empty), "A")
      val dtoId  = DTOId(TypePath(domA, Seq.empty), "B")
      val adtDef = RawTypeDef.Adt(
        adtId,
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "B"), None, meta)),
        meta,
      )
      val dtoDef = RawTypeDef.DTO(
        dtoId,
        RawStructure(
          Nil,
          Nil,
          Nil,
          List(RawField(
            izumi.idealingua.model.common.IndefiniteGeneric(Seq.empty, "list", List(IndefiniteId(Seq.empty, "A"))),
            Some("f"),
            meta,
          )),
          Nil,
        ),
        meta,
      )
      val (input, _) = fixture(List(adtDef, dtoDef), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = CycleDetector(rd0)

      rd.diagnostics.issues.collect { case d: Diagnostic.CyclicUsage => d } shouldBe empty
      rd.diagnostics.issues.collect { case d: Diagnostic.NonTerminatingCycle => d } shouldBe empty
      rd.loops.exists(_.terminating) shouldBe true
    }

    it("accepts recursive ADT through container element type (map value)") {
      // adt A = B; data B { f: map[str, A] }
      val adtId  = AdtId(TypePath(domA, Seq.empty), "A")
      val dtoId  = DTOId(TypePath(domA, Seq.empty), "B")
      val adtDef = RawTypeDef.Adt(
        adtId,
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "B"), None, meta)),
        meta,
      )
      val dtoDef = RawTypeDef.DTO(
        dtoId,
        RawStructure(
          Nil,
          Nil,
          Nil,
          List(RawField(
            izumi.idealingua.model.common.IndefiniteGeneric(
              Seq.empty,
              "map",
              List(IndefiniteId(Seq.empty, "str"), IndefiniteId(Seq.empty, "A")),
            ),
            Some("f"),
            meta,
          )),
          Nil,
        ),
        meta,
      )
      val (input, _) = fixture(List(adtDef, dtoDef), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = CycleDetector(rd0)

      rd.diagnostics.issues.collect { case d: Diagnostic.CyclicUsage => d } shouldBe empty
      rd.diagnostics.issues.collect { case d: Diagnostic.NonTerminatingCycle => d } shouldBe empty
      rd.loops.exists(_.terminating) shouldBe true
    }

    it("flags cyclic interface inheritance as CyclicInheritance") {
      // I1 extends I2; I2 extends I1
      val i1Id = InterfaceId(TypePath(domA, Seq.empty), "I1")
      val i2Id = InterfaceId(TypePath(domA, Seq.empty), "I2")
      val i1 = RawTypeDef.Interface(
        i1Id,
        RawStructure(interfaces = List(i2Id), Nil, Nil, Nil, Nil),
        meta,
      )
      val i2 = RawTypeDef.Interface(
        i2Id,
        RawStructure(interfaces = List(i1Id), Nil, Nil, Nil, Nil),
        meta,
      )
      val (input, _) = fixture(List(i1, i2), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = CycleDetector(rd0)

      rd.diagnostics.issues.collect { case d: Diagnostic.CyclicInheritance => d } should have size 1
      rd.diagnostics.issues.collect { case d: Diagnostic.CyclicUsage => d } shouldBe empty
    }
  }
}
