# Wire Format Reference (idealingua-v1)

Authoritative reference for the JSON wire format produced by the
idealingua-v1 transpilers (Scala/Circe, TypeScript/IRT, C#/Newtonsoft).
Each invariant cites the source file:line that authors the rule today.

This document describes ACTUAL emitter behavior, not aspirational. Where
the three back-ends diverge, the divergence is documented in
"Per-language divergences" with a pointer to the follow-up that may
reconcile it. Applications MUST NOT rely on cross-language byte-identity
beyond what this document promises.

Frozen baseline: git tag `wire-format-baseline-2026-05-03`. Any change
to a rule below requires a coordinated wire-format break across all
back-ends, a new freeze tag, and migration guidance for downstream
consumers (per §"How to extend the spec").

## Conventions

- UTF-8 throughout; no byte-order mark.
- Compact JSON: no insignificant whitespace, no trailing newline. The
  Layer B harness compares with `Json.noSpaces` on the Scala side and
  `JSON.stringify(value)` (no replacer, no indent) on the TypeScript
  side; the C# driver emits with `Formatting.None`. Pretty-printed
  output is for debugging only and does not round-trip byte-strict.
- Keys and string values are JSON strings (RFC 8259).
- Numbers are JSON numbers, except where this document explicitly
  states a JSON-string carrier (e.g. the future `TUInt64 ≥ 2^53` policy
  in §"Builtin scalars").

## §1. Envelope (`RpcPacket`)

The transport envelope is `RpcPacket`, defined in
`idealingua-v1/idealingua-v1-runtime-rpc-scala/src/main/scala/izumi/idealingua/runtime/rpc/packets.scala:86-94`:

```scala
case class RpcPacket(
  kind: RPCPacketKind,
  data: Option[Json],
  id: Option[RpcPacketId],
  ref: Option[RpcPacketId],
  service: Option[String],
  method: Option[String],
  headers: Option[Map[String, String]],
)
```

Fields are emitted in declaration order via Circe's `deriveEncoder` at
`packets.scala:111`. Optional fields use `Encoder[Option]` defaults:
on `Some(v)` the value is emitted; on `None` Circe emits `"key":null`
(NOT key-omission — see "Per-language divergences"). The generic
`data: Option[Json]` carries an arbitrary JSON sub-tree; its content
is shaped by the user-defined service/method I/O DTOs encoded per §3
or §8.

`RpcPacketId(v: String)` codecs at `packets.scala:82-83` map to a
plain JSON string.

### `RPCPacketKind`

A closed enumeration. Each case object overrides `toString` to a
literal kebab-style identifier. The Circe codec at
`packets.scala:14-15` uses `Encoder.encodeString.contramap(_.toString)`
and `Decoder.decodeString.map(RPCPacketKind.parse)`, so the wire form
is exactly the `toString` value:

| Case object   | Wire string         | Source                         |
|---------------|---------------------|--------------------------------|
| `Fail`        | `"?:failure"`       | `packets.scala:37-39`          |
| `RpcRequest`  | `"rpc:request"`     | `packets.scala:41-43`          |
| `RpcResponse` | `"rpc:response"`    | `packets.scala:45-47`          |
| `RpcFail`     | `"rpc:failure"`     | `packets.scala:49-51`          |
| `BuzzRequest` | `"buzzer:request"`  | `packets.scala:53-55`          |
| `BuzzResponse`| `"buzzer:response"` | `packets.scala:57-59`          |
| `BuzzFailure` | `"buzzer:failure"`  | `packets.scala:61-63`          |
| `S2CStream`   | `"stream:s2c"`      | `packets.scala:65-67`          |
| `C2SStream`   | `"stream:c2s"`      | `packets.scala:69-71`          |

`stream:*` kinds are deprecated-but-supported (per cross-cutting
decision C5/Q1 in `tasks.md`). New consumers SHOULD NOT generate
streams; existing producers MUST continue to emit the literal strings
above without change.

`Buzzer*` kinds are first-class and used in production (per C5/Q1).

## §2. `wireId` formula

Every user-defined type is identified on the wire by its `wireId`.
The formula is locked permanently (per cross-cutting decision C4/Q12
in `tasks.md`):

```
wireId = "<package>.<name>"
```

Defined at `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/common/TypeId.scala:31-33`:

```scala
def wireId: String = {
  s"${path.toPackage.mkString(".")}.$name"
}
```

`path.toPackage` is the dot-joined IDL package path (which combines
the domain id and any nested type-path segments). `name` is the
short type name as declared in IDL.

Examples (drawn from the corpus under
`idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/`):

- `idltest.dtofields.Point` — DTO `Point` in package `idltest.dtofields`.
- `izumi.test.domain01.AllTypes.Struct` — DTO `Struct` nested under
  interface `AllTypes` in package `izumi.test.domain01`.
- `idltest.algebraics.AdtTester` — ADT `AdtTester` in package
  `idltest.algebraics`.
- `idltest.identifiers.UserId` — Identifier `UserId` in package
  `idltest.identifiers`.

**Stability.** No type may change package or short name without a
coordinated wire-format break across all consumers. The lock is
absolute: a rename is a wire-format-break.

`wireId` is the discriminator key in §4 (ADT / interface encoding) and
is used by the Layer B / Layer C harness to dispatch fixtures.

## §3. Struct (DTO / Identifier-fields) encoding

Used for: `DTO` types, the inner `Struct` of an `Interface` (the
`implId(I) = DTOId(I, "Struct")` ephemeral, see §7), and method-I/O
ephemeral DTOs.

**Shape.** A JSON object whose keys are the IDL field names (with the
synthetic-name rule below for unnamed Identifier fields, §6) and whose
values are the per-field encoding (per-type rules in §9 / §10).

**Field order.** Declaration order from the IDL source file,
transitively flattened through inheritance. The flattening is
performed by `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/StructuralQueriesImpl.scala`
(`structure(id)` method); the Scala emitter reads the flattened
structure at
`idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/extensions/CirceTranslatorExtensionBase.scala:187`
(`val struct = ctx.typespace.structure.structure(id)`).

The legacy IR preserves declaration order through the typer (locked as
cross-cutting decision C12/L3 in `tasks.md`). The Scala emitter then
relies on Circe's `deriveEncoder`, which emits keys in case-class
declaration order.

**Per-language emitter sites.**

- **Scala / Circe.** Wrapped (default) form:
  `CirceTranslatorExtensionBase.scala:281-283` (the `deriveEncoder` /
  `deriveDecoder` block). Unwrapped form (singular method output, see §8):
  `CirceTranslatorExtensionBase.scala:225-241` (encoder dispatch),
  `:250-252` (decoder).
- **C# / Newtonsoft.** Hand-rolled `JsonConverter`. Per-DTO:
  `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/tocsharp/extensions/JsonNetExtension.scala:99-126`
  (the `_JsonNetConverter` template; `WriteJson` uses
  `writer.WriteStartObject` + per-field `writeProperty` in declaration
  order; `ReadJson` re-instantiates via `new $name(...)` with
  positional construction).
- **TypeScript / IRT.** Per-DTO `serialize()` template at
  `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/totypescript/TypeScriptTranslator.scala:205-209`
  (inside `renderDto`); the surrounding constructor / deserialize
  block at `:190-201`; field-emit helpers `renderSerializedObject` /
  `renderDeserializeObject` at `:515-523`.

**Empty struct.** A DTO with no fields encodes as `{}` (an empty JSON
object). All three back-ends agree.

**Inheritance.** A DTO that implements an interface flattens the
interface's fields into the DTO's own field set, preserving the
declaration order documented above. There is no nesting and no
discriminator at the struct level — the wire form is a flat JSON
object. Discrimination happens one level up, at the ADT / interface
level (§4).

## §4. ADT and interface encoding

Both ADTs (`adt`) and interfaces (`interface`) serialize as a single
JSON object whose key is a discriminator and whose value is the
encoded branch / implementor. The shape is identical:

```json
{ "<discriminator>": { /* branch or implementor fields */ } }
```

**Discriminator.**

- ADT branches: the **short member name** of the branch, NOT the
  full `wireId`. Concretely, for `adt AdtTester { Point | OptId | ... }`
  the discriminator for the `Point` branch is `"Point"`.
  *Empirical correction to the master plan §4 sketch*: the master plan
  said "`<wireId>`" universally; the actual emitter uses
  `c.wireId` where `c` is the `AdtMember`'s effective name (see
  `CirceTranslatorExtensionBase.scala:51` — the lit string is
  `c.wireId` for the AdtMember, which after `idNameFix`/`memberName`
  reduces to the short name). PR-03.3a/03.3b empirical fixtures (TS
  + C# legs) confirmed this against round-trip byte equality.
- Interface implementors: the **full `wireId`** of the concrete DTO
  implementor. For `interface AllTypes { ... }` with implementor DTO
  `AllTypes.Struct` (the synthetic `Struct` ephemeral, §7), the
  discriminator is `"izumi.test.domain01.AllTypes.Struct"`.

**Combined shape — ADT-with-interface branch.** When an ADT branch IS
an interface, the wire form nests: SHORT branch name keying FULL
implementor wireId keying the implementor's struct fields:

```json
{ "AFace": { "idltest.algebraics.SomeImpl": { /* SomeImpl fields */ } } }
```

Empirical example: see PR-03.3a notes in `tasks.md` (`AdtWithInterface`
fixture) and the TS dispatch implementation in
`idealingua-v1/idealingua-v1-test-harness/src/main/typescript/Dispatch.ts`.

**Decoder behavior.** All three back-ends pull the first (and only
expected) key from the JSON object, dispatch on it, then decode the
nested value as the corresponding branch / implementor struct.
Multi-key objects at the discriminator level are an error: the Scala
and C# decoders read `keys.head` / `Properties().First()`; behavior
on a non-matching key is a `DecodingFailure` (Scala) /
`System.Exception` (C#) / `Error` (TS).

**Per-language emitter sites.**

- **Scala ADT** encode/decode:
  `CirceTranslatorExtensionBase.scala:49-58` (the `enc` and `dec`
  case-arms; full block runs through `:65` with the missing-case
  handler at `:60-65`).
- **Scala interface** encode/decode:
  `CirceTranslatorExtensionBase.scala:112-121` (the `enc` and `dec`
  case-arms; full block runs through `:128`).
- **C# ADT**:
  `JsonNetExtension.scala:432-471` (the `_JsonNetConverter` template
  for `Adt`; `WriteJson` at `:441-453`, `ReadJson` at `:456-470`).
- **TS ADT**: `TypeScriptTranslator.scala:352-437` (the
  `renderAdtImpl` helper; `serialize` at `:375-409`, `deserialize` at
  `:411-435`).
  *Empirical correction to the master plan §4 sketch*: the plan cited
  `:404-434`; the actual span of the helper is `:352-437`. The
  `serialize`/`deserialize` static methods inside live near
  `:375-435`.

## §5. Enum encoding

Enums serialize as a JSON string whose value equals the enum member's
IDL name (case preserved).

```json
"GREEN"
```

**Per-language emitter sites.**

- **Scala**: `CirceTranslatorExtensionBase.scala:99-104`
  (`handleEnum` calls `withParseable`; `withParseable` produces
  `Encoder.encodeString.contramap(_.toString)` at `:173`).
- **C#**: `JsonNetExtension.scala:46-69` (the `_JsonNetConverter`
  template; `WriteJson` at `:51-61`, with `writer.WriteValue
  (value.ToString())` at `:60`).
- **TypeScript**: a TypeScript `enum` declaration with each member's
  RHS equal to the literal IDL name. Per-member format string
  `s"$m = '$m'"` at `TypeScriptTranslator.scala:443`; the
  `export enum ${i.id.name} {` declaration line at `:447`.

## §6. Identifier encoding

`identifier` types serialize as a JSON string with a formatted body
combining the field values:

```
"<TypeName>#<encodedField1>:<encodedField2>:..."
```

- The literal `<TypeName>` is the short Identifier type name.
- Field separator is `:` (literal colon).
- Each field value is `encodeURIComponent`-style URL-encoded so the
  separator never collides with field-value content.
- Field order in the body is **alphabetical by field name**, not
  declaration order. See `TypeScriptTranslator.scala:458`
  (`val sortedFields = fields.all.sortBy(_.field.name)`); Scala and C#
  match this rule via the same `idNameFix`-derived field set sorted
  alphabetically.

**Field-name source.** Identifiers may have unnamed fields. The
`idNameFix` rule at
`idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/il/ast/IDLTyper.scala:199-214`
fills in synthetic names:

```scala
private def idNameFix(f: RawField, fieldsCount: Int) = {
  f.name match {
    case Some(value) =>
      value
    case None if fieldsCount == 1 =>
      "value"
    case None =>
      // un-capitalized type name, with leading "#" stripped
      ...
  }
}
```

In short:
- A named field uses the explicit name.
- A single unnamed field uses the literal `"value"`.
- Multiple unnamed fields use the un-capitalized type name of each
  field's type (with any leading builtin `#` marker stripped).

**Per-language emitter sites.**

- **Scala** Identifier codec:
  `Encoder.encodeString.contramap(_.toString)` at
  `CirceTranslatorExtensionBase.scala:173` (via `withParseable`,
  invoked from `handleIdentifier` at `:27-32`).
- **C#**: `JsonNetExtension.scala:21-37` (the `_JsonNetConverter`
  template; `WriteJson` at `:29-31`, `ReadJson` at `:34-36` — both
  use `value.ToString()` and `From((string)reader.Value)`).
