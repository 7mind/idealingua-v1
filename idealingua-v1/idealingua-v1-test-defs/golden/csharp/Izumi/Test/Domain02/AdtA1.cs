// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(AdtA1_JsonNetConverter))]
    public class AdtA1 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02";
        public static readonly string RTTI_CLASSNAME = "AdtA1";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.AdtA1";
        public string GetPackageName() { return AdtA1.RTTI_PACKAGE; }
        public string GetClassName() { return AdtA1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AdtA1.RTTI_FULLCLASSNAME; }

        public int A { get; set; }

        public AdtA1() {
        }

        public AdtA1(int a) {
            this.A = a;
        }

    }
    public class AdtA1_JsonNetConverter: JsonNetConverter<AdtA1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AdtA1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AdtA1 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AdtA1 ReadJson(JsonReader reader, System.Type objectType, AdtA1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new AdtA1(
                json["a"].Value<int>()
            );
        }
    }
}