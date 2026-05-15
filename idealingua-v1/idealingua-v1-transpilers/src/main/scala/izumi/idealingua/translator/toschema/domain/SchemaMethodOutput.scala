package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.il.ast.typed.{AdtMember, DefMethod, SimpleStructure}
import izumi.idealingua.translator.toscala.domain.OutputWrapPolicy

/** Dispatches `DefMethod.Output` to a JSON Schema fragment for use as the
  * MCP `outputSchema` of a tool envelope.
  *
  * Per plan §4 output-variants table + M5.5 wrap-everywhere decision (F-M5-3):
  *   - `Void`           → wrapped `{"result":{"type":"null"}}`
  *   - `Singular(T)`    → schema of `T` (D9 unwrap); wrapped if `T` is non-object
  *   - `Struct(s)`      → flat object over `s.fields` (object — no wrap)
  *   - `Algebraic(alt)` → §4 ADT-style `oneOf`; wrapped (top-level isn't `object`)
  *   - `Alternative`    → `oneOf` over `Success` / `Failure`; wrapped
  *
  * MCP 2025-06-18 mandates `outputSchema.type == "object"`. Non-object dispatch
  * results are wrapped in the canonical
  * `{"type":"object","properties":{"result":<actual>},"required":["result"],
  *   "additionalProperties":false,"x-idealingua-wrapped":true}`
  * envelope so the emitted schema is strictly MCP-conformant. The bridge
  * (M5.6) honours the wrap when serialising actual responses.
  */
final class SchemaMethodOutput(resolver: SchemaTypeResolver) {

  // Mirrors `EphemeralSynthesizer` private constants (literal — wire-format).
  private val goodAltBranchName = "Success"
  private val badAltBranchName  = "Failure"

  /** MCP `outputSchema` for a method. Always returns a schema whose top-level
    * `type == "object"` (M5.5 / F-M5-3). Non-object outputs are wrapped.
    *
    * Per plan §6 D6 (MCP HTTP4s bridge): the wrap decision is delegated to
    * the shared `OutputWrapPolicy.isWrapped` predicate so the http4s bridge
    * (`DomainServiceMcpRenderer`) emits the same per-method static `wrap` flag
    * the schema describes. Behaviour-preserving: each `Output` variant lands
    * in the same wrap-or-not bucket as the prior `wrapIfNonObject(rendered)`
    * pass — see `OutputWrapPolicy` for the variant→bucket table.
    */
  def dispatch(out: DefMethod.Output): Json = {
    val raw = rawDispatch(out)
    if (OutputWrapPolicy.isWrapped(out)) wrapInResultEnvelope(raw) else raw
  }

  /** Pre-wrap dispatch — the "intrinsic" shape of the output as a schema. */
  private def rawDispatch(out: DefMethod.Output): Json = out match {
    case _: DefMethod.Output.Void =>
      Json.obj("type" -> Json.fromString("null"))

    case s: DefMethod.Output.Singular =>
      // D9: unwrap to the inner type's schema directly; advisory annotation
      // surfaces the compile-time unwrap decision to consumers.
      resolver.schemaFor(s.typeId).deepMerge(
        Json.obj("x-idealingua-unwrap" -> Json.True)
      )

    case s: DefMethod.Output.Struct =>
      structSchema(s.struct)

    case a: DefMethod.Output.Algebraic =>
      algebraicSchema(a.alternatives)

    case alt: DefMethod.Output.Alternative =>
      val successBranch = altBranch(goodAltBranchName, alt.success)
      val failureBranch = altBranch(badAltBranchName, alt.failure)
      Json.obj(
        "oneOf"             -> Json.arr(successBranch, failureBranch),
        "x-idealingua-kind" -> Json.fromString("alternative"),
      )
  }

  /** Wrap a non-object schema in the MCP-conformant
    * `{type:object, properties:{result:<schema>}, required:[result],
    *  additionalProperties:false, x-idealingua-wrapped:true}` envelope.
    *
    * Kept as a public method for callers that need to wrap an arbitrary
    * already-rendered schema (defensive layer). The dispatch path now goes
    * through `OutputWrapPolicy.isWrapped(Output)` for the IR-level decision.
    *
    * A schema counts as "object" iff its top-level `type` field is exactly
    * the string `"object"`. `oneOf` schemas, primitive `type:"null"`, and
    * anything else gets wrapped — even if every `oneOf` branch is itself
    * an object (per the strict MCP 2025-06-18 reading: the *top-level*
    * `outputSchema` must declare `type:"object"`).
    */
  def wrapIfNonObject(schema: Json): Json = {
    val isObject = schema.asObject.flatMap(_.apply("type")).flatMap(_.asString).contains("object")
    if (isObject) schema else wrapInResultEnvelope(schema)
  }

  /** Construct the canonical `{result:<schema>}` MCP envelope. */
  private def wrapInResultEnvelope(schema: Json): Json = Json.obj(
    "type"                 -> Json.fromString("object"),
    "properties"           -> Json.obj("result" -> schema),
    "required"             -> Json.arr(Json.fromString("result")),
    "additionalProperties" -> Json.False,
    "x-idealingua-wrapped" -> Json.True,
  )

  /** Flat-object schema from a `SimpleStructure` (input bag or struct output). */
  def structSchema(s: SimpleStructure): Json = {
    val propsList = s.fields.map { f =>
      f.name -> resolver.schemaFor(f.typeId)
    }
    val required = s.fields.collect {
      case f if !resolver.isOptional(f.typeId) => Json.fromString(f.name)
    }
    Json.obj(
      "$schema"              -> Json.fromString("https://json-schema.org/draft/2020-12/schema"),
      "type"                 -> Json.fromString("object"),
      "properties"           -> Json.fromFields(propsList),
      "required"             -> Json.fromValues(required),
      "additionalProperties" -> Json.False,
    )
  }

  /** ADT-style `oneOf` per plan §4 / wire-format §4. Branch discriminator is
    * `AdtMember.wireId` (= `memberName.getOrElse(typeId.name)`).
    */
  private def algebraicSchema(alternatives: List[AdtMember]): Json = {
    val branches = alternatives.map(branchSchema)
    Json.obj(
      "oneOf"             -> Json.fromValues(branches),
      "x-idealingua-kind" -> Json.fromString("adt"),
    )
  }

  private def branchSchema(member: AdtMember): Json = {
    val discriminator = member.wireId
    val ref           = resolver.schemaFor(member.typeId)
    Json.obj(
      "type"                 -> Json.fromString("object"),
      "properties"           -> Json.obj(discriminator -> ref),
      "required"             -> Json.arr(Json.fromString(discriminator)),
      "additionalProperties" -> Json.False,
    )
  }

  /** Wraps a `NonAlternativeOutput` branch as a `{ "<branchName>": <inner> }`
    * single-key wrapper, matching the §4 ADT wire shape that
    * `EphemeralSynthesizer` materialises for `Alternative` outputs.
    */
  private def altBranch(branchName: String, out: DefMethod.Output.NonAlternativeOutput): Json = {
    val inner = out match {
      case _: DefMethod.Output.Void       => Json.obj("type" -> Json.fromString("null"))
      case s: DefMethod.Output.Singular   => resolver.schemaFor(s.typeId)
      case s: DefMethod.Output.Struct     => structSchema(s.struct)
      case a: DefMethod.Output.Algebraic  => algebraicSchema(a.alternatives)
    }
    Json.obj(
      "type"                 -> Json.fromString("object"),
      "properties"           -> Json.obj(branchName -> inner),
      "required"             -> Json.arr(Json.fromString(branchName)),
      "additionalProperties" -> Json.False,
    )
  }
}
