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
    [JsonConverter(typeof(RTestMixin_JsonNetConverter))]
    public interface RTestMixin: IRTTI {
        Izumi.Test.Domain01.RTestEnum A { get; set; }
    }
    public class RTestMixin_JsonNetConverter: JsonNetConverter<RTestMixin> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RTestMixin_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RTestMixin value, JsonSerializer serializer) {
            // Serializing polymorphic type RTestMixin
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RTestMixin ReadJson(JsonReader reader, System.Type objectType, RTestMixin existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = RTestMixinStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (RTestMixin)res;
        }
    }

    [JsonConverter(typeof(RTestMixinStruct_JsonNetConverter))]
    public class RTestMixinStruct : RTestMixin {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.RTestMixin";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.RTestMixin.Struct";
        public string GetPackageName() { return RTestMixinStruct.RTTI_PACKAGE; }
        public string GetClassName() { return RTestMixinStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return RTestMixinStruct.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.RTestEnum A { get; set; }

        public RTestMixinStruct() {
        }

        public RTestMixinStruct(Izumi.Test.Domain01.RTestEnum a) {
            this.A = a;
        }

        public RTestMixin ToRTestMixin() {
            var res = new RTestMixinStruct();
            res.A = this.A;
            return res;
        }

        public void LoadRTestMixin(RTestMixin value) {
            this.A = value.A;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            RTestMixinStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            RTestMixinStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!RTestMixinStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface RTestMixin.");
            }

            return tpe;
        }

        static RTestMixinStruct() {
            var type = typeof(RTestMixin);
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
                            RTestMixinStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class RTestMixinStruct_JsonNetConverter: JsonNetConverter<RTestMixinStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RTestMixinStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RTestMixinStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RTestMixinStruct ReadJson(JsonReader reader, System.Type objectType, RTestMixinStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new RTestMixinStruct(
                Izumi.Test.Domain01.RTestEnumHelpers.From(json["a"].Value<string>())
            );
        }
    }
}