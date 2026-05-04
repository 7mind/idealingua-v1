// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    [JsonConverter(typeof(Failure_JsonNetConverter))]
    public class Failure {
        public static readonly string RTTI_PACKAGE = "idltest.algebraics";
        public static readonly string RTTI_CLASSNAME = "Failure";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.algebraics.Failure";
        public string GetPackageName() { return Failure.RTTI_PACKAGE; }
        public string GetClassName() { return Failure.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Failure.RTTI_FULLCLASSNAME; }

        public sbyte Code { get; set; }

        public Failure() {
        }

        public Failure(sbyte code) {
            this.Code = code;
        }

    }
    public class Failure_JsonNetConverter: JsonNetConverter<Failure> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Failure_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Failure v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("code");
            writer.WriteValue(v.Code);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Failure ReadJson(JsonReader reader, System.Type objectType, Failure existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Failure(
                json["code"].Value<sbyte>()
            );
        }
    }
}