package izumi.idealingua.il.parser.structure

import fastparse.NoWhitespace._
import fastparse._

trait Separators extends Comments {
  private def ws[$: P]: P[Unit] = P(" " | "\t")

  def wss[$: P]: P[Unit] = P(ws.rep)

  private def WsComment[$: P]: P[Unit] = P(wss ~ MultilineComment ~ wss)

  private def SepLineBase[$: P]: P[Unit] = P(NLC | (WsComment ~ NLC | (wss ~ ShortComment)))

  def inline[$: P]: P[Unit] = P(WsComment | wss)

  def any[$: P]: P[Unit] = P(wss ~ (WsComment | SepLineBase).rep ~ wss)

  def sepStruct[$: P]: P[Unit] = P(any ~ (";" | "," | SepLineBase | any) ~ any)

  def sepEnum[$: P]: P[Unit] = P((ws.rep(1) | NLC | WsComment | (wss ~ ShortComment)).rep(1))

  def sepAdt[$: P]: P[Unit] = P(sepEnum)

  def sepEnumFreeForm[$: P]: P[Unit] = P(any ~ ("|" | ";" | ",") ~ any)

  def sepAdtFreeForm[$: P]: P[Unit] = P(sepEnumFreeForm)

}
