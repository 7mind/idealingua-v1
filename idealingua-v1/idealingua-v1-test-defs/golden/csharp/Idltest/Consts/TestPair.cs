// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Consts {
    [JsonConverter(typeof(TestPair_JsonNetConverter))]
    public class TestPair {
        public static readonly string RTTI_PACKAGE = "idltest.consts";
        public static readonly string RTTI_CLASSNAME = "TestPair";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.consts.TestPair";
        public string GetPackageName() { return TestPair.RTTI_PACKAGE; }
        public string GetClassName() { return TestPair.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestPair.RTTI_FULLCLASSNAME; }

        public int Value { get; set; }
        public string Name { get; set; }

        public TestPair() {
        }

        public TestPair(int value, string name) {
            this.Value = value;
            this.Name = name;
        }

    }
    public class TestPair_JsonNetConverter: JsonNetConverter<TestPair> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestPair_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestPair v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestPair ReadJson(JsonReader reader, System.Type objectType, TestPair existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestPair(
                json["value"].Value<int>(), 
                json["name"].Value<string>()
            );
        }
    }
}