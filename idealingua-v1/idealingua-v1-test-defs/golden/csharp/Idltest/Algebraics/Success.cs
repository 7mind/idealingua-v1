// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    [JsonConverter(typeof(Success_JsonNetConverter))]
    public class Success {
        public static readonly string RTTI_PACKAGE = "idltest.algebraics";
        public static readonly string RTTI_CLASSNAME = "Success";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.algebraics.Success";
        public string GetPackageName() { return Success.RTTI_PACKAGE; }
        public string GetClassName() { return Success.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Success.RTTI_FULLCLASSNAME; }

        public string Message { get; set; }

        public Success() {
        }

        public Success(string message) {
            this.Message = message;
        }

    }
    public class Success_JsonNetConverter: JsonNetConverter<Success> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Success_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Success v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("message");
            writer.WriteValue(v.Message);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Success ReadJson(JsonReader reader, System.Type objectType, Success existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Success(
                json["message"].Value<string>()
            );
        }
    }
}