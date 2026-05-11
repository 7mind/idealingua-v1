package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common._
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.typed.{AdtMember, DefMethod, DomainMetadata, EnumMember, Field, IdField, NodeMeta, Anno, SimpleStructure, Super, TypedStream, ConstValue}
import izumi.idealingua.typer.ir._
import izumi.idealingua.typer.phase.ScopeBuilder.ScopedDomain

import scala.collection.mutable

/** Phase 2 — `NameResolver`.
  *
  * Converts a `ScopedDomain` into a `ResolvedDomain`:
  *
  *   - every raw type definition becomes a typed `TypeDef.*` IR value;
  *   - every service / buzzer / streams becomes a `ServiceDef` /
  *     `BuzzerDef` / `StreamsDef`;
  *   - every `AbstractIndefiniteId` reference is resolved to a definite
  *     `TypeId` using the scope's local + imported maps plus the builtin
  *     `Primitive` / `Generic` namespaces;
  *   - unresolved references produce a `Diagnostic.UnknownTypeRef` and the
  *     reference is replaced by a `Primitive.TString` placeholder (so the
  *     `ResolvedDomain` remains structurally well-formed for downstream
  *     phases);
  *   - referenced builtins are added lazily to `members` as
  *     `Member.Builtin(prim)`.
  *
  * Per C8/L1, this phase never throws on user-visible input errors.
  */
object NameResolver {

  /** Sentinel placeholder for unresolved references. Phase 4 (`KindChecker`)
    * and later phases ignore entries whose diagnostics flag them.
    */
  private val Placeholder: TypeId = Primitive.TString

  def apply(scoped: ScopedDomain): ResolvedDomain = {
    val ctx = new Ctx(scoped)

    val members = mutable.LinkedHashMap.empty[TypeId, Member]
    val userTypes = mutable.LinkedHashMap.empty[TypeId, TypeDef]

    scoped.raw.types.foreach {
      case d: RawTypeDef.Enumeration => placeUserType(members, userTypes, ctx.fixEnum(d))
      case d: RawTypeDef.Alias       => placeUserType(members, userTypes, ctx.fixAlias(d))
      case d: RawTypeDef.Identifier  => placeUserType(members, userTypes, ctx.fixIdentifier(d))
      case d: RawTypeDef.Interface   => placeUserType(members, userTypes, ctx.fixInterface(d))
      case d: RawTypeDef.DTO         => placeUserType(members, userTypes, ctx.fixDto(d))
      case d: RawTypeDef.Adt         => placeUserType(members, userTypes, ctx.fixAdt(d))
      case d: RawTypeDef.NewType     => placeUserType(members, userTypes, ctx.fixNewType(d))
      case _: RawTypeDef.ForeignType => () // diagnostic already emitted by ScopeBuilder
      case _: RawTypeDef.DeclaredType => ()
    }

    scoped.raw.services.foreach(s => placeUserType(members, userTypes, ctx.fixService(s)))
    scoped.raw.buzzers.foreach(b => placeUserType(members, userTypes, ctx.fixBuzzer(b)))
    scoped.raw.streams.foreach(s => placeUserType(members, userTypes, ctx.fixStreams(s)))

    // Pre-seed members with any builtins that were touched during reference resolution.
    ctx.referencedBuiltins.foreach {
      p => members.update(p, Member.Builtin(p))
    }

    val consts: List[RawConst] = scoped.raw.consts.toList.flatMap(_.consts)

    val imports = mutable.LinkedHashMap.empty[DomainId, Set[TypeId]]
    scoped.importedNames.values.foreach {
      tid =>
        // path.domain on builtins is Builtin; skip those.
        val home: DomainId = tid match {
          case _: Builtin => DomainId.Builtin
          case s: ServiceId => s.domain
          case s: BuzzerId  => s.domain
          case s: StreamsId => s.domain
          case other        => other.path.domain
        }
        if (home != DomainId.Builtin) {
          imports.update(home, imports.getOrElse(home, Set.empty) + tid)
        }
    }

    val mergedDiagnostics = scoped.diagnostics ++ Diagnostics(ctx.diagBuf.toVector)

    ResolvedDomain(
      id          = scoped.domainId,
      meta        = makeMeta(scoped),
      members     = members.toMap,
      userTypes   = userTypes.toMap,
      imports     = imports.toMap,
      aliases     = Map.empty, // filled by AliasDealiaser
      consts      = consts,
      diagnostics = mergedDiagnostics,
    )
  }

