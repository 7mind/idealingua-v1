# Sample-app generation — C# target

You are generating a self-contained C# driver (`sample_app.cs`) for the
**idl-regress** wire-format regression harness. The driver is executed under
`dotnet run --project <workDir>` against the generated idealingua-v1 C# tree.
Its job: emit canonical NDJSON to stdout, one line per fixture:

    <wireId>\t<scenario>\t<json>\n

where:

  * `wireId` — fully-qualified type name as it appears in the generated C#
    RTTI registry, e.g. `idltest.dtofields.IntPair.Struct`. For a struct
    use the value of its `RTTI_FULLCLASSNAME` const (lowercase dotted
    package + class). For plain types use the dotted package + class.
  * `scenario` — short identifier `[A-Za-z0-9_-]+` distinguishing variants of
    the same wireId (e.g. `default`, `nulls`, `nonempty`, `boundary-min`).
  * `json` — `JsonConvert.SerializeObject(value, settings)` where
    `settings = new JsonSerializerSettings { NullValueHandling = NullValueHandling.Ignore, Formatting = Formatting.None }`.
    The struct's `_JsonNetConverter` writes the wire form directly (no
    polymorphic wrapper for concrete types; concrete structs emit just
    their fields).

Determinism rules (the harness will refuse output that is not byte-stable):

  * No `DateTime.Now`, no `DateTime.UtcNow`, no `Guid.NewGuid()`, no
    `new Random()` / `Random.Shared`, no `Environment.GetEnvironmentVariable`,
    no filesystem reads. All scalar values are hard-coded literals.
  * Collections are in source-declaration order; the harness re-sorts by
    `(wireId, scenario)` post-emit, so within a `scenario` the JSON shape is
    whatever Newtonsoft.Json produces but it must be reproducible.
  * Time fields use a fixed reference instant — pick one literal and reuse it
    (e.g. `new DateTime(2024, 1, 1, 0, 0, 0, DateTimeKind.Utc)`).

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

### Generated tree (relative to scratch `gen-old/csharp/`)

```
{{GENERATED_TREE}}
```

## Output contract

A single file containing one C# source. Top-level structure:

```csharp
using System;
using Newtonsoft.Json;
using Idltest.Dtofields;  // … and other generated namespaces …

namespace IdlRegress.CSharpDriver {
  public static class Program {
    private static readonly JsonSerializerSettings S = new JsonSerializerSettings {
      NullValueHandling = NullValueHandling.Ignore,
      Formatting = Formatting.None,
    };

    public static int Main(string[] args) {
      Emit("idltest.dtofields.IntPair.Struct", "zero",  new IntPairStruct(0, 0));
      // … other fixtures …
      return 0;
    }

    private static void Emit(string wireId, string scenario, object v) {
      var json = JsonConvert.SerializeObject(v, S);
      Console.WriteLine($"{wireId}\t{scenario}\t{json}");
    }
  }
}
```

Use `Console.WriteLine` for stdout. Any diagnostic prints belong on
`Console.Error.WriteLine`.

Note on C# namespace casing: the generated C# capitalizes namespace
segments (`Idltest.Dtofields`) and type names (`IntPairStruct`), but the
on-wire `wireId` is always the dotted lowercase form
(`idltest.dtofields.IntPair.Struct`) — that's the value of
`RTTI_FULLCLASSNAME`. Do not confuse the two.

Do NOT introduce randomness, side effects, or non-trivial logic. Pure
construction + serialize + WriteLine. The harness will canonicalize the
JSON (re-sort keys, re-emit no-whitespace), so key ordering inside the
emitted JSON is irrelevant for the diff — but the fixture set itself must
be deterministic.

Do NOT emit any line that is not in the `wireId\tscenario\tjson` shape.

Return only the file contents — no commentary, no surrounding markdown fence.
