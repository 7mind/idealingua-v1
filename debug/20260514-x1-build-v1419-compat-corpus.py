#!/usr/bin/env python3
"""X1: Create main-tests-v1419-compat corpus subset under regression-harness/selftest-corpus/.

Strategy:
  - source/ tree mirrors main-tests/source/ but EXCLUDES coverage/.
  - Per-file symlinks point back at main-tests source files.
  - Sample apps (.scala, .ts, .cs) are filtered copies of the main-tests
    R3 sample apps with all `coverage.*` wireId lines removed.
  - meta sidecars carry a freshly computed idl_sha256 over the new tree
    (matches Harness.scala:hashIdlTree).

Run from repo root.
"""
from __future__ import annotations
import hashlib
import os
import pathlib
import re
import shutil
import sys


REPO_ROOT  = pathlib.Path(__file__).resolve().parents[1]
MAIN_TESTS = REPO_ROOT / "idealingua-v1" / "idealingua-v1-test-defs" / "src" / "main" / "resources" / "defs" / "main-tests"
COMPAT_DIR = REPO_ROOT / "regression-harness" / "selftest-corpus" / "main-tests-v1419-compat"

EXCLUDED_TOPLEVEL_DIR = "coverage"  # All R2-added fixtures live here
# Pre-R2 fixtures added/modified after v1.4.19 that trip v1.4.19's backends.
# idltest/blobtest.domain landed in commit 14c62ca (PR-02 F5-fix) and exercises
# TBLOB → Array[Byte] mapping, which v1.4.19's ScalaTypeConverter:80 does not
# implement (raises `IDLException: No type->generic mappings implemented yet`).
# The same omission likely applies to TS/C# backends, so we exclude it across all
# three legs uniformly.
EXCLUDED_RELPATHS: tuple[str, ...] = ("idltest/blobtest.domain",)
EXCLUDED_WIREID_PREFIXES: tuple[str, ...] = ("coverage.", "idltest.blobtest.")


def build_symlink_tree() -> None:
    """Re-create COMPAT_DIR/source/ with per-file symlinks to MAIN_TESTS/source/<non-coverage>/."""
    src_dst_root = COMPAT_DIR / "source"
    if src_dst_root.exists():
        shutil.rmtree(src_dst_root)
    src_dst_root.mkdir(parents=True)

    src_src_root = MAIN_TESTS / "source"
    for src_path in sorted(src_src_root.rglob("*")):
        if not src_path.is_file():
            continue
        rel = src_path.relative_to(src_src_root)
        if rel.parts and rel.parts[0] == EXCLUDED_TOPLEVEL_DIR:
            continue
        if str(rel) in EXCLUDED_RELPATHS:
            continue
        dst_path = src_dst_root / rel
        dst_path.parent.mkdir(parents=True, exist_ok=True)
        rel_target = os.path.relpath(src_path, dst_path.parent)
        dst_path.symlink_to(rel_target)
        print(f"  link {dst_path.relative_to(REPO_ROOT)} -> {rel_target}")


def hash_idl_tree(project: pathlib.Path) -> str:
    """Replicate Harness.scala:hashIdlTree byte-for-byte."""
    src = project / "source"
    files: list[pathlib.Path] = []
    for p in src.rglob("*"):
        if p.is_file() and (p.name.endswith(".domain") or p.name.endswith(".model")):
            files.append(p)
    # Also pick up `.model` files at <project>/ depth-1 (not walked recursively per Harness)
    for p in project.iterdir():
        if p.is_file() and p.name.endswith(".model"):
            files.append(p)
    md = hashlib.sha256()
    # Sort by absolute string path, just like the JVM (Path.toString() lexicographically).
    for p in sorted(files, key=lambda x: str(x)):
        rel = str(p.relative_to(project))
        md.update(rel.encode("utf-8"))
        md.update(b"\x00")
        md.update(p.read_bytes())
        md.update(b"\x00")
    return md.hexdigest()


def filter_scala_sample(src: str) -> tuple[str, int, int]:
    """Drop 4-line { val v: …; lines += s\"<wireId>\\t… } entries whose wireId starts with coverage."""
    lines = src.split("\n")
    out: list[str] = []
    i = 0
    dropped = 0
    kept = 0
    pat_open  = re.compile(r"^    \{$")
    pat_close = re.compile(r"^    \}$")
    pat_lines = re.compile(r'^      lines \+= s"([^"\\]*)\\t')
    while i < len(lines):
        line = lines[i]
        if pat_open.match(line) and i + 3 < len(lines):
            blk_end = i
            while blk_end < len(lines) and not pat_close.match(lines[blk_end]):
                blk_end += 1
            if blk_end < len(lines):
                # Found a complete block; scan for the wireId.
                wire = None
                for j in range(i, blk_end + 1):
                    m = pat_lines.match(lines[j])
                    if m:
                        wire = m.group(1)
                        break
                if wire is not None and any(wire.startswith(p) for p in EXCLUDED_WIREID_PREFIXES):
                    dropped += 1
                    i = blk_end + 1
                    continue
                else:
                    kept += 1
                    out.extend(lines[i : blk_end + 1])
                    i = blk_end + 1
                    continue
        out.append(line)
        i += 1
    return "\n".join(out), kept, dropped


