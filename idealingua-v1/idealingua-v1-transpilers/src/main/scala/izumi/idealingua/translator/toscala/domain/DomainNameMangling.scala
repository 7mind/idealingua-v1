package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId.AdtId
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod

/** Pure name-mangling helpers, mirroring `TypespaceToolsImpl` (legacy)
  * verbatim. These are pure functions (no `Typespace` dependency), so the
  * new-IR Domain renderer family can rely on them without re-deriving a
  * legacy `Typespace`.
  *
  * IMPL-7a.2 Phase B M4: introduced for the ADT / Service / Buzzer
  * rendering port. All literals match the legacy `TypespaceToolsImpl`
  * constants exactly (`Output`/`Input`/`Success`/`Failure`/`Struct`/`Defn`),
  * so wire-format JSON keys and synthesized type names stay in lockstep
  * with `EphemeralSynthesizer` (Phase 7 of the new typer) and the legacy
  * translator output.
  */
object DomainNameMangling {

  val methodOutputSuffix = "Output"
  val methodInputSuffix  = "Input"

  val goodAltBranchName = "Success"
  val badAltBranchName  = "Failure"

  val goodAltSuffix = "Success"
  val badAltSuffix  = "Failure"

  def idToParaName(id: TypeId): String = id.name.toLowerCase

  def methodToOutputName(method: RPCMethod): String =
    s"${method.name.capitalize}$methodOutputSuffix"

  def methodToInputName(method: RPCMethod): String =
    s"${method.name.capitalize}$methodInputSuffix"

  def methodToPositiveTypeName(method: RPCMethod): String =
    s"${method.name.capitalize}$goodAltSuffix"

  def methodToNegativeTypeName(method: RPCMethod): String =
    s"${method.name.capitalize}$badAltSuffix"

  def toPositiveBranchName(@scala.annotation.unused id: AdtId): String =
    goodAltBranchName

  def toNegativeBranchName(@scala.annotation.unused id: AdtId): String =
    badAltBranchName
}
