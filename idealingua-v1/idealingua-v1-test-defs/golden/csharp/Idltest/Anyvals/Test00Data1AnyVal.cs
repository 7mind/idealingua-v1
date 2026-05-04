// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(Test00Data1AnyVal_JsonNetConverter))]
    public class Test00Data1AnyVal {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "Test00Data1AnyVal";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.Test00Data1AnyVal";
        public string GetPackageName() { return Test00Data1AnyVal.RTTI_PACKAGE; }
        public string GetClassName() { return Test00Data1AnyVal.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Test00Data1AnyVal.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public Test00Data1AnyVal() {
        }

        public Test00Data1AnyVal(string value) {
            this.Value = value;
        }

    }
    public class Test00Data1AnyVal_JsonNetConverter: JsonNetConverter<Test00Data1AnyVal> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test00Data1AnyVal_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test00Data1AnyVal v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Test00Data1AnyVal ReadJson(JsonReader reader, System.Type objectType, Test00Data1AnyVal existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Test00Data1AnyVal(
                json["value"].Value<string>()
            );
        }
    }
}