#!/usr/bin/env bash
set -euo pipefail

function run-test-ts() {
shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/ts ./.mobala/steps/spec/ts_spec.sh
}