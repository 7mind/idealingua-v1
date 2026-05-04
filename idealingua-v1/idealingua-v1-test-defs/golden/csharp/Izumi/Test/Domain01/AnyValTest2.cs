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
    [JsonConverter(typeof(AnyValTest2_JsonNetConverter))]
    public interface AnyValTest2: IRTTI {
        Izumi.Test.Domain01.AnyValTest Field { get; set; }
    }
    public class AnyValTest2_JsonNetConverter: JsonNetConverter<AnyValTest2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AnyValTest2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AnyValTest2 value, JsonSerializer serializer) {
            // Serializing polymorphic type AnyValTest2
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AnyValTest2 ReadJson(JsonReader reader, System.Type objectType, AnyValTest2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = AnyValTest2Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (AnyValTest2)res;
        }
    }

    [JsonConverter(typeof(AnyValTest2Struct_JsonNetConverter))]
    public class AnyValTest2Struct : AnyValTest2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.AnyValTest2";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.AnyValTest2.Struct";
        public string GetPackageName() { return AnyValTest2Struct.RTTI_PACKAGE; }
        public string GetClassName() { return AnyValTest2Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AnyValTest2Struct.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.AnyValTest Field { get; set; }

        public AnyValTest2Struct() {
        }

        public AnyValTest2Struct(Izumi.Test.Domain01.AnyValTest field) {
            this.Field = field;
        }

        public AnyValTest2 ToAnyValTest2() {
            var res = new AnyValTest2Struct();
            res.Field = this.Field;
            return res;
        }

        public void LoadAnyValTest2(AnyValTest2 value) {
            this.Field = value.Field;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            AnyValTest2Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            AnyValTest2Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!AnyValTest2Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface AnyValTest2.");
            }

            return tpe;
        }

        static AnyValTest2Struct() {
            var type = typeof(AnyValTest2);
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
                            AnyValTest2Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class AnyValTest2Struct_JsonNetConverter: JsonNetConverter<AnyValTest2Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AnyValTest2Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AnyValTest2Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("field");
            // Serializing polymorphic type AnyValTest
            writer.WriteStartObject();
            writer.WritePropertyName(v.Field.GetFullClassName());
            serializer.Serialize(writer, v.Field);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AnyValTest2Struct ReadJson(JsonReader reader, System.Type objectType, AnyValTest2Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new AnyValTest2Struct(
                serializer.Deserialize<Izumi.Test.Domain01.AnyValTest>(json["field"].CreateReader())
            );
        }
    }
}