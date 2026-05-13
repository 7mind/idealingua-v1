package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.common.{Builtin, SigParam, SigParamSource, StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.Interfaces
import izumi.idealingua.model.typespace.structures.{ConverterDef, Struct}
import izumi.idealingua.translator.toscala.types.{ScalaStruct, ScalaType}

import scala.meta.Term

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
  * F-TextTree M8e: scaffolder ported off `scala.meta`. `decls`, `names`,
  * and `constructors` now return `List[String]` (pre-rendered Scala 3
  * source fragments). Consumers (composite + interface renderers, service
  * method product) splice the strings verbatim instead of feeding each
  * fragment through `DomainScalaParseBack.renderS30`. The constructor body
  * shapes (`def apply(...) = new T(...)` with the optional null-check
  * `assert((a) && (b))` over non-builtin source params) match the legacy
  * `q"..."`-quasiquote output byte-for-byte:
  *   - assertion: one operand → `assert(x)`; multi → `assert((a) && (b))`
  *     with operand parens (scalameta-printer parity, surfaced by
  *     `idltest/ast/TAppNode.scala:42`).
  *   - assignment ordering preserves the legacy fold:
  *     `assertions.tail.foldLeft(assertions.head)((a, acc) => acc && a)`
  *     puts the current element first — for fields `[typeinfo, appnode]`
  *     the assertion becomes `appnode && typeinfo`.
  *   - target / source field names are rendered through
  *     `dialect(Scala30)(Term.Name(_)).syntax` for Scala-3 keyword
  *     escape, matching the M8c cast-extension pattern.
  */
final class DomainCompositeStructure(
  ctx: DomainSTContext,
  val fields: ScalaStruct,
) {
  import izumi.idealingua.translator.toscala.types.ScalaField._

  val t: ScalaType = ctx.conv.toScala(fields.id)

  val composite: Interfaces = fields.fields.superclasses.interfaces

  /** Field declaration list (`name: Type` strings, Scala 3 keyword-safe). */
  val decls: List[String] = fields.all.toParams

  /** Bare field-name list (Scala 3 keyword-safe). */
  val names: List[String] = fields.all.toNames

  val constructors: List[String] = {
    val struct = fields.fields

    inlinedConstructors(struct)
      .map { cdef =>
        val constructorSignature = makeParams(cdef)
        val fullConstructorCode  = makeConstructor(cdef)
        (cdef, constructorSignature, fullConstructorCode)
      }
      .distinctBy(_._2.types)
      .map {
        case (_, sig, assignments) =>
          val params = sig.params.mkString(", ")
          val tFull  = t.typeFull.toString
          val ctor   = s"new $tFull(${assignments.mkString(", ")})"
          val assertionLine = sig.assertion
          val body   =
            if (assertionLine.isEmpty) s"  $ctor"
            else s"  $assertionLine\n  $ctor"
          s"def apply($params): $tFull = {\n$body\n}"
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
  // F-TextTree M8e: returns Strings instead of `scala.meta.Term.Param` /
  // `Term.ApplyInfix` trees.
  private def makeParams(t: ConverterDef): Params = {
    final case class Slot(field: SigParamSource, source: TypeId, nameSafe: String, tpe: String)
    val out: List[Slot] = t.outerParams.map { f =>
      val source = f.sourceType match {
        case s: DTOId => defnId(s)
        case o        => o
      }
      val scalaType = ctx.conv.toScala(source)
      val nameSafe  = DomainScalaParseBack.renderS30(Term.Name(f.sourceName))
      Slot(f, source, nameSafe, scalaType.typeFull.toString)
    }

    val assertions: List[String] = out.flatMap { slot =>
      val dealiased = dealias(slot.field.sourceType)
      if (!dealiased.isInstanceOf[Builtin]) {
        List(s"${slot.nameSafe}.asInstanceOf[_root_.scala.AnyRef] ne null")
      } else {
        List.empty
      }
    }

    Params(
      params     = out.map(s => s"${s.nameSafe}: ${s.tpe}"),
      types      = out.map(_.source),
      assertions = assertions,
    )
  }

  private def makeConstructor(t: ConverterDef): List[String] =
    t.allFields.map(toAssignment)

  private def toAssignment(f: SigParam): String = {
    val tgt = DomainScalaParseBack.renderS30(Term.Name(f.targetFieldName))
    val src = DomainScalaParseBack.renderS30(Term.Name(f.source.sourceName))
    f.sourceFieldName match {
      case Some(sourceFieldName) =>
        val sFn = DomainScalaParseBack.renderS30(Term.Name(sourceFieldName))
        s"$tgt = $src.$sFn"

      case None =>
        val sourceType = f.source.sourceType

        val mirror = sourceType match {
          case d: DTOId => defnId(d)
          case o        => o
        }

        if (mirror == sourceType) {
          s"$tgt = $src"
        } else {
          val ctor = ctx.conv.toScala(sourceType).termFull.toString
          s"$tgt = $ctor($src)"
        }
    }
  }

  private def dealias(tid: TypeId): TypeId = tid match {
    case a: izumi.idealingua.model.common.TypeId.AliasId =>
      ctx.domain.aliases.getOrElse(a, a)
    case other => other
  }
}

/** Constructor signature carrier.
  *
  * F-TextTree M8e: `params` and `assertions` are now String fragments
  * (Scala-3 rendered source text). `assertion` joins them per the legacy
  * `q"$acc && $a"` foldLeft shape — current element on the left, previous
  * accumulator on the right — so the printed text matches the legacy
  * scalameta-printer output byte-for-byte. Operand parens are added to
  * each `ne null` operand when there is more than one assertion, matching
  * the scalameta printer's disambiguation convention for
  * `Term.ApplyInfix(_, &&, _)` over `Term.ApplyInfix(_, ne, _)` (same
  * pattern as M8c's `DomainCastDownExpandExtension`).
  */
final case class Params(params: List[String], types: List[TypeId], assertions: List[String]) {
  /** Rendered assertion line (empty string when no assertions).
    *
    * F-TextTree M8e: mirrors the legacy
    * `assertions.tail.foldLeft(assertions.head)((a, acc) => q"$acc && $a")`
    * fold. Each operand is a `Term.ApplyInfix(_, ne, _)`; the scalameta
    * printer parenthesizes each `ne` operand (`(x ne null)`) under an
    * outer `&&`. For 3+ operands the legacy AST is right-grouped
    * (`x2 && (x1 && x0)`) because each fold step nests the accumulator
    * on the right side of `&&`; the printer parenthesizes the inner
    * `&&` because its default-parse would be left-associative.
    *
    * We replicate the right-grouped layout by walking the tail in order,
    * wrapping the running accumulator in parens for size > 1.
    */
  def assertion: String = {
    if (assertions.isEmpty) ""
    else if (assertions.size == 1) s"assert(${assertions.head})"
    else {
      // Walk the tail in order, tracking iteration count. The first
      // combination produces `(x1) && (x0)` — both operands are bare `ne`
      // terms, so the printer parenthesizes each one. Subsequent
      // combinations wrap the running accumulator in parens to surface
      // the right-grouping convention the scalameta printer applies.
      val (rendered, _) =
        assertions.tail.foldLeft((s"(${assertions.head})", 0)) {
          case ((acc, n), elem) =>
            val next = if (n == 0) s"($elem) && $acc" else s"($elem) && ($acc)"
            (next, n + 1)
        }
      s"assert($rendered)"
    }
  }
}
