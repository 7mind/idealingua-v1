package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.Generic.TMap
import izumi.idealingua.model.common.{Builtin, TypeId}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.runtime.circe.IRTTimeInstances
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.types.runtime
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.annotation.tailrec
import scala.meta.*

/** PR-02 IMPL-7a.2 Phase B M5: WIRE-FORMAT-CRITICAL new-IR port of
  * `CirceTranslatorExtensionBase`.
  *
  * Emits `Encoder`/`Decoder` boilerplate traits for Identifier, DTO, Adt,
  * Enum, and Interface types. Mirrors the legacy extension exactly at the
  * Defn-shape level so the produced wire format is byte-identical.
  *
  * Two relaxations vs. legacy (master plan §2 line 96 / IMPL-7a plan §3-A5):
  *
  *   - Legacy interface arm reads
  *     `ctx.typespace.inheritance.implementingDtos(interface.id)`. The new IR
  *     pre-materialises this in `Domain.implementingDtos`. **The set is sorted
  *     by `_.toString` here to make the emitted encoder/decoder case order
  *     deterministic** — without this the JSON case-ordering would depend on
  *     `Set`'s iteration order, breaking byte-stable wire-format output.
  *
  *   - Legacy `withDerivedClass` consumes a `StructContext` (struct + class
  *     source). The new-IR port replaces this with `ctx.domain.flattenedStructs(id)`
  *     for the field list. The `unwrap` heuristic (method-output singular →
  *     transparent codec) is dropped at M5: the production codec for top-level
  *     DTOs only needs the derived codec arm. Method-output unwrapping lands
  *     when service-codec wiring is restored at M6.
  *
  * Output shape per type:
  *   - Identifier / Enum: `String`-based codec via `withParseable` (same
  *     legacy shape).
  *   - DTO: derived `Encoder.AsObject` + `Decoder` via
  *     `deriveEncoder`/`deriveDecoder`. Scala 3 vs 2.13 deriver imports come
  *     from the `classDeriverImports` hook (mirroring legacy
  *     `CirceDerivationTranslatorExtension`).
  *   - Adt / Interface: tagged-union codec — encodes as
  *     `{ "<wireId>": <inner> }` and decodes by dispatching on the first key.
  *     Implementor case order is deterministic (sorted).
  */
trait DomainCirceTranslatorExtensionBase {

  protected case class CirceTrait(name: String, defn: Defn.Trait)

  /** Scala-version-specific deriver imports — Scala 3 uses
    * `io.circe.generic.semiauto`, Scala 2.13 uses `io.circe.derivation`.
    */
  protected def classDeriverImports(scalaVersions: List[String]): List[Import]

  private val circeRuntimePkg = runtime.Pkg.of[IRTTimeInstances]

  /** Defns to splice into the Identifier companion + as siblings. */
  def emitForIdentifier(ctx: DomainSTContext, id: NewTypeDef.Identifier): CirceTrait =
    withParseable(ctx, id.id)

  /** Defns for an Enum. */
  def emitForEnum(ctx: DomainSTContext, e: NewTypeDef.Enum): CirceTrait =
    withParseable(ctx, e.id)

  /** Defns for a DTO — derived codec. */
  def emitForDto(ctx: DomainSTContext, dto: NewTypeDef.Dto, scalaVersions: List[String]): CirceTrait =
    withDerivedClass(ctx, dto, scalaVersions)

