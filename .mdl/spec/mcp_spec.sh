# ShellSpec note: `When run` (not `When call`) is required for builder functions.
# `When call` disables set -e (errexit) — the function runs in an ignored errexit
# context (via &&:) so compilation failures silently pass. `When run` executes in
# a subshell where set -e works and exit is safely caught.
# See: https://github.com/shellspec/shellspec/blob/master/docs/references.md
#
# MCP bridge compile-regression.
#
# For a domain with emitMcpBridge=true, the emitted `<Svc>Mcp` pointer object
# plus `mcp/<Svc>.mcp.json` resource are platform-neutral and land in the SHARED
# sourceset. This spec generates an SBT project in each of the TWO manifest modes
# idealingua supports and asserts the emitted MCP code compiles:
#   (1) enableScalaJs=false — JVM-only crossproject; `sbt compile` exits 0.
#   (2) enableScalaJs=true  — cross JVM+JS; `sbt <id>JVM/compile` and
#       `sbt <id>JS/compile` both exit 0 (platform-neutral; the JS side needs no
#       http4s/cats.effect and does NOT compile the JVM-only interpreter).
#
# A third example validates the emitted tool schemas against the JSON Schema
# draft 2020-12 metaschema over a corpus whose service takes a cross-domain
# interface parameter (the case that previously emitted an empty `oneOf`). All
# examples share one sbt server, so keep them in a single spec/action: running
# the schema check as a separate concurrent mdl action contends on that server.

Describe 'Scala MCP bridge (emitMcpBridge=true)'
  Include ./.mdl/lib/builders.sh

  setup() {
    # Publish the idealingua runtime artifacts (incl. their _sjs1_ JS variants,
    # because the build cross-builds to JS) so the generated SBT project can
    # resolve idealingua-v1-runtime-rpc-scala / idealingua-v1-model in both modes.
    sbt --batch --no-server -Dsbt.server.forcestart=true -no-colors "$VERSION_COMMAND" publishLocal
  }

  BeforeAll 'setup'

  # main-tests is the only bundled corpus that defines services (so a `<Svc>Mcp`
  # data object is actually emitted under emitMcpBridge=true).
  domain="./idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests"

  # mcpschema: a minimal corpus whose service takes a cross-domain interface
  # parameter — the case that previously rendered an empty `oneOf`.
  schema_domain="./idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/mcpschema"

  It "compiles emitted MCP DATA in JVM-only mode (enableScalaJs=false)"
    When run test_scala_mcp_jvm_prj "$domain"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End

  It "compiles emitted MCP DATA for both JVM and JS targets (enableScalaJs=true)"
    When run test_scala_mcp_cross_prj "$domain"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End

  It "emits metaschema-valid tool schemas for a cross-domain interface parameter"
    When run test_scala_mcp_schema_valid "$schema_domain"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End
End
