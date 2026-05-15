# PR-02 IMPL-6 — Feature-flag plumbing for the new typer

Plan author: planning subagent (review-loop, 2026-05-11).
Source briefs: tasks.md PR-02 line 45 + F18; master plan §2 line 87-90, line 121-149, line 183-186; §4 line 740-754; cross-cutting decisions C1/C8/C12 + L1/L3/L8.

## §1 Goal & non-goals

**Goal.** Single atomic PR on `wip/necromancy` that adds a `--typer=new|legacy` feature flag plumbing through `IDLCArgs` → `UntypedCompilerOptions` → `TypespaceCompilerBaseFacade.compile`, plus a `Typespace`-shaped adapter wrapping the new `Domain` IR so the still-unported Scala/TS/C# translators can run on either code path. Default is `Legacy`; the only behavioural change at HEAD is that the new path becomes selectable.

**Post-IMPL-6 invariants.**
- Default-flag (`Legacy`) build is byte-identical to pre-PR HEAD on the four harness contracts (`verifyGoldens`, `runWireFixtures`, `runCrossLangInterop`, `idealingua-v1-test-harness/test`) at Scala 2.13.18 and 3.8.3.
- `--typer=new` selects the new pipeline; it compiles end-to-end; if user-error diagnostics arise it raises a single `IDLException` aggregating them (legacy-shaped throw contract — see C8 bridge in §2).
- A new smoke unit test exercises the new path against one fixture and asserts Scala translator output compiles. Byte-parity is *not* asserted at IMPL-6 (deferred to PR-03 harness parity gate before IMPL-9).

**Non-goals.**
- Per-translator port to read `Domain` directly (IMPL-7a/b/c).
- Removal of any legacy typer/`Typespace` query code (IMPL-10, IMPL-11).
- Flipping the default to `New` (IMPL-9).
- Structured-diagnostics carry-through to translators (deferred; TODO marker only).

## §2 Pre-locked decisions (planner-recommended)

**P1. `TyperImpl` location.** `idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TyperImpl.scala`, sibling of `IDLLanguage.scala`. Same package (`izumi.idealingua.translator`), same sealed-trait + `case object` shape as `IDLLanguage`. Two cases: `Legacy`, `NewTyper`. Rationale: `TypespaceCompilerBaseFacade`, the dispatch site, lives in this package; the enum should be alongside its consumer, not in `-model` (which must not depend on `-transpilers`).

**P2. `UntypedCompilerOptions.typerImpl` default = `TyperImpl.Legacy`.** Lifecycle step 1 (master plan §2 line 138-140). All existing call-sites — verified to be exactly two (`CommandlineIDLCompiler.scala:186`, `HarnessOptions.scala:55`) — keep working unchanged because the field is added at the end of the case-class parameter list with a default value. No named-arg breakage; no Scala 2/3 cross-build hazard for this shape.

**P3. Throw-on-error bridge (C8/L1 reconciliation).** The new typer cannot throw on user errors. But `TypespaceCompilerBaseFacade.compile` already accepts `Seq[LoadedDomain.Success]` — failures were filtered upstream by `ModelResolver.resolve`/`.successful` (see `CommandlineIDLCompiler.scala:152-162`). At IMPL-6, when the new-typer pipeline emits a non-empty `Diagnostics` from any of phases 1-12, the driver raises a single `IDLException("New typer diagnostics: ..." + diags.issues.niceList())`. This matches the legacy facade contract from the caller's point of view. A TODO comment in the driver flags this as a temporary bridge to be replaced in IMPL-9 with a path that emits diagnostics via the loader's `LoadedDomain.VerificationFailed` channel.

**P4. CLI flag is a global parameter, not per-language.** Single source of truth (C1 + L8). Lives on `IDLCArgs.P` (the global `ParserDef`), not on `LP`. CLI shape: `--typer=<new|legacy>` / short `-T=<...>`, defaulted at the `IDLCArgs` model level to `TyperImpl.Legacy`. Mirroring the existing `define`/`publish` pattern at `IDLCArgs.scala:50-51`.

