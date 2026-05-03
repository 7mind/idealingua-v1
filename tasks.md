# idealingua-v1 modernization — Task Ledger

Authoritative ledger of planned and completed work. Master plan: `./docs/drafts/20260503-1200-modernization-plan.md`.

The deliverable of this session is **plan documents only**, not implementation. Each PR below produces one plan doc that future implementation work will execute against.

Status: `[ ]` planned · `[~]` in progress · `[x]` done · `[!]` blocked

---

## Milestones (high-level)

- [~] **M1 — Modernization design package.** Three plan documents that together describe (a) the Baboon typer architecture lessons we adopt, (b) the concrete idealingua-v1 typer/IR/backend modernization plan, (c) the wire-format backward-compatibility test harness.
- [ ] **M2 — Implementation.** Execute the plans landed in M1. Out of scope for this session.

---

## Milestone 1 — PR breakdown

Detail in `./docs/drafts/20260503-1200-modernization-plan.md`. Sub-task and acceptance-criteria detail lives there; the lines below are pointers, one per deliverable plan doc.

- [x] **PR-01** — `docs/drafts/20260503-PR01-baboon-typer-lessons.md` (810 lines). Distills Baboon's per-domain typer micro-phases, surrounding infrastructure, IR-level table, lessons applied, non-goals. Two adversarial-review rounds; 17 defects raised in round 1, all resolved; round 2 clean.
- [ ] **PR-02** — Write `docs/drafts/20260503-PR02-idealingua-modernization-plan.md`. Phase-by-phase design of the new typer (proposed package `izumi.idealingua.typer.phase.*`), file-level diff plan (add/modify/delete), delete-plan for `togolang/`+`toprotobuf/`+`idealingua-v1-runtime-rpc-go/`, migration ordering with feature-flag rollout via `TypespaceCompilerBaseFacade`, baseline+target performance measurement plan over the 22 test domains. **Depends on PR-01.**
- [ ] **PR-03** — Write `docs/drafts/20260503-PR03-backcompat-test-harness-plan.md`. Wire-format safety net: (a) generated-source goldens at `idealingua-v1-test-defs/golden/<lang>/<domain>/`, (b) JSON-corpus fixtures decoded/re-encoded/byte-compared by Scala/TS/C# runtimes, (c) cross-language interop tests (ADT, interface, nested struct, identifier, enum, map). Wire-format spec doc skeleton (envelope from `packets.scala`, `wireId` formula from `TypeId.scala:31`, ADT/interface JSON shape from `CirceTranslatorExtensionBase.scala:49,110`). Pre-modernization baseline step that captures all goldens against the *current* compiler before any typer change lands. Can run in parallel with PR-02 once PR-01 is done.

---

## Cross-cutting architectural notes (locked)

These are decisions or open questions that span multiple PRs. The full enumerated list (with recommendations) is in the meta-plan, §"Cross-cutting decisions". Tracked here as visible state.

- [ ] **C1 — Build alongside vs. in-place.** Recommended: alongside, behind a feature flag, until harness proves byte-parity.
- [ ] **C2 — Delete `idealingua-v1-runtime-rpc-go` along with the Go transpiler?** Recommended yes.
- [ ] **C3 — Drop Scala 2.13 in the compiler module while keeping it in the runtime?** Open.
- [ ] **C4 — Lock `wireId = "<pkg>.<name>"` formula permanently.** No type may change package without a wire-format break.
- [ ] **C5 — Are Buzzers/Streams still used in production?** If yes, first-class in new IR; if no, deprecate-but-keep-working.
- [ ] **C6 — Are Constants (`RawVal`/`ConstValue`) in scope for the typer rewrite?** Recommended yes (the current TODOs around them are a known correctness gap).
- [ ] **C7 — Are Newtypes / ForeignType in scope for "make it work" or "delete syntax"?** Open.
- [x] **C8 — New typer emits diagnostics, never throws on user errors.** Locked (replaces `IDLException` from typer call paths).
- [ ] **C9 — CLI flag stability for `CommandlineIDLCompiler`.** Recommended: deprecation cycle for any flag rename/removal.
- [ ] **C10 — Where does the test harness live (compiler module's empty test tree, or a new `idealingua-v1-test-harness` module)?** Open.
- [x] **C11 — Cross-domain reference graph cycles enforced before dependency-graph construction, not after.** Locked.
- [ ] **C12 — Field-ordering invariant in the new IR.** The new IR must preserve declaration order on structs because Circe `deriveEncoder` emits keys in field-declaration order and current consumers may rely on byte equality. PR-02 must call this out explicitly.

---

## Completed

- **PR-01** (2026-05-03) — Wrote `docs/drafts/20260503-PR01-baboon-typer-lessons.md` (810 lines), the architectural reference that PR-02 cites. Covers: top-level Baboon pipeline mapped onto idealingua-v1, per-phase coverage of `BaboonTyper.process` (~13 phases plus trivial gluing steps, source-grounded against `BaboonTyper.scala` on the `main` branch), surrounding infrastructure (`ScopeBuilder`, `ScopeSupport`, `BaboonRules`, `BaboonEnquiries`, `RootExtractor`, `AdtInheritanceExpander`, `SymbolNames`, `TypeInfo`, `BaboonValidator`, `BaboonFamilyManager`, with `BaboonComparator` explicitly DROP), IR-level table (Raw → Scoped → Resolved → Structural → Validated → Final), 10 specific transferable lessons each anchored in idealingua-v1 file:line, non-goals (version evolution, UEBA codecs, conversion derivation, version-axis renaming, `any` builtin), and 7 open questions about Baboon. Verification: round 1 review found 17 defects (2 major, 9 minor, 6 nit); a single fix subagent applied all 17 in a single Edit session; round 2 review found no new defects. Two variant fixes flagged for re-reviewer (D08 — row already existed; D13 — `RawTypeDef.Anyvals` does not exist) were verified correct in round 2.
  Notes / surprises:
  - Baboon's default branch is `main`, not `develop`; the meta-plan's incidental `develop` references are stale and should be ignored. The header note in PR-01's deliverable now records this.
  - The originally suggested 13-phase list elides (a) the alias-root post-step on `RootExtractor.roots` (`BaboonTyper.scala:53-60`) and (b) the internal multi-step structure of `runTyper` (`:389-443`, two scope-builds around `AdtInheritanceExpander` plus a final alias sweep). Both are documented in the deliverable.
  - `BasicNamingConventionsRule` is a universal verifier rule (`TypespaceVerifier.scala:12`); `ReservedKeywordRule` is a per-translator concern (each `*TranslatorDescriptor.scala:24`). PR-02 must keep this distinction — reserved-keyword checking is translator-local, not domain-global.
  - `IDLException` throws in idealingua-v1's typer at `IDLTyper.scala:58, 157, 188, 192, 268, 322, 400, 547, 552`; `IllegalArgumentException` at `:115, :195` are intentional invariant assertions and PR-02 should preserve them as such.
  - `RawTypeDef.Anyvals` does NOT exist in the raw AST. "Anyvals" in idealingua-v1 is a Scala-translator-side optimization (`AnyvalExtension.scala:18`), not a typed-AST concept. Future work that mentions Anyvals must say which side it's referring to.
