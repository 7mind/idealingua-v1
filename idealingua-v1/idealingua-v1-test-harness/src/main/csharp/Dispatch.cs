using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace IdealinguaV1Harness.CSharpDriver {
    public interface IDispatchEntry {
        string Serialize(object value);
        object Deserialize(string json);
    }

    public sealed class DispatchEntry<T> : IDispatchEntry {
        public string Serialize(object value) => JsonConvert.SerializeObject((T)value, Settings.Default);
        public object Deserialize(string json) => JsonConvert.DeserializeObject<T>(json, Settings.Default);
    }

    public static class Settings {
        public static readonly JsonSerializerSettings Default = BuildDefault();

        private static JsonSerializerSettings BuildDefault() {
            var s = new JsonSerializerSettings();
            s.Converters.Add(new StringEnumConverter());
            s.NullValueHandling    = NullValueHandling.Ignore;
            s.TypeNameHandling     = TypeNameHandling.None;
            s.ReferenceLoopHandling = ReferenceLoopHandling.Serialize;
            s.DateParseHandling    = DateParseHandling.None;
            s.Formatting           = Formatting.None;
            return s;
        }
    }

    public static class Dispatch {
        public static readonly IReadOnlyDictionary<string, IDispatchEntry> Entries
            = new Dictionary<string, IDispatchEntry> {
                // Plain DTOs
                { "idltest.dtofields.Point",                    new DispatchEntry<Idltest.Dtofields.Point>() },
                { "idltest.dtofields.NullableObj",              new DispatchEntry<Idltest.Dtofields.NullableObj>() },
                { "idltest.dtofields.OptionalObj",              new DispatchEntry<Idltest.Dtofields.OptionalObj>() },
                { "idltest.dtofields.ListObj",                  new DispatchEntry<Idltest.Dtofields.ListObj>() },
                // Identifiers
                { "idltest.identifiers.ComplexID",              new DispatchEntry<Idltest.Identifiers.ComplexID>() },
                { "idltest.identifiers.UserId",                 new DispatchEntry<Idltest.Identifiers.UserId>() },
                { "idltest.identifiers.BucketID",               new DispatchEntry<Idltest.Identifiers.BucketID>() },
                { "idltest.identifiers.KVIDGeneric",            new DispatchEntry<Idltest.Identifiers.KVIDGeneric>() },
                // Enums
                { "idltest.identifiers.DepartmentEnum",         new DispatchEntry<Idltest.Identifiers.DepartmentEnum>() },
                { "idltest.identifiers.UserWithEnumId",         new DispatchEntry<Idltest.Identifiers.UserWithEnumId>() },
                // ADTs (abstract base classes; [JsonConverter] on each class drives dispatch)
                { "idltest.algebraics.AdtTester",               new DispatchEntry<Idltest.Algebraics.AdtTester>() },
                { "idltest.algebraics.AdtWithInterface",        new DispatchEntry<Idltest.Algebraics.AdtWithInterface>() },
                // Interface impls (concrete struct classes)
                { "idltest.inheritance.WithCovariance.Struct",  new DispatchEntry<Idltest.Inheritance.WithCovarianceStruct>() },
                { "idltest.inheritance.Empty.Struct",           new DispatchEntry<Idltest.Inheritance.EmptyStruct>() },
                // JSONLike ADT
                { "idltest.json.JSONLike",                      new DispatchEntry<Idltest.Json.JSONLike>() },
                // Big-types DTO (interface impl)
                { "izumi.test.domain01.AllTypes.Struct",        new DispatchEntry<Izumi.Test.Domain01.AllTypesStruct>() },
                // Cross-domain reference
                { "idltest.phase.Name_incoming",                new DispatchEntry<Idltest.Phase.Name_incoming>() },
            };
    }
}
