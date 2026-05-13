# MCP / JSON Schema Emission Plan (idealingua-v1)

**Date:** 2026-05-12
**Branch:** wip/necromancy
**Author:** planning subagent
**Status:** draft — for user review

## 1. Goal & non-goals

### Goal
Add a new compiler emission target that produces, from a `Domain` IR, both:
1. A **JSON Schema 2020-12** library describing every user-declared type's wire representation (per `docs/wire-format.md`), embedded in an **OpenAPI 3.1** document at `components.schemas`.
2. An **MCP (Model Context Protocol)** `tools.json` per service, exposing each method as a tool with `name`, `description`, `inputSchema`, `outputSchema`.

### Non-goals
- No code generation. No runtime. No client/server SDK.
- No cross-language byte-strict reconciliation — schema describes Scala-leg canonical wire (per `wire-format.md` per-language divergences are advisory `x-idealingua-divergence` annotations).
- No JSON Schema dialect older than 2020-12.
- No GraphQL / Avro / Protobuf side-outputs.
- No `paths:` in OpenAPI (schema library only, MCP carries the method shape).

## 2. Pre-locked decisions

| # | Decision | Rationale |
|---|----------|-----------|
| D1 | wireId-based naming (`<package>.<Type>`) for schema components | C4/Q12 lock; reuses `wireId` formula at `TypeId.scala:31`. |
| D2 | New translator `toschema/` mirrors `toscala/` layout; consumes `Domain` IR directly | Matches IMPL-7a/7b/7c Phase B pattern. |
| D3 | Output as OpenAPI 3.1 + MCP `tools.json` (two-file family per domain) | OpenAPI for validators; MCP `tools[]` for LLM consumption; cross-references via `$ref`. |
| D4 | Schema describes Scala-leg wire form (canonical) | `wire-format.md` already promotes Scala as canonical; divergences advisory. |
| D5 | TBLOB → `{"type": "string", "contentEncoding": "base64"}` (aspirational, Q3 lock) | Q3 locks base64 even though emitters diverge; consumers can't act on integer-array reality. |
| D6 | TOption[T] → `{"oneOf": [<T>, {"type": "null"}]}` | JSON Schema 2020-12 idiom; matches Scala `null` form. |
| D7 | ADT/Interface → `oneOf` with `{ "<discriminator>": <branch> }` wrapper schemas | Matches §4 of wire-format. |
| D8 | Identifier → `{"type": "string", "pattern": "^<Name>#..."}` + `x-idealingua-fields` advisory | Matches §6. |
| D9 | Singular unwrap (§8) honored: `Output.Singular(T)` outputSchema = schema of T directly | Compile-time decision must surface in schema. |
| D10 | Buzzers exposed as MCP tools with `outputSchema: {type:"null"}` + `x-idealingua-kind: "buzzer"` | Per C5/Q1 buzzers are first-class. |
| D11 | Streams skipped (deprecated per C5/Q1); advisory annotation only | Q1 locks deprecation. |
| D12 | New CLI role `:schema` with normal `LP` parser | Mirrors `:scala`/`:typescript`/`:csharp`. |
| D13 | New `IDLLanguage.JsonSchema` case object | Reuses dispatch through `descriptorsMap`. |

USER-DECISION items: D3 envelope choice, D9 unwrap-vs-wrap, D10 buzzer exposure, D11 streams policy.

## 3. JSON Schema mapping table

All fragments target JSON Schema 2020-12 (`"$schema": "https://json-schema.org/draft/2020-12/schema"`).

### 3.1 Primitives (per `TypeId.scala:120-211` and wire-format.md §9)

