# Clarifications: idealingua-v1 modernization — open decisions

**Context:** M1 (the design package) shipped — three plan docs landed on `wip/mudyla` (rebased onto `origin/develop`). M2 (implementation) cannot start until you ratify or override the cross-cutting decisions surfaced during the review-loop. The questions below are taken from the meta-plan §"Cross-cutting decisions", PR-02 §9, and PR-03 §11. Recommendations are pre-filled where the plan docs took a position.

**How to answer:** Write your response on the `Answer:` line under each question. Leave a question blank if you want to skip it. Answer in any order. Reference questions by their ID (`Q3`, `Q7`, …) in chat if convenient. When you're done, I'll fold the answers into `tasks.md` cross-cutting notes and propose the next implementation PR.

The questions are grouped:
- **Bucket A** (Q1–Q5) — blocks M2 kickoff. Answer first.
- **Bucket B** (Q6–Q9) — shapes the rollout. Needed before the IMPL-N breakdown is finalized.
- **Bucket C** (Q10–Q12) — per-phase scope. Can wait until the relevant phase lands.

---

## Q1: Are Buzzers and Streams still used in production?

**Context:** Both are first-class in the typed AST today (`Buzzer`, `Streams`, `TypedStream`). The runtime supports them — `BuzzRequest`, `S2CStream`, `C2SStream` packet kinds in `packets.scala`. This is a pure prior-knowledge question: only you know whether downstream consumers actually invoke them. The answer changes whether the new typer treats them on par with services or whether they get a deprecation path.

**Suggestions:**
- **Both used** — first-class in new IR; full coverage in PR-03 fixtures. Most work.
- **One used, one not** — pick which; first-class for the one used, deprecate-but-keep-working for the other.
- **Neither used** — deprecate-but-keep-working in the new IR (no behavior change, no fixtures), schedule removal for a later release.

Answer: buzzers - yes, streams - no

---

## Q2: Delete `idealingua-v1-runtime-rpc-go` along with the Go transpiler?

**Context:** You said Go has no production consumers. The transpiler (`togolang/`) is uncontroversial to delete. The runtime module (`idealingua-v1-runtime-rpc-go/`) is a separate Scala module that anyone using a published Go-codegen jar would have transitively. If nobody uses Go, the runtime is also dead.

**Suggestions:**
- **Yes, delete the runtime too** *(recommended)* — clean break, single atomic deletion in IMPL-8.
- **Delete transpiler only, keep runtime as a no-op stub** — defensive if you're not sure. Costs a build.sbt entry and a stub jar publish per release.
- **Delete-after-deprecation** — mark deprecated for one release, delete the next. Slowest path; only useful if there's any uncertainty about consumers.

Answer: yes, delete

---

## Q3: TBLOB encoding policy — what to do given C# is currently `???`

**Context:** C# back-end at `JsonNetExtension.scala:188` is `case Primitive.TBLOB => ???` — i.e. a Scala placeholder that throws `NotImplementedError` if any test domain uses TBLOB. There is no current C# baseline for TBLOB, so PR-02 cannot ship a TBLOB change without first picking a policy. Scala and TS today both encode TBLOB as base64-string (verify before locking).

**Suggestions:**
- **Lock base64-string for all three languages** *(recommended if Scala/TS already do this — confirm during baseline)*. Implement C# in PR-02 as part of the typer rewrite.
- **Formally exclude TBLOB from the wire-compat contract** — document that TBLOB is unsupported until a future minor release; add a typer diagnostic that rejects models containing `TBLOB`.
- **Defer the decision** — leave the C# `???` in place and treat any model touching TBLOB as out-of-scope for the modernization PRs.

Answer: Lock base64-string for all three languages

---

## Q4: TUInt64 representation when value exceeds 2^53

**Context:** JS Number can represent integers up to 2^53 - 1 exactly; values above that silently truncate when parsed via `JSON.parse`. Today's Scala/C# back-ends emit `TUInt64` as a JSON number. TS clients consuming such values will lose precision. This is a wire-visible policy decision, not a verification — fixtures need to know which encoding to assert.

