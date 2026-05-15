package regression_harness

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.Try

/** NDJSON canonicalization per plan §8.
 *
 *  Input:  one TSV line per sample, `wireId\tscenario\tjson`.
 *  Output: same lines, with `json` re-emitted in a canonical form
 *          (keys sorted recursively, no whitespace), sorted by (wireId, scenario).
 *
 *  This is intentionally implemented without an external JSON dependency: the
 *  harness itself is a scala-cli script and we want zero non-stdlib deps in
 *  Harness.scala. The sample-app side uses circe (full library); here we only
 *  need a re-parser + canonical re-emitter.
 */
object Canonicalize {

  private val LineRx = """^([A-Za-z0-9_.]+)\t([A-Za-z0-9_-]+)\t(.+)$""".r

  case class Entry(wireId: String, scenario: String, json: String)

  def canonicalize(in: Path, out: Path): Either[String, Int] = scala.util.boundary {
    if (!Files.isRegularFile(in)) scala.util.boundary.break(Left(s"missing input: $in"))
    val src = new String(Files.readAllBytes(in), StandardCharsets.UTF_8)

    val parsed = scala.collection.mutable.ArrayBuffer.empty[Entry]
    var lineNo = 0
    for (raw <- src.linesIterator) {
      lineNo += 1
      val line = raw.stripLineEnd
      if (line.nonEmpty) {
        line match {
          case LineRx(w, s, j) =>
            parseJson(j) match {
              case Right(node) =>
                parsed += Entry(w, s, emit(node))
              case Left(err) =>
                scala.util.boundary.break(Left(s"line $lineNo: invalid json: $err — raw=$line"))
            }
          case _ =>
            scala.util.boundary.break(Left(s"line $lineNo: malformed (expect wireId\\tscenario\\tjson): $line"))
        }
      }
    }

    val sorted = parsed.sortBy(e => (e.wireId, e.scenario))
    val sb     = new StringBuilder
    sorted.foreach { e =>
      sb.append(e.wireId).append('\t').append(e.scenario).append('\t').append(e.json).append('\n')
    }
    Files.write(out, sb.toString.getBytes(StandardCharsets.UTF_8))
    Right(sorted.size)
  }

  // ---- minimal JSON AST + parser + canonical emitter ----

  sealed trait Node
  case object JNull                              extends Node
  case class  JBool  (v: Boolean)                extends Node
  case class  JNum   (raw: String)               extends Node
  case class  JStr   (s: String)                 extends Node
  case class  JArr   (items: Vector[Node])       extends Node
  case class  JObj   (kvs: Vector[(String,Node)]) extends Node

  def parseJson(s: String): Either[String, Node] = {
    val p = new JsonParser(s)
    try {
      val n = p.parseValue()
      p.skipWs()
      if (!p.eof) Left(s"trailing chars at offset ${p.pos}")
      else        Right(n)
    } catch { case e: RuntimeException => Left(e.getMessage) }
  }

