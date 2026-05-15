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

---

## PR-02

## [PR-02-D01] Go-codec deletion list omits four implicit defs in Codecs.scala (decGoProjectLayout :41, decGoRepositoryOptions :43, encGoProjectLayout :83, encGoRepositoryOptions :85)
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:758, §10 row at 1260
**Description:** §5's Go atomic group enumerates only `decGo` (`:39`) and `encGo` (`:81`) for Codecs.scala, and §10 mirrors that. Codecs.scala lines 41, 43, 83, 85 also define implicit decoders/encoders for `GoProjectLayout` and `GoRepositoryOptions`, both of which live inside `GoLangBuildManifest.scala`. Deleting `GoLangBuildManifest.scala` without simultaneously dropping these four codec defs leaves dangling references; the Go atomic group does not actually compile under the cited line set. Doc claims §5 is "exhaustive".
**Fix:** Extended §5 Go-glue Codecs.scala row, §2 atomic-group bullet, and §10 modify row to include all six implicit defs (decGo, decGoProjectLayout, decGoRepositoryOptions, encGo, encGoProjectLayout, encGoRepositoryOptions) at lines 39, 41, 43, 81, 83, 85. Verified.

## [PR-02-D02] §5 and §10 omit CredentialsReader.scala entirely, despite GoCredentials/ProtobufCredentials and language-keyed dispatch
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md (no entry exists)
**Description:** `idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CredentialsReader.scala` defines `GoCredentials` (line 18), `ProtobufCredentials` (line 20), and the language-dispatched read at lines 27 and 29 (`case IDLLanguage.Go => read[GoCredentials]`, `case IDLLanguage.Protobuf => read[ProtobufCredentials]`). After Go and Protobuf are removed, these become dead code (Scala 3 missing-case checks would fail). The deletion plan never names this file.
**Fix:** Added rows for `CredentialsReader.scala` to §5 Go-glue (`:18`, `:27`), §5 Protobuf-glue (`:20-21`, `:29`), §10 modify list, and §2 atomic-group bullets. Lines verified.

## [PR-02-D03] §5 misses CommandlineIDLCompiler.scala:283 (IDLLanguage.Go literal in default version map)
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md (no entry exists)
**Description:** `idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CommandlineIDLCompiler.scala` contains a hard-coded `IDLLanguage.Go -> "0"` at line 283, inside a default-version map. Once `IDLLanguage.Go` is removed, this line stops compiling. Doc's atomic-group rule "anything left half-removed breaks the build" is therefore self-violated.
**Fix:** Added `CommandlineIDLCompiler.scala:283` (`IDLLanguage.Go -> "0"`) to §5 Go-glue, §10 modify list, and §2 Go atomic-group bullet. Verified.

## [PR-02-D04] §10 omits the entire `typespace/structures/` subtree (5 files) although unreachable after IMPL-10
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:1265-1289
**Description:** `idealingua-v1-model/.../typespace/structures/` contains `AbstractStruct.scala`, `ConstAbstractStruct.scala`, `ConverterDef.scala`, `FieldConflicts.scala`, `Struct.scala`. They are consumed only by the legacy query layer (`StructuralQueriesImpl`, `Typespace`, etc.) which §10 lists for IMPL-10 deletion. After those deletions, `structures/` is dead code, and §4's `FlatStruct` replaces all of it.
**Fix:** Added five `delete` rows to §10 for `typespace/structures/{AbstractStruct,ConstAbstractStruct,ConverterDef,FieldConflicts,Struct}.scala` under IMPL-10. Directory contents verified via `ls`.

