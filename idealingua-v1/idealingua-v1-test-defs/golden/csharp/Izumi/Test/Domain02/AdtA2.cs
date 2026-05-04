// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(AdtA2_JsonNetConverter))]
    public class AdtA2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02";
        public static readonly string RTTI_CLASSNAME = "AdtA2";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.AdtA2";
        public string GetPackageName() { return AdtA2.RTTI_PACKAGE; }
        public string GetClassName() { return AdtA2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AdtA2.RTTI_FULLCLASSNAME; }

        public int B { get; set; }

        public AdtA2() {
        }

        public AdtA2(int b) {
            this.B = b;
        }

    }
    public class AdtA2_JsonNetConverter: JsonNetConverter<AdtA2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AdtA2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AdtA2 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("b");
            writer.WriteValue(v.B);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AdtA2 ReadJson(JsonReader reader, System.Type objectType, AdtA2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new AdtA2(
                json["b"].Value<int>()
            );
        }
    }
}