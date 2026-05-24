package izumi.idealingua.harness

import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

import scala.meta.*

/** Regression coverage for the 1.5.0 Scala-output `apply`-signature
  * regression.
  *
  * The 1.5.0 typer rewrite (#610) replaced the recursive DFS field
  * extractor with a BFS flattener that visits each ancestor exactly
  * once. As a consequence, fields contributed by a *diamond apex*
  * mixin — an ancestor reachable from a DTO through two or more
  * branches — are no longer flagged as ambiguous. The Scala renderer
  * grouped them under a single synthesized mixin parameter inserted
  * mid-signature instead of emitting them as scalar parameters at the
  * tail of `apply(...)` as the pre-1.5.0 codegen did. The reordered
  * signature can silently rebind callers across versions when the apex
  * type and a sibling direct-super share a subtype relation.
  *
  * Fixture: `idealingua-v1-test-defs/.../idltest/diamondapply.domain`
  * defines two DTOs over a shared `Base` mixin:
  *
  *   - `Event`: asymmetric diamond — `Event → WithAlpha → Base` (depth
  *     2) and `Event → Contract → WithBeta → Base` (depth 3). Pre-1.5.0
  *     emits the apex (`Base`) fields as scalars. Positive coverage.
  *
  *   - `SymmetricEvent`: symmetric diamond — both
  *     `SymmetricEvent → WithAlpha → Base` and
  *     `SymmetricEvent → WithBeta → Base` are depth 2. Pre-1.5.0 emits
  *     `Base` as an ordinary mixin parameter (apex NOT scalar-ized).
  *     Negative coverage so future widenings of the detector cannot
  *     silently sweep symmetric diamonds into the scalar branch.
  */
final class DiamondApplySignatureSpec extends AnyFunSuite {

  private def renderDto(dtoName: String): String = {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.diamondApplyCorpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val options    = HarnessOptions.optionsFor(IDLLanguage.Scala)
    val layouted   = new TypespaceCompilerBaseFacade(options).compile(loaded)
    layouted.emodules.collectFirst {
      case ExtendedModule.DomainModule(_, m)
          if m.id.path.endsWith(Seq("idltest", "diamondapply")) && m.id.name == s"$dtoName.scala" =>
        m.content
    }.getOrElse(fail(s"idltest/diamondapply/$dtoName.scala not emitted by the Scala translator"))
  }

  /** Parse the rendered Scala source and return every `def apply` defn,
    * paired with its parameter list projected as (name, declared-type).
    */
  /** Depth-first descent over scalameta's `Tree.children`, collecting
    * every node that matches the partial function. (`Tree#collect` is
    * not exposed on `Tree` in the scalameta build the harness uses, so
    * we do the walk by hand.)
    */
  private def collectTree[T](root: Tree)(pf: PartialFunction[Tree, T]): List[T] = {
    val out = scala.collection.mutable.ListBuffer.empty[T]
    def walk(t: Tree): Unit = {
      if (pf.isDefinedAt(t)) out += pf(t)
      t.children.foreach(walk)
    }
    walk(root)
    out.toList
  }

  private def applyDefs(src: String): List[(Defn.Def, List[(String, String)])] = {
    implicit val dialect: Dialect = scala.meta.dialects.Scala30
    val tree                      = src.parse[Source].get
    collectTree(tree) {
      case d: Defn.Def if d.name.value == "apply" =>
        val params = d.paramClauses.headOption.map(_.values).getOrElse(Nil).map { p =>
          val tpe = p.decltpe.map(_.toString).getOrElse(fail(s"apply param ${p.name.value} has no declared type"))
          (p.name.value, tpe)
        }
        (d, params)
    }
  }

  /** Of the apply defs on a DTO companion, return the *structural*
    * apply — the one whose first parameter is NOT `defn:
    * <DtoName>.Defn`. There is exactly one such overload.
    */
  private def structuralApply(defs: List[(Defn.Def, List[(String, String)])]): List[(String, String)] = {
    val structural = defs.filterNot { case (_, params) => params.headOption.exists(_._1 == "defn") }
    assert(structural.sizeIs == 1, s"expected exactly one structural apply, got ${structural.size}")
    structural.head._2
  }

