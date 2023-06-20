package izumi.idealingua.runtime.model

import scala.scalajs.js.URIUtils

object Escaping {
  // TODO: we may need to use a better escaping
  def escape(s: String): String = URIUtils.encodeURI(s)

  def unescape(s: String): String = URIUtils.decodeURI(s)
}
