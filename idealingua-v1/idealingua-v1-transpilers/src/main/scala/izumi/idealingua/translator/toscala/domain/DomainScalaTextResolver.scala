package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.translator.toscala.types.ScalaTypeConverter

/** Per-domain Scala type-reference resolver.
  *
  * F-TextTree M4 — mirror of `DomainTSTypeResolver` / `DomainCSTypeResolver`
  * for the Scala renderer family. Turns a `ScalaRefHandle` into the
  * rendered Scala identifier; this is what
  * `TextTree[ScalaRefHandle].mapRender(resolver.resolve)` consumes at the
  * product boundary.
  *
  * Delegation: both cases route through the existing `ScalaTypeConverter`
  * — the single source of truth for `TypeId → Scala name` lowering
  * (handles primitives, generics, user types, the Scala 2 / Scala 3
  * dialect-neutral identifier shape, and the absolute-vs-minimized
  * package selection). The `.toString` calls below produce the same
  * Scala source the legacy `q"…".toString()` and `dialect(tree).syntax`
  * paths emit for `Type.Name` / `Type.Select` / `Type.Apply` shapes; this
  * preserves byte-parity with the legacy alias emission (the package
  * object body is joined via plain `_.toString()` in
  * `DomainScalaTranslator.translate()`, not via `dialect(tree).syntax`).
  *
  * `harvestTypeIds` is the projection used by a future import-collection
  * pass: once the renderer family has settled, an upstream pass can walk
  * `tree.values.collect { case TypeRef → id }` to compute the precise
  * import set for a file. For M4, the Scala translator continues to rely
  * on `ScalaTypeConverter.toImport` per-renderer.
  */
final class DomainScalaTextResolver(conv: ScalaTypeConverter) {
  import conv._

  /** Render an in-tree type reference to its native Scala identifier.
    * Used as the argument to `TextTree[ScalaRefHandle].mapRender(_)`.
    */
  def resolve(ref: ScalaRefHandle): String = ref match {
    case ScalaRefHandle.TypeName(id)             => conv.toScala(id).typeName.toString
    case ScalaRefHandle.TypeFull(id)             => conv.toScala(id).typeFull.toString
    case ScalaRefHandle.TypeAbsolute(id)         => conv.toScala(id).typeAbsolute.toString
    case ScalaRefHandle.TypeFullWithin(pid, nm)  => conv.toScala(pid).within(nm).typeFull.toString
    case ScalaRefHandle.TermFullWithin(pid, nm)  => conv.toScala(pid).within(nm).termFull.toString
    case ScalaRefHandle.TermFull(id)             => conv.toScala(id).termFull.toString
  }

  /** Project a collected reference set into the type-id set. The harvest
    * is a *projection* of the tree — it never adds references the tree
    * does not contain.
    */
  def harvestTypeIds(refs: Iterable[ScalaRefHandle]): List[TypeId] =
    refs.collect {
      case ScalaRefHandle.TypeName(id)            => id
      case ScalaRefHandle.TypeFull(id)            => id
      case ScalaRefHandle.TypeAbsolute(id)        => id
      case ScalaRefHandle.TypeFullWithin(pid, _)  => pid
      case ScalaRefHandle.TermFullWithin(pid, _)  => pid
      case ScalaRefHandle.TermFull(id)            => id
    }.toList.distinct
}
