package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.Generic.TMap
import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.common.{Builtin, TypeId}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.runtime.circe.IRTTimeInstances
import izumi.idealingua.translator.toscala.domain.{DomainSTContext, DomainScalaParseBack}
import izumi.idealingua.translator.toscala.types.runtime
import izumi.idealingua.typer.ir.{Member, TypeDef => NewTypeDef}

import scala.annotation.tailrec
import scala.meta.{Term, Type}

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
  *
  * F-TextTree M8d: ported off `scala.meta` quasiquotes — every `q"trait …"`
  * body is now composed as a plain Scala source string. Field / identifier
  * names route through `DomainScalaParseBack.renderS30(Term.Name(_))` for
  * Scala-3 reserved-word escape; type references render via
  * `ctx.conv.toScala(...).typeFull.toString`. The produced strings are still
  * consumed by `CogenProductSplice.parseSiblings` / `parseInit` at carrier
  * render time (parse-back boundary unchanged).
  *
  * Wire-format parity is preserved by construction: the strings parse back to
  * the same `Defn.Trait` AST as the legacy quasiquotes; scalameta's printer
  * then renormalises the AST to the canonical bytes that downstream goldens
  * expect. The `runWireFixtures` and `runCrossLangInterop` gates are the
  * authoritative oracles.
  */
trait DomainCirceTranslatorExtensionBase {

  /** F-TextTree M8a: Circe trait carrier.
    *
    * `defnText` is the rendered trait source for the `siblings` slot.
    * `initText` is the rendered companion-base init for the
    * `companionCirceBases` slot.
    * `name` is exposed for trace/debug callers. */
  protected case class CirceTrait(name: String, defnText: String, initText: String)

  /** F-TextTree M8d: accepts rendered trait source directly (no scala.meta
    * round-trip on the trait body). The init for the
    * `companionCirceBases` slot still flows through `ScalaTypeConverter`
    * (`ctx.conv.toScala(ownerId).sibling(name).init()`) — when `ownerId`
    * is the synthesized impl-DTO `DTOId(owner, "Struct")` the resulting
    * sibling-init type qualifies as `Owner.StructCirce` (not just
    * `StructCirce`), which is what the legacy `q"…"` quasiquote emitted
    * and what downstream goldens encode. We render the init to source
    * text once here so the `CirceTrait.initText` payload remains String. */
  private def mkCirceTrait(ctx: DomainSTContext, name: String, defnText: String, ownerId: izumi.idealingua.model.common.TypeId): CirceTrait = {
    import ctx.conv.*
    val init     = ctx.conv.toScala(ownerId).sibling(name).init()
    val initText = DomainScalaParseBack.renderS30(init)
    CirceTrait(name, defnText, initText)
  }

  /** Scala-version-specific deriver imports — Scala 3 uses
    * `io.circe.generic.semiauto`, Scala 2.13 uses `io.circe.derivation`.
    *
    * F-TextTree M8d: returns rendered import source text directly. */
  protected def classDeriverImports(scalaVersions: List[String]): List[String]

  private val circeRuntimePkg = runtime.Pkg.of[IRTTimeInstances]
  private val irtTimeInstancesBase: String =
    circeRuntimePkg.conv.toScala[IRTTimeInstances].typeAbsolute.toString

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
      val tpe         = stype.typeName.toString
      val singleField = flat.fields.head.field
      val ftpe        = ctx.conv.toScala(singleField.typeId).typeFull.toString
      // Legacy emitted bare `termName(d)` (not `termFull` — that would
      // qualify with the parent companion, breaking the goldens for
      // service-method-output ephemerals like `TestService.HelloOutput`).
      val termRef     = stype.termName.toString
      val fieldNm     = DomainScalaParseBack.renderS30(Term.Name(singleField.name))

      val encName = valName(s"encodeUnwrapped$name")
      val decName = valName(s"decodeUnwrapped$name")
      val traitNm = typeName(s"${name}Circe")
      // Indent each emitter helper at 2-space (trait body) for parse-back stability.
      val encoder =
        if (isObjectEncoder(ctx, singleField.typeId)) {
          s"""  implicit val $encName: Encoder.AsObject[$tpe] = Encoder.AsObject.instance { v => v.$fieldNm.asJsonObject }"""
        } else {
          s"""  implicit val $encName: Encoder[$tpe] = Encoder.instance { v => v.$fieldNm.asJson }"""
        }

