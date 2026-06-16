package izumi.idealingua.translator.toschema

import io.circe.{Json, parser}
import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.common.Primitive
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.ProjectVersion
import izumi.idealingua.model.publishing.manifests.SchemaBuildManifest
import izumi.idealingua.translator.toschema.domain.SchemaTypeResolver
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.{Files, Paths}

/** Regression guard: no emitted JSON Schema integer bound (`minimum`/`maximum`)
  * may exceed the IEEE-754 double safe-integer range (±(2^53 - 1)); larger bounds
  * are rejected by strict consumers.
  *
  * Two complementary checks:
  *   1. Integration — drives the full emitter against `coverage.primitives`, whose
  *      `AllPrimitivesFlat` DTO exercises every integer width including signed-64.
  *   2. Exhaustive — renders EVERY primitive in the authoritative registry
  *      (`Primitive.mapping`) directly through `SchemaTypeResolver.primitiveSchema`,
  *      independent of any corpus fixture. A newly-introduced primitive must be
  *      registered there to be usable in any `.domain`, so this pulls a new numeric
  *      primitive into the bound check automatically — no fixture edit can be
  *      forgotten.
  */
final class SchemaIntegerBoundsSpec extends AnyWordSpec {

  // IEEE-754 double safe-integer ceiling (`2^53 - 1`).
  private val MaxSafeInteger: BigInt = BigInt(9007199254740991L)

  private def repoRoot = {
    val cwd                       = Paths.get(System.getProperty("user.dir"))
    var found: java.nio.file.Path = null
    var p: java.nio.file.Path     = cwd.toAbsolutePath
    while (p != null) {
      if (Files.exists(p.resolve("idealingua-v1/idealingua-v1-test-defs"))) found = p
      p = p.getParent
    }
    require(found != null, s"could not locate repo root from $cwd")
    found
  }

  private val corpusRoot = repoRoot.resolve(
    "idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source"
  )

  private val schemaManifest: SchemaBuildManifest = SchemaBuildManifest(
    common = BuildManifest.Common.example.copy(
      izumiVersion = "schema-bounds-spec",
      version      = ProjectVersion(version = "0.0.0", release = true, snapshotQualifier = "spec"),
    )
  )

  private def loadPrimitivesDomain() = {
    val context  = new LocalModelLoaderContext(Seq(corpusRoot), Seq.empty[File])
    val resolver = new ModelResolver()
    val resolved = resolver.resolve(context.loader.load())
    val pick     = resolved.successful.find(_.parsed.id.toPackage.mkString(".") == "coverage.primitives")
    require(pick.isDefined, s"coverage.primitives not found under $corpusRoot")
    pick.get
  }

  private def schemaOptions: UntypedCompilerOptions =
    UntypedCompilerOptions(
      language           = IDLLanguage.JsonSchema,
      target             = None,
      manifest           = schemaManifest,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )

  /** Every numeric `minimum`/`maximum` leaf anywhere in the document. */
  private def collectBounds(j: Json): List[BigInt] =
    j.fold(
      Nil,
      _ => Nil,
      _ => Nil,
      _ => Nil,
      arr => arr.toList.flatMap(collectBounds),
      obj =>
        obj.toList.flatMap {
          case (k, v) =>
            val here =
              if (k == "minimum" || k == "maximum") v.asNumber.flatMap(_.toBigInt).toList
              else Nil
            here ++ collectBounds(v)
        },
    )

  "JSON Schema integer bounds" should {
    "stay within the IEEE-754 double safe-integer range for every primitive width" in {
      val loaded = loadPrimitivesDomain()
      val out    = new TypespaceCompilerBaseFacade(schemaOptions).compile(Seq(loaded))

      val schemaSource = out.emodules.collectFirst {
        case ExtendedModule.DomainModule(_, m) if m.id.name == "schema.json" => m.content
      }.getOrElse(fail("no schema.json module emitted for coverage.primitives"))

      // Non-vacuity: the flat-primitives DTO must be present, else this passes without exercising the bound.
      val _ = assert(schemaSource.contains("AllPrimitivesFlat"), s"AllPrimitivesFlat schema missing: $schemaSource")

      val doc        = parser.parse(schemaSource).fold(e => fail(s"emitted schema is not valid JSON: ${e.message}"), identity)
      val bounds     = collectBounds(doc)
      val outOfRange = bounds.filter(_.abs > MaxSafeInteger)

      val _ = assert(bounds.nonEmpty, "no integer bounds found in the emitted schema")
      assert(
        outOfRange.isEmpty,
        s"emitted integer bounds exceed the safe-integer range (±$MaxSafeInteger): $outOfRange",
      )
    }

    "stay within the IEEE-754 double safe-integer range for every primitive in the registry" in {
      // Authoritative enumeration: every primitive the parser recognises. A new
      // primitive must be registered in `Primitive.mapping` to be referenceable in
      // any `.domain`, so this is the right source of truth for "all primitive
      // widths" — and renders without a corpus or a `Domain`.
      val primitives = Primitive.mapping.values.toSet

      // Non-vacuity: the registry must include the 64-bit widths whose clamp this guards.
      val _ = assert(
        primitives.contains(Primitive.TInt64) && primitives.contains(Primitive.TUInt64),
        s"expected TInt64/TUInt64 in the primitive registry, got: $primitives",
      )

      val rendered = primitives.toList.map(p => p -> SchemaTypeResolver.primitiveSchema(p))

      // Non-vacuity: at least the bounded integer widths must actually emit bounds,
      // else the offender scan below would pass without exercising anything.
      val allBounds = rendered.flatMap { case (_, frag) => collectBounds(frag) }
      val _ = assert(allBounds.nonEmpty, "no integer bounds rendered for any primitive — emitter wiring changed?")

      val offenders = rendered.flatMap {
        case (p, frag) => collectBounds(frag).filter(_.abs > MaxSafeInteger).map(b => p -> b)
      }
      assert(
        offenders.isEmpty,
        s"these primitives emit integer bounds beyond the safe-integer range (±$MaxSafeInteger): $offenders",
      )
    }
  }
}
