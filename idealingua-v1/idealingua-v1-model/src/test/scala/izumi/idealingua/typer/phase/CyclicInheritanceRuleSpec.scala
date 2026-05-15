package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.TypePath
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.phase.rules.CyclicInheritanceRule
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class CyclicInheritanceRuleSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("CyclicInheritanceRule") {
    it("is a no-op: returns empty Diagnostics for any domain") {
      // The frozen Domain.loops does not carry per-edge kind metadata (Direct vs
      // Inheritance), so it is not possible to distinguish inheritance-only cycles
      // from field-reference cycles in the Validator.  CyclicInheritanceRule is
      // therefore a structural no-op; CyclicUsageRule covers all non-terminating
      // cycles as a defense-in-depth catch-all.
      val i1Id = InterfaceId(TypePath(domA, Seq.empty), "I1")
      val i2Id = InterfaceId(TypePath(domA, Seq.empty), "I2")
      val i1 = RawTypeDef.Interface(i1Id, RawStructure(interfaces = List(i2Id), Nil, Nil, Nil, Nil), meta)
      val i2 = RawTypeDef.Interface(i2Id, RawStructure(interfaces = List(i1Id), Nil, Nil, Nil, Nil), meta)
      val domain = assembleDomain(List(i1, i2))
      // Phase 5 populates domain.loops; the inheritance cycle IS detected.
      domain.loops should not be empty
      // But CyclicInheritanceRule itself returns empty (no-op).
      CyclicInheritanceRule(domain).isEmpty shouldBe true
    }
  }
}
