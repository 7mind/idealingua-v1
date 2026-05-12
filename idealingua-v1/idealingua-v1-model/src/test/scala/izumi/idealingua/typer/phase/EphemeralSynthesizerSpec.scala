package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Generic, IndefiniteGeneric, IndefiniteId, Primitive, TypePath}
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

    it("auto-wraps list-typed alt-output branch in a synthesized DTO") {
      val svcId = ServiceId(domA, "Svc")
      val method = RawMethod.RPCMethod(
        name = "doList",
        signature = RawMethod.Signature(
          input  = RawSimpleStructure(Nil, Nil),
          output = RawMethod.Output.Alternative(
            success = RawMethod.Output.Singular(IndefiniteGeneric(Seq.empty, "list", List(IndefiniteId(Seq.empty, "str")))),
            failure = RawMethod.Output.Singular(IndefiniteId(Seq.empty, "i32")),
          ),
        ),
        meta = meta,
      )
      val svc = RawService(svcId, List(method), meta)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(serviceFixture(List(svc))))))
      val rd  = EphemeralSynthesizer(StructuralFlattener(CycleDetector(rd0)))

      val ownerPath = TypePath(domA, Seq("Svc"))
      val adtId     = AdtId(ownerPath, "DoListOutput")
      val successId = DTOId(ownerPath, "DoListSuccess")
      val failureId = DTOId(ownerPath, "DoListFailure")

      val adt = rd.userTypes(adtId).asInstanceOf[IRTypeDef.Adt]
      adt.alternatives.map(_.typeId) shouldBe List(successId, failureId)

      // Wrapper DTOs exist as Member.Ephemeral.
      rd.members(successId) shouldBe a[Member.Ephemeral]
      rd.members(failureId) shouldBe a[Member.Ephemeral]

      // Wrapper DTO carries a single `value` field with the original branch
      // type (a `Generic.TList` for the success branch, a `Primitive.TInt32`
      // for the failure branch).
      val successEph = rd.members(successId).asInstanceOf[Member.Ephemeral].defn
      successEph.struct.fields.map(_.name) shouldBe List("value")
      successEph.struct.fields.head.typeId shouldBe Generic.TList(Primitive.TString)

      val failureEph = rd.members(failureId).asInstanceOf[Member.Ephemeral].defn
      failureEph.struct.fields.map(_.name) shouldBe List("value")
      failureEph.struct.fields.head.typeId shouldBe Primitive.TInt32

      rd.ephemeralOwner(successId) shouldBe svcId
      rd.ephemeralOwner(failureId) shouldBe svcId
    }

    it("auto-wraps map-typed alt-output branch") {
      val svcId = ServiceId(domA, "Svc")
      val method = RawMethod.RPCMethod(
        name = "doMap",
        signature = RawMethod.Signature(
          input  = RawSimpleStructure(Nil, Nil),
          output = RawMethod.Output.Alternative(
            success = RawMethod.Output.Singular(IndefiniteGeneric(Seq.empty, "map", List(IndefiniteId(Seq.empty, "str"), IndefiniteId(Seq.empty, "str")))),
            failure = RawMethod.Output.Singular(IndefiniteGeneric(Seq.empty, "set", List(IndefiniteId(Seq.empty, "str")))),
          ),
        ),
        meta = meta,
      )
      val svc = RawService(svcId, List(method), meta)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(serviceFixture(List(svc))))))
      val rd  = EphemeralSynthesizer(StructuralFlattener(CycleDetector(rd0)))

      val ownerPath = TypePath(domA, Seq("Svc"))
      val successId = DTOId(ownerPath, "DoMapSuccess")
      val failureId = DTOId(ownerPath, "DoMapFailure")

      val successEph = rd.members(successId).asInstanceOf[Member.Ephemeral].defn
      successEph.struct.fields.head.typeId shouldBe Generic.TMap(Primitive.TString, Primitive.TString)

      val failureEph = rd.members(failureId).asInstanceOf[Member.Ephemeral].defn
      failureEph.struct.fields.head.typeId shouldBe Generic.TSet(Primitive.TString)
    }

    it("does NOT wrap DTO-typed alt-output branch (passes through)") {
      val dtoId = DTOId(TypePath(domA, Seq.empty), "Payload")
      val dtoDef = RawTypeDef.DTO(dtoId, RawStructure(Nil, Nil, Nil, Nil, Nil), meta)
      val svcId = ServiceId(domA, "Svc")
      val method = RawMethod.RPCMethod(
        name = "doDto",
        signature = RawMethod.Signature(
          input  = RawSimpleStructure(Nil, Nil),
          output = RawMethod.Output.Alternative(
            success = RawMethod.Output.Singular(IndefiniteId(Seq.empty, "Payload")),
            failure = RawMethod.Output.Singular(IndefiniteId(Seq.empty, "Payload")),
          ),
        ),
        meta = meta,
      )
      val svc = RawService(svcId, List(method), meta)

      // Build a fixture with both a DTO and a service.
      val resolvedA = new DomainMeshResolved {
        override def id  = domA
        override def imports: Seq[Import] = Seq.empty
        override def members: Seq[RawTopLevelDefn] = Seq(
          RawTopLevelDefn.TLDBaseType(dtoDef),
          RawTopLevelDefn.TLDService(svc),
        )
        override def referenced: Map[izumi.idealingua.model.common.DomainId, DomainMeshResolved] = Map.empty
        override def origin: FSPath = FSPath.Name("a.domain")
        override def directInclusions: Seq[Inclusion] = Seq.empty
        override def meta: RawNodeMeta = ScopeBuilderSpec.meta
      }
      val loaded = DomainMeshLoaded(
        id = domA, origin = FSPath.Name("a.domain"), directInclusions = Seq.empty,
        originalImports = Seq.empty, meta = meta, types = Seq(dtoDef),
        services = List(svc), buzzers = Seq.empty, streams = Seq.empty,
        consts = Seq.empty, imports = Seq.empty, defn = resolvedA,
      )

      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(loaded))))
      val rd  = EphemeralSynthesizer(StructuralFlattener(CycleDetector(rd0)))

      val ownerPath = TypePath(domA, Seq("Svc"))
      val adtId     = AdtId(ownerPath, "DoDtoOutput")

      val adt = rd.userTypes(adtId).asInstanceOf[IRTypeDef.Adt]
      // Both branches reference the user-declared DTO directly — NO wrapper DTOs.
      adt.alternatives.map(_.typeId) shouldBe List(dtoId, dtoId)
      rd.members.contains(DTOId(ownerPath, "DoDtoSuccess")) shouldBe false
      rd.members.contains(DTOId(ownerPath, "DoDtoFailure")) shouldBe false
    }

    it("wrapper DTO names follow `<MethodBase><Success|Failure>` convention") {
      val svcId = ServiceId(domA, "TestService")
      val method = RawMethod.RPCMethod(
        name = "alternativeGeneric",
        signature = RawMethod.Signature(
          input  = RawSimpleStructure(Nil, Nil),
          output = RawMethod.Output.Alternative(
            success = RawMethod.Output.Singular(IndefiniteGeneric(Seq.empty, "list", List(IndefiniteId(Seq.empty, "str")))),
            failure = RawMethod.Output.Singular(IndefiniteGeneric(Seq.empty, "set", List(IndefiniteId(Seq.empty, "str")))),
          ),
        ),
        meta = meta,
      )
      val svc = RawService(svcId, List(method), meta)
      val rd0 = AliasDealiaser(KindChecker(NameResolver(scopeFor(serviceFixture(List(svc))))))
      val rd  = EphemeralSynthesizer(StructuralFlattener(CycleDetector(rd0)))

      val ownerPath = TypePath(domA, Seq("TestService"))
      rd.members.contains(DTOId(ownerPath, "AlternativeGenericSuccess")) shouldBe true
      rd.members.contains(DTOId(ownerPath, "AlternativeGenericFailure")) shouldBe true
      rd.members.contains(AdtId(ownerPath, "AlternativeGenericOutput")) shouldBe true
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
