# `main-tests` corpus — feature coverage audit (R2)

This document audits the test corpus under `source/` for IDL feature coverage.
The corpus drives:

- the build-time-generated test sources consumed by `idealingua-v1-test-harness` (Scala, TypeScript, C#);
- the MCP schema emitter and MCP HTTP4s bridge tests;
- the regression-harness sample apps (R3) and v1.4.19-vs-HEAD comparison (R4).

Feature inventory is derived from `idealingua-v1-model/.../typer/ir/TypeDef.scala`
(Primitive subclasses, Generic subclasses, user-type kinds) and
`idealingua-v1-core/.../il/parser/DefSignature.scala` (method Output variants).

## Legend

- **Exists** — at least one fixture under `source/` exercises the feature.
- **R2 added** — fixture added in this audit cycle (under `source/coverage/`).
- **Skip / rejected** — the feature is intentionally rejected by the typer
  (see `defs/negative/`); not a corpus gap.
- **Out of scope** — language keyword that has been retired or is unused in
  the current translator pipeline; surfaced for follow-up.

## Primitives (`TypeDef.Primitive`)

| Primitive | Bare field | In `opt[_]` | In `list[_]` | In `set[_]` | In `map[_, T]` key | In `map[T, _]` value | Where |
|---|---|---|---|---|---|---|---|
| `TBool`    | Exists | — | — | — | — | — | `domain01.AllTypes.b` |
| `TString`  | Exists | Exists | Exists | — | Exists | Exists | `domain01.AllTypes.s`, `enums.KVEnumGeneric` |
| `TInt8`    | Exists | — | — | — | — | — | `domain01.AllTypes.int8` |
| `TInt16`   | Exists | — | — | — | — | — | `domain01.AllTypes.int16` |
| `TInt32`   | Exists | — | — | — | R2 added | — | `domain01.AllTypes.int32`, `coverage.generics.mapkeys` |
| `TInt64`   | Exists | — | — | — | R2 added | — | `domain01.AllTypes.int64`, `coverage.generics.mapkeys` |
| `TUInt8`   | Exists | — | — | — | — | — | `domain01.AllTypes.uint8` |
| `TUInt16`  | Exists | — | — | — | — | — | `domain01.AllTypes.uint16` |
| `TUInt32`  | Exists | — | — | — | R2 added | — | `coverage.generics.mapkeys` |
| `TUInt64`  | Exists | — | — | — | — | — | `domain01.AllTypes.uint64` |
| `TFloat`   | Exists | — | — | — | — | — | `domain01.AllTypes.f` |
| `TDouble`  | Exists | — | — | — | — | — | `jsonlike.JLNumber`, `domain01.AllTypes.d` |
| `TUUID`    | Exists | — | — | — | R2 added | — | `anyvals.RecordId`, `coverage.generics.mapkeys` |
| `TBLOB`    | Exists | R2 added | R2 added | R2 added | — | R2 added | `blobtest`, `coverage.generics.blob` |
| `TTs`      | Exists | — | — | — | — | — | `inheritance.NotiBase.at` |
| `TTsTz`    | Exists | — | — | — | — | — | `domain01.AllTypes.tslocal` |
| `TTsU`     | Exists | — | — | — | — | — | `domain01.TsuData.since` |
| `TTime`    | Exists | Exists | — | — | — | — | `domain01.AllTypes.time` / `.optionTime` |
| `TDate`    | Exists | — | — | — | — | — | `domain01.AllTypes.date` |

**R2 fixture**: `coverage/primitives-flat.domain` — every primitive as a bare
field in one non-recursive DTO. Compared with `AllTypes` (which is
self-recursive through containers) this gives translators a "flat" reference.

## Generics (`TypeDef.Generic`)

| Generic | Of primitive | Of DTO | Nested | Where |
|---|---|---|---|---|
| `TList`  | Exists | Exists | R2 added | `domain01.AllTypes.list`, `coverage.generics.nested` |
| `TSet`   | Exists                    | Exists | R2 added | `domain01.AllTypes.selfSet`, `coverage.generics.nested` |
| `TOption`| Exists | Exists | R2 added | `dtofields.OptionalObj`, `coverage.generics.nested` |
| `TMap` (str key)   | Exists | Exists | R2 added | `enums.KVEnumGeneric`, `coverage.generics.nested` |
| `TMap` (non-str key) | R2 added (i32/i64/u32/uid/enum) | — | — | `coverage.generics.mapkeys` |

**Recursive constructs:**
- ADT cycle through container element type: `idltest.json.JSONLike` (mutual recursion via `map`/`list`).
- DTO with `opt[self]`: `domain01.NestedClass`.
- Mixin chain: `inheritance.NotiBase → NotiWithFile → NotiWithFileRevision`.

## User types

| Kind | Variant | Where |
|---|---|---|
| **DTO** | simple (no parents)             | `consts.TestPair`, `algebraics.Success`, many |
|         | with `+` concept mixin          | `datainheritance.TestData2`, `upcasts.Item` |
|         | with `&` interface mixin        | `dtofields.Point`, `phase.Name_view` |
|         | with `-` field subtraction      | `substraction.PublicUser1`, `substraction.PublicUser2` |
|         | covariant override              | `inheritance.CovariantDTO1`, `CovariantDTO2` |
|         | deep diamond                    | `diamonds.DTO1`, `phase.Name_view` |
| **Identifier** | scalar (single field, uid) | R2 added `coverage.usertypes.idsingle.Singleton` |
|                | multi-field scalar         | `identifiers.CompanyId`, `BucketID` |
|                | nested + enum field        | `identifiers.UserWithEnumId`, `ComplexID` |
| **Enum**  | single member                  | R2 added `coverage.usertypes.enumsingle.Singleton` |
|           | multi member                   | `enums.TestEnum`, many |
|           | as map-key                     | R2 added `coverage.generics.mapkeys.MapWithVariousKeys.byEnum` |
|           | as field                       | `enums.EnumHolder`, `identifiers.UserWithEnumId` |
| **ADT**   | DTO-typed branches             | `algebraics.Alternative`, `ast.AST` |
|           | branches-of-interface          | `algebraics.AdtWithInterface` (mixin branch) |
|           | branches-of-primitive          | **Rejected** (`negative/adt-members/adt-with-primitive-member`) |
|           | recursive (through container)  | `idltest.json.JSONLike` |
|           | branch-with-rename `as`        | `algebraics.Alternative`, `ast.AST` |
|           | branch from another domain     | R2 added `coverage.crossdom.importer.ForeignAdt` |
| **Interface (mixin)** | simple             | many |
|                       | deep inheritance   | `ast.TIntNode .. TIfNode`, `inheritance.NotiWith*` |
|                       | with `+` and `&`   | `ast.TIntNode`, `inheritance.NotificationWithAB` |
| **Alias** | to primitive                   | `model03shared01.Test1`, `model01.UserId` |
|           | to user type                   | `aliases.A1`, `datainheritancetransitive.CouponID` |
|           | cross-domain                   | `aliases.A2`, `domain02.RTestEnumIndirect`, R2 `coverage.crossdom.importer.LeafAlias` |
|           | alias chain (alias of alias)   | `domain03recursive01.Test111`, R2 `coverage.crossdom.importer.LeafAliasChain` |
| **Clone (newtype)** | empty modifiers (alias-like) | `clones.M1` |
|                     | with modifiers (DTO-like)    | `clones.M2` |

## Service / Buzzer Output variants (`RawMethod.Output`)

| Output variant | Service | Buzzer |
|---|---|---|
| `Void`                                       | Exists `services.unitToUnit`           | Exists `events.TestBuzzer.empty` + R2 `coverage.buzzers.alloutputs.voidEvent` |
| `Singular(primitive)`                        | Exists `services.parameterless`        | Exists `events.TestBuzzer.hello` + R2 `coverage.buzzers.alloutputs.primEvent` |
| `Singular(DTO)`                              | Exists `services.greetSingularOut` (str) — R2 added DTO via `coverage.services.alloutputs.singularDto` | R2 added `coverage.buzzers.alloutputs.dtoEvent` |
| `Struct(fields)`                             | Exists `services.greetImplicitStructOut` | R2 added `coverage.buzzers.alloutputs.structEvent` |
| `Algebraic(branches)`                        | Exists `services.greetAlgebraicOut`    | R2 added `coverage.buzzers.alloutputs.algebraicEvent` |
| `Alternative(success, failure)`              | Exists `services.alternative`          | R2 added `coverage.buzzers.alloutputs.alternativeEvent` |
| Empty input `()`                             | Exists `services.unitToUnit`           | Exists `events.TestBuzzer.empty` |
| Foreign-domain input                         | Exists `domain02.ImportIdService.some` | — |
| All-Output single-service witness            | R2 `coverage.services.alloutputs.AllOutputsService` | R2 `coverage.buzzers.alloutputs.AllOutputsBuzzer` |

## Cross-domain

| Feature | Where |
|---|---|
| Field type from another domain                  | `domain02.TestInterface1.fromOtherDomain`, R2 `coverage.crossdom.importer` |
| Cross-domain interface extension (`& other#Mix`)| R2 `coverage.crossdom.importer.ExtendsForeignMixin` |
| ADT branch from another domain                  | R2 `coverage.crossdom.importer.ForeignAdt`, `domain02.SomeResp` |
| Cross-domain alias                              | `aliases.A2`, R2 `coverage.crossdom.importer.LeafAlias` |
| Cyclic domain imports                           | `izumi.test.clashing` / `clashing.another` |

## Misc

| Feature | Where |
|---|---|
| Top-level constants (auto-typed scalar)         | `consts.someInt`, `consts.someString` |
| Top-level constants (typed scalar)              | `consts.anotherString: str` |
| Top-level constants (typed lst/lst-of-lst)      | `consts.typedlist`, `consts.insanelist` |
| Domain-level doc strings                        | `domain01.TestObject`, many |
| Annotations on types/fields/enums               | `domain01.TestObject`, `domain01.TestIdentifier.userId` |
| `declared X` placeholder + overlay              | `overlaytest.withoverlay.declared` |
| `streams` keyword (not exercised by translators)| `streams.TestStreams` |
| `ForeignType` keyword                           | Out of scope — translator support removed in PR-02 / C7 |

## Translator defects surfaced (but not fixed) in R2

1. **C# translator: `set[opt[T]]` emits duplicate local variable.** When a DTO
   field is typed `set[opt[<primitive>]]`, the C# deserializer emits two
   declarations of the inner `_<field>_d` variable in the same scope,
   triggering CS0128 ("a local variable named X is already defined") plus a
   knock-on CS0165 ("use of unassigned local variable"). Worked around by
   removing the field from `coverage/generics-nested.domain` and recording
   the gap here. Defect lives in the C# translator's
   `ServiceRendererHelpers`/`json-decoder` chain — the duplicate emission
   pattern is `Set[Option[X]]` specifically; `List[Option[X]]` and
   `Opt[Set[X]]` are unaffected (the existing corpus has `opt[list[map[..]]]`
   and that compiles).

## Known gaps not filled in R2

1. **TBLOB in `map` *key* position** — the typer reasonably rejects byte-string
   map keys; not added.
2. **Enum-keyed map**: added; if downstream emitters need stronger evidence
   (TS `Record<string, ...>` vs serialized enum-string keys), add a wire
   fixture in R3.
3. **`map[uid, T]` round-trip** — uuid map keys covered structurally in R2;
   wire round-trip fixture deferred to R3 sample apps.
4. **Streams** are *parsed* (`streams.TestStreams`) but not translated. No
   coverage addition warranted until C# transpiler ships streams support.
5. **`ForeignType` IDL keyword** — flagged as Out of scope per `C7/Q11`
   (likely to be removed). No fixture.
6. **ADT with primitive-singular branch (synthesized wrapper)** — rejected
   by the typer (`negative/adt-members/adt-with-primitive-member`); not a gap.
7. **Direct cyclic DTO** (`data X { _: X }`) — rejected by the typer
   (`negative/cyclic-usage`); the cyclic case is exercised only through
   `opt[self]` (`domain01.NestedClass`) and through containers (`JSONLike`).

## R2 fixtures added

Under `source/coverage/`:

| File | Domain | Purpose |
|---|---|---|
| `primitives-flat.domain`         | `coverage.primitives`              | Every primitive as a bare DTO field |
| `generics-blob.domain`           | `coverage.generics.blob`           | TBLOB inside opt/list/set/map value |
| `generics-map-keys.domain`       | `coverage.generics.mapkeys`        | Non-string map keys (i32/i64/u32/uid/enum) |
| `generics-nested.domain`         | `coverage.generics.nested`         | Nested generics (list[list], map[str,list], opt[list[map]], set[opt]) |
| `enum-single-member.domain`      | `coverage.usertypes.enumsingle`    | Single-member enum |
| `identifier-single-field.domain` | `coverage.usertypes.idsingle`      | Single-field identifier |
| `service-all-outputs.domain`     | `coverage.services.alloutputs`     | One service, every Output variant |
| `buzzer-all-outputs.domain`      | `coverage.buzzers.alloutputs`      | One buzzer, every Output variant |
| `crossdom-leaf.domain`           | `coverage.crossdom.leaf`           | Leaf for cross-domain importer fixture |
| `crossdom-importer.domain`       | `coverage.crossdom.importer`       | Cross-domain alias chain, field, mixin inheritance, ADT branch |

Existing 28 fixtures left untouched (history + downstream consumers depend on
their exact wire shape).
