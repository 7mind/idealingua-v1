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
    [JsonConverter(typeof(WithCovariance_JsonNetConverter))]
    public interface WithCovariance: IRTTI {
        Idltest.Inheritance.Covariant Field { get; set; }
    }
    public class WithCovariance_JsonNetConverter: JsonNetConverter<WithCovariance> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public WithCovariance_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, WithCovariance value, JsonSerializer serializer) {
            // Serializing polymorphic type WithCovariance
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override WithCovariance ReadJson(JsonReader reader, System.Type objectType, WithCovariance existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = WithCovarianceStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (WithCovariance)res;
        }
    }

    [JsonConverter(typeof(WithCovarianceStruct_JsonNetConverter))]
    public class WithCovarianceStruct : WithCovariance {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.WithCovariance";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.WithCovariance.Struct";
        public string GetPackageName() { return WithCovarianceStruct.RTTI_PACKAGE; }
        public string GetClassName() { return WithCovarianceStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return WithCovarianceStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Inheritance.Covariant Field { get; set; }

        public WithCovarianceStruct() {
        }

        public WithCovarianceStruct(Idltest.Inheritance.Covariant field) {
            this.Field = field;
        }

        public WithCovariance ToWithCovariance() {
            var res = new WithCovarianceStruct();
            res.Field = this.Field;
            return res;
        }

        public void LoadWithCovariance(WithCovariance value) {
            this.Field = value.Field;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            WithCovarianceStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            WithCovarianceStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!WithCovarianceStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface WithCovariance.");
            }

            return tpe;
        }

        static WithCovarianceStruct() {
            var type = typeof(WithCovariance);
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
                            WithCovarianceStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class WithCovarianceStruct_JsonNetConverter: JsonNetConverter<WithCovarianceStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public WithCovarianceStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, WithCovarianceStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("field");
            // Serializing polymorphic type Covariant
            writer.WriteStartObject();
            writer.WritePropertyName(v.Field.GetFullClassName());
            serializer.Serialize(writer, v.Field);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override WithCovarianceStruct ReadJson(JsonReader reader, System.Type objectType, WithCovarianceStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new WithCovarianceStruct(
                serializer.Deserialize<Idltest.Inheritance.Covariant>(json["field"].CreateReader())
            );
        }
    }
}