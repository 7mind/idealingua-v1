# PR-02 IMPL-7a — Scala translator port to consume `Domain` directly

Plan author: planning subagent (review-loop, 2026-05-11).
Source briefs: tasks.md PR-02 line 46; master plan §2 line 91-97 (IMPL-7a brief), §2 line 118 (parallelisable with 7b/7c), §2 line 138-149 (lifecycle), §2 line 183-191 (per-translator atomic group), §4 line 740-754 (IR-vs-translator boundary, locked); cross-cutting C12/L3 field-order, C8/L1 diagnostics, L6 freeze tag baseline; predecessor `docs/drafts/20260511-PR02-IMPL06-feature-flag-plan.md`.

---

## §1 Goal & non-goals

**Goal.** Single atomic PR on `wip/necromancy` that ports the Scala back-end (`idealingua-v1-transpilers/.../toscala/`) to consume the new-typer `Domain` IR directly under the `--typer=new` path, while leaving the legacy translator unchanged for `--typer=legacy` (still the default). Adds a corpus-wide byte-parity test that asserts byte-equal Scala output between the two paths. Closes IMPL-7a per master plan §2 line 91-97.

**Post-IMPL-7a invariants.**
- Default-flag (`Legacy`) build is byte-identical to pre-PR HEAD on all four FROZEN harness contracts at Scala 2.13.18 and 3.8.3.
- `--typer=new` selecting Scala produces byte-equal output to `--typer=legacy` on the entire `defs/main-tests/source/` corpus (28 `.domain` files per C13). Master-plan §2 line 143-145 parity gate, Scala-only.
- `DomainAsTypespace` adapter still in use for TS and C#; for Scala dispatch it is bypassed.
- Legacy `ScalaTranslator` class remains intact; IMPL-10 deletes it atomically.

**Non-goals.**
- Per-translator port for TS (IMPL-7b) or C# (IMPL-7c).
- Flipping the default flag (IMPL-9).
- Removing `DomainAsTypespace` or the legacy `Typespace` query trait (IMPL-10/11).
- Diagnostics carry-through to translators.
- Performance work.

---

## §2 Pre-locked decisions

**P1. Strategy = B (additive new translator class, NOT in-place port).** A fresh class lets IMPL-7a be purely additive on the new-path surface — zero risk to the legacy code path. The byte-parity gate becomes an explicit test diffing two distinct output streams. IMPL-10 deletion is then a single `git rm` over the legacy tree.

Trade-off accepted: ~1700 LOC of mostly-copied translator code lives in parallel until IMPL-10. Cost of A — every renderer mutating to discriminate on input shape — re-introduces lazy queries on Typespace, contradicting master plan §4 line 740-754.

**P2. New translator class lives at `translator/toscala/domain/DomainScalaTranslator.scala`.** Subdirectory under `toscala/` so IMPL-10's delete sweep is mechanical; afterward `domain/` is moved up.

**P3. `TranslatorDescriptor` gains `makeDomain(domain: Domain, options): Translator`.** `ScalaTranslatorDescriptor.makeDomain` returns `new DomainScalaTranslator(...)`. TS+C# descriptors stub with `throw new NotImplementedError("IMPL-7b/7c")`. Stubs never hit at IMPL-7a runtime — facade routes only Scala through `makeDomain`.

**P4. `Translated` widens to carry domain identity, not the new IR domain.** `case class Translated(domainId: DomainId, meta: DomainMetadata, modules: Seq[Module])` — drop `typespace: Typespace`. Both fields available regardless of which path produced them. Layouter call sites (`out.typespace.domain.id`, `out.typespace.domain.meta.directImports`) become `out.domainId`, `out.meta.directImports`.

**P5. New translator does NOT read `loaded.typespace` at all.** Strict invariant: `DomainScalaTranslator(domain: Domain, options: ScalaTranslatorOptions)` takes ONLY the new IR. Enacts master-plan §4 line 740-754.

**P6. Field-order invariant (C12/L3) enforced explicitly.** Translator iterates `domain.userTypes.values` in the same order legacy translator iterates `typespace.domain.types`. Since `Domain.userTypes` is a plain `Map`, order recovery comes from `loaded.parsed.members` (raw AST in declaration order) — see R1.

