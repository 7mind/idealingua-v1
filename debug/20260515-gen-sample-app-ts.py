#!/usr/bin/env python3
"""
Sample-app generator for the idl-regress harness — TypeScript target.

Walks a generated idlc TypeScript tree (PLAIN layout, withRuntime=true), parses
every user-declared type (DTO, Identifier, Enum, ADT, Interface->Struct), and
emits a deterministic `sample_app.ts` driver that constructs one fixture per
non-internal type with fixed literal values. Output shape:

    <wireId>\\t<scenario>\\t<json>\\n

Construction uses object-literal form against the generated `…Serialized` shape
(field set matches the per-class `…Serialized` interface).

Field-literal synthesis is type-string driven:

  * Primitive substring match (`number`, `boolean`, `string`).
  * Date fields are passed as strings (ISO-8601 reference instant).
  * Containers (`X[]`, `{[key: string]: X}`) inspected and recursed on element.
  * User-type references resolved via the file-discovered TypeInfo table.

Cycle-breaking: a recursion-set tracks user types in flight; a field whose
type re-enters the set falls back to an empty container if the field is a list
or map, or `undefined` if optional, otherwise a `null as any` sentinel.

Usage:

    python3 20260515-gen-sample-app-ts.py <gen-ts-root> <out-file> [--skip-timestamps]

The optional `--skip-timestamps` flag drops fixtures whose type transitively
references a timestamp-bearing field — used for the v1.4.19-vs-HEAD comparison
because v1.4.19's IRT moment-import is broken under `tsx`.
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
    raw_type: str   # the raw TS type expression from the …Serialized interface
                    # (e.g. "string", "number", "Foo[]", "{[key: string]: BarSerialized}").
    is_uuid: bool = False   # setter contains an `expects guid format` check
    is_blob: bool = False   # setter contains a `expects base64 format` / blob check


@dataclass
class TypeInfo:
    fqn: str                # dotted, e.g. "net.playq.foo.Bar" (matches FullClassName)
    kind: str               # "dto" | "id" | "enum" | "adt" | "struct" | "alias"
    file_path: str          # absolute path to declaring .ts file
    class_name: str         # exported class symbol (e.g. "Bar", "BarStruct")
    serialized_name: str = ""  # the `…Serialized` interface symbol (DTO/struct)
    serialized_fields: List[Field] = field(default_factory=list)  # for dto/struct
    runtime_fields: List[Field] = field(default_factory=list)
        # the `private _name: RuntimeType` declarations, ordered as they appear
        # in the class body. RuntimeType uses TS class symbols (Foo, Foo[], …)
        # — distinct from the Serialized interface's `string`/`number` shape.
    id_string_form: str = ""    # for identifiers — pre-built `Name#…` string
    enum_values: List[str] = field(default_factory=list)
    adt_branches: List[Tuple[str, str, str]] = field(default_factory=list)
        # (tag, branch_class_simple, branch_full_class_name)
    interface_name: str = ""  # for "struct" kind: the parent interface (e.g. "Foo" → struct "FooStruct")
    imports: Dict[str, str] = field(default_factory=dict)
        # simple-class-name → relative module path (e.g. "Foo" → "../bar/Foo").
        # Captured from the containing .ts file's import statements; used to
        # disambiguate cross-package class references.


# ---------------------------------------------------------------------------
# Parser
# ---------------------------------------------------------------------------

RE_CLASS = re.compile(r"export\s+class\s+(\w+)\s*(?:implements\s+([^{]+?))?\s*\{")
RE_INTERFACE = re.compile(r"export\s+interface\s+(\w+)\s*(?:extends\s+[^{]+?)?\s*\{([^}]*)\}")
RE_FULLCLASSNAME = re.compile(r"public\s+static\s+readonly\s+FullClassName\s*=\s*'([^']+)'")
RE_ENUM = re.compile(r"export\s+enum\s+(\w+)\s*\{([^}]+)\}")
RE_ENUM_VAL = re.compile(r"(\w+)\s*=\s*'([^']+)'")
RE_ADT_TYPE = re.compile(r"export\s+type\s+(\w+)\s*=\s*([^;\n]+);?")
RE_ADT_SWITCH_CASE = re.compile(
    # (a) `case 'tag': return XStruct.create(content)` — interface-backed branch.
    r"case\s+'([^']+)'\s*:\s*return\s+(\w+?)(?:Struct)?\.create\(|"
    # (b) `case 'tag': return XHelpers.deserialize(content)` — nested ADT branch.
    r"case\s+'([^']+)'\s*:\s*return\s+(\w+?)Helpers\.deserialize\(|"
    # (c) `case 'tag': return new X(content)` — concrete DTO branch.
    r"case\s+'([^']+)'\s*:\s*return\s+new\s+(\w+)\(",
)
RE_ID_TOSTRING_PREFIX = re.compile(r"return\s+'([A-Za-z0-9_]+)#'\s*\+\s*suffix")


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


def parse_field_decl(decl: str) -> Optional[Field]:
    """Parse `name: Type;` or `name?: Type;` from a TS interface body line."""
    decl = decl.strip().rstrip(";").rstrip(",")
    if not decl:
        return None
    colon = decl.find(":")
    if colon < 0:
        return None
    name = decl[:colon].strip()
    if name.endswith("?"):
        name = name[:-1].rstrip()
    if not re.match(r"^[A-Za-z_]\w*$", name):
        return None
    typ = decl[colon + 1:].strip()
    return Field(name=name, raw_type=typ)


def parse_interface_body(body: str) -> List[Field]:
    fields: List[Field] = []
    # Split on top-level semicolons (no nested ones in this codegen).
    for line in body.split(";"):
        f = parse_field_decl(line)
        if f is not None:
            fields.append(f)
    return fields


def _scan_setter(body: str, fname: str) -> Tuple[bool, bool]:
    """Locate the public setter for `fname` in `body` (handles the optional
    trailing underscore for TS-reserved-word fields) and return
    (is_uuid, is_blob) by string-matching the IRT's generated guards."""
    for accessor in (fname, fname + "_"):
        m = re.search(
            rf"set\s+{re.escape(accessor)}\s*\(\s*value\s*:[^)]*\)\s*\{{",
            body,
        )
        if m is None:
            continue
        setter_open = m.end() - 1
        setter_close = find_matching_brace(body, setter_open)
        if setter_close < 0:
            continue
        setter_body = body[setter_open:setter_close + 1]
        is_uuid = "guid format" in setter_body
        is_blob = "base64" in setter_body
        return is_uuid, is_blob
    return False, False