- **TS**: `serialize()` / `toString()` template at
  `TypeScriptTranslator.scala:500-508` (inside `renderIdentifier`);
  the string-form parser at `:484-491`.

## §7. Ephemeral DTO naming

The compiler synthesizes DTO types for several IDL constructs. The
ephemeral names use these suffixes from
`idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/typespace/TypespaceToolsImpl.scala:9-16`:

| Constant                | Value     | Used for                                          |
|-------------------------|-----------|---------------------------------------------------|
| `methodInputSuffix`     | `Input`   | RPC / buzzer method input ephemeral DTO           |
| `methodOutputSuffix`    | `Output`  | RPC / buzzer method output ephemeral DTO          |
| `goodAltSuffix`         | `Success` | Either-output success branch DTO type             |
| `badAltSuffix`          | `Failure` | Either-output failure branch DTO type             |
| `goodAltBranchName`     | `Success` | Discriminator key for either-output success       |
| `badAltBranchName`      | `Failure` | Discriminator key for either-output failure       |

**Interface → DTO mirror (`implId`).** Every interface `I` has an
ephemeral concrete struct `I.Struct` (the "Struct" in the IDL means
"the canonical implementor") with the constant name `Struct`:

`TypespaceToolsImpl.scala:20-22`:
```scala
override def implId(id: InterfaceId): DTOId = {
  DTOId(id, toDtoName(id))
}
```

