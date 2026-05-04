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

namespace Idltest.Ast {
    [JsonConverter(typeof(FloatNode_JsonNetConverter))]
    public interface FloatNode: IRTTI {
        float Lit { get; set; }
    }
    public class FloatNode_JsonNetConverter: JsonNetConverter<FloatNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public FloatNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, FloatNode value, JsonSerializer serializer) {
            // Serializing polymorphic type FloatNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override FloatNode ReadJson(JsonReader reader, System.Type objectType, FloatNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = FloatNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (FloatNode)res;
        }
    }

    [JsonConverter(typeof(FloatNodeStruct_JsonNetConverter))]
    public class FloatNodeStruct : FloatNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.FloatNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.FloatNode.Struct";
        public string GetPackageName() { return FloatNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return FloatNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return FloatNodeStruct.RTTI_FULLCLASSNAME; }

        public float Lit { get; set; }

        public FloatNodeStruct() {
        }

        public FloatNodeStruct(float lit) {
            this.Lit = lit;
        }

        public FloatNode ToFloatNode() {
            var res = new FloatNodeStruct();
            res.Lit = this.Lit;
            return res;
        }

        public void LoadFloatNode(FloatNode value) {
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            FloatNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            FloatNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!FloatNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface FloatNode.");
            }

            return tpe;
        }

        static FloatNodeStruct() {
            var type = typeof(FloatNode);
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
                            FloatNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class FloatNodeStruct_JsonNetConverter: JsonNetConverter<FloatNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public FloatNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, FloatNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("lit");
            writer.WriteValue(v.Lit);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override FloatNodeStruct ReadJson(JsonReader reader, System.Type objectType, FloatNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new FloatNodeStruct(
                json["lit"].Value<float>()
            );
        }
    }
}