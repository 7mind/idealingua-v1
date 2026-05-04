// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Json {
    [JsonConverter(typeof(JLBool_JsonNetConverter))]
    public class JLBool {
        public static readonly string RTTI_PACKAGE = "idltest.json";
        public static readonly string RTTI_CLASSNAME = "JLBool";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.json.JLBool";
        public string GetPackageName() { return JLBool.RTTI_PACKAGE; }
        public string GetClassName() { return JLBool.RTTI_CLASSNAME; }
        public string GetFullClassName() { return JLBool.RTTI_FULLCLASSNAME; }

        public bool Value { get; set; }

        public JLBool() {
        }

        public JLBool(bool value) {
            this.Value = value;
        }

    }
    public class JLBool_JsonNetConverter: JsonNetConverter<JLBool> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public JLBool_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, JLBool v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override JLBool ReadJson(JsonReader reader, System.Type objectType, JLBool existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new JLBool(
                json["value"].Value<bool>()
            );
        }
    }
}