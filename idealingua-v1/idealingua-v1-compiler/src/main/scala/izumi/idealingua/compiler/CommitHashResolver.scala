package izumi.idealingua.compiler

import scala.sys.process.*
import scala.util.Try

object CommitHashResolver {
  private val commitPattern = """<commit(\d*)>""".r

  def resolveCommitHash(): String = {
    Try(Process(Seq("git", "rev-parse", "HEAD")).lazyLines.head.trim)
      .getOrElse("unknown")
  }

  def containsCommitTemplate(qualifier: String): Boolean =
    commitPattern.findFirstIn(qualifier).isDefined

  def resolveQualifier(template: String, commitHash: String): String = {
    commitPattern.replaceAllIn(
      template,
      m => {
        val n = Option(m.group(1)).filter(_.nonEmpty).map(_.toInt).getOrElse(7)
        scala.util.matching.Regex.quoteReplacement(commitHash.take(n))
      },
    )
  }
}
