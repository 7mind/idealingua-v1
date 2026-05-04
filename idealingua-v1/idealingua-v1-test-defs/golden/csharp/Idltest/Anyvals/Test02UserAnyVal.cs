// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(Test02UserAnyVal_JsonNetConverter))]
    public class Test02UserAnyVal {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "Test02UserAnyVal";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.Test02UserAnyVal";
        public string GetPackageName() { return Test02UserAnyVal.RTTI_PACKAGE; }
        public string GetClassName() { return Test02UserAnyVal.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Test02UserAnyVal.RTTI_FULLCLASSNAME; }

        public Idltest.Anyvals.Test02DtoAnyVal Test02DtoAnyVal { get; set; }
        public sbyte I08 { get; set; }

        public Test02UserAnyVal() {
        }

        public Test02UserAnyVal(Idltest.Anyvals.Test02DtoAnyVal test02DtoAnyVal, sbyte i08) {
            this.Test02DtoAnyVal = test02DtoAnyVal;
            this.I08 = i08;
        }

    }
    public class Test02UserAnyVal_JsonNetConverter: JsonNetConverter<Test02UserAnyVal> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test02UserAnyVal_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test02UserAnyVal v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("test02DtoAnyVal");
            serializer.Serialize(writer, v.Test02DtoAnyVal);
            writer.WritePropertyName("i08");
            writer.WriteValue(v.I08);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Test02UserAnyVal ReadJson(JsonReader reader, System.Type objectType, Test02UserAnyVal existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _test02DtoAnyVal = serializer.Deserialize<Idltest.Anyvals.Test02DtoAnyVal>(json["test02DtoAnyVal"].CreateReader());
            return new Test02UserAnyVal(
                _test02DtoAnyVal, 
                json["i08"].Value<sbyte>()
            );
        }
    }
}