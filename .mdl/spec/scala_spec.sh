# ShellSpec note: `When run` (not `When call`) is required for builder functions.
# `When call` disables set -e (errexit) — the function runs in an ignored errexit
# context (via &&:) so compilation failures silently pass. `When run` executes in
# a subshell where set -e works and exit is safely caught.
# See: https://github.com/shellspec/shellspec/blob/master/docs/references.md

Describe 'Scala transpiler'
  Include ./.mdl/lib/builders.sh

  setup() {
    sbt --batch --no-server -Dsbt.server.forcestart=true -no-colors "$VERSION_COMMAND" publishLocal
    export classpath="$(TERM=dumb sbt --batch --no-server -Dsbt.server.forcestart=true -no-colors --error "$VERSION_COMMAND" "export idealingua-v1-compiler/runtime:fullClasspath")"
  }

  BeforeAll 'setup'

  Parameters:dynamic
    while read line; do
      %data "$line"
    done <<< "$(find ./idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs -maxdepth 2 -mindepth 2 -type d -name source -printf '%h\n' | sort -u)"
  End

  It "builds Scala SBT project in $1"
    When run test_scala_sbt_prj "$1"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End

  It "builds Scala Plain project in $1"
    When run test_scala_plain_prj "$1"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End
End


Describe 'Scala transpiler (scala-only case)'
  Include ./.mdl/lib/builders.sh


  setup() {
    export classpath="$(TERM=dumb sbt --batch --no-server -Dsbt.server.forcestart=true -no-colors --error "$VERSION_COMMAND" "export idealingua-v1-compiler/runtime:fullClasspath")"
  }

  BeforeAll 'setup'

  Parameters:dynamic
    while read line; do
      %data "$line"
    done <<< "$(find ./idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs-special -name 'scala-*' -maxdepth 1 -mindepth 1  -type d)"
  End

  It "builds Scala Plain project in $1"
    When run test_scala_plain_prj "$1"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End
End
