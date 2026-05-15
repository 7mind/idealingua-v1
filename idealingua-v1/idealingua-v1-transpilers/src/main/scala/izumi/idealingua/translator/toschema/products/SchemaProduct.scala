package izumi.idealingua.translator.toschema.products

import io.circe.Json

/** Internal product produced by `DomainSchemaTranslator` before the
  * `SchemaLayouter` materialises it as a `Module`.
  *
  * @param path     Relative path components of the emitted file (joined with `/`).
  * @param fileName Bare filename (e.g. `schema.json`).
  * @param doc      Top-level JSON document (an OpenAPI 3.1 envelope at M1).
  */
final case class SchemaProduct(path: Seq[String], fileName: String, doc: Json)
