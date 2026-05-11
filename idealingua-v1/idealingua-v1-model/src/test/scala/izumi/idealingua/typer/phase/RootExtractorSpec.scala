package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class RootExtractorSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  describe("RootExtractor") {
    it("emits roots equal to declared user-type ids (ephemerals excluded)") {
      val ifcId = InterfaceId(TypePath(domA, Seq.empty), "I")
      val ifc   = RawTypeDef.Interface(ifcId, RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val dtoId = DTOId(TypePath(domA, Seq.empty), "D")
      val dto   = RawTypeDef.DTO(dtoId,
        RawStructure(Nil, Nil, Nil, List(RawField(IndefiniteId(Seq.empty, "str"), Some("v"), meta)), Nil),
        meta)
      val (input, _) = fixture(List(ifc, dto), Nil, Map.empty)
      val rd = RootExtractor(
        FingerprintCalculator(
          EphemeralSynthesizer(
            StructuralFlattener(
              CycleDetector(AliasDealiaser(KindChecker(NameResolver(ScopeBuilder(input)))))
            )
          )
        )
      )
      rd.roots shouldBe Set(ifcId, dtoId)
    }
  }
}
