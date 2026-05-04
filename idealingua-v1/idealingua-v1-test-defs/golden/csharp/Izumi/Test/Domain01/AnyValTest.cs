// Auto-generated, any modifications may be overwritten in the future.

using IRT;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(AnyValTest_JsonNetConverter))]
    public interface AnyValTest: IRTTI {
        bool BoolField { get; set; }
    }
    public class AnyValTest_JsonNetConverter: JsonNetConverter<AnyValTest> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AnyValTest_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AnyValTest value, JsonSerializer serializer) {
            // Serializing polymorphic type AnyValTest
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AnyValTest ReadJson(JsonReader reader, System.Type objectType, AnyValTest existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = AnyValTestStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (AnyValTest)res;
        }
    }

    [JsonConverter(typeof(AnyValTestStruct_JsonNetConverter))]
    public class AnyValTestStruct : AnyValTest {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.AnyValTest";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.AnyValTest.Struct";
        public string GetPackageName() { return AnyValTestStruct.RTTI_PACKAGE; }
        public string GetClassName() { return AnyValTestStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AnyValTestStruct.RTTI_FULLCLASSNAME; }

        public bool BoolField { get; set; }

        public AnyValTestStruct() {
        }

        public AnyValTestStruct(bool boolField) {
            this.BoolField = boolField;
        }

        public AnyValTest ToAnyValTest() {
            var res = new AnyValTestStruct();
            res.BoolField = this.BoolField;
            return res;
        }

        public void LoadAnyValTest(AnyValTest value) {
            this.BoolField = value.BoolField;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            AnyValTestStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            AnyValTestStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!AnyValTestStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface AnyValTest.");
            }

            return tpe;
        }

        static AnyValTestStruct() {
            var type = typeof(AnyValTest);
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
                            AnyValTestStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class AnyValTestStruct_JsonNetConverter: JsonNetConverter<AnyValTestStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AnyValTestStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AnyValTestStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("boolField");
            writer.WriteValue(v.BoolField);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AnyValTestStruct ReadJson(JsonReader reader, System.Type objectType, AnyValTestStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new AnyValTestStruct(
                json["boolField"].Value<bool>()
            );
        }
    }
}