  private def placeUserType(
    members: mutable.LinkedHashMap[TypeId, Member],
    userTypes: mutable.LinkedHashMap[TypeId, TypeDef],
    defn: TypeDef,
  ): Unit = {
    userTypes.update(defn.id, defn)
    members.update(defn.id, Member.User(defn))
  }

  private def makeMeta(scoped: ScopedDomain): DomainMetadata = {
    DomainMetadata(
      origin           = scoped.raw.origin,
      directInclusions = scoped.raw.directInclusions.map(i => izumi.idealingua.model.il.ast.typed.Inclusion(i.i)),
      directImports    = scoped.raw.originalImports,
      meta             = NodeMeta(scoped.raw.meta.doc, Seq.empty, scoped.raw.meta.position),
    )
  }

  /** Per-domain mutable resolution context. Tracks referenced builtins so the
    * caller can pre-seed `members` with `Member.Builtin` entries, and
    * accumulates phase-2 diagnostics.
    */
  private final class Ctx(scoped: ScopedDomain) {
    val diagBuf: mutable.ArrayBuffer[Diagnostic] = mutable.ArrayBuffer.empty
    val referencedBuiltins: mutable.LinkedHashSet[Primitive] = mutable.LinkedHashSet.empty

    private val localNames = scoped.localNames
    private val importedNames = scoped.importedNames

    def fixEnum(d: RawTypeDef.Enumeration): TypeDef.Enum =
      TypeDef.Enum(d.id, d.struct.members.map(m => EnumMember(m.value, fixMeta(m.meta))), fixMeta(d.meta))

    def fixAlias(d: RawTypeDef.Alias): TypeDef.Alias =
      TypeDef.Alias(d.id, resolveRef(d.target, d.meta.position), fixMeta(d.meta))

    def fixIdentifier(d: RawTypeDef.Identifier): TypeDef.Identifier = {
      val fields = d.fields.map {
        f =>
          val tid  = resolveRef(f.typeId, f.meta.position)
          val name = derivedFieldName(f, tid, d.fields.size)
          tid match {
            case p: PrimitiveId => IdField.PrimitiveField(p, name, fixMeta(f.meta))
            case e: EnumId      => IdField.Enum(e, name, fixMeta(f.meta))
            case i: IdentifierId => IdField.SubId(i, name, fixMeta(f.meta))
            case other =>
              // Phase 4 (KindChecker) will flag this; here we still need a well-formed
              // IdField so downstream phases don't NPE. Carry the bad type as a
              // PrimitiveField sentinel and let KindChecker emit BadIdentifierFieldType.
              diagBuf += Diagnostic.BadIdentifierFieldType(d.id, name, other, f.meta.position)
              IdField.PrimitiveField(Primitive.TString, name, fixMeta(f.meta))
          }
      }
      TypeDef.Identifier(d.id, fields, fixMeta(d.meta))
    }

    def fixInterface(d: RawTypeDef.Interface): TypeDef.Interface =
      TypeDef.Interface(d.id, toStruct(d.struct), fixMeta(d.meta))

    def fixDto(d: RawTypeDef.DTO): TypeDef.Dto =
      TypeDef.Dto(d.id, toStruct(d.struct), fixMeta(d.meta))

