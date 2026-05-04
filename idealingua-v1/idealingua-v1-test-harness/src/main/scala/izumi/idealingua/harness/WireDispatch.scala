package izumi.idealingua.harness

import io.circe.{Decoder, Json}
import io.circe.syntax._

/**
  * Heterogeneous dispatch table: wireId → (decode, encode) pair.
  *
  * All entries are typed via `Any` because the table is heterogeneous; the type information is
  * encoded in the wireId, not the static type. Each entry's decode/encode pair closes over the
  * correct Circe Encoder/Decoder instances for its concrete type via the `._ import` pattern,
  * which is the Scala 2 cross-build-safe form (see §11 R6 of the plan).
  *
  * Naming conventions for wireIds that deviate from plan §5 placeholders:
  *   - Rows 16/17: plan said "GreeterService" which does not exist in the corpus; mapped to
  *     "idltest.services.TestService.SimpleInput" and
  *     "idltest.services.TestService.GreetSingularOutOutput" instead.
  *   - Row 15: "idltest.inheritance.Empty" is an interface; used "idltest.inheritance.Empty.Struct"
  *     (the concrete case class) for the wire fixture.
  *   - Row 7: "idltest.inheritance.WithCovariance" is an interface; used
  *     "idltest.inheritance.WithCovariance.Struct" as the concrete fixture type.
  *   - Rows 22-28: "izumi.test.domain01.AllTypes" is an interface; used
  *     "izumi.test.domain01.AllTypes.Struct" as the concrete fixture type.
  *   - Row 29: "idltest.phase.Name_incoming" is a DTO that inherits from the `Name` interface
  *     (idltest.phase domain) and transitively uses LengthInBytes from the same domain. This
  *     exercises the cross-domain reference pattern (idltest.phase imports idltest.aliases).
  */
private[harness] final case class RoundTripEntry(
  wireId: String,
  decode: Json => Decoder.Result[Any],
  encode: Any => Json,
)