**P7. Byte-parity gate is a new test in test-harness, NOT a new sbt task.** Spec at `idealingua-v1-test-harness/src/test/scala/izumi/idealingua/harness/ScalaTyperParitySpec.scala`. Runs under FROZEN contract #4.

**P8. Surviving Circe extension reads.** Per master plan §2 line 96: `CirceTranslatorExtensionBase.withDerivedClass` line 187 `ctx.typespace.structure.structure(id)` becomes `ctx.domain.flattenedStructs(id)` in the ported Circe extension. `inheritance.implementingDtos` at line 110 becomes `ctx.domain.implementingDtos.getOrElse(id, Set.empty).toList.sortBy(_.toString)` — explicit deterministic sort.

---

## §3 Audit results

### A1 — Every `ctx.typespace.*` access in `toscala/` (27 sites)

**`ScalaTranslator.scala`** (3 sites — domain field reads):
| Line | Call | Maps to |
|---|---|---|
| 36 | `ctx.typespace.domain.types.collect{case a: Alias => …}` | `domain.userTypes.values.collect{case a: TypeDef.Alias => …}` — C12/L3 requires declaration order (P6/R1) |
| 59 | `ctx.typespace.domain.types.flatMap(translateDef)` | Same as 36; declaration order required |
| 60 | `ctx.typespace.domain.services.flatMap(translateService)` | Filter `domain.userTypes` for `TypeDef.Service` (post-F16) |
| 61 | `ctx.typespace.domain.buzzers.flatMap(translateBuzzer)` | Filter for `TypeDef.Buzzer` |

**`tools/ScalaTranslationTools.scala`** (5 sites):
| Line | Call | Maps to |
|---|---|---|
| 31 | `ctx.typespace.structure.structure(id).toScala` | `domain.flattenedStructs(id)` → DomainScalaStruct conversion. Legacy `Struct` carries `unambigious`/`ambigious`/`all`; new `FlatStruct` carries `fields` + `conflictsSoft`/`conflictsHard`. Synthesise the legacy split |
| 35 | `ctx.typespace.tools.idToParaName(id)` | `id.name.toLowerCase` — copy 5 LOC |
| 47 | `ctx.typespace.tools.defnId(s)` | `domain.ephemeralOwner.get(id).collect{case i: InterfaceId => i}` or synth from name |
| 62 | `ctx.typespace.dealias(field.sourceType)` | Walk `domain.aliases` — Phase 3 pre-resolved |
| 86 | `ctx.typespace.tools.defnId(d: DTOId)` | Same as 47 |

**`extensions/CirceTranslatorExtensionBase.scala`** (3 sites):
| Line | Call | Maps to |
|---|---|---|
| 110 | `ctx.typespace.inheritance.implementingDtos(interface.id)` | `domain.implementingDtos.getOrElse(id, Set.empty).toList.sortBy(_.toString)` |
| 187 | `ctx.typespace.structure.structure(id)` | `domain.flattenedStructs(id)` + DomainScalaStruct conversion |
| 214 | `ctx.typespace(a: AliasId)` | `domain.userTypes(a)` |

**`extensions/AnyvalExtension.scala`** (4 sites): lines 31, 95, 106 `ctx.typespace.structure.structure(id)` → `domain.flattenedStructs(id)`; line 86 `ctx.typespace(a: AliasId)` → `domain.userTypes(a)`.

**`extensions/CastSimilarExtension.scala`** (2 sites):
- line 22: `ctx.typespace.structure.structure(interface)` → `domain.flattenedStructs(interface.id)`
- line 27: `ctx.typespace.structure.sameSignature(struct.id)` — NO direct IR equivalent. Compute on the fly (~10 LOC inline)

**`extensions/CastUpExtension.scala`** (3 sites):
- line 23: `ctx.typespace.structure.structure(interface)` → `domain.flattenedStructs`
- line 29-30: `ctx.typespace.structure.structuralParents(...)` — NO direct equivalent. Compute on the fly (~15 LOC inline)
- line 35: `ctx.typespace.tools.implId(i: InterfaceId)` → `DTOId(i, "Struct")`

**`extensions/CastDownExpandExtension.scala`** (1 site):
- line 12: `ctx.typespace.structure.conversions(interface.id)` — NO direct equivalent. Port body from `StructuralQueriesImpl.scala:195-239` (~50 LOC). Largest single port unit.