`TypespaceToolsImpl.scala:71-72`:
```scala
case _: InterfaceId =>
  "Struct"
```

So an interface `AllTypes` in package `izumi.test.domain01` has an
ephemeral `AllTypes.Struct` DTO with `wireId =
"izumi.test.domain01.AllTypes.Struct"`. The interface itself (rather
than its `Struct`) is the discriminated-encoded type per §4 when used
polymorphically; the bare struct is encoded per §3 when constructed
directly.

**Concrete examples.** From the corpus + PR-03.2/03.3a/03.3b
fixtures (see `tasks.md` Completed entries for `PR-03.2`, `PR-03.3a`,
`PR-03.3b`):

- Service method input: `idltest.services.TestService.SimpleInput`
  (method `simple` ⇒ `Simple` + `Input`).
- Service method singular output:
  `idltest.services.TestService.GreetSingularOutOutput`.
- Buzzer method input:
  `idltest.events.TestBuzzer.EnumInputInput` (method `enumInput`
  ⇒ `EnumInput` + `Input`; the doubled `Input` reflects the actual
  generated class name).

## §8. Singular "unwrap" mode for method outputs

A method whose output is `DefMethod.Output.Singular` (i.e. exactly one
output value, not a struct of named fields) encodes the single value
directly as the body of `RpcPacket.data`, without the synthetic
`Output` wrapper object.

