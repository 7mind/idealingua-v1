// Auto-generated, any modifications may be overwritten in the future.

using Idltest.Aliases2;
using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Aliases {
    [JsonConverter(typeof(D1_JsonNetConverter))]
    public class D1 : M1, M2 {
        public static readonly string RTTI_PACKAGE = "idltest.aliases";
        public static readonly string RTTI_CLASSNAME = "D1";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.aliases.D1";
        public string GetPackageName() { return D1.RTTI_PACKAGE; }
        public string GetClassName() { return D1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return D1.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }
        public string F2 { get; set; }

        public D1() {
        }

        public D1(string value, string f2) {
            this.Value = value;
            this.F2 = f2;
        }

        public M1 ToM1() {
            var res = new M1Struct();
            res.Value = this.Value;
            return res;
        }

        public void LoadM1(M1 value) {
            this.Value = value.Value;
        }

        public M2 ToM2() {
            var res = new M2Struct();
            res.F2 = this.F2;
            return res;
        }

        public void LoadM2(M2 value) {
            this.F2 = value.F2;
        }

    }
    public class D1_JsonNetConverter: JsonNetConverter<D1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public D1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, D1 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WritePropertyName("f2");
            writer.WriteValue(v.F2);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override D1 ReadJson(JsonReader reader, System.Type objectType, D1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new D1(
                json["value"].Value<string>(), 
                json["f2"].Value<string>()
            );
        }
    }
}