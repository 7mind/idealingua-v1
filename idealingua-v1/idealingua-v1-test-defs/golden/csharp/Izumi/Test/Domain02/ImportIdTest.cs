// Auto-generated, any modifications may be overwritten in the future.

using Izumi.Test.Domain01;
using IRT;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(ImportIdTest_JsonNetConverter))]
    public interface ImportIdTest: IRTTI {
        Izumi.Test.Domain01.ImportAppId Id { get; set; }
        Izumi.Test.Domain01.GenericFailure Fail { get; set; }
        Izumi.Test.Domain01.GenericFailureData Mix { get; set; }
    }
    public class ImportIdTest_JsonNetConverter: JsonNetConverter<ImportIdTest> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ImportIdTest_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ImportIdTest value, JsonSerializer serializer) {
            // Serializing polymorphic type ImportIdTest
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ImportIdTest ReadJson(JsonReader reader, System.Type objectType, ImportIdTest existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = ImportIdTestStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (ImportIdTest)res;
        }
    }

    [JsonConverter(typeof(ImportIdTestStruct_JsonNetConverter))]
    public class ImportIdTestStruct : ImportIdTest {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02.ImportIdTest";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.ImportIdTest.Struct";
        public string GetPackageName() { return ImportIdTestStruct.RTTI_PACKAGE; }
        public string GetClassName() { return ImportIdTestStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ImportIdTestStruct.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.ImportAppId Id { get; set; }
        public Izumi.Test.Domain01.GenericFailure Fail { get; set; }
        public Izumi.Test.Domain01.GenericFailureData Mix { get; set; }

        public ImportIdTestStruct() {
        }

        public ImportIdTestStruct(Izumi.Test.Domain01.ImportAppId id, Izumi.Test.Domain01.GenericFailure fail, Izumi.Test.Domain01.GenericFailureData mix) {
            this.Id = id;
            this.Fail = fail;
            this.Mix = mix;
        }

        public ImportIdTest ToImportIdTest() {
            var res = new ImportIdTestStruct();
            res.Id = this.Id;
            res.Fail = this.Fail;
            res.Mix = this.Mix;
            return res;
        }

        public void LoadImportIdTest(ImportIdTest value) {
            this.Id = value.Id;
            this.Fail = value.Fail;
            this.Mix = value.Mix;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            ImportIdTestStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            ImportIdTestStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!ImportIdTestStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface ImportIdTest.");
            }

            return tpe;
        }

        static ImportIdTestStruct() {
            var type = typeof(ImportIdTest);
            #if IRT_SCAN_ALL_ASSEMBLIES
                var assemblies = AppDomain.CurrentDomain.GetAssemblies();
            #else
                var assemblies = new[] {Assembly.GetExecutingAssembly()};
            #endif
            foreach (var assembly in assemblies) {
                System.Type[] types = null;
                try {
                    types = assembly.GetTypes();
                } catch (Exception) {
                    // ReflectionTypeLoadException potentially caught here
                    continue;
                }
                foreach (var tp in types) {
                    if (type.IsAssignableFrom(tp) && !tp.IsInterface) {
                        var rttiID = tp.GetField("RTTI_FULLCLASSNAME");
                        if (rttiID != null) {
                            ImportIdTestStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class ImportIdTestStruct_JsonNetConverter: JsonNetConverter<ImportIdTestStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ImportIdTestStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ImportIdTestStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WritePropertyName("fail");
            serializer.Serialize(writer, v.Fail);
            writer.WritePropertyName("mix");
            // Serializing polymorphic type GenericFailureData
            writer.WriteStartObject();
            writer.WritePropertyName(v.Mix.GetFullClassName());
            serializer.Serialize(writer, v.Mix);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ImportIdTestStruct ReadJson(JsonReader reader, System.Type objectType, ImportIdTestStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _fail = serializer.Deserialize<Izumi.Test.Domain01.GenericFailure>(json["fail"].CreateReader());
            return new ImportIdTestStruct(
                Izumi.Test.Domain01.ImportAppId.From(json["id"].Value<string>()), 
                _fail, 
                serializer.Deserialize<Izumi.Test.Domain01.GenericFailureData>(json["mix"].CreateReader())
            );
        }
    }
}