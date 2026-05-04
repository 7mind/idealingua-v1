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
    [JsonConverter(typeof(PrivateMixinParent_JsonNetConverter))]
    public interface PrivateMixinParent: IRTTI {
        string Parent { get; set; }
    }
    public class PrivateMixinParent_JsonNetConverter: JsonNetConverter<PrivateMixinParent> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PrivateMixinParent_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PrivateMixinParent value, JsonSerializer serializer) {
            // Serializing polymorphic type PrivateMixinParent
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PrivateMixinParent ReadJson(JsonReader reader, System.Type objectType, PrivateMixinParent existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = PrivateMixinParentStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (PrivateMixinParent)res;
        }
    }

    [JsonConverter(typeof(PrivateMixinParentStruct_JsonNetConverter))]
    public class PrivateMixinParentStruct : PrivateMixinParent {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.PrivateMixinParent";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.PrivateMixinParent.Struct";
        public string GetPackageName() { return PrivateMixinParentStruct.RTTI_PACKAGE; }
        public string GetClassName() { return PrivateMixinParentStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PrivateMixinParentStruct.RTTI_FULLCLASSNAME; }

        public string Parent { get; set; }

        public PrivateMixinParentStruct() {
        }

        public PrivateMixinParentStruct(string parent) {
            this.Parent = parent;
        }

        public PrivateMixinParent ToPrivateMixinParent() {
            var res = new PrivateMixinParentStruct();
            res.Parent = this.Parent;
            return res;
        }

        public void LoadPrivateMixinParent(PrivateMixinParent value) {
            this.Parent = value.Parent;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            PrivateMixinParentStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            PrivateMixinParentStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!PrivateMixinParentStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface PrivateMixinParent.");
            }

            return tpe;
        }

        static PrivateMixinParentStruct() {
            var type = typeof(PrivateMixinParent);
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
                            PrivateMixinParentStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class PrivateMixinParentStruct_JsonNetConverter: JsonNetConverter<PrivateMixinParentStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PrivateMixinParentStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PrivateMixinParentStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("parent");
            writer.WriteValue(v.Parent);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PrivateMixinParentStruct ReadJson(JsonReader reader, System.Type objectType, PrivateMixinParentStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PrivateMixinParentStruct(
                json["parent"].Value<string>()
            );
        }
    }
}