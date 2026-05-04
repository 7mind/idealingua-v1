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
    [JsonConverter(typeof(Covariant_JsonNetConverter))]
    public interface Covariant: IRTTI {
    }
    public class Covariant_JsonNetConverter: JsonNetConverter<Covariant> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Covariant_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Covariant value, JsonSerializer serializer) {
            // Serializing polymorphic type Covariant
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Covariant ReadJson(JsonReader reader, System.Type objectType, Covariant existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = CovariantStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Covariant)res;
        }
    }

    [JsonConverter(typeof(CovariantStruct_JsonNetConverter))]
    public class CovariantStruct : Covariant {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.Covariant";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.Covariant.Struct";
        public string GetPackageName() { return CovariantStruct.RTTI_PACKAGE; }
        public string GetClassName() { return CovariantStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CovariantStruct.RTTI_FULLCLASSNAME; }

        public CovariantStruct() {
        }

        public Covariant ToCovariant() {
            var res = new CovariantStruct();

            return res;
        }

        public void LoadCovariant(Covariant value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            CovariantStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            CovariantStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!CovariantStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Covariant.");
            }

            return tpe;
        }

        static CovariantStruct() {
            var type = typeof(Covariant);
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
                            CovariantStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class CovariantStruct_JsonNetConverter: JsonNetConverter<CovariantStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CovariantStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CovariantStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CovariantStruct ReadJson(JsonReader reader, System.Type objectType, CovariantStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new CovariantStruct(

            );
        }
    }
}