// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using IRT;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(GenericFailureData_JsonNetConverter))]
    public interface GenericFailureData: IRTTI {
        string Message { get; set; }
        string Diagnostics { get; set; }
        Dictionary<string, string> Reserved { get; set; }
    }
    public class GenericFailureData_JsonNetConverter: JsonNetConverter<GenericFailureData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GenericFailureData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GenericFailureData value, JsonSerializer serializer) {
            // Serializing polymorphic type GenericFailureData
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GenericFailureData ReadJson(JsonReader reader, System.Type objectType, GenericFailureData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = GenericFailureDataStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (GenericFailureData)res;
        }
    }

    [JsonConverter(typeof(GenericFailureDataStruct_JsonNetConverter))]
    public class GenericFailureDataStruct : GenericFailureData {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.GenericFailureData";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.GenericFailureData.Struct";
        public string GetPackageName() { return GenericFailureDataStruct.RTTI_PACKAGE; }
        public string GetClassName() { return GenericFailureDataStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return GenericFailureDataStruct.RTTI_FULLCLASSNAME; }

        public string Message { get; set; }
        public string Diagnostics { get; set; }
        public Dictionary<string, string> Reserved { get; set; }

        public GenericFailureDataStruct() {
            Reserved = new Dictionary<string, string>();
        }

        public GenericFailureDataStruct(string message, string diagnostics, Dictionary<string, string> reserved) {
            this.Message = message;
            this.Diagnostics = diagnostics;
            this.Reserved = reserved;
        }

        public GenericFailureData ToGenericFailureData() {
            var res = new GenericFailureDataStruct();
            res.Message = this.Message;
            res.Diagnostics = this.Diagnostics;
            res.Reserved = this.Reserved;
            return res;
        }

        public void LoadGenericFailureData(GenericFailureData value) {
            this.Message = value.Message;
            this.Diagnostics = value.Diagnostics;
            this.Reserved = value.Reserved;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            GenericFailureDataStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            GenericFailureDataStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!GenericFailureDataStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface GenericFailureData.");
            }

            return tpe;
        }

        static GenericFailureDataStruct() {
            var type = typeof(GenericFailureData);
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
                            GenericFailureDataStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class GenericFailureDataStruct_JsonNetConverter: JsonNetConverter<GenericFailureDataStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GenericFailureDataStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GenericFailureDataStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("message");
            writer.WriteValue(v.Message);
            if (v.Diagnostics != null) {
                writer.WritePropertyName("diagnostics");
                writer.WriteValue(v.Diagnostics);
            }

            writer.WritePropertyName("reserved");
            writer.WriteStartObject();
            foreach(var mkv in v.Reserved) {
                writer.WritePropertyName(mkv.Key.ToString());
                writer.WriteValue(mkv.Value);
            }
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GenericFailureDataStruct ReadJson(JsonReader reader, System.Type objectType, GenericFailureDataStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            string _diagnostics = null;
            var _diagnosticsRaw = json["diagnostics"];
            if (_diagnosticsRaw != null && _diagnosticsRaw.Type != JTokenType.Null) {
                _diagnostics = _diagnosticsRaw.Value<string>();
            }

            var _reserved = new Dictionary<string, string>();
            foreach (var _reserved_kv in ((JObject)json["reserved"]).Properties()) {
                string _reserved_dv;
                _reserved_dv = _reserved_kv.Value.Value<string>();
                _reserved.Add(_reserved_kv.Name, _reserved_dv);
            }

            return new GenericFailureDataStruct(
                json["message"].Value<string>(), 
                _diagnostics, 
                _reserved
            );
        }
    }
}