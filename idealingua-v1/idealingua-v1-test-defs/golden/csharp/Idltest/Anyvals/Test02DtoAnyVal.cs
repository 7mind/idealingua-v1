// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(Test02DtoAnyVal_JsonNetConverter))]
    public class Test02DtoAnyVal {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "Test02DtoAnyVal";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.Test02DtoAnyVal";
        public string GetPackageName() { return Test02DtoAnyVal.RTTI_PACKAGE; }
        public string GetClassName() { return Test02DtoAnyVal.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Test02DtoAnyVal.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public Test02DtoAnyVal() {
        }

        public Test02DtoAnyVal(string value) {
            this.Value = value;
        }

    }
    public class Test02DtoAnyVal_JsonNetConverter: JsonNetConverter<Test02DtoAnyVal> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test02DtoAnyVal_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test02DtoAnyVal v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Test02DtoAnyVal ReadJson(JsonReader reader, System.Type objectType, Test02DtoAnyVal existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Test02DtoAnyVal(
                json["value"].Value<string>()
            );
        }
    }
}