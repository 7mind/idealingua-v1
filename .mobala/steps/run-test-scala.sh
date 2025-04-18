#!/usr/bin/env bash
set -xeuo pipefail

function run-test-scala() {
  unset _JAVA_OPTIONS
  shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/scala ./.mobala/steps/spec/scala_spec.sh
}