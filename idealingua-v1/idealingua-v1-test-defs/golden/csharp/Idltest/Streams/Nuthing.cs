// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Streams {
    [JsonConverter(typeof(Nuthing_JsonNetConverter))]
    public class Nuthing {
        public static readonly string RTTI_PACKAGE = "idltest.streams";
        public static readonly string RTTI_CLASSNAME = "Nuthing";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.streams.Nuthing";
        public string GetPackageName() { return Nuthing.RTTI_PACKAGE; }
        public string GetClassName() { return Nuthing.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Nuthing.RTTI_FULLCLASSNAME; }

        public Nuthing() {
        }

    }
    public class Nuthing_JsonNetConverter: JsonNetConverter<Nuthing> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Nuthing_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Nuthing v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Nuthing ReadJson(JsonReader reader, System.Type objectType, Nuthing existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new Nuthing(

            );
        }
    }
}