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

namespace Idltest.Inheritance {
    [JsonConverter(typeof(PointLike_JsonNetConverter))]
    public interface PointLike: Metadata, IRTTI {
        int X { get; set; }
        int Y { get; set; }
    }
    public class PointLike_JsonNetConverter: JsonNetConverter<PointLike> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PointLike_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PointLike value, JsonSerializer serializer) {
            // Serializing polymorphic type PointLike
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PointLike ReadJson(JsonReader reader, System.Type objectType, PointLike existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = PointLikeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (PointLike)res;
        }
    }

    [JsonConverter(typeof(PointLikeStruct_JsonNetConverter))]
    public class PointLikeStruct : Metadata, PointLike {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.PointLike";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.PointLike.Struct";
        public string GetPackageName() { return PointLikeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return PointLikeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PointLikeStruct.RTTI_FULLCLASSNAME; }

        public string Id { get; set; }
        public string Name { get; set; }
        public int X { get; set; }
        public int Y { get; set; }

        public PointLikeStruct() {
        }

        public PointLikeStruct(string id, string name, int x, int y) {
            this.Id = id;
            this.Name = name;
            this.X = x;
            this.Y = y;
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

        public PointLike ToPointLike() {
            var res = new PointLikeStruct();
            res.Id = this.Id;
            res.Name = this.Name;
            res.X = this.X;
            res.Y = this.Y;
            return res;
        }

        public void LoadPointLike(PointLike value) {
            this.Id = value.Id;
            this.Name = value.Name;
            this.X = value.X;
            this.Y = value.Y;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            PointLikeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            PointLikeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!PointLikeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface PointLike.");
            }

            return tpe;
        }

        static PointLikeStruct() {
            var type = typeof(PointLike);
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
                            PointLikeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class PointLikeStruct_JsonNetConverter: JsonNetConverter<PointLikeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PointLikeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PointLikeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id);
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("x");
            writer.WriteValue(v.X);
            writer.WritePropertyName("y");
            writer.WriteValue(v.Y);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PointLikeStruct ReadJson(JsonReader reader, System.Type objectType, PointLikeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PointLikeStruct(
                json["id"].Value<string>(), 
                json["name"].Value<string>(), 
                json["x"].Value<int>(), 
                json["y"].Value<int>()
            );
        }
    }
}