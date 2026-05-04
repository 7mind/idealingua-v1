// Auto-generated, any modifications may be overwritten in the future.

using IRT;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Substraction {
    [JsonConverter(typeof(SecurityAttributes_JsonNetConverter))]
    public interface SecurityAttributes: PersonalAttributes, IRTTI {
        string Password { get; set; }
    }
    public class SecurityAttributes_JsonNetConverter: JsonNetConverter<SecurityAttributes> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SecurityAttributes_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SecurityAttributes value, JsonSerializer serializer) {
            // Serializing polymorphic type SecurityAttributes
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SecurityAttributes ReadJson(JsonReader reader, System.Type objectType, SecurityAttributes existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = SecurityAttributesStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (SecurityAttributes)res;
        }
    }

    [JsonConverter(typeof(SecurityAttributesStruct_JsonNetConverter))]
    public class SecurityAttributesStruct : PersonalAttributes, SecurityAttributes {
        public static readonly string RTTI_PACKAGE = "idltest.substraction.SecurityAttributes";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.SecurityAttributes.Struct";
        public string GetPackageName() { return SecurityAttributesStruct.RTTI_PACKAGE; }
        public string GetClassName() { return SecurityAttributesStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return SecurityAttributesStruct.RTTI_FULLCLASSNAME; }

        public string Ssn { get; set; }
        public string Password { get; set; }

        public SecurityAttributesStruct() {
        }

        public SecurityAttributesStruct(string ssn, string password) {
            this.Ssn = ssn;
            this.Password = password;
        }

        public PersonalAttributes ToPersonalAttributes() {
            var res = new PersonalAttributesStruct();
            res.Ssn = this.Ssn;
            return res;
        }

        public void LoadPersonalAttributes(PersonalAttributes value) {
            this.Ssn = value.Ssn;
        }

        public SecurityAttributes ToSecurityAttributes() {
            var res = new SecurityAttributesStruct();
            res.Ssn = this.Ssn;
            res.Password = this.Password;
            return res;
        }

        public void LoadSecurityAttributes(SecurityAttributes value) {
            this.Ssn = value.Ssn;
            this.Password = value.Password;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            SecurityAttributesStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            SecurityAttributesStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!SecurityAttributesStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface SecurityAttributes.");
            }

            return tpe;
        }

        static SecurityAttributesStruct() {
            var type = typeof(SecurityAttributes);
            #if IRT_SCAN_ALL_ASSEMBLIES
                var assemblies = AppDomain.CurrentDomain.GetAssemblies();
            #else
                var assemblies = new[] {Assembly.GetExecutingAssembly()};
            #endif
            foreach (var assembly in assemblies) {
                System.Type[] types = null;
                try {
                    types = assembly.GetTypes();
                } catch (Exception) {
                    // ReflectionTypeLoadException potentially caught here
                    continue;
                }
                foreach (var tp in types) {
                    if (type.IsAssignableFrom(tp) && !tp.IsInterface) {
                        var rttiID = tp.GetField("RTTI_FULLCLASSNAME");
                        if (rttiID != null) {
                            SecurityAttributesStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class SecurityAttributesStruct_JsonNetConverter: JsonNetConverter<SecurityAttributesStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SecurityAttributesStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SecurityAttributesStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("ssn");
            writer.WriteValue(v.Ssn);
            writer.WritePropertyName("password");
            writer.WriteValue(v.Password);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SecurityAttributesStruct ReadJson(JsonReader reader, System.Type objectType, SecurityAttributesStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new SecurityAttributesStruct(
                json["ssn"].Value<string>(), 
                json["password"].Value<string>()
            );
        }
    }
}