## [PR-02-D05] Phase 4 covers Identifier/Mixin/ADT but omits Interface, DTO, Enum kind invariants the brief required
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:283-303
**Description:** Brief explicitly demands Phase 3 cover "ADT/Interface/DTO/Identifier/Enum checks". Phase 4 names only Identifier-field kinds, mixin-target kinds, and ADT-branch nesting. It does not state "Interfaces and DTOs have no additional kind-shape checks" or "Enums require non-empty members / no duplicate values". `AdtMembersRule`, `BasicNamingConventionsRule`, and `DuplicateMemberRule` collectively cover these today; the doc's Phase 4 / Phase 12 split does not say which phase owns which case.
**Fix:** Extended §3 Phase 4 with explicit Interface/DTO/Enum subsections stating "no additional kind invariants" and naming where the corresponding checks live (NameResolver, SymbolNames, Phase 12 Validator).

## [PR-02-D06] §2 references `Domain.lookup` but §4's IR has no `lookup` method — type contract inconsistent
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:130-131
**Description:** §2 says: "the loader emits a `Domain` value via the new typer, and `descriptor.make` is supplied with a thin `Typespace` adapter that translates `Domain.lookup` calls into the legacy query API". §4's `Domain` case class exposes `members: Map[TypeId, Member]` and named index maps; there is no `lookup` method anywhere. Implementer cannot tell which is correct.
**Fix:** Replaced §2's `Domain.lookup` reference with `domain.members.get(id)` matching §4's actual IR shape.

## [PR-02-D07] ArtifactPublisher.scala publishGo body cited as :169-269 but actually ends at :280; publishProtobuf cited as :282-360 but actually ends at :374
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:760, §10 row at 1262
**Description:** `publishGo` runs from line 169 to line 280 (`}.toEither` at 280); `publishProtobuf` runs from line 282 to line 374. The doc cites `:169-269` and `:282-360`. Both off by ~11–14 lines, which would cause a mechanical line-range deletion to leave half a method body behind.
**Fix:** Replaced `:169-269` with `:169-280` and `:282-360` with `:282-374` everywhere via `replace_all`. Method-end lines verified in `ArtifactPublisher.scala`.

## [PR-02-D08] ManifestReader.scala glue row cites :22 (Writer arm) and :38 (example body), elides :37 (Reader case label) and Writer-arm distinction
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:759, §10 row 1261
**Description:** ManifestReader.scala line 22 (`case m: GoLangBuildManifest`) is in `ManifestWriter.write`, not in `read`. The Reader's `case IDLLanguage.Go =>` is at line 37 (label) followed by `readManifest(GoLangBuildManifest.example)` at line 38. The doc cites `:22` and `:38` and elides `:37`. Mechanical deletion at the cited offsets would leave an orphan `case IDLLanguage.Go =>`.
**Fix:** Updated ManifestReader.scala citations to `:22-23` (Writer arm) and `:37-38` (Reader arm) in §5 Go-glue row, §10 modify row, and §2 atomic-group bullet. Lines verified.

## [PR-02-D09] "7 rule files" wording vs 8 actual rule files; ReservedKeywordRule mislabelled `delete` then "NOT deleted" in §10
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:466-470, §10 rows 1278-1287
**Description:** §3 Phase 12 says "the same 7 diagnostic categories the legacy verifier emits". §10 lists 7 rules as `delete` and 1 (`ReservedKeywordRule.scala`) as a "kept" entry mislabelled `delete` with a "NOT deleted" note. Directory contains 8 files; legacy verifier auto-applies 7. A mechanical reader of the §10 row would still delete `ReservedKeywordRule.scala` despite the post-hoc note.
**Fix:** Changed §10 ReservedKeywordRule.scala row Action from `delete` to `keep` with full citation of per-translator descriptors. Reworded Phase 12 Diagnostics section to enumerate the actual 7 auto-applied rules and explicitly call out `ReservedKeywordRule` as the eighth, opt-in file.

