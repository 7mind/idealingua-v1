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

namespace Idltest.Clones {
    [JsonConverter(typeof(M0_JsonNetConverter))]
    public interface M0: IRTTI {
        string Value { get; set; }
    }
    public class M0_JsonNetConverter: JsonNetConverter<M0> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public M0_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, M0 value, JsonSerializer serializer) {
            // Serializing polymorphic type M0
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override M0 ReadJson(JsonReader reader, System.Type objectType, M0 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = M0Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (M0)res;
        }
    }

    [JsonConverter(typeof(M0Struct_JsonNetConverter))]
    public class M0Struct : M0 {
        public static readonly string RTTI_PACKAGE = "idltest.clones.M0";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.clones.M0.Struct";
        public string GetPackageName() { return M0Struct.RTTI_PACKAGE; }
        public string GetClassName() { return M0Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return M0Struct.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public M0Struct() {
        }

        public M0Struct(string value) {
            this.Value = value;
        }

        public M0 ToM0() {
            var res = new M0Struct();
            res.Value = this.Value;
            return res;
        }

        public void LoadM0(M0 value) {
            this.Value = value.Value;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            M0Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            M0Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!M0Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface M0.");
            }

            return tpe;
        }

        static M0Struct() {
            var type = typeof(M0);
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
                            M0Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class M0Struct_JsonNetConverter: JsonNetConverter<M0Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public M0Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, M0Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override M0Struct ReadJson(JsonReader reader, System.Type objectType, M0Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new M0Struct(
                json["value"].Value<string>()
            );
        }
    }
}