| IDL | JSON Schema fragment | Source |
|-----|---------------------|--------|
| `TBool` | `{"type": "boolean"}` | §9 TBool |
| `TString` | `{"type": "string"}` | §9 TString |
| `TInt8` | `{"type": "integer", "minimum": -128, "maximum": 127}` | §9 |
| `TInt16` | `{"type": "integer", "minimum": -32768, "maximum": 32767}` | §9 |
| `TInt32` | `{"type": "integer", "minimum": -2147483648, "maximum": 2147483647}` | §9 |
| `TInt64` | `{"type": "integer", "minimum": -9223372036854775808, "maximum": 9223372036854775807}` + `x-idealingua-divergence: "TS-int64-2e53"` | §9 + TS divergence |
| `TUInt8` | `{"type": "integer", "minimum": 0, "maximum": 255}` + `x-idealingua-divergence: "TUInt-Scala-wrap"` | §9 unsigned |
| `TUInt16` | `{"type": "integer", "minimum": 0, "maximum": 65535}` + ditto | |
| `TUInt32` | `{"type": "integer", "minimum": 0, "maximum": 4294967295}` + ditto | |
| `TUInt64` | `{"oneOf": [{"type": "integer", "minimum": 0, "maximum": 9007199254740991}, {"type": "string", "pattern": "^[0-9]{1,20}$"}]}` (Q4 hybrid) | §9, Q4 |
| `TFloat` / `TDouble` | `{"type": "number"}` (no NaN/Inf — see Float-zero/NaN divergence) | §9 |
| `TUUID` | `{"type": "string", "format": "uuid"}` | §9 TUUID |
| `TBLOB` | `{"type": "string", "contentEncoding": "base64"}` + `x-idealingua-divergence: "TBLOB-F5"` | §9 + F5 |
| `TTs` | `{"type": "string", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?$"}` | §9 TTs |
| `TTsTz` | `{"type": "string", "format": "date-time"}` + `x-idealingua-divergence: "Time-UTC-zone"` | §9 TTsTz |
| `TTsU` | `{"type": "string", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$"}` | §9 TTsU |
| `TTime` | `{"type": "string", "pattern": "^\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?$"}` | §9 TTime |
| `TDate` | `{"type": "string", "format": "date"}` | §9 TDate |

### 3.2 Generics

| IDL | JSON Schema | Source |
|-----|-------------|--------|
| `TList(T)` | `{"type": "array", "items": <T>}` | §10 TList |
| `TSet(T)` | `{"type": "array", "items": <T>, "uniqueItems": true}` (iteration order = insertion order per Q5; not expressible) | §10 TSet |
| `TOption(T)` | `{"oneOf": [<T>, {"type": "null"}]}` | §10 (Scala leg); divergence advisory for TS/C# key-drop |
| `TMap(K, V)` where K = `TString` | `{"type": "object", "additionalProperties": <V>}` | §10 TMap |
| `TMap(K, V)` where K ∈ {scalar string-form} | `{"type": "object", "additionalProperties": <V>, "x-idealingua-key-type": "<K-wireId>"}` | §10 TMap |

### 3.3 User types

#### DTO (`TypeDef.Dto`)
Flat object using `domain.flattenedStructs(id).fields` (inheritance already resolved):
```json
{
  "type": "object",
  "title": "<wireId>",
  "description": "<meta.doc if present>",
  "properties": { "<field>": <typeSchema>, ... },
  "required": ["<every non-Optional field>"],
  "additionalProperties": false
}
```
Field order honoured (declaration order from `FlatField.field`).

#### Identifier (`TypeDef.Identifier`)
String-form per §6:
```json
{
  "type": "string",
  "title": "<wireId>",
  "pattern": "^<ShortName>#(<urlEncodedField>)(:<urlEncodedField>)*$",
  "x-idealingua-kind": "identifier",
  "x-idealingua-fields": [
    { "name": "<sortedFieldName>", "schema": <fieldSchema> }
  ]
}
```
Fields **sorted alphabetically by name** (per §6 `sortedFields = fields.all.sortBy(_.field.name)`). `idNameFix` rule applied for unnamed fields.

#### Enum (`TypeDef.Enum`)
```json
{
  "type": "string",
  "title": "<wireId>",
  "enum": ["<member1>", "<member2>"]
}
```
Member order = declaration order.

#### ADT (`TypeDef.Adt`)
Per §4, wrapped-object form:
```json
{
  "title": "<wireId>",
  "oneOf": [
    { "type": "object", "properties": { "<branchShortName>": { "$ref": "#/components/schemas/<branchSchemaName>" } }, "required": ["<branchShortName>"], "additionalProperties": false }
  ]
}
```

#### Interface (`TypeDef.Interface`)
Per §4 (full-wireId discriminator over implementing DTOs):
```json
{
  "title": "<wireId>",
  "oneOf": [
    { "type": "object", "properties": { "<implFullWireId>": { "$ref": "#/components/schemas/<implWireId>" } }, "required": ["<implFullWireId>"], "additionalProperties": false }
  ]
}
```
Implementors enumerated from `domain.implementingDtos(interfaceId)`.