## [PR-02-D10] Cited `q"trait $name { ... }"` quasiquote location at ScalaTranslator.scala:71-93 is wrong file/lines
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:872-873, §10 row 1254
**Description:** §6 Scala says: "the actual Scala-meta quasiquote emission (`q"trait $name { ... }"` at `:71-93`) is unchanged". `ScalaTranslator.scala` lines 71-93 contain `translateService` and `translateDef` method bodies, no `q"trait …"`. The trait-shaped quasiquote actually lives in renderers under `…/toscala/types/`. Reader cannot verify the "unchanged" claim against the cited file.
**Fix:** Replaced the wrong `ScalaTranslator.scala:71-93` quasiquote reference with a correct cite to `InterfaceRenderer.scala:46-50` (`q"""trait ${t.typeName} extends ..$ifDecls { ..$decls }"""` inside `mkTrait`). Verified.

## [PR-02-D11] §6 calls `ctx.typespace(a)` "alias dealiasing" but the call returns a TypeDef, not the dealiased type; proposed substitution type-incorrect
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:1255
**Description:** `ctx.typespace(a)` at line 214 is `Typespace.apply(id)` returning `TypeDef`; it is type-lookup, not the `dealias` operation (`TypespaceImpl.dealias` at lines 41-49). The doc replaces `ctx.typespace(a)` with `ctx.domain.aliases.get(a)`, but `aliases.get(a)` returns the alias *target* `TypeId`, not `TypeDef`. The pattern-match against `TypeDef.Alias(_, target, _)` at line 215 will not work against a `TypeId`.
**Fix:** Updated §10 CirceTranslatorExtensionBase.scala row to specify the substitution as `ctx.domain.members(a)` (returns `TypeDef`) so the existing `case TypeDef.Alias(_, target, _)` match continues to work. Reworded the §6 mapping to clarify that the `:214` site is type-lookup, not dealiasing.

## [PR-02-D12] IDLTestTools.scala has zero Go/Protobuf references but doc lists it for cleanup with no enumeration
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:763, §10 row 1263
**Description:** `idealingua-v1-transpilers/.jvm/src/test/scala/izumi/idealingua/IDLTestTools.scala` is 60 lines and contains no Go, golang, protobuf, or togolang token (case-insensitive). Doc still lists it as a modify target despite there being nothing to drop. Fabricated cleanup work.
**Fix:** Removed the IDLTestTools.scala row from §5 Go-glue table and §10 modify list, replacing with an inline HTML comment explaining the file was checked and contains no Go/Protobuf tokens.

## [PR-02-D13] §9 missing recommendation surface for "C12"; meta-plan numbers C1-C11; brief asked for C1-C12
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:1212-1213
**Description:** Brief requires §9 recommendations for "C1–C12". Meta-plan numbers 1 through 11 (verified at `20260503-1200-modernization-plan.md:154-167`). Doc honestly notes the gap but only as a one-line parenthetical at the end of §9. The tasks.md ledger does have a C12 (field-ordering invariant) added by the orchestrator. A reviewer expects an explicit "C12 covered separately" rather than a buried parenthetical.
**Fix:** Hoisted a new `C12 — Field-ordering invariant` bullet to the top of §9, citing tasks.md, §4 IR's 'Field-ordering invariant' subsection, and §11's R1. Removed the trailing one-line parenthetical that previously claimed C12 was a placeholder.

## [PR-02-D14] §4 Fingerprint(value: Array[Byte]) silently breaks value equality (Scala Array uses reference equality)
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:411-412 vs §4
**Description:** Phase 9 outputs `Map[TypeId, Fingerprint]` plus a per-domain rollup. §4's `Fingerprint` is `Array[Byte]`. `Array[Byte]` in Scala does not have value-equality — `==` is reference equality. The "frozen-after-assembly" invariant will silently fail equality checks in the harness if the fingerprint is compared with `==`.
**Fix:** Changed §4 `Fingerprint(value: Array[Byte])` to `Fingerprint(value: IArray[Byte])`. Added a paragraph to the §4 frozen-after-assembly invariant explaining the Scala `Array` reference-equality footgun and the IArray fix (with ByteVector as Scala-2 fallback).

