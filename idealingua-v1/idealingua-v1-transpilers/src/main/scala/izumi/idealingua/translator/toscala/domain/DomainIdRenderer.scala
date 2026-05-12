package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.PrimitiveId
import izumi.idealingua.model.common.TypeId.{EnumId, IdentifierId}
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.translator.toscala.products.{CogenProduct, RenderableCogenProduct}
import izumi.idealingua.translator.toscala.types.ScalaField
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta._

/** Renders a new-IR `TypeDef.Identifier` as the same scala.meta `Defn`s the
  * legacy `IdRenderer.renderIdentifier` produces (modulo the extension
  * chain).
  *
  * IMPL-7a.2 Phase B M3 (relaxed parity): produces structurally correct
  * Scala — final case class with the identifier's fields, companion with a
  * `parse(String)` method, tools-class for the extension chain. Field
  * sorting matches the legacy renderer (`sortBy(_.field.field.name)` for
  * parsers; declaration order for `toString`).
  *
  * Inputs come directly off `TypeDef.Identifier.fields: List[IdField]` —
  * identifiers have no inheritance, so no flat-struct lookup is needed.
  *
  * The legacy renderer reads `typespace.structure.structure(i).toScala`,
  * which for `Identifier` produces a `PlainScalaStruct` over the identifier's
  * fields (each `IdField` is widened to a `Field`). We do the same widening
  * inline.
  */
final class DomainIdRenderer(ctx: DomainSTContext) {
  import izumi.idealingua.translator.toscala.types.ScalaField._
  import ctx.conv._

  def renderIdentifier(i: NewTypeDef.Identifier): RenderableCogenProduct = {
    val typeName = i.id.name

    val scalaFields: List[ScalaField] = i.fields.map { idf =>
      val widened = idfieldToField(idf)
      ScalaField(
        Term.Name(idf.name),
        ctx.conv.toScala(idf.typeId).typeFull,
        izumi.idealingua.model.common.ExtendedField(
          field = widened,
          defn  = izumi.idealingua.model.common.FieldDef(
            definedBy        = i.id,
            definedWithIndex = 0,
            usedBy           = i.id,
            distance         = 0,
          ),
        ),
      )
    }

    val decls = scalaFields.toParams

    val interp = Term.Interpolate(
      Term.Name("s"),
      List(Lit.String(typeName + "#"), Lit.String("")),
      List(Term.Name("suffix")),
    )

    val t     = ctx.conv.toScala(i.id)
    val tools = t.within(s"${i.id.name}Extensions")

    val qqTools = q"""implicit class ${tools.typeName}(_value: ${t.typeFull}) { }"""

    val sortedFields = scalaFields.sortBy(_.field.field.name)

    val parsers = sortedFields.zipWithIndex.map {
      case (field, idx) =>
        field.field.field.typeId match {
          case t: EnumId =>
            q"${field.name} = ${ctx.conv.toScala(t).termFull}.parse(parts(${Lit.Int(idx)}))"
          case t: IdentifierId =>
            q"${field.name} = ${ctx.conv.toScala(t).termFull}.parse(parts(${Lit.Int(idx)}))"
          case _: PrimitiveId =>
            q"${field.name} = parsePart[${field.fieldType}](parts(${Lit.Int(idx)}), classOf[${field.fieldType}])"
          case o =>
            throw new IDLException(s"Impossible case/id field: $o")
        }
    }

    val parts = sortedFields.map(fi => q"this.${fi.name}")

    val superClasses = List(ctx.rt.generated.init(), ctx.rt.tIDLIdentifier.init())

    val errorInterp = Term.Interpolate(
      Term.Name("s"),
      List(Lit.String("Serialized form of "), Lit.String(s" should start with $typeName#")),
      List(Term.Name("name")),
    )

    val qqCompanion =
      q"""object ${t.termName} {
            def parse(s: String): ${t.typeName} = {
              import ${ctx.rt.tIDLIdentifier.termBase}._
              if (!s.startsWith(${Lit.String(typeName.toString + "#")})) {
                val name = ${Lit.String(i.id.toString)}
                throw new IllegalArgumentException($errorInterp)
              }
              val withoutPrefix = s.substring(s.indexOf("#") + 1)
              val parts = withoutPrefix.split(':').map(part => unescape(part))
              ${t.termName}(..$parsers)
            }
      }"""

    val qqIdentifier =
      q"""final case class ${t.typeName} (..$decls) extends ..$superClasses {
            override def toString: String = {
              import ${ctx.rt.tIDLIdentifier.termBase}._
              val suffix = Seq(..$parts).map(part => escape(part.toString)).mkString(":")
              $interp
            }
         }"""

    // No extension hook in M3 — the legacy `ctx.ext.extend(...)` chain
    // requires a legacy `STContext`; M2/M3 deliberately keep the new
    // renderer at the pre-extension layer. Production swap (M6) will
    // reintegrate via a legacy-IR adapter at the extension boundary.
    CogenProduct(qqIdentifier, qqCompanion, qqTools, List.empty)
  }

  private def idfieldToField(idf: IdField): Field = Field(idf.typeId, idf.name, idf.meta)
}
