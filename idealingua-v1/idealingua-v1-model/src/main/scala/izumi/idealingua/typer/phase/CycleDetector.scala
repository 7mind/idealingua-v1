package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.{Generic, TypeId}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.typed.{DefMethod, IdField, SimpleStructure}
import izumi.idealingua.typer.ir._

import scala.collection.mutable

/** Phase 5 — `CycleDetector`.
  *
  * Runs Tarjan's SCC algorithm over the type-reference graph and annotates
  * `ResolvedDomain.loops` with every non-trivial cycle.  Each edge is either:
  *
  *   - **direct**: a field/member references the target without indirection,
  *     OR an inheritance reference (interface `extends`, DTO mixin).  Direct
  *     edges contribute to "non-terminating" cycles.
  *   - **container-broken**: the reference passes through `Generic.TList`,
  *     `Generic.TSet`, `Generic.TOption`, or `Generic.TMap.valueType`, OR
  *     it is an ADT alternative edge (selecting one alternative of a sum
  *     is structurally an indirection — recursion is well-founded as long
  *     as at least one alternative terminates).  A cycle whose every
  *     back-edge is container-broken is "terminating".
  *
  * Diagnostics:
  *   - `CyclicInheritance` — every edge in the cycle is an inheritance edge.
  *   - `CyclicUsage` — at least one edge is a field/member reference (not
  *     inheritance), and the cycle is non-terminating.
  *   - `NonTerminatingCycle` — marker emitted alongside `CyclicUsage`
  *     (kept distinct for downstream consumers).
  *
  * Per C8/L1, this phase never throws on cyclic input.
  */
object CycleDetector {

  /** Edge kind from `from` → `to`. */
  private sealed trait EdgeKind
  private object EdgeKind {
    case object Direct      extends EdgeKind
    case object Container   extends EdgeKind
    case object Inheritance extends EdgeKind
  }

  private final case class Edge(to: TypeId, kind: EdgeKind)

  def apply(rd: ResolvedDomain): ResolvedDomain = {
    val graph: Map[TypeId, List[Edge]] = buildGraph(rd)
    val sccs = tarjan(graph)

    val loopsBuf = mutable.LinkedHashSet.empty[Cycle[TypeId]]
    val diagBuf  = mutable.ArrayBuffer.empty[Diagnostic]

    sccs.foreach {
      scc =>
        val isNonTrivial = scc.size > 1 || hasSelfLoop(scc.head, graph)
        if (isNonTrivial) {
          val sccSet     = scc.toSet
          val backEdges  = scc.flatMap(n => graph.getOrElse(n, Nil).filter(e => sccSet.contains(e.to)).map(e => (n, e)))
          val allInherit = backEdges.nonEmpty && backEdges.forall(_._2.kind == EdgeKind.Inheritance)
          // A cycle is terminating iff every cyclic walk through the SCC traverses
          // at least one Container edge. Equivalently, the subgraph induced by
          // non-Container edges within the SCC is acyclic. The earlier "every
          // back-edge is Container" check rejected legal shapes where the
          // Container edge sits only on part of the cycle — e.g.
          // `data A { b: B }; adt B = C; data C { a: opt[A] }` — well-founded
          // via the `opt[A]` container indirection on `C → A`, even though
          // `A → B` is Direct.
          val terminating = backEdges.nonEmpty && {
            val subgraph: Map[TypeId, List[Edge]] = sccSet.iterator.map { n =>
              n -> graph.getOrElse(n, Nil).filter(e => sccSet.contains(e.to) && e.kind != EdgeKind.Container)
            }.toMap
            val subSccs = tarjan(subgraph)
            subSccs.forall(s => s.size == 1 && !hasSelfLoop(s.head, subgraph))
          }

          val cycle = Cycle(scc, terminating)
          val _     = loopsBuf.add(cycle)

          if (!terminating) {
            val pos = positionOf(rd, scc.head)
            if (allInherit) {
              diagBuf += Diagnostic.CyclicInheritance(scc, pos)
            } else {
              diagBuf += Diagnostic.CyclicUsage(scc, pos)
              diagBuf += Diagnostic.NonTerminatingCycle(scc, pos)
            }
          }
        }
    }

    rd.copy(
      loops       = loopsBuf.toSet,
      diagnostics = rd.diagnostics ++ Diagnostics(diagBuf.toVector),
    )
  }

  private def hasSelfLoop(n: TypeId, graph: Map[TypeId, List[Edge]]): Boolean =
    graph.getOrElse(n, Nil).exists(_.to == n)

  private def positionOf(rd: ResolvedDomain, id: TypeId): InputPosition =
    rd.userTypes.get(id).map(_.meta.pos).getOrElse(InputPosition.Undefined)

