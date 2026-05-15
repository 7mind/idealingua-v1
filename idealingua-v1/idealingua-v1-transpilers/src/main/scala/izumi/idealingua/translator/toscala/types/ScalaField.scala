package izumi.idealingua.translator.toscala.types

import izumi.idealingua.model.common.ExtendedField

import scala.collection.compat._

/** Field projection used by the Scala translator scaffolders.
  *
  * F-TextTree M8e: `ScalaField` is now String-native — `name` and
  * `fieldType` carry pre-rendered Scala 3 source text rather than
  * `scala.meta.Term.Name` / `scala.meta.Type`. The Scala-3 keyword escape
  * (e.g. `package` → `` `package` ``) is applied at construction site via
  * `DomainScalaParseBack.renderS30(Term.Name(_))` — see
  * `DomainScalaStruct.scalaStruct` and `ScalaTypeConverter.toScalaField`.
  *
  *   - `name`: bare identifier (raw field name, untouched — consumers that
  *     need keyword-safety read `nameSafe`).
  *   - `nameSafe`: Scala 3 keyword-escaped form, suitable for direct splice
  *     in declaration / call sites.
  *   - `fieldType`: rendered `typeFull` text (e.g. `_root_.scala.String`,
  *     `Option[Int]`, `idltest.Foo`).
  */
final case class ScalaField(name: String, nameSafe: String, fieldType: String, field: ExtendedField)

object ScalaField {
  implicit class ScalaFieldsExt(fields: IterableOnce[ScalaField]) {
    /** `name: Type` parameter strings (Scala 3 keyword-safe names). */
    def toParams: List[String] = fields.iterator.map(f => s"${f.nameSafe}: ${f.fieldType}").toList

    /** Bare identifier list (Scala 3 keyword-safe names). */
    def toNames: List[String] = fields.iterator.map(_.nameSafe).toList
  }
}