  /** Extract the field-assignment names from the structural apply's
    * `new <DtoName>(...)` constructor invocation, in source order. Used
    * to assert how scalar params flow into the constructor.
    */
  private def constructorAssignments(d: Defn.Def): List[String] =
    collectTree(d.body) {
      case Term.New(init) => init
    }.flatMap { init =>
      init.argClauses.headOption.map(_.values).getOrElse(Nil).collect {
        case Term.Assign(Term.Name(n), _) => n
      }
    }

  test("Event (asymmetric diamond): structural apply scalar-izes the apex and pins parameter order") {
    val src = renderDto("Event")
    val defs = applyDefs(src)
    val params = structuralApply(defs)

    // Pin the entire parameter list. The two direct-super mixins come
    // first; the diamond-apex (`Base`) fields follow as scalars in
    // Scala-`Map[String,_]` iteration order over the *full* field-name
    // set (`Map1`–`Map4` insertion order if the full set has ≤ 4
    // distinct names, `HashMap` hash-trie order if ≥ 5). For this
    // fixture the full set has 7 distinct names → `HashMap` → the apex
    // subset emerges as (`at`, `note`, `kind`) — byte-identical to
    // v1.4.18.
    val expected: List[(String, String)] = List(
      "withbeta"  -> "WithBeta",
      "withalpha" -> "WithAlpha",
      "at"        -> "java.time.ZonedDateTime",
      "note"      -> "Option[String]",
      "kind"      -> "Kind",
    )
    assert(params == expected, s"structural apply parameter list mismatch.\nexpected: $expected\nactual:   $params")

    // No synthesized apex mixin parameter.
    assert(!params.exists { case (_, tpe) => tpe == "Base" }, s"`Base` must not appear as an apply parameter: $params")
  }

  test("Event (asymmetric diamond): apex fields flow from the bare scalar params into the constructor") {
    val src        = renderDto("Event")
    val defs       = applyDefs(src)
    val structural = defs.find { case (_, p) => !p.headOption.exists(_._1 == "defn") }
      .getOrElse(fail("no structural apply"))
    val assignedNames = constructorAssignments(structural._1)
    val expectedAssignments = List("betaId", "betaName", "alphaId", "alphaName", "at", "note", "kind")
    assert(assignedNames == expectedAssignments, s"new Event(...) field-assignment order mismatch.\nexpected: $expectedAssignments\nactual:   $assignedNames")
  }

  test("SymmetricEvent (symmetric diamond): apex stays a mixin parameter, no diamond-apex scalars") {
    val src    = renderDto("SymmetricEvent")
    val defs   = applyDefs(src)
    val params = structuralApply(defs)

    // Symmetric paths: pre-1.5.0 emits `Base` as a mixin parameter and
    // the structural apply has no apex-derived scalars.
    val expected: List[(String, String)] = List(
      "base"      -> "Base",
      "withbeta"  -> "WithBeta",
      "withalpha" -> "WithAlpha",
    )
    assert(params == expected, s"structural apply parameter list mismatch.\nexpected: $expected\nactual:   $params")

    // Negative-coverage assertion: `Base`-owned scalar names must not
    // appear as apply parameters here. If a future change widens the
    // diamond-apex detector to fire on symmetric diamonds, the three
    // scalars would surface here and break this assertion.
    val paramNames = params.map(_._1).toSet
    assert(!paramNames("kind"), s"`kind` must NOT appear as a scalar parameter on symmetric diamond: $params")
    assert(!paramNames("at"),   s"`at` must NOT appear as a scalar parameter on symmetric diamond: $params")
    assert(!paramNames("note"), s"`note` must NOT appear as a scalar parameter on symmetric diamond: $params")
  }
}
