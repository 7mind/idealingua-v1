#!/usr/bin/env python3
"""
Sample-app generator for the idl-regress harness — C# target.

Walks a generated idlc C# tree (PLAIN layout), extracts every user-declared
class / enum / ADT branch, and emits a `sample_app.cs` driver that
constructs one fixture per non-internal type with deterministic literal
values. Output shape (one line per fixture, printed in sorted order):

    <wireId>\\t<scenario>\\t<json>

Parsing strategy:
  * Per-file regex pass collects: namespace, every top-level / nested
    `public [abstract|sealed]? class X[: Parent]`, every `public enum X`,
    every `RTTI_FULLCLASSNAME` constant, and the body of every non-default
    constructor (the field order ground truth).
  * Nested-class scope tracked via brace depth — outer types become a `.`-
    separated prefix on the C# qualified name so we can build correct
    constructor expressions like `new Outer.Inner(...)`.
  * ADT roots are recognised as `public abstract class X` whose body
    contains one or more `public sealed class Branch : X { public T Value;
    public Branch(T value) { ... } }`.

Synthesis rules — deterministic primitive literals:
  bool   → true
  string → "s1"
  byte   → (byte)1            sbyte → (sbyte)1
  short  → (short)1           ushort → (ushort)1
  int    → 1                  uint  → 1u
  long   → 2L                 ulong → 2UL
  float  → 1.5f               double → 2.5d
  decimal→ 1.5m
  Guid   → System.Guid.Parse("3a7f0c12-1234-5678-9abc-fedcba987654")
  DateTime → reference instant 2024-01-01T00:00:00Z with DateTimeKind.Utc
  TimeSpan → System.TimeSpan.FromSeconds(1)
  byte[] → System.Convert.FromBase64String("aGkh")

Container handling:
  List<T>             → new System.Collections.Generic.List<T> { <T> }
  Dictionary<K,V>     → new System.Collections.Generic.Dictionary<K,V> { { <K>, <V> } }
  Nullable<T> / T?    → <T>      (boxed nullable, just supply the inner literal)

Cycle break — when constructing a user type that is already on the
construction stack: if it appears inside a container we emit an empty
container; if it's an ADT we pick a non-cyclic branch; otherwise we fall
back to the zero-arg constructor (every generated DTO ships with a
default ctor).

Usage:

    python3 20260515-gen-sample-app-cs.py <gen-csharp-root> <out-file>
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
class CField:
    name: str
    typ: str   # raw C# type string

@dataclass
class TypeInfo:
    qname: str                           # C# qualified name with outer types, e.g. "Net.Playq.X.Outer.Inner"
    wire_id: Optional[str] = None        # the RTTI_FULLCLASSNAME if any (the dotted lowercase wire form)
    kind: str = "dto"                    # "dto" | "enum" | "adt" | "branch" | "interface" (skipped)
    ctor_fields: List[CField] = field(default_factory=list)
    enum_values: List[str] = field(default_factory=list)
    branches: List["TypeInfo"] = field(default_factory=list)  # adt branches (also stored standalone in table)
    parent_qname: Optional[str] = None   # for ADT branches: the parent abstract class qname
    branch_value_type: Optional[str] = None  # for ADT branches: the wrapped value type
    namespace: str = ""                  # owning C# namespace (without outer-type chain)
    outer_chain: List[str] = field(default_factory=list)

# ---------------------------------------------------------------------------
# Parsing
# ---------------------------------------------------------------------------

RE_NAMESPACE = re.compile(r"\bnamespace\s+([\w\.]+)\s*\{")
RE_USING_ALIAS = re.compile(r"\busing\s+(\w+)\s*=\s*([\w\.]+)\s*;")

RE_CLASS = re.compile(
    r"\b(?:public\s+)?(?:(?P<mod>abstract|sealed|static)\s+)?class\s+(?P<name>\w+)"
    r"(?:\s*:\s*(?P<parent>[\w\.\<\>]+(?:\s*,\s*[\w\.\<\>]+)*))?\s*\{"
)
RE_ENUM = re.compile(r"\b(?:public\s+)?enum\s+(?P<name>\w+)\s*\{(?P<body>[^}]*)\}")
RE_INTERFACE = re.compile(r"\b(?:public\s+)?interface\s+(\w+)")

# An RTTI_FULLCLASSNAME constant binding within a class body.
RE_RTTI = re.compile(r"public\s+static\s+readonly\s+string\s+RTTI_FULLCLASSNAME\s*=\s*\"([^\"]+)\"\s*;")

# A constructor signature inside a class — `public ClassName(args) {`.
# We match the *non-default* one (args non-empty) by checking the captured arg list later.
RE_CTOR = re.compile(r"public\s+(\w+)\s*\(\s*(?P<args>[^)]*)\)\s*\{")

# Internal name suffixes — generated converters/dispatchers/services we skip.
INTERNAL_SUFFIXES = (
    "_JsonNetConverter",
    "Helpers",
    "ClientGeneric",
    "Client",
    "ServerGeneric",
    "Server",
    "Dispatcher",
    "Visitor",          # nested ITgMetricDefVisitor etc.
)


def strip_line_comments(s: str) -> str:
    # Strip `// …\n` and `/* … */` style comments (non-regex to keep it simple).
    out = []
    i = 0
    n = len(s)
    while i < n:
        c = s[i]
        if c == "/" and i + 1 < n and s[i + 1] == "/":
            # Skip to end of line
            j = s.find("\n", i)
            if j == -1:
                break
            i = j
            continue
        if c == "/" and i + 1 < n and s[i + 1] == "*":
            j = s.find("*/", i + 2)
            if j == -1:
                break
            i = j + 2
            continue
        if c == '"':
            # Skip over string literal (handle simple escapes)
            out.append(c)
            i += 1
            while i < n:
                out.append(s[i])
                if s[i] == "\\" and i + 1 < n:
                    out.append(s[i + 1])
                    i += 2
                    continue
                if s[i] == '"':
                    i += 1
                    break
                i += 1
            continue
        out.append(c)
        i += 1
    return "".join(out)


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
    """Split a C# argument list on top-level commas (depth-aware on ()[]{<>})."""
    out: List[str] = []
    depth = 0
    buf: List[str] = []
    angle_depth = 0
    for ch in args:
        if ch in "([{":
            depth += 1
            buf.append(ch)
        elif ch in ")]}":
            depth -= 1
            buf.append(ch)
        elif ch == "<":
            angle_depth += 1
            buf.append(ch)
        elif ch == ">":
            angle_depth -= 1
            buf.append(ch)
        elif ch == "," and depth == 0 and angle_depth == 0:
            out.append("".join(buf).strip())
            buf = []
        else:
            buf.append(ch)
    if buf:
        out.append("".join(buf).strip())
    return [p for p in out if p]


