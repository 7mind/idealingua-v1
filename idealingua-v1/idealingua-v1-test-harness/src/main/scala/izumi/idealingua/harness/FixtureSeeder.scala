package izumi.idealingua.harness

import io.circe.syntax._
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.time._
import java.util.UUID

/**
  * One-shot seeder: constructs typed Scala values for each fixture and writes
  * Json.noSpaces-encoded bytes to wire-fixtures/scala/<wireId>/<scenario>.json.
  *
  * Run via: sbt 'runMain izumi.idealingua.harness.FixtureSeederMain <repoRoot>'
  *
  * Design: keep this file as a permanent diagnostic tool — it documents the
  * typed values that produced each fixture.
  */
object FixtureSeeder {

  // Bring encoder implicits into scope — same wildcard imports as WireDispatch.
  import idltest.dtofields.Point._
  import idltest.dtofields.NullableObj._
  import idltest.dtofields.OptionalObj._
  import idltest.dtofields.ListObj._
  import idltest.identifiers.ComplexID._
  import idltest.identifiers.UserId._
  import idltest.identifiers.BucketID._
  import idltest.identifiers.KVIDGeneric._
  import idltest.identifiers.DepartmentEnum._
  import idltest.identifiers.UserWithEnumId._
  import idltest.algebraics.AdtTester._
  import idltest.algebraics.AdtWithInterface._
  import idltest.algebraics.ComplexAdt._
  import idltest.algebraics.ComplexAdt2._
  import idltest.algebraics.AdtTestID._
  import idltest.algebraics.AFace._
  import idltest.algebraics.Success._
  import idltest.inheritance.WithCovariance._
  import idltest.inheritance.Empty._
  import idltest.inheritance.Covariant._
  import idltest.json.JSONLike._
  import idltest.json.JLString._
  import idltest.json.JLNull._
  import idltest.phase.Name_incoming._
  import idltest.events.EnumType._
  import idltest.events.BranchA._
  import idltest.events.ADTType._
  import idltest.services.TestService._
  import idltest.events.TestBuzzer._
  import izumi.test.domain01.AllTypes._
  import izumi.test.domain01.GoAliasEnumTest._

  private val fixedUuid1 = UUID.fromString("3a7f0c12-1234-5678-9abc-fedcba987654")
  private val fixedUuid2 = UUID.fromString("0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9")
  private val fixedUuid3 = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffaaaabbbb")

  // Fixed timestamp in UTC with ms precision. The encoder converts to UTC and formats with
  // ISO_ZONED_DATE_TIME_3NANO (which appends +00:00 for UTC offset datetimes).
  // Use 2025-01-15T10:30:45.000Z
  private val fixedTs      = ZonedDateTime.of(2025, 1, 15, 10, 30, 45, 0, ZoneOffset.UTC)
  private val fixedTsLocal = LocalDateTime.of(2025, 1, 15, 10, 30, 45, 0)
  private val fixedDate    = LocalDate.of(2025, 1, 15)
  private val fixedTime    = LocalTime.of(10, 30, 45, 0)

