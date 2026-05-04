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
    [JsonConverter(typeof(M2_JsonNetConverter))]
    public interface M2: IRTTI {
        string Value { get; set; }
        string Str { get; set; }
        int I32 { get; set; }
    }
    public class M2_JsonNetConverter: JsonNetConverter<M2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public M2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, M2 value, JsonSerializer serializer) {
            // Serializing polymorphic type M2
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override M2 ReadJson(JsonReader reader, System.Type objectType, M2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = M2Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (M2)res;
        }
    }

    [JsonConverter(typeof(M2Struct_JsonNetConverter))]
    public class M2Struct : M2 {
        public static readonly string RTTI_PACKAGE = "idltest.clones.M2";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.clones.M2.Struct";
        public string GetPackageName() { return M2Struct.RTTI_PACKAGE; }
        public string GetClassName() { return M2Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return M2Struct.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }
        public string Str { get; set; }
        public int I32 { get; set; }

        public M2Struct() {
        }

        public M2Struct(string value, string str, int i32) {
            this.Value = value;
            this.Str = str;
            this.I32 = i32;
        }

        public M2 ToM2() {
            var res = new M2Struct();
            res.Value = this.Value;
            res.Str = this.Str;
            res.I32 = this.I32;
            return res;
        }

        public void LoadM2(M2 value) {
            this.Value = value.Value;
            this.Str = value.Str;
            this.I32 = value.I32;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            M2Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            M2Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!M2Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface M2.");
            }

            return tpe;
        }

        static M2Struct() {
            var type = typeof(M2);
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
                            M2Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class M2Struct_JsonNetConverter: JsonNetConverter<M2Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public M2Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, M2Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WritePropertyName("str");
            writer.WriteValue(v.Str);
            writer.WritePropertyName("i32");
            writer.WriteValue(v.I32);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override M2Struct ReadJson(JsonReader reader, System.Type objectType, M2Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new M2Struct(
                json["value"].Value<string>(), 
                json["str"].Value<string>(), 
                json["i32"].Value<int>()
            );
        }
    }
}