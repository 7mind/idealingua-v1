package izumi.idealingua.`macro`


import java.time.LocalDateTime

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/*
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:sbt-version=${sbtVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}",
      s"-Xmacro-settings:scalatest-version=${V.scalatest}",
    ),
 */
object ProjectAttributeMacro {

  def extractSbtProjectGroupId(): Option[String] = macro extractProjectGroupIdMacro

  def extractSbtProjectVersion(): Option[String] = macro extractProjectVersionMacro

  def buildTimestampMacro(c: blackbox.Context)(): c.Expr[LocalDateTime] = {
    import c.universe._
    val time = LocalDateTime.now()
    c.Expr[LocalDateTime] {
      q"{_root_.java.time.LocalDateTime.of(${time.getYear}, ${time.getMonthValue}, ${time.getDayOfMonth}, ${time.getHour}, ${time.getMinute}, ${time.getSecond}, ${time.getNano})}"
    }
  }


  def extractProjectGroupIdMacro(c: blackbox.Context)(): c.Expr[Option[String]] = {
    extractAttr(c, "product-group")
  }

  def extractProjectVersionMacro(c: blackbox.Context)(): c.Expr[Option[String]] = {
    extractAttr(c, "product-version")
  }

  private def extractAttr(c: blackbox.Context, name: String): c.Expr[Option[String]] = {
    val prefix = s"$name="
    val value = c.settings.find(_.startsWith(prefix)).map(_.stripPrefix(prefix))
    if (value.isEmpty) {
      c.warning(c.enclosingPosition, s"Undefined macro parameter $name, add `-Xmacro-settings:$prefix<value>` into `scalac` options")
    }
    import c.universe._
    c.Expr[Option[String]](q"$value")
  }
}