def parse_ctor_arg(decl: str) -> Optional[CField]:
    """Parse a single C# ctor parameter: `Type name` or `Type name = default`."""
    decl = decl.strip()
    if not decl:
        return None
    # Strip default-value suffix
    eq = decl.find("=")
    if eq != -1:
        decl = decl[:eq].strip()
    # The last whitespace-separated token is the name; the rest is the type.
    # Names can be either bare identifiers or `@`-prefixed verbatim identifiers
    # (used to escape C# keywords like `ref`, `namespace`).
    m = re.match(r"^(?P<type>.+?)\s+(?P<name>@?\w+)$", decl, re.DOTALL)
    if not m:
        return None
    name = m.group("name")
    # Strip the verbatim-identifier prefix; the JSON field name is the bare ident.
    if name.startswith("@"):
        name = name[1:]
    return CField(name=name, typ=m.group("type").strip())


def is_internal_name(name: str) -> bool:
    for suf in INTERNAL_SUFFIXES:
        if name.endswith(suf):
            return True
    return False


def parse_cs_file(path: str) -> List[TypeInfo]:
    with open(path, "r", encoding="utf-8") as f:
        raw = f.read()
    src = strip_line_comments(raw)

    ns_m = RE_NAMESPACE.search(src)
    if ns_m is None:
        return []
    namespace = ns_m.group(1)

    # Inline `using Alias = X.Y.Z;` substitutions inside the namespace body —
    # they only show up in idl-generated ADT files and shadow nested branch
    # value types. Rewrite the body so the alias name is replaced by the
    # target qualified name everywhere it occurs as a whole word.
    for am in RE_USING_ALIAS.finditer(src):
        alias = am.group(1)
        target = am.group(2)
        # Replace whole-word occurrences only — avoids clobbering substrings.
        src = re.sub(rf"\b{re.escape(alias)}\b", target, src)

    results: List[TypeInfo] = []

    # Walk classes by recursively descending brace blocks.
    def walk_block(body: str, outer_chain: List[str]) -> None:
        i = 0
        n = len(body)
        while i < n:
            c = body[i]
            # Match `class Name [: Parent]? {`
            m = RE_CLASS.match(body, i)
            m_e = RE_ENUM.match(body, i)
            m_iface = RE_INTERFACE.match(body, i)
            if m and m.group("mod") != "static":
                name = m.group("name")
                parent_raw = m.group("parent") or ""
                # The first parent token (before comma) is the C# base class.
                parent = ""
                if parent_raw:
                    parent = parent_raw.split(",")[0].strip()
                # locate the `{` matched at end of the regex
                open_brace = body.find("{", i, m.end())
                if open_brace == -1:
                    i = m.end()
                    continue
                close_brace = find_matching_brace(body, open_brace)
                if close_brace == -1:
                    i = m.end()
                    continue
                cls_body = body[open_brace + 1:close_brace]
                qname = ".".join([namespace] + outer_chain + [name])

                # Extract RTTI (only valid for this class — not nested ones).
                # We scan only up to the first nested `class` declaration to avoid
                # confusion. Cheap heuristic: take all RTTI matches in the body and
                # pick the textually first one (the class's own one).
                rtti_match = RE_RTTI.search(cls_body)
                wire_id = rtti_match.group(1) if rtti_match else None

                # Find the longest ctor (with the most args) — that's the positional one.
                ctor_args: List[CField] = []
                for cm in RE_CTOR.finditer(cls_body):
                    if cm.group(1) != name:
                        continue
                    args_str = cm.group("args").strip()
                    if not args_str:
                        continue
                    parts = split_top_level_commas(args_str)
                    fs = []
                    for p in parts:
                        f_ = parse_ctor_arg(p)
                        if f_ is not None:
                            fs.append(f_)
                    if len(fs) > len(ctor_args):
                        ctor_args = fs

                # Kind detection
                mod = m.group("mod") or ""
                kind = "dto"
                branch_value: Optional[str] = None
                branch_parent: Optional[str] = None
                if mod == "abstract":
                    kind = "adt"
                elif mod == "sealed" and parent and outer_chain:
                    # sealed class Branch : Parent — ADT branch (nested in parent)
                    # The parent qname is namespace + outer_chain (which already
                    # contains the parent's name as the last entry).
                    branch_parent = ".".join([namespace] + outer_chain)
                    if ctor_args and len(ctor_args) == 1:
                        kind = "branch"
                        branch_value = ctor_args[0].typ

                # Skip internal helper classes by name.
                if is_internal_name(name):
                    pass  # don't add this type
                else:
                    info = TypeInfo(
                        qname=qname,
                        wire_id=wire_id,
                        kind=kind,
                        ctor_fields=ctor_args,
                        parent_qname=branch_parent,
                        branch_value_type=branch_value,
                        namespace=namespace,
                        outer_chain=list(outer_chain),
                    )
                    results.append(info)

                # Recurse into the body for nested classes/enums.
                walk_block(cls_body, outer_chain + [name])
                i = close_brace + 1
                continue
            elif m and m.group("mod") == "static":
                # static class (e.g. service entry-point) — recurse only, don't add.
                name = m.group("name")
                open_brace = body.find("{", i, m.end())
                close_brace = find_matching_brace(body, open_brace) if open_brace != -1 else -1
                if close_brace == -1:
                    i = m.end()
                    continue
                walk_block(body[open_brace + 1:close_brace], outer_chain + [name])
                i = close_brace + 1
                continue
            elif m_e:
                name = m_e.group("name")
                body_e = m_e.group("body")
                values = [v.strip() for v in body_e.split(",")]
                values = [v.split("=")[0].strip() for v in values if v.strip()]
                qname = ".".join([namespace] + outer_chain + [name])
                # enums get no RTTI_FULLCLASSNAME constant — derive the wire id
                # from the namespace (lowercased) + outer-type chain + name.
                tail_segments = list(outer_chain) + [name]
                wire_id = lower_wire(namespace) + "." + ".".join(tail_segments)
                info = TypeInfo(
                    qname=qname,
                    wire_id=wire_id,
                    kind="enum",
                    enum_values=values,
                    namespace=namespace,
                    outer_chain=list(outer_chain),
                )
                results.append(info)
                i = m_e.end()
                continue
            elif m_iface:
                # Skip interface block entirely (we don't construct values for these
                # — they exist as polymorphism roots and are exercised via their
                # `*Struct` mirror classes).
                open_brace = body.find("{", i)
                if open_brace == -1:
                    i = m_iface.end()
                    continue
                close_brace = find_matching_brace(body, open_brace)
                if close_brace == -1:
                    i = m_iface.end()
                    continue
                i = close_brace + 1
                continue
            i += 1

    # Find namespace body
    ns_open = src.find("{", ns_m.end() - 1)
    ns_close = find_matching_brace(src, ns_open) if ns_open != -1 else -1
    if ns_close == -1:
        return results
    walk_block(src[ns_open + 1:ns_close], [])
    return results


