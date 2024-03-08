package izumi.idealingua.il.parser.structure

import fastparse._, NoWhitespace._


trait Keywords extends Separators {
  def kw[$: P](s: String): P[Unit] = P(s ~ inline) //(sourcecode.Name(s"`$s`"))

  def kw[$: P](s: String, alt: String*): P[Unit] = {
    def alts = alt.foldLeft(P(s)) { case (acc, v) => acc | v }
    P(alts ~ inline) //(sourcecode.Name(s"`$s | $alt`"))
  }

  def domain[$: P]: P[Unit] = kw("domain", "package", "namespace")
  def include[$: P]: P[Unit] = kw("include")
  def `import`[$: P]: P[Unit] = kw("import")

  def `enum`[$: P]: P[Unit] = kw("enum")
  def adt[$: P]: P[Unit] = kw("adt", "choice")
  def alias[$: P]: P[Unit] = kw("alias", "type", "using")
  def newtype[$: P]: P[Unit] = kw("clone", "newtype", "copy")
  def id[$: P]: P[Unit] = kw("id")
  def mixin[$: P]: P[Unit] = kw("mixin", "interface")
  def data[$: P]: P[Unit] = kw("data", "dto", "struct")
  def foreign[$: P]: P[Unit] = kw("foreign")
  def service[$: P]: P[Unit] = kw("service", "server")
  def buzzer[$: P]: P[Unit] = kw("buzzer", "sender")
  def streams[$: P]: P[Unit] = kw("streams", "tunnel", "pump")
  def consts[$: P]: P[Unit] = kw("const", "values")

  def declared[$: P]: P[Unit] = kw("declared")

  def defm[$: P]: P[Unit] = kw("def", "fn", "fun", "func")
  def defe[$: P]: P[Unit] = kw("line", "event")
  def upstream[$: P]: P[Unit] = kw("toserver", "up", "upstream")
  def downstream[$: P]: P[Unit] = kw("toclient", "down", "downstream")

  def apply[T](kw: => P[Unit], defparser: => P[T])(implicit v: P[?]): P[T] = {
    P(kw ~/ defparser)
  }

}
