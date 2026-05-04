// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(Test00Data2AnyVal_JsonNetConverter))]
    public class Test00Data2AnyVal {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "Test00Data2AnyVal";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.Test00Data2AnyVal";
        public string GetPackageName() { return Test00Data2AnyVal.RTTI_PACKAGE; }
        public string GetClassName() { return Test00Data2AnyVal.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Test00Data2AnyVal.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }
        public sbyte SomeInt { get; set; }

        public Test00Data2AnyVal() {
        }

        public Test00Data2AnyVal(string value, sbyte someInt) {
            this.Value = value;
            this.SomeInt = someInt;
        }

    }
    public class Test00Data2AnyVal_JsonNetConverter: JsonNetConverter<Test00Data2AnyVal> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test00Data2AnyVal_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test00Data2AnyVal v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WritePropertyName("someInt");
            writer.WriteValue(v.SomeInt);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Test00Data2AnyVal ReadJson(JsonReader reader, System.Type objectType, Test00Data2AnyVal existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Test00Data2AnyVal(
                json["value"].Value<string>(), 
                json["someInt"].Value<sbyte>()
            );
        }
    }
}