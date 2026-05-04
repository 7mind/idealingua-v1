// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Substraction {
    [JsonConverter(typeof(User2_JsonNetConverter))]
    public class User2 {
        public static readonly string RTTI_PACKAGE = "idltest.substraction";
        public static readonly string RTTI_CLASSNAME = "User2";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.User2";
        public string GetPackageName() { return User2.RTTI_PACKAGE; }
        public string GetClassName() { return User2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return User2.RTTI_FULLCLASSNAME; }

        public string Ssn { get; set; }
        public string Password { get; set; }
        public string Name { get; set; }

        public User2() {
        }

        public User2(string ssn, string password, string name) {
            this.Ssn = ssn;
            this.Password = password;
            this.Name = name;
        }

    }
    public class User2_JsonNetConverter: JsonNetConverter<User2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public User2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, User2 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("ssn");
            writer.WriteValue(v.Ssn);
            writer.WritePropertyName("password");
            writer.WriteValue(v.Password);
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override User2 ReadJson(JsonReader reader, System.Type objectType, User2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new User2(
                json["ssn"].Value<string>(), 
                json["password"].Value<string>(), 
                json["name"].Value<string>()
            );
        }
    }
}