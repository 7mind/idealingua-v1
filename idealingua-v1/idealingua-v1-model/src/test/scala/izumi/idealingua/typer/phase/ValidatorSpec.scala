package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.IndefiniteId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.TypePath
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class ValidatorSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("Validator") {
    it("returns empty Diagnostics for a clean domain with two well-named DTOs") {
      val d1 = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Alpha"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("x"), meta)), Nil),
        meta,
      )
      val d2 = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "Beta"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "i32"), Some("n"), meta)), Nil),
        meta,
      )
      val domain = assembleDomain(List(d1, d2))
      val diags  = Validator(domain)
      diags.isEmpty shouldBe true
    }

    it("aggregates BadNamingConvention from BasicNamingConventionsRule") {
      val d = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "badName"),
        RawStructure(Nil, Nil, Nil, Nil, Nil),
        meta,
      )
      val domain = assembleDomain(List(d))
      val diags  = Validator(domain)
      diags.issues.collect { case bad: Diagnostic.BadNamingConvention => bad } should not be empty
    }

    it("aggregates DuplicateEnumMember from DuplicateMemberRule") {
      val e = RawTypeDef.Enumeration(
        EnumId(TypePath(domA, Seq.empty), "Status"),
        RawEnum(Nil, List(
          RawEnumMember("Active", None, meta),
          RawEnumMember("Active", None, meta),
        ), Nil),
        meta,
      )
      val domain = assembleDomain(List(e))
      val diags  = Validator(domain)
      diags.issues.collect { case d: Diagnostic.DuplicateEnumMember => d } should not be empty
    }

    it("aggregates PrimitiveAdtMember from AdtMembersRule") {
      val adtDef = RawTypeDef.Adt(
        AdtId(TypePath(domA, Seq.empty), "MyAdt"),
        List(RawAdt.Member.TypeRef(IndefiniteId(Seq.empty, "str"), None, meta)),
        meta,
      )
      val domain = assembleDomain(List(adtDef))
      val diags  = Validator(domain)
      diags.issues.collect { case d: Diagnostic.PrimitiveAdtMember => d } should not be empty
    }

    it("aggregates CyclicUsage from CyclicUsageRule for a self-referencing DTO") {
      val dtoDef = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "SelfRef"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "SelfRef"), Some("self"), meta)), Nil),
        meta,
      )
      val domain = assembleDomain(List(dtoDef))
      val diags  = Validator(domain)
      diags.issues.collect { case d: Diagnostic.CyclicUsage => d } should not be empty
    }

    it("collects diagnostics from multiple rules in one pass") {
      // A domain with: lowercase name + duplicate enum + primitive ADT branch
      val e = RawTypeDef.Enumeration(
        EnumId(TypePath(domA, Seq.empty), "Flag"),
        RawEnum(Nil, List(RawEnumMember("On", None, meta), RawEnumMember("On", None, meta)), Nil),
        meta,
      )
      val badDto = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "wrong"),
        RawStructure(Nil, Nil, Nil, Nil, Nil),
        meta,
      )
      val domain = assembleDomain(List(e, badDto))
      val diags  = Validator(domain)
      val classes = diags.issues.map(_.getClass.getSimpleName).toSet
      classes should contain("BadNamingConvention")
      classes should contain("DuplicateEnumMember")
    }
  }
}