## [PR-02-D15] Phase numbering inconsistent: §1/§2 reference "Phase 1 (cross-domain index)" and "Phases 6-10" but §3 numbers cross-domain index Phase 0; off-by-one across IMPL-N references
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:200-221
**Description:** §2 IMPL-4 says "IdealinguaFamilyManager (cross-domain index, see §3 Phase 1)" — but §3 numbers it Phase 0 (line 200). Likewise IMPL-3 says "Phases 6-10" but the actual structural-fact phases per §3 are Phases 5-11. An implementer cannot trace which phase block they own without re-reading §3 and re-mapping.
**Fix:** Picked the lower-churn option: kept Phase 0 = cross-domain index. Updated §2 IMPL-4 from 'Phase 1' to 'Phase 0'; §3 prelude from '(Phase 1)' to '(Phase 0)'; corrected IMPL-2 phase range from 'Phases 2-4' to 'Phases 1-4'; corrected IMPL-3 from 'Phases 6-10' to 'Phases 5-11'.

## [PR-02-D16] §10 closing parenthetical contains hand-waving about IDLTyper.scala that contradicts the row above it
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:1290-1294
**Description:** §10's last row says "delete | `…IDLTyper.scala` (legacy classes only)". Closing parenthetical says "the `IDLTyper.scala` row may end up as a modify followed by a delete in the next release — depends on whether anything external imports `IDLPretyper`/`IDLPostTyper`". The Action column already locks `delete`; the parenthetical re-opens it.
**Fix:** Collapsed the §10 closing parenthetical to a `delete` decision; added a new "A1" assumption block to §11 capturing the "no external consumers" rationale and the fallback to modify-then-delete-next-release if the assumption fails. Updated the §10 IDLTyper.scala row Notes to reference §11.

---

## PR-03

## [PR-03-D01] Layer B "canonicalized" payload comparison softens the byte-for-byte invariant the user demanded
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:286-313
**Description:** The user's explicit requirement is byte-for-byte stability of the JSON wire format. §3 Layer B applies "strict byte-equal" only to the envelope and "canonicalized" comparison (recursive key-sort, whitespace normalization, trailing-zero drop, integer/exponent normalization) to all inner payloads — DTOs, Identifiers, ADT branches, interfaces. That covers >99% of the actual wire surface. The doc itself admits at lines 309-313 that Layer B does NOT catch field-order regressions; the escape hatch ("Layer A's job") is unsound because Layer A is a *source-level* diff over generated code, brittle to harmless source changes the doc itself flags as Layer A's known weakness. A refactor that changes the emitted JSON byte stream (e.g. via a non-derived Encoder with the same field order but different float formatting) is therefore not caught by either layer.
**Suggested fix:** Add a third compare mode "wire-byte strict (raw)" for Layer B that compares the un-canonicalized output byte-for-byte for all payloads, and reserves canonicalization for a *parallel* sanity check (does today's Scala output canonicalize to today's TS output?). Make strict-byte raw the default; canonicalization opt-in for known-asymmetric scenarios.

## [PR-03-D02] TBLOB JSON encoding asserted as "base64" without verification, but C# back-end is `???` (NotImplementedError)
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:494,142-151
**Description:** §4 topic 9 states "TBLOB → JSON string, base64-encoded (verify each back-end)". §2 item 10 lists `TBLOB` among builtins whose JSON mapping is "implicit in each back-end's serializer choice". The actual C# emitter at `idealingua-v1-transpilers/.../tocsharp/extensions/JsonNetExtension.scala:188` reads `case Primitive.TBLOB => ???` — a Scala `???` placeholder that throws NotImplementedError when the transpiler emits a TBLOB type, not at runtime. There is no agreed C# encoding of TBLOB, so the harness cannot assert parity for TBLOB across languages. The executor's handoff flagged this and the document did not resolve it; §11 open questions does not list it.
**Suggested fix:** Promote TBLOB encoding to an explicit §11 open question. State that C# does not currently emit TBLOB at all and that PR-02 cannot ship a TBLOB change because there is no baseline. The §6 audit row for TBLOB-in-DTO must be added as a non-negotiable coverage gap.