**Suggestions:**
- **Keep JSON number** — preserves byte-equality with current behavior; JS clients accept silent truncation. **Lowest churn, highest risk.**
- **Switch to JSON string for `TUInt64` only** — lossless but breaks any current client that reads the value as `number`. **Wire-format break; requires a coordinated client update.**
- **Hybrid: number for values ≤ 2^53, string above** — preserves current bytes for "normal" ranges, switches at the boundary. Asymmetric and harder to specify; not recommended.
- **Leave the decision per-application via a typer flag** — adds CLI surface; complicates the harness.

Answer: hybrid

---

## Q5: TSet iteration order on the wire

**Context:** Today's Scala runtime uses `LinkedHashSet` for generated `TSet[T]` fields, so insertion order survives a round-trip if the encoder iterates the field as-is. TS and C# may not match. Fixtures cannot be byte-strict until the canonical order is committed.

**Suggestions:**
- **Insertion order, mirrored across all three languages** *(recommended if Scala already does this — confirm during baseline)*. PR-02 must ensure TS/C# emitters iterate in a way that matches Scala's `LinkedHashSet`.
- **Sorted order (stable, deterministic, language-independent)** — wire break for current Scala consumers that depended on insertion order; cleaner spec.
- **Indeterminate** — back-ends free to emit in any order; relax the wire contract for `TSet` payloads to canonicalized comparison only. Defeats Q1's "byte-strict raw default" for any payload containing a `TSet`.

Answer: Insertion order, mirrored across all three languages

---

## Q6: Build alongside vs. tear-down-and-rebuild for the new typer

**Context:** §2 of PR-02 outlines two strategies. Alongside means a new `izumi.idealingua.typer.phase.*` package coexists with `IDLPostTyper`, gated by a feature flag in `TypespaceCompilerBaseFacade`, until PR-03 harness proves byte-parity. Tear-down rewrites in place; faster but no safety net during the transition.

**Suggestions:**
- **Alongside, behind feature flag** *(recommended)* — slower, but PR-03 harness is the only thing that proves byte parity, and you cannot run the harness against both new and old at the same time without alongside.
- **Tear-down-and-rebuild** — only attractive if you trust the test corpus to catch regressions, which we know it doesn't (no goldens exist today).

Answer: Alongside, behind feature flag

---

## Q7: Where does the test harness live?

**Context:** The compiler module's existing `idealingua-v1-compiler/src/test/scala/izumi/` is empty. Putting the harness there keeps everything in one module but pulls TS/dotnet test-time dependencies into the compiler module. A new `idealingua-v1-test-harness` module keeps deps clean but adds a `build.sbt` entry.

**Suggestions:**
- **Option B — new `idealingua-v1-test-harness` module** *(recommended in PR-03 §9)*. Separates test-time deps from the compiler proper; lets the harness publish independently if needed.
- **Option A — compiler test tree** — simpler, single-module. TS/dotnet tooling becomes a transitive test-scope concern of the compiler.

Answer: option B - create new test harness

---

## Q8: Drop Scala 2.13 in the compiler module while keeping it in the runtime?

**Context:** Current build cross-compiles Scala 2 + Scala 3 (visible in `idealingua-v1-runtime-rpc-scala/src/main/scala-2/` and `scala-3/`). Runtime modules likely need 2.13 for downstream consumers. The compiler module itself ships a CLI binary; its Scala version is a build concern, not a wire concern.

**Suggestions:**
- **Drop 2.13 in the compiler, keep 2.13 in the runtime** — simplest path; uses Scala 3 features (e.g. `IArray`, opaque types) freely in the new typer/IR.
- **Keep cross-build everywhere** — costs developer ergonomics in the typer code (no Scala 3-only features) for the marginal benefit of a 2.13 compiler binary.

Answer: Keep cross-build everywhere

