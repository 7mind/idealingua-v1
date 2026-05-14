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

Cross-version regression harness: runs the high-value selftest cells
(`impl9-vs-head-*` and `v1419-vs-head-compat-*` across all three languages).
Catches wire-format and constructor-order regressions like F-DTO1-fieldorder
that the unit suite cannot surface.

The full-corpus `sanity-scala` cell is intentionally NOT included here:
its sample-app cache depends on an LLM-driven regeneration step keyed by
the IDL sha, and the in-tree cache lags the corpus on `wip/necromancy`.
The two cell families that ARE included use stable sample-app caches.

```bash
dep action.gen

source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"
ensure_numcpu

cells=(
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
echo "regression-harness summary (3 langs × 2 cells = 6 cells)"
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
dep action.test-ts
dep action.test-cs

ret success:bool=true
```