  /** Defns for an ADT — tagged-union codec. */
  def emitForAdt(ctx: DomainSTContext, adt: NewTypeDef.Adt): CirceTrait = {
    import ctx.conv.*
    val id  = adt.id
    val t   = toScala(id)
    val tpe = t.typeFull

    // Deterministic order: alternatives carry source order on `TypeDef.Adt.alternatives`
    // already (List, not Set — see Domain.scala "Field-ordering invariant").
    val implementors = adt.alternatives

    val enc = implementors.map { c =>
      p"""case v: ${t.within(c.typename).typeFull} => Map(${Lit.String(c.wireId)} -> v.value).asJsonObject"""
    }

    val dec = implementors.map { c =>
      p"""case ${Lit.String(c.wireId)} => value.as[${toScala(c.typeId).typeAbsolute}].map(${t.within(c.typename).termFull}.apply)"""
    }

    val missingDefinitionCase =
      p"""case _ =>
           val cname = ${Lit.String(id.wireId)}
           val alts = List(..${implementors.map(c => Lit.String(c.wireId))}).mkString(",")
           Left(DecodingFailure(s"Can't decode type $$fname as $$cname, expected one of [$$alts]", value.history))
      """

    val decCases = dec :+ missingDefinitionCase

    CirceTrait(
      s"${id.name}Circe",
      q"""trait ${Type.Name(s"${id.name}Circe")} {
             import _root_.io.circe.syntax._
             import _root_.io.circe.{Encoder, Decoder, DecodingFailure}

             implicit val ${Pat.Var(Term.Name(s"encode${id.name}"))}: Encoder.AsObject[$tpe] = Encoder.AsObject.instance {
                 ..case $enc
             }

             implicit val ${Pat.Var(Term.Name(s"decode${id.name}"))}: Decoder[$tpe] = Decoder.instance(c => {
                 val maybeContent = c.keys.flatMap(_.headOption)
                      .toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))

                 for {
                   fname <- maybeContent
                   value = c.downField(fname)
                   result <- fname match { ..case $decCases }
                 } yield {
                   result
                 }
               }
             )
          }
      """,
    )
  }