private[harness] object WireDispatch {

  // Import sealed-trait Circe companions for ADTs and interfaces.
  // The `._` form is required for cross-build compatibility with Scala 2.13.
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
  // Sealed-trait ADT companions — needed to bring encoder/decoder implicits into scope.
  // The `._` wildcard is the Scala 2 cross-build-safe form (R6).
  import idltest.algebraics.AdtTester._
  import idltest.algebraics.AdtWithInterface._
  import idltest.json.JSONLike._
  // Name_incoming has its own companion Circe; Name (interface) import not needed.
  import idltest.phase.Name_incoming._

  val entries: Map[String, RoundTripEntry] = Map(

    // 1. Plain DTO mixed scalars — field order audit (w, h, id, name, x, y, ownfield, export)
    "idltest.dtofields.Point" -> RoundTripEntry(
      "idltest.dtofields.Point",
      json => json.as[idltest.dtofields.Point].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.dtofields.Point].asJson,
    ),

    // 2. Identifier multi-field unnamed (ComplexID)
    "idltest.identifiers.ComplexID" -> RoundTripEntry(
      "idltest.identifiers.ComplexID",
      json => json.as[idltest.identifiers.ComplexID].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.identifiers.ComplexID].asJson,
    ),

    // 3. Identifier multi-field named (UserId — sorted-by-name serialization: company, value)
    "idltest.identifiers.UserId" -> RoundTripEntry(
      "idltest.identifiers.UserId",
      json => json.as[idltest.identifiers.UserId].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.identifiers.UserId].asJson,
    ),

    // 3b. BucketID (sub-identifier with 3 fields: app, bucket, user)
    "idltest.identifiers.BucketID" -> RoundTripEntry(
      "idltest.identifiers.BucketID",
      json => json.as[idltest.identifiers.BucketID].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.identifiers.BucketID].asJson,
    ),

    // 5. ADT multi-branch (AdtTester — discriminator key pattern)
    "idltest.algebraics.AdtTester" -> RoundTripEntry(
      "idltest.algebraics.AdtTester",
      json => json.as[idltest.algebraics.AdtTester].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.algebraics.AdtTester].asJson,
    ),

    // 6. ADT with interface branch (AdtWithInterface — AFace + Success branches)
    "idltest.algebraics.AdtWithInterface" -> RoundTripEntry(
      "idltest.algebraics.AdtWithInterface",
      json => json.as[idltest.algebraics.AdtWithInterface].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.algebraics.AdtWithInterface].asJson,
    ),

    // 7. Interface w/ implementing DTO — WithCovariance.Struct is the concrete case class
    "idltest.inheritance.WithCovariance.Struct" -> RoundTripEntry(
      "idltest.inheritance.WithCovariance.Struct",
      json => json.as[idltest.inheritance.WithCovariance.Struct].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.inheritance.WithCovariance.Struct].asJson,
    ),

    // 8+9. Optional present/absent (OptionalObj — Option[NullableObj])
    "idltest.dtofields.OptionalObj" -> RoundTripEntry(
      "idltest.dtofields.OptionalObj",
      json => json.as[idltest.dtofields.OptionalObj].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.dtofields.OptionalObj].asJson,
    ),

    // 10. List of structs (ListObj.all: List[NullableObj] — NullObj is a type alias)
    "idltest.dtofields.ListObj" -> RoundTripEntry(
      "idltest.dtofields.ListObj",
      json => json.as[idltest.dtofields.ListObj].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.dtofields.ListObj].asJson,
    ),

    // 11. Map with string keys (KVIDGeneric.test: Map[String, BucketID])
    "idltest.identifiers.KVIDGeneric" -> RoundTripEntry(
      "idltest.identifiers.KVIDGeneric",
      json => json.as[idltest.identifiers.KVIDGeneric].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.identifiers.KVIDGeneric].asJson,
    ),

    // 12. Enum (DepartmentEnum — string encoding: Engineering, Sales)
    "idltest.identifiers.DepartmentEnum" -> RoundTripEntry(
      "idltest.identifiers.DepartmentEnum",
      json => json.as[idltest.identifiers.DepartmentEnum].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.identifiers.DepartmentEnum].asJson,
    ),

    // 13. Enum inside Identifier (UserWithEnumId — company, dept, value)
    "idltest.identifiers.UserWithEnumId" -> RoundTripEntry(
      "idltest.identifiers.UserWithEnumId",
      json => json.as[idltest.identifiers.UserWithEnumId].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.identifiers.UserWithEnumId].asJson,
    ),

    // 14. Anyval-shaped DTO (NullableObj — forProduct1 path, single-field AnyVal)
    "idltest.dtofields.NullableObj" -> RoundTripEntry(
      "idltest.dtofields.NullableObj",
      json => json.as[idltest.dtofields.NullableObj].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.dtofields.NullableObj].asJson,
    ),

    // 15. Empty struct concrete case class (Empty is an interface; Struct is the empty case class)
    "idltest.inheritance.Empty.Struct" -> RoundTripEntry(
      "idltest.inheritance.Empty.Struct",
      json => json.as[idltest.inheritance.Empty.Struct].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.inheritance.Empty.Struct].asJson,
    ),

    // 16. Service method multi-field input wrapper (TestService.SimpleInput = simple.Input)
    //     Plan said "GreeterService.greet.Input" — no such service in corpus; mapped to TestService.
    "idltest.services.TestService.SimpleInput" -> RoundTripEntry(
      "idltest.services.TestService.SimpleInput",
      json => json.as[idltest.services.TestService.SimpleInput].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.services.TestService.SimpleInput].asJson,
    ),

    // 17. Service method singular output (GreetSingularOutOutput — unwrapped String output)
    //     Plan said "GreeterService.hello.Output" — mapped to TestService.GreetSingularOutOutput.
    "idltest.services.TestService.GreetSingularOutOutput" -> RoundTripEntry(
      "idltest.services.TestService.GreetSingularOutOutput",
      json => json.as[idltest.services.TestService.GreetSingularOutOutput].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.services.TestService.GreetSingularOutOutput].asJson,
    ),

    // 18. Buzzer method empty input wrapper
    "idltest.events.TestBuzzer.EmptyInput" -> RoundTripEntry(
      "idltest.events.TestBuzzer.EmptyInput",
      json => json.as[idltest.events.TestBuzzer.EmptyInput].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.events.TestBuzzer.EmptyInput].asJson,
    ),

    // 19. Buzzer enum input (EnumInputInput — wraps EnumType, AnyVal-shaped)
    "idltest.events.TestBuzzer.EnumInputInput" -> RoundTripEntry(
      "idltest.events.TestBuzzer.EnumInputInput",
      json => json.as[idltest.events.TestBuzzer.EnumInputInput].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.events.TestBuzzer.EnumInputInput].asJson,
    ),

    // 20. Buzzer ADT input (AdtInputInput — wraps ADTType, a regular struct)
    "idltest.events.TestBuzzer.AdtInputInput" -> RoundTripEntry(
      "idltest.events.TestBuzzer.AdtInputInput",
      json => json.as[idltest.events.TestBuzzer.AdtInputInput].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.events.TestBuzzer.AdtInputInput].asJson,
    ),

    // 21. Jsonlike ADT (JSONLike — recursive JSON-like structure with 6 branches)
    "idltest.json.JSONLike" -> RoundTripEntry(
      "idltest.json.JSONLike",
      json => json.as[idltest.json.JSONLike].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.json.JSONLike].asJson,
    ),

    // 22-28. AllTypes.Struct — covers TUInt64, TInt64, TFloat/TDouble, TUUID, TTsTz, TList,
    //        TSet (single-element only per PR-03.2 §8 Q11 deferral).
    //        AllTypes is an interface; AllTypes.Struct is the concrete case class.
    "izumi.test.domain01.AllTypes.Struct" -> RoundTripEntry(
      "izumi.test.domain01.AllTypes.Struct",
      json => json.as[izumi.test.domain01.AllTypes.Struct].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[izumi.test.domain01.AllTypes.Struct].asJson,
    ),

    // 29. Cross-domain reference — Name_incoming (idltest.phase) transitively references
    //     LengthInBytes from the same package, which itself is derived from idltest.aliases.
    //     Name_incoming is an AnyVal-shaped DTO implementing the Name interface.
    "idltest.phase.Name_incoming" -> RoundTripEntry(
      "idltest.phase.Name_incoming",
      json => json.as[idltest.phase.Name_incoming].asInstanceOf[Decoder.Result[Any]],
      v    => v.asInstanceOf[idltest.phase.Name_incoming].asJson,
    ),

    // 30. Extra Point fixture (different scenario values, same dispatch entry as row 1)
    // NOTE: Row 30 reuses "idltest.dtofields.Point" — no separate entry needed; T3 authors
    // a second fixture file under the same wireId directory.
  )
}
