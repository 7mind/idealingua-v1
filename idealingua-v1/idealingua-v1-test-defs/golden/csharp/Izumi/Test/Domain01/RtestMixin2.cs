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
    [JsonConverter(typeof(RtestMixin2_JsonNetConverter))]
    public interface RtestMixin2: IRTTI {
        Izumi.Test.Domain01.RTestMixin B { get; set; }
    }
    public class RtestMixin2_JsonNetConverter: JsonNetConverter<RtestMixin2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RtestMixin2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RtestMixin2 value, JsonSerializer serializer) {
            // Serializing polymorphic type RtestMixin2
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RtestMixin2 ReadJson(JsonReader reader, System.Type objectType, RtestMixin2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = RtestMixin2Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (RtestMixin2)res;
        }
    }

    [JsonConverter(typeof(RtestMixin2Struct_JsonNetConverter))]
    public class RtestMixin2Struct : RtestMixin2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.RtestMixin2";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.RtestMixin2.Struct";
        public string GetPackageName() { return RtestMixin2Struct.RTTI_PACKAGE; }
        public string GetClassName() { return RtestMixin2Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return RtestMixin2Struct.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.RTestMixin B { get; set; }

        public RtestMixin2Struct() {
        }

        public RtestMixin2Struct(Izumi.Test.Domain01.RTestMixin b) {
            this.B = b;
        }

        public RtestMixin2 ToRtestMixin2() {
            var res = new RtestMixin2Struct();
            res.B = this.B;
            return res;
        }

        public void LoadRtestMixin2(RtestMixin2 value) {
            this.B = value.B;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            RtestMixin2Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            RtestMixin2Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!RtestMixin2Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface RtestMixin2.");
            }

            return tpe;
        }

        static RtestMixin2Struct() {
            var type = typeof(RtestMixin2);
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
                            RtestMixin2Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class RtestMixin2Struct_JsonNetConverter: JsonNetConverter<RtestMixin2Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RtestMixin2Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RtestMixin2Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("b");
            // Serializing polymorphic type RTestMixin
            writer.WriteStartObject();
            writer.WritePropertyName(v.B.GetFullClassName());
            serializer.Serialize(writer, v.B);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RtestMixin2Struct ReadJson(JsonReader reader, System.Type objectType, RtestMixin2Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new RtestMixin2Struct(
                serializer.Deserialize<Izumi.Test.Domain01.RTestMixin>(json["b"].CreateReader())
            );
        }
    }
}