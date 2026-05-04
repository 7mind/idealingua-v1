# idealingua-v1 modernization — Task Ledger

Authoritative ledger of planned and completed work. Master plan: `./docs/drafts/20260503-1200-modernization-plan.md`.

The deliverable of this session is **plan documents only**, not implementation. Each PR below produces one plan doc that future implementation work will execute against.

Status: `[ ]` planned · `[~]` in progress · `[x]` done · `[!]` blocked

---

## Milestones (high-level)

- [x] **M1 — Modernization design package.** Three plan documents that together describe (a) the Baboon typer architecture lessons we adopt, (b) the concrete idealingua-v1 typer/IR/backend modernization plan, (c) the wire-format backward-compatibility test harness.
- [~] **M2 — Implementation.** Execute the plans landed in M1. PR-03.1 (pre-modernization Layer A goldens harness) is the first PR; planning complete, R1 resolved (broader scope = 28 `.domain` files), execution in progress.

---

## Milestone 1 — PR breakdown

Detail in `./docs/drafts/20260503-1200-modernization-plan.md`. Sub-task and acceptance-criteria detail lives there; the lines below are pointers, one per deliverable plan doc.

- [x] **PR-01** — `docs/drafts/20260503-PR01-baboon-typer-lessons.md` (810 lines). Distills Baboon's per-domain typer micro-phases, surrounding infrastructure, IR-level table, lessons applied, non-goals. Two adversarial-review rounds; 17 defects raised in round 1, all resolved; round 2 clean.
- [x] **PR-02** — `docs/drafts/20260503-PR02-idealingua-modernization-plan.md` (~1500 lines). Phase-by-phase design of the new typer, file-level diff plan, delete-plan for Go+Protobuf, migration ordering with feature-flag rollout, baseline+target performance plan. Two adversarial-review rounds; round 1 raised 16 defects (all resolved); round 2 found one regressed fix (PR-02-D17, type mismatch on D11); round 3 verified D17 clean.
- [x] **PR-03** — `docs/drafts/20260503-PR03-backcompat-test-harness-plan.md` (~1100 lines). Wire-format safety net (3-layer harness), wire-format spec doc skeleton, pre-modernization baseline workflow with committed freeze tag `wire-format-baseline-2026-05-03`. Three adversarial-review rounds; round 1 raised 11 defects (all resolved); round 2 found 2 minor leftovers (Q3 contradiction, freeze-tag hedge); round 3 caught one ghost reference at line 1160 (resolved); final review clean.

---

## Milestone 2 — PR breakdown

Detail in per-PR plan docs under `./docs/drafts/`. One line per PR here.

- [!] **PR-03.1** — `docs/drafts/20260503-2300-PR0301-baseline-harness-impl-plan.md`. Module skeleton + Layer A scaffold. Single commit on `wip/necromancy`. Adds `idealingua-v1-test-harness` (cross-built 2.13 + 3.8.3) under `./idealingua-v1/idealingua-v1-test-harness/`. Wires four contractual sbt tasks: `regenerateGoldens`, `verifyGoldens` (functional), `runWireFixtures`, `runCrossLangInterop` (no-op placeholders). Generates and commits Layer A goldens under `./idealingua-v1/idealingua-v1-test-defs/golden/{scala,typescript,csharp}/` for **all 28 `.domain` files** under `main-tests/source/` (broader scope per C13). Prerequisite for the `wire-format-baseline-2026-05-03` tag (cut by user post-merge). **Blocked**: F6 surfaced during T2/T3 — legacy `ScalaTranslator` is HashMap-non-deterministic on alias-using domains. Executor applied a 13-line fix in `ScalaTranslator.scala`; the fix is in compiler code (job.md forbids compiler changes in PR-03.1). User must decide before final commit.
- [ ] **PR-03.2** — Layer B wire-byte fixtures (Scala leg of legacy compiler runtime round-trip).
- [ ] **PR-03.3** — Layer B for TS + C#.
- [ ] **PR-03.4** — Layer C cross-language interop matrix.
- [ ] **PR-03.5** — Negative-test corpus (kind-of-diagnostic assertions deferred to PR-02).
- [ ] **PR-03.6** — `docs/wire-format.md` spec doc.
- [ ] **PR-02 IMPL-1..N** — typer rewrite, gated by F2/F5 resolution and freeze-tag baseline.

