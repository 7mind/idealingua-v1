#!/usr/bin/env bash

function test_scala_sbt_prj() {
  set -euo pipefail
  echo "IDL TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :scala -d layout=SBT -d sbt.scalaVersions=$SCALA_VERSION"

  pushd .
  cd "$tmpdir/scala"
  [[ -f build.sbt ]] || exit 1
  sbt clean compile
  popd
  echo "IDL TEST DONE: $1"
}

function test_scala_plain_prj() {
  set -euo pipefail
  echo "IDL TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :scala -d layout=PLAIN"

  pushd .
  cd "$tmpdir/scala"
  files=$(find . -name '*.scala' -print0 | xargs -0)

  mkdir ./target
  cs \
    launch \
    scalac:"${SCALA_VERSION}" \
    -- \
    -deprecation \
    -opt-warnings:_ \
    -d \
    ./target \
    -classpath "$classpath" \
    ${files}
  popd
  echo "IDL TEST DONE: $1"
}

# MCP compile-regression helpers.
#
# A domain with emitMcpBridge=true emits, per service, a platform-neutral
# `<Svc>Mcp.scala` pointer object (McpServiceResource) into the SHARED sourceset
# `src/main/scala`, plus a `mcp/<Svc>.mcp.json` classpath resource into
# `src/main/resources`. Neither artifact may land under a `.jvm/` platform
# sourceset, and the emitted code must NOT pull in http4s/cats.effect (the
# JVM-only http4s interpreter is not part of generated output).

# Assert the emitted MCP pointer + resource landed in the SHARED sourceset
# (src/main/scala, src/main/resources) and NOT under any `.jvm/` platform dir.
function assert_mcp_in_shared_sourceset() {
  set -euo pipefail
  local scaladir="$1"

  local mcp_scala
  mcp_scala="$(find "$scaladir" -path '*/src/main/scala/*' -name '*Mcp.scala' | head -n1)"
  [[ -n "$mcp_scala" ]] || { echo "FAIL: no <Svc>Mcp.scala under a shared src/main/scala"; exit 1; }

  local mcp_json
  mcp_json="$(find "$scaladir" -path '*/src/main/resources/*' -name '*.mcp.json' | head -n1)"
  [[ -n "$mcp_json" ]] || { echo "FAIL: no <Svc>.mcp.json under a shared src/main/resources"; exit 1; }

  # The emitted MCP data must never be routed into a `.jvm/` platform sourceset.
  if find "$scaladir" -type d -name '*.jvm' -path '*src/main*' | grep -q . ; then
    if find "$scaladir" -path '*.jvm/*' \( -name '*Mcp.scala' -o -name '*.mcp.json' \) | grep -q . ; then
      echo "FAIL: MCP artifact found under a .jvm/ platform sourceset (must be shared)"; exit 1
    fi
  fi

  # The generated project must not require the http4s interpreter / cats.effect.
  if grep -Rq 'http4s\|cats-effect\|cats\.effect' "$scaladir/build.sbt"; then
    echo "FAIL: generated build.sbt references http4s/cats.effect (interpreter must not be emitted)"; exit 1
  fi

  echo "OK: $mcp_scala"
  echo "OK: $mcp_json"
}

# (1) enableScalaJs=false — JVM-only crossproject. Generate with
# emitMcpBridge=true, assert the MCP data is in the shared sourceset, then
# `sbt compile` must exit 0.
function test_scala_mcp_jvm_prj() {
  set -euo pipefail
  echo "IDL MCP JVM-ONLY TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".mcp-jvm.XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :scala -d layout=SBT -d sbt.scalaVersions=$SCALA_VERSION -d sbt.enableScalaJs=false -d emitMcpBridge=true"

  pushd .
  cd "$tmpdir/scala"
  [[ -f build.sbt ]] || exit 1
  assert_mcp_in_shared_sourceset "$tmpdir/scala"
  sbt clean compile
  popd
  echo "IDL MCP JVM-ONLY TEST DONE: $1"
}

# (2) enableScalaJs=true — cross JVM+JS. Generate with emitMcpBridge=true,
# assert the MCP code is in the shared sourceset, then compile BOTH the JVM
# and the JS target of the service-bearing crossproject (`<id>JVM/compile`
# and `<id>JS/compile`), confirming it is platform-neutral and the JS side
# needs no http4s/cats.effect.
function test_scala_mcp_cross_prj() {
  set -euo pipefail
  echo "IDL MCP CROSS JVM+JS TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".mcp-cross.XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :scala -d layout=SBT -d sbt.scalaVersions=$SCALA_VERSION -d sbt.enableScalaJs=true -d emitMcpBridge=true"

  pushd .
  cd "$tmpdir/scala"
  [[ -f build.sbt ]] || exit 1
  assert_mcp_in_shared_sourceset "$tmpdir/scala"

  # Resolve the crossproject that carries an emitted `<Svc>Mcp.scala`, then
  # derive its sbt JVM/JS sub-project ids (`<id>JVM` / `<id>JS`). The api
  # module dir is the project root: <id>/src/main/scala/.../<Svc>Mcp.scala.
  local mcp_scala apimod projid
  mcp_scala="$(find "$tmpdir/scala" -path '*/src/main/scala/*' -name '*Mcp.scala' | head -n1)"
  apimod="${mcp_scala%%/src/main/scala/*}"
  projid="$(basename "$apimod")"
  echo "MCP crossproject: $projid (JVM+JS)"

  sbt clean "${projid}JVM/compile" "${projid}JS/compile"
  popd
  echo "IDL MCP CROSS JVM+JS TEST DONE: $1"
}

