// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Datainheritance {
    [JsonConverter(typeof(TestData1_JsonNetConverter))]
    public class TestData1 {
        public static readonly string RTTI_PACKAGE = "idltest.datainheritance";
        public static readonly string RTTI_CLASSNAME = "TestData1";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.datainheritance.TestData1";
        public string GetPackageName() { return TestData1.RTTI_PACKAGE; }
        public string GetClassName() { return TestData1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestData1.RTTI_FULLCLASSNAME; }

        public string Str { get; set; }
        public int I32 { get; set; }

        public TestData1() {
        }

        public TestData1(string str, int i32) {
            this.Str = str;
            this.I32 = i32;
        }

    }
    public class TestData1_JsonNetConverter: JsonNetConverter<TestData1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestData1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestData1 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("str");
            writer.WriteValue(v.Str);
            writer.WritePropertyName("i32");
            writer.WriteValue(v.I32);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestData1 ReadJson(JsonReader reader, System.Type objectType, TestData1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestData1(
                json["str"].Value<string>(), 
                json["i32"].Value<int>()
            );
        }
    }
}