RE_IMPORT_BLOCK = re.compile(
    r"import\s*\{\s*([^}]+)\s*\}\s*from\s*'([^']+)'\s*;?",
    re.MULTILINE | re.DOTALL,
)


def parse_imports(src: str) -> Dict[str, str]:
    """Parse top-level `import { A, B } from './x'` lines. Returns
    simple-name → relative-module-path."""
    imports: Dict[str, str] = {}
    for m in RE_IMPORT_BLOCK.finditer(src):
        names_blob = m.group(1)
        module = m.group(2)
        for raw in names_blob.split(","):
            n = raw.strip()
            if not n:
                continue
            # Strip aliases (e.g. `Foo as Bar` — idlc doesn't emit these, but be safe).
            n = n.split(" as ")[0].strip()
            if n:
                imports[n] = module
    return imports


def parse_ts_file(path: str) -> List[TypeInfo]:
    """Pragmatic parser — extracts user types from a single idlc-generated TS
    file. Multiple types per file are possible (e.g. struct + interface +
    Serialized + Helpers all coexist)."""
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    file_imports = parse_imports(src)
    results: List[TypeInfo] = []

    # ---- 1. enums
    for m in RE_ENUM.finditer(src):
        ename = m.group(1)
        body = m.group(2)
        vals = [v.group(1) for v in RE_ENUM_VAL.finditer(body)]
        # FQN for an enum is recoverable from the file path; we'll do that pass later.
        results.append(TypeInfo(
            fqn="",  # filled in caller
            kind="enum",
            file_path=path,
            class_name=ename,
            enum_values=vals,
        ))

    # ---- 2. ADT type aliases — only the first one per file is the ADT;
    #       *Serialized aliases are derived. We detect a Helpers class with a
    #       switch on tag-strings as the canonical ADT marker.
    adt_class_match = re.search(r"export\s+class\s+(\w+)Helpers\s*\{", src)
    if adt_class_match:
        adt_name = adt_class_match.group(1)
        # The corresponding `export type <Name> = ...` precedes Helpers.
        # Extract branch tags from the deserialize switch cases.
        helpers_open = adt_class_match.end() - 1
        helpers_close = find_matching_brace(src, helpers_open)
        if helpers_close > 0:
            helpers_body = src[helpers_open:helpers_close + 1]
            branches: List[Tuple[str, str, str]] = []
            for cm in RE_ADT_SWITCH_CASE.finditer(helpers_body):
                if cm.group(1):
                    tag, cls = cm.group(1), cm.group(2)
                elif cm.group(3):
                    tag, cls = cm.group(3), cm.group(4)
                else:
                    tag, cls = cm.group(5), cm.group(6)
                branches.append((tag, cls, ""))  # full name resolved later
            if branches:
                results.append(TypeInfo(
                    fqn="",
                    kind="adt",
                    file_path=path,
                    class_name=adt_name,
                    adt_branches=branches,
                ))

    # ---- 3. Classes (DTO / Identifier / Struct).
    # Iterate class declarations; for each, locate its FullClassName + Serialized
    # interface + identifier toString prefix.
    for m in RE_CLASS.finditer(src):
        cname = m.group(1)
        implements_clause = (m.group(2) or "").strip()
        # body
        open_brace = m.end() - 1
        close_brace = find_matching_brace(src, open_brace)
        if close_brace < 0:
            continue
        body = src[open_brace:close_brace + 1]
        # Skip the Helpers / Introspector / Formatter / IRT sidecar classes —
        # those don't have a FullClassName.
        fcm = RE_FULLCLASSNAME.search(body)
        if fcm is None:
            continue
        fqn = fcm.group(1)

        # Identifier marker: a `toString(): string { … 'Name#' + suffix; }`.
        id_match = RE_ID_TOSTRING_PREFIX.search(body)
        if id_match is not None:
            id_rt_fields: List[Field] = []
            for fm in re.finditer(r"private\s+_(\w+)\s*:\s*([^;]+?);", body):
                fname = fm.group(1)
                ftype = fm.group(2).strip()
                is_uuid, is_blob = _scan_setter(body, fname)
                id_rt_fields.append(Field(
                    name=fname, raw_type=ftype, is_uuid=is_uuid, is_blob=is_blob,
                ))
            results.append(TypeInfo(
                fqn=fqn,
                kind="id",
                file_path=path,
                class_name=cname,
                runtime_fields=id_rt_fields,
                id_string_form="",  # synthesised at fixture time
            ))
            continue

        # Struct marker: the class ends in `Struct` *and* its FullClassName ends
        # in `.Struct` (interface impl-DTO).
        is_struct = cname.endswith("Struct") and fqn.endswith(".Struct")
        kind = "struct" if is_struct else "dto"

        # Serialized interface for this class. Naming convention: `<Cls>Serialized`
        # for DTOs, `<CompositeName>Struct<...>Serialized` for structs — but the
        # constructor signature reveals the actual name.
        ctor_match = re.search(r"constructor\s*\(\s*data\s*:\s*([A-Za-z0-9_]+)", body)
        if ctor_match is None:
            # Some structs have constructor(data: A | B = undefined). Try a wider match.
            ctor_match = re.search(r"constructor\s*\(\s*data\s*:\s*([A-Za-z0-9_|\s]+?)\s*=", body)
        serialized_name = ""
        if ctor_match is not None:
            cand = ctor_match.group(1).strip()
            # If it's `string | IFoo` (identifier path), we won't reach here.
            if " " not in cand and "|" not in cand:
                serialized_name = cand

        # Runtime field shape from `private _x: T;` declarations.
        rt_fields: List[Field] = []
        for fm in re.finditer(r"private\s+_(\w+)\s*:\s*([^;]+?);", body):
            fname = fm.group(1)
            ftype = fm.group(2).strip()
            # Inspect the matching setter body for refinement hints. The
            # public accessor is `set <name>` or `set <name>_` (trailing
            # underscore for TS-reserved words like `from`). Try both.
            is_uuid, is_blob = _scan_setter(body, fname)
            rt_fields.append(Field(
                name=fname, raw_type=ftype, is_uuid=is_uuid, is_blob=is_blob,
            ))

        results.append(TypeInfo(
            fqn=fqn,
            kind=kind,
            file_path=path,
            class_name=cname,
            serialized_name=serialized_name,
            runtime_fields=rt_fields,
            interface_name=fqn.rsplit(".", 1)[0].rsplit(".", 1)[-1] if is_struct else "",
        ))

    # ---- 4. Interfaces — capture each `export interface NameSerialized {…}`
    # as field-shape metadata. We need this even when its host class wasn't
    # discovered first because the parser walks files independently.
    iface_payload: Dict[str, List[Field]] = {}
    # The simple regex above doesn't handle nested braces; do a manual scan.
    i = 0
    while i < len(src):
        m = re.match(r"export\s+interface\s+(\w+)", src[i:])
        if m is None:
            i += 1
            continue
        open_brace = src.find("{", i + m.end())
        if open_brace < 0:
            break
        close_brace = find_matching_brace(src, open_brace)
        if close_brace < 0:
            break
        iface_name = m.group(1)
        body = src[open_brace + 1:close_brace]
        # Strip method signatures (lines ending in `): TYPE;`). Field lines are
        # `name: type;` or `name?: type;`.
        cleaned: List[str] = []
        for raw_line in body.split(";"):
            line = raw_line.strip()
            if not line:
                continue
            if "(" in line:
                continue  # method
            cleaned.append(line)
        fields = []
        for line in cleaned:
            f = parse_field_decl(line)
            if f is not None:
                fields.append(f)
        iface_payload[iface_name] = fields
        i = close_brace + 1

    # Attach Serialized fields to classes.
    for t in results:
        if t.kind in ("dto", "struct"):
            if t.serialized_name and t.serialized_name in iface_payload:
                t.serialized_fields = iface_payload[t.serialized_name]

    return results