**`types/CompositeStructure.scala`** (1 site):
- line 22: `ctx.typespace.structure.constructors(struct)` — NO direct equivalent. Port from `StructuralQueriesImpl.scala:128-165` (~40 LOC).

**`types/ServiceMethodProduct.scala`** (6 sites in `Output` inner object):
- lines 209, 224, 225: tools name mangling — copy verbatim from `TypespaceToolsImpl.scala:46-55`
- lines 235, 237: `toPositiveBranchName/toNegativeBranchName` → constants `"Success"/"Failure"`
- lines 276, 279: `ctx.typespace.apply(typespaceId).asInstanceOf[Adt]` → `domain.userTypes(typespaceId).asInstanceOf[TypeDef.Adt]`

### A2 — `typespace.` accesses without `ctx.` prefix

- `ScalaTranslator.scala:27` constructor `ScalaTranslator(ts: Typespace, options: ScalaTranslatorOptions)` → twinned as `DomainScalaTranslator(domain, options)`.
- `ScalaTranslator.scala:30` `new STContext(ts, …)` → `new DomainSTContext(domain, …)`.
- `STContext.scala:11` `val typespace: Typespace` → `val domain: Domain` in `DomainSTContext`.
- `STContext.scala:15` `new ScalaTypeConverter(typespace.domain.id)` → `new ScalaTypeConverter(domain.id)`.
- `ScalaTranslator.scala:65` `Translated(ts, ctx.ext.extend(modules))` → `Translated(domain.id, domain.meta, ctx.ext.extend(modules))`.
- `layout/ScalaLayouter.scala:38,51,77` — `out.typespace.domain.id` and `out.typespace.domain.meta.directImports` survive via `out.domainId` / `out.meta.directImports`.
- `ScalaTranslatorDescriptor.scala:21` `make(typespace, options)` UNCHANGED (legacy); ADD `makeDomain(domain, options)`.

### A3 — Legacy → new-IR field-mapping table

| Legacy call | New-IR expression | Notes |
|---|---|---|
| `typespace.domain.id` | `domain.id` | Direct |
| `typespace.domain.meta` | `domain.meta` | Direct |
| `typespace.domain.types: List[TypeDef]` | Project from `loaded.parsed.members` order onto `domain.userTypes` | R1 — see below |
| `typespace.domain.services` / `.buzzers` | Filter `domain.userTypes` post-F16 | Same ordering risk |
| `typespace.apply(id)` | `domain.userTypes(id)` | Direct |
| `typespace.dealias(t)` | `t match { case a: AliasId => domain.aliases.getOrElse(a, t); case _ => t }` | Phase 3 pre-resolved |
| `typespace.structure.structure(id)` | `domain.flattenedStructs(id)` + DomainScalaStruct | Phase 6 materialised |
| `typespace.structure.sameSignature(tid)` | Compute inline | ~10 LOC |
| `typespace.structure.conversions(interface)` | Port from `StructuralQueriesImpl.scala:195-239` | ~50 LOC |
| `typespace.structure.constructors(struct)` | Port from `StructuralQueriesImpl.scala:128-165` | ~40 LOC |
| `typespace.structure.structuralParents(s)` | Compute inline | ~15 LOC |
| `typespace.inheritance.implementingDtos(id)` | `domain.implementingDtos.getOrElse(id, Set.empty).toList.sortBy(_.toString)` | R2 — order |
| `typespace.tools.idToParaName` etc. | Pure copy from `TypespaceToolsImpl` | Pure |
| `typespace.tools.implId(i: InterfaceId)` | `DTOId(i, "Struct")` | Constant |
| `typespace.tools.defnId(s)` | `domain.ephemeralOwner.get(s).collect{case i: InterfaceId => i}` or synth | Phase 7 materialised |
| `typespace.types.isInterfaceEphemeral(d)` | `domain.ephemeralOwner.contains(d)` with InterfaceId guard | Direct |

**Verdict**: every Typespace method used by Scala translator has either a direct `Domain.*` equivalent, a pure-name-mangling delegate (copy), or a small (≤50 LOC) port-in-place computation. No IR gap blocks IMPL-7a.

### A4 — IMPL-6 adapter shape & wiring

