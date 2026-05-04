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

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(Pair1_JsonNetConverter))]
    public interface Pair1: IRTTI {
        string X { get; set; }
        string Y { get; set; }
    }
    public class Pair1_JsonNetConverter: JsonNetConverter<Pair1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Pair1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Pair1 value, JsonSerializer serializer) {
            // Serializing polymorphic type Pair1
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Pair1 ReadJson(JsonReader reader, System.Type objectType, Pair1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = Pair1Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Pair1)res;
        }
    }

    [JsonConverter(typeof(Pair1Struct_JsonNetConverter))]
    public class Pair1Struct : Pair1 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02.Pair1";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.Pair1.Struct";
        public string GetPackageName() { return Pair1Struct.RTTI_PACKAGE; }
        public string GetClassName() { return Pair1Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Pair1Struct.RTTI_FULLCLASSNAME; }

        public string X { get; set; }
        public string Y { get; set; }

        public Pair1Struct() {
        }

        public Pair1Struct(string x, string y) {
            this.X = x;
            this.Y = y;
        }

        public Pair1 ToPair1() {
            var res = new Pair1Struct();
            res.X = this.X;
            res.Y = this.Y;
            return res;
        }

        public void LoadPair1(Pair1 value) {
            this.X = value.X;
            this.Y = value.Y;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            Pair1Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            Pair1Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!Pair1Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Pair1.");
            }

            return tpe;
        }

        static Pair1Struct() {
            var type = typeof(Pair1);
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
                            Pair1Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class Pair1Struct_JsonNetConverter: JsonNetConverter<Pair1Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Pair1Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Pair1Struct v, JsonSerializer serializer) {
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
        public override Pair1Struct ReadJson(JsonReader reader, System.Type objectType, Pair1Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Pair1Struct(
                json["x"].Value<string>(), 
                json["y"].Value<string>()
            );
        }
    }
}