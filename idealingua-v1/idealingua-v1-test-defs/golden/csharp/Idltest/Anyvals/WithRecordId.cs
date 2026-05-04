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

namespace Idltest.Anyvals {
    [JsonConverter(typeof(WithRecordId_JsonNetConverter))]
    public interface WithRecordId: IRTTI {
        Idltest.Anyvals.RecordId Id { get; set; }
    }
    public class WithRecordId_JsonNetConverter: JsonNetConverter<WithRecordId> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public WithRecordId_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, WithRecordId value, JsonSerializer serializer) {
            // Serializing polymorphic type WithRecordId
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override WithRecordId ReadJson(JsonReader reader, System.Type objectType, WithRecordId existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = WithRecordIdStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (WithRecordId)res;
        }
    }

    [JsonConverter(typeof(WithRecordIdStruct_JsonNetConverter))]
    public class WithRecordIdStruct : WithRecordId {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals.WithRecordId";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.WithRecordId.Struct";
        public string GetPackageName() { return WithRecordIdStruct.RTTI_PACKAGE; }
        public string GetClassName() { return WithRecordIdStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return WithRecordIdStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Anyvals.RecordId Id { get; set; }

        public WithRecordIdStruct() {
        }

        public WithRecordIdStruct(Idltest.Anyvals.RecordId id) {
            this.Id = id;
        }

        public WithRecordId ToWithRecordId() {
            var res = new WithRecordIdStruct();
            res.Id = this.Id;
            return res;
        }

        public void LoadWithRecordId(WithRecordId value) {
            this.Id = value.Id;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            WithRecordIdStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            WithRecordIdStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!WithRecordIdStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface WithRecordId.");
            }

            return tpe;
        }

        static WithRecordIdStruct() {
            var type = typeof(WithRecordId);
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
                            WithRecordIdStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class WithRecordIdStruct_JsonNetConverter: JsonNetConverter<WithRecordIdStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public WithRecordIdStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, WithRecordIdStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override WithRecordIdStruct ReadJson(JsonReader reader, System.Type objectType, WithRecordIdStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new WithRecordIdStruct(
                Idltest.Anyvals.RecordId.From(json["id"].Value<string>())
            );
        }
    }
}