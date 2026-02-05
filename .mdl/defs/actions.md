# Idealingua Build Actions

This file defines the mudyla build orchestration for idealingua-v1.

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

# action: gen

Generate sbt builds via sbtgen.

```bash
source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"

bash sbtgen.sc --js

ret success:bool=true
```

# action: coverage

Run Scala coverage and unit tests for the selected Scala version.

```bash
source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"

sbt_args=()
if [[ -n "${JAVA_HOME:-}" ]]; then
  sbt_args+=(--java-home "$JAVA_HOME")
fi

sbt -batch -no-colors -v \
  "${sbt_args[@]}" \
  "$VERSION_COMMAND clean" \
  coverage \
  "$VERSION_COMMAND Test/compile" \
  "$VERSION_COMMAND test" \
  "$VERSION_COMMAND coverageReport"

ret success:bool=true
```

# action: flake-refresh

Refresh flake inputs and regenerate the coursier lock with squish-find-the-brains.

```bash
source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"

nix flake update
squish-lockfile lockfile-config.json > deps.lock.json
git add flake.nix flake.lock deps.lock.json || true

ret success:bool=true
```

# action: validate-flake

Validate that `flake.nix` and lockfiles are up-to-date without mutating files.

```bash
source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"

tmp_lock="$(mktemp)"
squish-lockfile -n lockfile-config.json > "$tmp_lock"

if ! cmp -s "$tmp_lock" deps.lock.json; then
  echo "deps.lock.json is not up to date, run mdl :flake-refresh"
  exit 1
fi

nix flake check

ret success:bool=true
```

# action: publish-npm

Publish TypeScript runtime artifacts to npm (skips on pull requests or missing secrets).

```bash
source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"

if ! validate_publishing; then
  echo "Skipping npm publish"
  ret success:bool=true
fi

./idealingua-v1/idealingua-v1-runtime-rpc-typescript/src/npmjs/publish.sh

ret success:bool=true
```

# action: publish-nuget

Publish C# runtime artifacts to NuGet (skips on pull requests or missing secrets).

```bash
source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"

if ! validate_publishing; then
  echo "Skipping nuget publish"
  ret success:bool=true
fi

if [[ -z "${TOKEN_NUGET:-}" ]]; then
  echo "Missing TOKEN_NUGET, skipping nuget publish"
  ret success:bool=true
fi

./idealingua-v1/idealingua-v1-runtime-rpc-csharp/src/main/nuget/publish.sh

ret success:bool=true
```

# action: publish-scala

Publish Scala artifacts to Sonatype. Releases trigger `sonaUpload`/`sonaRelease`, develop builds publish snapshots.

```bash
source ./.mdl/lib/env.sh
prepare_build_env "${args.scala-version}"

if ! validate_publishing; then
  echo "Skipping scala publish"
  ret success:bool=true
fi

if [[ -z "${SONATYPE_SECRET:-}" || ! -f "${SONATYPE_SECRET}" ]]; then
  echo "SONATYPE_SECRET=${SONATYPE_SECRET:-} is not a file, skipping scala publish"
  ret success:bool=true
fi

if [[ "${CI_BRANCH:-}" == "develop" ]]; then
  sbt -batch -no-colors -v \
    "$VERSION_COMMAND clean" \
    "$VERSION_COMMAND package" \
    "$VERSION_COMMAND publishSigned"
else
  sbt -batch -no-colors -v \
    "$VERSION_COMMAND clean" \
    "$VERSION_COMMAND package" \
    "$VERSION_COMMAND publishSigned" \
    sonaUpload sonaRelease
fi

ret success:bool=true
```
