# idealingua-v1 modernization — Defect Ledger

Adversarial-review findings discovered during the review-loop session. Audit trail for each plan-doc PR.

Status: `[ ]` open · `[~]` under fix · `[x]` resolved

---

## PR-01

## [PR-01-D01] Lesson 9 incorrectly couples BasicNamingConventionsRule and ReservedKeywordRule as twin universal verifier rules
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:654-660
**Description:** `BasicNamingConventionsRule` is hard-wired into `TypespaceVerifier.basicRules` (`TypespaceVerifier.scala:9-17`), so it runs for every domain. `ReservedKeywordRule` is *not* — it is constructed per-translator and passed through the optional `rules: Seq[VerificationRule]` argument from each translator descriptor. Treating them as symmetric "post-typer verifier" rules leads PR-02 to a wrong conclusion about whether reserved-keyword checking is a domain-level invariant.
**Fix:** Lesson 9 rewritten to separate the two rules. `BasicNamingConventionsRule` cited as universal verifier rule at `TypespaceVerifier.scala:12`; `ReservedKeywordRule` cited as per-translator with verified line numbers in `ScalaTranslatorDescriptor.scala:24`, `CSharpTranslatorDescriptor.scala:24`, `TypescriptTranslatorDescriptor.scala:24`. Prescription updated so PR-02 keeps reserved-keyword check translator-local.

## [PR-01-D02] Lesson 5 cites IllegalArgumentException sites in a list of IDLException throws
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:608
**Description:** The lesson listed `IDLTyper.scala:58, 116, 157, 188, 192, 195, 269, 322, 400, 547, 552` as `IDLException` throw sites. Lines 116 and 195 are `throw new IllegalArgumentException(...)` (invariant-violation BUG asserts), not `IDLException`. Line 269 is off by one (the throw is at 268). The lesson's own prescription wisely preserved `IllegalArgumentException` as "BUG" assertions, so a fix subagent following only the cited list would have incorrectly attempted to convert two non-`IDLException` sites into diagnostics.
**Fix:** Lesson 5 throw list corrected: removed lines 116 and 195, fixed 269 → 268. Added a parenthetical noting that the two `IllegalArgumentException` sites at 115 and 195 are intentionally preserved as invariant assertions per the prescription.

## [PR-01-D03] Phase 14 references "M24 PR-J context" without defining it inside the document
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:308
**Description:** "no `renames` per the M24 PR-J context that doesn't apply" was a forward-reference to internal shorthand never introduced in this document. PR-01 is supposed to be a self-contained architectural reference.
**Fix:** Replaced with concrete in-line justification: "no version DAG, no `was[T]` annotations to track; cf. non-goal §6 'Version-axis renaming'."

## [PR-01-D04] IR-level table cites a non-existent "NameResolver" / "Resolution" component
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:537
**Description:** The "Scoped AST" row's "Consumed by" cell and the "Resolved AST" row's "Produced by" cell named `NameResolver` / `Resolution` — neither exists in Baboon. The actual consumer is `BaboonTranslator` factory invoked from `runTyper`'s fold (`BaboonTyper.scala:418-430`) plus `ScopeSupport`.
**Fix:** Both cells replaced with concrete Baboon citations: "BaboonTranslator (`BaboonTyper.scala:418-430`) + ScopeSupport (`ScopeSupport.scala:55-180`)".

## [PR-01-D05] BaboonComparator drop subsection lacks a citation in the actual Comparator source
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:513-524
**Description:** The "explicitly DROP" subsection cited only `BaboonModule.scala:59` (the DI binding). PR-02 needs the boundary grounded in the actual Comparator API surface.
**Fix:** Added entry-point trait method citation `BaboonComparator.scala:15` (`evolve(pkg, versions): F[NEList[BaboonIssue], BaboonEvolution]`); verified via web fetch from main branch.

## [PR-01-D06] Phase 1 verdict line contradicts its own rationale paragraph about where DomainId materializes
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:83-85
**Description:** Phase 1 verdict text said the package id is "fixed only in `IDLPostTyper.fixPkg`", but the rationale paragraph correctly noted that `defn.id: DomainId` is already available on `DomainMeshLoaded` after `IDLPretyper.perform`. The two disagreed.
**Fix:** Verdict text reworded to reconcile with rationale: DomainId is "available" on DomainMeshLoaded after `IDLPretyper.perform` (`IDLTyper.scala:60-74`) but "re-derived" inside `IDLPostTyper.fixPkg` (`IDLTyper.scala:577`) and `fixServiceId` (`IDLTyper.scala:469`).

## [PR-01-D07] Phase 11 "typeMeta assembly" is not really a phase — naming it inflates the count
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:82
**Description:** §1 claimed "Baboon: ~14 named phases" but Phase 11 (`typeMeta assembly`) is a 4-line `Map` construction. Inflates the count and pushes PR-02 toward 14 phase classes.
**Fix:** §1 mapping table claim downgraded to "~13 named phases plus trivial gluing steps" so §1 and §2 stay synchronized.

