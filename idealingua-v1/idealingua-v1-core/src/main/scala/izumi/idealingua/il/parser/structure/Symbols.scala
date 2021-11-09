package izumi.idealingua.il.parser.structure

import fastparse._
import fastparse.NoWhitespace._

trait Symbols {
  def NLC[$: P]: P[Unit] = P("\r\n" | "\n" | "\r")
  def String[$: P]: P[String] = P("\"" ~ CharsWhile(c => c != '"').rep().! ~ "\"")
}
