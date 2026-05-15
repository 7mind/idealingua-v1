package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.IndefiniteId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.TypePath
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import izumi.idealingua.typer.phase.rules.AdtMembersRule
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class AdtMembersRuleSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("AdtMembersRule") {
    it("accepts an ADT whose branches are all user types") {
      val branch = dto("Branch")
      val adtDef = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "MyAdt"),
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "Branch"), None, meta)),
        meta,
      )
      val domain = assembleDomain(List(branch, adtDef))
      AdtMembersRule(domain).issues
        .collect { case d: Diagnostic.PrimitiveAdtMember => d } shouldBe empty
    }

    it("emits PrimitiveAdtMember when an ADT branch is a primitive type") {
      // ADT with a primitive branch: str (Primitive.TString)
      // Phase 4 KindChecker doesn't reject primitive branches in ADTs;
      // that is AdtMembersRule's responsibility.
      val adtDef = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "MyAdt"),
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "str"), None, meta)),
        meta,
      )
      val domain = assembleDomain(List(adtDef))
      val diags = AdtMembersRule(domain).issues
        .collect { case d: Diagnostic.PrimitiveAdtMember => d }
      diags should not be empty
      diags.head.adtId.name shouldBe "MyAdt"
    }
  }
}