# ---------------------------------------------------------------------------
# Tree walker
# ---------------------------------------------------------------------------

def build_table(root: str) -> Tuple[Dict[str, TypeInfo], Dict[str, str]]:
    """Walks the generated TS tree. Returns:
       * `by_fqn`: TypeInfo keyed by FullClassName (`net.playq.foo.Bar`)
       * `class_to_fqn`: simple-class-name → list-of-fqns (for ADT branch
         resolution, where the deserialize switch references the class symbol).
    """
    by_fqn: Dict[str, TypeInfo] = {}
    class_to_fqn: Dict[str, str] = {}
    enum_pending: List[Tuple[TypeInfo, str]] = []   # (info, file_dotted_path)
    adt_pending: List[Tuple[TypeInfo, str]] = []

    for dirpath, _, files in os.walk(root):
        # Skip the IRT runtime tree.
        rel = os.path.relpath(dirpath, root)
        if rel.startswith("irt") or rel == "irt":
            continue
        for fn in files:
            if not fn.endswith(".ts"):
                continue
            path = os.path.join(dirpath, fn)
            types = parse_ts_file(path)
            # Recover dotted package from the file path (dir-relative-to-root).
            rel_parent = os.path.relpath(dirpath, root).replace(os.sep, ".")
            # Re-parse the file once for imports to attach to every TypeInfo.
            try:
                with open(path, "r", encoding="utf-8") as ff:
                    file_imports = parse_imports(ff.read())
            except OSError:
                file_imports = {}
            for t in types:
                if t.kind in ("enum", "adt"):
                    # Manufacture FQN from path: <dotted-parent>.<class_name>.
                    if rel_parent and rel_parent != ".":
                        t.fqn = f"{rel_parent}.{t.class_name}"
                    else:
                        t.fqn = t.class_name
                t.imports = file_imports
                if t.fqn:
                    by_fqn[t.fqn] = t
                    class_to_fqn[t.class_name] = t.fqn
    return by_fqn, class_to_fqn