  /** Defns for an Interface — tagged-union codec keyed by implementing DTOs.
    *
    * **Determinism note**: `Domain.implementingDtos(id)` is a `Set` — we sort
    * by `_.toString` so the emitted case order is byte-stable across runs.
    * Without this sort the wire format would non-deterministically pick one
    * iteration order, breaking goldens / cross-language interop.
    */
  def emitForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): CirceTrait = {
    import ctx.conv.*
    val t            = toScala(i.id)
    val tpe          = t.typeFull
    val implementors = ctx.domain.implementingDtos.getOrElse(i.id, Set.empty).toList.sortBy(_.toString)

    val enc = implementors.map { c =>
      p"""case v: ${toScala(c).typeFull} => Map(${Lit.String(c.wireId)} -> v).asJsonObject"""
    }

    val dec = implementors.map { c =>
      p"""case ${Lit.String(c.wireId)} => value.as[${toScala(c).typeFull}]"""
    }

    val missingDefinitionCase =
      p"""case _ =>
           val cname = ${Lit.String(i.id.wireId)}
           val alts = List(..${implementors.map(c => Lit.String(c.wireId))}).mkString(",")
           Left(DecodingFailure(s"Can't decode type $$fname as $$cname, expected one of [$$alts]", value.history))
      """

    val decCases = dec :+ missingDefinitionCase

    CirceTrait(
      s"${i.id.name}Circe",
      q"""trait ${Type.Name(s"${i.id.name}Circe")} {
             import _root_.io.circe.syntax._
             import _root_.io.circe.{Encoder, Decoder, DecodingFailure}

             implicit val ${Pat.Var(Term.Name(s"encode${i.id.name}"))}: Encoder.AsObject[$tpe] = Encoder.AsObject.instance {
               ..case $enc
             }

             implicit val ${Pat.Var(Term.Name(s"decode${i.id.name}"))}: Decoder[$tpe] = Decoder.instance(c => {
                 val maybeContent = c.keys.flatMap(_.headOption)
                      .toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))

                 for {
                   fname <- maybeContent
                   value = c.downField(fname)
                   result <- fname match { ..case $decCases }
                 } yield result
               }
             )
          }
      """,
    )
  }

  protected def withParseable(ctx: DomainSTContext, id: TypeId): CirceTrait = {
    val t   = ctx.conv.toScala(id)
    val tpe = t.typeFull
    CirceTrait(
      s"${id.name}Circe",
      q"""trait ${Type.Name(s"${id.name}Circe")} {
            import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
            import scala.util._
            implicit val ${Pat.Var(Term.Name(s"encode${id.name}"))}: Encoder[$tpe] = Encoder.encodeString.contramap(_.toString)
            implicit val ${Pat.Var(Term.Name(s"decode${id.name}"))}: Decoder[$tpe] = Decoder.decodeString.emapTry(v => Try(${t.termFull}.parse(v)))
            implicit val ${Pat.Var(Term.Name(s"encodeKey${id.name}"))}: KeyEncoder[$tpe] = KeyEncoder.encodeKeyString.contramap(_.toString)
            implicit val ${Pat.Var(Term.Name(s"decodeKey${id.name}"))}: KeyDecoder[$tpe] = new KeyDecoder[$tpe] {
              final def apply(key: String): Option[$tpe] = Try(${t.termFull}.parse(key)).toOption
            }
          }
      """,
    )
  }

  protected def withDerivedClass(ctx: DomainSTContext, dto: NewTypeDef.Dto, scalaVersions: List[String]): CirceTrait = {
    val id    = dto.id
    val stype = ctx.conv.toScala(id)
    val name  = stype.fullJavaType.name
    val tpe   = stype.typeName

    val base = Init(circeRuntimePkg.conv.toScala[IRTTimeInstances].typeAbsolute, Name.Anonymous(), Seq.empty)

    // Field list comes off the flat struct.
    val flat = ctx.domain.flattenedStructs.get(id)

    // Scala 3 AnyVal fallback (mirrors legacy fix): if the struct is exactly
    // one scalar field qualifying for AnyVal AND we're targeting Scala 3,
    // emit a manual forProduct1 codec — circe's deriver does not handle
    // AnyVal on Scala 3.
    val isScala3 = scalaVersions.exists(_.startsWith("3"))
    val anyvalCase = DomainAnyvalExtension.structCanBeAnyVal(ctx, dto)

    if (anyvalCase && isScala3 && flat.isDefined && flat.get.fields.size == 1) {
      val singleField = flat.get.fields.head.field
      val ftpe = ctx.conv.toScala(singleField.typeId).typeFull
      CirceTrait(
        s"${name}Circe",
        q"""trait ${Type.Name(s"${name}Circe")} extends $base {
              import _root_.io.circe.{Encoder, Decoder}

              implicit val ${Pat.Var(Term.Name(s"encode$name"))}: Encoder.AsObject[$tpe] = Encoder.forProduct1[$tpe, $ftpe](${Lit.String(singleField.name)})((v: $tpe) => v.${Term.Name(singleField.name)})
              implicit val ${Pat.Var(Term.Name(s"decode$name"))}: Decoder[$tpe] = Decoder.forProduct1[$tpe, $ftpe](${Lit.String(singleField.name)})((d: $ftpe) => new ${stype.typeName}(d))
            }
        """,
      )
    } else {
      CirceTrait(
        s"${name}Circe",
        q"""trait ${Type.Name(s"${name}Circe")} extends $base {
            ..${classDeriverImports(scalaVersions)}
            import _root_.io.circe.{Encoder, Decoder}

            implicit val ${Pat.Var(Term.Name(s"encode$name"))}: Encoder.AsObject[$tpe] = deriveEncoder[$tpe]
            implicit val ${Pat.Var(Term.Name(s"decode$name"))}: Decoder[$tpe] = deriveDecoder[$tpe]
          }
      """,
      )
    }
  }

  /** Walks alias targets to determine whether an encoder is an `Encoder.AsObject`
    * (legacy `isObjectEncoder`) — kept for reuse if M6 reinstates the unwrap
    * arm; not exercised at M5.
    */
  @tailrec
  protected final def isObjectEncoder(ctx: DomainSTContext, tpe: TypeId): Boolean = {
    tpe match {
      case _: TMap => true
      case a: TypeId.AliasId =>
        ctx.domain.aliases.get(a) match {
          case Some(target) => isObjectEncoder(ctx, target)
          case None         => throw new IDLException(s"DomainCirceTranslatorExtensionBase: unresolved alias $a")
        }
      case _: Builtin => false
      case _          => true
    }
  }
}

/** Concrete derivation-import-wiring subclass — mirrors legacy
  * `CirceDerivationTranslatorExtension`. Scala 2.13 uses
  * `io.circe.derivation`; Scala 3 uses `io.circe.generic.semiauto`.
  */
object DomainCirceDerivationTranslatorExtension extends DomainCirceTranslatorExtensionBase {
  override protected def classDeriverImports(scalaVersions: List[String]): List[Import] = {
    if (scalaVersions.exists(_.startsWith("3"))) List(scala3Import)
    else List(scala2Import)
  }

  private lazy val scala2Import = q""" import _root_.io.circe.derivation.{deriveDecoder, deriveEncoder} """
  private lazy val scala3Import = q""" import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder} """
}
