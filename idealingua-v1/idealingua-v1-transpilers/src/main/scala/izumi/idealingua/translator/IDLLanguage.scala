package izumi.idealingua.translator

sealed trait IDLLanguage

object IDLLanguage {
  case object Scala extends IDLLanguage {
    override val toString: String = "scala"
  }

  case object Typescript extends IDLLanguage {
    override val toString: String = "typescript"
  }

  case object CSharp extends IDLLanguage {
    override val toString: String = "csharp"
  }

  case object JsonSchema extends IDLLanguage {
    override val toString: String = "schema"
  }

  def parse(s: String): IDLLanguage = {
    (s.trim.toLowerCase: @unchecked) match {
      case Scala.toString =>
        Scala
      case Typescript.toString =>
        Typescript
      case CSharp.toString =>
        CSharp
      case JsonSchema.toString =>
        JsonSchema
    }
  }
}