# ---------------------------------------------------------------------------
# Literal synthesizer
# ---------------------------------------------------------------------------

REFERENCE_DATETIME = "2024-01-01T00:00:00.000Z"  # ISO with millis + Z (parses via Formatter)
REFERENCE_DATE = "2024-01-01"
REFERENCE_TIME = "00:00:00.000"
REFERENCE_UUID = "00000000-0000-0000-0000-000000000000"

# A field whose type is `string` MIGHT really be a Date-string in the Serialized
# layer (timestamps round-trip as ISO strings). We can't tell from `string`
# alone, but the canonical raw_type for those fields in the Serialized
# interface is `string`. The IRT only crashes on Date *reads* — passing an ISO
# string survives. So we leave the literal as "s1" for plain strings and only
# special-case fields that surface as `Date` (the non-Serialized class shape;
# not what we serialize).
DEFAULT_STRING = "s1"

# Recognize a few common primitive shapes from the raw TS type string.
RE_OPTIONAL_UNDEF = re.compile(r"\|\s*undefined\s*$")
RE_ARRAY = re.compile(r"^(.+)\[\]$")
RE_MAP_STR_TO_X = re.compile(
    r"^\{\s*\[\s*key\s*:\s*(string|number)\s*\]\s*:\s*(.+)\s*\}$",
)


def is_timestamp_field_name(name: str) -> bool:
    # Heuristic for the --skip-timestamps pass. The Serialized layer types
    # timestamps as `string`, which we can't distinguish from a real string
    # field. We approximate by name: any field whose name contains "At" / "Date"
    # / "Time" / "Timestamp" / "ts" suffix-style is flagged. Used only to
    # decide whether to drop the *entire* fixture from the v1.4.19 run.
    n = name
    needles = ("At", "Date", "Time", "Timestamp", "_at", "When")
    return any(s in n for s in needles)


def strip_optional(t: str) -> Tuple[str, bool]:
    """Strip a trailing `| undefined` (also leading `undefined |`). Returns
    (inner, optional)."""
    s = t.strip()
    m = RE_OPTIONAL_UNDEF.search(s)
    if m:
        return s[:m.start()].strip(), True
    if s.startswith("undefined |"):
        return s[len("undefined |"):].strip(), True
    return s, False


def is_array(t: str) -> Optional[str]:
    m = RE_ARRAY.match(t)
    if m:
        return m.group(1).strip()
    return None


def is_map(t: str) -> Optional[str]:
    m = RE_MAP_STR_TO_X.match(t)
    if m:
        return m.group(2).strip()
    return None


def map_key_kind(t: str) -> str:
    """Return the JS map key kind ('string' or 'number') for a parsed map type."""
    m = RE_MAP_STR_TO_X.match(t)
    if m:
        return m.group(1)
    return "string"


def type_terminates(
    rt_type: str,
    table: Dict[str, TypeInfo],
    visited: Set[str],
    seen: Set[str],
    ctx: Optional[TypeInfo] = None,
) -> bool:
    """True iff a value of `rt_type` can be constructed without re-entering
    any type in `visited`. Containers always terminate (they can be empty).
    """
    t, _ = strip_optional(rt_type)
    if t in ("number", "string", "boolean", "Date", "any", "unknown"):
        return True
    if is_array(t) is not None or is_map(t) is not None:
        return True
    info = resolve_in_ctx(table, ctx, simple_class(t))
    if info is None:
        # Maybe a `Foo` interface mapping to `FooStruct`.
        info = resolve_in_ctx(table, ctx, simple_class(t) + "Struct")
        if info is None:
            return True  # unknown — optimistic
    if info.kind in ("enum", "id"):
        return True
    if info.fqn in visited:
        return False
    if info.fqn in seen:
        return True   # cycle local to this probe — break optimistically
    next_seen = seen | {info.fqn}
    if info.kind in ("dto", "struct"):
        return all(
            type_terminates(f.raw_type, table, visited, next_seen, info)
            for f in (info.runtime_fields or info.serialized_fields)
        )
    if info.kind == "adt":
        if not info.adt_branches:
            return False
        for _, branch_cls, _ in info.adt_branches:
            branch_info = resolve_in_ctx(table, info, branch_cls)
            if branch_info is None:
                continue
            if branch_info.fqn in visited:
                continue
            if all(
                type_terminates(f.raw_type, table, visited, next_seen, branch_info)
                for f in (branch_info.runtime_fields or branch_info.serialized_fields)
            ):
                return True
        return False
    return True