#### Alias (`TypeDef.Alias`)
No separate schema (aliases collapsed by Phase 3); references through alias resolve to target schema directly.

## 4. MCP tool metadata shape

Per service (`TypeDef.Service`), emit one MCP tool per method:

```json
{
  "name": "idltest.services.TestService.simple",
  "description": "Documentation from method.meta.doc, or empty.",
  "inputSchema": {
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "type": "object",
    "properties": { "<inputField1>": "<fieldSchema>" },
    "required": ["<non-optional field names>"],
    "additionalProperties": false
  },
  "outputSchema": "<see-output-variants-below>",
  "x-idealingua-wireId-input": "idltest.services.TestService.SimpleInput",
  "x-idealingua-wireId-output": "idltest.services.TestService.SimpleOutput",
  "x-idealingua-kind": "rpc"
}
```

### Output variants

| `Output` variant | `outputSchema` |
|------------------|----------------|
| `Output.Void()` | `{"type": "null"}` |
| `Output.Singular(T)` | schema of `T` (unwrapped per §8) + `x-idealingua-unwrap: true` |
| `Output.Struct(s)` | object schema of `s.fields` (flat) |
| `Output.Algebraic(alts)` | ADT-style `oneOf` per §4 |
| `Output.Alternative(success, failure)` | `oneOf` over Success/Failure wrappers (matches §7 `goodAltBranchName`/`badAltBranchName`) |

### Tool name strategy
`<domainPackage>.<ServiceName>.<methodName>` — clean RPC-style identifier, NOT the input ephemeral wireId (which is `...SimpleInput` and awkward). Underlying schema `$ref`s use the ephemeral wireId.

### Buzzers
Same envelope, `outputSchema: {"type": "null"}`, `x-idealingua-kind: "buzzer"`.

## 5. CLI surface

### New role: `:schema`
Reuses `LP` parser (`-t target`, `-m manifest`, `-d define`, `-nr`/`-nz` flags — most no-op for schema).

```
idlc :schema -t target/schema
```

### `IDLCArgs.scala` change
Add to the `RoleParserSchema` list:
```scala
RoleParserSchema("schema", LP, Some("JSON Schema + MCP target"), None, freeArgsAllowed = false),
```

### `IDLLanguage` change
```scala
case object JsonSchema extends IDLLanguage { override val toString: String = "schema" }
```
plus matching `parse` arm.

### Output layout (`TranslationLayouter` impl)
```
target/schema/
  <domain.package>/
    schema.json                 // OpenAPI 3.1 with components.schemas (all types in domain)
    <ServiceName>.mcp.json      // one MCP tools doc per service+buzzer
    <BuzzerName>.mcp.json
```
Single combined OpenAPI per domain; MCP per service.

### `TypespaceCompilerBaseFacade.descriptors`
Append `SchemaTranslatorDescriptor` to the existing 3-element Seq.

## 6. Architecture / file layout

New tree under `idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toschema/`:

```
toschema/
  SchemaTranslatorDescriptor.scala      // entry point; binds to IDLLanguage.JsonSchema
  DomainSchemaTranslator.scala          // walks domain.members; calls renderers
  domain/
    SchemaTypeResolver.scala            // TypeId → JSON Schema fragment (primitives, generics, $ref)
    SchemaDtoRenderer.scala             // §3 flat DTO
    SchemaAdtRenderer.scala             // §4 ADT
    SchemaInterfaceRenderer.scala       // §4 Interface oneOf-over-impls
    SchemaIdentifierRenderer.scala      // §6 string-form
    SchemaEnumRenderer.scala            // §5
    SchemaServiceRenderer.scala         // MCP tools.json for services
    SchemaBuzzerRenderer.scala          // MCP tools.json for buzzers
    SchemaMethodOutput.scala            // §8 + Output variant dispatch
    SchemaDocBuilder.scala              // OpenAPI 3.1 envelope assembly
  layout/
    SchemaLayouter.scala                // TranslationLayouter — writes files + manifest
  products/
    SchemaProduct.scala                 // emitted-doc product type
```