  private final class JsonParser(s: String) {
    var pos    = 0
    def eof    = pos >= s.length
    def peek() = s.charAt(pos)
    def get()  = { val c = s.charAt(pos); pos += 1; c }
    def skipWs(): Unit = while (!eof && { val c = peek(); c == ' ' || c == '\t' || c == '\n' || c == '\r' }) pos += 1
    def expect(c: Char): Unit = {
      if (eof || get() != c) throw new RuntimeException(s"expected '$c' at offset $pos")
    }
    def parseValue(): Node = {
      skipWs()
      if (eof) throw new RuntimeException("unexpected EOF")
      peek() match {
        case '{' => parseObj()
        case '[' => parseArr()
        case '"' => JStr(parseStr())
        case 't' | 'f' => parseBool()
        case 'n' => parseNull()
        case c if c == '-' || c.isDigit => parseNum()
        case c   => throw new RuntimeException(s"unexpected '$c' at offset $pos")
      }
    }
    def parseObj(): JObj = {
      expect('{'); skipWs()
      val kvs = Vector.newBuilder[(String, Node)]
      if (!eof && peek() == '}') { pos += 1; return JObj(kvs.result()) }
      var more = true
      while (more) {
        skipWs()
        val k = parseStr()
        skipWs(); expect(':')
        val v = parseValue()
        kvs += (k -> v)
        skipWs()
        if (eof) throw new RuntimeException("unexpected EOF in object")
        peek() match {
          case ',' => pos += 1
          case '}' => pos += 1; more = false
          case c   => throw new RuntimeException(s"expected , or } got '$c' at $pos")
        }
      }
      JObj(kvs.result())
    }
    def parseArr(): JArr = {
      expect('['); skipWs()
      val xs = Vector.newBuilder[Node]
      if (!eof && peek() == ']') { pos += 1; return JArr(xs.result()) }
      var more = true
      while (more) {
        xs += parseValue()
        skipWs()
        if (eof) throw new RuntimeException("unexpected EOF in array")
        peek() match {
          case ',' => pos += 1
          case ']' => pos += 1; more = false
          case c   => throw new RuntimeException(s"expected , or ] got '$c' at $pos")
        }
      }
      JArr(xs.result())
    }
    def parseStr(): String = {
      expect('"')
      val sb = new StringBuilder
      while (!eof && peek() != '"') {
        val c = get()
        if (c == '\\') {
          val n = get()
          n match {
            case '"'  => sb.append('"')
            case '\\' => sb.append('\\')
            case '/'  => sb.append('/')
            case 'b'  => sb.append('\b')
            case 'f'  => sb.append('\f')
            case 'n'  => sb.append('\n')
            case 'r'  => sb.append('\r')
            case 't'  => sb.append('\t')
            case 'u'  =>
              val hex = s.substring(pos, pos + 4); pos += 4
              sb.append(Integer.parseInt(hex, 16).toChar)
            case other => throw new RuntimeException(s"bad escape \\$other at $pos")
          }
        } else sb.append(c)
      }
      if (eof) throw new RuntimeException("unterminated string")
      pos += 1 // consume closing quote
      sb.toString
    }
    def parseBool(): JBool = {
      if (s.startsWith("true", pos))  { pos += 4; JBool(true) }
      else if (s.startsWith("false", pos)) { pos += 5; JBool(false) }
      else throw new RuntimeException(s"bad literal at $pos")
    }
    def parseNull(): Node = {
      if (s.startsWith("null", pos)) { pos += 4; JNull }
      else throw new RuntimeException(s"bad literal at $pos")
    }
    def parseNum(): JNum = {
      val start = pos
      if (peek() == '-') pos += 1
      while (!eof && peek().isDigit) pos += 1
      if (!eof && peek() == '.') { pos += 1; while (!eof && peek().isDigit) pos += 1 }
      if (!eof && (peek() == 'e' || peek() == 'E')) {
        pos += 1
        if (!eof && (peek() == '+' || peek() == '-')) pos += 1
        while (!eof && peek().isDigit) pos += 1
      }
      JNum(s.substring(start, pos))
    }
  }

  /** Canonical emit: keys sorted lexicographically, arrays preserve order, no whitespace. */
  def emit(n: Node): String = {
    val sb = new StringBuilder
    emitInto(n, sb)
    sb.toString
  }
  private def emitInto(n: Node, sb: StringBuilder): Unit = n match {
    case JNull       => sb.append("null")
    case JBool(true) => sb.append("true")
    case JBool(false)=> sb.append("false")
    case JNum(raw)   => sb.append(raw)
    case JStr(s)     => emitStr(s, sb)
    case JArr(xs)    =>
      sb.append('[')
      var first = true
      xs.foreach { x => if (!first) sb.append(','); emitInto(x, sb); first = false }
      sb.append(']')
    case JObj(kvs)   =>
      sb.append('{')
      var first = true
      kvs.sortBy(_._1).foreach { case (k, v) =>
        if (!first) sb.append(',')
        emitStr(k, sb); sb.append(':'); emitInto(v, sb); first = false
      }
      sb.append('}')
  }
  private def emitStr(s: String, sb: StringBuilder): Unit = {
    sb.append('"')
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      c match {
        case '"'  => sb.append("\\\"")
        case '\\' => sb.append("\\\\")
        case '\b' => sb.append("\\b")
        case '\f' => sb.append("\\f")
        case '\n' => sb.append("\\n")
        case '\r' => sb.append("\\r")
        case '\t' => sb.append("\\t")
        case x if x < 0x20 => sb.append("\\u%04x".format(x.toInt))
        case x    => sb.append(x)
      }
      i += 1
    }
    sb.append('"')
  }
}
