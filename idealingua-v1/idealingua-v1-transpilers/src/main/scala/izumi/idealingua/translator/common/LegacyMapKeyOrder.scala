package izumi.idealingua.translator.common

/** Explicit reproduction of `scala.collection.immutable.Map[String, _]`'s
  * key iteration order, decoupled from the running stdlib version.
  *
  * The Scala 2.13 / 3.x stdlib's `Map` factory is size-specialised:
  *
  *   - size ≤ 4: `Map1` … `Map4` preserve insertion order verbatim.
  *   - size ≥ 5: `HashMap` switches to a CHAMP trie. Iteration walks
  *     each `BitmapIndexedMapNode` payload-first (slot indices ascending,
  *     based on 5-bit chunks of the improved hash), then recurses into
  *     nested nodes in slot-index order.
  *
  * Legacy `idlc` (pre-#610) populated soft-conflict scalars via
  * `all.groupBy(_.field.name)` and consumed them in `LinkedHashMap`
  * insertion order — which is exactly the `Map.keys` iteration order
  * for the full distinct-name set. Several renderers (Scala, future
  * targets) need that same ordering so the generated `def apply(...)`
  * scalar tail does not flip across versions.
  *
  * We could delegate to `Map[String, _]` directly, but the iteration
  * order is a stdlib implementation detail: a future release could
  * legitimately switch every `Map` to a hashCode sort, or change the
  * CHAMP layout, without breaking any documented contract. That would
  * silently shift our generated `def apply` parameter order on a
  * routine dependency bump. Replicating the algorithm here pins the
  * order to our code, not theirs — the only stable input is
  * `String.hashCode`, which is a documented JVM contract.
  */
object LegacyMapKeyOrder {

  /** Return `keys` in the same order Scala 2.13's `Map[String, _]`
    * factory would yield via `.keys.iterator`. Input must be already
    * distinct; callers de-duplicate first. */
  def apply(keys: List[String]): List[String] =
    if (keys.lengthCompare(4) <= 0) keys
    else champOrder(keys.map(k => k -> improve(k.hashCode)), 0)

  /** `scala.collection.immutable.HashMap.improve` (Scala 2.13.x):
    * a four-step bit mixer that distributes low-entropy `String`
    * hashes more uniformly across CHAMP trie slots. */
  private def improve(originalHash: Int): Int = {
    val a = originalHash + ~(originalHash << 9)
    val b = a ^ (a >>> 14)
    val c = b + (b << 4)
    c ^ (c >>> 10)
  }

  /** Depth-first traversal of a CHAMP `BitmapIndexedMapNode`-shaped
    * trie. At each level, the 5-bit chunk of `improved` selects a
    * slot in `[0, 32)`. Slots containing a single entry are "inline"
    * and are visited first in slot-index order; slots containing
    * multiple entries are nested nodes, visited afterwards in
    * slot-index order with a recursive walk at level+1.
    *
    * `Int` carries 32 bits → 6 full 5-bit levels plus a 2-bit
    * remainder, so any hash is exhausted after 7 levels. If two
    * keys collide on every chunk, fall back to source-list order:
    * the legacy `HashMap` would build a hash-collision list whose
    * iteration order is its construction order. */
  private def champOrder(keys: List[(String, Int)], level: Int): List[String] = {
    if (keys.lengthCompare(1) <= 0) keys.map(_._1)
    else if (level >= 7) keys.map(_._1)
    else {
      val grouped     = keys.groupBy(p => (p._2 >>> (level * 5)) & 31)
      val sortedSlots = grouped.keys.toList.sorted
      val partitions  = sortedSlots.map(s => s -> grouped(s))
      val singletons  = partitions.collect { case (_, p :: Nil) => p._1 }
      val groupsLeft  = partitions.collect { case (_, ps) if ps.lengthCompare(1) > 0 => ps }
      singletons ++ groupsLeft.flatMap(ps => champOrder(ps, level + 1))
    }
  }
}
