package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, FamilyIndex}

/** Phase 0 — cross-domain index builder.
  *
  * Given the root `DomainMeshLoaded` (which carries transitive referenced meshes
  * in `defn.referenced`), walks the import graph, builds the `FamilyIndex`, and
  * detects import cycles.
  *
  * Per C8/L1: cycles are emitted as `CyclicDomainImport` diagnostics, never
  * thrown.  Per C11/L2: cycle detection runs before any per-domain typing.
  * `FamilyIndex.loadOrder` always returns a complete, deterministic ordering
  * (cycling SCCs sorted by `DomainId.toString`).
  */
object IdealinguaFamilyManager {

  /** Build the cross-domain `FamilyIndex` from a root `DomainMeshLoaded`.
    *
    * Walks `root.defn.referenced` transitively via BFS, then detects cycles
    * using DFS-coloring (white/gray/black), computes topological order, and
    * returns the fully-populated index.
    */
  def apply(root: DomainMeshLoaded): FamilyIndex = {
    // ---- 1. BFS: collect all domains + direct import edges ------------------

    val domainMap   = scala.collection.mutable.LinkedHashMap.empty[DomainId, DomainMeshLoaded]
    val importGraph = scala.collection.mutable.LinkedHashMap.empty[DomainId, Set[DomainId]]
    val queue       = scala.collection.mutable.Queue.empty[DomainMeshLoaded]

    domainMap.update(root.id, root)
    queue.enqueue(root)

    while (queue.nonEmpty) {
      val current = queue.dequeue()
      val directImports = current.defn.referenced.keys.toSet
      importGraph.update(current.id, directImports)

      current.defn.referenced.foreach {
        case (id, mesh) =>
          if (!domainMap.contains(id)) {
            // Create a DomainMeshLoaded stub from the resolved mesh.
            // The resolved mesh may not be a full DomainMeshLoaded (it is a
            // DomainMeshResolved), so we synthesise the minimal wrapper needed
            // for FamilyIndex consumers (ScopeBuilder) that look up imports via
            // family.domains(importedDomainId).  We only need the fields that
            // ScopeBuilder actually reads: `id`, `defn`, `imports`, `types`.
            val stub = DomainMeshLoaded(
              id               = mesh.id,
              origin           = mesh.origin,
              directInclusions = mesh.directInclusions,
              originalImports  = mesh.imports,
              meta             = mesh.meta,
              types            = mesh.members.iterator.collect {
                case d: izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn.TLDBaseType  => d.v
                case d: izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn.TLDNewtype   => d.v
                case d: izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn.TLDForeignType => d.v
              }.toSeq,
              services         = mesh.members.iterator.collect {
                case d: izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn.TLDService => d.v
              }.toSeq,
              buzzers          = mesh.members.iterator.collect {
                case d: izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn.TLDBuzzer => d.v
              }.toSeq,
              streams          = mesh.members.iterator.collect {
                case d: izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn.TLDStreams => d.v
              }.toSeq,
              consts           = Seq.empty,
              imports          = mesh.imports.flatMap {
                imp => imp.identifiers.map(iid => izumi.idealingua.model.il.ast.raw.domains.SingleImport(imp.id, iid))
              }.toSeq,
              defn             = mesh,
            )
            domainMap.update(id, stub)
            queue.enqueue(stub)
          }
      }
    }

    // Ensure all graph nodes appear in importGraph (leaf domains have no imports).
    domainMap.keys.foreach { id =>
      importGraph.getOrElseUpdate(id, Set.empty)
    }

    // ---- 2. DFS-coloring cycle detection + topological order ----------------

    // Colors: 0 = white (unvisited), 1 = gray (in stack), 2 = black (done).
    val color    = scala.collection.mutable.HashMap.empty[DomainId, Int].withDefaultValue(0)
    val topoList = scala.collection.mutable.ArrayBuffer.empty[DomainId]
    val diagBuf  = scala.collection.mutable.ArrayBuffer.empty[Diagnostic]

    // Iterative DFS (avoid stack overflow on large graphs).
    // Stack entries: (node, iterator-over-successors, enteredGray).
    def dfs(start: DomainId): Unit = {
      // Stack: (node, successors-iterator, alreadyGrayed)
      val stack = scala.collection.mutable.ArrayStack.empty[(DomainId, Iterator[DomainId], Boolean)]

      if (color(start) != 0) return

      color.update(start, 1)
      stack.push((start, importGraph.getOrElse(start, Set.empty).toList.sorted(
        Ordering.by[DomainId, String](_.toString)
      ).iterator, true))

      while (stack.nonEmpty) {
        val (node, successors, _) = stack.top
        if (successors.hasNext) {
          val next = successors.next()
          color(next) match {
            case 0 => // white: push
              color.update(next, 1)
              stack.push((next, importGraph.getOrElse(next, Set.empty).toList.sorted(
                Ordering.by[DomainId, String](_.toString)
              ).iterator, true))
            case 1 => // gray: back-edge → cycle
              // Collect the cycle nodes from the stack (all gray nodes up to `next`).
              val cycleNodes = stack.iterator.map(_._1).takeWhile(_ != next).toList :+ next
              diagBuf += Diagnostic.CyclicDomainImport(
                (node :: cycleNodes.reverse).reverse,
                InputPosition.Undefined,
              )
            case _ => // black: already done, skip
          }
        } else {
          // All successors done: pop and mark black, add to topo.
          stack.pop()
          color.update(node, 2)
          topoList += node
        }
      }
    }

    // Visit all nodes in a deterministic order (sorted by DomainId.toString).
    domainMap.keys.toList.sortBy(_.toString).foreach { id =>
      if (color(id) == 0) dfs(id)
    }

    FamilyIndex(
      domains     = domainMap.toMap,
      importGraph = importGraph.toMap,
      loadOrder   = topoList.toList,
      diagnostics = Diagnostics(diagBuf.toVector),
    )
  }
}
