package izumi.idealingua.runtime.model

import java.net.{URLDecoder, URLEncoder}
import java.util.UUID


trait IDLIdentifier extends Any {
  this: IDLGeneratedType =>
}

object IDLIdentifier {
  def escape(s: String): String = Escaping.escape(s)

  def unescape(s: String): String = Escaping.unescape(s)

  def parsePart[T](v: String, clazz: Class[T]): T = {
    val ret = clazz match {
      case c if c.isAssignableFrom(classOf[String]) =>
        v
      case c if c.isAssignableFrom(classOf[Boolean]) =>
        v.toBoolean
      case c if c.isAssignableFrom(classOf[Byte]) =>
        v.toByte
      case c if c.isAssignableFrom(classOf[Short]) =>
        v.toShort
      case c if c.isAssignableFrom(classOf[Int]) =>
        v.toInt
      case c if c.isAssignableFrom(classOf[Long]) =>
        v.toLong
      case c if c.isAssignableFrom(classOf[Float]) =>
        v.toFloat
      case c if c.isAssignableFrom(classOf[Double]) =>
        v.toDouble
      case c if c.isAssignableFrom(classOf[UUID]) =>
        UUID.fromString(v)
      case t =>
        throw new IllegalArgumentException(s"Not supported by identifiers (yet?..): $v: $t => $clazz")
    }
    ret.asInstanceOf[T]
  }
}

