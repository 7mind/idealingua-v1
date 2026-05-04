// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Dtofields {
    [JsonConverter(typeof(NullableObj_JsonNetConverter))]
    public class NullableObj : NullableContent {
        public static readonly string RTTI_PACKAGE = "idltest.dtofields";
        public static readonly string RTTI_CLASSNAME = "NullableObj";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.dtofields.NullableObj";
        public string GetPackageName() { return NullableObj.RTTI_PACKAGE; }
        public string GetClassName() { return NullableObj.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NullableObj.RTTI_FULLCLASSNAME; }

        public int A { get; set; }

        public NullableObj() {
        }

        public NullableObj(int a) {
            this.A = a;
        }

        public NullableContent ToNullableContent() {
            var res = new NullableContentStruct();
            res.A = this.A;
            return res;
        }

        public void LoadNullableContent(NullableContent value) {
            this.A = value.A;
        }

    }
    public class NullableObj_JsonNetConverter: JsonNetConverter<NullableObj> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NullableObj_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NullableObj v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NullableObj ReadJson(JsonReader reader, System.Type objectType, NullableObj existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new NullableObj(
                json["a"].Value<int>()
            );
        }
    }
}