`DomainAsTypespace(val newDomain: NewDomain, legacy: Typespace) extends TypespaceImpl(legacy.domain)`. `newDomain` field is `val`, public.

IMPL-7a dispatch strategy: at `TypespaceCompilerBaseFacade.scala:13-22`, replace the unconditional `descriptor.make(typespace, options).translate()` with a per-language branch. Scala+NewTyper → `descriptor.makeDomain(newDomain, options).translate()` (skipping adapter); TS+C# NewTyper or any Legacy fall through to existing path.

### A5 — CirceTranslatorExtensionBase

Specific call-out per master plan §2 line 96: line 187 `ctx.typespace.structure.structure(id)` → `ctx.domain.flattenedStructs(id)` in the ported extension. Downstream pattern `struct.all.head.field` becomes `flatStruct.fields.head.field`. Both lists preserve declaration order; bytes equal iff orders match — see R1.

### A6 — Extensions enumeration

Production list (per `ScalaTranslator.scala:17-23` `defaultExtensions`):
1. `AnyvalExtension` (4 sites)
2. `CastSimilarExtension` (2 sites)
3. `CastDownExpandExtension` (1 site)
4. `CastUpExtension` (3 sites)
5. `CirceDerivationTranslatorExtension` extends `CirceTranslatorExtensionBase` (3 sites in base)

Total extension files to twin: 5. Trait + composer + descriptor stay shared. New extensions live at `toscala/domain/extensions/`.

### A7 — `STContext` audit

Constructor: `class STContext(val typespace: Typespace, extensions: Seq[ScalaTranslatorExtension], val sbtOptions: SbtOptions)`. Internal fields: `conv = new ScalaTypeConverter(typespace.domain.id)` — needs only `DomainId`. `tools`, `ext`, renderers — all take `this`.

Resolution: `DomainSTContext(val domain: Domain, extensions, val sbtOptions)`. Renderer files currently read `ctx.typespace.*` indirectly; full twin tree (renderers, tools, types) under `toscala/domain/`.

Renderer-file twin count:
- `CompositeRenderer`, `AdtRenderer`, `InterfaceRenderer`, `IdRenderer`, `ServiceRenderer`, `EnumRenderer` (6)
- `tools/ScalaTranslationTools`, `tools/ScalaMetaTools` (verify shared)
- `types/CompositeStructure`, `types/ServiceMethodProduct`, `types/StructContext`, `types/ScalaStruct`, `types/ScalaField`, `types/ScalaTypeConverter` (audit each)

Files with no `ctx.typespace.*` references can be shared via interface widening.

---

## §4 File-by-file edits

### New files (~20 production + 1 spec)

1. `…/toscala/domain/DomainScalaTranslator.scala` (~120 lines mirroring `ScalaTranslator.scala`).
2. `…/toscala/domain/DomainSTContext.scala` (~30 lines).
3. `…/toscala/domain/DomainScalaTranslationTools.scala` (~110 lines; ports 5 sites per A1).
4. `…/toscala/domain/DomainScalaStruct.scala` (~50 lines; wraps `FlatStruct`, synthesises legacy lists).
5. `…/toscala/domain/DomainCompositeStructure.scala` (~60 lines; ports `constructors`).
6-11. `…/toscala/domain/DomainCompositeRenderer.scala`, `DomainAdtRenderer.scala`, `DomainInterfaceRenderer.scala`, `DomainIdRenderer.scala`, `DomainServiceRenderer.scala`, `DomainEnumRenderer.scala` — twins.
12. `…/toscala/domain/DomainServiceMethodProduct.scala` (~280 lines; ports 6 sites).
13-15. `DomainStructContext`, `DomainFullServiceContext`, `DomainServiceContext` — twins if input references survive.
16. `…/toscala/domain/extensions/DomainAnyvalExtension.scala` (~115 lines).
17. `…/toscala/domain/extensions/DomainCastSimilarExtension.scala` (~55 lines; inlines `sameSignature`).
18. `…/toscala/domain/extensions/DomainCastUpExtension.scala` (~75 lines; inlines `structuralParents`).
19. `…/toscala/domain/extensions/DomainCastDownExpandExtension.scala` (~80 lines; inlines `conversions`).
20. `…/toscala/domain/extensions/DomainCirceTranslatorExtensionBase.scala` (~285 lines).
21. `…/toscala/domain/extensions/DomainCirceDerivationTranslatorExtension.scala` (~30 lines).

