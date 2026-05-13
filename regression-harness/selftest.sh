#!/usr/bin/env bash
# M1 sanity self-test: HEAD vs HEAD against the main-tests corpus.
# Expected outcome: zero divergences (or — on first run — exit code 3
# asking the operator to drop a sample_app.scala at the cache path).
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CORPUS="$REPO_ROOT/idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests"

if [[ ! -d "$CORPUS/source" ]]; then
  echo "selftest: missing corpus at $CORPUS/source" >&2
  exit 2
fi

cd "$REPO_ROOT"
exec "$REPO_ROOT/regression-harness/idl-regress" \
  --project "$CORPUS" \
  --old self \
  --new self \
  --lang scala \
  "$@"
