package izumi.idealingua.translator.toscala.products

import scala.meta.{Defn, Template}

trait RenderableCogenProduct {
  def preamble: String

  def render: List[Defn]

  def isEmpty: Boolean = render.isEmpty
}

object RenderableCogenProduct {
  def empty: RenderableCogenProduct = new RenderableCogenProduct {
    override def render: List[Defn] = List.empty

    override def preamble: String = ""
  }
}

trait UnaryCogenProduct[T <: Defn] extends RenderableCogenProduct {
  def defn: T

  /** F-TextTree M8a: hook that lets subclasses splice String-typed base
    * lists (e.g. AnyVal mixin) into the inner `Defn`'s template before
    * the carrier emits it. Default is identity — preserves the legacy
    * shape for `TraitProduct` and other non-extension carriers. */
  def defnEffective: T = defn

  override def render: List[Defn] = List(defnEffective)
}

trait MultipleCogenProduct[T <: Defn] extends UnaryCogenProduct[T] {
  def more: List[Defn]

  def defn: T

  override def render: List[Defn] = {
    super.render ++ more
  }
}

trait AccompaniedCogenProduct[T <: Defn] extends MultipleCogenProduct[T] {
  def companion: Defn.Object

  /** F-TextTree M8a: hook for String-typed top-level sibling fragments
    * (Circe trait, etc.). Parsed and spliced at carrier render time. */
  def extraSiblings: List[Defn] = List.empty

  protected def filterEmptyClasses(defns: List[Defn.Class]): List[Defn.Class] = {
    defns.filterNot(p => isEmpty(p.templ))
  }

  protected def filterEmptyObjects(defns: List[Defn.Object]): List[Defn.Object] = {
    defns.filterNot(p => isEmpty(p.templ))
  }

  private def isEmpty(t: Template): Boolean = t.body.stats.isEmpty && t.inits.isEmpty

  override def render: List[Defn] = {
    super.render ++ extraSiblings ++ filterEmptyObjects(List(companion))
  }
}
