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
    [JsonConverter(typeof(IntNode_JsonNetConverter))]
    public interface IntNode: IRTTI {
        int Lit { get; set; }
    }
    public class IntNode_JsonNetConverter: JsonNetConverter<IntNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IntNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IntNode value, JsonSerializer serializer) {
            // Serializing polymorphic type IntNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IntNode ReadJson(JsonReader reader, System.Type objectType, IntNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = IntNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (IntNode)res;
        }
    }

    [JsonConverter(typeof(IntNodeStruct_JsonNetConverter))]
    public class IntNodeStruct : IntNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.IntNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.IntNode.Struct";
        public string GetPackageName() { return IntNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return IntNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return IntNodeStruct.RTTI_FULLCLASSNAME; }

        public int Lit { get; set; }

        public IntNodeStruct() {
        }

        public IntNodeStruct(int lit) {
            this.Lit = lit;
        }

        public IntNode ToIntNode() {
            var res = new IntNodeStruct();
            res.Lit = this.Lit;
            return res;
        }

        public void LoadIntNode(IntNode value) {
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            IntNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            IntNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!IntNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface IntNode.");
            }

            return tpe;
        }

        static IntNodeStruct() {
            var type = typeof(IntNode);
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
                            IntNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class IntNodeStruct_JsonNetConverter: JsonNetConverter<IntNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IntNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IntNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("lit");
            writer.WriteValue(v.Lit);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IntNodeStruct ReadJson(JsonReader reader, System.Type objectType, IntNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new IntNodeStruct(
                json["lit"].Value<int>()
            );
        }
    }
}