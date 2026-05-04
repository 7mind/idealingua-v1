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
    [JsonConverter(typeof(WHPair_JsonNetConverter))]
    public interface WHPair: IRTTI {
        int W { get; set; }
        int H { get; set; }
    }
    public class WHPair_JsonNetConverter: JsonNetConverter<WHPair> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public WHPair_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, WHPair value, JsonSerializer serializer) {
            // Serializing polymorphic type WHPair
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override WHPair ReadJson(JsonReader reader, System.Type objectType, WHPair existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = WHPairStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (WHPair)res;
        }
    }

    [JsonConverter(typeof(WHPairStruct_JsonNetConverter))]
    public class WHPairStruct : WHPair {
        public static readonly string RTTI_PACKAGE = "idltest.dtofields.WHPair";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.dtofields.WHPair.Struct";
        public string GetPackageName() { return WHPairStruct.RTTI_PACKAGE; }
        public string GetClassName() { return WHPairStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return WHPairStruct.RTTI_FULLCLASSNAME; }

        public int W { get; set; }
        public int H { get; set; }

        public WHPairStruct() {
        }

        public WHPairStruct(int w, int h) {
            this.W = w;
            this.H = h;
        }

        public WHPair ToWHPair() {
            var res = new WHPairStruct();
            res.W = this.W;
            res.H = this.H;
            return res;
        }

        public void LoadWHPair(WHPair value) {
            this.W = value.W;
            this.H = value.H;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            WHPairStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            WHPairStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!WHPairStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface WHPair.");
            }

            return tpe;
        }

        static WHPairStruct() {
            var type = typeof(WHPair);
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
                            WHPairStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class WHPairStruct_JsonNetConverter: JsonNetConverter<WHPairStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public WHPairStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, WHPairStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("w");
            writer.WriteValue(v.W);
            writer.WritePropertyName("h");
            writer.WriteValue(v.H);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override WHPairStruct ReadJson(JsonReader reader, System.Type objectType, WHPairStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new WHPairStruct(
                json["w"].Value<int>(), 
                json["h"].Value<int>()
            );
        }
    }
}