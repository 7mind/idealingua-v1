package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, DomainMeshResolved, Import}
import izumi.idealingua.model.il.ast.raw.models.Inclusion
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.typer.ir.Diagnostic
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class IdealinguaFamilyManagerSpec extends AnyFunSpec with Matchers {

  import IdealinguaFamilyManagerSpec._

  describe("IdealinguaFamilyManager") {

    it("positive — single domain with no imports: 1 entry, empty importGraph, loadOrder = [domainId]") {
      val root = emptyDomain(domA, referenced = Map.empty)

      val fi = IdealinguaFamilyManager(root)

      fi.domains.keySet shouldBe Set(domA)
      fi.importGraph shouldBe Map(domA -> Set.empty)
      fi.loadOrder shouldBe List(domA)
      fi.diagnostics.isEmpty shouldBe true
    }

    it("positive — domain A imports domain B: 2 entries, correct graph, loadOrder is B then A") {
      val domainB = emptyDomain(domB, referenced = Map.empty)
      val root    = emptyDomain(domA, referenced = Map(domB -> domainB.defn))

      val fi = IdealinguaFamilyManager(root)

      fi.domains.keySet shouldBe Set(domA, domB)
      fi.importGraph(domA) shouldBe Set(domB)
      fi.importGraph(domB) shouldBe Set.empty
      fi.loadOrder shouldBe List(domB, domA)
      fi.diagnostics.isEmpty shouldBe true
    }

    it("negative — cyclic imports (A → B → A): CyclicDomainImport diagnostic emitted; loadOrder covers both") {
      // We implement DomainMeshResolved with a var-based forward reference so
      // both resolved meshes can point at each other (simulating A imports B
      // and B imports A).
      var resolvedBRef: DomainMeshResolved = null

      val resolvedA: DomainMeshResolved = new DomainMeshResolved {
        override val id: DomainId                                  = domA
        override val imports: Seq[Import]                          = Seq.empty
        override val members: Seq[RawTopLevelDefn]                 = Seq.empty
        override def referenced: Map[DomainId, DomainMeshResolved] = Map(domB -> resolvedBRef)
        override val origin: FSPath                                = FSPath.Name("a.domain")
        override val directInclusions: Seq[Inclusion]              = Seq.empty
        override val meta: RawNodeMeta                             = nodeMeta
      }

      resolvedBRef = new DomainMeshResolved {
        override val id: DomainId                                  = domB
        override val imports: Seq[Import]                          = Seq.empty
        override val members: Seq[RawTopLevelDefn]                 = Seq.empty
        override def referenced: Map[DomainId, DomainMeshResolved] = Map(domA -> resolvedA)
        override val origin: FSPath                                = FSPath.Name("b.domain")
        override val directInclusions: Seq[Inclusion]              = Seq.empty
        override val meta: RawNodeMeta                             = nodeMeta
      }

      val root = DomainMeshLoaded(
        id               = domA,
        origin           = FSPath.Name("a.domain"),
        directInclusions = Seq.empty,
        originalImports  = Seq.empty,
        meta             = nodeMeta,
        types            = Seq.empty,
        services         = Seq.empty,
        buzzers          = Seq.empty,
        streams          = Seq.empty,
        consts           = Seq.empty,
        imports          = Seq.empty,
        defn             = resolvedA,
      )

      val fi = IdealinguaFamilyManager(root)

      // Both domains must be discovered.
      fi.domains.keySet shouldBe Set(domA, domB)

      // At least one CyclicDomainImport diagnostic must be emitted.
      val cycleDiags = fi.diagnostics.issues.collect { case d: Diagnostic.CyclicDomainImport => d }
      cycleDiags should not be empty

      // loadOrder is complete and deterministic (both nodes present).
      fi.loadOrder.toSet shouldBe Set(domA, domB)
    }
  }
}

object IdealinguaFamilyManagerSpec {
  val domA: DomainId = DomainId(Seq("spec"), "a")
  val domB: DomainId = DomainId(Seq("spec"), "b")

  val nodeMeta: RawNodeMeta = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)

  /** Build a minimal `DomainMeshLoaded` with no types, pointing at `referenced`. */
  def emptyDomain(domId: DomainId, referenced: Map[DomainId, DomainMeshResolved]): DomainMeshLoaded = {
    val refMap = referenced
    val defnMesh = new DomainMeshResolved {
      override val id: DomainId                                  = domId
      override val imports: Seq[Import]                          = Seq.empty
      override val members: Seq[RawTopLevelDefn]                 = Seq.empty
      override val referenced: Map[DomainId, DomainMeshResolved] = refMap
      override val origin: FSPath                                = FSPath.Name(domId.id + ".domain")
      override val directInclusions: Seq[Inclusion]              = Seq.empty
      override val meta: RawNodeMeta                             = nodeMeta
    }
    DomainMeshLoaded(
      id               = domId,
      origin           = FSPath.Name(domId.id + ".domain"),
      directInclusions = Seq.empty,
      originalImports  = Seq.empty,
      meta             = nodeMeta,
      types            = Seq.empty,
      services         = Seq.empty,
      buzzers          = Seq.empty,
      streams          = Seq.empty,
      consts           = Seq.empty,
      imports          = Seq.empty,
      defn             = defnMesh,
    )
  }
}
