#!/usr/bin/env bash


set -euo pipefail

function run-publish-npm() {
  validate_publishing || exit 0

  ./idealingua-v1/idealingua-v1-runtime-rpc-typescript/src/npmjs/publish.sh || exit 1
}
