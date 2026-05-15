#!/usr/bin/env python3
"""
Sample-app generator for the idl-regress harness — Scala target.

Walks a generated idlc Scala tree (PLAIN layout), extracts every user-declared
case class / sealed trait, and emits a `sample_app.scala` driver that
constructs one fixture per non-internal type with deterministic literal
values. Output shape:

    <wireId>\\t<scenario>\\t<json>

Field-literal synthesis is type-string driven (primitive substring match for
Int / Long / String / etc.; container substring match for List / Set / Option
/ Map; everything else treated as a user-type reference and recursed on).
Cycle-breaking: a recursion-set tracks types in flight; a user-type field that
would re-enter the set falls back to an empty container if the field is
wrapped in opt/list/set/map, otherwise emits a one-level-deep nested instance
whose own recursive fields are empty.

Usage:

    python3 20260515-gen-sample-app.py <gen-scala-root> <out-file>

where <gen-scala-root> is e.g.
`target/regression-harness/<runId>/gen-old/scala/` and <out-file> is the
destination `sample_app.scala`.
"""

from __future__ import annotations

import os
import re
import sys
from dataclasses import dataclass, field
from typing import Dict, List, Optional, Set, Tuple

# ---------------------------------------------------------------------------
# Type model
# ---------------------------------------------------------------------------

@dataclass
class Field:
    name: str
    typ: str  # raw Scala type string (e.g. "Int", "Option[String]", "_root_.foo.Bar")

@dataclass
class TypeInfo:
    fqn: str            # dotted (no _root_ prefix), e.g. "com.example.foo.Bar"
    kind: str           # "dto" | "enum" | "adt" | "alias"
    pkg: str = ""       # declaring package + any nested object scopes (for short-name resolution)
    fields: List[Field] = field(default_factory=list)   # dto
    branches: List["AdtBranch"] = field(default_factory=list)  # adt
    enum_values: List[str] = field(default_factory=list)       # enum
    alias_target: str = ""                                     # alias

@dataclass
class AdtBranch:
    name: str           # simple name e.g. "Sr"
    value_type: str     # the wrapped value's type string

# ---------------------------------------------------------------------------
# Parser — pragmatic regex over normalised single-line text.
# ---------------------------------------------------------------------------

# We collapse multi-line case-class declarations onto one line before matching.
RE_PACKAGE = re.compile(r"^\s*package\s+([\w\.]+)", re.MULTILINE)

# A `final case class Name(args) extends ...` declaration. We capture name and
# argument list. The arg list is balanced-parens; we keep it lazy and recover
# via brace balancing after the match.
RE_CASE_CLASS = re.compile(r"\bfinal\s+case\s+class\s+(\w+)\s*\(", re.MULTILINE)
RE_SEALED_TRAIT = re.compile(r"\bsealed\s+trait\s+(\w+)\b[^{]*", re.MULTILINE)
RE_CASE_OBJECT = re.compile(r"\bcase\s+object\s+(\w+)\b[^{]*\bextends\s+([\w\.]+)", re.MULTILINE)
RE_OBJECT_BLOCK = re.compile(r"\bobject\s+(\w+)\b[^{]*\{", re.MULTILINE)

# Primitive / well-known type recognisers (operate on the bare type after
# stripping container wrappers).
PRIMITIVE_LITERAL = {
    "Int": "1",
    "Short": "1.toShort",
    "Byte": "1.toByte",
    "Long": "1L",
    "Float": "1.0f",
    "Double": "1.0",
    "Boolean": "true",
    "Char": "'a'",
    "String": "\"s1\"",
    "BigInt": "BigInt(1)",
    "BigDecimal": "BigDecimal(1)",
}

