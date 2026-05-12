package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.il.ast.typed.{NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.InterfaceProduct
import izumi.idealingua.translator.tocsharp.types.{CSharpClass, CSharpField}
import izumi.idealingua.typer.ir.{FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Interface` as the same pre-extension
  * `InterfaceProduct` the legacy `CSharpTranslator.renderInterface`
  * produces (modulo the extension chain).
  *
  * IMPL-7c Phase B M2: byte-parity port. Two emitted top-level shapes:
  *   - the interface declaration (`public interface <Name> : ... { ... }`),
  *   - the companion implementing class (`<Name><Name>Struct`).
  *
  * Flattened struct comes from `Domain.flattenedStructs(i.id)`; the
  * `Super` declaration comes from `i.struct.superclasses`. The
  * `<Iface>Struct` impl id is synthesized via `DomainCSStruct.implId(i.id)`
  * matching legacy `typespace.tools.implId(i.id)`.
  *
  * `parentsInherited(i.id)` and the `validFields` filter use the legacy
  * `Typespace` threaded per-call — `Domain.parents` exists but at M2 we
  * stay on legacy threading to keep the port focused on renderer-shape
  * parity. M5 production-swap will replace this with a `Domain`-backed
  * accessor.
  *
  * Extension chain (`ext.preModelEmit` / `ext.postModelEmit` /
  * `ext.imports`) is omitted: the default C# extension set
  * (`JsonNetExtension`) has no `Interface` or `DTO` overrides relevant
  * to this renderer, so the pre-extension product is byte-equal to the
  * post-extension product for the default extension list.
  */
final class DomainCSInterfaceRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderInterface(i: NewTypeDef.Interface, ts: Typespace, im: CSharpImports): InterfaceProduct = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val eid       = DomainCSStruct.implId(i.id)

    // Parents come from the legacy inheritance walk for byte-stable
    // ordering at M2 — the legacy emission depends on the exact iteration
    // order of `parentsInherited`. `Domain.parents` has the same
    // membership but is a `Set`; M5+ may swap once ordering is verified.
    val parentIfaces = ts.inheritance.parentsInherited(i.id).filter(_ != i.id)
    val validFields  = structure.all.filterNot(f => parentIfaces.contains(f.defn.definedBy))
    val ifaceFields =
      validFields.map(f => (f.defn.variance.nonEmpty, CSharpField(f.field, eid.name, Seq.empty)))

    val struct = CSharpClass(eid, i.id.name + eid.name, structure, List(i.id))
    val ifaceImplements =
      if (i.struct.superclasses.interfaces.isEmpty) ": IRTTI"
      else
        ": " +
        i.struct.superclasses.interfaces.map(ifc => ifc.name).mkString(", ") + ", IRTTI"

    // The synthetic DTO is only passed to `ext.preModelEmit`/`postModelEmit`
    // in the legacy renderer; with an empty extension list we still
    // construct it for parity (no observable effect on the output).
    val _dto: LegacyTypeDef.DTO = LegacyTypeDef.DTO(
      eid,
      Structure(validFields.map(f => f.field), List.empty, Super(List(i.id), List.empty, List.empty)),
      NodeMeta.empty,
    )
    val _ = _dto

    val iface =
      s"""${im.renderUsings()}
         |
         |public interface ${i.id.name}$ifaceImplements {
         |${ifaceFields
          .map(f => s"${if (f._1) "// Would have been covariance, but C# doesn't support it:\n// " else ""}${f._2.renderMember(true)}").mkString("\n").shift(4)}
         |}
         |
         |       """.stripMargin

    val companion =
      s"""
         |${struct.renderHeader()} {
         |${struct.render(withWrapper = false, withSlices = true, withRTTI = true, withCTORs = Some(i.id.name)).shift(4)}
         |}
         |
         |       """.stripMargin

    InterfaceProduct(
      iface,
      companion,
      im.renderImports(List("IRT", "System", "System.Collections", "System.Collections.Generic", "System.Reflection")),
    )
  }
}
