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

namespace Idltest.Algebraics {
    [JsonConverter(typeof(AFace_JsonNetConverter))]
    public interface AFace: IRTTI {
        int A { get; set; }
    }
    public class AFace_JsonNetConverter: JsonNetConverter<AFace> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AFace_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AFace value, JsonSerializer serializer) {
            // Serializing polymorphic type AFace
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AFace ReadJson(JsonReader reader, System.Type objectType, AFace existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = AFaceStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (AFace)res;
        }
    }

    [JsonConverter(typeof(AFaceStruct_JsonNetConverter))]
    public class AFaceStruct : AFace {
        public static readonly string RTTI_PACKAGE = "idltest.algebraics.AFace";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.algebraics.AFace.Struct";
        public string GetPackageName() { return AFaceStruct.RTTI_PACKAGE; }
        public string GetClassName() { return AFaceStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AFaceStruct.RTTI_FULLCLASSNAME; }

        public int A { get; set; }

        public AFaceStruct() {
        }

        public AFaceStruct(int a) {
            this.A = a;
        }

        public AFace ToAFace() {
            var res = new AFaceStruct();
            res.A = this.A;
            return res;
        }

        public void LoadAFace(AFace value) {
            this.A = value.A;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            AFaceStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            AFaceStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!AFaceStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface AFace.");
            }

            return tpe;
        }

        static AFaceStruct() {
            var type = typeof(AFace);
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
                            AFaceStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class AFaceStruct_JsonNetConverter: JsonNetConverter<AFaceStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AFaceStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AFaceStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AFaceStruct ReadJson(JsonReader reader, System.Type objectType, AFaceStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new AFaceStruct(
                json["a"].Value<int>()
            );
        }
    }
}