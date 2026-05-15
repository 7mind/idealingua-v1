package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteGeneric, IndefiniteId, TypePath}
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
            AliasDealiaser(KindChecker(NameResolver(scopeFor(input))))
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

    // ---- F3 sub-defect 1: top-level untyped consts -----------------------
    // Legacy `IDLPostTyper.translateValue` (`IDLTyper.scala:216-253`) accepts
    // untyped scalars/lists/maps and translates them to the matching untyped
    // `ConstValue` without inferring a target type. The new typer mirrors
    // that behaviour: no diagnostic, no synthetic `CTyped` wrapper.

    it("accepts a top-level untyped int const and translates it to ConstValue.CInt") {
      val cId   = ConstId(domA, "AnInt")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CInt(1), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern { case ConstValue.CInt(1) => }
      rd.diagnostics.issues.collect { case d: Diagnostic.BadConstValue => d } shouldBe empty
    }

    it("accepts a top-level untyped string const and translates it to ConstValue.CString") {
      val cId   = ConstId(domA, "AStr")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CString("xxx"), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern { case ConstValue.CString("xxx") => }
      rd.diagnostics.issues.collect { case d: Diagnostic.BadConstValue => d } shouldBe empty
    }

    it("accepts a top-level untyped float const and translates it to ConstValue.CFloat") {
      val cId   = ConstId(domA, "AFloat")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CFloat(1.5), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern { case ConstValue.CFloat(1.5) => }
      rd.diagnostics.issues.collect { case d: Diagnostic.BadConstValue => d } shouldBe empty
    }

    it("accepts a top-level untyped bool const and translates it to ConstValue.CBool") {
      val cId   = ConstId(domA, "ABool")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CBool(true), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern { case ConstValue.CBool(true) => }
      rd.diagnostics.issues.collect { case d: Diagnostic.BadConstValue => d } shouldBe empty
    }

    it("accepts a top-level untyped list literal and translates it to ConstValue.CList") {
      val cId   = ConstId(domA, "AList")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CList(List(RawVal.CInt(1), RawVal.CInt(2))), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern {
        case ConstValue.CList(List(ConstValue.CInt(1), ConstValue.CInt(2))) =>
      }
      rd.diagnostics.issues.collect { case d: Diagnostic.BadConstValue => d } shouldBe empty
    }

    it("accepts a top-level untyped map literal and translates it to ConstValue.CMap") {
      val cId   = ConstId(domA, "AMap")
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, RawVal.CMap(Map("k" -> RawVal.CString("v"))), cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern {
        case ConstValue.CMap(m) if m == Map("k" -> ConstValue.CString("v")) =>
      }
      rd.diagnostics.issues.collect { case d: Diagnostic.BadConstValue => d } shouldBe empty
    }

    // ---- F3 sub-defect 2: untyped object literal in lst[T] ---------------
    // The parser produces `RawVal.CMap` for `{ field = value, ... }` literals
    // that lack an inline type annotation (e.g. elements of `lst[TestPair]`
    // in `idltest.consts`). When the recursion target is a structural type,
    // the typer must treat the CMap as a struct literal and type-check its
    // fields, producing `CTypedObject`. Before the F3 fix the typer emitted
    // `ConstTypeMismatch(constName, TestPair, "CMap", ...)` instead.

    it("treats CMap as struct literal when nested inside a lst[StructureId] target") {
      val dtoId = DTOId(TypePath(domA, Seq.empty), "P")
      val dto   = RawTypeDef.DTO(dtoId,
        RawStructure(Nil, Nil, Nil, List(
          RawField(IndefiniteId(Seq.empty, "str"), Some("name"), meta),
          RawField(IndefiniteId(Seq.empty, "i32"), Some("age"), meta),
        ), Nil), meta)
      val cId  = ConstId(domA, "Ps")
      val raw  = RawVal.CTypedList(
        IndefiniteGeneric(Seq.empty, "lst", List(IndefiniteId(Seq.empty, "P"))),
        List(
          RawVal.CMap(Map("name" -> RawVal.CString("alice"), "age" -> RawVal.CInt(1))),
          RawVal.CMap(Map("name" -> RawVal.CString("bob"),   "age" -> RawVal.CInt(2))),
        ),
      )
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, raw, cMeta)
      val rd    = pipeline(withConsts(List(dto), List(const)))
      rd.typedConsts should have size 1
      rd.typedConsts.head.value should matchPattern {
        case ConstValue.CTypedList(_, ConstValue.CList(List(_: ConstValue.CTypedObject, _: ConstValue.CTypedObject))) =>
      }
      rd.diagnostics.issues.collect { case d: Diagnostic.ConstTypeMismatch => d } shouldBe empty
      rd.diagnostics.issues.collect { case d: Diagnostic.ConstFieldMissing => d } shouldBe empty
    }

    // Regression guard: type-mismatch detection still fires when literal kind
    // genuinely does not match the target (sub-defect 2 must not regress to
    // accepting anything).

    it("still rejects an int literal assigned to an str target inside a lst[str]") {
      val cId  = ConstId(domA, "BadStrs")
      val raw  = RawVal.CTypedList(
        IndefiniteGeneric(Seq.empty, "lst", List(IndefiniteId(Seq.empty, "str"))),
        List(RawVal.CInt(1)),
      )
      val cMeta = RawConstMeta(None, InputPosition.Undefined)
      val const = RawConst(cId, raw, cMeta)
      val rd    = pipeline(withConsts(Nil, List(const)))
      rd.diagnostics.issues.collect { case d: Diagnostic.ConstTypeMismatch => d } should have size 1
    }
  }
}
