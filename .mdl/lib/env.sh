#!/usr/bin/env bash

set -euo pipefail

function ensure_ci_env() {
  export CI_BUILD_UNIQ_SUFFIX="${CI_BUILD_UNIQ_SUFFIX:-$(date +%s)}"
}

function ensure_numcpu() {
  if [[ -n "${NUMCPU:-}" ]]; then
    return
  fi

  if command -v nproc >/dev/null 2>&1; then
    export NUMCPU="$(nproc)"
    return
  fi

  if command -v sysctl >/dev/null 2>&1; then
    export NUMCPU="$(sysctl -n hw.ncpu)"
    return
  fi

  export NUMCPU=4
}

function resolve_scala_version() {
  local requested="$1"
  local scala212
  local scala213
  local scala3

  scala212=$(grep 'val scala212 ' sbtgen/Deps.scala | sed -r 's/.*"(.*)".*/\1/')
  scala213=$(grep 'val scala213 ' sbtgen/Deps.scala | sed -r 's/.*"(.*)".*/\1/')
  scala3=$(grep 'val scala300 ' sbtgen/Deps.scala | sed -r 's/.*"(.*)".*/\1/')

  case "$requested" in
    "" )
      echo "$scala213"
      ;;
    2.12|2.12.* )
      echo "$scala212"
      ;;
    2.13|2.13.* )
      echo "$scala213"
      ;;
    3|3.* )
      echo "$scala3"
      ;;
    * )
      echo "$requested"
      ;;
  esac
}

function prepare_project_version() {
  local raw_version
  raw_version=$(sed -n 's/.*"\(.*\)".*/\1/p' version.sbt | head -n 1)
  local suffix="build.${CI_BUILD_UNIQ_SUFFIX}"
  export PROJECT_VERSION
  PROJECT_VERSION=$(echo "$raw_version" | sed -E "s/SNAPSHOT/${suffix}/")
}

function prepare_scala_env() {
  local requested="$1"
  local resolved

  ensure_ci_env
  resolved=$(resolve_scala_version "$requested")

  export SCALA_VERSION="$resolved"
  export VERSION_COMMAND="++ ${SCALA_VERSION}"
}

function prepare_build_env() {
  local requested_scala="$1"
  ensure_ci_env
  ensure_numcpu
  prepare_scala_env "$requested_scala"
  prepare_project_version
}

function validate_publishing() {
  if [[ "${CI_PULL_REQUEST:-false}" == "true" ]]; then
    echo "Publishing not allowed on pull requests"
    return 1
  fi

  if [[ "${CI_BRANCH:-}" != "develop" && ! "${CI_BRANCH_TAG:-}" =~ ^v ]]; then
    echo "Publishing not allowed (CI_BRANCH=${CI_BRANCH:-}, CI_BRANCH_TAG=${CI_BRANCH_TAG:-})"
    return 1
  fi

  return 0
}
