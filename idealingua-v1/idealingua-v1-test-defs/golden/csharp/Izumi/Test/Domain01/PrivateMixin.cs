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
    [JsonConverter(typeof(PrivateMixin_JsonNetConverter))]
    public interface PrivateMixin: PrivateMixinParent, IRTTI {
        string Parent_embedded { get; set; }
        bool Embedded { get; set; }
    }
    public class PrivateMixin_JsonNetConverter: JsonNetConverter<PrivateMixin> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PrivateMixin_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PrivateMixin value, JsonSerializer serializer) {
            // Serializing polymorphic type PrivateMixin
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PrivateMixin ReadJson(JsonReader reader, System.Type objectType, PrivateMixin existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = PrivateMixinStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (PrivateMixin)res;
        }
    }

    [JsonConverter(typeof(PrivateMixinStruct_JsonNetConverter))]
    public class PrivateMixinStruct : PrivateMixinParent, PrivateMixin {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.PrivateMixin";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.PrivateMixin.Struct";
        public string GetPackageName() { return PrivateMixinStruct.RTTI_PACKAGE; }
        public string GetClassName() { return PrivateMixinStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PrivateMixinStruct.RTTI_FULLCLASSNAME; }

        public string Parent_embedded { get; set; }
        public string Parent { get; set; }
        public bool Embedded { get; set; }

        public PrivateMixinStruct() {
        }

        public PrivateMixinStruct(string parent_embedded, string parent, bool embedded) {
            this.Parent_embedded = parent_embedded;
            this.Parent = parent;
            this.Embedded = embedded;
        }

        public PrivateMixinParent ToPrivateMixinParent() {
            var res = new PrivateMixinParentStruct();
            res.Parent = this.Parent;
            return res;
        }

        public void LoadPrivateMixinParent(PrivateMixinParent value) {
            this.Parent = value.Parent;
        }

        public PrivateMixin ToPrivateMixin() {
            var res = new PrivateMixinStruct();
            res.Parent_embedded = this.Parent_embedded;
            res.Parent = this.Parent;
            res.Embedded = this.Embedded;
            return res;
        }

        public void LoadPrivateMixin(PrivateMixin value) {
            this.Parent_embedded = value.Parent_embedded;
            this.Parent = value.Parent;
            this.Embedded = value.Embedded;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            PrivateMixinStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            PrivateMixinStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!PrivateMixinStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface PrivateMixin.");
            }

            return tpe;
        }

        static PrivateMixinStruct() {
            var type = typeof(PrivateMixin);
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
                            PrivateMixinStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class PrivateMixinStruct_JsonNetConverter: JsonNetConverter<PrivateMixinStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PrivateMixinStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PrivateMixinStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("parent_embedded");
            writer.WriteValue(v.Parent_embedded);
            writer.WritePropertyName("parent");
            writer.WriteValue(v.Parent);
            writer.WritePropertyName("embedded");
            writer.WriteValue(v.Embedded);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PrivateMixinStruct ReadJson(JsonReader reader, System.Type objectType, PrivateMixinStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PrivateMixinStruct(
                json["parent_embedded"].Value<string>(), 
                json["parent"].Value<string>(), 
                json["embedded"].Value<bool>()
            );
        }
    }
}