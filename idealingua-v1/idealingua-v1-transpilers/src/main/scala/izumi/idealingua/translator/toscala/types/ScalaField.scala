package izumi.idealingua.translator.toscala.types

import izumi.idealingua.model.common.ExtendedField

import scala.collection.compat._
import scala.meta.{Term, Type}

final case class ScalaField(name: Term.Name, fieldType: Type, field: ExtendedField)

object ScalaField {
  implicit class ScalaFieldsExt(fields: IterableOnce[ScalaField]) {
    def toParams: List[Term.Param] = fields.iterator.map(f => (f.name, f.fieldType)).toParams

    def toNames: List[Term.Name] = fields.iterator.map(_.name).iterator.toList
  }

  implicit class NamedTypeExt(fields: IterableOnce[(Term.Name, Type)]) {
    def toParams: List[Term.Param] = fields.iterator.map(f => (f._1, f._2)).map(toParam).toList
  }

  private def toParam(p: (Term.Name, Type)): Term.Param = {
    Term.Param(List.empty, p._1, Some(p._2), None)
  }
}
