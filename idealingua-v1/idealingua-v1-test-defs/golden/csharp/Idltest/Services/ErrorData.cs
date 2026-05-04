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
    [JsonConverter(typeof(ErrorData_JsonNetConverter))]
    public interface ErrorData: IRTTI {
        string Message { get; set; }
    }
    public class ErrorData_JsonNetConverter: JsonNetConverter<ErrorData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ErrorData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ErrorData value, JsonSerializer serializer) {
            // Serializing polymorphic type ErrorData
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ErrorData ReadJson(JsonReader reader, System.Type objectType, ErrorData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = ErrorDataStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (ErrorData)res;
        }
    }

    [JsonConverter(typeof(ErrorDataStruct_JsonNetConverter))]
    public class ErrorDataStruct : ErrorData {
        public static readonly string RTTI_PACKAGE = "idltest.services.ErrorData";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.services.ErrorData.Struct";
        public string GetPackageName() { return ErrorDataStruct.RTTI_PACKAGE; }
        public string GetClassName() { return ErrorDataStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ErrorDataStruct.RTTI_FULLCLASSNAME; }

        public string Message { get; set; }

        public ErrorDataStruct() {
        }

        public ErrorDataStruct(string message) {
            this.Message = message;
        }

        public ErrorData ToErrorData() {
            var res = new ErrorDataStruct();
            res.Message = this.Message;
            return res;
        }

        public void LoadErrorData(ErrorData value) {
            this.Message = value.Message;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            ErrorDataStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            ErrorDataStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!ErrorDataStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface ErrorData.");
            }

            return tpe;
        }

        static ErrorDataStruct() {
            var type = typeof(ErrorData);
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
                            ErrorDataStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class ErrorDataStruct_JsonNetConverter: JsonNetConverter<ErrorDataStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ErrorDataStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ErrorDataStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("message");
            writer.WriteValue(v.Message);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ErrorDataStruct ReadJson(JsonReader reader, System.Type objectType, ErrorDataStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new ErrorDataStruct(
                json["message"].Value<string>()
            );
        }
    }
}