package izumi.idealingua.il.loader

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import}
import izumi.idealingua.model.il.ast.raw.models.Inclusion
import izumi.idealingua.model.loader.FSPath

import scala.annotation.nowarn
import scala.collection.mutable

@nowarn("msg=Unused import")
private[loader] class DomainMeshResolvedMutable(
  override val id: DomainId,
  override val members: Seq[RawTopLevelDefn],
  override val origin: FSPath,
  override val directInclusions: Seq[Inclusion],
  override val imports: Seq[Import],
  override val meta: RawNodeMeta,
  refContext: mutable.Map[DomainId, DomainMeshResolved],
  requiredRefs: Set[DomainId],
) extends DomainMeshResolved {
  import scala.collection.compat.*

  override def referenced: Map[DomainId, DomainMeshResolved] = {
    refContext.view.filterKeys(requiredRefs.contains).toMap
  }

}
