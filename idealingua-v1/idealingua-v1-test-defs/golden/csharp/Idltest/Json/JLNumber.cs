// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Json {
    [JsonConverter(typeof(JLNumber_JsonNetConverter))]
    public class JLNumber {
        public static readonly string RTTI_PACKAGE = "idltest.json";
        public static readonly string RTTI_CLASSNAME = "JLNumber";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.json.JLNumber";
        public string GetPackageName() { return JLNumber.RTTI_PACKAGE; }
        public string GetClassName() { return JLNumber.RTTI_CLASSNAME; }
        public string GetFullClassName() { return JLNumber.RTTI_FULLCLASSNAME; }

        public double Value { get; set; }

        public JLNumber() {
        }

        public JLNumber(double value) {
            this.Value = value;
        }

    }
    public class JLNumber_JsonNetConverter: JsonNetConverter<JLNumber> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public JLNumber_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, JLNumber v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override JLNumber ReadJson(JsonReader reader, System.Type objectType, JLNumber existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new JLNumber(
                json["value"].Value<double>()
            );
        }
    }
}