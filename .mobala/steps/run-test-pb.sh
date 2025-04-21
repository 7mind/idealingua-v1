#!/usr/bin/env bash
set -euo pipefail

function run-test-pb() {
shellspec --format documentation --jobs "${NUMCPU}" -o junit --reportdir ./target/spec-reports/pb ./.mobala/steps/spec/pb_spec.sh
}