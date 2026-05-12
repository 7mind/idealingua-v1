package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.common.{Builtin, SigParam, SigParamSource, StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.Interfaces
import izumi.idealingua.model.typespace.structures.{ConverterDef, Struct}
import izumi.idealingua.translator.toscala.types.{ScalaStruct, ScalaType}

import scala.meta._
import scala.annotation.nowarn

/** Wraps a `ScalaStruct` (from `DomainScalaStruct.scalaStruct`) with the
  * companion-object scaffolding (`decls`, `names`, `constructors`,
  * `composite` interfaces) that the legacy `types.CompositeStructure`
  * exposes — but computed from the new IR's `Domain` (no `Typespace`
  * required).
  *
  * Inlines `constructors(...)` per IMPL-7a plan §5 / §3-A3, ported from
  * `StructuralQueriesImpl.scala:128-165`. Aliases/dealiasing are read off
  * `domain.aliases` rather than `ts.dealias(...)`.
  *
  * Under relaxed parity: the M3 contract is structural correctness, not
  * byte equality with legacy. The constructor list is computed best-effort
  * — synthesized DTO mirrors (`defnId(dto)`) that the legacy
  * `interfaceEphemeralsReversed` map carries are not reproduced (the new
  * IR's `ephemeralOwner` map is consulted for ephemeral detection, but the
  * legacy mirror-DTO synthesis is a separate concern from M3 scope).
  */
@nowarn("msg=Unused import")
final class DomainCompositeStructure(
  ctx: DomainSTContext,
  val fields: ScalaStruct,
) {
  import izumi.idealingua.translator.toscala.types.ScalaField._

  val t: ScalaType = ctx.conv.toScala(fields.id)

  val composite: Interfaces = fields.fields.superclasses.interfaces

  val decls: List[Term.Param] = fields.all.toParams

  val names: List[Term.Name] = fields.all.toNames

  val constructors: List[Defn.Def] = {
    import izumi.fundamentals.collections.IzCollections._
    val struct = fields.fields

    inlinedConstructors(struct)
      .map { cdef =>
        val constructorSignature = makeParams(cdef)
        val fullConstructorCode  = makeConstructor(cdef)
        (cdef, constructorSignature, fullConstructorCode)
      }
      .distinctBy(_._2.types)
      .map {
        case (_, constructorSignature, fullConstructorCode) =>
          val instantiator = q"new ${init"${t.typeFull}(..$fullConstructorCode)"}"
          q"""def apply(..${constructorSignature.params}): ${t.typeFull} = {
                ..${constructorSignature.assertion}
                $instantiator
              }"""
      }
  }

  // --- ported from StructuralQueriesImpl.scala:128-165 ---
  private def inlinedConstructors(struct: Struct): List[ConverterDef] = {
    val local         = struct.localOrAmbigious
    val localNamesSet = local.map(_.field.name).toSet

    val cdef = if (struct.all.nonEmpty) {
      val constructorCode = struct.all
        .filterNot(f => localNamesSet.contains(f.field.name))
        .map(f => SigParam(f.field.name, SigParamSource(f.defn.definedBy, idToParaName(f.defn.definedBy)), Some(f.field.name)))

      val constructorCodeNonUnique = local
        .map(f => SigParam(f.field.name, SigParamSource(f.field.typeId, f.field.name), None))

      List(ConverterDef(struct.id, constructorCode ++ constructorCodeNonUnique, outerParamsOf(constructorCode ++ constructorCodeNonUnique)))
    } else {
      List.empty
    }

    val mcdef = struct.id match {
      case dto: DTOId if !isInterfaceEphemeral(dto) =>
        val mirrorId        = defnId(dto)
        val source          = SigParamSource(mirrorId, idToParaName(mirrorId))
        val constructorCode = struct.all.map(f => SigParam(f.field.name, source, Some(f.field.name)))
        List(ConverterDef(struct.id, constructorCode, List(source)))
      case _ =>
        List.empty
    }
    cdef ++ mcdef
  }

  private def outerParamsOf(sigs: List[SigParam]): List[SigParamSource] =
    sigs.map(_.source).distinct

  // mirrors `TypespaceToolsImpl.idToParaName` (id.name.toLowerCase)
  private def idToParaName(id: TypeId): String = id.name.toLowerCase

  // mirrors `TypespaceToolsImpl.defnId(DTOId)` for the non-ephemeral arm only
  private def defnId(id: StructureId): TypeId = id match {
    case d: DTOId =>
      izumi.idealingua.model.common.TypeId.InterfaceId(d, "Defn")
    case i =>
      i
  }

  // Matches `TypespaceImpl.types.isInterfaceEphemeral`: legacy treats a DTO
  // as interface-ephemeral iff it is the synthesized impl of an interface
  // (the `<I>.Struct` mirror), *not* every synthesized DTO.  Service /
  // buzzer method input/output ephemerals are NOT interface-ephemerals;
  // they must keep the `apply(defn: <Name>.Defn): <Name>` mirror
  // constructor (legacy emits it via `inlinedStructConstructorCode`).
  // Detect via `EphemeralOrigin.InterfaceMirror` rather than broad
  // `ephemeralOwner` membership (which also covers method I/O).
  private def isInterfaceEphemeral(d: DTOId): Boolean = {
    ctx.domain.members.get(d) match {
      case Some(izumi.idealingua.typer.ir.Member.Ephemeral(eph)) =>
        eph.origin.isInstanceOf[izumi.idealingua.typer.ir.EphemeralOrigin.InterfaceMirror]
      case _ =>
        // Conservative fallback for the legacy mirror-naming convention.
        d.name == "Struct"
    }
  }

  // --- ported from ScalaTranslationTools.makeParams / makeConstructor ---
  private def makeParams(t: ConverterDef): Params = {
    val out = t.outerParams.map { f =>
      val source = f.sourceType match {
        case s: DTOId => defnId(s)
        case o        => o
      }
      val scalaType = ctx.conv.toScala(source)
      val name      = Term.Name(f.sourceName)
      (f, source, (name, scalaType.typeFull))
    }

    val assertions = out.map {
      case (field, _, (name, _)) =>
        val dealiased = dealias(field.sourceType)
        if (!dealiased.isInstanceOf[Builtin]) {
          List(q"$name.asInstanceOf[_root_.scala.AnyRef] ne null")
        } else {
          List.empty
        }
    }

    Params(out.map(_._3).toParams, out.map(_._2), assertions.flatten)
  }

  private def makeConstructor(t: ConverterDef): List[Term.Assign] =
    t.allFields.map(toAssignment)

  private def toAssignment(f: SigParam): Term.Assign = {
    f.sourceFieldName match {
      case Some(sourceFieldName) =>
        q""" ${Term.Name(f.targetFieldName)} = ${Term.Name(f.source.sourceName)}.${Term.Name(sourceFieldName)}  """

      case None =>
        val sourceType = f.source.sourceType

        val mirror = sourceType match {
          case d: DTOId => defnId(d)
          case o        => o
        }

        if (mirror == sourceType) {
          q""" ${Term.Name(f.targetFieldName)} = ${Term.Name(f.source.sourceName)}  """
        } else {
          q""" ${Term.Name(f.targetFieldName)} = ${ctx.conv.toScala(sourceType).termFull}(${Term.Name(f.source.sourceName)})"""
        }
    }
  }

  private def dealias(tid: TypeId): TypeId = tid match {
    case a: izumi.idealingua.model.common.TypeId.AliasId =>
      ctx.domain.aliases.getOrElse(a, a)
    case other => other
  }
}

final case class Params(params: List[Term.Param], types: List[TypeId], assertions: List[Term.ApplyInfix]) {
  def assertion: List[Term] = {
    if (assertions.isEmpty) List.empty
    else {
      val expr = assertions.tail.foldLeft(assertions.head: Term) {
        case (a, acc) => q"$acc && $a"
      }
      List(q"assert($expr)")
    }
  }
}