      val traitSrc =
        s"""trait $traitNm extends $irtTimeInstancesBase {
           |  import _root_.io.circe.*
           |  import _root_.io.circe.syntax.*
           |$encoder
           |  implicit val $decName: Decoder[$tpe] = Decoder.instance { v => v.as[$ftpe].map(d => $termRef(d)) }
           |}""".stripMargin

      mkCirceTrait(ctx, s"${name}Circe", traitSrc, dtoId)
    } else {
      withDerivedStructCore(ctx, dtoId, flat.fields, scalaVersions)
    }
  }

  /** Defns for an ADT — tagged-union codec. */
  def emitForAdt(ctx: DomainSTContext, adt: NewTypeDef.Adt): CirceTrait = {
    import ctx.conv.*
    val id  = adt.id
    val t   = toScala(id)
    val tpe = t.typeFull.toString

    // Deterministic order: alternatives carry source order on `TypeDef.Adt.alternatives`
    // already (List, not Set — see Domain.scala "Field-ordering invariant").
    val implementors = adt.alternatives

    // Encoder case-arm format mirrors the scalameta Scala30 printer output
    // for `q"...Encoder.AsObject.instance { case v: T => Map(...).asJsonObject }"`:
    // each case keyword at 4-space indent, body indented by another 2 spaces
    // on the next line.
    val encArms = implementors.map { c =>
      val altTypeFull = t.within(c.typename).typeFull.toString
      val wire        = quoteString(c.wireId)
      s"""    case v: $altTypeFull =>
         |      Map($wire -> v.value).asJsonObject""".stripMargin
    }

    val decArms = implementors.map { c =>
      val altTypeAbs  = toScala(c.typeId).typeAbsolute.toString
      val altTermFull = t.within(c.typename).termFull.toString
      val wire        = quoteString(c.wireId)
      s"""      case $wire =>
         |        value.as[$altTypeAbs].map($altTermFull.apply)""".stripMargin
    }

    val altList  = implementors.map(c => quoteString(c.wireId)).mkString(", ")
    val cnameLit = quoteString(id.wireId)
    val missingDefinitionCase =
      s"""      case _ =>
         |        val cname = $cnameLit
         |        val alts = List($altList).mkString(",")
         |        Left(DecodingFailure(s"Can't decode type $$fname as $$cname, expected one of [$$alts]", value.history))""".stripMargin

    val allEncArms = encArms.mkString("\n")
    val allDecArms = (decArms :+ missingDefinitionCase).mkString("\n")

    val encName = valName(s"encode${id.name}")
    val decName = valName(s"decode${id.name}")
    val traitNm = typeName(s"${id.name}Circe")
    val traitSrc =
      s"""trait $traitNm {
         |  import _root_.io.circe.syntax.*
         |  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
         |  implicit val $encName: Encoder.AsObject[$tpe] = Encoder.AsObject.instance {
         |$allEncArms
         |  }
         |  implicit val $decName: Decoder[$tpe] = Decoder.instance(c => {
         |    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \\"type_name\\": { ...fields } }", c.history))
         |    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
         |$allDecArms
         |    }) yield {
         |      result
         |    }
         |  })
         |}""".stripMargin

    mkCirceTrait(ctx, s"${id.name}Circe", traitSrc, id)
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
    val t   = toScala(i.id)
    val tpe = t.typeFull.toString

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

    // F-implementing-dtos-missing (PR-02 IMPL-7a.2-Fh3): legacy
    // `implementingDtos(i.id)` also includes the interface-mirror DTOs of
    // every descendant interface — because those mirrors extend their owner
    // via `&` and the owner extends `i.id` via `&`. The new-IR mirrors
    // (`DTOId(Sub, "Struct")`) live as `Member.Ephemeral` and were missed by
    // the user-DTO-only scan above. Walk every user Interface whose
    // `interfaceClosureContains(i.id)`, and surface its mirror DTO when
    // present in `members`.
    val descendantMirrors: Set[DTOId] = {
      val buf = scala.collection.mutable.LinkedHashSet.empty[DTOId]
      ctx.domain.userTypes.values.foreach {
        case ifc: NewTypeDef.Interface if ifc.id != i.id =>
          if (interfaceClosureContains(ctx, ifc.id, i.id)) {
            val mirrorId = DTOId(ifc.id, "Struct")
            ctx.domain.members.get(mirrorId) match {
              case Some(_: Member.Ephemeral) => val _ = buf.add(mirrorId); ()
              case _                         => ()
            }
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

    val implementors = (interfaceInheritedDtos ++ descendantMirrors ++ mirrorImplementor).toList.sortBy(_.toString)

    val encArms = implementors.map { c =>
      val implTypeFull = toScala(c).typeFull.toString
      val wire         = quoteString(c.wireId)
      s"""    case v: $implTypeFull =>
         |      Map($wire -> v).asJsonObject""".stripMargin
    }

    val decArms = implementors.map { c =>
      val implTypeFull = toScala(c).typeFull.toString
      val wire         = quoteString(c.wireId)
      s"""      case $wire =>
         |        value.as[$implTypeFull]""".stripMargin
    }

    val altList  = implementors.map(c => quoteString(c.wireId)).mkString(", ")
    val cnameLit = quoteString(i.id.wireId)
    val missingDefinitionCase =
      s"""      case _ =>
         |        val cname = $cnameLit
         |        val alts = List($altList).mkString(",")
         |        Left(DecodingFailure(s"Can't decode type $$fname as $$cname, expected one of [$$alts]", value.history))""".stripMargin

    val allEncArms = encArms.mkString("\n")
    val allDecArms = (decArms :+ missingDefinitionCase).mkString("\n")

    val encName = valName(s"encode${i.id.name}")
    val decName = valName(s"decode${i.id.name}")
    val traitNm = typeName(s"${i.id.name}Circe")
    val traitSrc =
      s"""trait $traitNm {
         |  import _root_.io.circe.syntax.*
         |  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
         |  implicit val $encName: Encoder.AsObject[$tpe] = Encoder.AsObject.instance {
         |$allEncArms
         |  }
         |  implicit val $decName: Decoder[$tpe] = Decoder.instance(c => {
         |    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \\"type_name\\": { ...fields } }", c.history))
         |    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
         |$allDecArms
         |    }) yield result
         |  })
         |}""".stripMargin

    mkCirceTrait(ctx, s"${i.id.name}Circe", traitSrc, i.id)
  }

  protected def withParseable(ctx: DomainSTContext, id: TypeId): CirceTrait = {
    val t       = ctx.conv.toScala(id)
    val tpe     = t.typeFull.toString
    val termAbs = t.termFull.toString
    val nm      = id.name
    val traitNm     = typeName(s"${nm}Circe")
    val encName     = valName(s"encode$nm")
    val decName     = valName(s"decode$nm")
    val encKeyName  = valName(s"encodeKey$nm")
    val decKeyName  = valName(s"decodeKey$nm")
    // KeyDecoder body matches the legacy quasiquote scalameta-Scala30
    // printer output, which collapses `new KeyDecoder[$tpe] { final def
    // apply(...): Option[$tpe] = ... }` onto a single line.
    val traitSrc =
      s"""trait $traitNm {
         |  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
         |  import scala.util.*
         |  implicit val $encName: Encoder[$tpe] = Encoder.encodeString.contramap(_.toString)
         |  implicit val $decName: Decoder[$tpe] = Decoder.decodeString.emapTry(v => Try($termAbs.parse(v)))
         |  implicit val $encKeyName: KeyEncoder[$tpe] = KeyEncoder.encodeKeyString.contramap(_.toString)
         |  implicit val $decKeyName: KeyDecoder[$tpe] = new KeyDecoder[$tpe] { final def apply(key: String): Option[$tpe] = Try($termAbs.parse(key)).toOption }
         |}""".stripMargin
    mkCirceTrait(ctx, s"${nm}Circe", traitSrc, id)
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
    val tpe   = stype.typeName.toString

    // Scala 3 AnyVal fallback (mirrors legacy fix): if the struct is exactly
    // one scalar field qualifying for AnyVal AND we're targeting Scala 3,
    // emit a manual forProduct1 codec — circe's deriver does not handle
    // AnyVal on Scala 3.
    //
    // Deduplicate `flatFields` by field name before counting: when an
    // interface re-declares a same-typed parent field (e.g. IA2 { & IA1;
    // Int: i32 } with IA1 { Int: i32 }), the BFS-flattener carries both
    // occurrences in `flat.fields` but the emitted case class deduplicates
    // (see `DomainScalaStruct.fromFlat`). The AnyVal predicate must match
    // the structure that actually appears in the generated source.
    val isScala3 = scalaVersions.exists(_.startsWith("3"))
    val dedupedFields: List[izumi.idealingua.typer.ir.FlatField] =
      flatFields.groupBy(_.field.name).values.map(_.head).toList
    val anyvalCase: Boolean = {
      dedupedFields.size == 1 && dedupedFields.forall(ff => isAnyValField(ctx, ff.field.typeId))
    }

    val traitNm = typeName(s"${name}Circe")
    val encName = valName(s"encode$name")
    val decName = valName(s"decode$name")
    if (anyvalCase && isScala3) {
      val singleField = dedupedFields.head.field
      val ftpe        = ctx.conv.toScala(singleField.typeId).typeFull.toString
      val fieldLit    = quoteString(singleField.name)
      val fieldNm     = DomainScalaParseBack.renderS30(Term.Name(singleField.name))
      val ctorType    = stype.typeName.toString
      val traitSrc =
        s"""trait $traitNm extends $irtTimeInstancesBase {
           |  import _root_.io.circe.{Encoder, Decoder}
           |  implicit val $encName: Encoder.AsObject[$tpe] = Encoder.forProduct1[$tpe, $ftpe]($fieldLit)((v: $tpe) => v.$fieldNm)
           |  implicit val $decName: Decoder[$tpe] = Decoder.forProduct1[$tpe, $ftpe]($fieldLit)((d: $ftpe) => new $ctorType(d))
           |}""".stripMargin
      mkCirceTrait(ctx, s"${name}Circe", traitSrc, id)
    } else {
      val deriverImports = classDeriverImports(scalaVersions).mkString("\n  ")
      val traitSrc =
        s"""trait $traitNm extends $irtTimeInstancesBase {
           |  $deriverImports
           |  import _root_.io.circe.{Encoder, Decoder}
           |  implicit val $encName: Encoder.AsObject[$tpe] = deriveEncoder[$tpe]
           |  implicit val $decName: Decoder[$tpe] = deriveDecoder[$tpe]
           |}""".stripMargin
      mkCirceTrait(ctx, s"${name}Circe", traitSrc, id)
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

  /** Render a val-/def-name identifier with Scala 2.13- and 3-correct
    * disambiguation for trailing-underscore names. The legacy quasiquote
    * (`q"implicit val ${Pat.Var(Term.Name("encodeName_stored_"))}: ..."`)
    * printed a space before the colon (`"... encodeName_stored_ :"`) to
    * prevent the parser from lexing `_:` as a typed-wildcard pattern.
    * Replicate that disambiguation by appending a space when the
    * identifier ends in `_`.
    *
    * Verified at `MetaProbeTest`: `Pat.Var(Term.Name("encodeName_stored_"))`
    * inside a `q"implicit val …: T = ???"` quasiquote prints as
    * `"implicit val encodeName_stored_ : T = ???"` on both Scala 2.13 and
    * Scala 3 dialects. */
  private def valName(s: String): String = {
    val rendered = DomainScalaParseBack.renderS30(Term.Name(s))
    if (rendered.endsWith("_")) s"$rendered " else rendered
  }

  /** Render a type-name identifier with backtick escaping for reserved
    * words. Drives the `trait $TypeName` and the type-name slot inside
    * `Encoder.AsObject[$TypeName]`. */
  private def typeName(s: String): String =
    DomainScalaParseBack.renderS30(Type.Name(s))

  /** Render a Scala `Lit.String` source-form for `s` — wraps in double
    * quotes and escapes embedded `"` and `\\`. F-TextTree M8d: replaces
    * `Lit.String(s)` callsites; the legacy quasiquote emitted these
    * literals via scalameta's printer which escapes only the same two
    * characters. */
  private def quoteString(s: String): String = {
    val sb = new StringBuilder(s.length + 2)
    sb.append('"')
    var i = 0
    while (i < s.length) {
      s.charAt(i) match {
        case '\\' => sb.append("\\\\")
        case '"'  => sb.append("\\\"")
        case c    => sb.append(c)
      }
      i += 1
    }
    sb.append('"')
    sb.toString
  }
}

/** Concrete derivation-import-wiring subclass — mirrors legacy
  * `CirceDerivationTranslatorExtension`. Scala 2.13 uses
  * `io.circe.derivation`; Scala 3 uses `io.circe.generic.semiauto`.
  */
object DomainCirceDerivationTranslatorExtension extends DomainCirceTranslatorExtensionBase {
  override protected def classDeriverImports(scalaVersions: List[String]): List[String] = {
    if (scalaVersions.exists(_.startsWith("3"))) List(scala3Import)
    else List(scala2Import)
  }

  private val scala2Import = "import _root_.io.circe.derivation.{deriveDecoder, deriveEncoder}"
  private val scala3Import = "import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}"
}
