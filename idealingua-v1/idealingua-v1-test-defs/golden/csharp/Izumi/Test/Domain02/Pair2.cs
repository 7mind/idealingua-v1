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
    [JsonConverter(typeof(Pair2_JsonNetConverter))]
    public interface Pair2: IRTTI {
        string Y { get; set; }
        string X { get; set; }
    }
    public class Pair2_JsonNetConverter: JsonNetConverter<Pair2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Pair2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Pair2 value, JsonSerializer serializer) {
            // Serializing polymorphic type Pair2
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Pair2 ReadJson(JsonReader reader, System.Type objectType, Pair2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = Pair2Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Pair2)res;
        }
    }

    [JsonConverter(typeof(Pair2Struct_JsonNetConverter))]
    public class Pair2Struct : Pair2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02.Pair2";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.Pair2.Struct";
        public string GetPackageName() { return Pair2Struct.RTTI_PACKAGE; }
        public string GetClassName() { return Pair2Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Pair2Struct.RTTI_FULLCLASSNAME; }

        public string Y { get; set; }
        public string X { get; set; }

        public Pair2Struct() {
        }

        public Pair2Struct(string y, string x) {
            this.Y = y;
            this.X = x;
        }

        public Pair2 ToPair2() {
            var res = new Pair2Struct();
            res.Y = this.Y;
            res.X = this.X;
            return res;
        }

        public void LoadPair2(Pair2 value) {
            this.Y = value.Y;
            this.X = value.X;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            Pair2Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            Pair2Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!Pair2Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Pair2.");
            }

            return tpe;
        }

        static Pair2Struct() {
            var type = typeof(Pair2);
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
                            Pair2Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class Pair2Struct_JsonNetConverter: JsonNetConverter<Pair2Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Pair2Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Pair2Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("y");
            writer.WriteValue(v.Y);
            writer.WritePropertyName("x");
            writer.WriteValue(v.X);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Pair2Struct ReadJson(JsonReader reader, System.Type objectType, Pair2Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Pair2Struct(
                json["y"].Value<string>(), 
                json["x"].Value<string>()
            );
        }
    }
}