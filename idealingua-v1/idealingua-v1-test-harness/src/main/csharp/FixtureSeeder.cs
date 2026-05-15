using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using Newtonsoft.Json;

namespace IdealinguaV1Harness.CSharpDriver {
    /// <summary>
    /// One-shot seeder: constructs typed values and writes their canonical Newtonsoft-encoded
    /// JSON to wire-fixtures/csharp/.  Run via:
    ///   dotnet bin/Debug/net9.0/Driver.dll seed &lt;repoRoot&gt;
    /// Kept as a permanent diagnostic tool for re-baselining.
    /// </summary>
    public static class FixtureSeeder {
        public static void Seed(string repoRoot) {
            var fixturesRoot = Path.Combine(repoRoot, "idealingua-v1", "idealingua-v1-test-defs", "wire-fixtures", "csharp");
            Console.Error.WriteLine($"[seeder] writing fixtures to: {fixturesRoot}");

            var fixtures = BuildFixtures();
            foreach (var (wireId, scenario, value) in fixtures) {
                if (!Dispatch.Entries.TryGetValue(wireId, out var entry)) {
                    Console.Error.WriteLine($"[seeder] WARN: no dispatch entry for {wireId}");
                    continue;
                }
                var json = entry.Serialize(value);
                var dir  = Path.Combine(fixturesRoot, wireId);
                Directory.CreateDirectory(dir);
                var path = Path.Combine(dir, scenario + ".json");
                File.WriteAllText(path, json, new System.Text.UTF8Encoding(encoderShouldEmitUTF8Identifier: false));
                Console.Error.WriteLine($"[seeder] wrote {wireId}/{scenario}.json  →  {json}");
            }
            Console.Error.WriteLine("[seeder] done.");
        }

