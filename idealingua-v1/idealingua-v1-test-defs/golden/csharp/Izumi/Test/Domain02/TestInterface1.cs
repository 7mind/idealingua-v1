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
    [JsonConverter(typeof(TestInterface1_JsonNetConverter))]
    public interface TestInterface1: IRTTI {
        int If1Field_overriden { get; set; }
        int If1Field_inherited { get; set; }
        long SameField { get; set; }
        long SameEverywhereField { get; set; }
        Izumi.Test.Domain01.TestValIdentifier FromOtherDomain { get; set; }
        Izumi.Test.Domain01.TestValIdentifier FromOtherDomainDirect { get; set; }
    }
    public class TestInterface1_JsonNetConverter: JsonNetConverter<TestInterface1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterface1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterface1 value, JsonSerializer serializer) {
            // Serializing polymorphic type TestInterface1
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestInterface1 ReadJson(JsonReader reader, System.Type objectType, TestInterface1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TestInterface1Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TestInterface1)res;
        }
    }

    [JsonConverter(typeof(TestInterface1Struct_JsonNetConverter))]
    public class TestInterface1Struct : TestInterface1 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02.TestInterface1";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.TestInterface1.Struct";
        public string GetPackageName() { return TestInterface1Struct.RTTI_PACKAGE; }
        public string GetClassName() { return TestInterface1Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestInterface1Struct.RTTI_FULLCLASSNAME; }

        public int If1Field_overriden { get; set; }
        public int If1Field_inherited { get; set; }
        public long SameField { get; set; }
        public long SameEverywhereField { get; set; }
        public Izumi.Test.Domain01.TestValIdentifier FromOtherDomain { get; set; }
        public Izumi.Test.Domain01.TestValIdentifier FromOtherDomainDirect { get; set; }

        public TestInterface1Struct() {
        }

        public TestInterface1Struct(int if1Field_overriden, int if1Field_inherited, long sameField, long sameEverywhereField, Izumi.Test.Domain01.TestValIdentifier fromOtherDomain, Izumi.Test.Domain01.TestValIdentifier fromOtherDomainDirect) {
            this.If1Field_overriden = if1Field_overriden;
            this.If1Field_inherited = if1Field_inherited;
            this.SameField = sameField;
            this.SameEverywhereField = sameEverywhereField;
            this.FromOtherDomain = fromOtherDomain;
            this.FromOtherDomainDirect = fromOtherDomainDirect;
        }

        public TestInterface1 ToTestInterface1() {
            var res = new TestInterface1Struct();
            res.If1Field_overriden = this.If1Field_overriden;
            res.If1Field_inherited = this.If1Field_inherited;
            res.SameField = this.SameField;
            res.SameEverywhereField = this.SameEverywhereField;
            res.FromOtherDomain = this.FromOtherDomain;
            res.FromOtherDomainDirect = this.FromOtherDomainDirect;
            return res;
        }

        public void LoadTestInterface1(TestInterface1 value) {
            this.If1Field_overriden = value.If1Field_overriden;
            this.If1Field_inherited = value.If1Field_inherited;
            this.SameField = value.SameField;
            this.SameEverywhereField = value.SameEverywhereField;
            this.FromOtherDomain = value.FromOtherDomain;
            this.FromOtherDomainDirect = value.FromOtherDomainDirect;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TestInterface1Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TestInterface1Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TestInterface1Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TestInterface1.");
            }

            return tpe;
        }

        static TestInterface1Struct() {
            var type = typeof(TestInterface1);
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
                            TestInterface1Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TestInterface1Struct_JsonNetConverter: JsonNetConverter<TestInterface1Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterface1Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterface1Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("if1Field_overriden");
            writer.WriteValue(v.If1Field_overriden);
            writer.WritePropertyName("if1Field_inherited");
            writer.WriteValue(v.If1Field_inherited);
            writer.WritePropertyName("sameField");
            writer.WriteValue(v.SameField);
            writer.WritePropertyName("sameEverywhereField");
            writer.WriteValue(v.SameEverywhereField);
            writer.WritePropertyName("fromOtherDomain");
            writer.WriteValue(v.FromOtherDomain.ToString());
            writer.WritePropertyName("fromOtherDomainDirect");
            writer.WriteValue(v.FromOtherDomainDirect.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestInterface1Struct ReadJson(JsonReader reader, System.Type objectType, TestInterface1Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestInterface1Struct(
                json["if1Field_overriden"].Value<int>(), 
                json["if1Field_inherited"].Value<int>(), 
                json["sameField"].Value<long>(), 
                json["sameEverywhereField"].Value<long>(), 
                Izumi.Test.Domain01.TestValIdentifier.From(json["fromOtherDomain"].Value<string>()), 
                Izumi.Test.Domain01.TestValIdentifier.From(json["fromOtherDomainDirect"].Value<string>())
            );
        }
    }
}