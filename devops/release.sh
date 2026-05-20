#!/usr/bin/env bash

set -xe

./sbtgen.sc --js

git add . || true

sbt "++2.13" "release with-defaults"
