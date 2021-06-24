package izumi.idealingua.translator.toprotobuf.tools

import izumi.idealingua
import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.toprotobuf.products.RenderableCogenProduct

class ModuleTools(configuredOptions: Map[String, String]) {

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
    val options = if(configuredOptions.nonEmpty) {
      configuredOptions.map {
        case (k, v) =>
          s"option $k = $v;"
      }.mkString("\n")
    } else {
      ""
    }
    val merged = List(preamble, options, code).filter(_.nonEmpty).mkString("\n\n")
    if (pkg.isEmpty) {
      s"""syntax = "proto2";
         |
         |// Auto-generated, any modifications may be overwritten in the future.
         |
         |$merged
         |""".stripMargin
    } else {
      s"""syntax = "proto2";
         |
         |// Auto-generated, any modifications may be overwritten in the future.
         |
         |package ${pkg.mkString(".")};
         |
         |$merged
       """.stripMargin
    }
  }

  def toModuleId(domain: DomainId): ModuleId = {
    ModuleId(domain.pkg, s"${domain.id}.proto")
  }
}
