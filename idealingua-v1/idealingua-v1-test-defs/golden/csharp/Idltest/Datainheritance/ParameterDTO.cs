// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Datainheritance {
    [JsonConverter(typeof(ParameterDTO_JsonNetConverter))]
    public class ParameterDTO {
        public static readonly string RTTI_PACKAGE = "idltest.datainheritance";
        public static readonly string RTTI_CLASSNAME = "ParameterDTO";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.datainheritance.ParameterDTO";
        public string GetPackageName() { return ParameterDTO.RTTI_PACKAGE; }
        public string GetClassName() { return ParameterDTO.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ParameterDTO.RTTI_FULLCLASSNAME; }

        public int I32 { get; set; }
        public string Str { get; set; }

        public ParameterDTO() {
        }

        public ParameterDTO(int i32, string str) {
            this.I32 = i32;
            this.Str = str;
        }

    }
    public class ParameterDTO_JsonNetConverter: JsonNetConverter<ParameterDTO> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ParameterDTO_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ParameterDTO v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("i32");
            writer.WriteValue(v.I32);
            writer.WritePropertyName("str");
            writer.WriteValue(v.Str);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ParameterDTO ReadJson(JsonReader reader, System.Type objectType, ParameterDTO existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new ParameterDTO(
                json["i32"].Value<int>(), 
                json["str"].Value<string>()
            );
        }
    }
}