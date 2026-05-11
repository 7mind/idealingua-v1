package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, DomainMeshResolved, Import}
import izumi.idealingua.model.il.ast.raw.models.Inclusion
import izumi.idealingua.model.il.ast.typed.ConstValue
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class ConstValueTyperSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  private def withConsts(types: List[RawTypeDef], consts: List[RawConst]): DomainMeshLoaded = {
    val resolvedA = new DomainMeshResolved {
      override def id  = domA
      override def imports: Seq[Import] = Seq.empty
      override def members: Seq[RawTopLevelDefn] = types.map {
        case w: RawTypeDef.WithId      => RawTopLevelDefn.TLDBaseType(w)
        case n: RawTypeDef.NewType     => RawTopLevelDefn.TLDNewtype(n)
        case f: RawTypeDef.ForeignType => RawTopLevelDefn.TLDForeignType(f)
        case d: RawTypeDef.DeclaredType => RawTopLevelDefn.TLDDeclared(d)
      }
      override def referenced: Map[izumi.idealingua.model.common.DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath = FSPath.Name("a.domain")
      override def directInclusions: Seq[Inclusion] = Seq.empty
      override def meta: RawNodeMeta = ScopeBuilderSpec.meta
    }
    DomainMeshLoaded(
      id = domA, origin = FSPath.Name("a.domain"), directInclusions = Seq.empty,
      originalImports = Seq.empty, meta = meta, types = types,
      services = Seq.empty, buzzers = Seq.empty, streams = Seq.empty,
      consts = Seq(RawConstBlock(consts)), imports = Seq.empty, defn = resolvedA,
    )
  }

  private def pipeline(input: DomainMeshLoaded) =
    ConstValueTyper(
      EphemeralSynthesizer(
        StructuralFlattener(
          CycleDetector(
            AliasDealiaser(KindChecker(NameResolver(ScopeBuilder(input))))
          )
        )
      )
    )

  describe("ConstValueTyper") {
    it("type-checks a primitive int32 const") {
      val cId   = ConstId(domA, "MyInt")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CTyped(IndefiniteId(Seq.empty, "i32"), RawVal.CInt(42)), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern { case ConstValue.CInt(42) => }
      rd.diagnostics.issues.collect { case d: Diagnostic.ConstTypeMismatch => d } shouldBe empty
    }

    it("type-checks a DTO-targeted CTypedObject") {
      val dtoId = DTOId(TypePath(domA, Seq.empty), "P")
      val dto   = RawTypeDef.DTO(dtoId,
        RawStructure(Nil, Nil, Nil, List(
          RawField(IndefiniteId(Seq.empty, "str"), Some("name"), meta),
          RawField(IndefiniteId(Seq.empty, "i32"), Some("age"), meta),
        ), Nil), meta)
      val cId  = ConstId(domA, "MyP")
      val raw  = RawVal.CTypedObject(IndefiniteId(Seq.empty, "P"), Map(
        "name" -> RawVal.CString("alice"),
        "age"  -> RawVal.CInt(30),
      ))
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, raw, cMeta)
      val rd    = pipeline(withConsts(List(dto), List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern { case _: ConstValue.CTypedObject => }
      rd.diagnostics.issues.collect { case d: Diagnostic.ConstTypeMismatch => d } shouldBe empty
      rd.diagnostics.issues.collect { case d: Diagnostic.ConstFieldMissing => d } shouldBe empty
    }

    it("emits ConstTypeMismatch when a string value is assigned to an i32 target") {
      val cId   = ConstId(domA, "BadInt")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CTyped(IndefiniteId(Seq.empty, "i32"), RawVal.CString("nope")), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.diagnostics.issues.collect { case d: Diagnostic.ConstTypeMismatch => d } should have size 1
    }
  }
}
