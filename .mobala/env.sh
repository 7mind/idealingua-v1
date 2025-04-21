#!/usr/bin/env bash

set -euo pipefail
if [[ "${DO_VERBOSE}" == 1 ]] ; then set -x ; fi

set_gh_env

replacement="build.${CI_BUILD_UNIQ_SUFFIX}"
export IDEALINGUA_VERSION=$(cat version.sbt | sed -r 's/.*\"(.*)\".**/\1/' | sed -E "s/SNAPSHOT/${replacement}/")

export SCALA212=$(cat project/Deps.sc | grep 'val scala212 ' |  sed -r 's/.*\"(.*)\".**/\1/')
export SCALA213=$(cat project/Deps.sc | grep 'val scala213 ' |  sed -r 's/.*\"(.*)\".**/\1/')
export SCALA3=$(cat project/Deps.sc | grep 'val scala300 ' |  sed -r 's/.*\"(.*)\".**/\1/')

[[ -z "${SCALA_VERSION+x}" ]] && echo "Missing SCALA_VERSION" && exit 1

case $SCALA_VERSION in
  2.12) SCALA_VERSION="$SCALA212" ;;
  2.13) SCALA_VERSION="$SCALA213" ;;
  3) SCALA_VERSION="$SCALA3" ;;
  *) exit 1 ;;
esac

export SCALA_VERSION="$SCALA_VERSION"
export VERSION_COMMAND="++ $SCALA_VERSION"
export NUMCPU="$(nproc)"

set_jvm_options
debug_env

# this script receives all the CLI args from the main script and may decide which flows should be enabled
flow_enable do-build