**P5. Adapter package + name.** `izumi.idealingua.translator.compat.DomainAsTypespace` in the **transpilers** module (NOT in `-model`). Rationale: the adapter is a transpiler-side compatibility shim that exists only to bridge surviving translators to the new IR and is scheduled for deletion in IMPL-11. Keeping it under `translator/compat/` makes the eventual deletion grep-trivial.

**P6. Pipeline driver location.** `idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/compat/NewTyperPipeline.scala`. Same rationale as P5 — it's the bridge that re-runs the new typer phases on top of an already-loaded `LoadedDomain.Success`. The phases themselves stay in `-model/typer/phase/`.

## §3 Audit results

### A1 — `UntypedCompilerOptions` constructor call-sites

Exactly two on the production path:
- `idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CommandlineIDLCompiler.scala:186` — `UntypedCompilerOptions(lang, exts, lopt.target, manifest, lopt.withRuntime, zipOutput = lopt.zip)`. Positional + one named arg. Adding `typerImpl` as a defaulted final parameter requires NO change here.
- `idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/HarnessOptions.scala:55-63` — all-named-args (`language = ..., extensions = ..., ...`). Defaulted final param requires NO change here either. The harness will pick up `Legacy` automatically (per A3 below this is REQUIRED for the four contracts to stay green).

No usages elsewhere in `-transpilers/.jvm`, `-test-harness/.jvm`, or `-compiler`. Mechanical risk: zero.

### A2 — `ctx.typespace.*` surface area used by surviving translators

Catalogue (collated from `toscala/`, `totypescript/`, `tocsharp/`):

**`typespace.domain.*`** (5 accessors): `id`, `meta`, `types`, `services`, `buzzers`. Trivial — `Domain` already carries `id`/`meta`/`userTypes`; services/buzzers/streams live in `userTypes` post-F16. Adapter must synthesize a `DomainDefinition` value or expose a subset shape the translators read.

**`typespace.structure.*`** (4 methods): `structure(StructureId): Struct`, `structure(WithStructure): Struct`, `structure(Identifier): PlainStruct`, `sameSignature`, `conversions`. Used heavily by Scala extensions (CirceTranslatorExtensionBase, AnyvalExtension, CastUp/Down, CastSimilar, CompositeStructure).

**`typespace.inheritance.*`** (3 methods): `allParents`, `parentsInherited`, `implementingDtos`. Used by Circe extension (line 110) and Scala translator.

**`typespace.tools.*`** (9 methods): `idToParaName`, `defnId`, `implId`, `sourceId`, `methodToOutputName`, `methodToPositiveTypeName`, `methodToNegativeTypeName`, `toPositiveBranchName`, `toNegativeBranchName`. All of these are pure name-mangling; today they live in `TypespaceToolsImpl` and depend on nothing typespace-shaped. The adapter can delegate to a `TypespaceToolsImpl(domain-derived TypespaceImpl-equivalent)` OR copy-paste the pure name-mangling logic.

**`typespace.resolver.*`** (`apply(ServiceId)`, `apply(TypeId)`, `get(InterfaceId)/DTO/Identifier/Structure`). Used internally by `StructuralQueriesImpl`/`FieldExtractor`. If the adapter reuses those impls (see §6 strategy), it needs to provide a working `resolver` field.

**`typespace.apply(...)`** + **`typespace.dealias(...)`**: top-level entries. Both are simple — `dealias` walks the alias chain (Domain.aliases is materialized), `apply` looks up via `members.get(id)` + `userTypes`.

Total adapter surface: ~25 methods across 5 sub-traits. NOT all need real impls — see §6's "real-impl vs stub" split.

### A3 — Harness construction sites for `CompilerOptions` / `TypespaceCompilerBaseFacade`