def filter_ts_sample(src: str) -> tuple[str, int, int]:
    """Drop:
      - any out.push(\"coverage....\" …) line
      - any import { … } from './coverage/…'; line
      - any `# x-skip-positional  coverage.…` skip-list comment
    """
    lines = src.split("\n")
    out: list[str] = []
    kept_emit = 0
    dropped_emit = 0
    # Module prefixes to strip from `import { … } from './<modpath>/…';` statements.
    excluded_module_prefixes = ("./coverage/", "./idltest/blobtest/")

    def line_drops_wireid(line: str) -> tuple[bool, str | None]:
        m = re.match(r'^out\.push\("([^"]+)"', line)
        if m:
            w = m.group(1)
            return (any(w.startswith(p) for p in EXCLUDED_WIREID_PREFIXES), w)
        return (False, None)

    for line in lines:
        # Drop excluded module imports.
        if line.startswith("import { ") and any(f"'{mp}" in line for mp in excluded_module_prefixes):
            continue
        # Drop excluded out.push lines.
        drop, wire = line_drops_wireid(line)
        if drop:
            dropped_emit += 1
            continue
        if wire is not None:
            kept_emit += 1
        # Drop skip-list comment lines for excluded wireIds.
        if line.startswith("// # x-skip-positional ") and any(f" {p}" in line for p in EXCLUDED_WIREID_PREFIXES):
            continue
        out.append(line)
    return "\n".join(out), kept_emit, dropped_emit


def filter_cs_sample(src: str) -> tuple[str, int, int]:
    """Drop any line `            buf[\"coverage.…\"] = …;`"""
    lines = src.split("\n")
    out: list[str] = []
    kept = 0
    dropped = 0
    for line in lines:
        m = re.match(r'^\s*buf\["([^"]+)"\] = ', line)
        if m:
            wire = m.group(1)
            if any(wire.startswith(p) for p in EXCLUDED_WIREID_PREFIXES):
                dropped += 1
                continue
            else:
                kept += 1
        out.append(line)
    return "\n".join(out), kept, dropped


def write_sample_apps(idl_sha: str) -> None:
    src_meta = MAIN_TESTS / ".idl-regression"
    dst_meta = COMPAT_DIR / ".idl-regression"
    dst_meta.mkdir(parents=True, exist_ok=True)

    # Scala
    scala_in  = (src_meta / "sample_app.scala").read_text()
    scala_out, sk, sd = filter_scala_sample(scala_in)
    (dst_meta / "sample_app.scala").write_text(scala_out)
    (dst_meta / "sample_app.scala.meta").write_text(f"idl_sha256={idl_sha}\n")
    print(f"  scala: kept={sk} dropped={sd}")

    # TypeScript
    ts_in  = (src_meta / "sample_app.ts").read_text()
    ts_out, tk, td = filter_ts_sample(ts_in)
    (dst_meta / "sample_app.ts").write_text(ts_out)
    (dst_meta / "sample_app.ts.meta").write_text(f"idl_sha256={idl_sha}\n")
    print(f"  ts:    kept={tk} dropped={td}")

    # C#
    cs_in  = (src_meta / "sample_app.cs").read_text()
    cs_out, ck, cd = filter_cs_sample(cs_in)
    (dst_meta / "sample_app.cs").write_text(cs_out)
    (dst_meta / "sample_app.cs.meta").write_text(f"idl_sha256={idl_sha}\n")
    print(f"  cs:    kept={ck} dropped={cd}")


def main() -> None:
    print(f"main-tests:    {MAIN_TESTS}")
    print(f"compat-dir:    {COMPAT_DIR}")
    print("== building symlink tree ==")
    build_symlink_tree()

    print("== hashing IDL tree (Harness.scala-compatible) ==")
    idl_sha = hash_idl_tree(COMPAT_DIR)
    print(f"  idl_sha256: {idl_sha}")

    print("== writing filtered sample apps ==")
    write_sample_apps(idl_sha)

    print("done.")


if __name__ == "__main__":
    main()
