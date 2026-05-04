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
    [JsonConverter(typeof(SymNode_JsonNetConverter))]
    public interface SymNode: IRTTI {
        string Lit { get; set; }
    }
    public class SymNode_JsonNetConverter: JsonNetConverter<SymNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SymNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SymNode value, JsonSerializer serializer) {
            // Serializing polymorphic type SymNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SymNode ReadJson(JsonReader reader, System.Type objectType, SymNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = SymNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (SymNode)res;
        }
    }

    [JsonConverter(typeof(SymNodeStruct_JsonNetConverter))]
    public class SymNodeStruct : SymNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.SymNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.SymNode.Struct";
        public string GetPackageName() { return SymNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return SymNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return SymNodeStruct.RTTI_FULLCLASSNAME; }

        public string Lit { get; set; }

        public SymNodeStruct() {
        }

        public SymNodeStruct(string lit) {
            this.Lit = lit;
        }

        public SymNode ToSymNode() {
            var res = new SymNodeStruct();
            res.Lit = this.Lit;
            return res;
        }

        public void LoadSymNode(SymNode value) {
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            SymNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            SymNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!SymNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface SymNode.");
            }

            return tpe;
        }

        static SymNodeStruct() {
            var type = typeof(SymNode);
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
                            SymNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class SymNodeStruct_JsonNetConverter: JsonNetConverter<SymNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SymNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SymNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("lit");
            writer.WriteValue(v.Lit);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SymNodeStruct ReadJson(JsonReader reader, System.Type objectType, SymNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new SymNodeStruct(
                json["lit"].Value<string>()
            );
        }
    }
}