  private def buildGraph(rd: ResolvedDomain): Map[TypeId, List[Edge]] = {
    val out = mutable.LinkedHashMap.empty[TypeId, mutable.ListBuffer[Edge]]

    def add(from: TypeId, to: TypeId, kind: EdgeKind): Unit = {
      // Only include edges whose target is also a user type in this domain.
      if (rd.userTypes.contains(to)) {
        val buf = out.getOrElseUpdate(from, mutable.ListBuffer.empty)
        buf += Edge(to, kind)
      }
    }

    def addRef(from: TypeId, tid: TypeId, baseKind: EdgeKind): Unit = tid match {
      case g: Generic.TList   => addRef(from, g.valueType, EdgeKind.Container)
      case g: Generic.TSet    => addRef(from, g.valueType, EdgeKind.Container)
      case g: Generic.TOption => addRef(from, g.valueType, EdgeKind.Container)
      case g: Generic.TMap    => addRef(from, g.valueType, EdgeKind.Container)
      case other              => add(from, other, baseKind)
    }

    def addSimpleStructure(from: TypeId, s: SimpleStructure): Unit = {
      s.fields.foreach(f => addRef(from, f.typeId, EdgeKind.Direct))
      s.concepts.foreach(c => add(from, c, EdgeKind.Inheritance))
    }

    def addOutputEdges(owner: TypeId, o: DefMethod.Output): Unit = o match {
      case s: DefMethod.Output.Struct        => addSimpleStructure(owner, s.struct)
      case s: DefMethod.Output.Singular      => addRef(owner, s.typeId, EdgeKind.Direct)
      case _: DefMethod.Output.Void          => ()
      case a: DefMethod.Output.Algebraic     => a.alternatives.foreach(m => addRef(owner, m.typeId, EdgeKind.Container))
      case alt: DefMethod.Output.Alternative =>
        addOutputEdges(owner, alt.success)
        addOutputEdges(owner, alt.failure)
    }

    def addMethodEdges(owner: TypeId, m: DefMethod): Unit = m match {
      case rpc: DefMethod.RPCMethod =>
        addSimpleStructure(owner, rpc.signature.input)
        addOutputEdges(owner, rpc.signature.output)
    }

    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        dto.struct.fields.foreach(f => addRef(dto.id, f.typeId, EdgeKind.Direct))
        dto.struct.superclasses.interfaces.foreach(i => add(dto.id, i, EdgeKind.Inheritance))
        dto.struct.superclasses.concepts.foreach(c => add(dto.id, c, EdgeKind.Inheritance))

      case ifc: TypeDef.Interface =>
        ifc.struct.fields.foreach(f => addRef(ifc.id, f.typeId, EdgeKind.Direct))
        ifc.struct.superclasses.interfaces.foreach(i => add(ifc.id, i, EdgeKind.Inheritance))
        ifc.struct.superclasses.concepts.foreach(c => add(ifc.id, c, EdgeKind.Inheritance))

      case id: TypeDef.Identifier =>
        id.fields.foreach {
          case f: IdField.PrimitiveField => addRef(id.id, f.typeId, EdgeKind.Direct)
          case f: IdField.SubId          => addRef(id.id, f.typeId, EdgeKind.Direct)
          case f: IdField.Enum           => addRef(id.id, f.typeId, EdgeKind.Direct)
        }

      case adt: TypeDef.Adt =>
        // ADT alternative edges are cycle-breaking: selecting a branch of a
        // sum is an indirection (well-founded if any alternative terminates),
        // matching the legacy CyclicUsageRule which only reports an ADT cycle
        // when *every* alternative is itself cyclic.
        adt.alternatives.foreach(m => addRef(adt.id, m.typeId, EdgeKind.Container))

      case _: TypeDef.Enum  => ()
      case _: TypeDef.Alias => ()

      case svc: TypeDef.Service =>
        svc.methods.foreach(m => addMethodEdges(svc.id, m))

      case bz: TypeDef.Buzzer =>
        bz.events.foreach(m => addMethodEdges(bz.id, m))

      case _: TypeDef.Streams => ()
    }

    out.view.mapValues(_.toList).toMap
  }

  // --- Tarjan's SCC algorithm -----------------------------------------------
  // Returns SCCs as List[List[TypeId]] in reverse topological order.
  private def tarjan(graph: Map[TypeId, List[Edge]]): List[List[TypeId]] = {
    val index   = mutable.LinkedHashMap.empty[TypeId, Int]
    val lowlink = mutable.LinkedHashMap.empty[TypeId, Int]
    val onStack = mutable.LinkedHashSet.empty[TypeId]
    val stack   = mutable.ArrayBuffer.empty[TypeId]
    var counter = 0
    val result  = mutable.ListBuffer.empty[List[TypeId]]

    def strongConnect(v: TypeId): Unit = {
      index.update(v, counter)
      lowlink.update(v, counter)
      counter += 1
      stack += v
      val _ = onStack.add(v)

      graph.getOrElse(v, Nil).foreach {
        e =>
          if (!index.contains(e.to)) {
            strongConnect(e.to)
            lowlink.update(v, math.min(lowlink(v), lowlink(e.to)))
          } else if (onStack.contains(e.to)) {
            lowlink.update(v, math.min(lowlink(v), index(e.to)))
          }
      }

      if (lowlink(v) == index(v)) {
        val comp = mutable.ListBuffer.empty[TypeId]
        var keepGoing = true
        while (keepGoing) {
          val w = stack.remove(stack.size - 1)
          val _ = onStack.remove(w)
          comp += w
          if (w == v) keepGoing = false
        }
        result += comp.toList
      }
    }

    graph.keys.foreach(v => if (!index.contains(v)) strongConnect(v))
    result.toList
  }
}