---

## Cross-cutting architectural notes (locked)

These are decisions or open questions that span multiple PRs. The full enumerated list (with recommendations) is in the meta-plan, §"Cross-cutting decisions". Tracked here as visible state.

User-decided 2026-05-03 in `docs/drafts/20260503-2159-questions-modernization-decisions.md`. Answers folded below.

- [x] **C1 / Q6 — Build alongside, behind feature flag.** New `izumi.idealingua.typer.phase.*` package coexists with `IDLPostTyper`, gated by a flag in `TypespaceCompilerBaseFacade`, until PR-03 harness proves byte-parity.
- [x] **C2 / Q2 — Delete `idealingua-v1-runtime-rpc-go` along with the Go transpiler.** Single atomic deletion in IMPL-8.
- [x] **C3 / Q8 — Keep cross-build (Scala 2.13 + Scala 3) everywhere.** **Collision with PR-02 D14 fix**: `Fingerprint(value: IArray[Byte])` is Scala 3 only. Implementation must substitute either `scodec.bits.ByteVector` or a hand-written wrapper with `equals`/`hashCode`; PR-02 §4 needs an addendum.
- [x] **C4 / Q12 — Lock `wireId = "<pkg>.<name>"` formula permanently.** No type may change package or name without a coordinated wire-format break across all consumers. Codify in `docs/wire-format.md` (PR-03 §4 spec doc).
- [x] **C5 / Q1 — Buzzers used in production; Streams not used.** New IR treats Buzzers first-class on par with Services. Streams: deprecate-but-keep-working in the new IR (no behavior change, no fixtures), schedule removal for a later release.
- [x] **C6 / Q10 — Constants in scope.** New typer adds a `ConstValueTyper` phase; closes the TODOs at `IDLPostTyper.translateValue:240,245,250`.
- [x] **C7 / Q11 — Drop `ForeignType` from the grammar; finish newtype support for all type kinds.** Existing models that use `ForeignType` get a typer diagnostic and stop compiling.
- [x] **C8 / L1 — New typer emits diagnostics, never throws on user errors.** Locked.
- [x] **C9 / Q9 — Hard remove of unused CLI flags on the M2 release.** No deprecation cycle. Deviation from recommendation; user accepts the risk that downstream automations may pass legacy flags.
- [x] **C10 / Q7 — New `idealingua-v1-test-harness` module.** Separates TS/dotnet test-time deps from the compiler module.
- [x] **C11 / L2 — Cross-domain reference graph cycles enforced before dependency-graph construction.** Locked.
- [x] **C12 / L3 — Field-ordering invariant in the new IR.** Locked. New IR preserves struct field declaration order (Circe `deriveEncoder` emits keys in declaration order; current consumers may rely on byte equality).
- [x] **Q3 — TBLOB lock base64-string for all three languages.** PR-02 implements C# in IMPL-N; PR-03 baseline must verify Scala/TS already match.
- [x] **Q4 — TUInt64 hybrid encoding.** Number for values ≤ 2^53 - 1 (JS max safe integer); string for ≥ 2^53. Boundary is documented in the wire-format spec.
- [x] **Q5 — TSet insertion order, mirrored across all three languages.** PR-02 must ensure TS/C# emitters iterate in a way that matches Scala's `LinkedHashSet`.
- [x] **L4 — Layer B byte-strict raw comparison default; canonicalization is a parallel sanity check.** Locked.
- [x] **L5 — Negative-test diagnostic-kind assertions deferred to PR-02.** PR-03 asserts only "legacy throws something". Locked.
- [x] **L6 — Freeze tag `wire-format-baseline-2026-05-03`.** Locked.
- [x] **C13 / R1 — PR-03.1 corpus scope = broader (28 `.domain` files).** Resolved 2026-05-04 by user. Scope: all `.domain` files under `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/`, comprising `idltest/` (22) + `izumi/test/` (5: clashing, clashing/another, domain01, domain02, domain03recursive01) + `overlaytest/` (1: withoverlay). Resolves master plan §11 Q9 (recommendation accepted). Master plan §12 PR-03.1 wording corrected; §3 / §5 narrower phrasings stand as descriptive of the predominant corpus and are read in light of this resolution.

### New follow-ups created by user answers

