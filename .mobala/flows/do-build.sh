#!/usr/bin/env bash

set -euo pipefail

function do-build() {
  step_run_cond run-gen
  step_run_cond run-coverage
  
  step_run_cond run-test-scala  
  step_run_cond run-test-pb  
  step_run_cond run-test-ts
  step_run_cond run-test-cs
  
  step_run_cond run-publish-scala
  step_run_cond run-publish-npm
  step_run_cond run-publish-nuget
}