def lower_wire(namespace: str) -> str:
    """Convert a C# namespace like `Net.Playq.Foo` to its wire form
    `net.playq.foo`. This is a best-effort guess used only for enums (where the
    generator doesn't emit RTTI constants)."""
    return ".".join(seg.lower() for seg in namespace.split("."))


# ---------------------------------------------------------------------------
# Type table
# ---------------------------------------------------------------------------

def build_table(root: str) -> Dict[str, TypeInfo]:
    table: Dict[str, TypeInfo] = {}
    for dirpath, _, files in os.walk(root):
        for fn in files:
            if not fn.endswith(".cs"):
                continue
            path = os.path.join(dirpath, fn)
            try:
                for info in parse_cs_file(path):
                    existing = table.get(info.qname)
                    if existing is None:
                        table[info.qname] = info
                    else:
                        # Prefer the richer entry.
                        if len(info.ctor_fields) + len(info.enum_values) > \
                                len(existing.ctor_fields) + len(existing.enum_values):
                            table[info.qname] = info
            except Exception as exc:  # noqa: BLE001
                print(f"warn: parse failed for {path}: {exc}", file=sys.stderr)

    # Attach branches to their parent ADT for convenience.
    for info in table.values():
        if info.kind == "branch" and info.parent_qname:
            parent = table.get(info.parent_qname)
            if parent and parent.kind == "adt":
                parent.branches.append(info)
    return table


