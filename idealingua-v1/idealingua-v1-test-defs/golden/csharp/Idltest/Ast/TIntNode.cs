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
    [JsonConverter(typeof(TIntNode_JsonNetConverter))]
    public interface TIntNode: IntNode, IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TIntNode_JsonNetConverter: JsonNetConverter<TIntNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TIntNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TIntNode value, JsonSerializer serializer) {
            // Serializing polymorphic type TIntNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TIntNode ReadJson(JsonReader reader, System.Type objectType, TIntNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TIntNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TIntNode)res;
        }
    }

    [JsonConverter(typeof(TIntNodeStruct_JsonNetConverter))]
    public class TIntNodeStruct : IntNode, TIntNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TIntNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TIntNode.Struct";
        public string GetPackageName() { return TIntNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TIntNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TIntNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }
        public int Lit { get; set; }

        public TIntNodeStruct() {
        }

        public TIntNodeStruct(Idltest.Ast.Type tpe, int lit) {
            this.Tpe = tpe;
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

        public TIntNode ToTIntNode() {
            var res = new TIntNodeStruct();
            res.Tpe = this.Tpe;
            res.Lit = this.Lit;
            return res;
        }

        public void LoadTIntNode(TIntNode value) {
            this.Tpe = value.Tpe;
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TIntNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TIntNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TIntNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TIntNode.");
            }

            return tpe;
        }

        static TIntNodeStruct() {
            var type = typeof(TIntNode);
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
                            TIntNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TIntNodeStruct_JsonNetConverter: JsonNetConverter<TIntNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TIntNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TIntNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("tpe");
            serializer.Serialize(writer, v.Tpe);
            writer.WritePropertyName("lit");
            writer.WriteValue(v.Lit);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TIntNodeStruct ReadJson(JsonReader reader, System.Type objectType, TIntNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            return new TIntNodeStruct(
                _tpe, 
                json["lit"].Value<int>()
            );
        }
    }
}