// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    [JsonConverter(typeof(ComplexAdt_JsonNetConverter))]
    public class ComplexAdt {
        public static readonly string RTTI_PACKAGE = "idltest.algebraics";
        public static readonly string RTTI_CLASSNAME = "ComplexAdt";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.algebraics.ComplexAdt";
        public string GetPackageName() { return ComplexAdt.RTTI_PACKAGE; }
        public string GetClassName() { return ComplexAdt.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ComplexAdt.RTTI_FULLCLASSNAME; }

        public Idltest.Algebraics.AdtTestID Id { get; set; }

        public ComplexAdt() {
        }

        public ComplexAdt(Idltest.Algebraics.AdtTestID id) {
            this.Id = id;
        }

    }
    public class ComplexAdt_JsonNetConverter: JsonNetConverter<ComplexAdt> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ComplexAdt_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ComplexAdt v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ComplexAdt ReadJson(JsonReader reader, System.Type objectType, ComplexAdt existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new ComplexAdt(
                Idltest.Algebraics.AdtTestID.From(json["id"].Value<string>())
            );
        }
    }
}