# ---------------------------------------------------------------------------
# Literal synthesis
# ---------------------------------------------------------------------------

PRIM_LITERALS = {
    "bool": "true",
    "Boolean": "true",
    "string": "\"s1\"",
    "String": "\"s1\"",
    "byte": "(byte)1",
    "sbyte": "(sbyte)1",
    "short": "(short)1",
    "ushort": "(ushort)1",
    "int": "1",
    "Int32": "1",
    "uint": "1u",
    "long": "2L",
    "Int64": "2L",
    "ulong": "2UL",
    "float": "1.5f",
    "Single": "1.5f",
    "double": "2.5d",
    "Double": "2.5d",
    "decimal": "1.5m",
    "Decimal": "1.5m",
    "char": "'a'",
}

GUID_LITERAL = "System.Guid.Parse(\"3a7f0c12-1234-5678-9abc-fedcba987654\")"
DATETIME_LITERAL = "new System.DateTime(2024, 1, 1, 0, 0, 0, System.DateTimeKind.Utc)"
TIMESPAN_LITERAL = "System.TimeSpan.FromSeconds(1)"
BYTE_ARRAY_LITERAL = "System.Convert.FromBase64String(\"aGkh\")"


def normalize_type(t: str) -> str:
    return t.strip().rstrip("?")


