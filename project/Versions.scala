
object V {
  // foundation

  val scalatest = "3.2.15"
  val http4s = "0.23.18"
  val http4s_blaze = "0.23.14"

  val scalameta = "4.7.7" // Not available for Scala 3 yet
  val fastparse = "3.0.1" // 3.0.0 is available for Scala 3

  val scala_xml = "2.1.0"

  // java-only dependencies below
  // java, we need it bcs http4s ws client isn't ready yet
  val asynchttpclient = "2.12.3"

  val slf4j = "1.7.30"
  val typesafe_config = "1.4.2"

  val kind_projector = "0.13.2"

  val circe_generic_extras = "0.14.3"

  val scala_java_time = "2.4.0"
}