def lookup_by_class_name(table: Dict[str, TypeInfo], cls: str) -> Optional[TypeInfo]:
    for cand in table.values():
        if cand.class_name == cls:
            return cand
    return None


def resolve_in_ctx(
    table: Dict[str, TypeInfo],
    ctx: Optional[TypeInfo],
    cls: str,
) -> Optional[TypeInfo]:
    """Resolve a class-symbol reference within the import-scope of `ctx`.

    Disambiguates duplicates (same simple name in multiple packages) by
    rewriting the relative import path → absolute path → matching TypeInfo's
    file_path. Falls back to plain class-name lookup."""
    if ctx is not None and cls in ctx.imports:
        rel_module = ctx.imports[cls]
        # Resolve relative to ctx.file_path's directory.
        ctx_dir = os.path.dirname(ctx.file_path)
        # rel_module is like './Foo' or '../bar/Baz' (no .ts extension).
        target_path = os.path.normpath(os.path.join(ctx_dir, rel_module)) + ".ts"
        for cand in table.values():
            if cand.class_name == cls and cand.file_path == target_path:
                return cand
        # An Interface named `Foo` only ships an `export class FooStruct` —
        # the interface itself isn't in the table. Try the Struct sibling
        # declared in the same file.
        for cand in table.values():
            if (cand.kind == "struct"
                and cand.class_name == cls + "Struct"
                and cand.file_path == target_path):
                return cand
        # Some imports point at a barrel re-export (index.ts) — fall through.
        # Resolve the directory's index.ts and look for any class named `cls`
        # declared anywhere under that directory tree.
        target_dir = os.path.normpath(os.path.join(ctx_dir, rel_module))
        if os.path.isdir(target_dir):
            for cand in table.values():
                if cand.class_name == cls and cand.file_path.startswith(target_dir + os.sep):
                    return cand
            for cand in table.values():
                if (cand.kind == "struct"
                    and cand.class_name == cls + "Struct"
                    and cand.file_path.startswith(target_dir + os.sep)):
                    return cand
    return lookup_by_class_name(table, cls)


def synth_serialized_for_runtime(
    rt_type: str,
    table: Dict[str, TypeInfo],
    visited: Set[str],
    ctx: Optional[TypeInfo] = None,
) -> str:
    """Return a JS expression producing a *Serialized* value of a field whose
    declared *runtime* type is `rt_type`.

    The Serialized form depends on what `rt_type` resolves to:
      - primitive (number/string/boolean) → literal
      - enum class → quoted enum-value name
      - identifier class → quoted `Name#...` string in toString format
      - DTO/Struct class → recursive Serialized object
      - ADT type alias → `{[tag]: branchSerialized}`
      - `T[]` → `[serialised(T)]`
      - `{[key: string]: T}` → `{[\"k1\"]: serialised(T)}`
      - `T | undefined` → recurse on T (we populate optionals when well-founded)
      - `Date` → ISO instant string (Serialized form is `string`, but the runtime
        class accessor is a Date; the *.toString-form for Serialized is also string)
    """
    t, opt = strip_optional(rt_type)

    # Arrays
    elem = is_array(t)
    if elem is not None:
        elem_clean, _ = strip_optional(elem.strip())
        if not type_terminates(elem_clean, table, visited, set(), ctx):
            return "[]"
        inner = synth_serialized_for_runtime(elem, table, visited, ctx)
        return f"[{inner}]"

    # Maps
    elem = is_map(t)
    if elem is not None:
        elem_clean, _ = strip_optional(elem.strip())
        if not type_terminates(elem_clean, table, visited, set(), ctx):
            return "{}"
        inner = synth_serialized_for_runtime(elem, table, visited, ctx)
        if map_key_kind(t) == "number":
            return f'{{ [1]: {inner} }}'
        return f'{{ ["k1"]: {inner} }}'

    # Primitives
    if t == "string":
        return f'"{DEFAULT_STRING}"'
    if t == "number":
        return "1"
    if t == "boolean":
        return "true"
    if t == "Date":
        # Serialized timestamp form: ISO-8601 with millis + Z (parses via the
        # IRT Formatter on HEAD; the v1.4.19 path crashes before the Date is
        # consumed, regardless of the literal — that case is handled by the
        # --skip-timestamps pass).
        return f'"{REFERENCE_DATETIME}"'
    if t == "any" or t == "unknown":
        return "null as any"

    # Try resolving as a user type by class name.
    info = resolve_in_ctx(table, ctx, simple_class(t))
    is_interface_envelope = False
    if info is not None and info.kind == "struct" and info.class_name == simple_class(t) + "Struct":
        # The queried name was the bare Interface name (`Foo`); resolution
        # mapped it to its `FooStruct`. The Serialized form for an interface
        # field is the polymorphic envelope `{[FullClassName]: structSerialized}`.
        is_interface_envelope = True
    if info is None:
        # Could be an ADT type alias whose Serialized form is a union; or a
        # `…Serialized` symbol from a nested constructor signature.
        if t.endswith("Serialized"):
            stem = t[: -len("Serialized")]
            info = resolve_in_ctx(table, ctx, stem)
        if info is None:
            return f"/*UNRESOLVED:{t}*/ null as any"

    if is_interface_envelope:
        if info.fqn in visited:
            return f'{{ "{info.fqn}": {{}} }}'
        inner = synth_serialized_object(info, table, visited | {info.fqn})
        return f'{{ "{info.fqn}": {inner} }}'

    if info.kind == "enum":
        if info.enum_values:
            return f'"{info.enum_values[0]}"'
        return '"unknown"'
    if info.kind == "id":
        return synth_identifier_string(info, table, visited)
    if info.kind == "adt":
        if not info.adt_branches:
            return "null as any"
        next_visited = visited | {info.fqn}
        # Prefer a branch whose value terminates without re-entering visited.
        chosen = None
        for tag, branch_cls, _ in info.adt_branches:
            branch_info = resolve_in_ctx(table, info, branch_cls)
            if branch_info is None:
                branch_info = resolve_in_ctx(table, info, branch_cls + "Struct")
            if branch_info is None:
                continue
            if branch_info.fqn in next_visited:
                continue
            if branch_info.kind == "adt":
                # Nested ADT — pessimistically accept (its own picker handles
                # its sub-cycle).
                chosen = (tag, branch_info)
                break
            fields = branch_info.runtime_fields or branch_info.serialized_fields
            if all(
                type_terminates(
                    f.raw_type, table,
                    next_visited | {branch_info.fqn}, set(), branch_info,
                )
                for f in fields
            ):
                chosen = (tag, branch_info)
                break
        if chosen is None:
            # No terminating branch — fall back to first branch with empty payload.
            tag, branch_cls, _ = info.adt_branches[0]
            return f'{{ "{tag}": {{}} }}'
        tag, branch_info = chosen
        if branch_info.kind == "adt":
            # Recurse — the wire shape for a nested ADT branch is its own
            # `{innerTag: …}` envelope.
            inner = synth_serialized_for_runtime(
                branch_info.class_name, table, next_visited, info,
            )
        else:
            inner = synth_serialized_object(
                branch_info, table, next_visited | {branch_info.fqn},
            )
            if branch_info.kind == "struct":
                inner = f'{{ "{branch_info.fqn}": {inner} }}'
        return f'{{ "{tag}": {inner} }}'
    if info.kind in ("dto", "struct"):
        if info.fqn in visited:
            return "{} as any"
        return synth_serialized_object(info, table, visited | {info.fqn})
    return "null as any"


