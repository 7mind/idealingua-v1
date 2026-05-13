package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.il.ast.typed.{NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.InterfaceProduct
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Interface` as the same pre-extension
  * `InterfaceProduct` the legacy `CSharpTranslator.renderInterface`
  * produces (modulo the extension chain).
  *
  * F-TextTree M3 — ported to the typed-renderer protocol. Two emitted
  * top-level shapes: the interface declaration and the companion
  * implementing class (`<Name><Name>Struct`). The envelope is composed
  * as `TextTree[CSRefHandle]`; the interface inheritance list is
  * pre-rendered (since legacy emits `<IfaceName>` plain — not a fully
  * qualified path — through `iface.name`, not through the converter).
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
    val resolver                    = new DomainCSTypeResolver()

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val eid       = DomainCSStruct.implId(i.id)

    val parentIfaces: Set[izumi.idealingua.model.common.TypeId] =
      strictInterfaceClosure(i.id).toSet[izumi.idealingua.model.common.TypeId]
    val validFields = structure.all.filterNot(f => parentIfaces.contains(f.defn.definedBy))
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

    val ifaceFieldsRendered = ifaceFields
      .map(f => s"${if (f._1) "// Would have been covariance, but C# doesn't support it:\n// " else ""}${f._2.renderMember(true)}").mkString("\n")

    val ifaceTree: TextTree[CSRefHandle] =
      q"""${im.renderUsings()}
         |$ifacePreSplice
         |public interface ${i.id.name}$ifaceImplements {
         |${ifaceFieldsRendered.shift(4)}
         |}
         |$ifacePostSplice
         |       """.stripMargin

    val companionTree: TextTree[CSRefHandle] =
      q"""$companionPreSplice
         |${struct.renderHeader()} {
         |${struct.render(withWrapper = false, withSlices = true, withRTTI = true, withCTORs = Some(i.id.name)).shift(4)}
         |}
         |$companionPostSplice
         |       """.stripMargin

    InterfaceProduct(
      ifaceTree.mapRender(resolver.resolve),
      companionTree.mapRender(resolver.resolve),
      im.renderImports(List("IRT", "System", "System.Collections", "System.Collections.Generic", "System.Reflection") ++ extraImports),
    )
  }

  /** Strict-interface-edge transitive closure. */
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