**Authoring site (Scala).**

- The `unwrap` decision: `CirceTranslatorExtensionBase.scala:191-202`.
- The unwrap encoder/decoder branch:
  `CirceTranslatorExtensionBase.scala:205-255`. Inside that branch,
  `isObjectEncoder` at `:210-223` decides whether the inner value
  encodes as a JSON object (then `Encoder.AsObject` is used) or as a
  general JSON value (then `Encoder` is used). The branch consumes
  the single field at `:206`:
  ```scala
  val singleField = struct.all.head.field
  ```

C# and TypeScript back-ends MUST agree with this dispatch — the same
method's output on the wire is a bare value, not a single-key object.

**Examples.**

- `Singular[i32]` output of value `42` ⇒ `"data": 42` (not
  `"data": {"value": 42}`).
- `Singular[Point]` output of `Point(1, 2)` ⇒
  `"data": {"x": 1, "y": 2}` (the unwrapped struct).

The unwrap decision is taken at compile time per method based on the
method's IR-level `Output.Singular` shape; consumers MUST NOT attempt
to detect "single-field struct" heuristics at runtime.

## §9. Builtin scalars

Authoritative scalar enumeration in
`idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/common/TypeId.scala:106-238`.

### `TBool`

JSON boolean (`true` / `false`). Per-language: native boolean.

### `TString`

JSON string. UTF-16 text (Unicode code points outside BMP are
surrogate-paired per JSON / RFC 8259); no encoding transformation.

### Signed integers `TInt8` / `TInt16` / `TInt32` / `TInt64`

JSON number. Range constraints honoured per back-end native type:

| IDL type  | Scala  | TypeScript    | C#      |
|-----------|--------|---------------|---------|
| `TInt8`   | `Byte` | `number`      | `sbyte` |
| `TInt16`  | `Short`| `number`      | `short` |
| `TInt32`  | `Int`  | `number`      | `int`   |
| `TInt64`  | `Long` | `number` *    | `long`  |

\* TypeScript `number` is IEEE-754 double; magnitudes ≥ 2^53 lose
precision. See "Per-language divergences — Int64 boundary".

Scala native types from `ScalaTypeConverter.scala:109-116`.

### Unsigned integers `TUInt8` / `TUInt16` / `TUInt32` / `TUInt64`

JSON number on the wire. **Scala storage is signed-wrapped** because
`ScalaTypeConverter.scala:109-116` maps `TUInt8 → Byte`,
`TUInt16 → Short`, `TUInt32 → Int`, `TUInt64 → Long` (the same Scala
primitive as the corresponding signed type). Concretely,
`TUInt8 = 200` round-trips as Scala `Byte = -56` (two's-complement
wrap); the wire bytes are the signed value `-56`. PR-03.2 fixtures
encode this signed-wrap form (see `tasks.md` PR-03.2 notes).

This is a Scala-side internal representation choice that LEAKS to the
wire. PR-02 may converge this; until then, see "Per-language
divergences — Unsigned ints".

`TUInt64 ≥ 2^53` policy (Q4 in `tasks.md`): planned hybrid encoding
(JSON number ≤ 2^53 - 1, JSON string ≥ 2^53 to preserve precision in
JS hosts). The hybrid is NOT yet implemented in any of the three
emitters; the current behavior is to emit a raw JSON number, which
loses precision at the TypeScript end. Documented as a known
divergence.

### `TFloat` / `TDouble`

JSON number. NaN / Infinity policy:
- **Scala / Circe**: emits `null` for `Double.NaN`, `+Infinity`,
  `-Infinity` by default (Circe `Encoder.encodeDouble` behavior).