def simple_class(t: str) -> str:
    """Strip generic args / array suffix / optional from a runtime type expr,
    returning a bare class identifier candidate."""
    s = t.strip()
    s, _ = strip_optional(s)
    elem = is_array(s)
    if elem is not None:
        s = elem.strip()
    elem = is_map(s)
    if elem is not None:
        s = elem.strip()
    # Drop generic args
    if "<" in s:
        s = s.split("<", 1)[0]
    return s


def synth_identifier_string(
    info: TypeInfo,
    table: Dict[str, TypeInfo],
    visited: Set[str],
) -> str:
    """Build the `Name#part1:part2` toString-form for an Identifier.

    Format taken from the generated `toString()`: parts are URL-encoded and
    joined by `:` in the order the toString method composes them — NOT
    necessarily the declaration order. We parse the toString body to recover
    the actual part sequence by scanning `this.<name>` references.
    """
    # Parse the toString body to recover the part-name sequence.
    part_order = parse_id_part_order(info.file_path, info.class_name)
    if part_order:
        # Build a name->Field lookup for the runtime fields.
        rt_by_name: Dict[str, Field] = {f.name: f for f in info.runtime_fields}
        ordered_fields = [rt_by_name[n] for n in part_order if n in rt_by_name]
    else:
        ordered_fields = info.runtime_fields
    parts: List[str] = []
    for f in ordered_fields:
        t, _ = strip_optional(f.raw_type)
        if f.is_uuid:
            parts.append(REFERENCE_UUID)
            continue
        if f.is_blob:
            parts.append("aGkh")
            continue
        if t == "number":
            parts.append("1")
        elif t == "boolean":
            parts.append("true")
        elif t == "string":
            parts.append(DEFAULT_STRING)
        elif t == "Date":
            parts.append(REFERENCE_DATETIME)
        else:
            sub = resolve_in_ctx(table, info, simple_class(t))
            if sub is None:
                parts.append(DEFAULT_STRING)
            elif sub.kind == "enum":
                parts.append(sub.enum_values[0] if sub.enum_values else "unknown")
            elif sub.kind == "id":
                # Nested identifier: its toString form is the whole `Name#…`
                # string. We keep it RAW here — the single `url_encode` pass at
                # the bottom of this function applies the one expected
                # `encodeURIComponent` (matches `encodeURIComponent(this.x.toString())`).
                nested = synth_identifier_string(sub, table, visited)
                if nested.startswith('"') and nested.endswith('"') and "+" not in nested:
                    parts.append(nested[1:-1])
                else:
                    parts.append(DEFAULT_STRING)
            else:
                parts.append(DEFAULT_STRING)
    encoded_parts = [url_encode(p) for p in parts]
    return f'"{info.class_name}#' + ":".join(encoded_parts) + '"'


