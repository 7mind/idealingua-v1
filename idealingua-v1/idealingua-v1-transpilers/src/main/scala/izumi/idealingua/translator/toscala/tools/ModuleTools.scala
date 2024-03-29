package izumi.idealingua.translator.toscala.tools

import izumi.idealingua
import izumi.idealingua.model.common.{DomainId, TypeId}
import izumi.idealingua.model.common.TypeId.{BuzzerId, ServiceId}
import izumi.idealingua.model.il.ast.typed.TypeDef.Alias
import izumi.idealingua.model.il.ast.typed.TypeDef
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.toscala.products.RenderableCogenProduct

import scala.meta.Tree
import scala.meta.internal.prettyprinters.TreeSyntax

class ModuleTools() {
  def toSource(id: DomainId, moduleId: ModuleId, product: RenderableCogenProduct, scalaVersion: Option[String]): Seq[Module] = {
    product match {
      case p if p.isEmpty =>
        Seq.empty

      case _ =>
        val dialect         = if (scalaVersion.exists(_.startsWith("3"))) scala.meta.dialects.Scala30 else scala.meta.dialects.Scala213
        val code            = (product.preamble +: product.render.map(TreeSyntax[Tree](dialect).apply(_).toString())).mkString("\n\n")
        val content: String = withPackage(id.toPackage, code)
        Seq(Module(moduleId, content))
    }

  }

  def withPackage(pkg: idealingua.model.common.Package, code: String): String = {
    val content = if (pkg.isEmpty) {
      code
    } else {
      s"""package ${pkg.mkString(".")}
         |
         |$code
       """.stripMargin
    }
    content
  }

  def toModuleId(defn: TypeDef): ModuleId = {
    defn match {
      case i: Alias =>
        val concrete = i.id
        ModuleId(concrete.path.toPackage, s"${concrete.path.toPackage.last}.scala")

      case other =>
        toModuleId(other.id)
    }
  }

  def toModuleId(id: TypeId): ModuleId = {
    ModuleId(id.path.toPackage, s"${id.name}.scala")
  }

  def toModuleId(id: ServiceId): ModuleId = {
    ModuleId(id.domain.toPackage, s"${id.name}.scala")
  }

  def toModuleId(id: BuzzerId): ModuleId = {
    ModuleId(id.domain.toPackage, s"${id.name}.scala")
  }

}
