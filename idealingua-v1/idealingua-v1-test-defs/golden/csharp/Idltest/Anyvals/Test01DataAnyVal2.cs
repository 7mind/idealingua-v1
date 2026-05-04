// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(Test01DataAnyVal2_JsonNetConverter))]
    public class Test01DataAnyVal2 {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "Test01DataAnyVal2";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.Test01DataAnyVal2";
        public string GetPackageName() { return Test01DataAnyVal2.RTTI_PACKAGE; }
        public string GetClassName() { return Test01DataAnyVal2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Test01DataAnyVal2.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }
        public sbyte SomeInt { get; set; }

        public Test01DataAnyVal2() {
        }

        public Test01DataAnyVal2(string value, sbyte someInt) {
            this.Value = value;
            this.SomeInt = someInt;
        }

    }
    public class Test01DataAnyVal2_JsonNetConverter: JsonNetConverter<Test01DataAnyVal2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test01DataAnyVal2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test01DataAnyVal2 v, JsonSerializer serializer) {
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
        public override Test01DataAnyVal2 ReadJson(JsonReader reader, System.Type objectType, Test01DataAnyVal2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Test01DataAnyVal2(
                json["value"].Value<string>(), 
                json["someInt"].Value<sbyte>()
            );
        }
    }
}