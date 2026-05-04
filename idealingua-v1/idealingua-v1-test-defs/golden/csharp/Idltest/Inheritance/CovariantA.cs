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
    [JsonConverter(typeof(CovariantA_JsonNetConverter))]
    public interface CovariantA: Covariant, IRTTI {
    }
    public class CovariantA_JsonNetConverter: JsonNetConverter<CovariantA> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CovariantA_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CovariantA value, JsonSerializer serializer) {
            // Serializing polymorphic type CovariantA
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CovariantA ReadJson(JsonReader reader, System.Type objectType, CovariantA existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = CovariantAStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (CovariantA)res;
        }
    }

    [JsonConverter(typeof(CovariantAStruct_JsonNetConverter))]
    public class CovariantAStruct : Covariant, CovariantA {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.CovariantA";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.CovariantA.Struct";
        public string GetPackageName() { return CovariantAStruct.RTTI_PACKAGE; }
        public string GetClassName() { return CovariantAStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CovariantAStruct.RTTI_FULLCLASSNAME; }

        public CovariantAStruct() {
        }

        public Covariant ToCovariant() {
            var res = new CovariantStruct();

            return res;
        }

        public void LoadCovariant(Covariant value) {
        }

        public CovariantA ToCovariantA() {
            var res = new CovariantAStruct();

            return res;
        }

        public void LoadCovariantA(CovariantA value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            CovariantAStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            CovariantAStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!CovariantAStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface CovariantA.");
            }

            return tpe;
        }

        static CovariantAStruct() {
            var type = typeof(CovariantA);
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
                            CovariantAStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class CovariantAStruct_JsonNetConverter: JsonNetConverter<CovariantAStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CovariantAStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CovariantAStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CovariantAStruct ReadJson(JsonReader reader, System.Type objectType, CovariantAStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new CovariantAStruct(

            );
        }
    }
}