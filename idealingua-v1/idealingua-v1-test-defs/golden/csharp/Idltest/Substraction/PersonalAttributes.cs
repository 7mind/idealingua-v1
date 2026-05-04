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
    [JsonConverter(typeof(PersonalAttributes_JsonNetConverter))]
    public interface PersonalAttributes: IRTTI {
        string Ssn { get; set; }
    }
    public class PersonalAttributes_JsonNetConverter: JsonNetConverter<PersonalAttributes> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PersonalAttributes_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PersonalAttributes value, JsonSerializer serializer) {
            // Serializing polymorphic type PersonalAttributes
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PersonalAttributes ReadJson(JsonReader reader, System.Type objectType, PersonalAttributes existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = PersonalAttributesStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (PersonalAttributes)res;
        }
    }

    [JsonConverter(typeof(PersonalAttributesStruct_JsonNetConverter))]
    public class PersonalAttributesStruct : PersonalAttributes {
        public static readonly string RTTI_PACKAGE = "idltest.substraction.PersonalAttributes";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.PersonalAttributes.Struct";
        public string GetPackageName() { return PersonalAttributesStruct.RTTI_PACKAGE; }
        public string GetClassName() { return PersonalAttributesStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PersonalAttributesStruct.RTTI_FULLCLASSNAME; }

        public string Ssn { get; set; }

        public PersonalAttributesStruct() {
        }

        public PersonalAttributesStruct(string ssn) {
            this.Ssn = ssn;
        }

        public PersonalAttributes ToPersonalAttributes() {
            var res = new PersonalAttributesStruct();
            res.Ssn = this.Ssn;
            return res;
        }

        public void LoadPersonalAttributes(PersonalAttributes value) {
            this.Ssn = value.Ssn;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            PersonalAttributesStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            PersonalAttributesStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!PersonalAttributesStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface PersonalAttributes.");
            }

            return tpe;
        }

        static PersonalAttributesStruct() {
            var type = typeof(PersonalAttributes);
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
                            PersonalAttributesStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class PersonalAttributesStruct_JsonNetConverter: JsonNetConverter<PersonalAttributesStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PersonalAttributesStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PersonalAttributesStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("ssn");
            writer.WriteValue(v.Ssn);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PersonalAttributesStruct ReadJson(JsonReader reader, System.Type objectType, PersonalAttributesStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PersonalAttributesStruct(
                json["ssn"].Value<string>()
            );
        }
    }
}