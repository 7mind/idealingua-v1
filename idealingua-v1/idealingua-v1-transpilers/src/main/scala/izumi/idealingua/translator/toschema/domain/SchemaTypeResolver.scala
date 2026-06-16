package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.common.Primitive._
import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.model.common.{Generic, Primitive, TypeId}
import izumi.idealingua.typer.ir.Domain

/** Maps a `TypeId` to an `io.circe.Json` JSON Schema 2020-12 fragment.
  *
  * Per plan §3.1 (primitives) and §3.2 (generics). User-type references
  * resolve to a `$ref` into `#/components/schemas/<wireId>` (within the same
  * OpenAPI document); aliases are eagerly dealiased through
  * `domain.aliases`. Foreign-domain references resolve to inline-expanded
  * `$ref` strings (M1 keeps the same form — cross-domain inline expansion
  * lands properly in later milestones once cross-domain identifier handling
  * is exercised).
  */
final class SchemaTypeResolver(domain: Domain) {

  /** Schema fragment for a single `TypeId` (primitive, generic, or user-type
    * reference).
    */
  def schemaFor(id: TypeId): Json = id match {
    case p: Primitive => SchemaTypeResolver.primitiveSchema(p)
    case g: Generic   => genericSchema(g)
    case a: AliasId   => schemaFor(dealias(a))
    case _            => refToComponent(id)
  }

  /** Inline `$ref` to a component schema slot inside the same OpenAPI doc. */
  def refToComponent(id: TypeId): Json =
    Json.obj("$ref" -> Json.fromString(s"#/components/schemas/${id.wireId}"))

  /** Dealias chain: returns the first non-alias `TypeId` reachable from `a`.
    * `domain.aliases` is fully resolved post-Phase 3 so a single lookup
    * suffices, but we loop defensively in case of intermediate aliases.
    */
  private def dealias(a: AliasId): TypeId = {
    var cur: TypeId = a
    var depth       = 0
    while (cur.isInstanceOf[AliasId] && depth < 32) {
      val next = domain.aliases.getOrElse(cur.asInstanceOf[AliasId], cur)
      if (next eq cur) return cur
      cur = next
      depth += 1
    }
    cur
  }

  private def genericSchema(g: Generic): Json = g match {
    case Generic.TList(v) =>
      Json.obj(
        "type"  -> Json.fromString("array"),
        "items" -> schemaFor(v),
      )

    case Generic.TSet(v) =>
      Json.obj(
        "type"                      -> Json.fromString("array"),
        "items"                     -> schemaFor(v),
        "uniqueItems"               -> Json.True,
        "x-idealingua-iteration"    -> Json.fromString("insertion"),
      )

    // D20: TOption -> oneOf [T, null]; nullability also excludes the field
    // from the surrounding object's `required` list (SchemaDtoRenderer).
    case Generic.TOption(v) =>
      Json.obj(
        "oneOf" -> Json.arr(
          schemaFor(v),
          Json.obj("type" -> Json.fromString("null")),
        )
      )

    case Generic.TMap(k, v) =>
      val base = Json.obj(
        "type"                 -> Json.fromString("object"),
        "additionalProperties" -> schemaFor(v),
      )
      // String keys are the natural OpenAPI default; advisory annotation
      // for any non-TString key (TUUID, integer-keyed maps, enum keys).
      k match {
        case Primitive.TString => base
        case other             =>
          base.deepMerge(
            Json.obj("x-idealingua-key-type" -> Json.fromString(other.wireId))
          )
      }
  }

  /** Whether the *outermost* type of a field is `TOption[_]`. Used by the
    * DTO renderer to drop the field from `required`.
    */
  def isOptional(id: TypeId): Boolean = id match {
    case _: Generic.TOption => true
    case a: AliasId         => isOptional(dealias(a))
    case _                  => false
  }
}

object SchemaTypeResolver {

  // IEEE-754 double safe-integer ceiling (2^53 - 1); 64-bit integer bounds clamp to this range.
  final val MaxSafeInteger: Long = 9007199254740991L

  /** Schema fragment for a single primitive width. Pure — depends only on the
    * `Primitive`, never on domain state — so the bound-clamping invariant
    * (`SchemaIntegerBoundsSpec`) can be exercised exhaustively over every
    * `Primitive` without materialising a `Domain`.
    */
  private[toschema] def primitiveSchema(p: Primitive): Json = p match {
    case TBool   => Json.obj("type" -> Json.fromString("boolean"))
    case TString => Json.obj("type" -> Json.fromString("string"))

    case TInt8 =>
      intBounded(Long.box(-128L), Long.box(127L))
    case TInt16 =>
      intBounded(Long.box(-32768L), Long.box(32767L))
    case TInt32 =>
      intBounded(Long.box(-2147483648L), Long.box(2147483647L))
    // Hybrid integer-or-string (mirrors TUInt64): integer branch clamped to the
    // safe range, string branch carries the full signed-64 range.
    case TInt64 =>
      Json.obj(
        "oneOf" -> Json.arr(
          Json.obj(
            "type"    -> Json.fromString("integer"),
            "minimum" -> Json.fromLong(-MaxSafeInteger),
            "maximum" -> Json.fromLong(MaxSafeInteger),
          ),
          Json.obj(
            "type"    -> Json.fromString("string"),
            "pattern" -> Json.fromString("^-?[0-9]{1,19}$"),
          ),
        )
      )

    case TUInt8 =>
      intBounded(Long.box(0L), Long.box(255L))
    case TUInt16 =>
      intBounded(Long.box(0L), Long.box(65535L))
    case TUInt32 =>
      intBounded(Long.box(0L), Long.box(4294967295L))

    // D18: hybrid integer-or-string for values that exceed JS Number safe range.
    case TUInt64 =>
      Json.obj(
        "oneOf" -> Json.arr(
          Json.obj(
            "type"    -> Json.fromString("integer"),
            "minimum" -> Json.fromLong(0L),
            "maximum" -> Json.fromLong(MaxSafeInteger),
          ),
          Json.obj(
            "type"    -> Json.fromString("string"),
            "pattern" -> Json.fromString("^[0-9]{1,20}$"),
          ),
        )
      )

    case TFloat  => Json.obj("type" -> Json.fromString("number"))
    case TDouble => Json.obj("type" -> Json.fromString("number"))

    case TUUID =>
      Json.obj(
        "type"   -> Json.fromString("string"),
        "format" -> Json.fromString("uuid"),
      )

    // D19: base64 everywhere (post-F5 unified).
    case TBLOB =>
      Json.obj(
        "type"            -> Json.fromString("string"),
        "contentEncoding" -> Json.fromString("base64"),
      )

    case TTs =>
      Json.obj(
        "type"    -> Json.fromString("string"),
        "pattern" -> Json.fromString("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?$"),
      )

    case TTsTz =>
      Json.obj(
        "type"   -> Json.fromString("string"),
        "format" -> Json.fromString("date-time"),
      )

    case TTsU =>
      Json.obj(
        "type"    -> Json.fromString("string"),
        "pattern" -> Json.fromString("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$"),
      )

    case TTime =>
      Json.obj(
        "type"    -> Json.fromString("string"),
        "pattern" -> Json.fromString("^\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?$"),
      )

    case TDate =>
      Json.obj(
        "type"   -> Json.fromString("string"),
        "format" -> Json.fromString("date"),
      )
  }

  private def intBounded(min: java.lang.Long, max: java.lang.Long): Json =
    Json.obj(
      "type"    -> Json.fromString("integer"),
      "minimum" -> Json.fromLong(min),
      "maximum" -> Json.fromLong(max),
    )
}