No runtime artifact module needed (data files only).

## 7. Domain-IR consumption

Mirror `DomainScalaTranslator`:

```scala
final class DomainSchemaTranslator(
  domain: izumi.idealingua.typer.ir.Domain,
  parsed: DomainMeshResolved,
  options: SchemaTranslatorOptions,
) extends Translator
```

Walk strategy:
1. Iterate `domain.userTypes` (or `domain.members.values.collect { case Member.User(td) => td }`) in `parsed.members` declaration order.
2. Dispatch on `TypeDef` subtype to corresponding renderer.
3. Each renderer returns a JSON-schema fragment (Circe `Json` AST internally).
4. Services + buzzers also produce one MCP tool document each.
5. Aliases are not rendered (already dealiased via `domain.aliases` map for field-level references).

### Resolver / witness pattern
The TextTree-style witness pattern (per M1.5 baboon investigation) is overkill here — JSON is a tree, not lexed text. Recommendation: model schemas as `io.circe.Json` AST internally, render via `Json.printer(Printer.spaces2)` at emission time. This:
- avoids string escaping bugs,
- gives strongly-typed `oneOf` composition,
- lets the JSON Schema validator run in-process against `Json` directly during tests (no parse step).

`io.circe` is already a transitive dependency of the transpiler module.

### Field references and ordering
- DTO fields: from `domain.flattenedStructs(dtoId).fields` (already resolved-inheritance order).
- Identifier fields: `td.fields` sorted alphabetically by `.name` (per §6).
- Enum members: `td.members` in declaration order.
- ADT alternatives: `td.alternatives` in declaration order.
- Method input fields: `method.signature.input.fields` (the SimpleStructure).

### Aliases
Inline-collapse via `domain.aliases.get(aliasId)` recursively. Never emit a schema entry for an alias.

## 8. Open questions for the user

(Recommended defaults bold.)

1. **Output envelope.** Bare JSON Schema only / OpenAPI 3.1 / MCP only / **all three (OpenAPI for schemas + MCP `tools.json` per service)** ?
   - Tradeoff: more files but cleanest separation. OpenAPI works in REST-tooling; MCP `tools.json` is what LLM clients ingest.
2. **MCP spec version.** 2024-11-05 / **2025-06-18** / floating-latest ?
   - Tradeoff: 2025-06-18 makes `outputSchema` standard; older servers ignore the field.
3. **Buzzer exposure.** Skip entirely / **expose as `outputSchema: null` tools with `x-idealingua-kind: "buzzer"`** / separate `events.json` document?
   - Tradeoff: exposing means LLMs can fire events without confirmation; advisory annotation lets clients filter.
4. **Streams.** **Skip silently** / advisory annotation only / emit anyway?
   - Tradeoff: streams deprecated per C5/Q1; skipping aligns with non-streaming MCP model.
5. **TUInt64 ≥ 2^53.** **Hybrid `oneOf: [integer ≤2^53-1, string]`** / always-string / always-integer-with-divergence-warning?
6. **TBLOB.** **Emit aspirational base64 per Q3 lock** / mirror current Scala array-of-ints reality / skip with `x-idealingua-blocked`?
7. **Optional field "required" semantics.** A `TOption[T]` field — **emit as `oneOf: [T, null]` AND exclude from `required`** / include in `required` with nullable form / both forms behind a flag?
   - Tradeoff: Scala emits `"key":null`; TS/C# drop the key. Excluding from `required` accepts both.
8. **Identifier `$ref` to primitive (cross-domain).** A foreign domain's Identifier — inline-expand or `$ref` across the per-domain OpenAPI boundary?
   - Recommendation: per-domain schema files cannot cross-`$ref` by URL by default. Inline-expand foreign Identifiers; add `x-idealingua-imported-from: "<domainId>"` annotation.
9. **Method name format for MCP tool name.** `<package>.<Service>.<method>` / `<Service>.<method>` / fully-qualified ephemeral input wireId?
   - **Recommend `<package>.<Service>.<method>`** (no collision risk; readable).
10. **Doc-comment escape rules.** Pass `meta.doc` through verbatim (Circe escapes) / Markdown / strip?
    - **Verbatim via Circe**; consumers render as plain text.

## 9. Verification plan

