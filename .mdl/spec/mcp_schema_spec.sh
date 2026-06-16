# Schema-validity regression: a cross-domain interface used as a method
# parameter must emit a draft-2020-12-valid tool schema. Such a foreign
# interface previously rendered an empty `oneOf` (no implementors resolvable in
# the consuming domain), which strict consumers reject.
#
# See mcp_spec.sh for the `When run` (not `When call`) rationale.

Describe 'MCP tool schema validity (draft 2020-12)'
  Include ./.mdl/lib/builders.sh

  domain="./idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/mcpschema"

  It "emits metaschema-valid tool schemas for a cross-domain interface parameter"
    When run test_scala_mcp_schema_valid "$domain"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End
End
