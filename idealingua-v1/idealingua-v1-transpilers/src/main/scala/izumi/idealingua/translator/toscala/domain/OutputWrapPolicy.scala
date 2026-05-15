package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.il.ast.typed.DefMethod

/** Shared predicate: does a method's `DefMethod.Output` materialise into a
  * non-object MCP `outputSchema` (and therefore receive the
  * `{"result": <bare>}` wrap envelope at both the schema-emission side and
  * the http4s-bridge side)?
  *
  * Per plan §3.2 (MCP HTTP4s bridge plan) the wrap table is:
  *
  * {{{
  * | Variant                       | wrap  | structuredContent shape                |
  * |-------------------------------|-------|----------------------------------------|
  * | Void                          | true  | {"result": null}                        |
  * | Singular(_)                   | true  | {"result": <inner>}                     |
  * | Struct(s)                     | false | bare {...}                              |
  * | Algebraic                     | true  | {"result": {"<branch>": ...}}           |
  * | Alternative                   | true  | {"result": {"Success"|"Failure": ...}}  |
  * }}}
  *
  * Note: the schema-side `SchemaMethodOutput.wrapIfNonObject` predicate inspects
  * the *rendered* JSON Schema fragment (looks for top-level `type == "object"`).
  * This module is the corresponding IR-level predicate: a compile-time
  * decision keyed off the `Output` variant. Both predicates must agree on the
  * same set of variants (proof: §3.2 table is the same for both). The schema
  * renderer continues to invoke `wrapIfNonObject` on the rendered schema for
  * defensiveness — see `SchemaMethodOutput` for the wrap envelope construction.
  *
  * Used by:
  *   - `SchemaMethodOutput` (toschema/domain/): the wrap envelope on emitted JSON Schema.
  *   - `DomainServiceMcpRenderer` (toscala/domain/): the per-method static `wrap`
  *     flag baked into the generated bridge code.
  */
object OutputWrapPolicy {

  /** `true` iff the method's MCP `outputSchema` materialises non-object at the
    * top level — i.e. the bridge must wrap the raw IRT response in
    * `{"result": <raw>}` before placing it in `structuredContent`.
    */
  def isWrapped(out: DefMethod.Output): Boolean = out match {
    case _: DefMethod.Output.Void        => true
    case _: DefMethod.Output.Singular    => true
    case _: DefMethod.Output.Struct      => false
    case _: DefMethod.Output.Algebraic   => true
    case _: DefMethod.Output.Alternative => true
  }
}
