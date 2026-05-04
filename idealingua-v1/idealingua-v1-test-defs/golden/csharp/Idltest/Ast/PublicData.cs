// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    [JsonConverter(typeof(PublicData_JsonNetConverter))]
    public class PublicData {
        public static readonly string RTTI_PACKAGE = "idltest.ast";
        public static readonly string RTTI_CLASSNAME = "PublicData";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.PublicData";
        public string GetPackageName() { return PublicData.RTTI_PACKAGE; }
        public string GetClassName() { return PublicData.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PublicData.RTTI_FULLCLASSNAME; }

        public PublicData() {
        }

    }
    public class PublicData_JsonNetConverter: JsonNetConverter<PublicData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PublicData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PublicData v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PublicData ReadJson(JsonReader reader, System.Type objectType, PublicData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new PublicData(

            );
        }
    }
}