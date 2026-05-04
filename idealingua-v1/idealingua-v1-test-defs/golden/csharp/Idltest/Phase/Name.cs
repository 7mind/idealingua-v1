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

namespace Idltest.Phase {
    [JsonConverter(typeof(Name_JsonNetConverter))]
    public interface Name: IRTTI {
        string Name { get; set; }
    }
    public class Name_JsonNetConverter: JsonNetConverter<Name> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Name_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Name value, JsonSerializer serializer) {
            // Serializing polymorphic type Name
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Name ReadJson(JsonReader reader, System.Type objectType, Name existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NameStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Name)res;
        }
    }

    [JsonConverter(typeof(NameStruct_JsonNetConverter))]
    public class NameStruct : Name {
        public static readonly string RTTI_PACKAGE = "idltest.phase.Name";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.phase.Name.Struct";
        public string GetPackageName() { return NameStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NameStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NameStruct.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }

        public NameStruct() {
        }

        public NameStruct(string name) {
            this.Name = name;
        }

        public Name ToName() {
            var res = new NameStruct();
            res.Name = this.Name;
            return res;
        }

        public void LoadName(Name value) {
            this.Name = value.Name;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NameStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NameStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NameStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Name.");
            }

            return tpe;
        }

        static NameStruct() {
            var type = typeof(Name);
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
                            NameStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NameStruct_JsonNetConverter: JsonNetConverter<NameStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NameStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NameStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NameStruct ReadJson(JsonReader reader, System.Type objectType, NameStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new NameStruct(
                json["name"].Value<string>()
            );
        }
    }
}