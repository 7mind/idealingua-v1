#!/usr/bin/env bash
# Self-tests for idl-regress.
#
# Modes:
#   sanity            — HEAD vs HEAD against main-tests (Scala). Zero divergences expected.
#   impl9-vs-head     — git:impl9-baseline (IMPL-9 default-flip) vs HEAD against dtofields-only.
#                       Compared to selftest-expectations/impl9-vs-head.scala.json.
#   sanity-ts         — HEAD vs HEAD against dtofields-only (TypeScript). Zero divergences expected.
#   impl9-vs-head-ts  — git:impl9-baseline vs HEAD against dtofields-only (TypeScript).
#                       Compared to selftest-expectations/impl9-vs-head.typescript.json.
#   sanity-cs         — HEAD vs HEAD against dtofields-only (C#). Zero divergences expected.
#   impl9-vs-head-cs  — git:impl9-baseline vs HEAD against dtofields-only (C#).
#                       Compared to selftest-expectations/impl9-vs-head.csharp.json.
#   matrix            — run all six cells above (3 langs × {sanity, impl9-vs-head})
#                       sequentially. Prints a one-line PASS/FAIL summary per cell
#                       and an overall summary table at the end. Exit 0 iff all
#                       six cells passed.
#   impl9-vs-head-scala       — alias for impl9-vs-head (matrix-aligned name)
#   impl9-vs-head-typescript  — alias for impl9-vs-head-ts
#   impl9-vs-head-csharp      — alias for impl9-vs-head-cs
#   sanity-scala              — alias for sanity
#   sanity-typescript         — alias for sanity-ts
#   sanity-csharp             — alias for sanity-cs
#   sanity-broad              — HEAD vs HEAD against the broad corpus (Scala).
#                               Zero divergences expected.
#   v1419-vs-head-broad       — git:v1.4.19 vs HEAD against the broad corpus
#                               (Scala). 13 fixtures across DTO + Enum +
#                               Identifier. Compared to
#                               selftest-expectations/v1419-vs-head.broad.scala.json
#                               (empty per wire-format-invariant policy).
#
# All extra args after the mode are forwarded to idl-regress (matrix mode
# does NOT forward args to the cell invocations).
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HARNESS_DIR="$REPO_ROOT/regression-harness"
MAIN_TESTS="$REPO_ROOT/idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests"
# Minimal in-tree corpus used by impl9-vs-head: just `idltest.dtofields.IntPair`
# + `WHPair`. Picked to compile cleanly under BOTH the IMPL-9 idlc and HEAD;
# the full `main-tests/` tree at HEAD contains `blobtest.domain` which trips
# IMPL-9's Scala backend (no TBLOB -> Array[Byte] mapping pre-F5-fix). See the
# M2 commit body for the cross-version audit finding.
DTOFIELDS_ONLY="$HARNESS_DIR/selftest-corpus/dtofields-only"
# Broadened in-tree corpus: dtofields + enums + identifiers. Covers DTO
# (mixin composition), Enum (long + short syntax), and Identifier (uid +
# i64 + str fields). Designed to compile cleanly under BOTH v1.4.19
# (the most recent published release) and HEAD on wip/necromancy. Used by
# the `v1419-vs-head-*` cells below — a 13-fixture cross-release regression
# probe vs the 3-fixture dtofields-only probe.
BROAD="$HARNESS_DIR/selftest-corpus/broad"
# X1 corpus: subset of main-tests that BOTH v1.4.19 and HEAD idlc compile (excludes
# the 10 R2-added coverage/* fixtures + idltest/blobtest.domain which exercises
# TBLOB → Array[Byte] not implemented in v1.4.19's Scala backend).
V1419_COMPAT="$HARNESS_DIR/selftest-corpus/main-tests-v1419-compat"

mode="${1:-sanity}"
shift || true

