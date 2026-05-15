package izumi.idealingua.typer.phase

import izumi.idealingua.model.common._
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawConst, RawVal}
import izumi.idealingua.model.il.ast.typed.{ConstValue, NodeMeta}
import izumi.idealingua.typer.ir._

import scala.collection.mutable

/** Phase 8 — `ConstValueTyper`.
  *
  * Walks `rd.consts: List[RawConst]` and produces a typed
  * `List[Const]` annotation on the `ResolvedDomain`, type-checking each
  * `RawVal` against the const's declared target.
  *
  * Q2 (locked): the phase performs in-phase `RawTypeRef → TypeId` resolution
  * via a small resolver that mirrors `NameResolver.resolveRef`. (The legacy
  * pipeline ran name resolution per-const at a different point; the new IR
  * keeps the lookup local here.)
  *
  * Q3 (locked): strict primitive matching.
  *   - `CInt   ↔ TInt32`
  *   - `CLong  ↔ TInt64 | TUInt64`
  *   - `CFloat ↔ TFloat | TDouble`
  *   - `CString ↔ TString | TUUID | TBLOB | time types`
  *   - `CBool  ↔ TBool`
  *
  * Diagnostics: `ConstTypeMismatch`, `ConstFieldMissing`, `ConstFieldUnknown`,
  * `BadConstValue`. Failed type-checks still emit a `Const` (with a sentinel
  * value) so the IR list count matches the source declaration count.
  *
  * Per C8/L1, never throws.
  */
object ConstValueTyper {

  def apply(rd: ResolvedDomain): ResolvedDomain = {
    val diagBuf  = mutable.ArrayBuffer.empty[Diagnostic]
    val typedBuf = mutable.ListBuffer.empty[Const]
    val resolver = new LocalResolver(rd, diagBuf)

    rd.consts.foreach {
      raw =>
        val pos       = raw.meta.position
        val (cv, _)   = typeCheckTopLevel(raw, resolver, diagBuf)
        typedBuf += Const(raw.id, cv, NodeMeta(raw.meta.doc, Seq.empty, pos))
    }

    rd.copy(
      typedConsts = typedBuf.toList,
      diagnostics = rd.diagnostics ++ Diagnostics(diagBuf.toVector),
    )
  }

  /** Returns (typedValue, ok). */
  private def typeCheckTopLevel(
    raw: RawConst,
    resolver: LocalResolver,
    diagBuf: mutable.ArrayBuffer[Diagnostic],
  ): (ConstValue, Boolean) = {
    // The const's declared target type comes from the wrapping RawVal:
    // - CTyped(target, _)        — scalar with explicit target
    // - CTypedList(target, _)    — list with explicit target
    // - CTypedObject(target, _)  — object with explicit target
    // - untyped (CInt/CString/CFloat/CBool/CLong/CList/CMap): legacy
    //   `IDLPostTyper.translateValue` accepts these and translates to the
    //   matching untyped `ConstValue`. We mirror that by emitting an
    //   untyped `ConstValue` without diagnostics (no inference of a target
    //   type is attempted — legacy does no inference either, see
    //   `IDLTyper.scala:216-253`).
    raw.const match {
      case RawVal.CTyped(t, inner) =>
        val target = resolver.resolve(t, raw.meta.position)
        typeCheckValue(raw.id.name, target, inner, raw.meta.position, resolver, diagBuf)

      case RawVal.CTypedList(t, vals) =>
        val target = resolver.resolve(t, raw.meta.position)
        target match {
          case g: Generic.TList =>
            val elemValues = vals.map(v => typeCheckValue(raw.id.name, g.valueType, v, raw.meta.position, resolver, diagBuf)._1)
            (ConstValue.CTypedList(target, ConstValue.CList(elemValues)), true)
          case other =>
            diagBuf += Diagnostic.ConstTypeMismatch(raw.id.name, other, "CTypedList", raw.meta.position)
            (sentinel(target), false)
        }

      case RawVal.CTypedObject(t, kvs) =>
        val target = resolver.resolve(t, raw.meta.position)
        target match {
          case sid: StructureId =>
            val checked = typeCheckObject(raw.id.name, sid, kvs, raw.meta.position, resolver, diagBuf)
            (ConstValue.CTypedObject(target, ConstValue.CMap(checked)), true)
          case other =>
            diagBuf += Diagnostic.ConstTypeMismatch(raw.id.name, other, "CTypedObject", raw.meta.position)
            (sentinel(target), false)
        }

      // Top-level untyped consts — legacy-compatible passthrough.
      case other =>
        (translateRawUntyped(other, resolver, raw.meta.position, diagBuf), true)
    }
  }