## [PR-03-D03] §4 spec topic 3 cites TS authoring file as "search for serialize() / deserialize" instead of file:line
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:434-435
**Description:** Brief required every spec-skeleton topic to cite the file:line authoring the rule today. Topic 3's TS citation reads `TypeScriptTranslator.scala (search for serialize() / deserialize)`. That is not a citation. The TS struct serializer is real and locatable (the file does contain `serialize()`/`deserialize` definitions), but the doc punts. Executor's handoff explicitly flagged this as an open item ("TS Identifier serialize/deserialize body") and the document carried the placeholder forward.
**Suggested fix:** Read the TS struct serialize/deserialize emit site and cite the precise line range in `TypeScriptTranslator.scala` (or whichever extension file emits the per-DTO `serialize()`/`deserialize` template). Same treatment for Identifier in topic 6.

## [PR-03-D04] §5 "Exact SBT commands" hedged as "subject to revision … listed for orientation, not as a contract"
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:550-559
**Description:** Brief required §5 to specify exact sbt commands. The doc lists candidate task names (`regenerateGoldens`, `testOnly izumi.idealingua.harness.GoldenSpec`, etc.) but immediately disclaims them as "not a contract". §5 is the most load-bearing section: the baseline workflow needs to be reproducible months from now. The §5 step 3 freeze-tag instruction also names the tag `wire-format-baseline-2026-05-XX` with `XX` literal, leaving the date unresolved.
**Suggested fix:** Either commit to the task names as contracts (and update later via PR if needed) or specify them as requirements on PR-03.1 ("PR-03.1 must add an SBT task named exactly `regenerateGoldens` …"). Resolve the tag-date placeholder by referencing a concrete date in §11 Q5.

## [PR-03-D05] Layer B for TS depends on "npm install in a temp dir" without specifying how generated TS becomes a runnable module
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:282-285,1020-1031
**Description:** Generated TypeScript from idealingua-v1 is not published to npm. The harness needs to either (a) use a workspace-style local path resolution, (b) compile generated TS as a sibling `tsc` step inside the harness, or (c) run TS through ts-node/tsx without compilation. §3 Layer B and PR-03.4 hand-wave this with "depending on a npm install in a temp dir whose node_modules includes the generated TS plus its runtime dependencies." Equivalent question for C#: how does generated C# become a `dotnet test`-runnable assembly? A solution exists, but the doc does not pick one, and that decision is a real blocker for PR-03.4 implementation.
**Suggested fix:** Add a sub-section under §3 Layer B describing the per-language build pipeline: TS via `tsc --noEmit` against generated sources plus a hand-written test driver; C# via `dotnet build` of a small csproj that references the generated `.cs` files. Drop the "npm install" framing — the generated TS isn't a published package.

