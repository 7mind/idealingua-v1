#!/usr/bin/env bash
set -euo pipefail

function run-test-cs() {
  shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/cs ./.mobala/steps/spec/cs_spec.sh
}