def split_generic_args(inner: str) -> List[str]:
    """Split a comma-separated generic argument list at top level."""
    out: List[str] = []
    angle = 0
    buf: List[str] = []
    for ch in inner:
        if ch == "<":
            angle += 1
            buf.append(ch)
        elif ch == ">":
            angle -= 1
            buf.append(ch)
        elif ch == "," and angle == 0:
            out.append("".join(buf).strip())
            buf = []
        else:
            buf.append(ch)
    if buf:
        out.append("".join(buf).strip())
    return out


def parse_generic(t: str) -> Optional[Tuple[str, List[str]]]:
    """If t is `Outer<A, B>` (possibly fully qualified) return `(Outer, [A, B])`.
    Only the *last* segment of the head is examined for the container kind."""
    t = t.strip()
    lt = t.find("<")
    if lt == -1:
        return None
    if not t.endswith(">"):
        return None
    head = t[:lt].strip()
    inner = t[lt + 1:-1]
    return (head, split_generic_args(inner))


def head_simple_name(head: str) -> str:
    return head.rsplit(".", 1)[-1]


def resolve_qname(name: str, ctx_outer: List[str], ctx_ns: str, table: Dict[str, TypeInfo]) -> Optional[str]:
    """Resolve a (possibly short) C# type name to a fully-qualified TypeInfo qname
    in the table. Tries:
      1. exact match
      2. namespace + outer scope path (peeled)
      3. namespace + name (top-level in the current ns)
    """
    if name in table:
        return name
    # Walk outer chain
    chain = list(ctx_outer)
    while True:
        cand = ".".join([ctx_ns] + chain + [name])
        if cand in table:
            return cand
        if not chain:
            break
        chain.pop()
    # Already may be fully qualified but starting with `Net.Playq...` — check direct
    return None