  private def sentinel(target: TypeId): ConstValue = ConstValue.CTyped(target, ConstValue.CString(""))

  private def typeCheckValue(
    constName: String,
    target: TypeId,
    raw: RawVal,
    pos: InputPosition,
    resolver: LocalResolver,
    diagBuf: mutable.ArrayBuffer[Diagnostic],
  ): (ConstValue, Boolean) = (target, raw) match {
    // ---- Primitive scalars ----
    case (Primitive.TInt32, RawVal.CInt(v))     => (ConstValue.CInt(v), true)
    case (Primitive.TInt64, RawVal.CLong(v))    => (ConstValue.CLong(v), true)
    case (Primitive.TUInt64, RawVal.CLong(v))   => (ConstValue.CLong(v), true)
    case (Primitive.TFloat, RawVal.CFloat(v))   => (ConstValue.CFloat(v), true)
    case (Primitive.TDouble, RawVal.CFloat(v))  => (ConstValue.CFloat(v), true)
    case (Primitive.TString, RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TUUID,   RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TBLOB,   RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TTs,     RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TTsTz,   RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TTsU,    RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TTime,   RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TDate,   RawVal.CString(v)) => (ConstValue.CString(v), true)
    case (Primitive.TBool,   RawVal.CBool(v))   => (ConstValue.CBool(v), true)

    // ---- Containers ----
    case (g: Generic.TList, RawVal.CList(vals)) =>
      val elems = vals.map(v => typeCheckValue(constName, g.valueType, v, pos, resolver, diagBuf)._1)
      (ConstValue.CList(elems), true)

    case (g: Generic.TMap, RawVal.CMap(kvs)) =>
      val entries = kvs.map { case (k, v) => k -> typeCheckValue(constName, g.valueType, v, pos, resolver, diagBuf)._1 }
      (ConstValue.CMap(entries), true)

    // Untyped object literal (CMap) against a structural target: the parser
    // emits `RawVal.CMap` for `{ field = value, ... }` literals that lack an
    // inline type annotation (e.g. elements of `lst[TestPair]` in
    // `idltest.consts`). Treat the CMap as a struct literal and type-check
    // its fields against the target structure.
    case (sid: StructureId, RawVal.CMap(kvs)) =>
      val checked = typeCheckObject(constName, sid, kvs, pos, resolver, diagBuf)
      (ConstValue.CTypedObject(sid, ConstValue.CMap(checked)), true)

    // ---- Nested CTyped — recurse ----
    case (_, RawVal.CTyped(t, inner)) =>
      val nested = resolver.resolve(t, pos)
      typeCheckValue(constName, nested, inner, pos, resolver, diagBuf)

    case (_, RawVal.CTypedList(t, vals)) =>
      val nested = resolver.resolve(t, pos)
      nested match {
        case g: Generic.TList =>
          val elems = vals.map(v => typeCheckValue(constName, g.valueType, v, pos, resolver, diagBuf)._1)
          (ConstValue.CTypedList(nested, ConstValue.CList(elems)), true)
        case _ =>
          diagBuf += Diagnostic.ConstTypeMismatch(constName, nested, "CTypedList", pos)
          (sentinel(nested), false)
      }

    case (sid: StructureId, RawVal.CTypedObject(t, kvs)) =>
      val nested = resolver.resolve(t, pos)
      val checked = typeCheckObject(constName, sid, kvs, pos, resolver, diagBuf)
      (ConstValue.CTypedObject(nested, ConstValue.CMap(checked)), true)

    case (other, badRaw) =>
      diagBuf += Diagnostic.ConstTypeMismatch(constName, other, badRaw.getClass.getSimpleName, pos)
      (sentinel(other), false)
  }

