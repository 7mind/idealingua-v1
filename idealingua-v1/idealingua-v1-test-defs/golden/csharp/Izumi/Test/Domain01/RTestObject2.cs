// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(RTestObject2_JsonNetConverter))]
    public class RTestObject2 : RtestMixin2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "RTestObject2";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.RTestObject2";
        public string GetPackageName() { return RTestObject2.RTTI_PACKAGE; }
        public string GetClassName() { return RTestObject2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return RTestObject2.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.RTestMixin B { get; set; }

        public RTestObject2() {
        }

        public RTestObject2(Izumi.Test.Domain01.RTestMixin b) {
            this.B = b;
        }

        public RtestMixin2 ToRtestMixin2() {
            var res = new RtestMixin2Struct();
            res.B = this.B;
            return res;
        }

        public void LoadRtestMixin2(RtestMixin2 value) {
            this.B = value.B;
        }

    }
    public class RTestObject2_JsonNetConverter: JsonNetConverter<RTestObject2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RTestObject2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RTestObject2 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("b");
            // Serializing polymorphic type RTestMixin
            writer.WriteStartObject();
            writer.WritePropertyName(v.B.GetFullClassName());
            serializer.Serialize(writer, v.B);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RTestObject2 ReadJson(JsonReader reader, System.Type objectType, RTestObject2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new RTestObject2(
                serializer.Deserialize<Izumi.Test.Domain01.RTestMixin>(json["b"].CreateReader())
            );
        }
    }
}