    def fixAdt(d: RawTypeDef.Adt): TypeDef.Adt = {
      val members = d.alternatives.map {
        case RawAdt.Member.TypeRef(typeId, memberName, m) =>
          AdtMember(resolveRef(typeId, m.position), memberName, fixMeta(m))
        case RawAdt.Member.NestedDefn(nested) =>
          // Nested defns are unsupported; emit diagnostic and synthesize a
          // sentinel AdtMember referencing TString so the shape stays valid.
          diagBuf += Diagnostic.NestedAdtMemberUnsupported(d.id, nested.id, NodeMetaPos.of(nested))
          AdtMember(Placeholder, None, NodeMeta.empty)
      }
      TypeDef.Adt(d.id, members, fixMeta(d.meta))
    }

    /** Newtypes become aliases at this layer (legacy behaviour); the
      * `Some(modifiers)` case extends a base structural type — Phase 6
      * (IMPL-3) will materialise it. For now we synthesize an alias.
      */
    def fixNewType(d: RawTypeDef.NewType): TypeDef.Alias =
      TypeDef.Alias(d.id.toAliasId, resolveRef(d.source, d.meta.position), fixMeta(d.meta))

    def fixService(s: RawService): TypeDef.Service =
      TypeDef.Service(s.id, s.methods.map(fixMethod), fixMeta(s.meta))

    def fixBuzzer(b: RawBuzzer): TypeDef.Buzzer =
      TypeDef.Buzzer(b.id, b.events.map(fixMethod), fixMeta(b.meta))

    def fixStreams(s: RawStreams): TypeDef.Streams =
      TypeDef.Streams(s.id, s.streams.map(fixStream), fixMeta(s.meta))

    private def fixMethod(m: RawMethod): DefMethod = m match {
      case rpc: RawMethod.RPCMethod =>
        DefMethod.RPCMethod(rpc.name, fixSignature(rpc.signature), fixMeta(rpc.meta))
    }

    private def fixStream(s: RawStream): TypedStream = s match {
      case d: RawStream.Directed =>
        TypedStream.Directed(d.name, d.direction, fixSimpleStructure(d.signature, d.meta.position), fixMeta(d.meta))
    }

    private def fixSignature(sig: RawMethod.Signature): DefMethod.Signature =
      DefMethod.Signature(fixSimpleStructure(sig.input, InputPosition.Undefined), fixOutput(sig.output))

    private def fixOutput(o: RawMethod.Output): DefMethod.Output = o match {
      case alt: RawMethod.Output.Alternative =>
        DefMethod.Output.Alternative(fixNonAltOutput(alt.success), fixNonAltOutput(alt.failure))
      case n: RawMethod.Output.NonAlternativeOutput =>
        fixNonAltOutput(n)
    }

    private def fixNonAltOutput(o: RawMethod.Output.NonAlternativeOutput): DefMethod.Output.NonAlternativeOutput = o match {
      case s: RawMethod.Output.Struct    => DefMethod.Output.Struct(fixSimpleStructure(s.input, InputPosition.Undefined))
      case a: RawMethod.Output.Algebraic =>
        DefMethod.Output.Algebraic(a.alternatives.map {
          case RawAdt.Member.TypeRef(tid, mn, m) => AdtMember(resolveRef(tid, m.position), mn, fixMeta(m))
          case RawAdt.Member.NestedDefn(n)       =>
            diagBuf += Diagnostic.NestedAdtMemberUnsupported(scoped.raw.id.toTypeId, n.id, NodeMetaPos.of(n))
            AdtMember(Placeholder, None, NodeMeta.empty)
        })
      case s: RawMethod.Output.Singular  => DefMethod.Output.Singular(resolveRef(s.typeId, InputPosition.Undefined))
      case _: RawMethod.Output.Void      => DefMethod.Output.Void()
    }