def synth_literal(typ: str, table: Dict[str, TypeInfo], visited: Set[str], ctx_outer: List[str], ctx_ns: str) -> str:
    t = normalize_type(typ)

    # Primitive
    if t in PRIM_LITERALS:
        return PRIM_LITERALS[t]
    if t in ("Guid", "System.Guid"):
        return GUID_LITERAL
    if t in ("DateTime", "System.DateTime"):
        return DATETIME_LITERAL
    if t in ("TimeSpan", "System.TimeSpan"):
        return TIMESPAN_LITERAL
    if t in ("byte[]", "System.Byte[]"):
        return BYTE_ARRAY_LITERAL

    # Generics
    gen = parse_generic(t)
    if gen is not None:
        head, args = gen
        simple = head_simple_name(head)
        if simple in ("List", "IList", "IReadOnlyList", "IEnumerable", "ICollection"):
            inner = args[0]
            inner_lit = synth_literal_or_break(inner, table, visited, ctx_outer, ctx_ns)
            if inner_lit is None:
                return f"new System.Collections.Generic.List<{inner}>()"
            return f"new System.Collections.Generic.List<{inner}> {{ {inner_lit} }}"
        if simple in ("Dictionary", "IDictionary", "IReadOnlyDictionary"):
            k, v = args[0], args[1]
            k_lit = synth_literal_or_break(k, table, visited, ctx_outer, ctx_ns)
            v_lit = synth_literal_or_break(v, table, visited, ctx_outer, ctx_ns)
            if k_lit is None or v_lit is None:
                return f"new System.Collections.Generic.Dictionary<{k}, {v}>()"
            return f"new System.Collections.Generic.Dictionary<{k}, {v}> {{ {{ {k_lit}, {v_lit} }} }}"
        if simple == "Nullable":
            return synth_literal(args[0], table, visited, ctx_outer, ctx_ns)
        if simple in ("HashSet", "ISet"):
            inner = args[0]
            inner_lit = synth_literal_or_break(inner, table, visited, ctx_outer, ctx_ns)
            if inner_lit is None:
                return f"new System.Collections.Generic.HashSet<{inner}>()"
            return f"new System.Collections.Generic.HashSet<{inner}> {{ {inner_lit} }}"

    # User type
    qname = resolve_qname(t, ctx_outer, ctx_ns, table)
    if qname is None:
        # Try interpreting as a fully qualified name where the last segment is a Struct mirror
        # — interface Foo with mirror FooStruct in same namespace. Try `<t>Struct`.
        qname = resolve_qname(t + "Struct", ctx_outer, ctx_ns, table)
    if qname is None:
        # last-resort: unknown — emit a default-constructed of the C# raw name
        return f"default({t}) /* UNKNOWN-TYPE */"

    return synth_user_value(table[qname], table, visited)


def synth_literal_or_break(typ: str, table, visited, ctx_outer, ctx_ns) -> Optional[str]:
    """Like synth_literal but returns None if the inner is a user type that
    is already in `visited` — so the caller can choose to emit an empty
    container instead. Primitives and other recursive containers always
    return a literal."""
    t = normalize_type(typ)
    if t in PRIM_LITERALS or t in ("Guid", "System.Guid", "DateTime", "System.DateTime",
                                    "TimeSpan", "System.TimeSpan", "byte[]", "System.Byte[]"):
        return synth_literal(t, table, visited, ctx_outer, ctx_ns)
    gen = parse_generic(t)
    if gen is not None:
        # Recursive container — always emit (its own break logic handles inner cycles).
        return synth_literal(t, table, visited, ctx_outer, ctx_ns)
    qname = resolve_qname(t, ctx_outer, ctx_ns, table) or resolve_qname(t + "Struct", ctx_outer, ctx_ns, table)
    if qname and qname in visited:
        return None
    return synth_literal(t, table, visited, ctx_outer, ctx_ns)


def synth_user_value(info: TypeInfo, table: Dict[str, TypeInfo], visited: Set[str]) -> str:
    if info.qname in visited:
        # Cycle — fall back to default-constructed (every generated DTO has a
        # parameterless ctor). For ADTs we can still pick a non-cyclic branch
        # if one exists.
        if info.kind == "adt" and info.branches:
            br = pick_non_cyclic_branch(info, table, visited)
            if br is not None:
                value_expr = synth_literal(
                    br.branch_value_type or "",
                    table, visited,
                    outer_chain_of(info.qname),
                    namespace_of(info.qname),
                )
                return f"new {br.qname}({value_expr})"
        return f"new {info.qname}()"
    if info.kind == "enum":
        if not info.enum_values:
            return f"default({info.qname})"
        return f"{info.qname}.{info.enum_values[0]}"
    if info.kind == "adt":
        # Pick branch.
        if info.branches:
            br = pick_non_cyclic_branch(info, table, visited | {info.qname}) or info.branches[0]
            value_expr = synth_literal(
                br.branch_value_type or "",
                table,
                visited | {info.qname},
                outer_chain_of(info.qname),
                namespace_of(info.qname),
            )
            return f"new {br.qname}({value_expr})"
        return f"default({info.qname})"
    # dto or branch
    next_visited = visited | {info.qname}
    args_strs = []
    for f_ in info.ctor_fields:
        args_strs.append(synth_literal(
            f_.typ, table, next_visited,
            outer_chain_of(info.qname),
            namespace_of(info.qname),
        ))
    args_joined = ", ".join(args_strs)
    return f"new {info.qname}({args_joined})"


