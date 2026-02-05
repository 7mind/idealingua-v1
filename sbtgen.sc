#!/bin/sh

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
cd "$script_dir/sbtgen"

scala-cli \
  --power \
  --java-home "$JAVA_HOME" \
  --server=false \
  --main-class Idealingua \
  . \
  -- "$@"