def parse_id_part_order(path: str, class_name: str) -> List[str]:
    """Parse an Identifier .ts file's `toString` method body. Returns the list
    of field names in the order they appear inside the generated
    `encodeURIComponent(this.<name>...)` chain — which matches the
    constructor's `split(':')` parsing order."""
    try:
        with open(path, "r", encoding="utf-8") as f:
            src = f.read()
    except OSError:
        return []
    # The toString function appears once per class file. Find it.
    m = re.search(r"public\s+toString\s*\(\s*\)\s*:\s*string\s*\{", src)
    if m is None:
        return []
    open_brace = m.end() - 1
    close_brace = find_matching_brace(src, open_brace)
    if close_brace < 0:
        return []
    body = src[open_brace:close_brace + 1]
    names = []
    for nm in re.finditer(r"this\.(\w+)", body):
        fname = nm.group(1)
        if fname not in names:
            names.append(fname)
    return names


def url_encode(s: str) -> str:
    """Apply a minimal subset of encodeURIComponent for the characters that
    might appear in our hard-coded literals (`#`, `:`, `+`, etc.). For our
    canonical literal palette (`s1`, `1`, `true`, enum-name) the encoded form
    is identical to the source — only nested-id strings carry `#` and `:`."""
    import urllib.parse
    return urllib.parse.quote(s, safe="-_.~!*'()")


def synth_serialized_object(
    info: TypeInfo,
    table: Dict[str, TypeInfo],
    visited: Set[str],
) -> str:
    """Build a `{ field: lit, … }` Serialized object literal.

    Uses `runtime_fields` to know the real per-field type (the Serialized
    interface flattens enums/identifiers to `string`, which is insufficient
    to construct a valid object that survives the runtime setter checks)."""
    fields = info.runtime_fields if info.runtime_fields else info.serialized_fields
    if not fields:
        return "{}"
    parts: List[str] = []
    for f in fields:
        if f.is_uuid:
            lit = f'"{REFERENCE_UUID}"'
        elif f.is_blob:
            lit = '"aGkh"'  # base64 of "hi!"
        else:
            lit = synth_serialized_for_runtime(f.raw_type, table, visited, info)
        parts.append(f"{f.name}: {lit}")
    return "{ " + ", ".join(parts) + " }"


# ---------------------------------------------------------------------------
# Timestamp detection — transitively flag types whose Serialized shape carries
# a field whose name suggests a Date/Time/Timestamp.
# ---------------------------------------------------------------------------

def collect_timestamp_typed_fqns(table: Dict[str, TypeInfo]) -> Set[str]:
    flagged: Set[str] = set()
    # Direct: any DTO/struct whose runtime field type is `Date` (the IRT
    # accessor reads/writes via `Formatter`, which `import`s moment — broken
    # on v1.4.19 under tsx).
    for info in table.values():
        if info.kind not in ("dto", "struct"):
            continue
        for f in info.runtime_fields:
            t, _ = strip_optional(f.raw_type)
            elem = is_array(t) or is_map(t) or t
            if elem == "Date":
                flagged.add(info.fqn)
                break

    # Propagate: walk a fixed-point closure where a type referencing a flagged
    # type (directly via a Serialized symbol) becomes flagged.
    changed = True
    while changed:
        changed = False
        for info in table.values():
            if info.fqn in flagged:
                continue
            if info.kind == "adt":
                for _, branch_cls, _ in info.adt_branches:
                    sub = resolve_in_ctx(table, info, branch_cls)
                    if sub is not None and sub.fqn in flagged:
                        flagged.add(info.fqn)
                        changed = True
                        break
                continue
            if info.kind not in ("dto", "struct"):
                continue
            for f in info.runtime_fields:
                t, _ = strip_optional(f.raw_type)
                elem = is_array(t) or is_map(t) or t
                sub = resolve_in_ctx(table, info, simple_class(elem))
                if sub is not None and sub.fqn in flagged:
                    flagged.add(info.fqn)
                    changed = True
                    break
    return flagged


# ---------------------------------------------------------------------------
# Fixture emission
# ---------------------------------------------------------------------------

def is_internal_name(class_name: str) -> bool:
    if class_name.endswith("Helpers"):
        return True
    if class_name.endswith("Client") or class_name.endswith("Server"):
        return True
    if class_name.endswith("Dispatcher"):
        return True
    if class_name.endswith("Marshaller") or class_name.endswith("Marshallers"):
        return True
    return False


def relative_import_path(out_dir: str, target_path: str) -> str:
    rel = os.path.relpath(target_path, out_dir)
    rel = rel[:-3] if rel.endswith(".ts") else rel  # strip .ts
    if not rel.startswith("."):
        rel = "./" + rel
    return rel