- `HarnessOptions.scala:55-63` — only construction site (named args; A1 confirms no churn).
- `idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/GoldenCompile.scala` — invokes `TypespaceCompilerBaseFacade` via the options. Reads `HarnessOptions.optionsFor(lang)`. The default is `Legacy` so the four harness contracts remain on the legacy path. **CRITICAL invariant**: do not touch `HarnessOptions.scala` in IMPL-6 — leaving its `Legacy` default in place is the only way the contracts stay green by definition. (Parity testing of the new path under the harness is PR-03's job.)
- `IDLTestTools.scala` (test) — uses `UntypedCompilerOptions` indirectly; same default mechanism keeps it on legacy.

### A4 — How does `LoadedDomain.Success` carry the parsed AST?

It does NOT. Today (`LoadedDomain.scala:11`):

```scala
final case class Success(path: FSPath, typespace: Typespace, warnings: Vector[IDLWarning])
```

It carries only the post-typed `Typespace`. The new typer's Phase 0 (`IdealinguaFamilyManager`) takes `DomainMeshLoaded` (`IdealinguaFamilyManager.scala:27`), which is the pre-typer, post-loader shape.

**Tactic chosen** (the minimum-scope solution): the adapter and the pipeline driver in IMPL-6 cannot recover the parsed shape from `ts.domain` (it's already post-typed).

So IMPL-6 must take ONE OF the following alternatives:

  **(a)** Extend `LoadedDomain.Success` with `parsed: DomainMeshResolved`, populated by `ModelResolver.resolve`.

  **(b)** Re-run the loader from the file path: `LoadedDomain.Success.path` is `FSPath`, but invoking the loader twice doubles parse cost and re-introduces a re-resolution pass.

**Recommendation: (a).** Concretely:

- `LoadedDomain.scala:11` → add `parsed: DomainMeshResolved` to the `Success` case class.
- `ModelResolver.scala:32-40` `makeTyped` → propagate the `DomainMeshResolved` through the `for/yield` into `LoadedDomain.Success` (it's already in scope as `d` at `runVerifier` invocation time; thread it as a third argument to `runVerifier` and embed it in the `Right(LoadedDomain.Success(...))` at line 46).

This is a 3-line surgical extension of `LoadedDomain.Success`. No new fields on the loader; no double-parse. The new pipeline driver then operates on `success.parsed`.

**Risk acknowledged**: this expands the IMPL-6 surface into `-model`. It is justifiable because the alternative is materially more expensive and harder to test, and the field is forward-compatible (it will become the *only* field once the legacy `typespace` is removed in IMPL-10).

### A5 — Type-ID compatibility between legacy AST and new typer IR

Per F16 resolution (IMPL-2/3 absorbed `RawTypeDef`/`TypeDef` into the same `TypeId` keyspace): `idealingua-v1-model/.../typer/ir/TypeDef.scala` uses `izumi.idealingua.model.common.TypeId` — the same `TypeId` type the legacy `Typespace` exposes. **Confirmed: no ID translation needed at the adapter boundary.** The adapter `Domain.members.get(id)` lookup is direct; no type-id remapping is required.

## §4 File-by-file edits (new files + edits with line numbers from HEAD)

### New files (4 in the production tree, 1 spec)

1. `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TyperImpl.scala` (new, ~20 lines).
   - `sealed trait TyperImpl`, `case object Legacy`, `case object NewTyper`, `def parse(s: String): TyperImpl` matching IDLLanguage shape, unknown → `MatchError` per the IDLLanguage convention.

2. `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/compat/NewTyperPipeline.scala` (new, ~80 lines).
   - `def run(parsed: DomainMeshResolved): Domain` — chains Phase 0 → 1 → ... → 12, threads `Diagnostics` across phases, throws `IDLException` aggregating diagnostics if any are non-empty (C8 bridge per §2-P3).

3. `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/compat/DomainAsTypespace.scala` (new, ~250 lines). See §6 shape.

4. `idealingua-v1/idealingua-v1-transpilers/src/test/scala/izumi/idealingua/translator/compat/NewTyperFeatureFlagSpec.scala` (new, ~80 lines). Smoke test per §7.

### Edits

5. `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/CompilerOptions.scala:69-77`. Append `typerImpl: TyperImpl = TyperImpl.Legacy` as a new defaulted parameter at the end of the `UntypedCompilerOptions` case class. Import `TyperImpl` (same package, no import needed). Update the `toString` at line 78-83 to include `typerImpl` representation (e.g. `s"+typer=${typerImpl}"`).

6. `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/TypespaceCompilerBaseFacade.scala:9-14`. Replace `compile` body. New shape:

   ```scala
   def compile(toCompile: Seq[LoadedDomain.Success]): Layouted = {
     val descriptor = TypespaceCompilerBaseFacade.descriptor(options.language)
     val compiled = toCompile.map { loaded =>
       options.typerImpl match {
         case TyperImpl.Legacy =>
           descriptor.make(loaded.typespace, options).translate()
         case TyperImpl.NewTyper =>
           val domain = NewTyperPipeline.run(loaded.parsed)
           val adapter = new DomainAsTypespace(domain, loaded.typespace)
           descriptor.make(adapter, options).translate()
       }
     }
     val hook = descriptor.makeHook(options)
     hook.layout(compiled)
   }
   ```

   The `loaded.typespace` argument passed as a fallback into `DomainAsTypespace`'s constructor is a *temporary* defensive measure: any adapter method that throws `NotImplementedError` at IMPL-6 can be widened to fall back to the legacy `Typespace` (so the new-typer path still produces valid output during IMPL-7a/b/c porting). Once IMPL-7c lands, the fallback can be removed.

7. `idealingua-v1/idealingua-v1-model/src/main/scala/izumi/idealingua/model/loader/LoadedDomain.scala:11`. Per §3-A4 recommendation (a): change `final case class Success(path: FSPath, typespace: Typespace, warnings: Vector[IDLWarning])` to `final case class Success(path: FSPath, typespace: Typespace, parsed: DomainMeshResolved, warnings: Vector[IDLWarning])`. Add `import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved` at the top.

8. `idealingua-v1/idealingua-v1-core/src/main/scala/izumi/idealingua/il/loader/ModelResolver.scala`:
   - Line 33: change `for { d <- f; ts <- runTyper(d); result <- runVerifier(ts) }` → `for { d <- f; ts <- runTyper(d); result <- runVerifier(ts, d) }`.
   - Line 42 signature: `private def runVerifier(ts: Typespace, parsed: DomainMeshResolved): Either[...VerificationFailed, LoadedDomain.Success]`.
   - Line 46: `Right(LoadedDomain.Success(ts.domain.meta.origin, ts, parsed, issues.warnings))`.

9. `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/IDLCArgs.scala`:
   - Line 20-30: add `typerImpl: TyperImpl = TyperImpl.Legacy` to the `IDLCArgs` case class.
   - Line 33-42 (`default`): pass `TyperImpl.Legacy` (or rely on default).
   - Line 44-52 (`P` ParserDef): add `final val typerImpl = arg("typer", "T", "typer implementation", "<legacy|new>")`.
   - Line 96-105 area: after `parseDefs`, decode the optional `typerImpl` arg via `parameters.findValue(P.typerImpl).map(v => TyperImpl.parse(v.value)).getOrElse(TyperImpl.Legacy)`.
   - Line 132-142 (constructor call): pass the decoded value.
   - Import `izumi.idealingua.translator.TyperImpl` at the top.

10. `idealingua-v1/idealingua-v1-compiler/src/main/scala/izumi/idealingua/compiler/CommandlineIDLCompiler.scala:181-187` (`toOptions`):
    - Thread `conf.typerImpl` from `IDLCArgs` into `UntypedCompilerOptions`. Add `typerImpl = conf.typerImpl` as a named arg at line 186.

11. `idealingua-v1/idealingua-v1-test-harness/src/main/scala/izumi/idealingua/harness/HarnessOptions.scala:55-63`. **NO CHANGE.** The named-arg construction picks up the default `Legacy` automatically per §3-A3 invariant.

12. Any other tests that destructure `LoadedDomain.Success(path, ts, warnings)` need a 3→4 component update. Grep target:
    - Update spec files under `-model/src/test/` and `-test-harness/src/test/` that construct `LoadedDomain.Success(...)` directly.

**Total estimated diff: ~400 LOC added, ~10 LOC edited, 6 files touched + 4 new files.**

## §5 NewTyper pipeline driver shape

`NewTyperPipeline.run(parsed: DomainMeshResolved): Domain`:

```scala
object NewTyperPipeline {
  def run(parsed: DomainMeshResolved): Domain = {
    val rootMesh = synthesizeLoaded(parsed)
    val family = IdealinguaFamilyManager(rootMesh)

    val scoped       = ScopeBuilder(parsed.id, family)
    val resolved     = NameResolver(scoped)
    val dealiased    = AliasDealiaser(resolved)
    val kindChecked  = KindChecker(dealiased)
    val withCycles   = CycleDetector(kindChecked)
    val flattened    = StructuralFlattener(withCycles)
    val withEphem    = EphemeralSynthesizer(flattened)
    val withConsts   = ConstValueTyper(withEphem)
    val withFinger   = FingerprintCalculator(withConsts)
    val rooted       = RootExtractor(withFinger)
    val domain       = Assembler(rooted)

    val validatorDiags = Validator(domain)

    val allDiags = family.diagnostics ++ scoped.diagnostics ++ ... ++ validatorDiags
    if (allDiags.issues.nonEmpty) {
      // TODO IMPL-9: route diagnostics through LoadedDomain.VerificationFailed
      throw new IDLException(s"New typer diagnostics for ${parsed.id}: ${allDiags.issues.niceList()}")
    }

    domain
  }
}
```

**Open call signatures to verify against actual phase APIs**: implementers must adjust the chaining to the actual ADTs in `typer/ir/ResolvedDomain.scala` / `typer/ir/FamilyIndex.scala`. The shape above is the *intent*; the wiring is mechanical.

## §6 `DomainAsTypespace` adapter shape

Lives at `translator/compat/DomainAsTypespace.scala`. Constructor:

```scala
final class DomainAsTypespace(
  domain: Domain,
  legacyFallback: Typespace, // temporary; removed in IMPL-11
) extends Typespace
```

The adapter's strategy is **delegate-by-default + selective override**. Because the surviving translators read 25+ methods (per §3-A2), reimplementing every one is wasted effort given IMPL-7a/b/c will delete the call sites. So:

- **Real implementations (high-value, simple, queried by *every* translator):**
  - `domain: DomainDefinition` — synthesize from `Domain.userTypes` + `Domain.ephemeralsOf`. Map `userTypes.values.collect{case s: Service => s}` → `services`; same for buzzers/streams. `id = domain.id`. `meta = domain.meta`.
  - `apply(id: TypeId): TypeDef` — `domain.members(id)` match: `Member.User(defn) ⇒ defn`; `Member.Ephemeral(e) ⇒ e.asTypeDef`; `Member.Builtin(p) ⇒ p.asTypeDef`.
  - `apply(id: ServiceId): Service` — lookup in `domain.userTypes`.
  - `dealias(t)` — walk `Domain.aliases` once (it's already materialized).
  - `types: TypeCollection` — re-wrap.
  - `tools: TypespaceTools` — instantiate the existing `TypespaceToolsImpl(this)` from `-model`. It is *pure*.

- **Delegated to `legacyFallback` at IMPL-6:**
  - `structure: StructuralQueries` — `legacyFallback.structure`.
  - `inheritance: InheritanceQueries` — `legacyFallback.inheritance`.
  - `resolver: TypeResolver` — `legacyFallback.resolver`.
  - `transitivelyReferenced` — `legacyFallback.transitivelyReferenced`.

This minimizes adapter LOC at IMPL-6 while still exercising the new pipeline on every code path. As IMPL-7a/b/c lands, each translator stops using its delegated sub-trait, and the corresponding fallback can be removed.

**Field-ordering invariant (C12/L3).** The adapter MUST NOT call any sort on field lists when surfacing `DomainDefinition.types`. Implementer note: verify against `typer/ir/Domain.scala:55-72` whether `userTypes` is an ordered map (`ListMap`) — if it's a plain `Map`, the adapter must reconstruct order from `parsed.members` or from Phase 7's `flattenedStructs` insertion order.

## §7 Verification protocol

**Default-flag (`Legacy`) regression.** Re-run all four FROZEN harness contracts:
- `sbt 'verifyGoldens'` — exit code 0 on Scala 2.13.18 + 3.8.3.
- `sbt 'runWireFixtures'` — exit code 0.
- `sbt 'runCrossLangInterop'` — exit code 0.
- `sbt 'idealingua-v1-test-harness/test'` — exit code 0.

**New-path smoke test (single unit test).** `NewTyperFeatureFlagSpec.scala`. Pseudocode:

```scala
class NewTyperFeatureFlagSpec extends AnyWordSpec {
  "TypespaceCompilerBaseFacade with TyperImpl.NewTyper" should {
    "compile a small fixture domain to Scala source" in {
      val loaded = TestFixtures.loadDomain("simple/structures.domain")
      val options = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.NewTyper)
      val out = new TypespaceCompilerBaseFacade(options).compile(Seq(loaded))
      assert(out.emodules.nonEmpty)
    }
  }
}
```

Fixture pick: a domain with no services and no constants. The intent is path-coverage, not parity.

**CLI smoke (manual; not in automated test).** Hand-run `./idlc --typer=new …` on a small project.

## §8 Risks

**R1 — Adapter trait shape is deeper than it appears.** Mitigated by §6's delegate-by-default strategy.

**R2 — `LoadedDomain.Success` signature change ripples to specs.** Expected churn: 5-10 sites in test files. *Acceptable.*

**R3 — Defaulted-final-param compatibility.** Verified at A1: both call sites either use positional + one named arg or all-named args.

**R4 — C8 bridge throws on new-typer user errors.** Until IMPL-9. Documented in code with TODO IMPL-9 marker.

**R5 — Phase wiring divergence from this plan's pseudo-code in §5.** Each phase's actual constructor signature must be cross-checked against `typer/phase/*.scala`. *Implementer task.*

**R6 — Field ordering in `DomainAsTypespace`.** Per C12/L3 (locked), fields must not be re-sorted. *Implementer task.*

**R7 — F18 re-emergence.** Closed.

## §9 Sub-task breakdown

- **T1.** Add `TyperImpl.scala` enum (file #1).
- **T2.** Add `typerImpl` field to `UntypedCompilerOptions` (edit #5). Compile -transpilers.
- **T3.** Extend `LoadedDomain.Success` with `parsed: DomainMeshResolved` (edit #7). Compile -model.
- **T4.** Wire `parsed` through `ModelResolver` (edit #8). Compile -core.
- **T5.** Fix downstream pattern-match call-sites for the new field (sweep). Compile + tests green.
- **T6.** Add `NewTyperPipeline.scala` (file #2) wiring phases 0-12. Local Scala compile.
- **T7.** Add `DomainAsTypespace.scala` (file #3). Verify against `Typespace.scala` member list. Local compile.
- **T8.** Edit `TypespaceCompilerBaseFacade.compile` to dispatch (edit #6). Verify Legacy path still works.
- **T9.** Add `--typer` CLI flag in `IDLCArgs` (edit #9). Thread through `CommandlineIDLCompiler.toOptions` (edit #10).
- **T10.** Add `NewTyperFeatureFlagSpec.scala` smoke test (file #4). Run.
- **T11.** Final sweep: full `sbt test`, `verifyGoldens`, `runWireFixtures`, `runCrossLangInterop`. Confirm green.
- **T12.** Update tasks.md: mark IMPL-6 done; F18 reconfirmed closed.

T1-T2 can be parallelised. T3-T5 are a single logical unit (one commit). T6-T8 must be sequenced (T8 depends on T6+T7). T9 is independent of T6-T7. T10-T11 close the loop. Estimated 1-2 engineering days.