- **TypeScript / `JSON.stringify`**: emits `null` for `NaN`,
  `Infinity`, `-Infinity`.
- **C# / Newtonsoft**: throws by default on non-finite values unless
  `FloatFormatHandling` is configured. The default driver settings in
  `JsonNetMarshaller.cs` do not configure `FloatFormatHandling`;
  consumers writing non-finite floats will see an exception.

Applications SHOULD NOT rely on `null`-as-NaN round-trip. The IDL
should prefer wrapping non-finite values in a sentinel `option` or
explicit ADT.

`TFloat` zero-form divergence: see "Per-language divergences — Float
zero".

### `TUUID`

JSON string in canonical 8-4-4-4-12 lowercase-hex form (per
RFC 4122 §3 string representation):

```
"550e8400-e29b-41d4-a716-446655440000"
```

Per-language sources:
- Scala: `ScalaTypeConverter.scala:124-125` (`JavaType.get[UUID]`);
  Circe's default `Encoder[UUID]` produces canonical form.
- TS: see `TypeScriptTypeConverter.scala:28` and `:59` — TS treats
  UUID as a transparent string passthrough.
- C#: `JsonNetExtension.scala:189` — `writer.WriteValue($src.ToString())`
  for `Guid.ToString()` default form (canonical 8-4-4-4-12).

### `TBLOB` (KNOWN-DIVERGENCE — see F5 in `tasks.md`)

The intended wire form (locked as Q3 in `tasks.md`) is a JSON string
holding the byte payload base64-encoded. **No back-end currently
implements that contract.** Today's emitter behavior:

- **Scala**: `ScalaTypeConverter.scala:122-123` maps
  `TBLOB → Array[Byte]`. Circe's default `Encoder[Array[Byte]]`
  encodes as a JSON array of integers (NOT base64).
- **TypeScript**: `TypeScriptTypeConverter.scala:29, 60, 93, 182, 232,
  282, 325` and
  `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/totypescript/extensions/IntrospectionExtension.scala:34`
  emit Scala `???` (`scala.NotImplementedError`) — the transpiler
  refuses to generate code for any DTO containing a `TBLOB` field.
- **C#**: `JsonNetExtension.scala:188, 241, 351` and
  `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/tocsharp/types/CSharpType.scala:53, 213`
  emit Scala `???`.

The harness corpus is `TBLOB`-free
(`grep -rn 'blb\b\|TBLOB' .../main-tests/source/` returns zero
matches), so the divergence is currently vacuous on real inputs. The
Q3 lock (base64 for all three) requires PR-02 emitter work in TS and
C# and a Scala-side switch from `Array[Byte]` to a base64-string codec.
Until that work lands, **applications MUST NOT use `TBLOB` in any
`.domain` consumed cross-language.**

### Time scalars `TTs`, `TTsTz`, `TTsU`, `TTime`, `TDate`

All five are JSON strings in ISO-8601 forms. The exact fractional and
zone forms are back-end-specific.

| IDL type | Description                  | Scala native type | Format                     |
|----------|------------------------------|-------------------|----------------------------|
| `TTs`    | Local datetime (no zone)     | `LocalDateTime`   | `yyyy-MM-ddTHH:mm:ss[.fff]`|
| `TTsTz`  | Zoned datetime               | `ZonedDateTime`   | `yyyy-MM-ddTHH:mm:ss[.fff]<offset>` |
| `TTsU`   | UTC datetime (zone fixed)    | `ZonedDateTime`   | `yyyy-MM-ddTHH:mm:ss[.fff]Z` |
| `TTime`  | Time-of-day (no zone)        | `LocalTime`       | `HH:mm:ss[.fff]`           |
| `TDate`  | Date (no zone)               | `LocalDate`       | `yyyy-MM-dd`               |

Scala native types from `ScalaTypeConverter.scala:126-135`. Scala
codec implementations and default formatters (3-digit fractional via
`ISO_LOCAL_DATE_TIME_3NANO`, etc.):
`idealingua-v1/idealingua-v1-runtime-rpc-scala/src/main/scala/izumi/idealingua/runtime/circe/IRTTimeInstances.scala`
(class is referenced from `CirceTranslatorExtensionBase.scala:25`).
Specifically:

- `IRTTimeInstances.scala:113-114` — `LocalDateTime` defaults read
  `ISO_LOCAL_DATE_TIME` (lenient fractional), write
  `ISO_LOCAL_DATE_TIME_3NANO` (3-digit fractional).
- `IRTTimeInstances.scala:135-136` — `ZonedDateTime` encoder
  pre-converts to UTC then formats as `OffsetDateTime`. **The Scala
  encoder always emits `Z` for the UTC offset** (Java
  `DateTimeFormatter` `OFFSET_TIME_ID` rule for zero offset).

C# format constants in
`idealingua-v1/idealingua-v1-runtime-rpc-csharp/src/main/resources/runtime/csharp/IRT/Marshaller/JsonNetMarshaller.cs:105-146`:

