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
    [JsonConverter(typeof(Request_JsonNetConverter))]
    public interface Request: IRTTI {
        string FirstName { get; set; }
        string SecondName { get; set; }
    }
    public class Request_JsonNetConverter: JsonNetConverter<Request> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Request_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Request value, JsonSerializer serializer) {
            // Serializing polymorphic type Request
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Request ReadJson(JsonReader reader, System.Type objectType, Request existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = RequestStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Request)res;
        }
    }

    [JsonConverter(typeof(RequestStruct_JsonNetConverter))]
    public class RequestStruct : Request {
        public static readonly string RTTI_PACKAGE = "idltest.services.Request";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.services.Request.Struct";
        public string GetPackageName() { return RequestStruct.RTTI_PACKAGE; }
        public string GetClassName() { return RequestStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return RequestStruct.RTTI_FULLCLASSNAME; }

        public string FirstName { get; set; }
        public string SecondName { get; set; }

        public RequestStruct() {
        }

        public RequestStruct(string firstName, string secondName) {
            this.FirstName = firstName;
            this.SecondName = secondName;
        }

        public Request ToRequest() {
            var res = new RequestStruct();
            res.FirstName = this.FirstName;
            res.SecondName = this.SecondName;
            return res;
        }

        public void LoadRequest(Request value) {
            this.FirstName = value.FirstName;
            this.SecondName = value.SecondName;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            RequestStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            RequestStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!RequestStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Request.");
            }

            return tpe;
        }

        static RequestStruct() {
            var type = typeof(Request);
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
                            RequestStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class RequestStruct_JsonNetConverter: JsonNetConverter<RequestStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RequestStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RequestStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("firstName");
            writer.WriteValue(v.FirstName);
            writer.WritePropertyName("secondName");
            writer.WriteValue(v.SecondName);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RequestStruct ReadJson(JsonReader reader, System.Type objectType, RequestStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new RequestStruct(
                json["firstName"].Value<string>(), 
                json["secondName"].Value<string>()
            );
        }
    }
}