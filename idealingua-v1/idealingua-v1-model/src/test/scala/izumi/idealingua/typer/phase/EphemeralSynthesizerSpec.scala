package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, DomainMeshResolved, Import}
import izumi.idealingua.model.il.ast.raw.models.Inclusion
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.typer.ir.{EphemeralOrigin, Member, TypeDef => IRTypeDef}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class EphemeralSynthesizerSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def serviceFixture(services: List[RawService]): DomainMeshLoaded = {
    val resolvedA = new DomainMeshResolved {
      override def id  = domA
      override def imports: Seq[Import] = Seq.empty
      override def members: Seq[RawTopLevelDefn] = services.map(RawTopLevelDefn.TLDService.apply)
      override def referenced: Map[izumi.idealingua.model.common.DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath = FSPath.Name("a.domain")
      override def directInclusions: Seq[Inclusion] = Seq.empty
      override def meta: RawNodeMeta = ScopeBuilderSpec.meta
    }
    DomainMeshLoaded(
      id = domA, origin = FSPath.Name("a.domain"), directInclusions = Seq.empty,
      originalImports = Seq.empty, meta = meta, types = Seq.empty,
      services = services, buzzers = Seq.empty, streams = Seq.empty,
      consts = Seq.empty, imports = Seq.empty, defn = resolvedA,
    )
  }

  describe("EphemeralSynthesizer") {
    it("synthesizes Input + Output ephemeral DTOs for a Singular RPC method") {
      val svcId = ServiceId(domA, "Svc")
      val method = RawMethod.RPCMethod(
        name = "ping",
        signature = RawMethod.Signature(
          input  = RawSimpleStructure(Nil, Nil),
          output = RawMethod.Output.Singular(IndefiniteId(Seq.empty, "str")),
        ),
        meta = meta,
      )
      val svc = RawService(svcId, List(method), meta)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(serviceFixture(List(svc))))))
      val rd  = EphemeralSynthesizer(StructuralFlattener(CycleDetector(rd0)))

      val inputId  = DTOId(TypePath(domA, Seq("Svc")), "PingInput")
      val outputId = DTOId(TypePath(domA, Seq("Svc")), "PingOutput")
      rd.members.get(inputId) shouldBe a[Some[?]]
      rd.members(inputId) shouldBe a[Member.Ephemeral]
      rd.members(outputId) shouldBe a[Member.Ephemeral]
      rd.ephemeralsOf(svcId) should contain allOf (inputId, outputId)
      rd.ephemeralOwner(inputId) shouldBe svcId
    }

    it("synthesizes Success + Failure + ADT for an Alternative output") {
      val svcId = ServiceId(domA, "Svc")
      val method = RawMethod.RPCMethod(
        name = "do",
        signature = RawMethod.Signature(
          input  = RawSimpleStructure(Nil, Nil),
          output = RawMethod.Output.Alternative(
            success = RawMethod.Output.Singular(IndefiniteId(Seq.empty, "str")),
            failure = RawMethod.Output.Singular(IndefiniteId(Seq.empty, "i32")),
          ),
        ),
        meta = meta,
      )
      val svc = RawService(svcId, List(method), meta)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(serviceFixture(List(svc))))))
      val rd  = EphemeralSynthesizer(StructuralFlattener(CycleDetector(rd0)))

      val adtId = AdtId(TypePath(domA, Seq("Svc")), "DoOutput")
      rd.members.get(adtId) shouldBe a[Some[?]]
      val adt = rd.userTypes(adtId).asInstanceOf[IRTypeDef.Adt]
      adt.alternatives.map(_.memberName) shouldBe List(Some("Success"), Some("Failure"))
      rd.ephemeralOwner(adtId) shouldBe svcId
    }

    it("synthesizes an interface mirror DTO (Struct) for every interface") {
      val ifcId = InterfaceId(TypePath(domA, Seq.empty), "I")
      val ifc = RawTypeDef.Interface(ifcId, RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val (input, _) = fixture(List(ifc), Nil, Map.empty)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
      val rd  = EphemeralSynthesizer(StructuralFlattener(CycleDetector(rd0)))

      val mirrorId = DTOId(ifcId, "Struct")
      rd.members.get(mirrorId) shouldBe a[Some[?]]
      rd.members(mirrorId) shouldBe a[Member.Ephemeral]
      val eph = rd.members(mirrorId).asInstanceOf[Member.Ephemeral].defn
      eph.origin shouldBe EphemeralOrigin.InterfaceMirror(ifcId)
      rd.ephemeralOwner(mirrorId) shouldBe ifcId
    }
  }
}
