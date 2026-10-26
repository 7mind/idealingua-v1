package izumi.idealingua.harness

import io.circe.Json
import io.circe.parser.parse
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}
import izumi.idealingua.typer.ir.TypeDef
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

/** An ADT alternative must be described by the schema of the type it carries.
  *
  * The alternatives of an ADT used to be emitted as a `$ref` to the
  * alternative's own type id. For an alternative that names an alias the
  * reference pointed at the alias id, which has no definition (aliases are
  * dealiased everywhere else), so it resolved nowhere and the alias target was
  * missing from MCP `$defs`. In `schema.json` the same happened to the ADTs
  * synthesized for `( | )` and `!!` outputs, and the generic and primitive
  * alternatives of `!!` outputs got references such as
  * `#/components/schemas//.lst`.
  *
  * Fixture: `defs/branchrefs`. Each method of `api.ProbeService` puts one kind
  * of type into an ADT alternative.
  */
final class AdtBranchSchemaSpec extends AnyFunSuite {

  private val defsRefPrefix       = "#/$defs/"
  private val componentsRefPrefix = "#/components/schemas/"
  private val stringSchema        = Json.obj("type" -> Json.fromString("string"))

  private val loaded = HarnessCorpus.loadCorpus(HarnessCorpus.branchRefsCorpusRoot(HarnessCorpus.repoRootForTests()))

  test("MCP tool schemas describe ADT alternatives that name aliases") {
    // wire id -> flattened field names, as computed in the type's own domain
    val expectedFields: Map[String, Set[String]] = loaded.flatMap { ld =>
      ld.domain.userTypes.values.collect {
        case dto: TypeDef.Dto => dto.id.wireId -> ld.domain.flattenedStructs(dto.id).fields.map(_.field.name).toSet
      }
    }.toMap

    val options  = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(manifest = HarnessOptions.scala.copy(emitMcpBridge = true))
    val envelope = emitted(options, "ProbeService.mcp.json").headOption.getOrElse(fail("ProbeService.mcp.json was not emitted"))
    val tools    = envelope.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(fail("ProbeService.mcp.json has no tools[]"))

    val problems  = mutable.ArrayBuffer.empty[String]
    val described = mutable.LinkedHashSet.empty[String]
    val allDefs   = mutable.LinkedHashMap.empty[String, Json]
    tools.foreach { tool =>
      val toolName = tool.hcursor.get[String]("name").fold(_ => fail("tool without a name"), identity)
      Seq("inputSchema", "outputSchema").foreach { kind =>
        val schema = tool.hcursor.downField(kind).focus.getOrElse(fail(s"$toolName has no $kind"))
        val defs   = objectAt(schema, "$defs")
        refTargets(schema).foreach { target =>
          if (!target.startsWith(defsRefPrefix) || !defs.contains(target.stripPrefix(defsRefPrefix))) {
            problems += s"$toolName $kind: reference $target has no $$defs entry"
          }
        }
        defs.foreach {
          case (key, definition) =>
            allDefs.update(key, definition)
            expectedFields.get(key).foreach { fields =>
              described += key
              val properties = definition.hcursor.downField("properties").keys.map(_.toSet).getOrElse(Set.empty[String])
              if (properties != fields) {
                problems += s"$toolName $kind: $$defs[$key] has properties ${properties.toList.sorted.mkString("[", ", ", "]")}, " +
                s"the type has fields ${fields.toList.sorted.mkString("[", ", ", "]")}"
              }
            }
        }
      }
    }

    assert(problems.isEmpty, problems.mkString("\n", "\n", ""))
    assert(expectedFields.keySet.subsetOf(described), s"data types absent from every tool schema: ${(expectedFields.keySet -- described).toList.sorted}")

    val primitiveAdt = allDefs.getOrElse("branchrefs.api.PrimitiveAliasChoice", fail("PrimitiveAliasChoice is absent from every tool schema"))
    assert(branch(primitiveAdt, "label") == stringSchema)
  }

  test("schema.json references from ADT alternatives resolve") {
    val documents = emitted(HarnessOptions.optionsFor(IDLLanguage.JsonSchema), "schema.json").map(objectAt(_, "components", "schemas"))
    assert(documents.size == loaded.size, s"expected one schema.json per domain, got ${documents.size}")

    // A reference to a type of another domain points at a component of that
    // domain's document, so references are resolved against every document.
    val components = documents.flatMap(_.keySet).toSet
    val problems   = mutable.ArrayBuffer.empty[String]
    documents.foreach(_.foreach {
      case (name, schema) =>
        refTargets(schema).foreach { target =>
          if (!target.startsWith(componentsRefPrefix) || !components.contains(target.stripPrefix(componentsRefPrefix))) {
            problems += s"$name: reference $target has no component"
          }
        }
    })
    assert(problems.isEmpty, problems.mkString("\n", "\n", ""))

    val api = documents.find(_.contains("branchrefs.api.ProbeService.PrimitiveAlternativeOutput")).getOrElse(fail("branchrefs.api schema.json was not emitted"))
    assert(branch(api("branchrefs.api.ProbeService.PrimitiveAlternativeOutput"), "Success") == stringSchema)
    assert(
      branch(api("branchrefs.api.ProbeService.GenericAlternativeOutput"), "Success") ==
      Json.obj("type" -> Json.fromString("array"), "items" -> stringSchema)
    )
  }

  private def emitted(options: UntypedCompilerOptions, moduleName: String): Seq[Json] = {
    new TypespaceCompilerBaseFacade(options).compile(loaded).emodules.collect {
      case ExtendedModule.DomainModule(_, m) if m.id.name == moduleName =>
        parse(m.content).fold(e => fail(s"${m.id.path.mkString("/")}/$moduleName is not JSON: ${e.message}"), identity)
    }
  }

  private def objectAt(json: Json, path: String*): Map[String, Json] =
    path.foldLeft(json.hcursor.focus)((cur, key) => cur.flatMap(_.hcursor.downField(key).focus))
      .flatMap(_.asObject).map(_.toMap).getOrElse(Map.empty[String, Json])

  /** The schema of the alternative `discriminator` inside an ADT schema. */
  private def branch(adt: Json, discriminator: String): Json =
    adt.hcursor.downField("oneOf").focus.flatMap(_.asArray).getOrElse(fail(s"not an ADT schema: ${adt.noSpaces}"))
      .flatMap(_.hcursor.downField("properties").downField(discriminator).focus)
      .headOption.getOrElse(fail(s"no alternative $discriminator in ${adt.noSpaces}"))

  private def refTargets(json: Json): Vector[String] = json.fold(
    Vector.empty,
    _ => Vector.empty,
    _ => Vector.empty,
    _ => Vector.empty,
    _.flatMap(refTargets),
    _.toVector.flatMap {
      case ("$ref", value) => value.asString.toVector
      case (_, value)      => refTargets(value)
    },
  )
}
