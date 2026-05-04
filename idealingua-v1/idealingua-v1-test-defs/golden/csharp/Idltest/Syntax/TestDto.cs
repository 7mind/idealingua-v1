// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Syntax {
    [JsonConverter(typeof(TestDto_JsonNetConverter))]
    public class TestDto {
        public static readonly string RTTI_PACKAGE = "idltest.syntax";
        public static readonly string RTTI_CLASSNAME = "TestDto";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.syntax.TestDto";
        public string GetPackageName() { return TestDto.RTTI_PACKAGE; }
        public string GetClassName() { return TestDto.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestDto.RTTI_FULLCLASSNAME; }

        public Idltest.Syntax.TestMixin Value { get; set; }

        public TestDto() {
        }

        public TestDto(Idltest.Syntax.TestMixin value) {
            this.Value = value;
        }

    }
    public class TestDto_JsonNetConverter: JsonNetConverter<TestDto> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestDto_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestDto v, JsonSerializer serializer) {
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
        public override TestDto ReadJson(JsonReader reader, System.Type objectType, TestDto existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestDto(
                serializer.Deserialize<Idltest.Syntax.TestMixin>(json["value"].CreateReader())
            );
        }
    }
}