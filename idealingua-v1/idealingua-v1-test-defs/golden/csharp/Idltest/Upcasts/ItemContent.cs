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
    [JsonConverter(typeof(ItemContent_JsonNetConverter))]
    public interface ItemContent: IRTTI {
        Guid Id { get; set; }
        string Name { get; set; }
    }
    public class ItemContent_JsonNetConverter: JsonNetConverter<ItemContent> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ItemContent_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ItemContent value, JsonSerializer serializer) {
            // Serializing polymorphic type ItemContent
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ItemContent ReadJson(JsonReader reader, System.Type objectType, ItemContent existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = ItemContentStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (ItemContent)res;
        }
    }

    [JsonConverter(typeof(ItemContentStruct_JsonNetConverter))]
    public class ItemContentStruct : ItemContent {
        public static readonly string RTTI_PACKAGE = "idltest.upcasts.ItemContent";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.upcasts.ItemContent.Struct";
        public string GetPackageName() { return ItemContentStruct.RTTI_PACKAGE; }
        public string GetClassName() { return ItemContentStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ItemContentStruct.RTTI_FULLCLASSNAME; }

        public Guid Id { get; set; }
        public string Name { get; set; }

        public ItemContentStruct() {
        }

        public ItemContentStruct(Guid id, string name) {
            this.Id = id;
            this.Name = name;
        }

        public ItemContent ToItemContent() {
            var res = new ItemContentStruct();
            res.Id = this.Id;
            res.Name = this.Name;
            return res;
        }

        public void LoadItemContent(ItemContent value) {
            this.Id = value.Id;
            this.Name = value.Name;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            ItemContentStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            ItemContentStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!ItemContentStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface ItemContent.");
            }

            return tpe;
        }

        static ItemContentStruct() {
            var type = typeof(ItemContent);
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
                            ItemContentStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class ItemContentStruct_JsonNetConverter: JsonNetConverter<ItemContentStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ItemContentStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ItemContentStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ItemContentStruct ReadJson(JsonReader reader, System.Type objectType, ItemContentStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new ItemContentStruct(
                new System.Guid(json["id"].Value<string>()), 
                json["name"].Value<string>()
            );
        }
    }
}