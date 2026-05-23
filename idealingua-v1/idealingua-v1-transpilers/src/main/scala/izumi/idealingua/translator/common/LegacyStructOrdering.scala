package izumi.idealingua.translator.common

import izumi.idealingua.model.common.{ExtendedField, StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, Super}
import izumi.idealingua.typer.ir.{Domain, Member, TypeDef => NewTypeDef}

/** Renderer-neutral helpers that reproduce the legacy
  * `StructuralQueriesImpl.structure(id)` ordering and traversal semantics
  * from the new-IR `FlatStruct`.
  *
  * Used by `toscala/`, `tocsharp/`, `totypescript/`, and `toschema/` to
  * surface case-class-equivalent property order regardless of the target
  * language. Previously each renderer carried its own copy of these
  * helpers; the only language-specific axis is duplicate-field dedup (C#
  * needs a covariant-narrowing branch, the others don't), so dedup stays
  * in each renderer.
  *
  * See `DomainScalaStruct.fromFlat` for the per-step rationale; this
  * module is the factor-out, behaviour-preserving.
  */
object LegacyStructOrdering {

  /** Apply the canonical sort matching `StructuralQueriesImpl.scala:41`:
    * `(distance ASC, definedBy.toString ASC, definedWithIndex DESC).reverse`.
    * Produces a deterministic depth-prefix-stable order distinct from the
    * BFS layer order the new `StructuralFlattener` emits.
    */
  def sortLegacyKey(fields: List[ExtendedField]): List[ExtendedField] =
    fields
      .sortBy(f => (f.defn.distance, f.defn.definedBy.toString, -f.defn.definedWithIndex))
      .reverse

  /** Replicate legacy `FieldExtractor.extractFields` DFS emission order
    * for `id` and return a positional index per `(origin, fieldName)`.
    *
    * Legacy: `extractFields(t, depth) = superFields ++ embeddedFields ++
    * thisFields` where
    *   - `superFields = struct.superclasses.interfaces.flatMap(extractFields(_, depth+1))`
    *   - `embeddedFields = struct.superclasses.concepts.flatMap(extractFields(_, depth+1))`
    *   - `thisFields = struct.fields` declared on `t` (source order).
    *
    * Used to break dedup ties for equal-type field duplicates so the new
    * IR picks the same primary as legacy `NonContradictive` returning
    * `fields.head` (F-DTO1-fieldorder). Cycles short-circuit via
    * `visited` (legacy `.distinct` collapses repeat encounters).
    */
  def legacyDfsPosition(id: StructureId, domain: Domain): Map[(TypeId, String), Int] = {
    val buf     = scala.collection.mutable.LinkedHashMap.empty[(TypeId, String), Int]
    val visited = scala.collection.mutable.LinkedHashSet.empty[TypeId]
    def fieldsOfStruct(tid: TypeId): Option[(List[Field], Super)] = {
      domain.userTypes.get(tid) match {
        case Some(d: NewTypeDef.Dto)       => Some((d.struct.fields, d.struct.superclasses))
        case Some(i: NewTypeDef.Interface) => Some((i.struct.fields, i.struct.superclasses))
        case _ =>
          domain.members.get(tid) match {
            case Some(Member.Ephemeral(eph)) => Some((eph.struct.fields, eph.struct.superclasses))
            case _                           => None
          }
      }
    }
    def walk(tid: TypeId): Unit = {
      if (!visited.add(tid)) return
      fieldsOfStruct(tid) match {
        case Some((thisFields, sup)) =>
          sup.interfaces.foreach(walk)
          sup.concepts.foreach(walk)
          thisFields.foreach { f =>
            val key = (tid, f.name)
            if (!buf.contains(key)) buf.update(key, buf.size)
          }
        case None => ()
      }
    }
    walk(id)
    buf.toMap
  }

