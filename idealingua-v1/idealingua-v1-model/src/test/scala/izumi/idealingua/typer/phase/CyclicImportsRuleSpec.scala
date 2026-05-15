package izumi.idealingua.typer.phase

import izumi.idealingua.typer.phase.rules.CyclicImportsRule
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class CyclicImportsRuleSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[izumi.idealingua.model.il.ast.raw.defns.RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("CyclicImportsRule") {
    it("returns empty Diagnostics (no-op: import-cycle detection is Phase 0's responsibility)") {
      // The frozen Domain does not carry the inter-domain import graph, so
      // CyclicImportsRule cannot re-detect import cycles.  It returns empty
      // Diagnostics for any input.
      val domain = assembleDomain(List(dto("MyType")))
      CyclicImportsRule(domain).isEmpty shouldBe true
    }
  }
}
