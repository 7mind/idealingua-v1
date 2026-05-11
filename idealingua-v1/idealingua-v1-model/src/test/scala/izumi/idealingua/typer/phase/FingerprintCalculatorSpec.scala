package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class FingerprintCalculatorSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def pipeline(input: izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded) =
    FingerprintCalculator(
      EphemeralSynthesizer(
        StructuralFlattener(
          CycleDetector(AliasDealiaser(KindChecker(NameResolver(ScopeBuilder(input)))))
        )
      )
    )

  describe("FingerprintCalculator") {
    it("produces a deterministic fingerprint for identical input across two runs") {
      val dtoId = DTOId(TypePath(domA, Seq.empty), "P")
      val dto = RawTypeDef.DTO(dtoId,
        RawStructure(Nil, Nil, Nil, List(
          RawField(IndefiniteId(Seq.empty, "str"), Some("name"), meta),
          RawField(IndefiniteId(Seq.empty, "i32"), Some("age"), meta),
        ), Nil), meta)
      val (input1, _) = fixture(List(dto), Nil, Map.empty)
      val (input2, _) = fixture(List(dto), Nil, Map.empty)

      val rd1 = pipeline(input1)
      val rd2 = pipeline(input2)

      rd1.fingerprints(dtoId).value shouldBe rd2.fingerprints(dtoId).value
      rd1.domainFingerprint.value shouldBe rd2.domainFingerprint.value
    }

    it("changes the fingerprint when fields are reordered (C12 enforcement)") {
      val dtoId = DTOId(TypePath(domA, Seq.empty), "P")
      val dtoAB = RawTypeDef.DTO(dtoId,
        RawStructure(Nil, Nil, Nil, List(
          RawField(IndefiniteId(Seq.empty, "str"), Some("a"), meta),
          RawField(IndefiniteId(Seq.empty, "i32"), Some("b"), meta),
        ), Nil), meta)
      val dtoBA = RawTypeDef.DTO(dtoId,
        RawStructure(Nil, Nil, Nil, List(
          RawField(IndefiniteId(Seq.empty, "i32"), Some("b"), meta),
          RawField(IndefiniteId(Seq.empty, "str"), Some("a"), meta),
        ), Nil), meta)
      val (inAB, _) = fixture(List(dtoAB), Nil, Map.empty)
      val (inBA, _) = fixture(List(dtoBA), Nil, Map.empty)

      val rdAB = pipeline(inAB)
      val rdBA = pipeline(inBA)

      rdAB.fingerprints(dtoId).value should not equal rdBA.fingerprints(dtoId).value
    }
  }
}
