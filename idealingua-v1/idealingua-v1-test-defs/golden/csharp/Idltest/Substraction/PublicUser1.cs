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
    [JsonConverter(typeof(PublicUser1_JsonNetConverter))]
    public interface PublicUser1: IRTTI {
        string Name { get; set; }
    }
    public class PublicUser1_JsonNetConverter: JsonNetConverter<PublicUser1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PublicUser1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PublicUser1 value, JsonSerializer serializer) {
            // Serializing polymorphic type PublicUser1
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PublicUser1 ReadJson(JsonReader reader, System.Type objectType, PublicUser1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = PublicUser1Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (PublicUser1)res;
        }
    }

    [JsonConverter(typeof(PublicUser1Struct_JsonNetConverter))]
    public class PublicUser1Struct : PublicUser1 {
        public static readonly string RTTI_PACKAGE = "idltest.substraction.PublicUser1";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.PublicUser1.Struct";
        public string GetPackageName() { return PublicUser1Struct.RTTI_PACKAGE; }
        public string GetClassName() { return PublicUser1Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PublicUser1Struct.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }

        public PublicUser1Struct() {
        }

        public PublicUser1Struct(string name) {
            this.Name = name;
        }

        public PublicUser1 ToPublicUser1() {
            var res = new PublicUser1Struct();
            res.Name = this.Name;
            return res;
        }

        public void LoadPublicUser1(PublicUser1 value) {
            this.Name = value.Name;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            PublicUser1Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            PublicUser1Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!PublicUser1Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface PublicUser1.");
            }

            return tpe;
        }

        static PublicUser1Struct() {
            var type = typeof(PublicUser1);
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
                            PublicUser1Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class PublicUser1Struct_JsonNetConverter: JsonNetConverter<PublicUser1Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PublicUser1Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PublicUser1Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PublicUser1Struct ReadJson(JsonReader reader, System.Type objectType, PublicUser1Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PublicUser1Struct(
                json["name"].Value<string>()
            );
        }
    }
}