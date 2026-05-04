// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(Test01DataAnyVal1_JsonNetConverter))]
    public class Test01DataAnyVal1 : Test01MixinAnyVal {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "Test01DataAnyVal1";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.Test01DataAnyVal1";
        public string GetPackageName() { return Test01DataAnyVal1.RTTI_PACKAGE; }
        public string GetClassName() { return Test01DataAnyVal1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Test01DataAnyVal1.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }
        public sbyte SomeInt { get; set; }

        public Test01DataAnyVal1() {
        }

        public Test01DataAnyVal1(string value, sbyte someInt) {
            this.Value = value;
            this.SomeInt = someInt;
        }

        public Test01MixinAnyVal ToTest01MixinAnyVal() {
            var res = new Test01MixinAnyValStruct();
            res.Value = this.Value;
            return res;
        }

        public void LoadTest01MixinAnyVal(Test01MixinAnyVal value) {
            this.Value = value.Value;
        }

    }
    public class Test01DataAnyVal1_JsonNetConverter: JsonNetConverter<Test01DataAnyVal1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test01DataAnyVal1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test01DataAnyVal1 v, JsonSerializer serializer) {
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
        public override Test01DataAnyVal1 ReadJson(JsonReader reader, System.Type objectType, Test01DataAnyVal1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Test01DataAnyVal1(
                json["value"].Value<string>(), 
                json["someInt"].Value<sbyte>()
            );
        }
    }
}