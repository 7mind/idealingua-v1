#!/usr/bin/env bash
set -euo pipefail

function run-test-scala() {
  shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/scala ./.mobala/steps/spec/scala_spec.sh
}