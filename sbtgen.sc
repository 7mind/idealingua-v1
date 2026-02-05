#!/bin/sh

scala-cli \
  --java-home "$JAVA_HOME" \
  --server=false \
  --main-class Idealingua \
  ./sbtgen/ \
  -- "$@"
