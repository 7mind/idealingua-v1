package izumi.idealingua.harness

import io.circe.Json
import io.circe.parser.parse
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade}
import izumi.idealingua.typer.ir.TypeDef
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

/** MCP tool schemas must describe data types of other domains completely.
  *
  * A data type of another domain that is reached only through an ADT branch,
  * an output branch (`!!` or `( | )`) or a generic argument
  * (`opt`/`list`/`set`/`map`) used to be emitted into `$defs` as an object with
  * no properties and `additionalProperties: false`, which rejects every value,
  * and a domain reached only that way produced a `$ref` without a `$defs` entry.
  *
  * Fixture: `defs/mcpcrossdomain`. `api.ProbeService` reaches each data type of
  * `shared` through exactly one such position, and `leaf.Deep` only through
  * `opt[Deep]` inside a `shared` type. `Plain` (a direct parameter) and the
  * local types of `api` are controls.
  */
final class McpCrossDomainSchemaSpec extends AnyFunSuite {

  private val defsRefPrefix = "#/$defs/"
  private val foreignPrefixes = Seq("mcpcrossdomain.shared.", "mcpcrossdomain.leaf.")

  test("tool schemas carry complete $defs for data types of other domains") {
    val repoRoot = HarnessCorpus.repoRootForTests()
    val loaded   = HarnessCorpus.loadCorpus(HarnessCorpus.mcpCrossDomainCorpusRoot(repoRoot))

    // wire id -> flattened field names, as computed in the type's own domain
    val expectedFields: Map[String, Set[String]] = loaded.flatMap { ld =>
      ld.domain.userTypes.values.collect {
        case dto: TypeDef.Dto => dto.id.wireId -> ld.domain.flattenedStructs(dto.id).fields.map(_.field.name).toSet
      }
    }.toMap

    val options  = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(manifest = HarnessOptions.scala.copy(emitMcpBridge = true))
    val layouted = new TypespaceCompilerBaseFacade(options).compile(loaded)
    val envelope = layouted.emodules.collectFirst {
      case ExtendedModule.DomainModule(_, m) if m.id.name == "ProbeService.mcp.json" => m.content
    }.getOrElse(fail("ProbeService.mcp.json was not emitted"))

    val tools = parse(envelope)
      .fold(e => fail(s"ProbeService.mcp.json is not JSON: ${e.message}"), identity)
      .hcursor.downField("tools").focus.flatMap(_.asArray)
      .getOrElse(fail("ProbeService.mcp.json has no tools[]"))

    val problems = mutable.ArrayBuffer.empty[String]
    val checked  = mutable.LinkedHashSet.empty[String]
    tools.foreach { tool =>
      val toolName = tool.hcursor.get[String]("name").fold(_ => fail("tool without a name"), identity)
      Seq("inputSchema", "outputSchema").foreach { kind =>
        val schema = tool.hcursor.downField(kind).focus.getOrElse(fail(s"$toolName has no $kind"))
        val defs   = schema.hcursor.downField("$defs").focus.flatMap(_.asObject).map(_.toMap).getOrElse(Map.empty[String, Json])
        refTargets(schema).foreach { target =>
          if (!target.startsWith(defsRefPrefix) || !defs.contains(target.stripPrefix(defsRefPrefix))) {
            problems += s"$toolName $kind: reference $target has no $$defs entry"
          }
        }
        defs.foreach {
          case (key, definition) =>
            expectedFields.get(key).foreach { fields =>
              checked += key
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

    val foreignTypes = expectedFields.keySet.filter(k => foreignPrefixes.exists(k.startsWith))
    assert(foreignTypes.nonEmpty, "fixture defines no foreign data types")
    assert(foreignTypes.subsetOf(checked), s"foreign data types absent from every tool schema: ${(foreignTypes -- checked).toList.sorted}")
  }

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
