package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.{StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, Super}

/** Struct as declared on a single type, preserving source declaration order.
  *
  * `fields` and `removedFields` are `List`, not `Set` or `Map`. Declaration
  * order is part of the IR contract: Circe's `deriveEncoder` emits object keys
  * in field-declaration order, so reordering fields changes the wire format
  * (see master plan §4 "Field-ordering invariant").
  *
  * @param fields        Fields declared directly on this type, in source order.
  * @param removedFields Fields explicitly removed by this type (override /
  *                      removal declarations), in source order.
  * @param superclasses  Superclass/concept references for this type.
  */
final case class Struct(
  fields: List[Field],
  removedFields: List[Field],
  superclasses: Super,
)

/** A field appearing in a flattened struct, annotated with its origin.
  *
  * @param field    The field as declared on the originating type.
  * @param origin   The `TypeId` of the type where this field was declared.
  * @param distance Inheritance distance from the owning type (0 = own field,
  *                 1 = direct parent, etc.).
  */
final case class FlatField(
  field: Field,
  origin: TypeId,
  distance: Int,
)

/** Pre-materialized flattened struct for a concrete structure type.
  *
  * Computed once at Phase 6 (StructuralFlattener) and stored in `Domain`
  * so translators read instead of recomputing. `fields` is in resolved
  * inheritance order (ancestors first, per the legacy `sortBy(distance)` in
  * `StructuralQueriesImpl.scala:41`).
  *
  * @param ownerId       The type this flattened struct belongs to.
  * @param fields        All fields in resolved inheritance order.
  * @param conflictsHard Fields that conflict irreconcilably across the
  *                      inheritance chain (type mismatch).
  * @param conflictsSoft Fields that conflict but can be resolved by shadowing
  *                      (same name, different origin, compatible type).
  */
final case class FlatStruct(
  ownerId: StructureId,
  fields: List[FlatField],
  conflictsHard: List[FieldConflict],
  conflictsSoft: List[FieldConflict],
)

/** A conflict between two fields of the same name across an inheritance chain.
  *
  * Hard conflicts have incompatible types; soft conflicts have the same name
  * with a compatible (or identical) type and can be resolved by shadowing.
  *
  * @param name    The conflicting field name.
  * @param fields  All `FlatField` instances that share `name` in the flattened
  *                inheritance chain of the owning type.
  */
final case class FieldConflict(name: String, fields: List[FlatField])