  /** Legacy DFS-depth assignment for each field, keyed by `(origin, fieldName)`.
    *
    * Mirrors `FieldExtractor.extractFields(t, depth)` (`FieldExtractor.scala:11`):
    * the distance of a field reached at recursion depth `d` is `d` (= legacy
    * `FieldDef.distance`), and the FIRST occurrence in DFS preorder wins
    * (later visits to the same struct are pruned, mirroring legacy
    * `FieldExtractor.compositeFields` + the `.distinct` call). The walk
    * visits `interfaces` then `concepts` then `thisFields` at each level.
    *
    * Why this is necessary: `StructuralFlattener` flattens BFS, so a
    * struct reached via two paths of different length always gets the
    * SHORTER distance. Legacy `FieldExtractor` was DFS, so the same
    * struct's recorded distance was its FIRST DFS-preorder depth —
    * which depends on the declaration order of a DTO's direct supers
    * and can exceed the BFS shortest. The legacy sort key
    * `(distance, definedBy.toString, idx)` inverts depth ordering, so
    * feeding it BFS distance can place a diamond apex's fields next to
    * a shallower-distance layer than the legacy renderer would. See
    * the `idltest.diamondapply` fixture (`data Event { &WithAlpha;
    * &Contract }`, where `Contract` is the depth-asymmetry wrapper
    * around `WithBeta`) for a worked diamond shape exercised by the
    * regression suite.
    *
    * Callers (`DomainScalaStruct.fromFlat`, `DomainCSStruct.fromFlat`,
    * `DomainTSStruct.fromFlat`) override the BFS distance with the value
    * from this map before invoking `sortLegacyKey`.
    */
  def legacyDfsDistance(id: StructureId, domain: Domain): Map[(TypeId, String), Int] = {
    val buf     = scala.collection.mutable.LinkedHashMap.empty[(TypeId, String), Int]
    val visited = scala.collection.mutable.LinkedHashSet.empty[TypeId]
    def fieldsOfStruct(tid: TypeId): Option[(List[Field], Super)] = {
      domain.userTypes.get(tid) match {
        case Some(d: NewTypeDef.Dto)       => Some((d.struct.fields, d.struct.superclasses))
        case Some(i: NewTypeDef.Interface) => Some((i.struct.fields, i.struct.superclasses))
        case _ =>
          domain.members.get(tid) match {
            case Some(Member.Ephemeral(eph)) => Some((eph.struct.fields, eph.struct.superclasses))
            case _                           => None
          }
      }
    }
    def walk(tid: TypeId, depth: Int): Unit = {
      if (!visited.add(tid)) return
      fieldsOfStruct(tid) match {
        case Some((thisFields, sup)) =>
          sup.interfaces.foreach(s => walk(s, depth + 1))
          sup.concepts.foreach(s => walk(s, depth + 1))
          thisFields.foreach { f =>
            val key = (tid, f.name)
            if (!buf.contains(key)) buf.update(key, depth)
          }
        case None => ()
      }
    }
    walk(id, 0)
    buf.toMap
  }

  /** Recover legacy `definedWithIndex` for a field defined on `origin`:
    * the index of the field within the originating type's declared
    * `Struct.fields` list. Mirrors `FieldExtractor.toExtendedFields`
    * (`FieldExtractor.scala:47-52` — `fields.zipWithIndex`).
    *
    * Lookup order: user types first (DTO/Interface), then ephemerals
    * (`Member.Ephemeral`) — both can be field origins after F8.
    * Identifier types aren't `StructureId`s and never appear as an origin.
    *
    * Returns `None` when the origin can't be located in the domain (test
    * stubs with empty `userTypes`/`members`); callers fall back to the
    * field's position within the contiguous run of same-origin entries.
    */
  def originIndex(domain: Domain, origin: TypeId, fieldName: String): Option[Int] = {
    def indexIn(fields: List[Field]): Option[Int] = {
      val idx = fields.indexWhere(_.name == fieldName)
      if (idx < 0) None else Some(idx)
    }
    domain.userTypes.get(origin) match {
      case Some(d: NewTypeDef.Dto)       => indexIn(d.struct.fields)
      case Some(i: NewTypeDef.Interface) => indexIn(i.struct.fields)
      case _ =>
        domain.members.get(origin) match {
          case Some(Member.Ephemeral(eph)) => indexIn(eph.struct.fields)
          case _                           => None
        }
    }
  }
}
