object V {
  // foundation

  val scalatest = "3.2.19"

  val http4s       = "0.23.34"
  val http4s_blaze = "0.23.17"

  val scalameta = "4.16.2"
  val fastparse = "3.1.1" // 3.0.0 is available for Scala 3

  val scala_xml = "2.4.0"

  val kind_projector = "0.13.4"

  val circe_derivation     = "0.13.0-M5"
  val circe_generic_extras = "0.14.5-RC1" // 0.14.4 has no Scala 3 artifact

  val scala_java_time = "2.6.0"

  // java-only dependencies below
  // java, we need it bcs http4s ws client isn't ready yet
  val asynchttpclient = "3.0.10"

  val slf4j           = "1.7.30"
  val typesafe_config = "1.4.8"

  val scodec_bits = "1.1.38"

  // PR-04 IMPL-MCP-M5: JSON Schema 2020-12 validator (test-scope only).
  val json_schema_validator = "3.0.3"
}