        private static List<(string wireId, string scenario, object value)> BuildFixtures() {
            var list = new List<(string, string, object)>();

            // ── Plain DTOs ──────────────────────────────────────────────────────────
            list.Add((
                "idltest.dtofields.Point", "basic",
                new Idltest.Dtofields.Point(10, 20, "abc", "point-1", 3, 4, "of", true)
            ));
            list.Add((
                "idltest.dtofields.Point", "extra",
                new Idltest.Dtofields.Point(0, 100, "xyz", "origin", 0, 0, "", false)
            ));

            list.Add((
                "idltest.dtofields.NullableObj", "basic",
                new Idltest.Dtofields.NullableObj(99)
            ));

            // Optional present: No = NullableObj(42)
            list.Add((
                "idltest.dtofields.OptionalObj", "with-some",
                new Idltest.Dtofields.OptionalObj(new Idltest.Dtofields.NullableObj(42))
            ));
            // Optional absent: No = null → serialises as {}
            list.Add((
                "idltest.dtofields.OptionalObj", "with-none",
                new Idltest.Dtofields.OptionalObj(null)
            ));

            // ListObj: list of two NullableObjs
            list.Add((
                "idltest.dtofields.ListObj", "basic",
                new Idltest.Dtofields.ListObj(new List<Idltest.Dtofields.NullableObj> {
                    new Idltest.Dtofields.NullableObj(1),
                    new Idltest.Dtofields.NullableObj(2),
                })
            ));

            // ── Identifiers ─────────────────────────────────────────────────────────
            // BucketID: App=3a7f0c12..., Bucket="my-bucket", User=0a1b2c3d...
            var appGuid    = new Guid("3a7f0c12-1234-5678-9abc-fedcba987654");
            var userGuid   = new Guid("0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9");
            var uid2       = new Guid("aaaabbbb-cccc-dddd-eeee-ffffaaaabbbb");
            var bucket1    = new Idltest.Identifiers.BucketID(appGuid, userGuid, "my-bucket");
            var bucket2    = new Idltest.Identifiers.BucketID(appGuid, userGuid, "test-bucket");
            var bucket3    = new Idltest.Identifiers.BucketID(appGuid, userGuid, "b1");

            list.Add((
                "idltest.identifiers.BucketID", "basic",
                bucket1
            ));

            // UserId: Company=0a1b2c3d..., Value=3a7f0c12...
            // ToString() → "UserId#" + company + ":" + value
            var userId = new Idltest.Identifiers.UserId(appGuid, userGuid);
            list.Add((
                "idltest.identifiers.UserId", "basic",
                userId
            ));

            // DepartmentEnum
            list.Add((
                "idltest.identifiers.DepartmentEnum", "basic",
                Idltest.Identifiers.DepartmentEnum.Engineering
            ));

            // UserWithEnumId: Company=0a1b2c3d..., Dept=Sales, Value=3a7f0c12...
            // ToString() → "UserWithEnumId#" + company + ":" + dept + ":" + value
            var userWithEnumId = new Idltest.Identifiers.UserWithEnumId(appGuid, userGuid, Idltest.Identifiers.DepartmentEnum.Sales);
            list.Add((
                "idltest.identifiers.UserWithEnumId", "basic",
                userWithEnumId
            ));

            // ComplexID: Bucket=bucket2, User=userWithEnumIdE, I32=42, Uid=uid2, Str="hello"
            // where userWithEnumIdE uses Engineering dept (to match Scala fixture)
            var userWithEnumIdE = new Idltest.Identifiers.UserWithEnumId(appGuid, userGuid, Idltest.Identifiers.DepartmentEnum.Engineering);
            var complexId = new Idltest.Identifiers.ComplexID(bucket2, userWithEnumIdE, 42, uid2, "hello");
            list.Add((
                "idltest.identifiers.ComplexID", "basic",
                complexId
            ));

            // KVIDGeneric: map "key1" → bucket3
            var kvMap = new Dictionary<string, Idltest.Identifiers.BucketID> { { "key1", bucket3 } };
            list.Add((
                "idltest.identifiers.KVIDGeneric", "basic",
                new Idltest.Identifiers.KVIDGeneric(kvMap)
            ));

            // ── ADTs ────────────────────────────────────────────────────────────────
            list.Add((
                "idltest.algebraics.AdtTester", "as-ComplexAdt",
                (object)new Idltest.Algebraics.AdtTester.ComplexAdt(
                    new Idltest.Algebraics.ComplexAdt(new Idltest.Algebraics.AdtTestID("alpha"))
                )
            ));
            list.Add((
                "idltest.algebraics.AdtTester", "as-ComplexAdt2",
                (object)new Idltest.Algebraics.AdtTester.ComplexAdt2(
                    new Idltest.Algebraics.ComplexAdt2(new Idltest.Algebraics.AdtTestID("beta"))
                )
            ));

            // AdtWithInterface: AFace branch with AFaceStruct(a=7)
            list.Add((
                "idltest.algebraics.AdtWithInterface", "basic",
                (object)new Idltest.Algebraics.AdtWithInterface.AFace(new Idltest.Algebraics.AFaceStruct(7))
            ));

            // ── Interface impls ─────────────────────────────────────────────────────
            // WithCovariance.Struct: bare struct with field = CovariantStruct
            list.Add((
                "idltest.inheritance.WithCovariance.Struct", "basic",
                new Idltest.Inheritance.WithCovarianceStruct(
                    new Idltest.Inheritance.CovariantStruct()
                )
            ));

            // Empty.Struct: bare empty struct
            list.Add((
                "idltest.inheritance.Empty.Struct", "empty",
                new Idltest.Inheritance.EmptyStruct()
            ));

            // ── JSONLike ADT ─────────────────────────────────────────────────────────
            list.Add((
                "idltest.json.JSONLike", "basic",
                (object)new Idltest.Json.JSONLike.JLString(new Idltest.Json.JLString("hello"))
            ));

            // ── Cross-domain ─────────────────────────────────────────────────────────
            list.Add((
                "idltest.phase.Name_incoming", "basic",
                new Idltest.Phase.Name_incoming("test-name")
            ));

            // ── AllTypes.Struct (BC=N: ts → +00:00, unsigned native, float 1.5/−2.25) ──
            // ts:      UTC zoned  → TszDefault = "yyyy-MM-ddTHH:mm:ss.fffzzz".
            //          C# ParseExact with literal-Z format produces DateTimeKind.Unspecified,
            //          so re-serialize uses TszDefault → "+00:00" on UTC host (this host).
            //          We construct the ts value by parsing "+00:00" to get the stable round-trip form.
            // tsuni:   UTC univ   → TsuDefault = "yyyy-MM-ddTHH:mm:ss.fffZ"   → "Z"
            // tslocal: local      → TslDefault = "yyyy-MM-ddTHH:mm:ss.fff"    → no zone
            // uint*:   native positive values
            // Parse ts from the stable +00:00 form so seeder emits what the driver round-trips to.
            var tsUtc   = DateTime.ParseExact("2025-01-15T10:30:45.000+00:00",
                              IRT.Marshaller.JsonNetTimeFormats.Tsz,
                              System.Globalization.CultureInfo.InvariantCulture,
                              System.Globalization.DateTimeStyles.None);
            var tsLocal = new DateTime(2025, 1, 15, 10, 30, 45, DateTimeKind.Unspecified);
            var tsUni   = new DateTime(2025, 1, 15, 10, 30, 45, DateTimeKind.Utc);
            var time    = new TimeSpan(0, 10, 30, 45, 0);
            var date    = new DateTime(2025, 1, 15);
            var uuid    = new Guid("3a7f0c12-1234-5678-9abc-fedcba987654");
            var enumMap = new Dictionary<string, Izumi.Test.Domain01.GoAliasEnumTest> {
                { "k1", Izumi.Test.Domain01.GoAliasEnumTest.Val1 }
            };
            list.Add((
                "izumi.test.domain01.AllTypes.Struct", "basic",
                new Izumi.Test.Domain01.AllTypesStruct(
                    b: true,
                    s: "hello",
                    int8: 42,
                    int16: 1000,
                    int32: 100000,
                    int64: -9007199254740993L,
                    f: 1.5f,
                    d: -2.25,
                    uuid: uuid,
                    ts: tsUtc,
                    tslocal: tsLocal,
                    tsuni: tsUni,
                    time: time,
                    date: date,
                    uint8: 200,
                    uint16: 60000,
                    uint32: 4000000000u,
                    uint64: 9007199254740993ul,
                    list: new List<Izumi.Test.Domain01.AllTypes>(),
                    another: new List<Izumi.Test.Domain01.AllTypes>(),
                    selfMap: new Dictionary<string, Izumi.Test.Domain01.AllTypes>(),
                    enumMap: enumMap,
                    option: null,
                    selfSet: new List<Izumi.Test.Domain01.AllTypes>(),
                    optionDate: null,
                    optionTime: null
                )
            ));

            return list;
        }
    }
}
