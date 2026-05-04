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
    [JsonConverter(typeof(Empty_JsonNetConverter))]
    public interface Empty: IRTTI {
    }
    public class Empty_JsonNetConverter: JsonNetConverter<Empty> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Empty_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Empty value, JsonSerializer serializer) {
            // Serializing polymorphic type Empty
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Empty ReadJson(JsonReader reader, System.Type objectType, Empty existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = EmptyStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Empty)res;
        }
    }

    [JsonConverter(typeof(EmptyStruct_JsonNetConverter))]
    public class EmptyStruct : Empty {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.Empty";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.Empty.Struct";
        public string GetPackageName() { return EmptyStruct.RTTI_PACKAGE; }
        public string GetClassName() { return EmptyStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return EmptyStruct.RTTI_FULLCLASSNAME; }

        public EmptyStruct() {
        }

        public Empty ToEmpty() {
            var res = new EmptyStruct();

            return res;
        }

        public void LoadEmpty(Empty value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            EmptyStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            EmptyStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!EmptyStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Empty.");
            }

            return tpe;
        }

        static EmptyStruct() {
            var type = typeof(Empty);
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
                            EmptyStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class EmptyStruct_JsonNetConverter: JsonNetConverter<EmptyStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public EmptyStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, EmptyStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override EmptyStruct ReadJson(JsonReader reader, System.Type objectType, EmptyStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new EmptyStruct(

            );
        }
    }
}