package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.TypeId.ConstId
import izumi.idealingua.model.il.ast.typed.{ConstValue, NodeMeta}

/** A type-checked constant, produced by Phase 8 (ConstValueTyper).
  *
  * In the legacy typer the three `translateValue` branches at
  * `IDLTyper.scala:240, 245, 250` are stubs (`// TODO: verify structure`).
  * Phase 8 closes those gaps: every constant's value is type-checked against
  * its declared `ConstId` and stored here as a `ConstValue`.
  *
  * `Domain.consts` is a `List` (not a `Set`) to preserve const-block
  * declaration order (see master plan §4 "Field-ordering invariant").
  *
  * @param id    Fully-qualified identifier for this constant.
  * @param value Type-checked value (see `ConstValue` for the value ADT).
  * @param meta  Source position, doc comment, annotations.
  */
final case class Const(id: ConstId, value: ConstValue, meta: NodeMeta)
