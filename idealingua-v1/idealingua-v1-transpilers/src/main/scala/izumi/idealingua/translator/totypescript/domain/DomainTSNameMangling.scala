package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod

/** Pure name-mangling helpers for the TypeScript renderer family, mirroring
  * the literal mangling rules embedded inline in the legacy
  * `TypeScriptTranslator` (e.g. `In${m.name.capitalize}` /
  * `Out${m.name.capitalize}` / `${method}Success` / `${method}Failure` /
  * `<Method>Client` / `<Method>Dispatcher` / `<Method>Server`).
  *
  * IMPL-7b Phase B M3: introduced for the ADT / Service / Buzzer rendering
  * port. These are pure functions (no `Typespace` dependency), so the
  * new-IR Domain renderer family can rely on them without re-deriving a
  * legacy `Typespace`. Counterpart of `DomainNameMangling` on the Scala
  * side, but with the TS-specific prefix style (`In`/`Out` rather than
  * `<Name>Input`/`<Name>Output`).
  *
  * Wire-format-visible mangling (the `In`/`Out` prefixes are reflected in
  * the generated TS class names, which are then keys in the wire-format
  * envelope for service/buzzer methods) stays in lockstep with the legacy
  * inline literals.
  */
object DomainTSNameMangling {

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
    * `TypeScriptTranslationTools.idToParaName`.
    */
  def idToParaName(id: TypeId): String = id.name.toLowerCase
}
