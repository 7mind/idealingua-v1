package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.il.ast.typed.{AdtMember, DefMethod, SimpleStructure}

/** Dispatches `DefMethod.Output` to a JSON Schema fragment for use as the
  * MCP `outputSchema` of a tool envelope.
  *
  * Per plan §4 output-variants table:
  *   - `Void`           → `{"type":"null"}`
  *   - `Singular(T)`    → schema of `T` (D9 unwrap) + `x-idealingua-unwrap: true`
  *   - `Struct(s)`      → flat object over `s.fields`
  *   - `Algebraic(alt)` → §4 ADT-style `oneOf` of `{ "<discr>": <branchRef> }`
  *   - `Alternative`    → `oneOf` over `Success` / `Failure` wrappers (suffixes
  *     `goodAltBranchName`/`badAltBranchName` from `EphemeralSynthesizer`).
  *
  * For the `Alternative` case we mirror the ephemeral-ADT shape that
  * `EphemeralSynthesizer.synthesizeOutput` already produces: a 2-branch ADT
  * with discriminators `Success` / `Failure`, whose inner shape derives from
  * the corresponding `NonAlternativeOutput`. The branch reference is built
  * inline (no `$ref`) because the success/failure branches are themselves
  * non-trivial trees (e.g. `Output.Singular(list[SuccessData])` which has no
  * separate component schema once Phase 7 stopped synthesizing wrapper DTOs
  * for Builtin singular branches — see `EphemeralSynthesizer.synthesizeAltBranch`
  * notes).
  */
final class SchemaMethodOutput(resolver: SchemaTypeResolver) {

  // Mirrors `EphemeralSynthesizer` private constants (literal — wire-format).
  private val goodAltBranchName = "Success"
  private val badAltBranchName  = "Failure"

  def dispatch(out: DefMethod.Output): Json = out match {
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
