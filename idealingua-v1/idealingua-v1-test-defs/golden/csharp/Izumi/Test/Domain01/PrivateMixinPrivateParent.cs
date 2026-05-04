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
    [JsonConverter(typeof(PrivateMixinPrivateParent_JsonNetConverter))]
    public interface PrivateMixinPrivateParent: IRTTI {
        string Parent_embedded { get; set; }
    }
    public class PrivateMixinPrivateParent_JsonNetConverter: JsonNetConverter<PrivateMixinPrivateParent> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PrivateMixinPrivateParent_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PrivateMixinPrivateParent value, JsonSerializer serializer) {
            // Serializing polymorphic type PrivateMixinPrivateParent
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PrivateMixinPrivateParent ReadJson(JsonReader reader, System.Type objectType, PrivateMixinPrivateParent existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = PrivateMixinPrivateParentStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (PrivateMixinPrivateParent)res;
        }
    }

    [JsonConverter(typeof(PrivateMixinPrivateParentStruct_JsonNetConverter))]
    public class PrivateMixinPrivateParentStruct : PrivateMixinPrivateParent {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.PrivateMixinPrivateParent";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.PrivateMixinPrivateParent.Struct";
        public string GetPackageName() { return PrivateMixinPrivateParentStruct.RTTI_PACKAGE; }
        public string GetClassName() { return PrivateMixinPrivateParentStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PrivateMixinPrivateParentStruct.RTTI_FULLCLASSNAME; }

        public string Parent_embedded { get; set; }

        public PrivateMixinPrivateParentStruct() {
        }

        public PrivateMixinPrivateParentStruct(string parent_embedded) {
            this.Parent_embedded = parent_embedded;
        }

        public PrivateMixinPrivateParent ToPrivateMixinPrivateParent() {
            var res = new PrivateMixinPrivateParentStruct();
            res.Parent_embedded = this.Parent_embedded;
            return res;
        }

        public void LoadPrivateMixinPrivateParent(PrivateMixinPrivateParent value) {
            this.Parent_embedded = value.Parent_embedded;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            PrivateMixinPrivateParentStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            PrivateMixinPrivateParentStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!PrivateMixinPrivateParentStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface PrivateMixinPrivateParent.");
            }

            return tpe;
        }

        static PrivateMixinPrivateParentStruct() {
            var type = typeof(PrivateMixinPrivateParent);
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
                            PrivateMixinPrivateParentStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class PrivateMixinPrivateParentStruct_JsonNetConverter: JsonNetConverter<PrivateMixinPrivateParentStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PrivateMixinPrivateParentStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PrivateMixinPrivateParentStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("parent_embedded");
            writer.WriteValue(v.Parent_embedded);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PrivateMixinPrivateParentStruct ReadJson(JsonReader reader, System.Type objectType, PrivateMixinPrivateParentStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PrivateMixinPrivateParentStruct(
                json["parent_embedded"].Value<string>()
            );
        }
    }
}