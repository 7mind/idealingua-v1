package izumi.idealingua.translator.toprotobuf.tools

import izumi.idealingua
import izumi.idealingua.model.common.TypeId.{BuzzerId, ServiceId}
import izumi.idealingua.model.common.{DomainId, TypeId}
import izumi.idealingua.model.il.ast.typed.TypeDef
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.toprotobuf.products.RenderableCogenProduct

class ModuleTools() {
  def toSource(id: DomainId, moduleId: ModuleId, product: RenderableCogenProduct): Seq[Module] = {
    product match {
      case p if p.isEmpty =>
        Seq.empty

      case _ =>
        val code = (product.preamble :: product.render).mkString("\n")
        val content: String = withPackage(id.toPackage, code)
        Seq(Module(moduleId, content))
    }
  }

  def withPackage(pkg: idealingua.model.common.Package, code: String): String = {
    val content = if (pkg.isEmpty) {
     s"""syntax = "proto3";
        |// Auto-generated, any modifications may be overwritten in the future.
        |$code
        |""".stripMargin
    } else {
      s"""syntax = "proto3";
         |// Auto-generated, any modifications may be overwritten in the future.
         |package ${pkg.mkString(".")};
         |
         |$code
       """.stripMargin
    }
    content
  }

  def toModuleId(defn: TypeDef): ModuleId = {
    toModuleId(defn.id)
  }

  def toModuleId(id: TypeId): ModuleId = {
    ModuleId(id.path.toPackage, s"${id.name}.proto")
  }

  def toModuleId(id: ServiceId): ModuleId = {
    ModuleId(id.domain.toPackage, s"${id.name}.proto")
  }

  def toModuleId(id: BuzzerId): ModuleId = {
    ModuleId(id.domain.toPackage, s"${id.name}.proto")
  }
}
