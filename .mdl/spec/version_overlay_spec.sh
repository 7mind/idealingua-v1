Describe 'Version overlay'
  Include ./.mdl/lib/builders.sh

  testroot="./idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/overlay-version"

  mk_version_json() {
    local file="$1"
    local release="$2"
    shift 2
    local qualifiers=""
    local first=true
    for kv in "$@"; do
      local key="${kv%%=*}"
      local val="${kv#*=}"
      if [ "$first" = true ]; then
        first=false
      else
        qualifiers="$qualifiers,"
      fi
      qualifiers="$qualifiers\"$key\":\"$val\""
    done
    cat > "$file" <<EOF
{
  "version": "1.2.3",
  "release": $release,
  "snapshotQualifiers": {$qualifiers}
}
EOF
  }

  extract_ts_version() {
    grep '"version"' "$1/typescript/package.json" | head -1 | sed 's/.*: *"\(.*\)".*/\1/'
  }

  extract_scala_version() {
    grep 'version :=' "$1/scala/build.sbt" | head -1 | sed 's/.*:= *"\(.*\)".*/\1/'
  }

  extract_cs_version() {
    local nuspec
    nuspec=$(find "$1/csharp" -name '*.nuspec' | head -1)
    grep '<version>' "$nuspec" | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/'
  }

  resolve_scala() {
    local requested="$1"
    local scala213
    local scala3
    scala213=$(grep 'val scala213 ' sbtgen/Deps.scala | sed -r 's/.*"(.*)".*/\1/')
    scala3=$(grep 'val scala300 ' sbtgen/Deps.scala | sed -r 's/.*"(.*)".*/\1/')
    case "$requested" in
      2.13|2.13.* ) echo "$scala213" ;;
      3|3.* )       echo "$scala3" ;;
      * )           echo "$requested" ;;
    esac
  }

  compile_lang() {
    local scala_ver="$1"
    local lang="$2"
    local tmpdir="$3"
    local version_json="$4"
    local layout_flags="${5:-}"
    local resolved
    resolved=$(resolve_scala "$scala_ver")
    local ver_cmd="++ $resolved"
    sbt --batch "$ver_cmd ; idealingua-v1-compiler/run --root=$testroot --source=$testroot/source --target=$tmpdir --overlay-version=$version_json :$lang $layout_flags" >&2
  }

  Parameters:dynamic
    %data "2.13"
    %data "3"
  End

  Describe 'release mode'
    It "produces bare version without qualifier for TypeScript (Scala $1)"
      run_test() {
        set -euo pipefail
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" true typescript=ignored
        compile_lang "$1" typescript "$tmpdir" "$vj" "-d layout=PLAIN"
        extract_ts_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should equal '1.2.3'
      The stderr should match pattern '*'
    End

    It "produces bare version without qualifier for Scala (Scala $1)"
      run_test() {
        set -euo pipefail
        local resolved
        resolved=$(resolve_scala "$1")
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" true scala=ignored
        compile_lang "$1" scala "$tmpdir" "$vj" "-d layout=SBT -d sbt.scalaVersions=$resolved"
        extract_scala_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should equal '1.2.3'
      The stderr should match pattern '*'
    End

    It "produces bare version without qualifier for CSharp (Scala $1)"
      run_test() {
        set -euo pipefail
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" true csharp=ignored
        compile_lang "$1" csharp "$tmpdir" "$vj" "-d layout=NUGET"
        extract_cs_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should equal '1.2.3'
      The stderr should match pattern '*'
    End
  End

  Describe 'snapshot with SNAPSHOT qualifier'
    It "appends SNAPSHOT for Scala (Scala $1)"
      run_test() {
        set -euo pipefail
        local resolved
        resolved=$(resolve_scala "$1")
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" false scala=SNAPSHOT
        compile_lang "$1" scala "$tmpdir" "$vj" "-d layout=SBT -d sbt.scalaVersions=$resolved"
        extract_scala_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should equal '1.2.3-SNAPSHOT'
      The stderr should match pattern '*'
    End

    It "appends SNAPSHOT for CSharp (Scala $1)"
      run_test() {
        set -euo pipefail
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" false csharp=SNAPSHOT
        compile_lang "$1" csharp "$tmpdir" "$vj" "-d layout=NUGET"
        extract_cs_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should equal '1.2.3-SNAPSHOT'
      The stderr should match pattern '*'
    End
  End

  Describe 'TypeScript timestamp qualifier'
    It "appends timestamp when no <commit> template (Scala $1)"
      run_test() {
        set -euo pipefail
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" false typescript=b.
        compile_lang "$1" typescript "$tmpdir" "$vj" "-d layout=PLAIN"
        extract_ts_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should match pattern '1.2.3-b.-[0-9]*'
      The stderr should match pattern '*'
    End
  End

  Describe 'commit hash qualifier'
    It "resolves <commit> for TypeScript without timestamp (Scala $1)"
      run_test() {
        set -euo pipefail
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" false 'typescript=b.<commit>'
        compile_lang "$1" typescript "$tmpdir" "$vj" "-d layout=PLAIN"
        extract_ts_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should match pattern '1.2.3-b.[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f]'
      The stderr should match pattern '*'
    End

    It "resolves <commit15> for Scala (Scala $1)"
      run_test() {
        set -euo pipefail
        local resolved
        resolved=$(resolve_scala "$1")
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" false 'scala=build.<commit15>'
        compile_lang "$1" scala "$tmpdir" "$vj" "-d layout=SBT -d sbt.scalaVersions=$resolved"
        extract_scala_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should match pattern '1.2.3-build.[0-9a-f]*'
      The stderr should match pattern '*'
    End

    It "resolves <commit15> for CSharp (Scala $1)"
      run_test() {
        set -euo pipefail
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" false 'csharp=build.<commit15>'
        compile_lang "$1" csharp "$tmpdir" "$vj" "-d layout=NUGET"
        extract_cs_version "$tmpdir"
      }
      When run run_test "$1"
      The status should be success
      The output should match pattern '1.2.3-build.[0-9a-f]*'
      The stderr should match pattern '*'
    End

    It "resolves <commit> (default 7) for Go without error (Scala $1)"
      run_test() {
        set -euo pipefail
        local tmpdir
        tmpdir="$(mktemp -d)"
        local vj="$tmpdir/version.json"
        mk_version_json "$vj" false 'go=build.<commit>'
        compile_lang "$1" go "$tmpdir" "$vj"
      }
      When run run_test "$1"
      The status should be success
      The stderr should match pattern '*'
    End
  End
End
