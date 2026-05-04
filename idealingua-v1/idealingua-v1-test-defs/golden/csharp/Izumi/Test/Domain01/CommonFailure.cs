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

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(CommonFailure_JsonNetConverter))]
    public interface CommonFailure: IRTTI {
        int Code { get; set; }
    }
    public class CommonFailure_JsonNetConverter: JsonNetConverter<CommonFailure> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CommonFailure_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CommonFailure value, JsonSerializer serializer) {
            // Serializing polymorphic type CommonFailure
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CommonFailure ReadJson(JsonReader reader, System.Type objectType, CommonFailure existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = CommonFailureStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (CommonFailure)res;
        }
    }

    [JsonConverter(typeof(CommonFailureStruct_JsonNetConverter))]
    public class CommonFailureStruct : CommonFailure {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.CommonFailure";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.CommonFailure.Struct";
        public string GetPackageName() { return CommonFailureStruct.RTTI_PACKAGE; }
        public string GetClassName() { return CommonFailureStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CommonFailureStruct.RTTI_FULLCLASSNAME; }

        public int Code { get; set; }

        public CommonFailureStruct() {
        }

        public CommonFailureStruct(int code) {
            this.Code = code;
        }

        public CommonFailure ToCommonFailure() {
            var res = new CommonFailureStruct();
            res.Code = this.Code;
            return res;
        }

        public void LoadCommonFailure(CommonFailure value) {
            this.Code = value.Code;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            CommonFailureStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            CommonFailureStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!CommonFailureStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface CommonFailure.");
            }

            return tpe;
        }

        static CommonFailureStruct() {
            var type = typeof(CommonFailure);
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
                            CommonFailureStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class CommonFailureStruct_JsonNetConverter: JsonNetConverter<CommonFailureStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CommonFailureStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CommonFailureStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("code");
            writer.WriteValue(v.Code);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CommonFailureStruct ReadJson(JsonReader reader, System.Type objectType, CommonFailureStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new CommonFailureStruct(
                json["code"].Value<int>()
            );
        }
    }
}