    private def toStruct(s: RawStructure): Struct = {
      Struct(
        fields        = s.fields.map(fixField),
        removedFields = s.removedFields.map(fixField),
        superclasses  = Super(
          interfaces      = s.interfaces,
          concepts        = s.concepts.map(m => resolveStructure(m)),
          removedConcepts = s.removedConcepts.map(m => resolveStructure(m)),
        ),
      )
    }

    private def fixSimpleStructure(s: RawSimpleStructure, @scala.annotation.unused pos: InputPosition): SimpleStructure =
      SimpleStructure(
        concepts = s.concepts.map(resolveStructure),
        fields   = s.fields.map(fixField),
      )

    private def fixField(f: RawField): Field = {
      val tid  = resolveRef(f.typeId, f.meta.position)
      val name = derivedFieldName(f, tid, fieldsCount = 2)
      Field(typeId = tid, name = name, meta = fixMeta(f.meta))
    }

    private def derivedFieldName(f: RawField, tid: TypeId, fieldsCount: Int): String = f.name match {
      case Some(value) => value
      case None if fieldsCount == 1 => "value"
      case None =>
        import izumi.fundamentals.platform.strings.IzString._
        val n = tid.name.uncapitalize
        if (n.startsWith("#")) n.substring(1) else n
    }

    private def resolveStructure(m: IndefiniteMixin): StructureId = {
      val id = IndefiniteId(m.pkg, m.name)
      resolveRef(id, InputPosition.Undefined) match {
        case s: StructureId => s
        case other          =>
          diagBuf += Diagnostic.BadMixinTarget(scoped.raw.id.toTypeId, other, InputPosition.Undefined)
          // synthesize a sentinel DTOId so IR stays well-formed
          DTOId(TypePath(scoped.domainId, Seq.empty), s"<bad-mixin:${m.name}>")
      }
    }

    private def resolveRef(ref: AbstractIndefiniteId, pos: InputPosition): TypeId = ref match {
      case g: IndefiniteGeneric => resolveGeneric(g, pos)
      case _ =>
        if (Primitive.mapping.contains(ref.name) && ref.pkg.isEmpty) {
          val p = Primitive.mapping(ref.name)
          val _ = referencedBuiltins.add(p)
          p
        } else {
          // Lookup order mirrors the legacy IDLPostTyper: when the reference is
          // unqualified or names the current domain, search local names first
          // and fall back to imports; when fully qualified to a different
          // package, search imports directly. Unresolved names produce a
          // diagnostic and a placeholder (non-fatal per C8/L1).
          val isLocalCandidate = ref.pkg.isEmpty || ref.pkg == scoped.domainId.toPackage
          val candidate: Option[TypeId] =
            if (isLocalCandidate) localNames.get(ref.name).orElse(importedNames.get(ref.name))
            else importedNames.get(ref.name).orElse(localNames.get(ref.name))
          candidate match {
            case Some(tid) => tid
            case None =>
              diagBuf += Diagnostic.UnknownTypeRef(s"${ref.pkg.mkString(".")}.${ref.name}", pos)
              val _ = referencedBuiltins.add(Primitive.TString)
              Placeholder
          }
        }
    }

