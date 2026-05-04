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

namespace Idltest.Phase {
    [JsonConverter(typeof(LengthInBytes_JsonNetConverter))]
    public interface LengthInBytes: IRTTI {
        long Bytes { get; set; }
    }
    public class LengthInBytes_JsonNetConverter: JsonNetConverter<LengthInBytes> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public LengthInBytes_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, LengthInBytes value, JsonSerializer serializer) {
            // Serializing polymorphic type LengthInBytes
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override LengthInBytes ReadJson(JsonReader reader, System.Type objectType, LengthInBytes existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = LengthInBytesStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (LengthInBytes)res;
        }
    }

    [JsonConverter(typeof(LengthInBytesStruct_JsonNetConverter))]
    public class LengthInBytesStruct : LengthInBytes {
        public static readonly string RTTI_PACKAGE = "idltest.phase.LengthInBytes";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.phase.LengthInBytes.Struct";
        public string GetPackageName() { return LengthInBytesStruct.RTTI_PACKAGE; }
        public string GetClassName() { return LengthInBytesStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return LengthInBytesStruct.RTTI_FULLCLASSNAME; }

        public long Bytes { get; set; }

        public LengthInBytesStruct() {
        }

        public LengthInBytesStruct(long bytes) {
            this.Bytes = bytes;
        }

        public LengthInBytes ToLengthInBytes() {
            var res = new LengthInBytesStruct();
            res.Bytes = this.Bytes;
            return res;
        }

        public void LoadLengthInBytes(LengthInBytes value) {
            this.Bytes = value.Bytes;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            LengthInBytesStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            LengthInBytesStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!LengthInBytesStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface LengthInBytes.");
            }

            return tpe;
        }

        static LengthInBytesStruct() {
            var type = typeof(LengthInBytes);
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
                            LengthInBytesStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class LengthInBytesStruct_JsonNetConverter: JsonNetConverter<LengthInBytesStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public LengthInBytesStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, LengthInBytesStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("bytes");
            writer.WriteValue(v.Bytes);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override LengthInBytesStruct ReadJson(JsonReader reader, System.Type objectType, LengthInBytesStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new LengthInBytesStruct(
                json["bytes"].Value<long>()
            );
        }
    }
}