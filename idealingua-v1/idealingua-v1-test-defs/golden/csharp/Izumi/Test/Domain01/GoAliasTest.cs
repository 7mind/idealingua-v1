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
    [JsonConverter(typeof(GoAliasTest_JsonNetConverter))]
    public interface GoAliasTest: IRTTI {
        string A { get; set; }
    }
    public class GoAliasTest_JsonNetConverter: JsonNetConverter<GoAliasTest> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GoAliasTest_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GoAliasTest value, JsonSerializer serializer) {
            // Serializing polymorphic type GoAliasTest
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GoAliasTest ReadJson(JsonReader reader, System.Type objectType, GoAliasTest existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = GoAliasTestStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (GoAliasTest)res;
        }
    }

    [JsonConverter(typeof(GoAliasTestStruct_JsonNetConverter))]
    public class GoAliasTestStruct : GoAliasTest {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.GoAliasTest";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.GoAliasTest.Struct";
        public string GetPackageName() { return GoAliasTestStruct.RTTI_PACKAGE; }
        public string GetClassName() { return GoAliasTestStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return GoAliasTestStruct.RTTI_FULLCLASSNAME; }

        public string A { get; set; }

        public GoAliasTestStruct() {
        }

        public GoAliasTestStruct(string a) {
            this.A = a;
        }

        public GoAliasTest ToGoAliasTest() {
            var res = new GoAliasTestStruct();
            res.A = this.A;
            return res;
        }

        public void LoadGoAliasTest(GoAliasTest value) {
            this.A = value.A;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            GoAliasTestStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            GoAliasTestStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!GoAliasTestStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface GoAliasTest.");
            }

            return tpe;
        }

        static GoAliasTestStruct() {
            var type = typeof(GoAliasTest);
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
                            GoAliasTestStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class GoAliasTestStruct_JsonNetConverter: JsonNetConverter<GoAliasTestStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GoAliasTestStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GoAliasTestStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GoAliasTestStruct ReadJson(JsonReader reader, System.Type objectType, GoAliasTestStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new GoAliasTestStruct(
                json["a"].Value<string>()
            );
        }
    }
}