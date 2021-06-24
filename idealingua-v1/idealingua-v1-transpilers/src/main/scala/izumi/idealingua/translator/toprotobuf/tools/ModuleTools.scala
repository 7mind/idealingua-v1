package izumi.idealingua.translator.toprotobuf.tools

import izumi.idealingua
import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.common.TypeId.ServiceId
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.toprotobuf.products.RenderableCogenProduct

class ModuleTools() {

  def toSource(id: DomainId, moduleId: ModuleId, products: Seq[RenderableCogenProduct]): Seq[Module] = {
    products match {
      case p if p.isEmpty =>
        Seq.empty

      case _ =>
        val preamble = products.flatMap(_.preamble).distinct.mkString("\n")
        val code = products.flatMap(_.render).distinct.mkString("\n")
        val content: String = withPackage(id.toPackage, preamble, code)
        Seq(Module(moduleId, content))
    }
  }

  def withPackage(pkg: idealingua.model.common.Package, preamble: String, code: String): String = {
    val content = if (pkg.isEmpty) {
      s"""syntax = "proto2";
         |
         |// Auto-generated, any modifications may be overwritten in the future.
         |$preamble
         |
         |$code
         |""".stripMargin
    } else {
      s"""syntax = "proto2";
         |
         |// Auto-generated, any modifications may be overwritten in the future.
         |
         |package ${pkg.mkString(".")};
         |
         |$preamble
         |
         |$code
       """.stripMargin
    }
    content
  }

  def toModuleId(domain: DomainId): ModuleId = {
    ModuleId(domain.pkg, s"${domain.id}.proto")
  }

  def toModuleId(serviceId: ServiceId): ModuleId = {
    ModuleId(serviceId.domain.toPackage, s"${serviceId.name}.proto")
  }
}
