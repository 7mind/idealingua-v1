package izumi.idealingua.runtime.model

import java.net.{URLDecoder, URLEncoder}

object Escaping {
  // TODO: we may need to use a better escaping
  def escape(s: String): String = URLEncoder.encode(s, "UTF-8")

  def unescape(s: String): String = URLDecoder.decode(s, "UTF-8")
}