### Layer A: source-level goldens
Mirror existing `regenerateGoldens` / `verifyGoldens`:
- `idealingua-v1-test-defs/golden/schema/<package>/<file>.json` committed.
- `verifySchemaGoldens` sbt task — byte-strict equality against the corpus.

### Layer B: schema-validates-fixtures
- For every Layer B fixture under `wire-fixtures/scala/<wireId>/<scenario>.json`, assert the fixture validates against the emitted schema component for `<wireId>`.
- Use `com.networknt:json-schema-validator` (JSON Schema 2020-12 support).
- New sbt task `validateSchemaAgainstFixtures`.

### Layer C: MCP envelope shape
- Validate emitted `*.mcp.json` against the published MCP JSON Schema for `ListToolsResult` (download once into `idealingua-v1-test-defs/schema/mcp-<version>.json`).

### Round-trip sanity
- Generate schemas, feed into an off-the-shelf JSON Schema validator with the Layer B golden inputs, then mutate fields (drop required, wrong type) and assert rejection.

## 10. Milestone breakdown

| M | Scope | Est. days |
|---|-------|-----------|
| **M1** | Scaffold: `toschema/` tree, `IDLLanguage.JsonSchema`, `IDLCArgs` role, primitives + generics, DTO, Enum, Identifier. `verifySchemaGoldens` task. Layer A goldens for ~10 simple corpus domains. | 3-4 |
| **M2** | ADT (§4 wrapped form), Interface (§4 oneOf-over-impls), ADT-of-interface nesting. Goldens extended. | 2 |
| **M3** | Services + buzzers: walk methods/events; emit per-method MCP tool docs; honour §8 singular unwrap; map all 5 `Output` variants. | 2-3 |
| **M4** | MCP envelope (`ListToolsResult` shape with version + serverInfo if needed), `x-idealingua-*` annotations, descriptions from `meta.doc`. | 1 |
| **M5** | Layer B `validateSchemaAgainstFixtures` task; Layer C MCP-spec validation; cover remaining divergences as advisory annotations. | 2 |
| **M6** | Regression-harness integration: add `:schema` to `regression-harness/adapters` so future PRs can re-run the full corpus through the schema emitter. | 1 |

Total: ~11-13 days.

## 11. Risks

| # | Risk | Mitigation |
|---|------|------------|
| R1 | MCP spec drift (2024-11-05 vs 2025-06-18 vs newer) | Parameterise spec version; snapshot the target version's schema into the test-defs module; refresh in a tracked PR when bumping. |
| R2 | Per-language wire divergences cannot all be captured in one schema | Schema describes Scala-leg canonical form; surface each divergence as `x-idealingua-divergence: "<id>"` annotation pointing at `wire-format.md`. |
| R3 | §8 Singular unwrap couples output schema to compile-time decision | Handle `Output.Singular` distinctly in M3; emit inner schema directly, mark with `x-idealingua-unwrap: true`. |
| R4 | Identifier string-form regex pattern is informational; LLMs producing malformed strings still fail at decode time | Emit `x-idealingua-fields: [{name, schema}]` advisory so schema-aware UIs can prompt for structured fields then format. |
| R5 | No code-compile gate (unlike Scala/TS/C#) — schema bugs land silently | Layer B `validateSchemaAgainstFixtures` runs every fixture against the emitted schema. CI-gated. |
| R6 | Ephemeral wireId names (`SimpleInput`, `GreetSingularOutOutput`, `EnumInputInput`) leak as schema component names | Decouple: MCP tool `name` = clean `<pkg>.<Svc>.<method>`; schema components use ephemeral wireId. |
| R7 | TBLOB / TUInt-Scala-wrap / TBLOB-F5 known divergences may confuse consumers | Each known divergence carries a stable `x-idealingua-divergence` ID matching the F-numbers in `tasks.md`. |
| R8 | Foreign / cross-domain types — schema files are per-domain | Inline-expand foreign references at field level; do not attempt cross-file `$ref` URLs. |
| R9 | Field-order changes between typer versions could silently change schema property order | M5 includes an order-stability assertion: regenerate goldens with a swapped typer revision; assert byte-equal. |
| R10 | OpenAPI 3.1 tooling support varies (some validators still on 3.0) | Document 3.1 as required. `--openapi-version=3.0` fallback toggle as a stretch goal. |
