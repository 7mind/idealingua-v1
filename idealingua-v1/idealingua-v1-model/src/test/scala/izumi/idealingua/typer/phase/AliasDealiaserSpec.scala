package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{IndefiniteId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.raw.defns._
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
