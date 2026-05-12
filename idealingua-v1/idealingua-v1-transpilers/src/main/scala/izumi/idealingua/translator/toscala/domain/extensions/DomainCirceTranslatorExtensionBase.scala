package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.Generic.TMap
import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.common.{Builtin, TypeId}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.runtime.circe.IRTTimeInstances
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.types.runtime
import izumi.idealingua.typer.ir.{Member, TypeDef => NewTypeDef}

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

  /** Defns for an interface-impl mirror DTO (`<Iface>.Struct`) — derived
    * codec.  Emitted INSIDE the interface companion (legacy parity:
    * `CirceTranslatorExtensionBase.handleComposite` ran for every impl DTO
    * synthesized by `CompositeRenderer.defns(_, CsInterface)`).
    *
    * Operates off the synthetic `implFlat` because impl IDs are not
    * first-class user declarations and are absent from
    * `Domain.flattenedStructs`. AnyVal eligibility on Scala 3 is computed
    * from the synthetic flat directly.
    */
  def emitForImplStruct(
    ctx: DomainSTContext,
    implId: izumi.idealingua.model.common.TypeId.DTOId,
    implFlat: izumi.idealingua.typer.ir.FlatStruct,
    scalaVersions: List[String],
  ): CirceTrait =
    withDerivedStructCore(
      ctx           = ctx,
      id            = implId,
      flatFields    = implFlat.fields,
      scalaVersions = scalaVersions,
    )

  /** Defns for a service-method Input / Output ephemeral DTO. Mirrors
    * legacy `CirceTranslatorExtensionBase.withDerivedClass` arm for
    * `ClassSource.CsMethodInput` / `CsMethodOutput`. The `unwrap` flag
    * is set when the source method has `DefMethod.Output.Singular(_)` —
    * the legacy unwrap branch emits `encodeUnwrapped<Name>` /
    * `decodeUnwrapped<Name>` codecs that pass the single inner field
    * through transparently (so the wire-format does not include the
    * synthetic wrapper).
    */
  def emitForMethodStruct(
    ctx: DomainSTContext,
    dtoId: izumi.idealingua.model.common.TypeId.DTOId,
    flat: izumi.idealingua.typer.ir.FlatStruct,
    unwrap: Boolean,
    scalaVersions: List[String],
  ): CirceTrait = {
    if (unwrap && flat.fields.sizeIs == 1) {
      val stype       = ctx.conv.toScala(dtoId)
      val name        = stype.fullJavaType.name
      val tpe         = stype.typeName
      val singleField = flat.fields.head.field
      val ftpe        = ctx.conv.toScala(singleField.typeId)
      val base        = Init(circeRuntimePkg.conv.toScala[IRTTimeInstances].typeAbsolute, Name.Anonymous(), Seq.empty)

      val encoder =
        if (isObjectEncoder(ctx, singleField.typeId)) {
          q"""
             implicit val ${Pat.Var(Term.Name(s"encodeUnwrapped$name"))}: Encoder.AsObject[$tpe] = Encoder.AsObject.instance {
               v => v.${Term.Name(singleField.name)}.asJsonObject
             }
           """
        } else {
          q"""
             implicit val ${Pat.Var(Term.Name(s"encodeUnwrapped$name"))}: Encoder[$tpe] = Encoder.instance {
               v => v.${Term.Name(singleField.name)}.asJson
             }
           """
        }
      CirceTrait(
        s"${name}Circe",
        q"""trait ${Type.Name(s"${name}Circe")} extends $base {
              import _root_.io.circe._
              import _root_.io.circe.syntax._

              $encoder;

              implicit val ${Pat.Var(Term.Name(s"decodeUnwrapped$name"))}: Decoder[$tpe] = Decoder.instance {
                v => v.as[${ftpe.typeFull}].map(d => ${stype.termName}(d))
              }
            }
        """,
      )
    } else {
      withDerivedStructCore(ctx, dtoId, flat.fields, scalaVersions)
    }
  }

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
    * **Implementor semantics (IMPL-7a.2-Fc, defect #4)**: legacy
    * `ts.inheritance.implementingDtos(id)` returns *every* DTO registered in
    * `ts.types.index` whose `parentsInherited` (interface-chain only, NOT
    * concept-chain) contains `id`. The legacy index ALSO carries the
    * synthesized mirror DTO `DTOId(id, "Struct")` so that mirror always
    * appears in the implementor list — yielding the legacy
    * `case v: T.Struct => ...` arm. The new-IR `Domain.implementingDtos`
    * (a) collapses interface- and concept-channel parents and (b) excludes
    * ephemerals (mirrors) from the user-types-only index. We replicate
    * legacy semantics here locally:
    *
    *   - For every user DTO whose `struct.superclasses.interfaces`
    *     transitively contains `i.id` → include as implementor.
    *   - Include the mirror `DTOId(i.id, "Struct")` if it exists as
    *     `Member.Ephemeral` in `ctx.domain.members` (it always does for
    *     user-declared interfaces — `EphemeralSynthesizer` Phase 7
    *     guarantees this).
    *
    * **Determinism note**: result is sorted by `_.toString` so the emitted
    * case order is byte-stable across runs.
    */
  def emitForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): CirceTrait = {
    import ctx.conv.*
    val t            = toScala(i.id)
    val tpe          = t.typeFull

    val interfaceInheritedDtos: Set[DTOId] = {
      // Walk every user DTO and check whether `i.id` is reachable via the
      // interface-inheritance closure only (mirroring legacy `parentsInherited`).
      val buf = scala.collection.mutable.LinkedHashSet.empty[DTOId]
      ctx.domain.userTypes.values.foreach {
        case dto: NewTypeDef.Dto =>
          if (interfaceClosureContains(ctx, dto.id, i.id)) {
            val _ = buf.add(dto.id)
          }
        case _ => ()
      }
      buf.toSet
    }

    val mirrorImplementor: Option[DTOId] = {
      val mirrorId = DTOId(i.id, "Struct")
      ctx.domain.members.get(mirrorId) match {
        case Some(_: Member.Ephemeral) => Some(mirrorId)
        case _                         => None
      }
    }

    val implementors = (interfaceInheritedDtos ++ mirrorImplementor).toList.sortBy(_.toString)

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
    val flat = ctx.domain.flattenedStructs.get(dto.id).map(_.fields).getOrElse(List.empty)
    withDerivedStructCore(ctx, dto.id, flat, scalaVersions)
  }

  /** Common machinery for `withDerivedClass` and `emitForImplStruct`. Builds
    * the `XCirce` trait carrying derived (or AnyVal-forProduct1) Encoder
    * and Decoder for `id`. `flatFields` is the resolved field list.
    */
  protected def withDerivedStructCore(
    ctx: DomainSTContext,
    id: izumi.idealingua.model.common.StructureId,
    flatFields: List[izumi.idealingua.typer.ir.FlatField],
    scalaVersions: List[String],
  ): CirceTrait = {
    val stype = ctx.conv.toScala(id)
    val name  = stype.fullJavaType.name
    val tpe   = stype.typeName

    val base = Init(circeRuntimePkg.conv.toScala[IRTTimeInstances].typeAbsolute, Name.Anonymous(), Seq.empty)

    // Scala 3 AnyVal fallback (mirrors legacy fix): if the struct is exactly
    // one scalar field qualifying for AnyVal AND we're targeting Scala 3,
    // emit a manual forProduct1 codec — circe's deriver does not handle
    // AnyVal on Scala 3.
    val isScala3 = scalaVersions.exists(_.startsWith("3"))
    val anyvalCase: Boolean = {
      flatFields.size == 1 && flatFields.forall(ff => isAnyValField(ctx, ff.field.typeId))
    }

    if (anyvalCase && isScala3) {
      val singleField = flatFields.head.field
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

  /** Mirrors `DomainAnyvalExtension.canBeAnyValField` — duplicated locally
    * because that helper is `private` and we need to gate the impl-DTO
    * AnyVal path here too.
    */
  private def isAnyValField(ctx: DomainSTContext, typeId: TypeId): Boolean = typeId match {
    case _: izumi.idealingua.model.common.Generic       => false
    case _: izumi.idealingua.model.common.Builtin       => true
    case _: TypeId.EnumId                                => true
    case _: TypeId.AdtId                                 => false
    case a: TypeId.AliasId                               =>
      ctx.domain.aliases.get(a) match {
        case Some(target) => isAnyValField(ctx, target)
        case None         => throw new IDLException(s"unresolved alias $a")
      }
    case d: TypeId.DTOId =>
      ctx.domain.flattenedStructs.get(d).exists(_.fields.size > 1)
    case i: TypeId.InterfaceId =>
      ctx.domain.flattenedStructs.get(i).exists(_.fields.size > 1)
    case t: TypeId.IdentifierId =>
      ctx.domain.userTypes.get(t) match {
        case Some(NewTypeDef.Identifier(_, fields, _)) => fields.size > 1
        case _                                          => false
      }
    case _ => false
  }

  /** Returns true iff `target` is reachable from `from` by walking only the
    * interface-channel parent links (`struct.superclasses.interfaces`),
    * stopping when a non-interface link is encountered. Mirrors legacy
    * `parentsInherited` (`InheritanceQueriesImpl.scala:21-23,36-70`) which
    * excludes concepts/mixins.
    *
    * Used by `emitForInterface` (defect #4) to compute the implementor list
    * with legacy semantics.
    */
  private def interfaceClosureContains(ctx: DomainSTContext, from: TypeId, target: TypeId): Boolean = {
    val visited = scala.collection.mutable.Set.empty[TypeId]
    val queue   = scala.collection.mutable.Queue.empty[TypeId]
    queue.enqueue(from)
    while (queue.nonEmpty) {
      val cur = queue.dequeue()
      if (visited.add(cur)) {
        ctx.domain.userTypes.get(cur) match {
          case Some(dto: NewTypeDef.Dto) =>
            dto.struct.superclasses.interfaces.foreach { iid =>
              if (iid == target) return true
              queue.enqueue(iid)
            }
          case Some(ifc: NewTypeDef.Interface) =>
            ifc.struct.superclasses.interfaces.foreach { iid =>
              if (iid == target) return true
              queue.enqueue(iid)
            }
          case _ => ()
        }
      }
    }
    false
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
