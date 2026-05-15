package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.TypePath
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import izumi.idealingua.typer.phase.rules.BasicNamingConventionsRule
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class BasicNamingConventionsRuleSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def assembleDomain(types: List[RawTypeDef]) = {
    val (input, _) = fixture(types, Nil, Map.empty)
    Assembler(
      RootExtractor(FingerprintCalculator(ConstValueTyper(EphemeralSynthesizer(
        StructuralFlattener(CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))))
      ))))
    )
  }

  describe("BasicNamingConventionsRule") {
    it("accepts a correctly-named DTO") {
      val d = dto("Correct")
      val domain = assembleDomain(List(d))
      BasicNamingConventionsRule(domain).issues
        .collect { case bad: Diagnostic.BadNamingConvention => bad } shouldBe empty
    }

    it("emits BadNamingConvention for a lowercase-starting DTO name") {
      val d = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "lowercase"),
        RawStructure(Nil, Nil, Nil, Nil, Nil),
        meta,
      )
      val domain = assembleDomain(List(d))
      val diags = BasicNamingConventionsRule(domain).issues
        .collect { case bad: Diagnostic.BadNamingConvention => bad }
      diags should not be empty
      diags.head.typeId.name shouldBe "lowercase"
    }

    it("emits BadNamingConvention for a single-character type name") {
      val d = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "A"),
        RawStructure(Nil, Nil, Nil, Nil, Nil),
        meta,
      )
      val domain = assembleDomain(List(d))
      val diags = BasicNamingConventionsRule(domain).issues
        .collect { case bad: Diagnostic.BadNamingConvention => bad }
      diags should not be empty
    }

    it("emits BadNamingConvention for a reserved-prefix type name") {
      val d = RawTypeDef.DTO(
        DTOId(TypePath(domA, Seq.empty), "IDLMyType"),
        RawStructure(Nil, Nil, Nil, Nil, Nil),
        meta,
      )
      val domain = assembleDomain(List(d))
      val diags = BasicNamingConventionsRule(domain).issues
        .collect { case bad: Diagnostic.BadNamingConvention => bad }
      diags should not be empty
      diags.exists(_.typeId.name == "IDLMyType") shouldBe true
    }
  }
}