- [x] **F1 — Substitute Scala-3-only `IArray[Byte]` in PR-02 §4 `Fingerprint` (per C3/Q8 cross-build decision).** Resolved 2026-05-03 — picked `scodec.bits.ByteVector` as the primary type (hand-equality-wrapper noted as fallback). PR-02 §4 IR pseudo-code and the frozen-after-assembly invariant paragraph updated.
- [ ] **F2 — TBLOB cross-language verification pre-baseline.** PR-03.1 (baseline capture) must confirm Scala and TS already encode `TBLOB` as base64-string before the freeze tag is cut; if they don't, Q3 reopens. **Status update (2026-05-03 planner audit)**: vacuous on the in-scope corpus — `command grep -rn 'blb\b\|TBLOB' …/defs/main-tests/source/` returns zero matches. Mark `[x]` once PR-03.1 lands and the executor confirms the grep result against the final corpus.
- [!] **F6 — Legacy `ScalaTranslator` HashMap-iteration non-determinism (surfaced during PR-03.1 T2/T3).** `ScalaTranslator.translate()` at `idealingua-v1-transpilers/src/main/scala/.../toscala/ScalaTranslator.scala:33-38` collects aliases into `.toMultimap` (immutable HashMap) then iterates — emits package-object.scala alias lines in a JVM-instance-dependent order. Empirically reproduced: two cold `sbt regenerateGoldens` runs produced different alias orderings on `idltest/aliases`, `idltest/aliases2`, `izumi/test/domain01..03`. Without a fix, Layer A's byte-strict contract is unenforceable: the freeze tag baselines accidental ordering, and PR-02's typer rewrite cannot reproduce "same legacy ordering". T2/T3 executor applied a 13-line fix (sort by `ModuleId.toString` after `groupBy`) — verified deterministic across multiple sbt-JVM cold starts (700 files, identical sha256). **However**, `job.md` forbids compiler changes in PR-03.1 scope: "Do not touch the typer or any compiler module." Decision pending user: (a) keep the fix as the minimal change required for harness viability, (b) revert and accept fragile baseline, (c) other (e.g., escalate to a separate PR-03.0.5 that lands the determinism fix before PR-03.1's freeze tag is cut). See `defects.md` PR-03.1-D09 for details. Blocks the PR-03.1 final commit.
- [ ] **F5 — TBLOB Q3 divergence (out of PR-03.1 scope; urgent for PR-03.3).** Surfaced during PR-03.1 planning audit: all three transpilers diverge from Q3 (locked as base64-string for all three). (a) Scala maps TBLOB → `Array[Byte]` at `idealingua-v1-transpilers/.../toscala/types/ScalaTypeConverter.scala:122`, which Circe's default `Encoder[Array[Byte]]` encodes as a JSON array of integers (NOT base64). (b) TypeScript emits `???` for TBLOB at `…/totypescript/types/TypeScriptTypeConverter.scala:29, 60, 93, 182, 232, 282, 325` and `…/totypescript/extensions/IntrospectionExtension.scala:34`. (c) C# already known per PR-03-D02 in `defects-m1.md`: `…/tocsharp/extensions/JsonNetExtension.scala:188, 241, 351`; `…/tocsharp/types/CSharpType.scala:53, 213`. User must reconfirm Q3 before any future PR adds TBLOB fixtures: (a) keep base64 lock and patch all three emitters as part of PR-02; (b) downgrade to per-language TBLOB-undefined and disallow TBLOB in the wire-format spec; (c) other.
- [x] **F3 — Streams deprecation note.** Resolved 2026-05-03 — added "Deprecated-but-supported types: Streams" subsection in PR-02 §3 (between Phase 12 and the phase-dependency DAG); rewrote §9 C5 and §12 Q2 to mark the decision RESOLVED.
- [x] **F4 — `ForeignType` removal grammar surface.** Resolved 2026-05-03 — verified via grep that zero `.domain` fixtures under `idealingua-v1-test-defs/` use the `foreign` keyword. Grammar surface to remove enumerated in PR-02 §9 C7: keyword `Keywords.scala:25`, parser entry `DefStructure.scala:131-136`, raw AST nodes `RawTypeDef.scala:32` and `RawTopLevelDefn.scala:24`. §12 Q4 marked RESOLVED.

---

## Completed

- **PR-02** (2026-05-03) — Wrote `docs/drafts/20260503-PR02-idealingua-modernization-plan.md` (~1500 lines), the master plan for replacing the legacy `IDLPostTyper`/`TypespaceImpl` with a multi-phase typer + materialized IR. Twelve sections: §1 goal/scope, §2 strategy (build alongside, feature-flag in `TypespaceCompilerBaseFacade`, atomic-group rules), §3 phase-by-phase typer design (Phase 0 cross-domain index → Phase 12 Validator/Assembler, each with named Input/Output IR + Replaces in current code), §4 IR pseudo-code (Domain, Member, FlatStruct, Fingerprint as `IArray[Byte]`, the field-ordering invariant), §5 backend deletion plan (Go, Protobuf, glue with file:line citations), §6 backend restructure for Scala/TS/C# (with wire-format invariants per backend), §7 CLI/packaging, §8 performance plan, §9 cross-cutting decisions C1-C12, §10 file-level diff table, §11 risks/assumptions, §12 open questions. Verification: 16 defects in round 1 (6 major, 9 minor, 1 nit) — all resolved; round 2 caught one regressed fix (D11→D17, type mismatch fixed by introducing `Domain.userTypes` projection); round 3 verified clean.
  Notes / surprises:
  - The user's "delete Go" mandate had more glue than the meta-plan listed: `Codecs.scala` had 6 implicit defs (not 2); `CredentialsReader.scala` and `CommandlineIDLCompiler.scala:283` were missed entirely. Round-1 review caught all of these; PR-02 §5/§10 now enumerates them.
  - `IArray[Byte]` not `Array[Byte]` for `Fingerprint`. Scala's `Array[Byte]` uses reference equality; the frozen-after-assembly invariant requires structural equality, so the IR uses `IArray[Byte]` (or `ByteVector` as a Scala-2 fallback). PR-02 §4 frozen-invariant section calls this out.
  - The `typespace/structures/` subtree (5 files) is unreachable after IMPL-10 — added to the §10 deletion plan.
  - `ReservedKeywordRule` is **kept**, not deleted: it is an opt-in per-translator rule (cited in each `*TranslatorDescriptor.scala:24`), distinct from the 7 universal rules in `TypespaceVerifier.basicRules`.
  - `ScalaTranslator.scala:71-93` has no `q"trait"` quasiquote; the trait quasiquote lives in `InterfaceRenderer.scala:46-50`. Citation corrected.
  - `Domain.userTypes: Map[TypeId, TypeDef]` is a projection convenience over `Domain.members: Map[TypeId, Member]`. The legacy `ctx.typespace(a)` call sites should map to `ctx.domain.userTypes(a)` so the existing `case TypeDef.Alias(_, target, _)` pattern match continues to compile.
  - Phase numbering convention: cross-domain index = Phase 0 (precondition), structural-fact phases = Phases 5-11. IMPL-N references in §2 must use these numbers.

- **PR-03** (2026-05-03) — Wrote `docs/drafts/20260503-PR03-backcompat-test-harness-plan.md` (~1100 lines), the wire-format safety-net plan. Twelve sections: §1 goal/non-goals, §2 threat model (8 numbered failure modes, each with current-code file:line), §3 three-layer harness (A: generated-source goldens with character-for-character source equality; B: wire-byte fixtures with byte-strict raw comparison as the default; C: cross-language interop tests with generated dispatch tables), §4 wire-format spec skeleton (10 numbered topics, each citing the rule's authoring site), §5 pre-modernization baseline workflow with committed freeze tag `wire-format-baseline-2026-05-03` and four contractual sbt task names (`regenerateGoldens`, `verifyGoldens`, `runWireFixtures`, `runCrossLangInterop`), §6 corpus coverage audit (27 patterns), §7 negative tests (kind-of-diagnostic assertions deferred to PR-02), §8 CI integration, §9 module layout (Option B — new `idealingua-v1-test-harness` module), §10 risks, §11 open questions (12 of them, including TUInt64-near-2^63 policy, TSet iteration order, TBLOB encoding given the C# `???` stub), §12 implementation PR breakdown (PR-03.1 through PR-03.6). Verification: round 1 found 11 defects (3 major, 5 minor, 3 nit) — all resolved; round 2 found 2 minor leftovers (Q3 contradicting §7, freeze-tag hedge undermining D04) — resolved; round 3 caught a ghost `2026-MM-DD` placeholder at line 1160 (resolved); final review clean.
  Notes / surprises:
  - **Byte-strict raw is the default for Layer B**, not canonicalization. Earlier draft demoted byte-strictness to envelope-only and applied canonicalization to inner payloads; that silently accepted field-order regressions. The corrected policy: byte-strict raw for ALL payloads; canonicalization is a *parallel* sanity check that catches cross-language drift.
  - **C# emits `???` for TBLOB** (`JsonNetExtension.scala:188`), so there is no current C# baseline for TBLOB. PR-02 cannot ship a TBLOB change because there is no fixed point. §11 Q12 captures this.
  - **TS Identifier serialization** at `TypeScriptTranslator.scala:500-508` (`toString`/`serialize`) and `:484-491` (string-form parser); TS struct serialize/deserialize at `:205-209,190-201,515-523`. Citations now precise.
  - **Layer C drivers need generated dispatch tables** (one entry per type in the corpus) because TS and C# erase types at runtime. The harness build emits `Dispatch.ts`/`Dispatch.cs` files mapping wireId → typed encode/decode call.
  - **Freeze tag `wire-format-baseline-2026-05-03`** is a committed contract — the date does not float to baseline-landing day.
  - The `idealingua-v1-test-defs/.../source/` corpus has 22 `.domain` files plus subdirs `izumi/` and `overlaytest/` (not `izumi/test/` or `overlays/` as initially miswritten).

- **PR-01** (2026-05-03) — Wrote `docs/drafts/20260503-PR01-baboon-typer-lessons.md` (810 lines), the architectural reference that PR-02 cites. Covers: top-level Baboon pipeline mapped onto idealingua-v1, per-phase coverage of `BaboonTyper.process` (~13 phases plus trivial gluing steps, source-grounded against `BaboonTyper.scala` on the `main` branch), surrounding infrastructure (`ScopeBuilder`, `ScopeSupport`, `BaboonRules`, `BaboonEnquiries`, `RootExtractor`, `AdtInheritanceExpander`, `SymbolNames`, `TypeInfo`, `BaboonValidator`, `BaboonFamilyManager`, with `BaboonComparator` explicitly DROP), IR-level table (Raw → Scoped → Resolved → Structural → Validated → Final), 10 specific transferable lessons each anchored in idealingua-v1 file:line, non-goals (version evolution, UEBA codecs, conversion derivation, version-axis renaming, `any` builtin), and 7 open questions about Baboon. Verification: round 1 review found 17 defects (2 major, 9 minor, 6 nit); a single fix subagent applied all 17 in a single Edit session; round 2 review found no new defects. Two variant fixes flagged for re-reviewer (D08 — row already existed; D13 — `RawTypeDef.Anyvals` does not exist) were verified correct in round 2.
  Notes / surprises:
  - Baboon's default branch is `main`, not `develop`; the meta-plan's incidental `develop` references are stale and should be ignored. The header note in PR-01's deliverable now records this.
  - The originally suggested 13-phase list elides (a) the alias-root post-step on `RootExtractor.roots` (`BaboonTyper.scala:53-60`) and (b) the internal multi-step structure of `runTyper` (`:389-443`, two scope-builds around `AdtInheritanceExpander` plus a final alias sweep). Both are documented in the deliverable.
  - `BasicNamingConventionsRule` is a universal verifier rule (`TypespaceVerifier.scala:12`); `ReservedKeywordRule` is a per-translator concern (each `*TranslatorDescriptor.scala:24`). PR-02 must keep this distinction — reserved-keyword checking is translator-local, not domain-global.
  - `IDLException` throws in idealingua-v1's typer at `IDLTyper.scala:58, 157, 188, 192, 268, 322, 400, 547, 552`; `IllegalArgumentException` at `:115, :195` are intentional invariant assertions and PR-02 should preserve them as such.
  - `RawTypeDef.Anyvals` does NOT exist in the raw AST. "Anyvals" in idealingua-v1 is a Scala-translator-side optimization (`AnyvalExtension.scala:18`), not a typed-AST concept. Future work that mentions Anyvals must say which side it's referring to.
