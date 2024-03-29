package izumi.idealingua.model.il.ast.typed

import izumi.idealingua.model.common.TypeId.{EnumId, IdentifierId, InterfaceId}
import izumi.idealingua.model.common.{PrimitiveId, TypeId}

final case class Field(typeId: TypeId, name: String, meta: NodeMeta) {
  override def toString: String = s"$name:$typeId"
}

sealed trait IdField {
  def name: String
  def typeId: TypeId
  def meta: NodeMeta
}

object IdField {
  final case class PrimitiveField(typeId: PrimitiveId, name: String, meta: NodeMeta) extends IdField

  final case class SubId(typeId: IdentifierId, name: String, meta: NodeMeta) extends IdField

  final case class Enum(typeId: EnumId, name: String, meta: NodeMeta) extends IdField
}

final case class SimpleStructure(concepts: Structures, fields: Tuple)

final case class Super(
  interfaces: Interfaces,
  concepts: Structures,
  removedConcepts: Structures,
) {
  val all: Structures = interfaces ++ concepts
}

object Super {
  def empty: Super = Super(List.empty, List.empty, List.empty)
}

final case class Structure(fields: Tuple, removedFields: Tuple, superclasses: Super)

object Structure {
  // def empty: Structure = Structure(List.empty, List.empty, Super.empty)

  def interfaces(ids: List[InterfaceId]): Structure = Structure(List.empty, List.empty, Super(ids, List.empty, List.empty))

}
