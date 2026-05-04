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
    [JsonConverter(typeof(Test01MixinAnyVal_JsonNetConverter))]
    public interface Test01MixinAnyVal: IRTTI {
        string Value { get; set; }
    }
    public class Test01MixinAnyVal_JsonNetConverter: JsonNetConverter<Test01MixinAnyVal> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test01MixinAnyVal_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test01MixinAnyVal value, JsonSerializer serializer) {
            // Serializing polymorphic type Test01MixinAnyVal
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Test01MixinAnyVal ReadJson(JsonReader reader, System.Type objectType, Test01MixinAnyVal existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = Test01MixinAnyValStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Test01MixinAnyVal)res;
        }
    }

    [JsonConverter(typeof(Test01MixinAnyValStruct_JsonNetConverter))]
    public class Test01MixinAnyValStruct : Test01MixinAnyVal {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals.Test01MixinAnyVal";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.Test01MixinAnyVal.Struct";
        public string GetPackageName() { return Test01MixinAnyValStruct.RTTI_PACKAGE; }
        public string GetClassName() { return Test01MixinAnyValStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Test01MixinAnyValStruct.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public Test01MixinAnyValStruct() {
        }

        public Test01MixinAnyValStruct(string value) {
            this.Value = value;
        }

        public Test01MixinAnyVal ToTest01MixinAnyVal() {
            var res = new Test01MixinAnyValStruct();
            res.Value = this.Value;
            return res;
        }

        public void LoadTest01MixinAnyVal(Test01MixinAnyVal value) {
            this.Value = value.Value;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            Test01MixinAnyValStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            Test01MixinAnyValStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!Test01MixinAnyValStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Test01MixinAnyVal.");
            }

            return tpe;
        }

        static Test01MixinAnyValStruct() {
            var type = typeof(Test01MixinAnyVal);
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
                            Test01MixinAnyValStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class Test01MixinAnyValStruct_JsonNetConverter: JsonNetConverter<Test01MixinAnyValStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Test01MixinAnyValStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Test01MixinAnyValStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Test01MixinAnyValStruct ReadJson(JsonReader reader, System.Type objectType, Test01MixinAnyValStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Test01MixinAnyValStruct(
                json["value"].Value<string>()
            );
        }
    }
}