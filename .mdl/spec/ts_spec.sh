Describe 'Typescript transpiler'
  Include ./.mdl/lib/builders.sh

  Parameters:dynamic
    while read line; do
      %data "$line"
    done <<< "$(find ./idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs -maxdepth 2 -mindepth 2 -type d -name source -printf '%h\n' | sort -u)"
  End

  It "builds Typescript Yarn project in $1"
    When run test_ts_yarn_prj "$1"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End

  It "builds Typescript Plain project in $1"
    When run test_ts_plain_prj "$1"
    The status should be success
    The output should match pattern '*'
    The stderr should match pattern '*'
  End
End
