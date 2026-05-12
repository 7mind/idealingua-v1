package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common._
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns._
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded
import izumi.idealingua.model.il.ast.typed.{AdtMember, Anno, ConstValue, DefMethod, DomainMetadata, EnumMember, Field, IdField, NodeMeta, SimpleStructure, Super, TypedStream}
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

  /** Test-friendly overload: derive a `FamilyIndex` from the scoped domain's
    * underlying `DomainMeshLoaded`.  Production callers
    * (`NewTyperPipeline`) pass the family index explicitly so cross-domain
    * lookups consult the same index that Phase 0 built.
    */
  def apply(scoped: ScopedDomain): ResolvedDomain =
    apply(scoped, IdealinguaFamilyManager(scoped.raw))

  def apply(scoped: ScopedDomain, family: FamilyIndex): ResolvedDomain = {
    val ctx = new Ctx(scoped, family)

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
  private final class Ctx(scoped: ScopedDomain, family: FamilyIndex) {
    val diagBuf: mutable.ArrayBuffer[Diagnostic] = mutable.ArrayBuffer.empty
    val referencedBuiltins: mutable.LinkedHashSet[Primitive] = mutable.LinkedHashSet.empty

    private val localNames = scoped.localNames
    private val importedNames = scoped.importedNames

    /** Lazy cache of per-domain exported simple-name → TypeId maps.  Populated on
      * first cross-domain reference into a given domain.  Closes F4 / F5a / F5b
      * (NameResolver did not consult the family index for `domain#Type` refs).
      */
    private val familyScopes = mutable.HashMap.empty[DomainId, Map[String, TypeId]]

    private def familyScope(d: DomainId): Map[String, TypeId] =
      familyScopes.getOrElseUpdate(d, family.domains.get(d).map(collectExportedNames).getOrElse(Map.empty))

    /** Mirrors `ScopeBuilder.collectLocalNames` (private), kept here to avoid a
      * cross-object dependency and keep the resolver self-contained.  Both
      * compute simple-name → fully-qualified-TypeId from the raw type list.
      */
    private def collectExportedNames(domain: DomainMeshLoaded): Map[String, TypeId] = {
      domain.types.iterator.collect {
        case d: RawTypeDef.WithId  => d.id.name -> ScopeBuilder.normalizeId(d.id, domain.id)
        case d: RawTypeDef.NewType => d.id.name -> ScopeBuilder.normalizeId(d.id.toAliasId, domain.id)
      }.toMap
    }

    /** Normalise a raw-AST `TypeId` so its `path.domain` (or `domain` for
      * service-family ids) reflects the owning domain rather than the
      * `DomainId.Undefined` produced by `ParsedId.typePath` for unqualified
      * local declarations. Mirrors `ScopeBuilder.normalizeId` and the legacy
      * `IDLPostTyper.fixPkg` / `fixServiceId` step. Keeps the IR-shape
      * invariant: every `TypeDef.id` declared in domain `X` carries
      * `path.domain == X` (closes IMPL-7a.2 Phase B M2 finding).
      */
    private def own(t: TypeId): TypeId = ScopeBuilder.normalizeId(t, scoped.domainId)

    def fixEnum(d: RawTypeDef.Enumeration): TypeDef.Enum =
      TypeDef.Enum(own(d.id).asInstanceOf[EnumId], d.struct.members.map(m => EnumMember(m.value, fixMeta(m.meta))), fixMeta(d.meta))

    def fixAlias(d: RawTypeDef.Alias): TypeDef.Alias =
      TypeDef.Alias(own(d.id).asInstanceOf[AliasId], resolveRef(d.target, d.meta.position), fixMeta(d.meta))

    def fixIdentifier(d: RawTypeDef.Identifier): TypeDef.Identifier = {
      val ownedId = own(d.id).asInstanceOf[IdentifierId]
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
              diagBuf += Diagnostic.BadIdentifierFieldType(ownedId, name, other, f.meta.position)
              IdField.PrimitiveField(Primitive.TString, name, fixMeta(f.meta))
          }
      }
      TypeDef.Identifier(ownedId, fields, fixMeta(d.meta))
    }

    def fixInterface(d: RawTypeDef.Interface): TypeDef.Interface =
      TypeDef.Interface(own(d.id).asInstanceOf[InterfaceId], toStruct(d.struct), fixMeta(d.meta))

    def fixDto(d: RawTypeDef.DTO): TypeDef.Dto =
      TypeDef.Dto(own(d.id).asInstanceOf[DTOId], toStruct(d.struct), fixMeta(d.meta))

    def fixAdt(d: RawTypeDef.Adt): TypeDef.Adt = {
      val ownedId = own(d.id).asInstanceOf[AdtId]
      val members = d.alternatives.map {
        case RawAdt.Member.TypeRef(typeId, memberName, m) =>
          AdtMember(resolveRef(typeId, m.position), memberName, fixMeta(m))
        case RawAdt.Member.NestedDefn(nested) =>
          // Nested defns are unsupported; emit diagnostic and synthesize a
          // sentinel AdtMember referencing TString so the shape stays valid.
          diagBuf += Diagnostic.NestedAdtMemberUnsupported(ownedId, nested.id, NodeMetaPos.of(nested))
          AdtMember(Placeholder, None, NodeMeta.empty)
      }
      TypeDef.Adt(ownedId, members, fixMeta(d.meta))
    }

    /** Newtypes become aliases at this layer (legacy behaviour); the
      * `Some(modifiers)` case extends a base structural type — Phase 6
      * (IMPL-3) will materialise it. For now we synthesize an alias.
      */
    def fixNewType(d: RawTypeDef.NewType): TypeDef.Alias =
      TypeDef.Alias(own(d.id.toAliasId).asInstanceOf[AliasId], resolveRef(d.source, d.meta.position), fixMeta(d.meta))

    def fixService(s: RawService): TypeDef.Service =
      TypeDef.Service(own(s.id).asInstanceOf[ServiceId], s.methods.map(fixMethod), fixMeta(s.meta))

    def fixBuzzer(b: RawBuzzer): TypeDef.Buzzer =
      TypeDef.Buzzer(own(b.id).asInstanceOf[BuzzerId], b.events.map(fixMethod), fixMeta(b.meta))

    def fixStreams(s: RawStreams): TypeDef.Streams =
      TypeDef.Streams(own(s.id).asInstanceOf[StreamsId], s.streams.map(fixStream), fixMeta(s.meta))

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
          interfaces      = s.interfaces.map(resolveInterface),
          concepts        = s.concepts.map(m => resolveStructure(m)),
          removedConcepts = s.removedConcepts.map(m => resolveStructure(m)),
        ),
      )
    }

    /** Re-target a parser-produced `InterfaceId` (which carries
      * `DomainId.Undefined` and only the bare name) to a definite id via
      * `resolveRef`.  If the name resolves to an `AliasId` whose local target
      * is a `StructureId`, dealias one hop so downstream phases see the
      * concrete interface/DTO id — mirrors the legacy `fixSimpleId` arm at
      * `IDLTyper.scala:549-561` that special-cases alias entries in the
      * type-id index.  Multi-hop alias chains are handled by Phase 3
      * (`AliasDealiaser`); a single hop is enough for the common case
      * (`alias A = M ; data D { & A }`).
      */
    private def resolveInterface(t: InterfaceId): InterfaceId = {
      val name = t.name
      val refPkg: Package =
        if (t.path.domain == DomainId.Undefined) Seq.empty
        else t.path.toPackage
      val resolved = resolveRef(IndefiniteId(refPkg, name), InputPosition.Undefined)
      resolved match {
        case i: InterfaceId => i
        case a: AliasId =>
          dealiasOneHop(a) match {
            case i: InterfaceId => i
            case other =>
              diagBuf += Diagnostic.BadMixinTarget(scoped.raw.id.toTypeId, other, InputPosition.Undefined)
              t.copy(path = TypePath(scoped.domainId, Seq.empty))
          }
        case other =>
          diagBuf += Diagnostic.BadMixinTarget(scoped.raw.id.toTypeId, other, InputPosition.Undefined)
          t.copy(path = TypePath(scoped.domainId, Seq.empty))
      }
    }

    /** Resolve a single alias hop using the raw type index.  Returns the
      * alias's syntactic target as a `TypeId` (the immediate `.target` of the
      * `RawTypeDef.Alias`).  Used by `resolveInterface` / `resolveStructure`
      * to handle `& AliasOfMixin` references; multi-hop chasing is the job of
      * Phase 3.
      */
    private def dealiasOneHop(a: AliasId): TypeId = {
      scoped.index.get(a) match {
        case Some(d: RawTypeDef.Alias) => resolveRef(d.target, InputPosition.Undefined)
        case _                          => a
      }
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
        case a: AliasId =>
          // Mirror the legacy `fixSimpleId` alias-dealias arm
          // (`IDLTyper.scala:549-561`): `+ AliasOfMixin` is valid when the
          // alias targets a structural type.
          dealiasOneHop(a) match {
            case s: StructureId => s
            case other =>
              diagBuf += Diagnostic.BadMixinTarget(scoped.raw.id.toTypeId, other, InputPosition.Undefined)
              DTOId(TypePath(scoped.domainId, Seq.empty), s"<bad-mixin:${m.name}>")
          }
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
          // Lookup order mirrors the legacy IDLPostTyper.lookupAnother /
          // lookupLocal split (IDLTyper.scala:377-407):
          //   - empty pkg or pkg == this domain ⇒ search local names then imports.
          //   - pkg names a different domain  ⇒ consult the cross-domain family
          //     index for that domain's exported types (closes F4/F5a/F5b-part1),
          //     then fall back to imports / local names so a fully-qualified
          //     reference still resolves when the family doesn't carry the
          //     domain (single-domain test fixtures).
          // Unresolved names produce a diagnostic and a placeholder (non-fatal per C8/L1).
          val isLocalCandidate = ref.pkg.isEmpty || ref.pkg == scoped.domainId.toPackage
          val candidate: Option[TypeId] =
            if (isLocalCandidate) {
              localNames.get(ref.name).orElse(importedNames.get(ref.name))
            } else {
              // Derive the referenced DomainId from the qualified package
              // (matches legacy `DomainId(v.init, v.last)`).
              val otherDomain = DomainId(ref.pkg.init, ref.pkg.last)
              familyScope(otherDomain).get(ref.name)
                .orElse(importedNames.get(ref.name))
                .orElse(localNames.get(ref.name))
            }
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
