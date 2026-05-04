// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(RTestObject1_JsonNetConverter))]
    public class RTestObject1 : RtestMixin2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "RTestObject1";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.RTestObject1";
        public string GetPackageName() { return RTestObject1.RTTI_PACKAGE; }
        public string GetClassName() { return RTestObject1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return RTestObject1.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.RTestMixin B { get; set; }

        public RTestObject1() {
        }

        public RTestObject1(Izumi.Test.Domain01.RTestMixin b) {
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
    public class RTestObject1_JsonNetConverter: JsonNetConverter<RTestObject1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RTestObject1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RTestObject1 v, JsonSerializer serializer) {
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
        public override RTestObject1 ReadJson(JsonReader reader, System.Type objectType, RTestObject1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new RTestObject1(
                serializer.Deserialize<Izumi.Test.Domain01.RTestMixin>(json["b"].CreateReader())
            );
        }
    }
}