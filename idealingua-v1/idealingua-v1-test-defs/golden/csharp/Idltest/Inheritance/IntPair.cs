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
    [JsonConverter(typeof(IntPair_JsonNetConverter))]
    public interface IntPair: IRTTI {
        int X { get; set; }
        int Y { get; set; }
    }
    public class IntPair_JsonNetConverter: JsonNetConverter<IntPair> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IntPair_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IntPair value, JsonSerializer serializer) {
            // Serializing polymorphic type IntPair
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IntPair ReadJson(JsonReader reader, System.Type objectType, IntPair existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = IntPairStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (IntPair)res;
        }
    }

    [JsonConverter(typeof(IntPairStruct_JsonNetConverter))]
    public class IntPairStruct : IntPair {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.IntPair";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.IntPair.Struct";
        public string GetPackageName() { return IntPairStruct.RTTI_PACKAGE; }
        public string GetClassName() { return IntPairStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return IntPairStruct.RTTI_FULLCLASSNAME; }

        public int X { get; set; }
        public int Y { get; set; }

        public IntPairStruct() {
        }

        public IntPairStruct(int x, int y) {
            this.X = x;
            this.Y = y;
        }

        public IntPair ToIntPair() {
            var res = new IntPairStruct();
            res.X = this.X;
            res.Y = this.Y;
            return res;
        }

        public void LoadIntPair(IntPair value) {
            this.X = value.X;
            this.Y = value.Y;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            IntPairStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            IntPairStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!IntPairStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface IntPair.");
            }

            return tpe;
        }

        static IntPairStruct() {
            var type = typeof(IntPair);
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
                            IntPairStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class IntPairStruct_JsonNetConverter: JsonNetConverter<IntPairStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IntPairStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IntPairStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("x");
            writer.WriteValue(v.X);
            writer.WritePropertyName("y");
            writer.WriteValue(v.Y);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IntPairStruct ReadJson(JsonReader reader, System.Type objectType, IntPairStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new IntPairStruct(
                json["x"].Value<int>(), 
                json["y"].Value<int>()
            );
        }
    }
}