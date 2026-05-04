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

namespace Idltest.Syntax {
    [JsonConverter(typeof(TestMixin_JsonNetConverter))]
    public interface TestMixin: IRTTI {
    }
    public class TestMixin_JsonNetConverter: JsonNetConverter<TestMixin> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestMixin_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestMixin value, JsonSerializer serializer) {
            // Serializing polymorphic type TestMixin
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestMixin ReadJson(JsonReader reader, System.Type objectType, TestMixin existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TestMixinStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TestMixin)res;
        }
    }

    [JsonConverter(typeof(TestMixinStruct_JsonNetConverter))]
    public class TestMixinStruct : TestMixin {
        public static readonly string RTTI_PACKAGE = "idltest.syntax.TestMixin";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.syntax.TestMixin.Struct";
        public string GetPackageName() { return TestMixinStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TestMixinStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestMixinStruct.RTTI_FULLCLASSNAME; }

        public TestMixinStruct() {
        }

        public TestMixin ToTestMixin() {
            var res = new TestMixinStruct();

            return res;
        }

        public void LoadTestMixin(TestMixin value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TestMixinStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TestMixinStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TestMixinStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TestMixin.");
            }

            return tpe;
        }

        static TestMixinStruct() {
            var type = typeof(TestMixin);
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
                            TestMixinStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TestMixinStruct_JsonNetConverter: JsonNetConverter<TestMixinStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestMixinStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestMixinStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestMixinStruct ReadJson(JsonReader reader, System.Type objectType, TestMixinStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new TestMixinStruct(

            );
        }
    }
}