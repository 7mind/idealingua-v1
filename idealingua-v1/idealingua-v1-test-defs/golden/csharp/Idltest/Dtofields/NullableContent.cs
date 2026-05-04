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

namespace Idltest.Dtofields {
    [JsonConverter(typeof(NullableContent_JsonNetConverter))]
    public interface NullableContent: IRTTI {
        int A { get; set; }
    }
    public class NullableContent_JsonNetConverter: JsonNetConverter<NullableContent> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NullableContent_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NullableContent value, JsonSerializer serializer) {
            // Serializing polymorphic type NullableContent
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NullableContent ReadJson(JsonReader reader, System.Type objectType, NullableContent existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NullableContentStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (NullableContent)res;
        }
    }

    [JsonConverter(typeof(NullableContentStruct_JsonNetConverter))]
    public class NullableContentStruct : NullableContent {
        public static readonly string RTTI_PACKAGE = "idltest.dtofields.NullableContent";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.dtofields.NullableContent.Struct";
        public string GetPackageName() { return NullableContentStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NullableContentStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NullableContentStruct.RTTI_FULLCLASSNAME; }

        public int A { get; set; }

        public NullableContentStruct() {
        }

        public NullableContentStruct(int a) {
            this.A = a;
        }

        public NullableContent ToNullableContent() {
            var res = new NullableContentStruct();
            res.A = this.A;
            return res;
        }

        public void LoadNullableContent(NullableContent value) {
            this.A = value.A;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NullableContentStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NullableContentStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NullableContentStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface NullableContent.");
            }

            return tpe;
        }

        static NullableContentStruct() {
            var type = typeof(NullableContent);
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
                            NullableContentStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NullableContentStruct_JsonNetConverter: JsonNetConverter<NullableContentStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NullableContentStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NullableContentStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NullableContentStruct ReadJson(JsonReader reader, System.Type objectType, NullableContentStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new NullableContentStruct(
                json["a"].Value<int>()
            );
        }
    }
}