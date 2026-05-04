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
    [JsonConverter(typeof(BoolNode_JsonNetConverter))]
    public interface BoolNode: IRTTI {
        bool Lit { get; set; }
    }
    public class BoolNode_JsonNetConverter: JsonNetConverter<BoolNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public BoolNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, BoolNode value, JsonSerializer serializer) {
            // Serializing polymorphic type BoolNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override BoolNode ReadJson(JsonReader reader, System.Type objectType, BoolNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = BoolNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (BoolNode)res;
        }
    }

    [JsonConverter(typeof(BoolNodeStruct_JsonNetConverter))]
    public class BoolNodeStruct : BoolNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.BoolNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.BoolNode.Struct";
        public string GetPackageName() { return BoolNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return BoolNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return BoolNodeStruct.RTTI_FULLCLASSNAME; }

        public bool Lit { get; set; }

        public BoolNodeStruct() {
        }

        public BoolNodeStruct(bool lit) {
            this.Lit = lit;
        }

        public BoolNode ToBoolNode() {
            var res = new BoolNodeStruct();
            res.Lit = this.Lit;
            return res;
        }

        public void LoadBoolNode(BoolNode value) {
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            BoolNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            BoolNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!BoolNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface BoolNode.");
            }

            return tpe;
        }

        static BoolNodeStruct() {
            var type = typeof(BoolNode);
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
                            BoolNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class BoolNodeStruct_JsonNetConverter: JsonNetConverter<BoolNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public BoolNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, BoolNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("lit");
            writer.WriteValue(v.Lit);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override BoolNodeStruct ReadJson(JsonReader reader, System.Type objectType, BoolNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new BoolNodeStruct(
                json["lit"].Value<bool>()
            );
        }
    }
}