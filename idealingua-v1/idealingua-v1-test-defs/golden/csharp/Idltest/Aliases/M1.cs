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

namespace Idltest.Aliases {
    [JsonConverter(typeof(M1_JsonNetConverter))]
    public interface M1: IRTTI {
        string Value { get; set; }
    }
    public class M1_JsonNetConverter: JsonNetConverter<M1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public M1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, M1 value, JsonSerializer serializer) {
            // Serializing polymorphic type M1
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override M1 ReadJson(JsonReader reader, System.Type objectType, M1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = M1Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (M1)res;
        }
    }

    [JsonConverter(typeof(M1Struct_JsonNetConverter))]
    public class M1Struct : M1 {
        public static readonly string RTTI_PACKAGE = "idltest.aliases.M1";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.aliases.M1.Struct";
        public string GetPackageName() { return M1Struct.RTTI_PACKAGE; }
        public string GetClassName() { return M1Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return M1Struct.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public M1Struct() {
        }

        public M1Struct(string value) {
            this.Value = value;
        }

        public M1 ToM1() {
            var res = new M1Struct();
            res.Value = this.Value;
            return res;
        }

        public void LoadM1(M1 value) {
            this.Value = value.Value;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            M1Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            M1Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!M1Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface M1.");
            }

            return tpe;
        }

        static M1Struct() {
            var type = typeof(M1);
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
                            M1Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class M1Struct_JsonNetConverter: JsonNetConverter<M1Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public M1Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, M1Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override M1Struct ReadJson(JsonReader reader, System.Type objectType, M1Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new M1Struct(
                json["value"].Value<string>()
            );
        }
    }
}