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

namespace Idltest.Diamonds {
    [JsonConverter(typeof(TestInterface3_JsonNetConverter))]
    public interface TestInterface3: IRTTI {
        int If1Field_overriden { get; set; }
        int If1Field_inherited { get; set; }
        long SameField { get; set; }
        long SameEverywhereField { get; set; }
        long If3Field { get; set; }
    }
    public class TestInterface3_JsonNetConverter: JsonNetConverter<TestInterface3> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterface3_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterface3 value, JsonSerializer serializer) {
            // Serializing polymorphic type TestInterface3
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestInterface3 ReadJson(JsonReader reader, System.Type objectType, TestInterface3 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TestInterface3Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TestInterface3)res;
        }
    }

    [JsonConverter(typeof(TestInterface3Struct_JsonNetConverter))]
    public class TestInterface3Struct : TestInterface3 {
        public static readonly string RTTI_PACKAGE = "idltest.diamonds.TestInterface3";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.diamonds.TestInterface3.Struct";
        public string GetPackageName() { return TestInterface3Struct.RTTI_PACKAGE; }
        public string GetClassName() { return TestInterface3Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestInterface3Struct.RTTI_FULLCLASSNAME; }

        public int If1Field_overriden { get; set; }
        public int If1Field_inherited { get; set; }
        public long SameField { get; set; }
        public long SameEverywhereField { get; set; }
        public long If3Field { get; set; }

        public TestInterface3Struct() {
        }

        public TestInterface3Struct(int if1Field_overriden, int if1Field_inherited, long sameField, long sameEverywhereField, long if3Field) {
            this.If1Field_overriden = if1Field_overriden;
            this.If1Field_inherited = if1Field_inherited;
            this.SameField = sameField;
            this.SameEverywhereField = sameEverywhereField;
            this.If3Field = if3Field;
        }

        public TestInterface3 ToTestInterface3() {
            var res = new TestInterface3Struct();
            res.If1Field_overriden = this.If1Field_overriden;
            res.If1Field_inherited = this.If1Field_inherited;
            res.SameField = this.SameField;
            res.SameEverywhereField = this.SameEverywhereField;
            res.If3Field = this.If3Field;
            return res;
        }

        public void LoadTestInterface3(TestInterface3 value) {
            this.If1Field_overriden = value.If1Field_overriden;
            this.If1Field_inherited = value.If1Field_inherited;
            this.SameField = value.SameField;
            this.SameEverywhereField = value.SameEverywhereField;
            this.If3Field = value.If3Field;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TestInterface3Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TestInterface3Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TestInterface3Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TestInterface3.");
            }

            return tpe;
        }

        static TestInterface3Struct() {
            var type = typeof(TestInterface3);
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
                            TestInterface3Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TestInterface3Struct_JsonNetConverter: JsonNetConverter<TestInterface3Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterface3Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterface3Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("if1Field_overriden");
            writer.WriteValue(v.If1Field_overriden);
            writer.WritePropertyName("if1Field_inherited");
            writer.WriteValue(v.If1Field_inherited);
            writer.WritePropertyName("sameField");
            writer.WriteValue(v.SameField);
            writer.WritePropertyName("sameEverywhereField");
            writer.WriteValue(v.SameEverywhereField);
            writer.WritePropertyName("if3Field");
            writer.WriteValue(v.If3Field);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestInterface3Struct ReadJson(JsonReader reader, System.Type objectType, TestInterface3Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestInterface3Struct(
                json["if1Field_overriden"].Value<int>(), 
                json["if1Field_inherited"].Value<int>(), 
                json["sameField"].Value<long>(), 
                json["sameEverywhereField"].Value<long>(), 
                json["if3Field"].Value<long>()
            );
        }
    }
}