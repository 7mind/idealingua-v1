// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    [JsonConverter(typeof(EventData_JsonNetConverter))]
    public class EventData {
        public static readonly string RTTI_PACKAGE = "idltest.ast";
        public static readonly string RTTI_CLASSNAME = "EventData";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.EventData";
        public string GetPackageName() { return EventData.RTTI_PACKAGE; }
        public string GetClassName() { return EventData.RTTI_CLASSNAME; }
        public string GetFullClassName() { return EventData.RTTI_FULLCLASSNAME; }

        public EventData() {
        }

    }
    public class EventData_JsonNetConverter: JsonNetConverter<EventData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public EventData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, EventData v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override EventData ReadJson(JsonReader reader, System.Type objectType, EventData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new EventData(

            );
        }
    }
}