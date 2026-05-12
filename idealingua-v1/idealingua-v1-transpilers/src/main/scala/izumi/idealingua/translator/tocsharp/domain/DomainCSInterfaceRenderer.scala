package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.il.ast.typed.{NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.InterfaceProduct
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Interface` as the same pre-extension
  * `InterfaceProduct` the legacy `CSharpTranslator.renderInterface`
  * produces (modulo the extension chain).
  *
  * IMPL-10-prep-Cs1: byte-parity port now consumes `DomainCSClass` /
  * `DomainCSField` (Domain-backed). `Typespace` no longer threaded; the
  * legacy `ts.inheritance.parentsInherited(i.id).filter(_ != i.id)` lookup
  * collapses to `domain.parents.getOrElse(i.id, Set.empty)` (which is by
  * contract the set of transitively-inherited interfaces, excluding self).
  *
  * Two emitted top-level shapes: the interface declaration and the
  * companion implementing class (`<Name><Name>Struct`).
  *
  * Extension chain is omitted as in the legacy default.
  */
final class DomainCSInterfaceRenderer(ctx: DomainCSContext) {

  def renderInterface(i: NewTypeDef.Interface, im: CSharpImports): InterfaceProduct =
    renderInterface(
      i, im,
      ifacePreSplice      = "",
      ifacePostSplice     = "",
      companionPreSplice  = "",
      companionPostSplice = "",
      extraImports        = List.empty,
    )

  def renderInterface(
    i: NewTypeDef.Interface,
    im: CSharpImports,
    ifacePreSplice: String,
    ifacePostSplice: String,
    companionPreSplice: String,
    companionPostSplice: String,
    extraImports: List[String],
  ): InterfaceProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val eid       = DomainCSStruct.implId(i.id)

    // Legacy `ts.inheritance.parentsInherited(i.id).filter(_ != i.id)` —
    // walks ONLY the `interfaces` edge (not `+concept` mixins). The
    // `domain.parents(i.id)` set is broader: it includes concept-derived
    // interface mixins too. Recompute the strict-interface-edge closure
    // here so `+IntPair`-mixed fields stay surfaced on the iface (legacy
    // `InheritanceQueriesImpl.safeParentsInherited`).
    val parentIfaces: Set[izumi.idealingua.model.common.TypeId] =
      strictInterfaceClosure(i.id).toSet[izumi.idealingua.model.common.TypeId]
    val validFields  = structure.all.filterNot(f => parentIfaces.contains(f.defn.definedBy))
    val ifaceFields =
      validFields.map(f => (f.defn.variance.nonEmpty, DomainCSField(f.field, eid.name, Seq.empty)))

    val struct = DomainCSClass(eid, i.id.name + eid.name, structure, List(i.id))
    val ifaceImplements =
      if (i.struct.superclasses.interfaces.isEmpty) ": IRTTI"
      else
        ": " +
        i.struct.superclasses.interfaces.map(ifc => ifc.name).mkString(", ") + ", IRTTI"

    val _dto: LegacyTypeDef.DTO = LegacyTypeDef.DTO(
      eid,
      Structure(validFields.map(f => f.field), List.empty, Super(List(i.id), List.empty, List.empty)),
      NodeMeta.empty,
    )
    val _ = _dto

    val iface =
      s"""${im.renderUsings()}
         |$ifacePreSplice
         |public interface ${i.id.name}$ifaceImplements {
         |${ifaceFields
          .map(f => s"${if (f._1) "// Would have been covariance, but C# doesn't support it:\n// " else ""}${f._2.renderMember(true)}").mkString("\n").shift(4)}
         |}
         |$ifacePostSplice
         |       """.stripMargin

    val companion =
      s"""$companionPreSplice
         |${struct.renderHeader()} {
         |${struct.render(withWrapper = false, withSlices = true, withRTTI = true, withCTORs = Some(i.id.name)).shift(4)}
         |}
         |$companionPostSplice
         |       """.stripMargin

    InterfaceProduct(
      iface,
      companion,
      im.renderImports(List("IRT", "System", "System.Collections", "System.Collections.Generic", "System.Reflection") ++ extraImports),
    )
  }

  /** Strict-interface-edge transitive closure (mirrors legacy
    * `InheritanceQueriesImpl.safeParentsInherited`). Walks only
    * `struct.superclasses.interfaces`, not concept mixins. Returns the
    * set of transitive parent interfaces *excluding* `start` itself.
    */
  private def strictInterfaceClosure(start: InterfaceId): Set[InterfaceId] = {
    val acc     = scala.collection.mutable.LinkedHashSet.empty[InterfaceId]
    val visited = scala.collection.mutable.LinkedHashSet.empty[InterfaceId]
    def walk(cur: InterfaceId): Unit = {
      if (visited.add(cur)) {
        ctx.domain.userTypes.get(cur) match {
          case Some(ifc: NewTypeDef.Interface) =>
            ifc.struct.superclasses.interfaces.foreach { p =>
              val _ = acc.add(p)
              walk(p)
            }
          case _ => ()
        }
      }
    }
    walk(start)
    acc.toSet
  }
}
