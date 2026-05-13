# Sample-app generation — TypeScript target

You are generating a self-contained TypeScript driver (`sample_app.ts`) for the
**idl-regress** wire-format regression harness. The driver is executed under
`bun` / `tsx` / `node --experimental-strip-types` alongside the generated
idealingua-v1 TypeScript sources. Its job: emit canonical NDJSON to stdout,
one line per fixture:

    <wireId>\t<scenario>\t<json>\n

where:

  * `wireId` — fully-qualified type name as it appears in the generated TS
    polymorphic registry, e.g. `idltest.dtofields.IntPair.Struct`. Use the
    value of `<TypeName>.FullClassName` for structs; for plain types use the
    dotted package + class.
  * `scenario` — short identifier `[A-Za-z0-9_-]+` distinguishing variants of
    the same wireId (e.g. `default`, `nulls`, `nonempty`, `boundary-min`).
  * `json` — `JSON.stringify(value.serialize())` (or the equivalent
    helper-driven serialization for ADTs / identifiers).

Determinism rules (the harness will refuse output that is not byte-stable):

  * No `Date.now()`, `Math.random()`, `process.env`, or filesystem reads. All
    scalar values are hard-coded literals.
  * Collections are in source-declaration order; the harness re-sorts by
    `(wireId, scenario)` post-emit, so within a `scenario` the JSON shape is
    whatever the encoder produces but it must be reproducible.
  * Time fields use a fixed reference instant — pick one literal and reuse it.

Coverage target: at least one fixture per non-internal type in the generated
tree. Two scenarios when a type has optional or sum-type alternatives worth
exercising. Service input/output envelopes count as types.

## Context

IDL sha256: `{{IDL_SHA256}}`
idealingua-v1 runtime version: `{{RUNTIME_VERSION}}`

### IDL tree (relative to `<project>/source/`)

```
{{IDL_TREE}}
```

### Generated tree (relative to scratch `gen-old/typescript/`)

```
{{GENERATED_TREE}}
```

## Output contract

A single file containing one TypeScript source. Top-level structure:

```typescript
// idl-regress sample app for {{IDL_SHA256}}
import { IntPairStruct } from './idltest/dtofields/IntPair';
// … other imports …

function emit(wireId: string, scenario: string, value: { serialize(): unknown }): void {
  console.log(`${wireId}\t${scenario}\t${JSON.stringify(value.serialize())}`);
}

// for each fixture:
emit('idltest.dtofields.IntPair.Struct', 'zero', new IntPairStruct({ x: 0, y: 0 }));
// …
```

Use `console.log()` for stdout. Any diagnostic prints belong on
`console.error()`.

Do NOT introduce randomness, side effects, or non-trivial logic. Pure
construction + serialize + console.log. The harness will canonicalize the
JSON (re-sort keys, re-emit no-whitespace), so key ordering inside the
emitted JSON is irrelevant for the diff — but the fixture set itself must
be deterministic.

Do NOT emit any line that is not in the `wireId\tscenario\tjson` shape.

Return only the file contents — no commentary, no surrounding markdown fence.
