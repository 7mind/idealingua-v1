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

# action: test-pb

Protobuf transpiler integration tests.

```bash
dep action.gen

source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"
ensure_numcpu

mkdir -p ./target/spec-reports/pb
shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/pb ./.mdl/spec/pb_spec.sh

ret success:bool=true
```

# action: test

Run the full integration test suite.

```bash
dep action.test-scala
dep action.test-ts
dep action.test-cs
dep action.test-pb

ret success:bool=true
```
