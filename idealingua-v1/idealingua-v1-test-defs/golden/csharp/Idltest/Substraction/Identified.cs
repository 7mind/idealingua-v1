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
    [JsonConverter(typeof(Identified_JsonNetConverter))]
    public interface Identified: IRTTI {
        string Id { get; set; }
    }
    public class Identified_JsonNetConverter: JsonNetConverter<Identified> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Identified_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Identified value, JsonSerializer serializer) {
            // Serializing polymorphic type Identified
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Identified ReadJson(JsonReader reader, System.Type objectType, Identified existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = IdentifiedStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Identified)res;
        }
    }

    [JsonConverter(typeof(IdentifiedStruct_JsonNetConverter))]
    public class IdentifiedStruct : Identified {
        public static readonly string RTTI_PACKAGE = "idltest.substraction.Identified";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.Identified.Struct";
        public string GetPackageName() { return IdentifiedStruct.RTTI_PACKAGE; }
        public string GetClassName() { return IdentifiedStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return IdentifiedStruct.RTTI_FULLCLASSNAME; }

        public string Id { get; set; }

        public IdentifiedStruct() {
        }

        public IdentifiedStruct(string id) {
            this.Id = id;
        }

        public Identified ToIdentified() {
            var res = new IdentifiedStruct();
            res.Id = this.Id;
            return res;
        }

        public void LoadIdentified(Identified value) {
            this.Id = value.Id;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            IdentifiedStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            IdentifiedStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!IdentifiedStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Identified.");
            }

            return tpe;
        }

        static IdentifiedStruct() {
            var type = typeof(Identified);
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
                            IdentifiedStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class IdentifiedStruct_JsonNetConverter: JsonNetConverter<IdentifiedStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IdentifiedStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IdentifiedStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IdentifiedStruct ReadJson(JsonReader reader, System.Type objectType, IdentifiedStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new IdentifiedStruct(
                json["id"].Value<string>()
            );
        }
    }
}