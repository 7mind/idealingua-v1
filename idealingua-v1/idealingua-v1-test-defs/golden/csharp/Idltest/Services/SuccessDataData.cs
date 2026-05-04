// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Services {
    [JsonConverter(typeof(SuccessDataData_JsonNetConverter))]
    public class SuccessDataData : SuccessData {
        public static readonly string RTTI_PACKAGE = "idltest.services";
        public static readonly string RTTI_CLASSNAME = "SuccessDataData";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.services.SuccessDataData";
        public string GetPackageName() { return SuccessDataData.RTTI_PACKAGE; }
        public string GetClassName() { return SuccessDataData.RTTI_CLASSNAME; }
        public string GetFullClassName() { return SuccessDataData.RTTI_FULLCLASSNAME; }

        public string Greeting { get; set; }

        public SuccessDataData() {
        }

        public SuccessDataData(string greeting) {
            this.Greeting = greeting;
        }

        public SuccessData ToSuccessData() {
            var res = new SuccessDataStruct();
            res.Greeting = this.Greeting;
            return res;
        }

        public void LoadSuccessData(SuccessData value) {
            this.Greeting = value.Greeting;
        }

    }
    public class SuccessDataData_JsonNetConverter: JsonNetConverter<SuccessDataData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SuccessDataData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SuccessDataData v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("greeting");
            writer.WriteValue(v.Greeting);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SuccessDataData ReadJson(JsonReader reader, System.Type objectType, SuccessDataData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new SuccessDataData(
                json["greeting"].Value<string>()
            );
        }
    }
}