package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId

/** Scala type-reference witness used as the `T` in `TextTree[T]`.
  *
  * F-TextTree M4 — mirror of the TS port's `TSRefHandle` and the C# port's
  * `CSRefHandle` for the Scala renderer family. Renderers compose
  * `TextTree[ScalaRefHandle]` so that:
  *   1. type references travel through the tree as
  *      `ValueNode(ScalaRefHandle.<case>(typeId))`,
  *   2. the boundary call `tree.mapRender(resolver.resolve)` substitutes
  *      each reference with the rendered Scala identifier.
  *
  * Pattern source: idealingua-v1 TS M1.5/M2 and C# M3; baboon-compiler's
  * swift/typescript translators use the same `q"…"` + value-substitution
  * shape.
  *
  * Shape rationale: the Scala renderer family interpolates two different
  * shapes of a type reference into emitted source — the bare type name
  * (the LHS of an alias, a fresh declaration) and the fully-qualified
  * reference with type arguments (the RHS of an alias, every other
  * reference into the type graph). The two cases mirror the
  * `ScalaType.typeName` / `ScalaType.typeFull` split that the legacy
  * scala.meta-based renderers used. Resolver dispatch on the witness
  * preserves this dual shape without depending on scala.meta at the
  * envelope layer.
  *
  * The ADT is sealed and extensible so future cycles can add cases
  * (e.g. `TermFull` for term-position references when the structural /
  * service renderers migrate off scala.meta, or `ImportPath` for an
  * import-collection pass) without breaking resolver dispatch.
  */
sealed trait ScalaRefHandle

object ScalaRefHandle {

  /** Bare type-name reference — the LHS of an alias, or any declaration
    * site where the identifier appears unqualified. Resolver emits the
    * `ScalaType.typeName` shape (a single `Type.Name`).
    */
  final case class TypeName(typeId: TypeId) extends ScalaRefHandle

  /** Fully-qualified type reference — every reference into the type
    * graph at use-site. Resolver emits the `ScalaType.typeFull` shape,
    * which carries the qualified `Type.Select` chain and any type
    * arguments (`Option[Foo]`, `Map[K, V]`, etc.) collapsed by
    * `ScalaTypeConverter.toScala`.
    */
  final case class TypeFull(typeId: TypeId) extends ScalaRefHandle

  /** Root-qualified type reference — equivalent of `ScalaType.typeAbsolute`,
    * carrying the `_root_.<…>` prefix the legacy renderer used for ADT
    * branch targets (`final case class A(value: <target.typeAbsolute>)`).
    *
    * F-TextTree M5: introduced for the ADT branch shape.
    */
  final case class TypeAbsolute(typeId: TypeId) extends ScalaRefHandle

  /** Nested type reference produced via `ScalaTypeOps.within(name).typeFull`
    * — the qualified path of a synthetic name (e.g. an ADT branch case
    * class) inside its enclosing companion. Used by ADT branch converters
    * for the `from<Branch>` parameter type (`mt.typeFull`).
    *
    * F-TextTree M5: introduced for the ADT branch shape.
    */
  final case class TypeFullWithin(parentId: TypeId, name: String) extends ScalaRefHandle

  /** Term-position counterpart of `TypeFullWithin` — qualified term path
    * of a nested name (e.g. `<Adt>.<Branch>` for an ADT branch's term
    * reference). Used by ADT branch converters for the `into<Branch>`
    * constructor call site.
    *
    * F-TextTree M5: introduced for the ADT branch shape.
    */
  final case class TermFullWithin(parentId: TypeId, name: String) extends ScalaRefHandle
}
