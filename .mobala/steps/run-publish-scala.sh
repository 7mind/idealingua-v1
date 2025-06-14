#!/usr/bin/env bash


set -euo pipefail

function run-publish-scala() {
  validate_publishing || exit 0
  
  if [[ ! -f "$SONATYPE_SECRET" ]] ; then 
    echo "SONATYPE_SECRET=$SONATYPE_SECRET is not a file"
    exit 0
  fi
  
  echo "PUBLISH SCALA LIBRARIES..."
  
  if [[ "$CI_BRANCH" == "develop" ]] ; then
    sbt -batch -no-colors -v \
      "$VERSION_COMMAND clean" \
      "$VERSION_COMMAND package" \
      "$VERSION_COMMAND publishSigned"
  else
    sbt -batch -no-colors -v \
      "$VERSION_COMMAND clean" \
      "$VERSION_COMMAND package" \
      "$VERSION_COMMAND publishSigned" \
      sonaUpload sonaRelease || exit 1
  fi

}