## [PR-01-D08] §1 mapping table omits BaboonFamilyManager despite §3 calling it the most load-bearing component
**Status:** resolved (variant — row already existed; verdict text added)
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:84-85
**Description:** §1 mapping table appeared to lack a row for `BaboonFamilyManager`. §3 calls it "*the* component with the most leverage" for the perf complaint.
**Fix:** Fix subagent reports a row already existed; rewrote the existing row to include the verdict line "KEEP shape, drop lineage/reload — see §3 BaboonFamilyManager and Lesson 3" rather than adding a duplicate. Re-reviewer: please confirm the row is now present with that verdict, and that the original reviewer's claim of "no row" was a miscount, not a missing row.

## [PR-01-D09] Off-by-one citation: `IDLTyper.scala:85` consistently used where `:84` is the def signature
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md (multiple)
**Description:** `def getDomain` is at line 84; document repeatedly cited `:85`/`:85-90`.
**Fix:** All `IDLTyper.scala:85` references updated to `:84-89` (occurrences in §3 BaboonFamilyManager rationale, Lesson 3, and §1 table).

## [PR-01-D10] ScopeBuilder citation `:115-142` for service-input synthesis is loose
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:334,341
**Description:** Range conflated the synthesizer block with the surrounding case.
**Fix:** Citation tightened: synthesis at `ScopeBuilder.scala:116-127` (the `// BAB-G01` block), broader `case service: RawService =>` at `:115-141`.

## [PR-01-D11] Note about develop branch wording implies executor had to discover branch was main
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:7-9
**Description:** Wording read as if executor discovered the canonical branch through trial and error.
**Fix:** Reworded to factual statement that Baboon's default branch is `main` and the meta-plan's incidental `develop` references are stale.

## [PR-01-D12] Two of four named non-goals lack a precise file:line for where the feature lives in Baboon
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:680-687,705-709
**Description:** "Multi-version evolution / `BaboonComparator`" cited only the DI wiring; "UEBA binary codecs" cited the wiring lines but not the actual generators.
**Fix:** BaboonComparator non-goal now cites `BaboonComparator.scala:15` (the `evolve` method); UEBA non-goal now cites three generator paths (`translator/csharp/CSUEBACodecGenerator.scala`, `translator/scl/ScUEBACodecGenerator.scala`, `translator/typescript/TsUEBACodecGenerator.scala`) plus `BaboonRuntimeCodec.scala`. Filenames verified via WebFetch on the main branch (note: TypeScript file is `TsUEBACodecGenerator.scala`, not the originally hinted `TSUEBACodecGenerator.scala`).

## [PR-01-D13] `any` builtin non-goal mentions Anyvals but does not cite where the case is defined
**Status:** resolved (variant — RawTypeDef.Anyvals does not exist; cited the actual implementation site instead)
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:720-722
**Description:** Non-goal mentioned `Anyvals` without a file:line.
**Fix:** Corrected the factually-wrong claim that "RawTopLevelDefn admits an Anyvals case" (verified by grep — no such case exists). Replaced with citation to `AnyvalExtension.scala:18` (the Scala translator extension that implements the AnyVal-detection optimization) and the test fixture path. Stated explicitly that no `RawTypeDef.Anyvals` case exists in the raw AST. Re-reviewer: please verify this re-framing is correct (Anyvals is a translator-side optimization, not a raw-AST type).

## [PR-01-D14] Lesson 10 lacks an idealingua-v1 file pointer in its prescription
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:670
**Description:** Lesson 10 named the to-be-introduced `EmissionPolicy` but referred only vaguely to "translator-extension hooks".
**Fix:** Added concrete file:line `ScalaTranslator.scala:54` (`ctx.typespace.domain.types.flatMap(translateDef)`) as the per-type emit loop.

## [PR-01-D15] Phase 4 citation `:47-51` is correct but worth verifying contiguity with Phase 5
**Status:** resolved (verified, no fix needed)
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:148
**Description:** Reviewer flagged for verification only.
**Fix:** Verified — Phase 4 cites `BaboonTyper.scala:47-51`; Phase 5 begins at `:52`. Contiguous, no gap. No edit required.

## [PR-01-D16] §2 prelude does not warn that Phase 3 (runTyper) source lines are not in monotonic order with Phases 4–14
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:91-94
**Description:** A reader expecting monotonic line numbers would be disoriented when Phase 3 (lines 389-443) precedes Phases 4-14 (lines 47-90).
**Fix:** Added the warning sentence to §2 prelude: "Phase 3 (`runTyper`) is a private method called from `process` at line 45; its body lives at lines 389-443. The remaining phases are inlined into `process` itself at lines 47-90."

## [PR-01-D17] Phase 5 Output IR description elides that "the root set" is a union of two Maps
**Status:** resolved (chose rephrase option)
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR01-baboon-typer-lessons.md:159-161
**Description:** Phase 5's output described as "Map[TypeId, DomainMember.User] (the root set)" but `BaboonTyper.scala:60` is `roots = directRoots ++ aliasRoots`.
**Fix:** Output IR clarified to "Map[TypeId, DomainMember.User] (the union `directRoots ++ aliasRoots`, `BaboonTyper.scala:60`)". Fix subagent chose the rephrase option as the union is structurally significant.
