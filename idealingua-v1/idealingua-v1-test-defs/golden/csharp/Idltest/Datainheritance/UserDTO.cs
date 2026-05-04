// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Datainheritance {
    [JsonConverter(typeof(UserDTO_JsonNetConverter))]
    public class UserDTO {
        public static readonly string RTTI_PACKAGE = "idltest.datainheritance";
        public static readonly string RTTI_CLASSNAME = "UserDTO";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.datainheritance.UserDTO";
        public string GetPackageName() { return UserDTO.RTTI_PACKAGE; }
        public string GetClassName() { return UserDTO.RTTI_CLASSNAME; }
        public string GetFullClassName() { return UserDTO.RTTI_FULLCLASSNAME; }

        public Idltest.Datainheritance.ParameterDTO Value { get; set; }

        public UserDTO() {
        }

        public UserDTO(Idltest.Datainheritance.ParameterDTO value) {
            this.Value = value;
        }

    }
    public class UserDTO_JsonNetConverter: JsonNetConverter<UserDTO> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public UserDTO_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, UserDTO v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            serializer.Serialize(writer, v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override UserDTO ReadJson(JsonReader reader, System.Type objectType, UserDTO existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _value = serializer.Deserialize<Idltest.Datainheritance.ParameterDTO>(json["value"].CreateReader());
            return new UserDTO(
                _value
            );
        }
    }
}