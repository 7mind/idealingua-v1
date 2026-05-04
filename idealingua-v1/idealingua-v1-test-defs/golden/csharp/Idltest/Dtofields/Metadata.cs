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

namespace Idltest.Dtofields {
    [JsonConverter(typeof(Metadata_JsonNetConverter))]
    public interface Metadata: IRTTI {
        string Id { get; set; }
        string Name { get; set; }
    }
    public class Metadata_JsonNetConverter: JsonNetConverter<Metadata> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Metadata_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Metadata value, JsonSerializer serializer) {
            // Serializing polymorphic type Metadata
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Metadata ReadJson(JsonReader reader, System.Type objectType, Metadata existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = MetadataStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Metadata)res;
        }
    }

    [JsonConverter(typeof(MetadataStruct_JsonNetConverter))]
    public class MetadataStruct : Metadata {
        public static readonly string RTTI_PACKAGE = "idltest.dtofields.Metadata";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.dtofields.Metadata.Struct";
        public string GetPackageName() { return MetadataStruct.RTTI_PACKAGE; }
        public string GetClassName() { return MetadataStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return MetadataStruct.RTTI_FULLCLASSNAME; }

        public string Id { get; set; }
        public string Name { get; set; }

        public MetadataStruct() {
        }

        public MetadataStruct(string id, string name) {
            this.Id = id;
            this.Name = name;
        }

        public Metadata ToMetadata() {
            var res = new MetadataStruct();
            res.Id = this.Id;
            res.Name = this.Name;
            return res;
        }

        public void LoadMetadata(Metadata value) {
            this.Id = value.Id;
            this.Name = value.Name;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            MetadataStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            MetadataStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!MetadataStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Metadata.");
            }

            return tpe;
        }

        static MetadataStruct() {
            var type = typeof(Metadata);
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
                            MetadataStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class MetadataStruct_JsonNetConverter: JsonNetConverter<MetadataStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public MetadataStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, MetadataStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id);
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override MetadataStruct ReadJson(JsonReader reader, System.Type objectType, MetadataStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new MetadataStruct(
                json["id"].Value<string>(), 
                json["name"].Value<string>()
            );
        }
    }
}