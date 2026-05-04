// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Syntax {
    [JsonConverter(typeof(TestOneliners_JsonNetConverter))]
    public class TestOneliners {
        public static readonly string RTTI_PACKAGE = "idltest.syntax";
        public static readonly string RTTI_CLASSNAME = "TestOneliners";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.syntax.TestOneliners";
        public string GetPackageName() { return TestOneliners.RTTI_PACKAGE; }
        public string GetClassName() { return TestOneliners.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestOneliners.RTTI_FULLCLASSNAME; }

        public Idltest.Syntax.TestDto TestDto { get; set; }
        public string Str { get; set; }
        public sbyte I08 { get; set; }

        public TestOneliners() {
        }

        public TestOneliners(Idltest.Syntax.TestDto testDto, string str, sbyte i08) {
            this.TestDto = testDto;
            this.Str = str;
            this.I08 = i08;
        }

    }
    public class TestOneliners_JsonNetConverter: JsonNetConverter<TestOneliners> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestOneliners_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestOneliners v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("testDto");
            serializer.Serialize(writer, v.TestDto);
            writer.WritePropertyName("str");
            writer.WriteValue(v.Str);
            writer.WritePropertyName("i08");
            writer.WriteValue(v.I08);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestOneliners ReadJson(JsonReader reader, System.Type objectType, TestOneliners existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _testDto = serializer.Deserialize<Idltest.Syntax.TestDto>(json["testDto"].CreateReader());
            return new TestOneliners(
                _testDto, 
                json["str"].Value<string>(), 
                json["i08"].Value<sbyte>()
            );
        }
    }
}