TIME_LITERAL = {
    "java.time.Instant": "java.time.Instant.parse(\"2024-01-01T00:00:00Z\")",
    "java.time.LocalDate": "java.time.LocalDate.parse(\"2024-01-01\")",
    "java.time.LocalDateTime": "java.time.LocalDateTime.parse(\"2024-01-01T00:00:00\")",
    "java.time.LocalTime": "java.time.LocalTime.parse(\"00:00:00\")",
    "java.time.OffsetDateTime": "java.time.OffsetDateTime.parse(\"2024-01-01T00:00:00Z\")",
    "java.time.ZonedDateTime": "java.time.ZonedDateTime.parse(\"2024-01-01T00:00:00Z[UTC]\")",
    "java.time.ZoneOffset": "java.time.ZoneOffset.UTC",
    "java.time.ZoneId": "java.time.ZoneId.of(\"UTC\")",
}

UUID_LITERAL = "java.util.UUID.fromString(\"00000000-0000-0000-0000-000000000000\")"


def normalise_type(t: str) -> str:
    """Strip whitespace and leading `_root_.` from a Scala type expression."""
    t = t.strip()
    if t.startswith("_root_."):
        t = t[len("_root_."):]
    return t


def find_matching_paren(text: str, open_idx: int) -> int:
    """Return the index of the closing paren matching the open at open_idx."""
    depth = 0
    i = open_idx
    while i < len(text):
        c = text[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def find_matching_brace(text: str, open_idx: int) -> int:
    depth = 0
    i = open_idx
    while i < len(text):
        c = text[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def split_top_level_commas(args: str) -> List[str]:
    """Split a Scala arg list on top-level commas (depth-aware on ()[]{})."""
    out: List[str] = []
    depth = 0
    buf: List[str] = []
    for ch in args:
        if ch in "([{":
            depth += 1
            buf.append(ch)
        elif ch in ")]}":
            depth -= 1
            buf.append(ch)
        elif ch == "," and depth == 0:
            out.append("".join(buf).strip())
            buf = []
        else:
            buf.append(ch)
    if buf:
        out.append("".join(buf).strip())
    return [p for p in out if p]


def parse_field_decl(decl: str) -> Optional[Field]:
    """Parse `name: Type` (also `name : Type = default`). Returns None if shapeless."""
    # Strip any `override`/`val`/`var` keyword prefixes; constructor args don't carry them
    # but be defensive.
    decl = decl.strip()
    # Strip default-value suffix
    eq = decl.find("=")
    if eq != -1:
        # Make sure '=' is at top level — but in case-class args we can keep simple.
        decl = decl[:eq].strip()
    colon = decl.find(":")
    if colon == -1:
        return None
    name = decl[:colon].strip()
    # In case of backticks
    if name.startswith("`") and name.endswith("`") and len(name) >= 2:
        name = name[1:-1]
    typ = decl[colon + 1:].strip()
    return Field(name=name, typ=typ)


def parse_scala_file(path: str) -> Tuple[Optional[str], List[TypeInfo]]:
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    m = RE_PACKAGE.search(src)
    pkg = m.group(1) if m else None
    types: List[TypeInfo] = []

    # 1) Top-level case classes (not inside an `object`/`trait` block).
    #    We approximate "top-level" by tracking brace depth.
    i = 0
    depth = 0
    object_stack: List[str] = []   # names of nested objects (for FQN expansion)
    while i < len(src):
        # Cheap scan: advance one char at a time but jump on keyword matches.
        ch = src[i]
        if ch == "{":
            depth += 1
            i += 1
            continue
        if ch == "}":
            depth -= 1
            if object_stack and len(object_stack) > depth:
                # pop matched object names when we leave their block
                object_stack.pop()
            i += 1
            continue

        # Try to match a `final case class Name(`
        if src[i:i + 18].startswith("final case class "):
            m = re.match(r"final\s+case\s+class\s+(\w+)\s*\(", src[i:])
            if m:
                name = m.group(1)
                paren_open = i + m.end() - 1   # index of "("
                paren_close = find_matching_paren(src, paren_open)
                if paren_close != -1:
                    arg_str = src[paren_open + 1:paren_close]
                    fields = []
                    for fd in split_top_level_commas(arg_str):
                        f_ = parse_field_decl(fd)
                        if f_:
                            fields.append(f_)
                    # FQN: pkg + nested-object-stack + name
                    qual = pkg or ""
                    if object_stack:
                        qual = qual + "." + ".".join(object_stack) if qual else ".".join(object_stack)
                    fqn = (qual + "." + name) if qual else name
                    types.append(TypeInfo(fqn=fqn, kind="dto", pkg=qual, fields=fields))
                    i = paren_close + 1
                    continue

        # Try to match `sealed trait Name extends ...`
        m = re.match(r"sealed\s+trait\s+(\w+)\b([^{\n]*)", src[i:])
        if m:
            name = m.group(1)
            tail = m.group(2)
            kind = None
            if "IDLEnumElement" in tail:
                kind = "enum"
            elif "IDLAdtElement" in tail:
                kind = "adt"
            if kind is not None:
                qual = pkg or ""
                if object_stack:
                    qual = qual + "." + ".".join(object_stack) if qual else ".".join(object_stack)
                fqn = (qual + "." + name) if qual else name
                types.append(TypeInfo(fqn=fqn, kind=kind, pkg=qual))
            i += m.end()
            continue

        # Match `object Name extends ...` to push onto stack (depth changes at `{`).
        m = re.match(r"object\s+(\w+)\b[^{\n]*\{", src[i:])
        if m:
            name = m.group(1)
            # Push at the depth that the `{` brings us to.
            object_stack.append(name)
            depth_at_obj = depth + 1  # after the open brace
            # Pad object_stack to depth_at_obj so the pop on `}` matches.
            while len(object_stack) < depth_at_obj:
                object_stack.insert(-1, "_")
            # Advance past the opening brace
            i += m.end()
            depth = depth_at_obj
            continue

        i += 1

    # 2) Pass 2 for ADT branches and Enum values: scan inner `case class`/`case object`
    #    statements and attach to the matching top-level ADT/Enum.
    type_by_simple: Dict[str, TypeInfo] = {}
    for t in types:
        simple = t.fqn.rsplit(".", 1)[-1]
        type_by_simple.setdefault(simple, t)

    for m in re.finditer(r"final\s+case\s+class\s+(\w+)\s*\(([^)]*)\)\s*extends\s+([\w\.]+)", src):
        branch_name = m.group(1)
        args = m.group(2)
        parent = m.group(3).split(".")[-1]   # extends might be local or qualified
        parent_t = type_by_simple.get(parent)
        if parent_t and parent_t.kind == "adt":
            # Pull the single `value: T` field
            f_ = parse_field_decl(args.strip())
            if f_:
                parent_t.branches.append(AdtBranch(name=branch_name, value_type=f_.typ))

    for m in re.finditer(r"case\s+object\s+(\w+)\b[^{\n]*\bextends\s+([\w\.]+)", src):
        val_name = m.group(1)
        parent = m.group(2).split(".")[-1]
        parent_t = type_by_simple.get(parent)
        if parent_t and parent_t.kind == "enum":
            parent_t.enum_values.append(val_name)

    # 3) Type aliases inside `package object <X> { type Foo = Bar }`.
    pkg_obj_match = re.search(r"package\s+object\s+(\w+)\s*\{", src)
    if pkg_obj_match:
        po_name = pkg_obj_match.group(1)
        po_open = pkg_obj_match.end() - 1   # the `{`
        po_close = find_matching_brace(src, po_open)
        if po_close != -1:
            body = src[po_open + 1:po_close]
            host_pkg = (pkg + "." + po_name) if pkg else po_name
            for am in re.finditer(r"\btype\s+(\w+)\s*=\s*([^\n]+)", body):
                alias_name = am.group(1).strip()
                target = am.group(2).strip().rstrip(";").rstrip()
                fqn = host_pkg + "." + alias_name
                types.append(TypeInfo(fqn=fqn, kind="alias", pkg=host_pkg, alias_target=target))

    return pkg, types


# ---------------------------------------------------------------------------
# Type-table walker.
# ---------------------------------------------------------------------------

def build_table(root: str) -> Dict[str, TypeInfo]:
    table: Dict[str, TypeInfo] = {}
    for dirpath, _, files in os.walk(root):
        for fn in files:
            if not fn.endswith(".scala"):
                continue
            path = os.path.join(dirpath, fn)
            _, types = parse_scala_file(path)
            for t in types:
                # Prefer the entry with most data on collision (case class may be parsed
                # twice — once at depth 0, once nested).
                existing = table.get(t.fqn)
                if existing is None or len(t.fields) + len(t.branches) + len(t.enum_values) > \
                        len(existing.fields) + len(existing.branches) + len(existing.enum_values):
                    table[t.fqn] = t
    return table


# ---------------------------------------------------------------------------
# Literal synthesizer.
# ---------------------------------------------------------------------------

# Container detection (must be checked in order — longest-prefix-first).
CONTAINER_PATTERNS = [
    ("Option", re.compile(r"^Option\s*\[(.+)\]$", re.DOTALL)),
    ("Some",   re.compile(r"^Some\s*\[(.+)\]$", re.DOTALL)),
    ("List",   re.compile(r"^List\s*\[(.+)\]$", re.DOTALL)),
    ("Seq",    re.compile(r"^Seq\s*\[(.+)\]$", re.DOTALL)),
    ("Vector", re.compile(r"^Vector\s*\[(.+)\]$", re.DOTALL)),
    ("Set",    re.compile(r"^Set\s*\[(.+)\]$", re.DOTALL)),
    ("Map",    re.compile(r"^Map\s*\[(.+),(.+)\]$", re.DOTALL)),
]

RE_ARRAY_BYTE = re.compile(r"^Array\s*\[\s*Byte\s*\]$")


def split_two_type_args(inner: str) -> Tuple[str, str]:
    """Split a balanced `K, V` argument inside a Map[K, V]."""
    depth = 0
    for i, ch in enumerate(inner):
        if ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        elif ch == "," and depth == 0:
            return inner[:i].strip(), inner[i + 1:].strip()
    raise ValueError(f"expected K,V at top level: {inner}")


def parse_container(t: str) -> Optional[Tuple[str, List[str]]]:
    t = t.strip()
    for name, pat in CONTAINER_PATTERNS:
        if name == "Map":
            m = re.match(r"^Map\s*\[", t)
            if m:
                # balanced-bracket split
                bracket_open = t.find("[")
                # Find matching close
                depth = 0
                for i in range(bracket_open, len(t)):
                    ch = t[i]
                    if ch == "[":
                        depth += 1
                    elif ch == "]":
                        depth -= 1
                        if depth == 0:
                            inner = t[bracket_open + 1:i]
                            k, v = split_two_type_args(inner)
                            return ("Map", [k, v])
            continue
        m = pat.match(t)
        if m:
            return (name, [m.group(1).strip()])
    return None


def resolve_short_name(name: str, ctx_pkg: str, table: Dict[str, TypeInfo]) -> Optional[str]:
    """Try to resolve a bare type name against the context package, walking
    parent scopes. Returns the resolved FQN if found, else None."""
    if name in table:
        return name
    if not ctx_pkg:
        return None
    # Walk from full ctx down to single segments.
    parts = ctx_pkg.split(".")
    while parts:
        cand = ".".join(parts) + "." + name
        if cand in table:
            return cand
        parts.pop()
    return None


def synth_literal(typ: str, table: Dict[str, TypeInfo], visited: Set[str], ctx_pkg: str, depth: int = 0) -> str:
    """Return a Scala expression producing a deterministic value of `typ`.

    visited tracks user-type FQNs currently on the construction stack. A repeat
    visit returns an empty container (if inside a container) or a defensive
    sentinel (handled at field-level)."""
    t = normalise_type(typ)

    # Primitives
    if t in PRIMITIVE_LITERAL:
        return PRIMITIVE_LITERAL[t]
    # Time and UUID
    if t in TIME_LITERAL:
        return TIME_LITERAL[t]
    if t == "java.util.UUID":
        return UUID_LITERAL
    if t == "Array[Byte]" or RE_ARRAY_BYTE.match(t):
        return "scala.Array.empty[Byte]"

    # Containers
    cont = parse_container(t)
    if cont is not None:
        kind, args = cont
        if kind == "Option":
            inner = args[0]
            inner_resolved = resolve_short_name(normalise_type(inner), ctx_pkg, table) or normalise_type(inner)
            if inner_resolved in visited:
                return "None"
            return f"Some({synth_literal(inner, table, visited, ctx_pkg, depth + 1)})"
        if kind in ("List", "Seq", "Vector"):
            inner = args[0]
            inner_resolved = resolve_short_name(normalise_type(inner), ctx_pkg, table) or normalise_type(inner)
            if inner_resolved in visited:
                return f"{kind}.empty"
            return f"{kind}({synth_literal(inner, table, visited, ctx_pkg, depth + 1)})"
        if kind == "Set":
            inner = args[0]
            inner_resolved = resolve_short_name(normalise_type(inner), ctx_pkg, table) or normalise_type(inner)
            if inner_resolved in visited:
                return "Set.empty"
            return f"Set({synth_literal(inner, table, visited, ctx_pkg, depth + 1)})"
        if kind == "Map":
            k, v = args
            k_resolved = resolve_short_name(normalise_type(k), ctx_pkg, table) or normalise_type(k)
            v_resolved = resolve_short_name(normalise_type(v), ctx_pkg, table) or normalise_type(v)
            if k_resolved in visited or v_resolved in visited:
                return "Map.empty"
            kv_k = synth_literal(k, table, visited, ctx_pkg, depth + 1)
            kv_v = synth_literal(v, table, visited, ctx_pkg, depth + 1)
            return f"Map({kv_k} -> {kv_v})"
        if kind == "Some":
            return f"Some({synth_literal(args[0], table, visited, ctx_pkg, depth + 1)})"

    # User type — look up directly, then via short-name resolution, then via
    # `<fqn>.Struct` (interface impl-DTO mirror).
    fqn = t if t in table else resolve_short_name(t, ctx_pkg, table)
    if fqn is None:
        # Try `<t>.Struct` — interface impl-DTO.
        struct_fqn = t + ".Struct"
        if struct_fqn in table:
            fqn = struct_fqn
        else:
            resolved_root = resolve_short_name(t, ctx_pkg, table)
            if resolved_root is None:
                # Try resolving `t` as a partial path and appending `.Struct`.
                struct_resolved = resolve_short_name(t + ".Struct", ctx_pkg, table)
                if struct_resolved is not None:
                    fqn = struct_resolved
    if fqn is None:
        return f"/*UNKNOWN-TYPE:{t}*/ null.asInstanceOf[_root_.{t}]"
    return synth_user_value(table[fqn], table, visited, depth)


def synth_user_value(info: TypeInfo, table: Dict[str, TypeInfo], visited: Set[str], depth: int) -> str:
    """Construct a value of the user type `info`."""
    if info.kind == "alias":
        return synth_literal(info.alias_target, table, visited, info.pkg, depth + 1)

    if info.fqn in visited:
        # If the type is an ADT and any branch terminates without re-entering
        # the visited set, pick that branch — recursion is well-founded.
        if info.kind == "adt" and info.branches:
            br = pick_non_cyclic_branch(info, table, visited)
            if br is not None:
                value_expr = synth_literal(br.value_type, table, visited, info.pkg, depth + 1)
                return f"new _root_.{info.fqn}.{br.name}({value_expr})"
        return f"/*CYCLE:{info.fqn}*/ null.asInstanceOf[_root_.{info.fqn}]"

    next_visited = visited | {info.fqn}
    if info.kind == "dto":
        args = ", ".join(
            synth_literal(f.typ, table, next_visited, info.pkg, depth + 1) for f in info.fields
        )
        return f"new _root_.{info.fqn}({args})"
    if info.kind == "enum":
        if info.enum_values:
            return f"_root_.{info.fqn}.{info.enum_values[0]}"
        return f"/*EMPTY-ENUM:{info.fqn}*/ ???"
    if info.kind == "adt":
        if info.branches:
            br = pick_non_cyclic_branch(info, table, next_visited) or info.branches[0]
            value_expr = synth_literal(br.value_type, table, next_visited, info.pkg, depth + 1)
            return f"new _root_.{info.fqn}.{br.name}({value_expr})"
        return f"/*EMPTY-ADT:{info.fqn}*/ ???"
    return f"/*UNKNOWN-KIND:{info.kind}:{info.fqn}*/ ???"


def pick_non_cyclic_branch(info: TypeInfo, table: Dict[str, TypeInfo], visited: Set[str]) -> Optional[AdtBranch]:
    """Prefer an ADT branch whose value type does not transitively re-enter
    `visited`. Avoids null-via-cycle placeholders for the common case where at
    least one alternative is non-recursive (matches the new typer's
    "any alt terminates ⇒ cycle terminates" rule)."""
    for br in info.branches:
        if branch_terminates(br, table, visited, set()):
            return br
    return None


def branch_terminates(br: AdtBranch, table: Dict[str, TypeInfo], visited: Set[str], seen: Set[str]) -> bool:
    """True iff constructing `br` can be done without re-entering a type in
    `visited`. Conservative — Container references are always OK (empty)."""
    return type_terminates(br.value_type, table, visited, seen)


def type_terminates(typ: str, table: Dict[str, TypeInfo], visited: Set[str], seen: Set[str]) -> bool:
    t = normalise_type(typ)
    if t in PRIMITIVE_LITERAL or t in TIME_LITERAL or t == "java.util.UUID":
        return True
    if t == "Array[Byte]" or RE_ARRAY_BYTE.match(t):
        return True
    cont = parse_container(t)
    if cont is not None:
        # Containers can always be empty.
        return True
    # User type: resolve, then check recursively.
    fqn = t if t in table else None
    if fqn is None:
        # Optimistic: unknown types treated as terminating.
        return True
    if fqn in visited:
        return False
    if fqn in seen:
        # We've already opened this on the way down — break optimistically.
        return True
    info = table[fqn]
    if info.kind == "dto":
        next_seen = seen | {fqn}
        return all(type_terminates(f.typ, table, visited, next_seen) for f in info.fields)
    if info.kind in ("enum", "interface"):
        return True
    if info.kind == "adt":
        # An ADT terminates if any branch terminates.
        next_seen = seen | {fqn}
        return any(type_terminates(br.value_type, table, visited, next_seen) for br in info.branches)
    return True


# ---------------------------------------------------------------------------
# Fixture emission.
# ---------------------------------------------------------------------------

def is_internal(fqn: str) -> bool:
    """Skip generated wrapper/contract/codec types that are internal to idlc."""
    last = fqn.rsplit(".", 1)[-1]
    if last.endswith("Contract"):
        return True
    if last.endswith("Circe"):
        return True
    if last.endswith("Codecs"):
        return True
    if last.endswith("Server") or last.endswith("Client"):
        return True
    if last.endswith("WrappedClient") or last.endswith("WrappedServer"):
        return True
    return False


def emit_fixtures(table: Dict[str, TypeInfo]) -> List[str]:
    # Inner branch-wrapper case classes (e.g. `Outer.Success(value)` inside an
    # ADT's companion) don't have a standalone Circe encoder — encoding flows
    # through the parent ADT's encoder via the branch alternative. Emitting a
    # fixture `(v: Outer.Success).asJson` would fail to find an Encoder.
    branch_wrapper_fqns: Set[str] = set()
    for info in table.values():
        if info.kind == "adt":
            for br in info.branches:
                branch_wrapper_fqns.add(info.fqn + "." + br.name)

    blocks: List[str] = []
    for fqn in sorted(table):
        info = table[fqn]
        if is_internal(fqn):
            continue
        if fqn in branch_wrapper_fqns:
            continue
        if info.kind == "dto":
            expr = synth_user_value(info, table, set(), 0)
            block = (
                f"    {{\n"
                f"      val v: _root_.{info.fqn} = {expr}\n"
                f"      lines += s\"{info.fqn}\\tdefault\\t${{(v: _root_.{info.fqn}).asJson.printWith(P)}}\"\n"
                f"    }}"
            )
            blocks.append(block)
        elif info.kind == "enum":
            for val in info.enum_values:
                expr = f"_root_.{info.fqn}.{val}"
                block = (
                    f"    {{\n"
                    f"      val v: _root_.{info.fqn} = {expr}\n"
                    f"      lines += s\"{info.fqn}\\t{val}\\t${{(v: _root_.{info.fqn}).asJson.printWith(P)}}\"\n"
                    f"    }}"
                )
                blocks.append(block)
        elif info.kind == "adt":
            for br in info.branches:
                value_expr = synth_literal(br.value_type, table, {info.fqn}, info.pkg, 0)
                expr = f"new _root_.{info.fqn}.{br.name}({value_expr})"
                block = (
                    f"    {{\n"
                    f"      val v: _root_.{info.fqn} = {expr}\n"
                    f"      lines += s\"{info.fqn}\\t{br.name}\\t${{(v: _root_.{info.fqn}).asJson.printWith(P)}}\"\n"
                    f"    }}"
                )
                blocks.append(block)
    return blocks


PROLOGUE = """\
// Auto-generated by debug/20260515-gen-sample-app.py for the idl-regress harness.
// Covers every generated final case class (DTO, Identifier, service/buzzer
// ephemeral), every Enum, and every ADT (one scenario per branch).
// Determinism: fixed scalars and timestamps. Per-chunk emitter objects keep
// each Scala class under the JVM 64KB class-size and per-method bytecode
// limits.
package sample_app

import io.circe._
import io.circe.syntax._

"""

MAIN_OBJECT_TEMPLATE = """\
object SampleApp {{
  private val P = Printer.noSpaces
  private val lines = scala.collection.mutable.ArrayBuffer.empty[String]

  def main(args: Array[String]): Unit = {{
{calls}
    lines.sortInPlace()
    lines.foreach(println)
  }}
}}
"""

# JVM limits per-method bytecode to 64KB and per-class size separately. Each
# chunk object holds a single `emit` method whose constant pool stays small
# enough.
CHUNK_SIZE = 200


def main():
    if len(sys.argv) != 3:
        print(f"usage: {sys.argv[0]} <gen-scala-root> <out-file>", file=sys.stderr)
        sys.exit(2)
    root, out_path = sys.argv[1], sys.argv[2]
    table = build_table(root)
    print(f"parsed {len(table)} types from {root}", file=sys.stderr)
    blocks = emit_fixtures(table)
    print(f"emitting {len(blocks)} fixtures", file=sys.stderr)

    chunks = [blocks[i:i + CHUNK_SIZE] for i in range(0, len(blocks), CHUNK_SIZE)]
    chunk_objects: List[str] = []
    for idx, chunk in enumerate(chunks):
        body = (
            f"private object Emitter_{idx} {{\n"
            f"  private val P = Printer.noSpaces\n"
            f"  def emit(lines: scala.collection.mutable.ArrayBuffer[String]): Unit = {{\n"
            + "\n".join(chunk)
            + "\n  }\n"
            f"}}"
        )
        chunk_objects.append(body)
    calls = "\n".join(f"    Emitter_{idx}.emit(lines)" for idx in range(len(chunks)))

    with open(out_path, "w", encoding="utf-8") as f:
        f.write(PROLOGUE)
        f.write("\n\n".join(chunk_objects))
        f.write("\n\n")
        f.write(MAIN_OBJECT_TEMPLATE.format(calls=calls))


if __name__ == "__main__":
    main()
