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
    [JsonConverter(typeof(ExtendedMixin_JsonNetConverter))]
    public interface ExtendedMixin: IRTTI {
        string Parent_embedded { get; set; }
        string Parent { get; set; }
        bool Embedded { get; set; }
        sbyte Own { get; set; }
    }
    public class ExtendedMixin_JsonNetConverter: JsonNetConverter<ExtendedMixin> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ExtendedMixin_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ExtendedMixin value, JsonSerializer serializer) {
            // Serializing polymorphic type ExtendedMixin
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ExtendedMixin ReadJson(JsonReader reader, System.Type objectType, ExtendedMixin existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = ExtendedMixinStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (ExtendedMixin)res;
        }
    }

    [JsonConverter(typeof(ExtendedMixinStruct_JsonNetConverter))]
    public class ExtendedMixinStruct : ExtendedMixin {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.ExtendedMixin";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.ExtendedMixin.Struct";
        public string GetPackageName() { return ExtendedMixinStruct.RTTI_PACKAGE; }
        public string GetClassName() { return ExtendedMixinStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ExtendedMixinStruct.RTTI_FULLCLASSNAME; }

        public string Parent_embedded { get; set; }
        public string Parent { get; set; }
        public bool Embedded { get; set; }
        public sbyte Own { get; set; }

        public ExtendedMixinStruct() {
        }

        public ExtendedMixinStruct(string parent_embedded, string parent, bool embedded, sbyte own) {
            this.Parent_embedded = parent_embedded;
            this.Parent = parent;
            this.Embedded = embedded;
            this.Own = own;
        }

        public ExtendedMixin ToExtendedMixin() {
            var res = new ExtendedMixinStruct();
            res.Parent_embedded = this.Parent_embedded;
            res.Parent = this.Parent;
            res.Embedded = this.Embedded;
            res.Own = this.Own;
            return res;
        }

        public void LoadExtendedMixin(ExtendedMixin value) {
            this.Parent_embedded = value.Parent_embedded;
            this.Parent = value.Parent;
            this.Embedded = value.Embedded;
            this.Own = value.Own;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            ExtendedMixinStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            ExtendedMixinStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!ExtendedMixinStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface ExtendedMixin.");
            }

            return tpe;
        }

        static ExtendedMixinStruct() {
            var type = typeof(ExtendedMixin);
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
                            ExtendedMixinStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class ExtendedMixinStruct_JsonNetConverter: JsonNetConverter<ExtendedMixinStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ExtendedMixinStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ExtendedMixinStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("parent_embedded");
            writer.WriteValue(v.Parent_embedded);
            writer.WritePropertyName("parent");
            writer.WriteValue(v.Parent);
            writer.WritePropertyName("embedded");
            writer.WriteValue(v.Embedded);
            writer.WritePropertyName("own");
            writer.WriteValue(v.Own);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ExtendedMixinStruct ReadJson(JsonReader reader, System.Type objectType, ExtendedMixinStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new ExtendedMixinStruct(
                json["parent_embedded"].Value<string>(), 
                json["parent"].Value<string>(), 
                json["embedded"].Value<bool>(), 
                json["own"].Value<sbyte>()
            );
        }
    }
}