22. `idealingua-v1/idealingua-v1-test-harness/src/test/scala/izumi/idealingua/harness/ScalaTyperParitySpec.scala` (~80 lines, per §6 V2).

### Edits (6 files)

23. `idealingua-v1/idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/Translator.scala:6`: change `case class Translated(typespace: Typespace, modules: Seq[Module])` → `Translated(domainId: DomainId, meta: DomainMetadata, modules: Seq[Module])`.

24. `…/translator/TranslatorDescriptor.scala`: add abstract `makeDomain(domain: Domain, options): Translator`.

25. `…/translator/toscala/ScalaTranslatorDescriptor.scala:21`: keep `make`; add `override def makeDomain(...) = new DomainScalaTranslator(...)`.

26. `…/translator/totypescript/TypescriptTranslatorDescriptor.scala`: stub `makeDomain` with NotImplementedError("IMPL-7b"). Same for `…/tocsharp/CSharpTranslatorDescriptor.scala`.

27. `…/translator/TypespaceCompilerBaseFacade.scala:12-22`: replace `match` body with per-language branch on NewTyper (see A4).

28. Layouter sweep: `ScalaLayouter.scala:38,51,77`, `TypescriptLayouter`, `CSharpLayouter`: `out.typespace.domain.id` → `out.domainId`; `out.typespace.domain.meta.directImports` → `out.meta.directImports`. Also ripple the new `Translated` shape into all three legacy translators' `translate()` body.

---

## §5 `DomainScalaStruct` adapter shape

```scala
final class DomainScalaStruct(
  val flat: FlatStruct,
  val id: StructureId,
  val superclasses: Super,
  val unambigious: List[ExtendedField],
  val ambigious: List[ExtendedField],
  val all: List[ExtendedField],
)

object DomainScalaStruct {
  def fromFlat(flat: FlatStruct, superclasses: Super, domain: Domain): DomainScalaStruct = {
    val ambigNames = flat.conflictsSoft.iterator.map(_.name).toSet
    val all = flat.fields.map(ff => toExtendedField(ff, domain))
    val unambigious = all.filterNot(f => ambigNames.contains(f.field.name))
    val ambigious   = all.filter(f => ambigNames.contains(f.field.name))
    new DomainScalaStruct(flat, flat.ownerId, superclasses, unambigious, ambigious, all)
  }

  private def toExtendedField(ff: FlatField, domain: Domain): ExtendedField = {
    val originStruct = domain.userTypes(ff.origin).asInstanceOf[TypeDef.WithFields]
    val definedWithIndex = originStruct.fields.indexWhere(_.name == ff.field.name)
    ExtendedField(ff.field, FieldDef(ff.origin, definedWithIndex, ..., ff.distance, ...))
  }
}
```

**R3 — Field-order parity caveat**: legacy `StructuralQueriesImpl.scala:41` sorts by `(distance, definedBy.toString, -definedWithIndex)` reversed. New `FlatStruct.fields` is BFS-flattened in raw declaration+queue order. If byte parity diverges, executor applies the same sort in `DomainScalaStruct.fromFlat`. `definedWithIndex` recoverable by indexing into source struct's `fields`.

---

## §6 Verification protocol

### V1 — Default-flag regression (FROZEN contracts)

Four FROZEN harness contracts at Scala 2.13.18 and 3.8.3:
- `sbt verifyGoldens` — exit 0
- `sbt runWireFixtures` — 25/19/20 pass
- `sbt runCrossLangInterop` — 112/10/6
- `sbt idealingua-v1-test-harness/test` (including new `ScalaTyperParitySpec` + existing `NegativeSpec`)

### V2 — Scala byte-parity gate

