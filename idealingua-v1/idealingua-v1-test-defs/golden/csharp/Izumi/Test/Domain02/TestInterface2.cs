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

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(TestInterface2_JsonNetConverter))]
    public interface TestInterface2: IRTTI {
        long If2Field { get; set; }
        long SameField { get; set; }
        long SameEverywhereField { get; set; }
    }
    public class TestInterface2_JsonNetConverter: JsonNetConverter<TestInterface2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterface2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterface2 value, JsonSerializer serializer) {
            // Serializing polymorphic type TestInterface2
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestInterface2 ReadJson(JsonReader reader, System.Type objectType, TestInterface2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TestInterface2Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TestInterface2)res;
        }
    }

    [JsonConverter(typeof(TestInterface2Struct_JsonNetConverter))]
    public class TestInterface2Struct : TestInterface2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02.TestInterface2";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.TestInterface2.Struct";
        public string GetPackageName() { return TestInterface2Struct.RTTI_PACKAGE; }
        public string GetClassName() { return TestInterface2Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestInterface2Struct.RTTI_FULLCLASSNAME; }

        public long If2Field { get; set; }
        public long SameField { get; set; }
        public long SameEverywhereField { get; set; }

        public TestInterface2Struct() {
        }

        public TestInterface2Struct(long if2Field, long sameField, long sameEverywhereField) {
            this.If2Field = if2Field;
            this.SameField = sameField;
            this.SameEverywhereField = sameEverywhereField;
        }

        public TestInterface2 ToTestInterface2() {
            var res = new TestInterface2Struct();
            res.If2Field = this.If2Field;
            res.SameField = this.SameField;
            res.SameEverywhereField = this.SameEverywhereField;
            return res;
        }

        public void LoadTestInterface2(TestInterface2 value) {
            this.If2Field = value.If2Field;
            this.SameField = value.SameField;
            this.SameEverywhereField = value.SameEverywhereField;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TestInterface2Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TestInterface2Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TestInterface2Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TestInterface2.");
            }

            return tpe;
        }

        static TestInterface2Struct() {
            var type = typeof(TestInterface2);
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
                            TestInterface2Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TestInterface2Struct_JsonNetConverter: JsonNetConverter<TestInterface2Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterface2Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterface2Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("if2Field");
            writer.WriteValue(v.If2Field);
            writer.WritePropertyName("sameField");
            writer.WriteValue(v.SameField);
            writer.WritePropertyName("sameEverywhereField");
            writer.WriteValue(v.SameEverywhereField);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestInterface2Struct ReadJson(JsonReader reader, System.Type objectType, TestInterface2Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestInterface2Struct(
                json["if2Field"].Value<long>(), 
                json["sameField"].Value<long>(), 
                json["sameEverywhereField"].Value<long>()
            );
        }
    }
}