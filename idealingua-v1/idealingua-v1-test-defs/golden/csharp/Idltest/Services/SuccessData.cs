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

namespace Idltest.Services {
    [JsonConverter(typeof(SuccessData_JsonNetConverter))]
    public interface SuccessData: IRTTI {
        string Greeting { get; set; }
    }
    public class SuccessData_JsonNetConverter: JsonNetConverter<SuccessData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SuccessData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SuccessData value, JsonSerializer serializer) {
            // Serializing polymorphic type SuccessData
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SuccessData ReadJson(JsonReader reader, System.Type objectType, SuccessData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = SuccessDataStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (SuccessData)res;
        }
    }

    [JsonConverter(typeof(SuccessDataStruct_JsonNetConverter))]
    public class SuccessDataStruct : SuccessData {
        public static readonly string RTTI_PACKAGE = "idltest.services.SuccessData";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.services.SuccessData.Struct";
        public string GetPackageName() { return SuccessDataStruct.RTTI_PACKAGE; }
        public string GetClassName() { return SuccessDataStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return SuccessDataStruct.RTTI_FULLCLASSNAME; }

        public string Greeting { get; set; }

        public SuccessDataStruct() {
        }

        public SuccessDataStruct(string greeting) {
            this.Greeting = greeting;
        }

        public SuccessData ToSuccessData() {
            var res = new SuccessDataStruct();
            res.Greeting = this.Greeting;
            return res;
        }

        public void LoadSuccessData(SuccessData value) {
            this.Greeting = value.Greeting;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            SuccessDataStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            SuccessDataStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!SuccessDataStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface SuccessData.");
            }

            return tpe;
        }

        static SuccessDataStruct() {
            var type = typeof(SuccessData);
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
                            SuccessDataStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class SuccessDataStruct_JsonNetConverter: JsonNetConverter<SuccessDataStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SuccessDataStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SuccessDataStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("greeting");
            writer.WriteValue(v.Greeting);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SuccessDataStruct ReadJson(JsonReader reader, System.Type objectType, SuccessDataStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new SuccessDataStruct(
                json["greeting"].Value<string>()
            );
        }
    }
}