    private def resolveGeneric(g: IndefiniteGeneric, pos: InputPosition): TypeId = {
      def arity(n: Int): Boolean = g.args.size == n
      if (Generic.TList.aliases.contains(g.name)) {
        if (!arity(1)) diagBuf += Diagnostic.WrongGenericArity(g.name, 1, g.args.size, pos)
        Generic.TList(resolveRef(g.args.headOption.getOrElse(IndefiniteId(Seq.empty, "str")), pos))
      } else if (Generic.TSet.aliases.contains(g.name)) {
        if (!arity(1)) diagBuf += Diagnostic.WrongGenericArity(g.name, 1, g.args.size, pos)
        Generic.TSet(resolveRef(g.args.headOption.getOrElse(IndefiniteId(Seq.empty, "str")), pos))
      } else if (Generic.TOption.aliases.contains(g.name)) {
        if (!arity(1)) diagBuf += Diagnostic.WrongGenericArity(g.name, 1, g.args.size, pos)
        Generic.TOption(resolveRef(g.args.headOption.getOrElse(IndefiniteId(Seq.empty, "str")), pos))
      } else if (Generic.TMap.aliases.contains(g.name)) {
        if (!arity(2)) diagBuf += Diagnostic.WrongGenericArity(g.name, 2, g.args.size, pos)
        val k = resolveRef(g.args.headOption.getOrElse(IndefiniteId(Seq.empty, "str")), pos)
        val v = resolveRef(g.args.lift(1).getOrElse(IndefiniteId(Seq.empty, "str")), pos)
        val keyScalar: ScalarId = k match {
          case s: ScalarId => s
          case _ =>
            diagBuf += Diagnostic.UnknownTypeRef(s"map-key:${g.name}", pos)
            val _ = referencedBuiltins.add(Primitive.TString)
            Primitive.TString
        }
        Generic.TMap(keyScalar, v)
      } else {
        diagBuf += Diagnostic.UnknownTypeRef(g.name, pos)
        val _ = referencedBuiltins.add(Primitive.TString)
        Placeholder
      }
    }

    private def fixMeta(m: RawNodeMeta): NodeMeta = NodeMeta(m.doc, m.annos.map(fixAnno), m.position)

    private def fixAnno(a: RawAnno): Anno = Anno(a.name, a.values.value.map { case (k, v) => k -> translateValue(v) }, a.position)

    private def translateValue(v: RawVal): ConstValue = v match {
      case RawVal.CInt(value)    => ConstValue.CInt(value)
      case RawVal.CLong(value)   => ConstValue.CLong(value)
      case RawVal.CFloat(value)  => ConstValue.CFloat(value)
      case RawVal.CString(value) => ConstValue.CString(value)
      case RawVal.CBool(value)   => ConstValue.CBool(value)
      case RawVal.CMap(value)    => ConstValue.CMap(value.map { case (k, v) => k -> translateValue(v) })
      case RawVal.CList(value)   => ConstValue.CList(value.map(translateValue))
      case RawVal.CTypedList(typeId, value) =>
        ConstValue.CTypedList(resolveRef(typeId, InputPosition.Undefined), ConstValue.CList(value.map(translateValue)))
      case RawVal.CTyped(typeId, value) =>
        ConstValue.CTyped(resolveRef(typeId, InputPosition.Undefined), translateValue(value))
      case RawVal.CTypedObject(typeId, value) =>
        ConstValue.CTypedObject(resolveRef(typeId, InputPosition.Undefined), ConstValue.CMap(value.map { case (k, v) => k -> translateValue(v) }))
    }
  }

  /** Best-effort position extraction for nested WithId members (used by ADT
    * nested-defn diagnostics; nested defns carry their own meta).
    */
  private object NodeMetaPos {
    def of(d: RawTypeDef.WithId): InputPosition = d match {
      case t: RawTypeDef.Interface   => t.meta.position
      case t: RawTypeDef.DTO         => t.meta.position
      case t: RawTypeDef.Enumeration => t.meta.position
      case t: RawTypeDef.Alias       => t.meta.position
      case t: RawTypeDef.Identifier  => t.meta.position
      case t: RawTypeDef.Adt         => t.meta.position
    }
  }

  /** Build a synthetic TypeId pointing at the domain itself (used as an
    * "owner" reference when no narrower owner is available). The legacy
    * model does not have a first-class domain-id-as-TypeId; the closest
    * stand-in is a domain-rooted AliasId of the domain's name. The
    * resulting TypeId is purely diagnostic-payload.
    */
  private implicit class DomainIdOps(val d: DomainId) extends AnyVal {
    def toTypeId: TypeId = AliasId(TypePath(d, Seq.empty), s"<domain:${d.id}>")
  }
}
