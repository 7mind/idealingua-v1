// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    [JsonConverter(typeof(ComplexAdt2_JsonNetConverter))]
    public class ComplexAdt2 {
        public static readonly string RTTI_PACKAGE = "idltest.algebraics";
        public static readonly string RTTI_CLASSNAME = "ComplexAdt2";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.algebraics.ComplexAdt2";
        public string GetPackageName() { return ComplexAdt2.RTTI_PACKAGE; }
        public string GetClassName() { return ComplexAdt2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ComplexAdt2.RTTI_FULLCLASSNAME; }

        public Idltest.Algebraics.AdtTestID Id { get; set; }

        public ComplexAdt2() {
        }

        public ComplexAdt2(Idltest.Algebraics.AdtTestID id) {
            this.Id = id;
        }

    }
    public class ComplexAdt2_JsonNetConverter: JsonNetConverter<ComplexAdt2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ComplexAdt2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ComplexAdt2 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ComplexAdt2 ReadJson(JsonReader reader, System.Type objectType, ComplexAdt2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new ComplexAdt2(
                Idltest.Algebraics.AdtTestID.From(json["id"].Value<string>())
            );
        }
    }
}