def namespace_of(qname: str) -> str:
    """Return the leading namespace portion of a C# qname — everything up to
    the first capitalised type segment is namespace; everything after is outer
    types. Approximation: drop the last segment (the simple name) and walk
    backward while the previous segment also starts uppercase but only one type
    chain. For idl-generated trees the namespace ends right before the first
    type that owns nested types. We approximate by recognising classes in the
    table: the longest table-key prefix that is a known type tells us the
    outer chain — but here we only need namespace, so a simpler heuristic
    works: namespace is the prefix up to the first segment whose value, when
    looked at in isolation, doesn't appear as a known *type* alone. Since we
    don't have that lookup here, fall back to: namespace = qname up to the
    second-to-last segment if no type with that prefix exists. For our
    purposes the namespace is the path's parent and outer chain is empty —
    nested ADT branches override via their qname directly in the caller.
    """
    parts = qname.rsplit(".", 1)
    return parts[0] if len(parts) > 1 else ""


def outer_chain_of(qname: str) -> List[str]:
    return []  # The synth_literal use of ctx_outer is rare; resolve_qname tries every nesting via walking.


def pick_non_cyclic_branch(info: TypeInfo, table: Dict[str, TypeInfo], visited: Set[str]) -> Optional[TypeInfo]:
    for br in info.branches:
        if branch_terminates(br, table, visited, set()):
            return br
    return None


def branch_terminates(br: TypeInfo, table, visited, seen) -> bool:
    return type_terminates(br.branch_value_type or "", table, visited, seen)


def type_terminates(typ: str, table, visited, seen) -> bool:
    t = normalize_type(typ)
    if t in PRIM_LITERALS:
        return True
    if t in ("Guid", "System.Guid", "DateTime", "System.DateTime",
             "TimeSpan", "System.TimeSpan", "byte[]", "System.Byte[]"):
        return True
    gen = parse_generic(t)
    if gen is not None:
        return True
    qname = None
    if t in table:
        qname = t
    if qname is None:
        return True  # unknown — optimistic
    if qname in visited:
        return False
    if qname in seen:
        return True
    info = table[qname]
    if info.kind == "enum":
        return True
    if info.kind == "adt":
        return any(type_terminates(br.branch_value_type or "", table, visited, seen | {qname})
                   for br in info.branches)
    # dto / branch
    return all(type_terminates(f_.typ, table, visited, seen | {qname}) for f_ in info.ctor_fields)


# ---------------------------------------------------------------------------
# Emission
# ---------------------------------------------------------------------------

def derive_wire_id(info: "TypeInfo") -> str:
    """For types whose generated C# class lacks an RTTI_FULLCLASSNAME constant
    (e.g. plain ADT roots), reconstruct the wire id by lower-casing the C#
    namespace and keeping the outer-type chain + simple name CamelCased."""
    ns_lower = ".".join(seg.lower() for seg in info.namespace.split("."))
    tail = info.outer_chain + [info.qname.rsplit(".", 1)[-1]]
    return ns_lower + "." + ".".join(tail)


def is_runtime_namespace(qname: str) -> bool:
    """The generated tree includes the IRT runtime (Logger / Marshaller /
    Transport infrastructure). These types are not user-IDL; we skip them."""
    return qname.startswith("IRT.")


