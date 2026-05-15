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

    it("accepts DTO → ADT → DTO cycle when one back-edge is Container (mixed-kind cycle)") {
      // data A { b: B }       — Direct edge A → B
      // adt  B = C            — Container edge B → C (ADT alt)
      // data C { a: opt[A] }  — Container edge C → A
      // Subgraph of SCC {A, B, C} restricted to non-Container edges = { A → B }; acyclic ⇒ terminating.
      val aId = DTOId(TypePath(domA, Seq.empty), "A")
      val bId = AdtId(TypePath(domA, Seq.empty), "B")
      val cId = DTOId(TypePath(domA, Seq.empty), "C")
      val aDef = RawTypeDef.DTO(
        aId,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "B"), Some("b"), meta)), Nil),
        meta,
      )
      val bDef = RawTypeDef.Adt(
        bId,
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "C"), None, meta)),
        meta,
      )
      val cDef = RawTypeDef.DTO(
        cId,
        RawStructure(
          Nil,
          Nil,
          Nil,
          List(RawField(
            izumi.idealingua.model.common.IndefiniteGeneric(Seq.empty, "opt", List(IndefiniteId(Seq.empty, "A"))),
            Some("a"),
            meta,
          )),
          Nil,
        ),
        meta,
      )
      val (input, _) = fixture(List(aDef, bDef, cDef), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = CycleDetector(rd0)

      rd.diagnostics.issues.collect { case d: Diagnostic.CyclicUsage => d } shouldBe empty
      rd.diagnostics.issues.collect { case d: Diagnostic.NonTerminatingCycle => d } shouldBe empty
      rd.loops.exists(_.terminating) shouldBe true
    }

    it("accepts recursive ADT with multi-DTO direct fan-in to the ADT") {
      // data F1 { tpe: A }       — Direct F1 → A
      // data F2 { tpe: A }       — Direct F2 → A
      // data F3 { items: list[A] } — Container F3 → A
      // adt  A = F1 | F2 | F3    — Container A → {F1, F2, F3} (ADT alts)
      // Subgraph of SCC restricted to non-Container = { F1 → A, F2 → A };
      // acyclic (A has no outgoing non-Container edges) ⇒ terminating.
      val f1Id = DTOId(TypePath(domA, Seq.empty), "F1")
      val f2Id = DTOId(TypePath(domA, Seq.empty), "F2")
      val f3Id = DTOId(TypePath(domA, Seq.empty), "F3")
      val aId  = AdtId(TypePath(domA, Seq.empty), "A")
      val f1 = RawTypeDef.DTO(
        f1Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "A"), Some("tpe"), meta)), Nil),
        meta,
      )
      val f2 = RawTypeDef.DTO(
        f2Id,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "A"), Some("tpe"), meta)), Nil),
        meta,
      )
      val f3 = RawTypeDef.DTO(
        f3Id,
        RawStructure(
          Nil,
          Nil,
          Nil,
          List(RawField(
            izumi.idealingua.model.common.IndefiniteGeneric(Seq.empty, "list", List(IndefiniteId(Seq.empty, "A"))),
            Some("items"),
            meta,
          )),
          Nil,
        ),
        meta,
      )
      val aDef = RawTypeDef.Adt(
        aId,
        List(
          RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "F1"), None, meta),
          RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "F2"), None, meta),
          RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "F3"), None, meta),
        ),
        meta,
      )
      val (input, _) = fixture(List(f1, f2, f3, aDef), Nil, Map.empty)
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
