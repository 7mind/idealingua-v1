# Idealingua Test Actions

Test orchestration for idealingua-v1.

# action: test-scala

Scala transpiler integration tests (SBT and plain layouts).

```bash
dep action.gen

source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"
ensure_numcpu

mkdir -p ./target/spec-reports/scala
shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/scala ./.mdl/spec/scala_spec.sh

ret success:bool=true
```

# action: test-scala-mcp

MCP bridge compile-regression: for a domain with emitMcpBridge=true, assert the
emitted platform-neutral `<Svc>Mcp` pointer object + `mcp/<Svc>.mcp.json` land in
the SHARED sourceset and compile in BOTH manifest modes — JVM-only
(enableScalaJs=false) and cross JVM+JS (enableScalaJs=true). Exactly two
examples; there is no JS-only mode.

```bash
dep action.gen

source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"
ensure_numcpu

mkdir -p ./target/spec-reports/scala-mcp
shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/scala-mcp ./.mdl/spec/mcp_spec.sh

ret success:bool=true
```

# action: test-ts

TypeScript transpiler integration tests for Yarn and plain layouts.

```bash
dep action.gen

source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"
ensure_numcpu

mkdir -p ./target/spec-reports/ts
shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/ts ./.mdl/spec/ts_spec.sh

ret success:bool=true
```

# action: test-cs

C# transpiler integration tests for MSBuild and NuGet layouts.

```bash
dep action.gen

source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"
ensure_numcpu

mkdir -p ./target/spec-reports/cs
shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/cs ./.mdl/spec/cs_spec.sh

ret success:bool=true
```

# action: test-regression

Cross-version regression harness: runs the selftest cells that catch
wire-format and constructor-order regressions like F-DTO1-fieldorder
that the unit suite cannot surface. Three cell families across all three
languages:

  - `sanity-{scala,typescript-full,csharp-full}` — HEAD vs HEAD against
    the full main-tests corpus (smoke: sample app builds and produces
    byte-stable JSON).
  - `impl9-vs-head-{scala,typescript,csharp}` — git:ea697f5 (IMPL-9
    default-flip era) vs HEAD against `dtofields-only`.
  - `v1419-vs-head-compat-{scala,typescript,csharp}` — git:v1.4.19 vs
    HEAD against the v1.4.19-compatible subset of main-tests. Catches
    cross-release wire-format regressions.

```bash
dep action.gen

source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"
ensure_numcpu

cells=(
  "sanity-scala"
  "sanity-typescript-full"
  "sanity-csharp-full"
  "impl9-vs-head-scala"
  "impl9-vs-head-typescript"
  "impl9-vs-head-csharp"
  "v1419-vs-head-compat-scala"
  "v1419-vs-head-compat-typescript"
  "v1419-vs-head-compat-csharp"
)

overall=0
declare -a results=()
for cell in "${cells[@]}"; do
  echo "==============================="
  echo "regression cell: $cell"
  echo "==============================="
  if ./regression-harness/selftest.sh "$cell"; then
    results+=("PASS  $cell")
  else
    rc=$?
    results+=("FAIL($rc)  $cell")
    overall=1
  fi
done

echo
echo "=============================================================="
echo "regression-harness summary (3 langs × 3 cell families = 9 cells)"
echo "=============================================================="
for r in "${results[@]}"; do
  echo "  $r"
done

if [[ "$overall" -ne 0 ]]; then
  exit "$overall"
fi

ret success:bool=true
```

# action: test

Run the full integration test suite.

```bash
dep action.test-scala
dep action.test-scala-mcp
dep action.test-ts
dep action.test-cs

ret success:bool=true
```
