package izumi.idealingua.translator.totypescript

import izumi.idealingua.model.common.TypeId

/** Resolved TS-side import descriptor: the source `TypeId` and the package
  * string the new-typer pipeline wants to render.
  *
  * IMPL-10c (2026-05-12): pulled out of the (now-deleted) legacy
  * `TypeScriptImports` companion so `tocsharp/domain/DomainTSImports` keeps
  * its import-record vocabulary unchanged after the legacy translator tree
  * was retired.
  */
final case class TypeScriptImport(id: TypeId, pkg: String)