function test_ts_yarn_prj() {
  set -euo pipefail
  echo "IDL TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :typescript -d layout=YARN"

  pushd .
  cd "$tmpdir/typescript"
  [[ -f tsconfig.json ]] || exit 1

  yarn install
  yarn build

  popd
  echo "IDL TEST DONE: $1"
}

function test_ts_plain_prj() {
  set -euo pipefail
  echo "IDL TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :typescript -d layout=PLAIN"

  pushd .
  cd "$tmpdir/typescript"
  [[ -f tsconfig.json ]] || exit 1

  yarn install
  tsc -p tsconfig.json

  popd
  echo "IDL TEST DONE: $1"
}

function test_cs_msbuild_prj() {
  set -euo pipefail
  echo "IDL TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :csharp -d layout=NUGET -d enableNUnit=true"

  pushd .
  cd "$tmpdir/csharp"

  msbuild /t:Restore /t:Rebuild

  for f in nuspec/*.nuspec; do
    nuget pack "$f"
  done

  popd
  echo "IDL TEST DONE: $1"
}

function test_cs_plain_prj() {
  set -euo pipefail
  echo "IDL TEST ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :csharp -d layout=PLAIN -d enableNUnit=true"

  pushd .
  cd "$tmpdir/csharp"

  csc -target:library -out:tests.dll "-recurse:\\*.cs" $refs
  cp "$refsdir"/*.dll .
  nunit3-console tests.dll

  popd
  echo "IDL TEST DONE: $1"
}

# Validate every emitted MCP tool input/output schema against the JSON Schema
# draft 2020-12 metaschema. Each tool schema is self-contained (embeds its
# `$defs`), so an unresolved interface surfaces as an empty `oneOf`, which the
# metaschema rejects.
function assert_mcp_schemas_valid() {
  set -euo pipefail
  local scaladir="$1"
  local tmp
  tmp="$(mktemp -d)"
  local n=0
  local mcp service header schema idx rest name kind seq
  # One jq pass per file extracts every object-typed tool schema to its own
  # file, then a single check-jsonschema run validates them all. Validation is
  # trivially cheap per schema; the cost is interpreter startup, so spawning
  # once over many files beats once per schema by orders of magnitude. -cr
  # emits the header string raw (real tabs) while keeping each schema object on
  # a single compact line.
  while IFS= read -r -d '' mcp; do
    service="$(basename "$mcp" .mcp.json)"
    while IFS= read -r header && IFS= read -r schema; do
      idx="${header%%$'\t'*}"; rest="${header#*$'\t'}"
      name="${rest%%$'\t'*}"; kind="${rest##*$'\t'}"
      printf -v seq '%05d' "$n"
      printf '%s\n' "$schema" >"$tmp/${seq}_${service}.${name}.${kind}.json"
      n=$((n + 1))
    done < <(jq -cr '
      (.tools // []) | to_entries[]
      | .key as $i | .value as $t
      | ($t.name // ("tool_" + ($i|tostring))) as $nm
      | ( {kind:"inputSchema", s:$t.inputSchema}, {kind:"outputSchema", s:$t.outputSchema} )
      | select(.s != null and (.s|type == "object"))
      | "\($i)\t\($nm)\t\(.kind)", (.s)
    ' "$mcp")
  done < <(find "$scaladir" -name '*.mcp.json' -print0)
  [[ "$n" -gt 0 ]] || {
    echo "FAIL: no MCP tool schemas found under $scaladir"
    exit 1
  }
  # xargs -0 packs paths up to the OS argv limit and splits into additional
  # invocations only when needed, so this stays correct at any corpus size.
  if ! find "$tmp" -name '*.json' -print0 | xargs -0 -r check-jsonschema --check-metaschema; then
    echo "FAIL: MCP schema validation failed (offending file named above as <seq>_<service>.<tool>.<kind>.json)"
    exit 1
  fi
  echo "OK: $n MCP tool schemas valid (draft 2020-12 metaschema)"
}

# Generate the MCP bridge output for a domain and metaschema-validate every
# emitted tool schema.
function test_scala_mcp_schema_valid() {
  set -euo pipefail
  echo "IDL MCP SCHEMA VALIDATION ABOUT TO START: $1"
  testname="$(basename "$1")"
  tmpdir="$(mktemp -d -t "$testname".mcp-schema.XXXXXXXX)"

  sbt "$VERSION_COMMAND ; idealingua-v1-compiler/run --root=$1 --source=$1/source --overlay=$1/overlay --target=$tmpdir :scala -d layout=SBT -d sbt.scalaVersions=$SCALA_VERSION -d sbt.enableScalaJs=false -d emitMcpBridge=true"

  assert_mcp_schemas_valid "$tmpdir/scala"
  echo "IDL MCP SCHEMA VALIDATION DONE: $1"
}
