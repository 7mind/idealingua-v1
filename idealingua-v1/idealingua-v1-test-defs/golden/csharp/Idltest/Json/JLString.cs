// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Json {
    [JsonConverter(typeof(JLString_JsonNetConverter))]
    public class JLString {
        public static readonly string RTTI_PACKAGE = "idltest.json";
        public static readonly string RTTI_CLASSNAME = "JLString";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.json.JLString";
        public string GetPackageName() { return JLString.RTTI_PACKAGE; }
        public string GetClassName() { return JLString.RTTI_CLASSNAME; }
        public string GetFullClassName() { return JLString.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public JLString() {
        }

        public JLString(string value) {
            this.Value = value;
        }

    }
    public class JLString_JsonNetConverter: JsonNetConverter<JLString> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public JLString_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, JLString v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override JLString ReadJson(JsonReader reader, System.Type objectType, JLString existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new JLString(
                json["value"].Value<string>()
            );
        }
    }
}