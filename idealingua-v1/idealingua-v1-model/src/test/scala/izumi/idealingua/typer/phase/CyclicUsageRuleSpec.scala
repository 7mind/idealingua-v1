package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.IndefiniteId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.TypePath
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import izumi.idealingua.typer.phase.rules.CyclicUsageRule
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class CyclicUsageRuleSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("CyclicUsageRule") {
    it("emits no CyclicUsage for an acyclic DTO") {
      val d = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Leaf"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("x"), meta)), Nil),
        meta,
      )
      val domain = assembleDomain(List(d))
      CyclicUsageRule(domain).issues
        .collect { case d: Diagnostic.CyclicUsage => d } shouldBe empty
    }

    it("emits CyclicUsage for a DTO with a direct self-reference (re-validates domain.loops)") {
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Self"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "Self"), Some("self"), meta)), Nil),
        meta,
      )
      val domain = assembleDomain(List(dtoDef))
      domain.loops should not be empty
      CyclicUsageRule(domain).issues
        .collect { case d: Diagnostic.CyclicUsage => d } should not be empty
    }
  }
}
