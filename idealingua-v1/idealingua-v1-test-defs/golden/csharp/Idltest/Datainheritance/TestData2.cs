// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Datainheritance {
    [JsonConverter(typeof(TestData2_JsonNetConverter))]
    public class TestData2 {
        public static readonly string RTTI_PACKAGE = "idltest.datainheritance";
        public static readonly string RTTI_CLASSNAME = "TestData2";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.datainheritance.TestData2";
        public string GetPackageName() { return TestData2.RTTI_PACKAGE; }
        public string GetClassName() { return TestData2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestData2.RTTI_FULLCLASSNAME; }

        public string Str { get; set; }
        public int I32 { get; set; }
        public sbyte Value { get; set; }

        public TestData2() {
        }

        public TestData2(string str, int i32, sbyte value) {
            this.Str = str;
            this.I32 = i32;
            this.Value = value;
        }

    }
    public class TestData2_JsonNetConverter: JsonNetConverter<TestData2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestData2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestData2 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("str");
            writer.WriteValue(v.Str);
            writer.WritePropertyName("i32");
            writer.WriteValue(v.I32);
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestData2 ReadJson(JsonReader reader, System.Type objectType, TestData2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestData2(
                json["str"].Value<string>(), 
                json["i32"].Value<int>(), 
                json["value"].Value<sbyte>()
            );
        }
    }
}