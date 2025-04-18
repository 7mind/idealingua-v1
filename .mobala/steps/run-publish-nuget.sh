#!/usr/bin/env bash


set -euo pipefail

function run-publish-nuget() {
  validate_publishing || exit 0
  
  [[ -z "$TOKEN_NUGET" ]] && echo "Missing TOKEN_NUGET" && exit 0
  
  ./idealingua-v1/idealingua-v1-runtime-rpc-csharp/src/main/nuget/publish.sh || exit 1
}