def emit_app(table: Dict[str, TypeInfo], gen_root: str, skip_timestamps: bool) -> str:
    """Return the contents of `sample_app.ts`."""
    # Filter types.
    fqns = sorted(table.keys())

    flagged_ts = collect_timestamp_typed_fqns(table) if skip_timestamps else set()

    chosen: List[TypeInfo] = []
    for fqn in fqns:
        info = table[fqn]
        if is_internal_name(info.class_name):
            continue
        if skip_timestamps and fqn in flagged_ts:
            continue
        chosen.append(info)

    # Build per-class import map. We import each used class symbol with an
    # alias keyed by FQN to avoid name collisions across packages.
    # The sample is dropped at `<project>/.idl-regression/sample_app.ts`, and
    # the harness copies it into `<workDir>/sample_app.ts` alongside the
    # generated tree (workDir root). Imports therefore resolve relative to the
    # workdir root, NOT the project location. The generated tree files live
    # at `<workDir>/<pkg-dotted-as-dirs>/<Class>.ts`.
    import_lines: List[str] = []
    alias_for_fqn: Dict[str, str] = {}
    for info in chosen:
        # Path from sample_app.ts to the generated module
        rel = os.path.relpath(info.file_path, gen_root)
        if rel.endswith(".ts"):
            rel = rel[:-3]
        rel = "./" + rel.replace(os.sep, "/")
        alias = "T_" + info.fqn.replace(".", "_")
        alias_for_fqn[info.fqn] = alias
        if info.kind == "enum":
            # enums are exported as the bare enum symbol.
            import_lines.append(f"import {{ {info.class_name} as {alias} }} from {rel!r};")
        elif info.kind == "adt":
            # ADT type aliases — we don't actually need to import the union
            # type itself; we just emit the serialized envelope as a literal.
            # Skip the import for ADTs.
            continue
        else:
            # dto / struct / id
            import_lines.append(f"import {{ {info.class_name} as {alias} }} from {rel!r};")

    emit_lines: List[str] = []
    for info in chosen:
        wire_id = info.fqn
        scenario = "default"
        if info.kind == "enum":
            # Enum fixtures: emit one per value, JSON is just the string.
            for val in info.enum_values:
                emit_lines.append(
                    f'console.log("{wire_id}" + "\\t" + "{val}" + "\\t" + JSON.stringify("{val}"));'
                )
            continue
        if info.kind == "adt":
            # ADT fixture: one envelope per branch.
            for tag, branch_cls, _ in info.adt_branches:
                branch_info = resolve_in_ctx(table, info, branch_cls)
                if branch_info is None:
                    # Branch references an Interface symbol — resolve to its Struct.
                    branch_info = resolve_in_ctx(table, info, branch_cls + "Struct")
                if branch_info is None or branch_info.kind not in ("dto", "struct"):
                    payload = "{}"
                else:
                    payload = synth_serialized_object(
                        branch_info, table, {info.fqn, branch_info.fqn},
                    )
                    if branch_info.kind == "struct":
                        payload = f'{{ "{branch_info.fqn}": {payload} }}'
                envelope = f"{{ {tag!r}: {payload} }}"
                emit_lines.append(
                    f'console.log("{wire_id}" + "\\t" + "{tag}" + "\\t" + JSON.stringify({envelope}));'
                )
            continue
        if info.kind == "id":
            # Identifier: serialize() returns toString(), which is
            # `<Name>#<urlencoded-fields-joined-by-:>`. We pass the string form
            # to the constructor — the round-trip exercises parsing.
            alias = alias_for_fqn[info.fqn]
            id_str_expr = synth_identifier_string(info, table, set())
            emit_lines.append(
                f'console.log("{wire_id}" + "\\t" + "{scenario}" + "\\t" + '
                f'JSON.stringify(new {alias}({id_str_expr}).serialize()));'
            )
            continue
        if info.kind in ("dto", "struct"):
            alias = alias_for_fqn[info.fqn]
            payload = synth_serialized_object(info, table, {info.fqn})
            emit_lines.append(
                f'console.log("{wire_id}" + "\\t" + "{scenario}" + "\\t" + '
                f'JSON.stringify(new {alias}({payload}).serialize()));'
            )
            continue

    prologue = (
        "// Auto-generated by debug/20260515-gen-sample-app-ts.py for the idl-regress harness.\n"
        "// Covers every generated user type (DTO, Identifier, Enum, ADT, Interface->Struct)\n"
        "// with deterministic literal fixtures. Output: one `<wireId>\\t<scenario>\\t<json>`\n"
        "// line per fixture on stdout.\n"
        "\n"
    )
    return prologue + "\n".join(import_lines) + "\n\n" + "\n".join(emit_lines) + "\n"


def main():
    args = sys.argv[1:]
    skip_timestamps = False
    if "--skip-timestamps" in args:
        skip_timestamps = True
        args = [a for a in args if a != "--skip-timestamps"]
    if len(args) != 2:
        print(
            f"usage: {sys.argv[0]} <gen-ts-root> <out-file> [--skip-timestamps]",
            file=sys.stderr,
        )
        sys.exit(2)
    root, out_path = args
    table, _ = build_table(root)
    print(f"parsed {len(table)} types from {root}", file=sys.stderr)
    if skip_timestamps:
        ts_flagged = collect_timestamp_typed_fqns(table)
        print(f"--skip-timestamps: skipping {len(ts_flagged)} timestamp-bearing types", file=sys.stderr)
    src = emit_app(table, root, skip_timestamps)
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(src)
    n_lines = src.count("console.log(")
    print(f"emitted {n_lines} fixtures to {out_path}", file=sys.stderr)


if __name__ == "__main__":
    main()
