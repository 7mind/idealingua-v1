# Idealingua Test Actions

Test orchestration for idealingua-v1.

# arguments

- `args.scala-version`: Scala version selector (`2.12`, `2.13`, `3`, or full version)
   - type: `string`
   - default: `"2.13"`

# environment

- `LANG=C.UTF-8`

## passthrough

- `HOME`
- `USER`
- `CI`
- `CI_BRANCH`
- `CI_BRANCH_TAG`
- `CI_PULL_REQUEST`
- `CI_BUILD_UNIQ_SUFFIX`
- `JAVA_HOME`
- `SCALA_VERSION`
- `SONATYPE_SECRET`
- `TOKEN_NPM`
- `TOKEN_NUGET`
- `ACTIONS_ID_TOKEN_REQUEST_URL`
- `ACTIONS_ID_TOKEN_REQUEST_TOKEN`
- `GITHUB_ACTIONS`
- `GITHUB_SERVER_URL`
- `GITHUB_REF`
- `GITHUB_SHA`
- `GITHUB_REPOSITORY`
- `GITHUB_REPOSITORY_ID`
- `GITHUB_REPOSITORY_OWNER_ID`
- `GITHUB_RUN_ATTEMPT`
- `GITHUB_RUN_ID`
- `GITHUB_WORKFLOW_REF`
- `GITHUB_WORKFLOW_SHA`
- `GITHUB_EVENT_NAME`

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
