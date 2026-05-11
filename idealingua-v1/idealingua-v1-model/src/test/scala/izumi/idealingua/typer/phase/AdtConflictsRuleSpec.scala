package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.IndefiniteId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.TypePath
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import izumi.idealingua.typer.phase.rules.AdtConflictsRule
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class AdtConflictsRuleSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("AdtConflictsRule") {
    it("accepts an ADT with distinct branch types") {
      val b1 = dto("BranchOne")
      val b2 = dto("BranchTwo")
      val adtDef = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "MyAdt"),
        List(
          RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "BranchOne"), None, meta),
          RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "BranchTwo"), None, meta),
        ),
        meta,
      )
      val domain = assembleDomain(List(b1, b2, adtDef))
      AdtConflictsRule(domain).issues
        .collect { case d: Diagnostic.DuplicateAdtBranch => d } shouldBe empty
    }

    it("emits DuplicateAdtBranch when the same type is listed twice in an ADT") {
      val branch = dto("Branch")
      val adtDef = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "MyAdt"),
        List(
          RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "Branch"), None, meta),
          RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "Branch"), None, meta),
        ),
        meta,
      )
      val domain = assembleDomain(List(branch, adtDef))
      val diags = AdtConflictsRule(domain).issues
        .collect { case d: Diagnostic.DuplicateAdtBranch => d }
      diags should not be empty
    }
  }
}