`ScalaTyperParitySpec`:
```scala
class ScalaTyperParitySpec extends AnyWordSpec {
  "the Scala translator under --typer=new" should {
    "produce byte-equal output to --typer=legacy on the full corpus" in {
      val corpus = HarnessCorpus.loadCorpus(...)
      val legacyOpts = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.Legacy)
      val newOpts    = legacyOpts.copy(typerImpl = TyperImpl.NewTyper)
      val legacyOut  = new TypespaceCompilerBaseFacade(legacyOpts).compile(corpus)
      val newOut     = new TypespaceCompilerBaseFacade(newOpts).compile(corpus)
      val legacyMap  = bytesByPath(legacyOut)
      val newMap     = bytesByPath(newOut)
      assert(legacyMap.keySet == newMap.keySet)
      legacyMap.foreach { case (path, legacyBytes) =>
        assert(legacyBytes sameElements newMap(path), s"byte diff at $path")
      }
    }
  }
}
```

### V3 — Smoke (manual)

`./idlc --typer=new idltest/enums.domain --target=/tmp/out` and diff against `--typer=legacy`.

---

## §7 Risks

**R1 — Domain.userTypes iteration order** (high). Legacy `domain.types: List[TypeDef]` is in declaration order; new `Domain.userTypes: Map[TypeId, TypeDef]` is hash-order. Resolution: read order from `loaded.parsed.members` (raw AST in declaration order) and project onto new IR. Tested first on smallest fixture before porting bulk. If declaration order unrecoverable from `Domain` alone, surfaces as F-followup blocker — IR widens to add `Domain.declarationOrder: List[TypeId]`.

**R2 — `domain.implementingDtos` is a Set** (high). Circe extension's `enc`/`dec` lists at `CirceTranslatorExtensionBase.scala:112-128` are order-sensitive. Legacy `InheritanceQueriesImpl.implementingDtos` returns `List` with order that may matter for byte-equality. Verification: dump legacy `implementingDtos` for the corpus's interface-bearing domains and compare against `domain.implementingDtos(_).toList.sortBy(_.toString)`. If diverges, sort by legacy ordering key.

**R3 — `FlatStruct.fields` order vs legacy `Struct.all` order** (see §5). Mitigated by adding legacy sort to `DomainScalaStruct.fromFlat`.

**R4 — `definedWithIndex` recoverable** — confirmed: read from `domain.userTypes(ff.origin)` and index source struct's fields. No IR widening required.

**R5 — Twin tree duplication** — accepted by P1.

**R6 — Layouter widening breaks TS/C#** — `Translated` field change ripples; one-line replace at each `out.typespace.domain.id` site.

**R7 — TS/C# `makeDomain` stubs** — unreachable until IMPL-7b/7c flip per-language branch.

**R8 — F10/F13 unchanged** — parity spec is Scala-only.

**R9 — IDLException C8 bridge** — exception message text differs but parity spec compares output bytes, not exception text.

---

## §8 Sub-task breakdown

- **T1.** Audit-confirm A1-A7 against HEAD.
- **T2.** Widen `Translated` (edit #23); ripple to layouters (edit #28) + all three legacy translators' `translate()` body. Compile clean on legacy path.
- **T3.** Add `TranslatorDescriptor.makeDomain` (edit #24); stub TS+C# (edit #26).
- **T4.** Scaffold `DomainSTContext`, `DomainScalaTranslator` skeleton (#1, #2) with stub `translate()`. Compile.
- **T5.** Wire `TypespaceCompilerBaseFacade.compile` dispatch (edit #27). Re-compile; default-flag harness contracts green.
- **T6.** Port `ScalaTranslator.translate()` body — alias loop, types/services/buzzers iteration with R1 ordering resolved.
- **T7.** Port `ScalaTranslationTools` (#3), `DomainScalaStruct` (#4), `DomainCompositeStructure` (#5).
- **T8.** Port 6 renderer twins (#6-#11).
- **T9.** Port `DomainServiceMethodProduct` (#12) and 3 service context twins (#13-#15).
- **T10.** Port 5 extensions (#16-#21). Port Circe base last (most byte-sensitive).
- **T11.** Add `ScalaTranslatorDescriptor.makeDomain` wiring (edit #25).
- **T12.** Add `ScalaTyperParitySpec` (#22). Iterate on order/conflict diffs until byte-parity over 28-domain corpus is green on both Scala versions.
- **T13.** Final sweep: four FROZEN harness contracts green. Update `tasks.md`.

**Sequencing**: T2-T3 parallel; T4-T5 sequential; T6-T11 parallel across renderer twins; T12 is the verification gate. Estimated 3-5 engineering days.
