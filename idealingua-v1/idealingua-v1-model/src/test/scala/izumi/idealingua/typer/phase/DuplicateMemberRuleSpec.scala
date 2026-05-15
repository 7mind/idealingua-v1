package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.TypePath
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import izumi.idealingua.typer.phase.rules.DuplicateMemberRule
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class DuplicateMemberRuleSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("DuplicateMemberRule") {
    it("accepts an enum with distinct members") {
      val e = RawTypeDef.Enumeration(
        EnumId(TypePath(domA, Seq.empty), "Color"),
        RawEnum(Nil, List(
          RawEnumMember("Red", None, meta),
          RawEnumMember("Green", None, meta),
          RawEnumMember("Blue", None, meta),
        ), Nil),
        meta,
      )
      val domain = assembleDomain(List(e))
      DuplicateMemberRule(domain).issues
        .collect { case d: Diagnostic.DuplicateEnumMember => d } shouldBe empty
    }

    it("emits DuplicateEnumMember for an enum with repeated values") {
      val e = RawTypeDef.Enumeration(
        EnumId(TypePath(domA, Seq.empty), "Color"),
        RawEnum(Nil, List(
          RawEnumMember("Red", None, meta),
          RawEnumMember("Red", None, meta),
        ), Nil),
        meta,
      )
      val domain = assembleDomain(List(e))
      val diags = DuplicateMemberRule(domain).issues
        .collect { case d: Diagnostic.DuplicateEnumMember => d }
      diags should not be empty
      diags.head.memberName shouldBe "Red"
    }

    it("emits DuplicateAdtBranch for an ADT with two branches of the same type") {
      val dtoDef = dto("Branch")
      val adtDef = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "MyAdt"),
        List(
          RawAdt.Member.TypeRef(izumi.idealingua.model.common.IndefiniteId(Seq.empty, "Branch"), None, meta),
          RawAdt.Member.TypeRef(izumi.idealingua.model.common.IndefiniteId(Seq.empty, "Branch"), None, meta),
        ),
        meta,
      )
      val domain = assembleDomain(List(dtoDef, adtDef))
      val diags = DuplicateMemberRule(domain).issues
        .collect { case d: Diagnostic.DuplicateAdtBranch => d }
      diags should not be empty
    }
  }
}