  private def typeCheckObject(
    constName: String,
    target: StructureId,
    kvs: Map[String, RawVal],
    pos: InputPosition,
    resolver: LocalResolver,
    diagBuf: mutable.ArrayBuffer[Diagnostic],
  ): Map[String, ConstValue] = {
    val flat = resolver.rd.flattenedStructs.get(target) match {
      case Some(f) => f.fields
      case None    =>
        diagBuf += Diagnostic.BadConstValue(constName, s"target ${target} has no flattened struct", pos)
        return Map.empty
    }
    val declared = flat.map(_.field.name).toSet
    declared.foreach {
      n =>
        if (!kvs.contains(n)) {
          diagBuf += Diagnostic.ConstFieldMissing(constName, target, n, pos)
        }
    }
    val out = mutable.LinkedHashMap.empty[String, ConstValue]
    flat.foreach {
      ff =>
        kvs.get(ff.field.name).foreach {
          v =>
            val (cv, _) = typeCheckValue(constName, ff.field.typeId, v, pos, resolver, diagBuf)
            out.update(ff.field.name, cv)
        }
    }
    kvs.keys.foreach {
      k =>
        if (!declared.contains(k)) {
          diagBuf += Diagnostic.ConstFieldUnknown(constName, target, k, pos)
        }
    }
    out.toMap
  }

  /** Best-effort untyped raw → ConstValue projection (sentinel path for
    * malformed top-level consts).  Mirrors `NameResolver.translateValue` but
    * resolves type-ids via the local resolver.
    */
  private def translateRawUntyped(
    raw: RawVal,
    resolver: LocalResolver,
    pos: InputPosition,
    diagBuf: mutable.ArrayBuffer[Diagnostic],
  ): ConstValue = raw match {
    case RawVal.CInt(v)    => ConstValue.CInt(v)
    case RawVal.CLong(v)   => ConstValue.CLong(v)
    case RawVal.CFloat(v)  => ConstValue.CFloat(v)
    case RawVal.CString(v) => ConstValue.CString(v)
    case RawVal.CBool(v)   => ConstValue.CBool(v)
    case RawVal.CList(vs)  => ConstValue.CList(vs.map(v => translateRawUntyped(v, resolver, pos, diagBuf)))
    case RawVal.CMap(kvs)  => ConstValue.CMap(kvs.map { case (k, v) => k -> translateRawUntyped(v, resolver, pos, diagBuf) })
    case RawVal.CTyped(t, v) =>
      val tid = resolver.resolve(t, pos)
      ConstValue.CTyped(tid, translateRawUntyped(v, resolver, pos, diagBuf))
    case RawVal.CTypedList(t, vs) =>
      val tid = resolver.resolve(t, pos)
      ConstValue.CTypedList(tid, ConstValue.CList(vs.map(v => translateRawUntyped(v, resolver, pos, diagBuf))))
    case RawVal.CTypedObject(t, kvs) =>
      val tid = resolver.resolve(t, pos)
      ConstValue.CTypedObject(tid, ConstValue.CMap(kvs.map { case (k, v) => k -> translateRawUntyped(v, resolver, pos, diagBuf) }))
  }

  /** In-phase resolver mirroring `NameResolver.resolveRef`. Treats unresolved
    * references as `Primitive.TString` (consistent with NameResolver
    * placeholder behaviour).
    */
  private final class LocalResolver(val rd: ResolvedDomain, diagBuf: mutable.ArrayBuffer[Diagnostic]) {
    private val byName: Map[String, TypeId] = rd.userTypes.keys.map(t => t.name -> t).toMap

    def resolve(ref: AbstractIndefiniteId, pos: InputPosition): TypeId = ref match {
      case g: IndefiniteGeneric =>
        if (Generic.TList.aliases.contains(g.name) && g.args.size == 1) {
          Generic.TList(resolve(g.args.head, pos))
        } else if (Generic.TSet.aliases.contains(g.name) && g.args.size == 1) {
          Generic.TSet(resolve(g.args.head, pos))
        } else if (Generic.TOption.aliases.contains(g.name) && g.args.size == 1) {
          Generic.TOption(resolve(g.args.head, pos))
        } else if (Generic.TMap.aliases.contains(g.name) && g.args.size == 2) {
          val k = resolve(g.args.head, pos)
          val v = resolve(g.args(1), pos)
          val ks: ScalarId = k match {
            case s: ScalarId => s
            case _           => Primitive.TString
          }
          Generic.TMap(ks, v)
        } else {
          diagBuf += Diagnostic.UnknownTypeRef(g.name, pos)
          Primitive.TString
        }
      case _ =>
        if (Primitive.mapping.contains(ref.name) && ref.pkg.isEmpty) {
          Primitive.mapping(ref.name)
        } else {
          byName.get(ref.name) match {
            case Some(tid) => tid
            case None =>
              diagBuf += Diagnostic.UnknownTypeRef(s"${ref.pkg.mkString(".")}.${ref.name}", pos)
              Primitive.TString
          }
        }
    }
  }
}
