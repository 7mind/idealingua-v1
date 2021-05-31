package izumi.idealingua.il.parser

import izumi.idealingua.model.il.ast.InputPosition
import fastparse.NoWhitespace._
import fastparse._

class DefPositions(context: IDLParserContext) {
  def positioned[T](defparser: => P[T])(implicit v: P[?]): P[(InputPosition, T)] = {
    (Index ~ defparser ~ Index).map {
      case (start, value, stop) =>
        (InputPosition.Defined(start, stop, context.file), value)
    }
  }
}
