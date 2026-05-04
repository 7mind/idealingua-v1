// Auto-generated, any modifications may be overwritten in the future.

using System;
using IRT;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Upcasts {
    [JsonConverter(typeof(Identifiable_JsonNetConverter))]
    public interface Identifiable: IRTTI {
        Guid Id { get; set; }
    }
    public class Identifiable_JsonNetConverter: JsonNetConverter<Identifiable> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Identifiable_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Identifiable value, JsonSerializer serializer) {
            // Serializing polymorphic type Identifiable
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Identifiable ReadJson(JsonReader reader, System.Type objectType, Identifiable existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = IdentifiableStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Identifiable)res;
        }
    }

    [JsonConverter(typeof(IdentifiableStruct_JsonNetConverter))]
    public class IdentifiableStruct : Identifiable {
        public static readonly string RTTI_PACKAGE = "idltest.upcasts.Identifiable";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.upcasts.Identifiable.Struct";
        public string GetPackageName() { return IdentifiableStruct.RTTI_PACKAGE; }
        public string GetClassName() { return IdentifiableStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return IdentifiableStruct.RTTI_FULLCLASSNAME; }

        public Guid Id { get; set; }

        public IdentifiableStruct() {
        }

        public IdentifiableStruct(Guid id) {
            this.Id = id;
        }

        public Identifiable ToIdentifiable() {
            var res = new IdentifiableStruct();
            res.Id = this.Id;
            return res;
        }

        public void LoadIdentifiable(Identifiable value) {
            this.Id = value.Id;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            IdentifiableStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            IdentifiableStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!IdentifiableStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Identifiable.");
            }

            return tpe;
        }

        static IdentifiableStruct() {
            var type = typeof(Identifiable);
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
                            IdentifiableStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class IdentifiableStruct_JsonNetConverter: JsonNetConverter<IdentifiableStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IdentifiableStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IdentifiableStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IdentifiableStruct ReadJson(JsonReader reader, System.Type objectType, IdentifiableStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new IdentifiableStruct(
                new System.Guid(json["id"].Value<string>())
            );
        }
    }
}