- `TslDefault = "yyyy-MM-ddTHH:mm:ss.fff"` — local datetime, no zone.
- `TszDefault = "yyyy-MM-ddTHH:mm:ss.fffzzz"` — zoned datetime, **always
  emits `+00:00` for UTC** (the `zzz` specifier is offset-form).
- `TsuDefault = "yyyy-MM-ddTHH:mm:ss.fffZ"` — UTC datetime, literal `Z`.

C# emitter dispatch at `JsonNetExtension.scala:190-194`:
- `TTime`: hand-formatted `"HH:mm:ss.fff"`.
- `TDate`: `ToString("yyyy-MM-dd", CultureInfo.InvariantCulture)`.
- `TTs`: `JsonNetTimeFormats.TslDefault`.
- `TTsTz`: branch on `Kind == DateTimeKind.Utc`: UTC ⇒ `TsuDefault`
  (literal `Z`); zoned ⇒ `TszDefault` (offset form). After
  round-trip-via-string the `Kind` typically settles to `Unspecified`
  and re-serialization picks `TszDefault`, so the stable C# form for
  `ts` is `+00:00` (per PR-03.3b empirical note in `tasks.md`).
- `TTsU`: `ToUniversalTime().ToString(JsonNetTimeFormats.TsuDefault, ...)`
  ⇒ literal `Z`.

TypeScript formatter
(`idealingua-v1/idealingua-v1-runtime-rpc-typescript/src/main/resources/runtime/typescript/irt/formatter.ts`):

- `writeTime`: `moment(value).format('HH:mm:ss.SSS')`.
- `writeDate`: `moment(value).format('YYYY-MM-DD')`.
- `writeZoneDateTime`:
  `moment(value).format('YYYY-MM-DDTHH:mm:ss.SSSZ')` ⇒ **emits
  `+00:00` for UTC** (moment `Z` token = `±HH:mm`).
- `writeLocalDateTime`: `moment(value).format('YYYY-MM-DDTHH:mm:ss.SSS')`.
- `writeUTCDateTime`:
  `moment(value).utc().format('YYYY-MM-DDTHH:mm:ss.SSSZ')` ⇒
  `+00:00`. **NOTE**: this diverges from Scala's literal `Z` for
  `TTsU`. See "Per-language divergences — Time UTC zone".

## §10. Optional / list / set / map

### `TOption[T]`

`Some(v)` ⇒ encoding of `v` (per §9 / §10 / §3 / etc.).
`None` ⇒ **`"key":null`** in the parent struct (Scala / Circe default
for `Option`-typed case-class fields). PR-03.2 verified empirically:
`OptionalObj/with-none.json = {"no":null}` round-trips byte-identically
through the Scala leg (see `tasks.md` PR-03.2 notes).

The original master plan §4.10 said "Circe omits the key on None"; the
correction is that `deriveEncoder` does NOT use `dropNullValues`, so
the key IS emitted with value `null`.

TypeScript and C# diverge from this rule — see "Per-language
divergences — Optional-None".

### `TList[T]`

