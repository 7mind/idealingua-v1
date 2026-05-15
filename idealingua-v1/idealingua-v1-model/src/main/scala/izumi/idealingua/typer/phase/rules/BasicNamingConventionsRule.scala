package izumi.idealingua.typer.phase.rules

import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, Domain}

/** Validator rule: naming conventions for user-declared types.
  *
  * Ported from the legacy `BasicNamingConventionsRule`. Three sub-checks:
  *   1. Type name must be at least 2 characters.
  *   2. Type name must start with an upper-case letter.
  *   3. Type name must not start with a reserved prefix ("Iz", "IRT", "IDL").
  *
  * Emits `Diagnostic.BadNamingConvention` for each violation (diagnostics-mode,
  * never throws — per C8/L1).
  */
object BasicNamingConventionsRule {

  private val reservedPrefixes: Set[String] = Set("Iz", "IRT", "IDL")

  def apply(domain: Domain): Diagnostics = {
    val buf = Vector.newBuilder[Diagnostic]

    domain.userTypes.values.foreach {
      typeDef =>
        val name = typeDef.id.name
        val pos  = typeDef.meta.pos

        if (name.length < 2) {
          buf += Diagnostic.BadNamingConvention(typeDef.id, s"type name '$name' is shorter than 2 characters", pos)
        }

        if (name.nonEmpty && name.head.isLower) {
          buf += Diagnostic.BadNamingConvention(typeDef.id, s"type name '$name' does not start with an upper-case letter", pos)
        }

        reservedPrefixes.find(name.startsWith) match {
          case Some(prefix) =>
            buf += Diagnostic.BadNamingConvention(
              typeDef.id,
              s"type name '$name' starts with reserved prefix '$prefix' (reserved: ${reservedPrefixes.mkString(", ")})",
              pos,
            )
          case None =>
        }
    }

    Diagnostics(buf.result())
  }
}
