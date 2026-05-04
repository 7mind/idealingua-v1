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
    [JsonConverter(typeof(IA2_JsonNetConverter))]
    public interface IA2: IA1, IRTTI {
    }
    public class IA2_JsonNetConverter: JsonNetConverter<IA2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IA2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IA2 value, JsonSerializer serializer) {
            // Serializing polymorphic type IA2
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IA2 ReadJson(JsonReader reader, System.Type objectType, IA2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = IA2Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (IA2)res;
        }
    }

    [JsonConverter(typeof(IA2Struct_JsonNetConverter))]
    public class IA2Struct : IA1, IA2 {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.IA2";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.IA2.Struct";
        public string GetPackageName() { return IA2Struct.RTTI_PACKAGE; }
        public string GetClassName() { return IA2Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return IA2Struct.RTTI_FULLCLASSNAME; }

        public int Int { get; set; }

        public IA2Struct() {
        }

        public IA2Struct(int @int) {
            this.Int = @int;
        }

        public IA1 ToIA1() {
            var res = new IA1Struct();
            res.Int = this.Int;
            return res;
        }

        public void LoadIA1(IA1 value) {
            this.Int = value.Int;
        }

        public IA2 ToIA2() {
            var res = new IA2Struct();
            res.Int = this.Int;
            return res;
        }

        public void LoadIA2(IA2 value) {
            this.Int = value.Int;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            IA2Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            IA2Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!IA2Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface IA2.");
            }

            return tpe;
        }

        static IA2Struct() {
            var type = typeof(IA2);
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
                            IA2Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class IA2Struct_JsonNetConverter: JsonNetConverter<IA2Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IA2Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IA2Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("Int");
            writer.WriteValue(v.Int);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IA2Struct ReadJson(JsonReader reader, System.Type objectType, IA2Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new IA2Struct(
                json["Int"].Value<int>()
            );
        }
    }
}