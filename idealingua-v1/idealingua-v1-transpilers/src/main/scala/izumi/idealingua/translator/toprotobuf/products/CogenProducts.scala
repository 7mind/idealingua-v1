package izumi.idealingua.translator.toprotobuf.products

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.translator.toprotobuf.types.{ProtobufAdtMember, ProtobufField, ProtobufMethod, ProtobufType}

object CogenProducts {

  final case class Service(domain: DomainId, name: String, methods: List[ProtobufMethod]) extends RenderableCogenProduct {
    private lazy val cogenMessages: List[RenderableCogenProduct] = methods.flatMap(cogenMethodMessages)

    override def preamble: List[String] = {
      val self = ProtobufType.importFromPackage(domain.toPackage)
      (cogenMessages.flatMap(_.preamble).toSet -- self).toList
    }

    override def render: List[String] = {
      val methodsRendered = methods.map {
        method =>
          val inputType = methodInputType(method)
          val outputType = methodOutputType(method)
          s"\trpc ${method.name}(${inputType.fullName}) returns (${outputType.fullName});"
      }.mkString("\n")
      cogenMessages.flatMap(_.render) ++ List(
        s"""
           |service $name {
           |$methodsRendered
           |}
           |""".stripMargin
      )
    }

    private[this] def methodInputType(method: ProtobufMethod) = {
      ProtobufType(Seq.empty, s"$name${method.name.capitalize}Input")
    }

    private[this] def methodOutputType(method: ProtobufMethod) = {
      ProtobufType(Seq.empty, s"$name${method.name.capitalize}Output")
    }


    private[this] def cogenMethodMessages(method: ProtobufMethod): Seq[RenderableCogenProduct] = {
      def cogenNonAlternativeOutputs(tpe: ProtobufType, outputs: ProtobufMethod.NonAlternativeOutput): RenderableCogenProduct = {
        outputs match {
          case ProtobufMethod.Void => Message(tpe, Nil)
          case ProtobufMethod.ADT(members) => ADT(tpe, members)
          case ProtobufMethod.Single(singleTpe) => Message(tpe, List(ProtobufField("single", singleTpe)))
          case ProtobufMethod.Structure(fields) => Message(tpe, fields)
        }
      }

      val inputType = methodInputType(method)
      val outputType = methodOutputType(method)
      val input = Message(inputType, method.inputs)
      val output = method.output match {
        case na: ProtobufMethod.NonAlternativeOutput => List(cogenNonAlternativeOutputs(outputType, na))
        case ProtobufMethod.Alternative(successOut, failureOut) =>
          val successType = ProtobufType(Seq.empty, s"$name${method.name.capitalize}OutputSuccess")
          val failureType = ProtobufType(Seq.empty, s"$name${method.name.capitalize}OutputFailure")
          List(
            ADT(
              outputType,
              List(
                ProtobufAdtMember(Some("success"), successType),
                ProtobufAdtMember(Some("failure"), failureType),
              )
            ),
            cogenNonAlternativeOutputs(successType, successOut),
            cogenNonAlternativeOutputs(failureType, failureOut),
          )
      }
      input :: output
    }
  }

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
      val enumsRendered = members.map {
        member =>
          s"\t$member = ${sorted(member)};"
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