  // Fixtures: (wireId, scenario, encodedJson)
  // Each entry's encodedJson is obtained by constructing the typed value and calling .asJson.noSpaces
  def buildAll(): Seq[(String, String, String)] = {
    val buf = Seq.newBuilder[(String, String, String)]

    // ---- 1. Plain DTO mixed scalars — basic ----
    buf += (("idltest.dtofields.Point", "basic",
      idltest.dtofields.Point(w = 10, h = 20, id = "abc", name = "point-1", x = 3, y = 4, ownfield = "of", `export` = true).asJson.noSpaces
    ))

    // ---- 30. Point extra fixture (different values, same wireId) ----
    buf += (("idltest.dtofields.Point", "extra",
      idltest.dtofields.Point(w = 0, h = 100, id = "xyz", name = "origin", x = 0, y = 0, ownfield = "", `export` = false).asJson.noSpaces
    ))

    // ---- 2/4. Identifier multi-field unnamed (ComplexID) ----
    // Parts in toString order: bucket, i32, str, uid, user (sorted alphabetically by field name)
    // Parse order from parse() method: parts(0)=bucket, parts(1)=i32, parts(2)=str, parts(3)=uid, parts(4)=user
    // BucketID toString: BucketID#<app>:<bucket>:<user>
    // UserWithEnumId toString: UserWithEnumId#<company>:<dept>:<value>
    val bucketForComplex = idltest.identifiers.BucketID(app = fixedUuid1, user = fixedUuid2, bucket = "test-bucket")
    val userForComplex   = idltest.identifiers.UserWithEnumId(
      value   = fixedUuid1,
      company = fixedUuid2,
      dept    = idltest.identifiers.DepartmentEnum.Engineering,
    )
    val complexId = idltest.identifiers.ComplexID(
      bucket = bucketForComplex,
      user   = userForComplex,
      i32    = 42,
      uid    = fixedUuid3,
      str    = "hello",
    )
    buf += (("idltest.identifiers.ComplexID", "basic", complexId.asJson.noSpaces))

    // ---- 3. Identifier multi-field named (UserId — toString order: company, value) ----
    val userId = idltest.identifiers.UserId(value = fixedUuid1, company = fixedUuid2)
    buf += (("idltest.identifiers.UserId", "basic", userId.asJson.noSpaces))

    // ---- 3b. BucketID (toString order: app, bucket, user) ----
    val bucketId = idltest.identifiers.BucketID(app = fixedUuid1, user = fixedUuid2, bucket = "my-bucket")
    buf += (("idltest.identifiers.BucketID", "basic", bucketId.asJson.noSpaces))

    // ---- 5. ADT multi-branch — ComplexAdt branch ----
    // Encode via the sealed trait type (AdtTester) so the encodeAdtTester implicit is used.
    val adtTester1: idltest.algebraics.AdtTester = idltest.algebraics.AdtTester.ComplexAdt(
      idltest.algebraics.ComplexAdt(id = idltest.algebraics.AdtTestID("alpha"))
    )
    buf += (("idltest.algebraics.AdtTester", "as-ComplexAdt", adtTester1.asJson.noSpaces))

    // ---- 5b. ADT multi-branch — ComplexAdt2 branch ----
    val adtTester2: idltest.algebraics.AdtTester = idltest.algebraics.AdtTester.ComplexAdt2(
      idltest.algebraics.ComplexAdt2(id = idltest.algebraics.AdtTestID("beta"))
    )
    buf += (("idltest.algebraics.AdtTester", "as-ComplexAdt2", adtTester2.asJson.noSpaces))

    // ---- 6. ADT with interface branch — AFace branch ----
    // Encode via AdtWithInterface sealed trait so encodeAdtWithInterface is used.
    val adtWithIface: idltest.algebraics.AdtWithInterface = idltest.algebraics.AdtWithInterface.AFace(
      idltest.algebraics.AFace.Struct(a = 7)
    )
    buf += (("idltest.algebraics.AdtWithInterface", "basic", adtWithIface.asJson.noSpaces))

    // ---- 7. Interface w/ implementing DTO — WithCovariance.Struct ----
    // WithCovariance.Struct(field: Covariant) — field encoded as Covariant interface encoder
    val withCov = idltest.inheritance.WithCovariance.Struct(
      field = idltest.inheritance.Covariant.Struct()
    )
    buf += (("idltest.inheritance.WithCovariance.Struct", "basic", withCov.asJson.noSpaces))

    // ---- 8. Optional present ----
    val optWith = idltest.dtofields.OptionalObj(no = Some(idltest.dtofields.NullableObj(a = 42)))
    buf += (("idltest.dtofields.OptionalObj", "with-some", optWith.asJson.noSpaces))

    // ---- 9. Optional absent ----
    val optNone = idltest.dtofields.OptionalObj(no = None)
    buf += (("idltest.dtofields.OptionalObj", "with-none", optNone.asJson.noSpaces))

    // ---- 10. List of structs ----
    // ListObj.all: List[NullObj] — NullObj is a type alias for NullableObj (per package-object.scala)
    val listObj = idltest.dtofields.ListObj(
      all = List(idltest.dtofields.NullableObj(a = 1), idltest.dtofields.NullableObj(a = 2))
    )
    buf += (("idltest.dtofields.ListObj", "basic", listObj.asJson.noSpaces))

    // ---- 11. Map with string keys (KVIDGeneric.test: Map[String, BucketID]) ----
    val kvId = idltest.identifiers.KVIDGeneric(
      test = Map("key1" -> idltest.identifiers.BucketID(app = fixedUuid1, user = fixedUuid2, bucket = "b1"))
    )
    buf += (("idltest.identifiers.KVIDGeneric", "basic", kvId.asJson.noSpaces))

    // ---- 12. Enum ----
    // Must encode via the sealed-trait type, not the case-object singleton type.
    val engEnum: idltest.identifiers.DepartmentEnum = idltest.identifiers.DepartmentEnum.Engineering
    buf += (("idltest.identifiers.DepartmentEnum", "basic",
      engEnum.asJson.noSpaces
    ))

    // ---- 13. Enum inside Identifier (UserWithEnumId — toString order: company, dept, value) ----
    val userWithEnum = idltest.identifiers.UserWithEnumId(
      value   = fixedUuid1,
      company = fixedUuid2,
      dept    = idltest.identifiers.DepartmentEnum.Sales,
    )
    buf += (("idltest.identifiers.UserWithEnumId", "basic", userWithEnum.asJson.noSpaces))

    // ---- 14. Anyval-shaped DTO (NullableObj — forProduct1 path) ----
    val nullableObj = idltest.dtofields.NullableObj(a = 99)
    buf += (("idltest.dtofields.NullableObj", "basic", nullableObj.asJson.noSpaces))

    // ---- 15. Empty struct ----
    val emptyStruct = idltest.inheritance.Empty.Struct()
    buf += (("idltest.inheritance.Empty.Struct", "empty", emptyStruct.asJson.noSpaces))

    // ---- 16. Service method input wrapper ----
    val simpleInput = idltest.services.TestService.SimpleInput(firstName = "John", secondName = "Doe")
    buf += (("idltest.services.TestService.SimpleInput", "basic", simpleInput.asJson.noSpaces))

    // ---- 17. Service method singular output (unwrapped String) ----
    val singularOut = idltest.services.TestService.GreetSingularOutOutput(value = "Hello, World!")
    buf += (("idltest.services.TestService.GreetSingularOutOutput", "basic", singularOut.asJson.noSpaces))

    // ---- 18. Buzzer method empty input ----
    val emptyBuzzerInput = idltest.events.TestBuzzer.EmptyInput()
    buf += (("idltest.events.TestBuzzer.EmptyInput", "empty", emptyBuzzerInput.asJson.noSpaces))

    // ---- 19. Buzzer enum input (EnumInputInput — forProduct1 wrapping EnumType) ----
    val enumBuzzerInput = idltest.events.TestBuzzer.EnumInputInput(value = idltest.events.EnumType.EnumA)
    buf += (("idltest.events.TestBuzzer.EnumInputInput", "basic", enumBuzzerInput.asJson.noSpaces))

    // ---- 20. Buzzer ADT input (AdtInputInput — wraps ADTType) ----
    val adtBuzzerInput = idltest.events.TestBuzzer.AdtInputInput(
      value = idltest.events.ADTType.BranchA(idltest.events.BranchA(a = "test-branch"))
    )
    buf += (("idltest.events.TestBuzzer.AdtInputInput", "basic", adtBuzzerInput.asJson.noSpaces))

    // ---- 21. JSONLike ADT — JLString branch ----
    // Encode via JSONLike sealed trait encoder which wraps with discriminator key.
    val jsonLikeVal: idltest.json.JSONLike = idltest.json.JSONLike.JLString(idltest.json.JLString(value = "hello"))
    buf += (("idltest.json.JSONLike", "basic", jsonLikeVal.asJson.noSpaces))

    // ---- 22-28. AllTypes.Struct — covers TUInt64, TInt64, TFloat, TUUID, TTsTz, list (single-elem), selfSet (single-elem) ----
    // selfSet: Set[AllTypes] — single-element to avoid iteration-order non-determinism.
    // When encoding Set[AllTypes], each element is encoded via encodeAllTypes (interface encoder with discriminator key).
    // list: List[AllTypes] — single-element for coverage.
    // selfMap, enumMap, option, another: minimal empty/None values.
    //
    // AllTypes.Struct field order (from case class declaration):
    //   b, s, int8, int16, int32, int64, f, d, uuid, ts, tslocal, tsuni, time, date,
    //   uint8, uint16, uint32, uint64, list, another, selfMap, enumMap, option, selfSet,
    //   optionDate, optionTime
    //
    // Recursive AllTypes element for list/selfSet:
    // We use a minimal Struct with empty list/set/map/option to avoid infinite nesting.
    val minimalAllTypes: izumi.test.domain01.AllTypes.Struct = izumi.test.domain01.AllTypes.Struct(
      b         = false,
      s         = "min",
      int8      = 0,
      int16     = 0,
      int32     = 0,
      int64     = 0L,
      f         = 0.0f,
      d         = 0.0,
      uuid      = fixedUuid2,
      ts        = fixedTs,
      tslocal   = fixedTsLocal,
      tsuni     = fixedTs,
      time      = fixedTime,
      date      = fixedDate,
      uint8     = 0,
      uint16    = 0,
      uint32    = 0,
      uint64    = 0L,
      list      = Nil,
      another   = Nil,
      selfMap   = Map.empty,
      enumMap   = Map.empty,
      option    = None,
      selfSet   = Set.empty,
      optionDate = None,
      optionTime = None,
    )

    val allTypesStruct = izumi.test.domain01.AllTypes.Struct(
      b         = true,
      s         = "hello",
      int8      = 42.toByte,
      int16     = 1000.toShort,
      int32     = 100000,
      int64     = -9007199254740993L,   // TInt64 beyond JS safe-integer range
      f         = 1.5f,                 // exact IEEE-754
      d         = -2.25,
      uuid      = fixedUuid1,
      ts        = fixedTs,
      tslocal   = fixedTsLocal,
      tsuni     = fixedTs,
      time      = fixedTime,
      date      = fixedDate,
      uint8     = 200.toByte,           // uint8 stored as Byte (wraps)
      uint16    = 60000.toShort,        // uint16 stored as Short (wraps)
      uint32    = 4000000000L.toInt,    // uint32 stored as Int (wraps)
      uint64    = 9007199254740993L,    // TUInt64: 2^53+1, beyond JS safe-integer
      list      = List(minimalAllTypes),
      another   = Nil,
      selfMap   = Map.empty,
      enumMap   = Map("k1" -> izumi.test.domain01.GoAliasEnumTest.Val1),
      option    = None,
      selfSet   = Set(minimalAllTypes),
      optionDate = None,
      optionTime = None,
    )
    buf += (("izumi.test.domain01.AllTypes.Struct", "basic", allTypesStruct.asJson.noSpaces))

    // ---- 29. Cross-domain reference — Name_incoming (AnyVal, forProduct1) ----
    val nameIncoming = idltest.phase.Name_incoming(name = "test-name")
    buf += (("idltest.phase.Name_incoming", "basic", nameIncoming.asJson.noSpaces))

    buf.result()
  }

  def seed(root: Path): Unit = {
    val fixtures = buildAll()
    println(s"FixtureSeeder: generating ${fixtures.size} fixture files under $root")
    for ((wireId, scenario, json) <- fixtures) {
      val dir  = root.resolve(wireId)
      Files.createDirectories(dir)
      val file = dir.resolve(s"$scenario.json")
      val bytes = json.getBytes(StandardCharsets.UTF_8)
      Files.write(file, bytes)
      println(s"  wrote ${bytes.length} bytes → $file")
    }
    println("FixtureSeeder: done.")
  }
}

object FixtureSeederMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: FixtureSeederMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = Paths.get(args(0))
    val scalaRoot = HarnessCorpus.wireFixturesScalaRoot(repoRoot)
    FixtureSeeder.seed(scalaRoot)
  }
}