## [PR-03-D06] TSet iteration order, TUInt64-near-2^63, TBLOB flagged in §4 but absent from §11 open questions
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:506-508,898-961
**Description:** Executor's handoff flagged: TUInt64 near 2^63 (JSON number vs string, lossy in JS), TSet stable iteration. §4 topic 9 has a parenthetical for TUInt64 and §4 topic 10 has a parenthetical for TSet ("today's Scala runtime iterates Set in insertion order for LinkedHashSet-typed generated fields; verify and document"). Neither is escalated to §11. The doc treats them as "verify during fixture authoring" — but they are decisions, not verifications. JS truncation of numbers >2^53 is not a verifiable fact about current code; it is a downstream consequence requiring a policy choice.
**Suggested fix:** Add §11 Q10 (TUInt64-near-2^63 representation policy), §11 Q11 (TSet iteration order canonicalization), and §11 Q12 (TBLOB encoding in C# given the `???` stub).

## [PR-03-D07] TS enum encoding citation `:447` points at the `export enum` declaration line, not the member format-string line
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:452
**Description:** §4 topic 5 cites `TypeScriptTranslator.scala:447, '$m = $m'`. The actual format-string `s"$m = '$m'"` is at line 443; line 447 is `s"""export enum ${i.id.name} {`. Off by 4, within nit tolerance.
**Suggested fix:** Update the citation to `TypeScriptTranslator.scala:443`.

## [PR-03-D08] §6 and §11 Q9 reference `izumi/test/` and `overlays/` directories that do not exist with those names
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:582-588,956-961
**Description:** Actual layout is `idealingua-v1-test-defs/src/main/resources/defs/main-tests/source/izumi/` (no `test/` subdirectory) and `.../source/overlaytest/` (not `overlays/`). Cosmetic but a future contributor following §11 Q9 literally will not find the directories.
**Suggested fix:** Replace `izumi/test/` with `izumi/` and `overlays/` with `overlaytest/`, throughout.

## [PR-03-D09] Layer C "encode <typeId>" CLI driver protocol does not specify how typeIds are passed for languages that erase types at runtime
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:344-409
**Description:** §3 Layer C says each driver exposes `encode <typeId> <jsonInput>` and `decode <typeId> <jsonInput>`. In TypeScript, runtime type information is erased; there is no `lookup_class_by_wireId` registry generated by the current TS back-end. Same for C# unless the back-end emits a static dispatch table (it does not — `serializer.Deserialize<T>(...)` requires a compile-time `T`). The harness therefore needs *generated* per-typeId entry points, but §3 Layer C does not say so.
**Suggested fix:** Add a clause: "The drivers contain a generated dispatch table (one entry per type in the test corpus) that maps wireId → typed encode/decode call. The dispatch table is produced as part of the harness build and lives next to the driver source." Note this in PR-03.4 scope.

## [PR-03-D10] §7 negative-test diagnostic-kind assertion contradicts the rest of the doc about legacy compiler diagnostics
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:651-657
**Description:** §7 lists kinds (`DuplicateMember`, `AdtMembers`, etc.) "from `TypespaceVerifier.scala`" without a path or line range. Legacy compiler reports errors via `IDLException`-thrown strings. Bridging "thrown string" → "structured `kind`" is not free; it requires either parsing the message string (brittle) or shipping a parallel diagnostics type alongside PR-02. §7's recommendation lands the harness with a dependency on PR-02-typer-diagnostics, but §12 PR-03.6 implies the harness can extract kinds from the *legacy* compiler. These cannot both be true.
**Suggested fix:** Pick one. Either §7 explicitly defers all kind-based assertions to PR-02 (and PR-03.6 just asserts "legacy throws something, anything"), or §7 defines a small kind-extractor that pattern-matches the throw site of `IDLException` against a fixed set of substrings — and admits that this is a temporary bridge.

## [PR-03-D11] Layer A "byte-for-byte" conflates source-byte equality with wire-byte equality
**Status:** resolved
**Severity:** nit
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:222
**Description:** §3 Layer A's pre-modernization baseline step says "the new typer must reproduce the legacy goldens byte-for-byte unless the diff has been classified per the policy above". The goldens are *generated source code* (Scala `.scala`, TS `.ts`, C# `.cs`), not wire-format JSON. Source-byte equality is a stronger and orthogonal commitment to wire-byte equality. Layer B later acknowledges this distinction but §3-A wording uses the same "byte-for-byte" phrase reserved for the wire-format invariant.
**Suggested fix:** Replace "byte-for-byte" in §3-A with "character-for-character source equality" or "exact source-text equality" to keep "byte-for-byte" reserved for the wire-format invariant.


## [PR-03-D12] §11 Q3 still recommends diagnostic-kind assertions, contradicting §7 and PR-03.6 (D10 fix incomplete)
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:1038-1043
**Description:** D10's fix updated §7 (lines 755-785) and §12 PR-03.6 (lines 1217-1239) to defer kind-of-diagnostic assertions to PR-02 and have PR-03 assert only that the legacy compiler "throws something". §11 Q3, however, still reads "§7 recommends yes, with diagnostic-kind assertions (not message-string assertions). Confirm?" A reader resolving Q3 by confirming "yes, with diagnostic-kind assertions" would re-introduce the dependency on PR-02 structured diagnostics that D10 explicitly removed.
**Suggested fix:** Reword Q3 to match §7: "§7 recommends yes; PR-03 ships fixtures plus a 'legacy throws something' assertion, with kind-of-diagnostic assertions deferred to PR-02 once structured diagnostics ship. Confirm?"

## [PR-03-D13] §5 step 4 freeze-tag wording undermines D04's "committed date" claim
**Status:** resolved
**Severity:** minor
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:615-623
**Description:** D04's fix asserted the freeze-tag date is now committed as 2026-05-03. Lines 651-652 do treat the four sbt-task names as a contract, but lines 619-622 explicitly hedge: "the actual freeze tag will be cut on the day the baseline lands and the date in the tag string updated then via a single search-and-replace through this document." This re-opens the date as a placeholder. Combined with §11 Q5's recommendation to tag on develop "at the next minor release commit" (lines 1062-1064), the "committed" date is not actually committed.
**Suggested fix:** Either delete lines 619-622's hedge so the date is genuinely committed, or rewrite §5 step 4 to say "the tag is cut on baseline-landing day; the date in the tag string is whatever that day is, with no advance commitment" so D04's status is honestly partial.


## [PR-03-D14] PR-03.2 step uses placeholder `wire-format-baseline-2026-MM-DD` contradicting committed §5 step 4 tag
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR03-backcompat-test-harness-plan.md:1160
**Description:** Round-3 review found a ghost reference of the search-and-replace hedge that D13 was supposed to remove: §12 PR-03.2 atomic-PR description still said "Tag the commit `wire-format-baseline-2026-MM-DD`." This contradicts §5 step 4's "the date in the tag string is fixed and does not move." commitment.
**Fix:** Replaced `wire-format-baseline-2026-MM-DD` on line 1160 with `wire-format-baseline-2026-05-03`. Verified via `command grep -n 'wire-format-baseline'` that all three remaining references (lines 615, 664, 1160) now read the same committed string.


## [PR-02-D17] D11 fix introduces a type mismatch — `ctx.domain.members(a)` returns `Member`, not `TypeDef`
**Status:** resolved
**Severity:** major
**Location:** /home/pavel/work/safe/idealingua-v1/docs/drafts/20260503-PR02-idealingua-modernization-plan.md:940-942 and :1308
**Description:** §4 (line 599) defines `Domain.members: Map[TypeId, Member]` where `Member` is a sealed trait with cases `User(defn: TypeDef) | Ephemeral(defn: EphemeralDto) | Builtin(prim: Primitive)`. Both §6 (line 941) and §10 (line 1308) prescribe replacing `ctx.typespace(a)` with `ctx.domain.members(a)` and assert that the existing `case TypeDef.Alias(_, target, _)` match "continues to compile". That is false: `members(a)` evaluates to `Member`, not `TypeDef`. The fix subagent's claim that `members(a)` "returns `TypeDef`, not `TypeId`" was wrong — it returns `Member`. As written, §6 and §10 contradict §4 and would mislead the IMPL-7a engineer.
**Suggested fix:** Pick option (b) from the reviewer: add a `Domain.userTypes: Map[TypeId, TypeDef]` projection to §4 and use `ctx.domain.userTypes(a)` in §6/§10. Cleanest given §4's emphasis on "field-read, not query". Update §6 narrative and §10 row Notes accordingly.
