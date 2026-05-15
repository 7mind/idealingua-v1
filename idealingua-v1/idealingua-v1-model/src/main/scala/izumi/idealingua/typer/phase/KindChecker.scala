package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.Primitive
import izumi.idealingua.typer.ir._

import scala.collection.mutable

/** Phase 4 — `KindChecker`.
  *
  * Enforces three per-defn-kind invariants on the resolved IR. Per defects-m1
  * PR-02-D05, this phase is intentionally narrow; duplicate-member /
  * naming-convention / empty-enum checks live in Phase 12 Validator (IMPL-5).
  *
  *   1. **Identifier fields** must be `Primitive` | `IdentifierId` | `EnumId`.
  *      Replaces the throw at legacy `IDLTyper.scala:157`. Diagnostic:
  *      `Diagnostic.BadIdentifierFieldType`. Phase 2 (`NameResolver`) emits
  *      this diagnostic at IR construction time (the IR's `IdField` ADT can
  *      only carry valid kinds, so this phase can no longer observe a bad
  *      kind on a typed value — the check is exercised at the raw → IR
  *      boundary).
  *   2. **Mixin targets** in `Struct.superclasses.concepts` /
  *      `removedConcepts` must resolve to `DTOId` | `InterfaceId`. Replaces
  *      the throw at legacy `IDLTyper.scala:322`. Diagnostic:
  *      `Diagnostic.BadMixinTarget`.
  *   3. **ADT branches** must resolve to a known user type. AdtId branches
  *      are permitted (legacy `IDLTyper.toMember` only rejects inline
  *      `RawAdt.Member.NestedDefn`, never an ADT referenced by name — that
  *      rejection lives in `NameResolver.fixAdt`). KindChecker only flags
  *      structurally invalid branches via
  *      `Diagnostic.NestedAdtMemberUnsupported`.
  *
  * Per C8/L1, never throws on user-visible input errors.
  */
object KindChecker {

  def apply(resolved: ResolvedDomain): ResolvedDomain = {
    val diagBuf = mutable.ArrayBuffer.empty[Diagnostic]

    resolved.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        checkConcepts(dto.id, dto.struct, resolved, diagBuf)

      case iface: TypeDef.Interface =>
        checkConcepts(iface.id, iface.struct, resolved, diagBuf)

      case adt: TypeDef.Adt =>
        adt.alternatives.foreach {
          alt =>
            // Dealias the branch first; an alias-to-ADT counts the same as a
            // direct AdtId reference. Legacy `IDLTyper.toMember` accepts any
            // resolved TypeId via the `TypeRef` arm and only rejects inline
            // `NestedDefn` (handled in `NameResolver.fixAdt`).
            val effective = dealiasOnce(resolved, alt.typeId)
            effective match {
              case _: DTOId | _: InterfaceId | _: IdentifierId | _: AdtId =>
                ()
              case _: Primitive =>
                () // permitted at this layer (legacy permits via aliases)
              case _: EnumId =>
                () // enum branches are tolerated; legacy emits via alias paths
              case other =>
                diagBuf += Diagnostic.NestedAdtMemberUnsupported(adt.id, other, adt.meta.pos)
            }
        }

      case _: TypeDef.Identifier => () // see scaladoc
      case _: TypeDef.Enum       => ()
      case _: TypeDef.Alias      => ()
      case _: TypeDef.Service    => ()
      case _: TypeDef.Buzzer     => ()
      case _: TypeDef.Streams    => ()
    }

    resolved.copy(diagnostics = resolved.diagnostics ++ Diagnostics(diagBuf.toVector))
  }

  private def checkConcepts(
    owner: TypeId,
    struct: Struct,
    resolved: ResolvedDomain,
    diagBuf: mutable.ArrayBuffer[Diagnostic],
  ): Unit = {
    val all = struct.superclasses.concepts ++ struct.superclasses.removedConcepts
    all.foreach {
      sid =>
        val target = dealiasOnce(resolved, sid)
        target match {
          case _: DTOId | _: InterfaceId => ()
          case bad =>
            diagBuf += Diagnostic.BadMixinTarget(owner, bad, izumi.idealingua.model.il.ast.InputPosition.Undefined)
        }
    }
  }

  private def dealiasOnce(resolved: ResolvedDomain, id: TypeId): TypeId = id match {
    case a: AliasId => resolved.aliases.getOrElse(a, a)
    case other      => other
  }
}
