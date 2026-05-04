// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Json {
    [JsonConverter(typeof(JLNull_JsonNetConverter))]
    public class JLNull {
        public static readonly string RTTI_PACKAGE = "idltest.json";
        public static readonly string RTTI_CLASSNAME = "JLNull";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.json.JLNull";
        public string GetPackageName() { return JLNull.RTTI_PACKAGE; }
        public string GetClassName() { return JLNull.RTTI_CLASSNAME; }
        public string GetFullClassName() { return JLNull.RTTI_FULLCLASSNAME; }

        public JLNull() {
        }

    }
    public class JLNull_JsonNetConverter: JsonNetConverter<JLNull> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public JLNull_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, JLNull v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override JLNull ReadJson(JsonReader reader, System.Type objectType, JLNull existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new JLNull(

            );
        }
    }
}