case "$mode" in
  matrix)
    cells=(
      "sanity-scala"
      "impl9-vs-head-scala"
      "v1419-vs-head-compat-scala"
      "sanity-typescript"
      "sanity-typescript-full"
      "impl9-vs-head-typescript"
      "v1419-vs-head-compat-typescript"
      "sanity-csharp"
      "sanity-csharp-full"
      "impl9-vs-head-csharp"
      "v1419-vs-head-compat-csharp"
    )
    declare -a results=()
    overall=0
    for cell in "${cells[@]}"; do
      echo "==============================="
      echo "matrix cell: $cell"
      echo "==============================="
      if "$0" "$cell"; then
        results+=("PASS  $cell")
      else
        rc=$?
        results+=("FAIL($rc)  $cell")
        overall=1
      fi
    done
    echo
    echo "=============================================================="
    echo "matrix summary (sanity + impl9-vs-head + v1419-vs-head-compat + TS/CS full = 11 cells)"
    echo "=============================================================="
    for r in "${results[@]}"; do
      echo "  $r"
    done
    exit "$overall"
    ;;
  sanity-scala) exec "$0" sanity "$@" ;;
  sanity-typescript) exec "$0" sanity-ts "$@" ;;
  sanity-csharp) exec "$0" sanity-cs "$@" ;;
  sanity-typescript-full)
    if [[ ! -d "$MAIN_TESTS/source" ]]; then
      echo "selftest: missing corpus at $MAIN_TESTS/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$MAIN_TESTS" \
      --old self \
      --new self \
      --lang typescript \
      "$@"
    ;;
  sanity-csharp-full)
    if [[ ! -d "$MAIN_TESTS/source" ]]; then
      echo "selftest: missing corpus at $MAIN_TESTS/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$MAIN_TESTS" \
      --old self \
      --new self \
      --lang csharp \
      "$@"
    ;;
  impl9-vs-head-scala) exec "$0" impl9-vs-head "$@" ;;
  impl9-vs-head-typescript) exec "$0" impl9-vs-head-ts "$@" ;;
  impl9-vs-head-csharp) exec "$0" impl9-vs-head-cs "$@" ;;
  sanity)
    if [[ ! -d "$MAIN_TESTS/source" ]]; then
      echo "selftest: missing corpus at $MAIN_TESTS/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$MAIN_TESTS" \
      --old self \
      --new self \
      --lang scala \
      "$@"
    ;;
  impl9-vs-head)
    if [[ ! -d "$DTOFIELDS_ONLY/source" ]]; then
      echo "selftest: missing corpus at $DTOFIELDS_ONLY/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$DTOFIELDS_ONLY" \
      --old "git:impl9-baseline" \
      --new self \
      --lang scala \
      "$@"
    ;;
  sanity-ts)
    if [[ ! -d "$DTOFIELDS_ONLY/source" ]]; then
      echo "selftest: missing corpus at $DTOFIELDS_ONLY/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$DTOFIELDS_ONLY" \
      --old self \
      --new self \
      --lang typescript \
      "$@"
    ;;
  impl9-vs-head-ts)
    if [[ ! -d "$DTOFIELDS_ONLY/source" ]]; then
      echo "selftest: missing corpus at $DTOFIELDS_ONLY/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$DTOFIELDS_ONLY" \
      --old "git:impl9-baseline" \
      --new self \
      --lang typescript \
      "$@"
    ;;
  sanity-cs)
    if [[ ! -d "$DTOFIELDS_ONLY/source" ]]; then
      echo "selftest: missing corpus at $DTOFIELDS_ONLY/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$DTOFIELDS_ONLY" \
      --old self \
      --new self \
      --lang csharp \
      "$@"
    ;;
  impl9-vs-head-cs)
    if [[ ! -d "$DTOFIELDS_ONLY/source" ]]; then
      echo "selftest: missing corpus at $DTOFIELDS_ONLY/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$DTOFIELDS_ONLY" \
      --old "git:impl9-baseline" \
      --new self \
      --lang csharp \
      "$@"
    ;;
  sanity-broad)
    if [[ ! -d "$BROAD/source" ]]; then
      echo "selftest: missing corpus at $BROAD/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$BROAD" \
      --old self \
      --new self \
      --lang scala \
      "$@"
    ;;
  v1419-vs-head-broad|v1419-vs-head-broad-scala)
    if [[ ! -d "$BROAD/source" ]]; then
      echo "selftest: missing corpus at $BROAD/source" >&2
      exit 2
    fi
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$BROAD" \
      --old "git:v1.4.19" \
      --new self \
      --lang scala \
      "$@"
    ;;
  v1419-vs-head-compat-scala|v1419-vs-head-compat-typescript|v1419-vs-head-compat-csharp)
    if [[ ! -d "$V1419_COMPAT/source" ]]; then
      echo "selftest: missing corpus at $V1419_COMPAT/source" >&2
      exit 2
    fi
    case "$mode" in
      v1419-vs-head-compat-scala)      lang=scala ;;
      v1419-vs-head-compat-typescript) lang=typescript ;;
      v1419-vs-head-compat-csharp)     lang=csharp ;;
    esac
    cd "$REPO_ROOT"
    exec "$HARNESS_DIR/idl-regress" \
      --project "$V1419_COMPAT" \
      --old "git:v1.4.19" \
      --new self \
      --lang "$lang" \
      "$@"
    ;;
  *)
    echo "selftest: unknown mode '$mode'" >&2
    echo "  cells: sanity | impl9-vs-head | sanity-ts | impl9-vs-head-ts | sanity-cs | impl9-vs-head-cs" >&2
    echo "  aliases: sanity-{scala,typescript,csharp} | impl9-vs-head-{scala,typescript,csharp}" >&2
    echo "  matrix: matrix  (runs all six cells, summarizes)" >&2
    exit 126
    ;;
esac
