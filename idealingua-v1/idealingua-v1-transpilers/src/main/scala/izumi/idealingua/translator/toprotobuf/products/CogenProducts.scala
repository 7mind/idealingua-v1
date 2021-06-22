package izumi.idealingua.translator.toprotobuf.products

import izumi.idealingua.translator.toprotobuf.types.ProtobufField

object CogenProducts {
  final case class Message(name: String, fields: List[ProtobufField]) extends RenderableCogenProduct {
    override def preamble: String = {
      fields.filter(_.tpe.pkg.nonEmpty).map {
        field =>
        s"""import ${field.tpe.pkg.mkString("/")};"""
      }.mkString("\n")
    }

    override def render: List[String] = {
      val sorted = fields.sortBy(_.name).zipWithIndex.toMap
      val fieldsRendered = fields.map {
        field =>
        if (field.tpe.optional) {
          s"\t${field.tpe.name} ${field.name} = ${sorted(field)};"
        } else {
          s"\trequired ${field.tpe.name} ${field.name} = ${sorted(field)};"
        }
      }.mkString("\n")
      List(
        s"""
           |message $name {
           |$fieldsRendered
           |}
           |""".stripMargin
      )
    }
  }
}
