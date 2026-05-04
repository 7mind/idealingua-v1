// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Syntax {
    [JsonConverter(typeof(TestDto1_JsonNetConverter))]
    public class TestDto1 {
        public static readonly string RTTI_PACKAGE = "idltest.syntax";
        public static readonly string RTTI_CLASSNAME = "TestDto1";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.syntax.TestDto1";
        public string GetPackageName() { return TestDto1.RTTI_PACKAGE; }
        public string GetClassName() { return TestDto1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestDto1.RTTI_FULLCLASSNAME; }

        public Idltest.Syntax.TestMixin Value { get; set; }

        public TestDto1() {
        }

        public TestDto1(Idltest.Syntax.TestMixin value) {
            this.Value = value;
        }

    }
    public class TestDto1_JsonNetConverter: JsonNetConverter<TestDto1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestDto1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestDto1 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            // Serializing polymorphic type TestMixin
            writer.WriteStartObject();
            writer.WritePropertyName(v.Value.GetFullClassName());
            serializer.Serialize(writer, v.Value);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestDto1 ReadJson(JsonReader reader, System.Type objectType, TestDto1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestDto1(
                serializer.Deserialize<Idltest.Syntax.TestMixin>(json["value"].CreateReader())
            );
        }
    }
}