package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{TypeId, TypePath}
import izumi.idealingua.model.il.ast.typed.{AdtMember, DefMethod, Field, NodeMeta, Super}
import izumi.idealingua.typer.ir._

import scala.collection.mutable

/** Phase 7 — `EphemeralSynthesizer`.
  *
  * Synthesizes:
  *   1. Service/Buzzer method inputs and outputs as ephemeral DTOs (and
  *      ADTs for `Output.Algebraic` / `Output.Alternative`).
  *   2. Interface mirror DTOs (`DTOId(I, "Struct")`) for every interface.
  *   3. DTO → Interface mirrors (`InterfaceId(D, "Defn")`) for every DTO.
  *
  * Q1 (locked): ephemeral ADTs are emitted as plain
  * `Member.User(TypeDef.Adt)`; DTO-shaped ephemerals retain
  * `Member.Ephemeral(EphemeralDto)` with an `EphemeralOrigin`.  Both
  * categories are recorded in `ephemeralsOf` / `ephemeralOwner`.
  *
  * Alternative-output ADTs (`X !! Y`) whose `Singular` branches are
  * `Builtin` (primitives or `Generic` containers — `list`/`set`/`map`/`opt`)
  * are auto-wrapped in a synthesized DTO `<base><Success|Failure>` carrying
  * a single `value: <branchTypeId>` field.  This keeps Phase 12
  * `AdtMembersRule` from rejecting the synthesized ADT as
  * `PrimitiveAdtMember` while staying invisible to byte-parity (the legacy
  * Scala renderer re-derives its own Typespace and ignores new-typer IR).
  * See IMPL-7a.2-F5c (T1 portion).
  *
  * Naming constants are inlined (`private val` below) — legacy
  * `TypespaceToolsImpl` is untouched and stays alive until IMPL-10.
  *
  * Diagnostics: `EphemeralNameCollision`.
  */
object EphemeralSynthesizer {

  // Naming constants (literal — wire-format invariant). Mirror of legacy
  // `TypespaceToolsImpl.scala:9-89`.
  private val methodInputSuffix  = "Input"
  private val methodOutputSuffix = "Output"
  private val goodAltSuffix      = "Success"
  private val badAltSuffix       = "Failure"
  private val goodAltBranchName  = "Success"
  private val badAltBranchName   = "Failure"
  private val interfaceMirrorSuffix = "Struct"  // DTO synthesized from Interface
  private val dtoMirrorSuffix       = "Defn"    // Interface synthesized from DTO

