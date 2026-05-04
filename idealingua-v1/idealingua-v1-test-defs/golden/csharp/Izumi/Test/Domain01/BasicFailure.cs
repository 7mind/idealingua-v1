// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(BasicFailure_JsonNetConverter))]
    public class BasicFailure : CommonFailure {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "BasicFailure";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.BasicFailure";
        public string GetPackageName() { return BasicFailure.RTTI_PACKAGE; }
        public string GetClassName() { return BasicFailure.RTTI_CLASSNAME; }
        public string GetFullClassName() { return BasicFailure.RTTI_FULLCLASSNAME; }

        public int Code { get; set; }

        public BasicFailure() {
        }

        public BasicFailure(int code) {
            this.Code = code;
        }

        public CommonFailure ToCommonFailure() {
            var res = new CommonFailureStruct();
            res.Code = this.Code;
            return res;
        }

        public void LoadCommonFailure(CommonFailure value) {
            this.Code = value.Code;
        }

    }
    public class BasicFailure_JsonNetConverter: JsonNetConverter<BasicFailure> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public BasicFailure_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, BasicFailure v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("code");
            writer.WriteValue(v.Code);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override BasicFailure ReadJson(JsonReader reader, System.Type objectType, BasicFailure existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new BasicFailure(
                json["code"].Value<int>()
            );
        }
    }
}