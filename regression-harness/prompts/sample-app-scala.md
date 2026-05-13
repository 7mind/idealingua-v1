# Sample-app generation — Scala target

You are generating a self-contained Scala 3 driver (`sample_app.scala`) for the
**idl-regress** wire-format regression harness. The driver is compiled and run
under scala-cli alongside the generated idealingua-v1 Scala sources. Its job:
emit canonical NDJSON to stdout, one line per fixture:

    <wireId>\t<scenario>\t<json>\n

where:

  * `wireId` — fully-qualified Scala type name in idealingua-v1 output, e.g.
    `idltest.dtofields.Point`. Use the dotted package + class name as it
    appears in the generated sources.
  * `scenario` — short identifier `[A-Za-z0-9_-]+` distinguishing variants of
    the same wireId (e.g. `default`, `nulls`, `nonempty`, `boundary-min`).
  * `json` — circe `Encoder` output with `Printer.noSpaces` for the value.

Determinism rules (the harness will refuse output that is not byte-stable):

  * No `System.currentTimeMillis`, `Instant.now`, `UUID.randomUUID`, or
    environment reads. All scalar values are hard-coded literals.
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

### Generated tree (relative to scratch `gen-old/`)

```
{{GENERATED_TREE}}
```

## Output contract

A single file containing one Scala 3 source. Top-level structure:

```scala
package sample_app

import io.circe.*
import io.circe.syntax.*
import io.circe.Printer

@main def runSampleApp(): Unit =
  val P = Printer.noSpaces
  // for each fixture, println(s"$wireId\t$scenario\t${value.asJson.printWith(P)}")
  // ...
```

Do NOT introduce randomness, side effects, or non-trivial logic. Pure
construction + encode + println. The harness will canonicalize the JSON
(re-sort keys, re-emit no-whitespace), so key ordering inside the emitted JSON
is irrelevant for the diff — but the fixture set itself must be deterministic.

Do NOT emit any line that is not in the `wireId\tscenario\tjson` shape. Any
diagnostic prints belong on `System.err`.

Return only the file contents — no commentary, no surrounding markdown fence.