---

## Q9: CLI flag stability for `CommandlineIDLCompiler` — deprecation cycle for renames/removals?

**Context:** `CommandlineIDLCompiler` is the published binary downstream automations call. Removing flags (e.g. Go-specific `--go-package-prefix`) breaks any pipeline that still passes them. A deprecation cycle costs one release of overlap.

**Suggestions:**
- **Yes, deprecation cycle for any flag rename/removal** *(recommended)* — one release of "warn but accept", then remove. Cheap insurance.
- **Hard remove on the M2 release** — fastest, but requires you to be confident that no automation passes legacy flags.

Answer: hard remove

---

## Q10: Are constants (`RawVal` / `ConstValue`) in scope for the typer rewrite?

**Context:** Current code has TODOs at `IDLPostTyper.translateValue:240, 245, 250` — known correctness gaps in const-block typing. The new multi-phase typer makes it cheap to add a `ConstValueTyper` phase. If consts are out of scope, the new typer just preserves the existing partial behavior.

**Suggestions:**
- **In scope** *(recommended)* — closes the known TODOs; const-block correctness gets first-class diagnostics.
- **Out of scope (preserve existing partial behavior)** — saves one phase; consts remain partially-typed forever.
- **In scope but only for a subset (e.g. scalar consts)** — narrower scope; ADT/record const values stay TODO.

Answer: In scope

---

## Q11: Newtypes / `ForeignType` — make them work, or delete the syntax?

**Context:** `IDLPostTyper.fixType:192` throws `s"TODO: foreign type isn't supported yet"`. Newtypes are partially supported (DTO/Interface only, lines 175–187). The grammar accepts both. Either implement them properly in the new typer, or remove from the grammar and reject existing inputs that use them.

**Suggestions:**
- **Make them work** — finish the implementation in the new typer. Both newtypes and foreign types become first-class.
- **Delete `ForeignType` from the grammar; finish newtype support for all type kinds** — drops the most-broken feature, fixes the partially-broken one.
- **Delete both from the grammar** — simplest. Any existing model file that uses them breaks; check `idealingua-v1-test-defs/` for usage first.
- **Leave as-is (status quo)** — typer continues to throw on `ForeignType`; newtypes remain DTO/Interface-only.

Answer: drop foreigns, make newtypes work

---

## Q12: Lock `wireId = "<pkg>.<name>"` formula permanently?

**Context:** The ADT/interface JSON discriminant in the wire format is `wireId = path.toPackage.mkString(".") + "." + name` (`TypeId.scala:31`). Changing this formula or moving any existing type into a different package would silently break clients. PR-02 §6 implicitly assumes this is locked but does not state it as a project-wide rule.

**Suggestions:**
- **Yes, lock permanently** *(recommended)* — codify in `docs/wire-format.md` (the spec doc PR-03 §4 sketches) as a forbidding rule: no type may change its package or its name without a coordinated wire-format break across all consumers.
- **No, allow renames with a migration tool** — far more work; requires the harness to support rename mappings; not currently designed for.

Answer: yes, lock

---

## Already locked (no action needed; listed for completeness)

These were resolved during the review-loop and are baked into the plan docs. Override here if you disagree.

- **L1** — New typer emits diagnostics, never throws on user errors (was C8). Override?
- **L2** — Cross-domain cycle check runs before dependency-graph construction, not after (was C11). Override?
- **L3** — IR preserves struct field declaration order (was C12; PR-02 §4 field-ordering invariant). Override?
- **L4** — Layer B byte-strict raw comparison is the default for ALL payloads; canonicalization is a parallel sanity check (was Q1 in PR-03). Override?
- **L5** — Negative-test diagnostic-kind assertions deferred to PR-02 (was Q3 in PR-03); PR-03 only asserts "legacy throws something". Override?
- **L6** — Freeze tag committed as `wire-format-baseline-2026-05-03` (was Q5 in PR-03). Override?

Answer (only if overriding):
