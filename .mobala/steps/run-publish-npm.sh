#!/usr/bin/env bash


set -euo pipefail

function run-publish-npm() {
  validate_publishing || exit 0

  [[ -z "$TOKEN_NPM" ]] && echo "Missing TOKEN_NPM" && exit 0

  echo "//registry.npmjs.org/:_authToken=${TOKEN_NPM}" > ~/.npmrc
  npm whoami

  ./idealingua-v1/idealingua-v1-runtime-rpc-typescript/src/npmjs/publish.sh || exit 1
}
