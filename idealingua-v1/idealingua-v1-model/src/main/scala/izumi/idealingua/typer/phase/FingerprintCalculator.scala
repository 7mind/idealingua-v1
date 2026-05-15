package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.typer.ir._
import scodec.bits.ByteVector

import scala.collection.mutable

/** Phase 9 — `FingerprintCalculator`.
  *
  * Computes per-type SHA-256 fingerprints over a canonical serialization that
  * preserves IR declaration order (C12/L3 invariant), plus a single
  * domain-wide fingerprint derived from the sorted concat of per-type
  * fingerprints (sort on serialization is OK because each per-type
  * fingerprint already encodes its own declaration order).
  *
  * Cross-domain refs serialize the imported `typeId.wireId` only (Q4 lock).
  *
  * No diagnostics — pure computation.
  */
object FingerprintCalculator {

  private val Sep: Byte = 0x1f.toByte
  private val TagDto: Byte = 1
  private val TagInterface: Byte = 2
  private val TagIdentifier: Byte = 3
  private val TagAdt: Byte = 4
  private val TagEnum: Byte = 5
  private val TagAlias: Byte = 6
  private val TagService: Byte = 7
  private val TagBuzzer: Byte = 8
  private val TagStreams: Byte = 9

  def apply(rd: ResolvedDomain): ResolvedDomain = {
    val perType = mutable.LinkedHashMap.empty[TypeId, Fingerprint]

    rd.userTypes.foreach {
      case (id, defn) =>
        perType.update(id, fingerprintOf(id, defn, rd))
    }

    val domainBytes = {
      val sorted = perType.toSeq.sortBy(_._1.wireId)
      val md = new Sha256
      md.update(rd.id.toString.getBytes("UTF-8"))
      md.update(Sep)
      sorted.foreach {
        case (id, fp) =>
          md.update(id.wireId.getBytes("UTF-8"))
          md.update(Sep)
          md.update(fp.value.toArray)
          md.update(Sep)
      }
      ByteVector(md.digest())
    }

    rd.copy(
      fingerprints      = perType.toMap,
      domainFingerprint = Fingerprint(domainBytes),
    )
  }

  private def fingerprintOf(id: TypeId, defn: TypeDef, rd: ResolvedDomain): Fingerprint = {
    val md = new Sha256

    def putStr(s: String): Unit = { md.update(s.getBytes("UTF-8")); md.update(Sep) }
    def putByte(b: Byte): Unit  = { md.update(b); md.update(Sep) }
    def putRef(t: TypeId): Unit = putStr(t.wireId)

    putStr(id.wireId)

    defn match {
      case dto: TypeDef.Dto =>
        putByte(TagDto)
        rd.flattenedStructs.get(dto.id) match {
          case Some(flat) =>
            flat.fields.foreach {
              ff =>
                putStr(ff.field.name)
                putRef(ff.field.typeId)
                putStr(ff.distance.toString)
                putRef(ff.origin)
            }
          case None =>
            dto.struct.fields.foreach { f => putStr(f.name); putRef(f.typeId) }
        }
        dto.struct.superclasses.interfaces.foreach(i => putRef(i))

      case ifc: TypeDef.Interface =>
        putByte(TagInterface)
        rd.flattenedStructs.get(ifc.id) match {
          case Some(flat) =>
            flat.fields.foreach {
              ff =>
                putStr(ff.field.name)
                putRef(ff.field.typeId)
                putStr(ff.distance.toString)
                putRef(ff.origin)
            }
          case None =>
            ifc.struct.fields.foreach { f => putStr(f.name); putRef(f.typeId) }
        }
        ifc.struct.superclasses.interfaces.foreach(i => putRef(i))

      case idn: TypeDef.Identifier =>
        putByte(TagIdentifier)
        idn.fields.foreach {
          f => putStr(f.name); putRef(f.typeId)
        }

      case adt: TypeDef.Adt =>
        putByte(TagAdt)
        adt.alternatives.foreach {
          m =>
            putRef(m.typeId)
            putStr(m.memberName.getOrElse(""))
        }

      case e: TypeDef.Enum =>
        putByte(TagEnum)
        e.members.foreach(m => putStr(m.value))

      case a: TypeDef.Alias =>
        putByte(TagAlias)
        putRef(a.target)

      case s: TypeDef.Service =>
        putByte(TagService)
        s.methods.foreach(m => putMethodSig(m, putStr, putRef))

      case b: TypeDef.Buzzer =>
        putByte(TagBuzzer)
        b.events.foreach(m => putMethodSig(m, putStr, putRef))

      case _: TypeDef.Streams =>
        putByte(TagStreams)
    }

    Fingerprint(ByteVector(md.digest()))
  }

  private def putMethodSig(
    m: izumi.idealingua.model.il.ast.typed.DefMethod,
    putStr: String => Unit,
    putRef: TypeId => Unit,
  ): Unit = m match {
    case rpc: izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod =>
      putStr(rpc.name)
      rpc.signature.input.fields.foreach(f => { putStr(f.name); putRef(f.typeId) })
      putOutput(rpc.signature.output, putStr, putRef)
  }

  private def putOutput(
    o: izumi.idealingua.model.il.ast.typed.DefMethod.Output,
    putStr: String => Unit,
    putRef: TypeId => Unit,
  ): Unit = {
    import izumi.idealingua.model.il.ast.typed.DefMethod.Output
    o match {
      case s: Output.Singular   => putStr("singular"); putRef(s.typeId)
      case _: Output.Void       => putStr("void")
      case s: Output.Struct     => putStr("struct"); s.struct.fields.foreach(f => { putStr(f.name); putRef(f.typeId) })
      case a: Output.Algebraic  => putStr("algebraic"); a.alternatives.foreach(m => putRef(m.typeId))
      case alt: Output.Alternative =>
        putStr("alternative")
        putOutput(alt.success, putStr, putRef)
        putOutput(alt.failure, putStr, putRef)
    }
  }
}