def emit_fixtures(table: Dict[str, TypeInfo]) -> List[Tuple[str, str, str]]:
    """Return a list of (wireId, scenario, expression) tuples."""
    # Track ADT-branch qnames — these are constructed via their parent.
    branch_qnames: Set[str] = set()
    for info in table.values():
        if info.kind == "adt":
            for br in info.branches:
                branch_qnames.add(br.qname)

    fixtures: List[Tuple[str, str, str]] = []
    for qname in sorted(table):
        info = table[qname]
        if is_runtime_namespace(qname):
            continue
        if info.kind == "branch":
            # Emitted from parent ADT loop below.
            continue
        if info.kind == "enum":
            for val in info.enum_values:
                wire = info.wire_id or qname
                expr = f"{info.qname}.{val}"
                fixtures.append((wire, val, expr))
            continue
        if info.kind == "adt":
            wire = info.wire_id or derive_wire_id(info)
            if not info.branches:
                continue
            for br in info.branches:
                value_expr = synth_literal(
                    br.branch_value_type or "",
                    table, {info.qname},
                    outer_chain_of(info.qname),
                    namespace_of(info.qname),
                )
                expr = f"new {br.qname}({value_expr})"
                fixtures.append((wire, br.qname.rsplit(".", 1)[-1], expr))
            continue
        # dto
        if not info.wire_id:
            # No RTTI — skip (likely a helper / non-user type).
            continue
        expr = synth_user_value(info, table, set())
        fixtures.append((info.wire_id, "default", expr))
    return fixtures


HEADER = """\
// Auto-generated by debug/20260515-gen-sample-app-cs.py for the idl-regress harness.
// Covers every generated user-declared DTO (with RTTI_FULLCLASSNAME), every
// enum, and every ADT branch. Deterministic literal values throughout.
//
// IRT.Transport.UrlEscaper shim — the harness's Driver.csproj excludes
// IRT/UrlEscaper.cs (it depends on System.Web, which is not in net9.0 by
// default). Identifier types call UrlEscaper.Escape/UnEscape from their
// serialization paths, so we supply a minimal replacement backed by
// System.Net.WebUtility.

using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace IRT.Transport {
    public static class UrlEscaper {
        public static string Escape(string s) { return System.Net.WebUtility.UrlEncode(s); }
        public static string UnEscape(string s) { return System.Net.WebUtility.UrlDecode(s); }
    }
}

namespace IdlRegress.CSharpDriver {
    public static class Program {
        private static readonly JsonSerializerSettings S = new JsonSerializerSettings {
            NullValueHandling = NullValueHandling.Ignore,
            Formatting = Formatting.None,
        };

        public static int Main(string[] args) {
            var buf = new SortedDictionary<string, string>(StringComparer.Ordinal);
"""

FOOTER = """\
            foreach (var kv in buf) Console.WriteLine(kv.Key + "\\t" + kv.Value);
            return 0;
        }
    }
}
"""


def main():
    if len(sys.argv) != 3:
        print(f"usage: {sys.argv[0]} <gen-csharp-root> <out-file>", file=sys.stderr)
        sys.exit(2)
    root, out_path = sys.argv[1], sys.argv[2]
    table = build_table(root)
    print(f"parsed {len(table)} types from {root}", file=sys.stderr)
    fixtures = emit_fixtures(table)
    print(f"emitting {len(fixtures)} fixtures", file=sys.stderr)

    lines = [HEADER]
    for wire, scen, expr in fixtures:
        # Escape the scenario string and wire just in case
        key = f"{wire}\\t{scen}"
        lines.append(
            f'            buf["{key}"] = JsonConvert.SerializeObject({expr}, S);\n'
        )
    lines.append(FOOTER)
    with open(out_path, "w", encoding="utf-8") as f:
        f.writelines(lines)


if __name__ == "__main__":
    main()
