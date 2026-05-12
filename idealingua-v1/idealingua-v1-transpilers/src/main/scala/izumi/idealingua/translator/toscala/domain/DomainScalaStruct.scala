package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.common.{ExtendedField, FieldDef, StructureId}
import izumi.idealingua.model.il.ast.typed.{Field, Super}
import izumi.idealingua.translator.toscala.types.{ScalaField, ScalaStruct, ScalaTypeConverter}
import izumi.idealingua.typer.ir.{FlatStruct, Struct, TypeDef => NewTypeDef}

/** Adapter that surfaces the legacy `Struct`-shape (`fields`, `superclasses`,
  * `all`, `unambigious`, `ambigious`) from the new IR's `FlatStruct`.
  *
  * IMPL-7a.2 Phase B M3: the structural renderers consume the legacy
  * `ScalaStruct` shape because `ScalaTypeConverter.ConflictsOps.toScala`
  * (and the rest of the legacy plumbing) is reused unchanged. Rather than
  * port every consumer site to a parallel shape, this adapter constructs an
  * equivalent legacy `typespace.structures.Struct` from the new IR's
  * `FlatStruct` + the originating `TypeDef.Dto/Interface.struct.superclasses`.
  *
  * This is intentionally a thin compatibility shim. M4+ may simplify by
  * introducing a parallel `ScalaStruct`-free renderer, but for M3 the legacy
  * `ScalaTypeConverter` integration provides direct structural parity for
  * the field/super-class projection.
  */
object DomainScalaStruct {

  /** Build a legacy `Struct` from the new IR's flat struct plus the source
    * `Super` declaration that lives on `TypeDef.Dto.struct`/`TypeDef.Interface.struct`.
    */
  def fromFlat(id: StructureId, flat: FlatStruct, supers: Super): izumi.idealingua.model.typespace.structures.Struct = {
    val extended: List[ExtendedField] = flat.fields.map { ff =>
      ExtendedField(
        field = ff.field,
        defn  = FieldDef(
          definedBy        = ff.origin,
          definedWithIndex = 0,
          usedBy           = id,
          distance         = ff.distance,
        ),
      )
    }

    val ambiguousNames: Set[String] =
      (flat.conflictsSoft.map(_.name) ++ flat.conflictsHard.map(_.name)).toSet

    val unambigious = extended.filterNot(f => ambiguousNames.contains(f.field.name))
    val ambigious   = extended.filter(f => ambiguousNames.contains(f.field.name))

    new izumi.idealingua.model.typespace.structures.Struct(
      id           = id,
      superclasses = supers,
      unambigious  = unambigious,
      ambigious    = ambigious,
      all          = extended,
    )
  }

  /** Convenience: build a `ScalaStruct` (rendered-form) directly from the
    * new IR flat struct + source `Super`.
    */
  def scalaStruct(
    id: StructureId,
    flat: FlatStruct,
    supers: Super,
    conv: ScalaTypeConverter,
  ): ScalaStruct = {
    val legacyStruct = fromFlat(id, flat, supers)

    def toScalaField(field: ExtendedField): ScalaField = {
      import scala.meta._
      ScalaField(
        Term.Name(field.field.name),
        conv.toScala(field.field.typeId).typeFull,
        field,
      )
    }

    val good = legacyStruct.unambigious.map(toScalaField)
    val soft = legacyStruct.ambigious.map(toScalaField)
    val all  = legacyStruct.all.map(toScalaField)
    new ScalaStruct(legacyStruct, good, soft, all)
  }

  /** Build a synthetic `FlatStruct` for an interface impl id (`<Iface>.Struct`)
    * from the interface's flat struct. New IR does not pre-materialise the
    * `defnId(impl)` flat struct because impl IDs are not first-class user
    * declarations. The fields are the same as the interface; only the
    * `usedBy`/`ownerId` differs.
    */
  def implFlatStruct(implId: izumi.idealingua.model.common.TypeId.DTOId, ifaceFlat: FlatStruct): FlatStruct = {
    FlatStruct(
      ownerId       = implId,
      fields        = ifaceFlat.fields,
      conflictsHard = ifaceFlat.conflictsHard,
      conflictsSoft = ifaceFlat.conflictsSoft,
    )
  }

  /** Extract `Super` from a `TypeDef.Dto`/`TypeDef.Interface`. */
  def superOf(td: NewTypeDef): Super = td match {
    case d: NewTypeDef.Dto       => d.struct.superclasses
    case i: NewTypeDef.Interface => i.struct.superclasses
    case _                       => Super.empty
  }

  /** Extract `Struct` (the new IR's per-type declaration struct) from
    * `TypeDef.Dto`/`TypeDef.Interface`.
    */
  def declStruct(td: NewTypeDef): Option[Struct] = td match {
    case d: NewTypeDef.Dto       => Some(d.struct)
    case i: NewTypeDef.Interface => Some(i.struct)
    case _                       => None
  }

  /** Construct an impl `DTOId` for an interface (mirrors legacy
    * `TypespaceToolsImpl.implId`: `DTOId(iface, "Struct")`).
    */
  def implId(id: InterfaceId): izumi.idealingua.model.common.TypeId.DTOId = {
    izumi.idealingua.model.common.TypeId.DTOId(id, "Struct")
  }

  /** Trivial structural Fields-from-FlatStruct projection used by the
    * Identifier renderer's parser/toString machinery.
    */
  def fieldsOf(flat: FlatStruct): List[Field] = flat.fields.map(_.field)
}
