package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.raw.domains.{ImportedId, SingleImport}
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class AliasDealiaserSpec extends AnyFunSpec with Matchers {

  import ScopeBuilderSpec._

  describe("AliasDealiaser") {
    it("resolves A -> B -> primitive") {
      val a = RawTypeDef.Alias(AliasId(TypePath(domA, Seq.empty), "A"), IndefiniteId(Seq.empty, "B"), meta)
      val b = RawTypeDef.Alias(AliasId(TypePath(domA, Seq.empty), "B"), IndefiniteId(Seq.empty, "str"), meta)
      val (input, _) = fixture(List(a, b), Nil, Map.empty)
      val r          = AliasDealiaser(NameResolver(scopeFor(input)))

      r.aliases(a.id) shouldBe Primitive.TString
      r.aliases(b.id) shouldBe Primitive.TString
      r.diagnostics.issues.collect { case d: Diagnostic.CyclicAlias => d } shouldBe empty
    }

    it("identity alias to same-name in another domain is not cyclic (F5b key-by-FQN)") {
      // `alias X = otherDomain#X`. Both sides have the simple name "X" but live
      // in different domains; the dealiaser's alias graph must key by
      // fully-qualified TypeId (AliasId carries its domain) so the local
      // `domA.X` and the foreign `domB.X` are distinct vertices — the edge is
      // an external pointer, not a self-loop.  Closes F5b-part2.
      val foreignX = dto("X", domB) // a real DTO in B named X
      val localXAlias = RawTypeDef.Alias(
        AliasId(TypePath(domA, Seq.empty), "X"),
        IndefiniteId(domB.toPackage, "X"),
        meta,
      )
      val (input, _) = fixture(
        local      = List(localXAlias),
        imports    = List(SingleImport(domB, ImportedId("X", None))),
        referenced = Map(domB -> resolved(domB, List(foreignX))),
      )
      val r = AliasDealiaser(NameResolver(scopeFor(input)))

      r.diagnostics.issues.collect { case d: Diagnostic.CyclicAlias => d } shouldBe empty
      r.aliases(localXAlias.id) shouldBe foreignX.id
    }

    it("emits CyclicAlias for A -> B -> A") {
      val a = RawTypeDef.Alias(AliasId(TypePath(domA, Seq.empty), "A"), IndefiniteId(Seq.empty, "B"), meta)
      val b = RawTypeDef.Alias(AliasId(TypePath(domA, Seq.empty), "B"), IndefiniteId(Seq.empty, "A"), meta)
      val (input, _) = fixture(List(a, b), Nil, Map.empty)
      val r          = AliasDealiaser(NameResolver(scopeFor(input)))

      r.diagnostics.issues.collect { case d: Diagnostic.CyclicAlias => d } should not be empty
      r.aliases.get(a.id) shouldBe None
    }
  }
}
