package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class AssemblerSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def scopeFor(input: izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded) = {
    val family = IdealinguaFamilyManager(input)
    ScopeBuilder(input.id, input, family)
  }

  private def fullPipeline(input: izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded) =
    Assembler(
      RootExtractor(
        FingerprintCalculator(
          ConstValueTyper(
            EphemeralSynthesizer(
              StructuralFlattener(
                CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input)))))
              )
            )
          )
        )
      )
    )

  private def resolveOnly(input: izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded) =
    RootExtractor(
      FingerprintCalculator(
        ConstValueTyper(
          EphemeralSynthesizer(
            StructuralFlattener(
              CycleDetector(AliasDealiaser(KindChecker(NameResolver(scopeFor(input)))))
            )
          )
        )
      )
    )

  describe("Assembler") {
    it("yields a frozen Domain with populated roots, members, fingerprints for a positive 2-DTO domain") {
      val d1 = RawTypeDef.DTO(DTOId(TypePath(domA, Seq.empty), "A"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("x"), meta)), Nil), meta)
      val d2 = RawTypeDef.DTO(DTOId(TypePath(domA, Seq.empty), "B"),
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "i32"), Some("y"), meta)), Nil), meta)
      val (input, _) = fixture(List(d1, d2), Nil, Map.empty)
      val dom = fullPipeline(input)

      dom.id shouldBe domA
      dom.roots shouldBe Set(d1.id, d2.id)
      dom.members.keySet should contain allOf (d1.id, d2.id)
      dom.fingerprints.keySet should contain allOf (d1.id, d2.id)
      dom.consts shouldBe empty
    }

    it("accumulates multiple diagnostics from independent phases (Phase 2 + Phase 5)") {
      val dtoSelf = RawTypeDef.DTO(DTOId(TypePath(domA, Seq.empty), "Self"),
        RawStructure(Nil, Nil, Nil, List(
          RawField(IndefiniteId(Seq.empty, "Self"), Some("self"), meta),         // direct self-ref → CyclicUsage
          RawField(IndefiniteId(Seq.empty, "Missing"), Some("m"), meta),         // unknown → UnknownTypeRef
        ), Nil), meta)
      val (input, _) = fixture(List(dtoSelf), Nil, Map.empty)
      val rd = resolveOnly(input)
      val codes = rd.diagnostics.issues.map(_.getClass.getSimpleName).toSet
      codes should contain("UnknownTypeRef")
      codes should contain("CyclicUsage")
      // sanity: assembler still runs without exceptions
      val _ = Assembler(rd)
      succeed
    }
  }
}
