package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod

/** Pure name-mangling helpers for the C# renderer family, mirroring the
  * literal mangling rules embedded inline in the legacy `CSharpTranslator`.
  *
  * IMPL-7c Phase B M3: introduced for the ADT / Service / Buzzer rendering
  * port. C# counterpart of `DomainTSNameMangling` (TS uses `In`/`Out`
  * prefixes — same convention here, since the wire-format envelope keys
  * for service / buzzer methods carry these names).
  *
  * Wire-format-visible mangling (`In<Method>` / `Out<Method>` /
  * `<Method>Success` / `<Method>Failure`) stays in lockstep with the
  * legacy inline literals.
  */
object DomainCSNameMangling {

  /** `<MethodName>` capitalised, e.g. `ping` → `Ping`. */
  private def cap(name: String): String = name.capitalize

  /** `In<MethodName>` for the per-method input struct class. */
  def methodToInputName(m: RPCMethod): String = s"In${cap(m.name)}"

  /** `Out<MethodName>` for the per-method output struct class. */
  def methodToOutputName(m: RPCMethod): String = s"Out${cap(m.name)}"

  /** `<Method>Success` — the success branch label for `Alternative` outputs. */
  def methodToPositiveTypeName(m: RPCMethod): String = s"${cap(m.name)}Success"

  /** `<Method>Failure` — the failure branch label for `Alternative` outputs. */
  def methodToNegativeTypeName(m: RPCMethod): String = s"${cap(m.name)}Failure"

  /** Lower-case parameter name from a type id, matching legacy
    * `TypespaceToolsImpl.idToParaName`.
    */
  def idToParaName(id: TypeId): String = id.name.toLowerCase
}
