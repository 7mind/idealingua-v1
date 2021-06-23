package izumi.idealingua.translator.toprotobuf.products

import izumi.idealingua.translator.toprotobuf.types.{ProtobufAdtMember, ProtobufField, ProtobufType}

object CogenProducts {

  final case class ADT(self: ProtobufType, alternatives: List[ProtobufAdtMember]) extends RenderableCogenProduct {
    override def preamble: List[String] = {
      val selfImport = self.imports
      (alternatives.flatMap(_.tpe.imports).toSet -- selfImport).toList
    }

    override def render: List[String] = {
      val sorted = alternatives.sortBy(_.name).zipWithIndex.toMap
      val adtsRendered = alternatives.map {
        alt =>
          s"\t\t${alt.tpe.fullName} ${alt.name} = ${sorted(alt) + 1};"
      }.mkString("\n")
      List(
        s"""
           |message ${self.name} {
           |  oneof adt {
           |$adtsRendered
           |  }
           |}
           |""".stripMargin
      )
    }
  }

  final case class Enum(self: ProtobufType, members: List[String]) extends RenderableCogenProduct {
    override def preamble: List[String] = Nil

    override def render: List[String] = {
      val sorted = members.sorted.zipWithIndex.toMap
      val enumsRendered = sorted.map {
        case (str, idx) =>
          s"\t$str = $idx;"
      }.mkString("\n")
      List(
        s"""
           |enum ${self.name} {
           |$enumsRendered
           |}
           |""".stripMargin
      )
    }
  }

  final case class Message(self: ProtobufType, fields: List[ProtobufField]) extends RenderableCogenProduct {
    override def preamble: List[String] = {
      val selfImport = self.imports
      (fields.flatMap(_.tpe.imports).toSet -- selfImport).toList
    }

    override def render: List[String] = {
      val sorted = fields.sortBy(_.name).zipWithIndex.toMap
      val fieldsRendered = fields.map {
        case field@ProtobufField(name, tpe@ProtobufType(_, _, _, Some(true))) =>
          s"\toptional ${tpe.fullName} $name = ${sorted(field) + 1};"
        case field@ProtobufField(name, tpe@ProtobufType(_, _, _, Some(false))) =>
          s"\trequired ${tpe.fullName} $name = ${sorted(field) + 1};"
        case field@ProtobufField(name, tpe@ProtobufType(_, _, _, None)) =>
          s"\t${tpe.fullName} $name = ${sorted(field) + 1};"
      }.mkString("\n")
      List(
        s"""
           |message ${self.name} {
           |$fieldsRendered
           |}
           |""".stripMargin
      )
    }
  }
}
