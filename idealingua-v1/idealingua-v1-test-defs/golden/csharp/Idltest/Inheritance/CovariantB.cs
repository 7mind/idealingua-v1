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
    [JsonConverter(typeof(CovariantB_JsonNetConverter))]
    public interface CovariantB: Covariant, IRTTI {
    }
    public class CovariantB_JsonNetConverter: JsonNetConverter<CovariantB> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CovariantB_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CovariantB value, JsonSerializer serializer) {
            // Serializing polymorphic type CovariantB
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CovariantB ReadJson(JsonReader reader, System.Type objectType, CovariantB existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = CovariantBStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (CovariantB)res;
        }
    }

    [JsonConverter(typeof(CovariantBStruct_JsonNetConverter))]
    public class CovariantBStruct : Covariant, CovariantB {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.CovariantB";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.CovariantB.Struct";
        public string GetPackageName() { return CovariantBStruct.RTTI_PACKAGE; }
        public string GetClassName() { return CovariantBStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CovariantBStruct.RTTI_FULLCLASSNAME; }

        public CovariantBStruct() {
        }

        public Covariant ToCovariant() {
            var res = new CovariantStruct();

            return res;
        }

        public void LoadCovariant(Covariant value) {
        }

        public CovariantB ToCovariantB() {
            var res = new CovariantBStruct();

            return res;
        }

        public void LoadCovariantB(CovariantB value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            CovariantBStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            CovariantBStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!CovariantBStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface CovariantB.");
            }

            return tpe;
        }

        static CovariantBStruct() {
            var type = typeof(CovariantB);
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
                            CovariantBStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class CovariantBStruct_JsonNetConverter: JsonNetConverter<CovariantBStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CovariantBStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CovariantBStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CovariantBStruct ReadJson(JsonReader reader, System.Type objectType, CovariantBStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new CovariantBStruct(

            );
        }
    }
}