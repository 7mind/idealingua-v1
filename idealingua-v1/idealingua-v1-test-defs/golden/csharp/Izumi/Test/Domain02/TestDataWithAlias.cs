// Auto-generated, any modifications may be overwritten in the future.

using Izumi.Test.Domain01;
using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(TestDataWithAlias_JsonNetConverter))]
    public class TestDataWithAlias {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02";
        public static readonly string RTTI_CLASSNAME = "TestDataWithAlias";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.TestDataWithAlias";
        public string GetPackageName() { return TestDataWithAlias.RTTI_PACKAGE; }
        public string GetClassName() { return TestDataWithAlias.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestDataWithAlias.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.RTestEnum A { get; set; }

        public TestDataWithAlias() {
        }

        public TestDataWithAlias(Izumi.Test.Domain01.RTestEnum a) {
            this.A = a;
        }

    }
    public class TestDataWithAlias_JsonNetConverter: JsonNetConverter<TestDataWithAlias> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestDataWithAlias_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestDataWithAlias v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestDataWithAlias ReadJson(JsonReader reader, System.Type objectType, TestDataWithAlias existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TestDataWithAlias(
                Izumi.Test.Domain01.RTestEnumHelpers.From(json["a"].Value<string>())
            );
        }
    }
}