  def apply(rd: ResolvedDomain): ResolvedDomain = {
    val newMembers   = mutable.LinkedHashMap.empty[TypeId, Member] ++ rd.members
    val newUserTypes = mutable.LinkedHashMap.empty[TypeId, TypeDef] ++ rd.userTypes
    val ephOf        = mutable.LinkedHashMap.empty[TypeId, mutable.LinkedHashSet[TypeId]]
    val ephOwner     = mutable.LinkedHashMap.empty[TypeId, TypeId]
    val diagBuf      = mutable.ArrayBuffer.empty[Diagnostic]

    def recordOwner(owner: TypeId, eph: TypeId): Unit = {
      val set = ephOf.getOrElseUpdate(owner, mutable.LinkedHashSet.empty)
      val _   = set.add(eph)
      ephOwner.update(eph, owner)
    }

    def placeEphemeralDto(owner: TypeId, eph: EphemeralDto): Unit = {
      val id = eph.id
      if (newMembers.contains(id)) {
        diagBuf += Diagnostic.EphemeralNameCollision(id, id, izumi.idealingua.model.il.ast.InputPosition.Undefined)
      } else {
        newMembers.update(id, Member.Ephemeral(eph))
        recordOwner(owner, id)
      }
    }

    def placeEphemeralAdt(owner: TypeId, adt: TypeDef.Adt): Unit = {
      val id = adt.id
      if (newMembers.contains(id)) {
        diagBuf += Diagnostic.EphemeralNameCollision(id, id, izumi.idealingua.model.il.ast.InputPosition.Undefined)
      } else {
        newMembers.update(id, Member.User(adt))
        newUserTypes.update(id, adt)
        recordOwner(owner, id)
      }
    }

    def placeMirrorInterface(owner: DTOId, ifc: TypeDef.Interface): Unit = {
      val id = ifc.id
      if (newMembers.contains(id)) {
        diagBuf += Diagnostic.EphemeralNameCollision(id, id, izumi.idealingua.model.il.ast.InputPosition.Undefined)
      } else {
        newMembers.update(id, Member.User(ifc))
        newUserTypes.update(id, ifc)
        recordOwner(owner, id)
      }
    }

    def baseName(s: String): String = s.capitalize

    def synthesizeOutput(owner: TypeId, ownerPath: TypePath, base: String, out: DefMethod.Output): TypeId = out match {
      case s: DefMethod.Output.Singular =>
        val id = DTOId(ownerPath, s"$base$methodOutputSuffix")
        val struct = Struct(
          fields        = List(Field(s.typeId, "value", NodeMeta.empty)),
          removedFields = Nil,
          superclasses  = Super.empty,
        )
        placeEphemeralDto(owner, EphemeralDto(id, EphemeralOrigin.MethodOutput(owner, base), struct))
        id

      case s: DefMethod.Output.Struct =>
        val id = DTOId(ownerPath, s"$base$methodOutputSuffix")
        val struct = Struct(
          fields        = s.struct.fields,
          removedFields = Nil,
          superclasses  = Super(interfaces = Nil, concepts = s.struct.concepts, removedConcepts = Nil),
        )
        placeEphemeralDto(owner, EphemeralDto(id, EphemeralOrigin.MethodOutput(owner, base), struct))
        id

      case _: DefMethod.Output.Void =>
        val id = DTOId(ownerPath, s"$base$methodOutputSuffix")
        val struct = Struct(Nil, Nil, Super.empty)
        placeEphemeralDto(owner, EphemeralDto(id, EphemeralOrigin.MethodOutput(owner, base), struct))
        id

      case a: DefMethod.Output.Algebraic =>
        val id = AdtId(ownerPath, s"$base$methodOutputSuffix")
        placeEphemeralAdt(owner, TypeDef.Adt(id, a.alternatives, NodeMeta.empty))
        id

      case alt: DefMethod.Output.Alternative =>
        // Synthesize Success + Failure branches recursively, then a top-level ADT.
        val (successId, _) = synthesizeAltBranch(owner, ownerPath, base, goodAltSuffix, alt.success)
        val (failureId, _) = synthesizeAltBranch(owner, ownerPath, base, badAltSuffix, alt.failure)
        val adtId = AdtId(ownerPath, s"$base$methodOutputSuffix")
        val adt = TypeDef.Adt(
          adtId,
          alternatives = List(
            AdtMember(successId, Some(goodAltBranchName), NodeMeta.empty),
            AdtMember(failureId, Some(badAltBranchName), NodeMeta.empty),
          ),
          meta = NodeMeta.empty,
        )
        placeEphemeralAdt(owner, adt)
        adtId
    }

    def synthesizeAltBranch(owner: TypeId, ownerPath: TypePath, base: String, suffix: String, out: DefMethod.Output.NonAlternativeOutput): (TypeId, Unit) = out match {
      case s: DefMethod.Output.Singular =>
        // IMPL-7a.2-Fi1 (F-alt-output-cast-targets-nonexistent): legacy
        // `TypeCollection.toOutDef` returns the original `s.typeId` unchanged
        // for `Output.Singular(Builtin)` branches — no wrapper DTO is
        // synthesized. The resulting Adt's `AdtMember.typeId` is the Builtin
        // directly (`list[SuccessData]`, `set[ErrorData]`, …), and
        // `AdtRenderer` emits `final case class Success(value: List[SuccessData])
        // extends <Output>`. Synthesizing a wrapper DTO (the previous
        // approach) had three problems:
        //   1. The wrapper DTO leaked into `Domain.flattenedStructs` as a
        //      single-field `value: <T>` struct, which made the cast-similar
        //      peer scan emit `<OtherMethodOutput>_cast_into_<Wrapper>` casts
        //      referencing a top-level type that the renderer never actually
        //      emits (the renderer treats `Singular` alt-branches as inline
        //      `case class`es inside the Output companion, not as their own
        //      DTOs).
        //   2. It changed the on-wire encoding (two wrap levels vs one).
        //   3. The synthesized name `<Base><Success|Failure>` collided with
        //      type id resolution for nested `<Output>.Success / .Failure`.
        // The follow-up `AdtMembersRule` change exempts ephemeral
        // (synthesized) ADTs from the `PrimitiveAdtMember` check so this
        // pass-through is accepted by Phase 12.
        (s.typeId, ())
      case s: DefMethod.Output.Struct =>
        val id = DTOId(ownerPath, s"$base$suffix")
        val struct = Struct(
          fields        = s.struct.fields,
          removedFields = Nil,
          superclasses  = Super(Nil, s.struct.concepts, Nil),
        )
        placeEphemeralDto(owner, EphemeralDto(id, EphemeralOrigin.MethodOutput(owner, s"$base$suffix"), struct))
        (id, ())
      case _: DefMethod.Output.Void =>
        val id = DTOId(ownerPath, s"$base$suffix")
        placeEphemeralDto(owner, EphemeralDto(id, EphemeralOrigin.MethodOutput(owner, s"$base$suffix"), Struct(Nil, Nil, Super.empty)))
        (id, ())
      case a: DefMethod.Output.Algebraic =>
        val id = AdtId(ownerPath, s"$base$suffix")
        placeEphemeralAdt(owner, TypeDef.Adt(id, a.alternatives, NodeMeta.empty))
        (id, ())
    }

    def synthesizeInput(owner: TypeId, ownerPath: TypePath, base: String, sig: DefMethod.Signature): Unit = {
      val id = DTOId(ownerPath, s"$base$methodInputSuffix")
      val struct = Struct(
        fields        = sig.input.fields,
        removedFields = Nil,
        superclasses  = Super(Nil, sig.input.concepts, Nil),
      )
      placeEphemeralDto(owner, EphemeralDto(id, EphemeralOrigin.MethodInput(owner, base), struct))
    }

    def synthesizeMethods(owner: TypeId, ownerPath: TypePath, methods: List[DefMethod]): Unit = methods.foreach {
      case rpc: DefMethod.RPCMethod =>
        val base = baseName(rpc.name)
        synthesizeInput(owner, ownerPath, base, rpc.signature)
        val _ = synthesizeOutput(owner, ownerPath, base, rpc.signature.output)
    }

    // ---- Service / Buzzer ephemerals ----
    rd.userTypes.values.foreach {
      case svc: TypeDef.Service =>
        synthesizeMethods(svc.id, TypePath(svc.id.domain, Seq(svc.id.name)), svc.methods)
      case bz: TypeDef.Buzzer =>
        synthesizeMethods(bz.id, TypePath(bz.id.domain, Seq(bz.id.name)), bz.events)
      case _ => ()
    }

    // ---- Interface mirror DTO (Struct) — DTOId(I, "Struct") ----
    rd.userTypes.values.foreach {
      case ifc: TypeDef.Interface =>
        val mirrorId = DTOId(ifc.id, interfaceMirrorSuffix)
        val struct   = Struct(Nil, Nil, Super(interfaces = List(ifc.id), concepts = Nil, removedConcepts = Nil))
        placeEphemeralDto(ifc.id, EphemeralDto(mirrorId, EphemeralOrigin.InterfaceMirror(ifc.id), struct))
      case _ => ()
    }

    // ---- DTO → Interface mirror (Defn) — InterfaceId(D, "Defn") ----
    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        val mirrorId = InterfaceId(dto.id, dtoMirrorSuffix)
        val ifc = TypeDef.Interface(mirrorId, dto.struct, NodeMeta.empty)
        placeMirrorInterface(dto.id, ifc)
      case _ => ()
    }

    rd.copy(
      members        = newMembers.toMap,
      userTypes      = newUserTypes.toMap,
      ephemeralsOf   = ephOf.view.mapValues(_.toSet).toMap,
      ephemeralOwner = ephOwner.toMap,
      diagnostics    = rd.diagnostics ++ Diagnostics(diagBuf.toVector),
    )
  }
}
