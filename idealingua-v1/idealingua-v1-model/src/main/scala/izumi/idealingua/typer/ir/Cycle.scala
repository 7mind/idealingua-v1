package izumi.idealingua.typer.ir

/** A detected cycle in the type or import graph.
  *
  * @param members     Ordered sequence of nodes that form the cycle.
  * @param terminating `true` if the cycle is broken by a non-recursive reference
  *                    (e.g. an Option or List wrapper); `false` for a hard
  *                    unresolvable cycle.
  */
final case class Cycle[T](members: List[T], terminating: Boolean)