JSON array. Element order is the application-supplied order
(insertion-equivalent for the underlying language collection: Scala
`List`, TS `Array`, C# `List<T>`).

### `TSet[T]`

JSON array (NOT a JSON object / set — JSON has no set type). Iteration
order is **insertion order** as a wire-format invariant (locked as Q5
in `tasks.md`):

- Scala uses `LinkedHashSet`-typed generated fields (insertion
  order-preserving).
- TS / C# emitters MUST agree.

PR-03.2 fixtures cover single-element TSet only. Multi-element TSet
fixtures are deferred to F8 in `tasks.md` (post-PR-02 typer rewrite,
where the iteration-order policy is enforced uniformly).

### `TMap[K, V]`

JSON object when `K` is string-shaped at the wire level (i.e. `K` is
`TString`, an enum, an identifier, or any scalar whose wire form is a
JSON string). Object keys are the encoded `K` values; object values
are the encoded `V` values.

`K` MUST be a `ScalarId` per `TypeId.scala:281`
(`final case class TMap(keyType: ScalarId, valueType: TypeId)`). The
typer rejects non-scalar keys at IDL-load time.

Scala's `Encoder.AsObject` requirement is enforced at
`CirceTranslatorExtensionBase.scala:212` (`case _: TMap => true` in
the `isObjectEncoder` decision for §8 unwrap). Circe rejects any `K`
that lacks a `KeyEncoder` at compile time; the per-scalar
`KeyEncoder` derivations are at `withParseable`
(`CirceTranslatorExtensionBase.scala:175-178`).

**Iteration order.** Insertion-order, mirrored from Scala
`LinkedHashMap`-typed generated fields. Same invariant as `TSet`.

## Per-language divergences (informational)

The empirical divergences below were observed during PR-03.3a (TS
leg) and PR-03.3b (C# leg) Layer B fixture authoring, and confirmed
during PR-03.4 (Layer C interop matrix). All are tracked under F10 in
`tasks.md`. The Layer C harness's exclusion list documents the
specific (source, target, fixture) tuples that cannot round-trip
without compiler/typer changes (see `tasks.md` PR-03.4 entry: 6
excluded tuples out of 128 attempted).

These are KNOWN DIVERGENCES, not aspirational. Future PR-02 typer/
emitter work may converge them; until then, applications MUST NOT
depend on cross-language byte-identity.

### Optional-None (struct field of type `TOption[T]`, value `None`)

| Back-end   | Wire form    | Source                                       |
|------------|--------------|----------------------------------------------|
| Scala      | `"key":null` | Circe `deriveEncoder` for `Option`           |
| TypeScript | key dropped  | `undefined` value ⇒ `JSON.stringify` omits   |
| C#         | key dropped  | `NullValueHandling.Ignore` + per-converter `if (v.X != null)` guards |

Cross-language symptom: Scala-encoded `{"no":null}` is rejected by
the TS decoder (which expects `{}`). Confirmed in PR-03.4 as
exclusion tuples F-A, F-B (see `tasks.md` PR-03.4 entry).

### Float zero (`0.0`)

| Back-end   | Wire form | Source                                      |
|------------|-----------|---------------------------------------------|
| Scala      | `0.0`     | Circe `encodeDouble` keeps fractional `.0`  |
| TypeScript | `0`       | `JSON.stringify(0.0)` emits integer-shape   |
| C#         | `0.0`     | Newtonsoft round-trip default               |

Byte-strict diffs at the wire level; semantically equivalent.

### Unsigned ints (`TUInt8` / `TUInt16` / `TUInt32` / `TUInt64`)

| Back-end   | In-memory representation                | Behavior on negative-input            |
|------------|-----------------------------------------|---------------------------------------|
| Scala      | signed primitive (`Byte`/`Short`/`Int`/`Long`); two's-complement wrap | Silent wrap (`uint8 = 200` ⇒ `Byte = -56`) |
| TypeScript | native `number`; codec validates        | Validators throw on negative input    |
| C#         | native unsigned (`byte`/`ushort`/`uint`/`ulong`) | Throws on negative input         |

Confirmed PR-03.4 exclusion: `CSharp→Scala AllTypes.Struct (basic)`
(C# `uint8=200` ⇒ Scala `Byte` saturates), and the mirror
`Scala→C#` direction.

### Int64 boundary (≥ 2^53)

| Back-end   | Behavior                                          |
|------------|---------------------------------------------------|
| Scala      | Full 64-bit `Long`                                |
| TypeScript | `Number` type; precision lost ≥ 2^53              |
| C#         | Full 64-bit `long`                                |

A Scala-emitted `9223372036854775000` round-trips through TS as
`9223372036854776000` (last 3 digits lost). PR-03.4 excludes
`Typescript→*` for any int64 fixture > 2^53; not yet excluded
empirically because no in-corpus fixture sits in that range.

### Time UTC zone (`TTsTz` / `TTsU` UTC values)

| IDL type | Scala       | TypeScript                       | C#                                   |
|----------|-------------|----------------------------------|--------------------------------------|
| `TTsTz`  | literal `Z` | `+00:00` (moment `Z` token)      | `+00:00` (`zzz` specifier round-trip)|
| `TTsU`   | literal `Z` | `+00:00` (moment `.utc().format`)| literal `Z` (`TsuDefault` ends in `Z`)|

For `TTsTz`, the C# round-trip path produces `+00:00` because
`DateTime.ParseExact` of literal-`Z` input produces
`DateTimeKind.Unspecified`, and re-serialization picks `TszDefault`.
For `TTsU`, only TS diverges from the spec's "literal `Z`" rule.
PR-03.4 captures these as exclusion tuples; TS-leg only
loses the `AllTypes.Struct` fixture (also F13 — see below).

### Identifier URL-escape case

| Back-end   | Hex case |
|------------|----------|
| Scala      | uppercase (`%3A`, `%23`)        |
| TypeScript | uppercase (`%3A`, `%23`)        |
| C#         | lowercase (`%3a`, `%23`) (`IRT.Transport.UrlEscaper`) |

Cross-language: Scala-encoded Identifier strings round-trip cleanly
to TS but not to C#, and vice versa. PR-03.4 notes (see
`tasks.md` PR-03.3b) flag this for PR-02 reconciliation.

### `TBLOB`

See §9 above. All three back-ends DIVERGE from the locked Q3
(base64-string) contract today:

| Back-end   | Behavior                                                |
|------------|---------------------------------------------------------|
| Scala      | `Array[Byte]` ⇒ JSON array of integers (NOT base64)     |
| TypeScript | Transpiler emits `???` (NotImplementedError); refuses   |
| C#         | Transpiler emits `???` (NotImplementedError); refuses   |

Q3 lock currently false on all three. PR-02 must reconcile (see F5 in
`tasks.md`).

### `AllTypes.Struct` TS-leg fixture (F13)

The TypeScript Layer B leg drops the
`izumi.test.domain01.AllTypes.Struct` fixture due to a
moment + tsx-runtime incompatibility: the IRT formatter imports
`moment` via legacy CommonJS interop, which under tsx 4.21 + Node 24
becomes a non-callable wrapper at runtime. Scala and C# legs DO
include the fixture. Three potential fixes are documented in F13
(`tasks.md`); PR-03.3a chose to accept the drop, with 16 of 17
wireIds and 19 of 20 TS fixtures landing.

This is a harness-environment limitation, not a wire-format spec
divergence — the wire form for `AllTypes.Struct` IS the standard
struct encoding per §3.

### Service / Buzzer method input class export status (F9)

The TypeScript transpiler emits service/buzzer method input classes
WITHOUT `export`
(`golden/typescript/idltest/services/TestService.ts:250`,
`golden/typescript/idltest/events/TestBuzzer.ts:24` show
`class InSimple` / `class InEmpty` rather than `export class ...`).
Only `Out*` classes are exported. The C# leg additionally diverges in
that some methods don't synthesize Input/Output classes at all
(parameterless / `+Mixin` / singular-string-output cases) and uses
`In<Method>` / `Out<Method>` naming, so RTTI strings differ from
Scala's `<Method>Input`.

PR-03.3a/03.3b excluded 5 service/buzzer wireIds from the TS leg and
5 from the C# leg as a result. The wire format for these types when
they DO encode is the standard struct encoding per §3; the divergence
is in code-generation surface area, not in the wire bytes themselves.
PR-02 (typer rewrite) should consider exporting input classes uniformly.

## Frozen baseline

The wire format described in this document is frozen at git tag
`wire-format-baseline-2026-05-03` (locked as L6 in `tasks.md`). The
date in the tag is fixed and does not move; the tag points to the
single one-shot baseline commit produced by PR-03.1 (Layer A goldens
+ harness module skeleton; commit on branch `wip/necromancy`).

The freeze tag is the reference point that all future drift diffs
against. The Layer A goldens (700 generated source files committed
under
`idealingua-v1/idealingua-v1-test-defs/golden/{scala,typescript,csharp}/`)
together with the Layer B fixtures
(`idealingua-v1/idealingua-v1-test-defs/wire-fixtures/{scala,typescript,csharp}/<wireId>/<scenario>.json`)
ENCODE this specification operationally. If any rule above is
ambiguous, the goldens + fixtures are authoritative.

The four contractual sbt tasks operationalize the spec:

- `regenerateGoldens` — regenerate Layer A source-level goldens.
- `verifyGoldens` — assert character-for-character source equality
  against the committed goldens (Layer A).
- `runWireFixtures` — drive Layer B byte-strict JSON round-trip on
  each language back-end against the committed fixtures.
- `runCrossLangInterop` — drive Layer C cross-language full-loop
  byte-strict round-trip across the 6 ordered (source, mediator)
  pairs.

A green run of all four constitutes empirical proof that the bytes on
the wire have not changed since the freeze tag.

## How to extend the spec

Future wire-format changes (whether driven by PR-02 typer rewrite or
post-modernization feature work) MUST:

1. **Update this document.** Every numbered topic that the change
   touches gets a precise edit, with new file:line citations.
2. **Update Layer A goldens.** Run `sbt regenerateGoldens` against the
   new compiler; commit the diff.
3. **Update Layer B fixtures** for any wireId whose bytes-on-the-wire
   change. Authoring path:
   `idealingua-v1/idealingua-v1-test-defs/wire-fixtures/<lang>/<wireId>/<scenario>.json`.
4. **Update Layer C exclusions** if a divergence resolves (move tuple
   off the exclusion list) or widens (add tuple to the exclusion
   list). Update F10 status in `tasks.md` accordingly.
5. **Cut a new freeze tag** (after PR-02 ships, or after any future
   wire-affecting release), naming it
   `wire-format-baseline-YYYY-MM-DD`. Document the migration guidance
   for downstream consumers in the release notes.

The spec, the goldens, the fixtures, and the exclusion list MUST move
together. Skipping any of (1)-(5) puts the harness and the spec out
of sync and silently re-introduces the threat that the harness exists
to mitigate.

## See also

- `tasks.md` — full ledger of cross-cutting decisions, follow-ups, and
  per-PR completion notes (including the empirical divergences cited
  in this document).
- `docs/drafts/20260503-PR03-backcompat-test-harness-plan.md` — PR-03
  master plan; this document was authored by PR-03.6 against §4
  (skeleton) of that plan.
- `docs/drafts/20260503-PR02-idealingua-modernization-plan.md` —
  PR-02 modernization plan; the typer rewrite that may converge the
  per-language divergences.
- `docs/drafts/20260508-PR0304-layer-c-interop-plan.md` — PR-03.4
  Layer C cross-language interop matrix (the empirical divergence
  observations that fed the "Per-language divergences" section).
- The `runWireFixtures` and `runCrossLangInterop` sbt tasks, defined
  in `sbtgen/Deps.scala`, and the harness module under
  `idealingua-v1/idealingua-v1-test-harness/`.
