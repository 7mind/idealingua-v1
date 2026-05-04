// Auto-generated, any modifications may be overwritten in the future.

using System;
using IRT;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    [JsonConverter(typeof(TIfNode_JsonNetConverter))]
    public interface TIfNode: IfNode, IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TIfNode_JsonNetConverter: JsonNetConverter<TIfNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TIfNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TIfNode value, JsonSerializer serializer) {
            // Serializing polymorphic type TIfNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TIfNode ReadJson(JsonReader reader, System.Type objectType, TIfNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TIfNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TIfNode)res;
        }
    }

    [JsonConverter(typeof(TIfNodeStruct_JsonNetConverter))]
    public class TIfNodeStruct : IfNode, TIfNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TIfNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TIfNode.Struct";
        public string GetPackageName() { return TIfNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TIfNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TIfNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }
        public Idltest.Ast.AST Cond { get; set; }
        public Idltest.Ast.AST ThenNode { get; set; }
        public Idltest.Ast.AST ElseNode { get; set; }

        public TIfNodeStruct() {
        }

        public TIfNodeStruct(Idltest.Ast.Type tpe, Idltest.Ast.AST cond, Idltest.Ast.AST thenNode, Idltest.Ast.AST elseNode) {
            this.Tpe = tpe;
            this.Cond = cond;
            this.ThenNode = thenNode;
            this.ElseNode = elseNode;
        }

        public IfNode ToIfNode() {
            var res = new IfNodeStruct();
            res.Cond = this.Cond;
            res.ThenNode = this.ThenNode;
            res.ElseNode = this.ElseNode;
            return res;
        }

        public void LoadIfNode(IfNode value) {
            this.Cond = value.Cond;
            this.ThenNode = value.ThenNode;
            this.ElseNode = value.ElseNode;
        }

        public TIfNode ToTIfNode() {
            var res = new TIfNodeStruct();
            res.Tpe = this.Tpe;
            res.Cond = this.Cond;
            res.ThenNode = this.ThenNode;
            res.ElseNode = this.ElseNode;
            return res;
        }

        public void LoadTIfNode(TIfNode value) {
            this.Tpe = value.Tpe;
            this.Cond = value.Cond;
            this.ThenNode = value.ThenNode;
            this.ElseNode = value.ElseNode;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TIfNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TIfNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TIfNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TIfNode.");
            }

            return tpe;
        }

        static TIfNodeStruct() {
            var type = typeof(TIfNode);
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
                            TIfNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TIfNodeStruct_JsonNetConverter: JsonNetConverter<TIfNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TIfNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TIfNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("tpe");
            serializer.Serialize(writer, v.Tpe);
            if (v.Cond != null) {
                writer.WritePropertyName("cond");
                serializer.Serialize(writer, v.Cond);
            }

            if (v.ThenNode != null) {
                writer.WritePropertyName("thenNode");
                serializer.Serialize(writer, v.ThenNode);
            }

            if (v.ElseNode != null) {
                writer.WritePropertyName("elseNode");
                serializer.Serialize(writer, v.ElseNode);
            }

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TIfNodeStruct ReadJson(JsonReader reader, System.Type objectType, TIfNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            Idltest.Ast.AST _cond = null;
            var _condRaw = json["cond"];
            if (_condRaw != null && _condRaw.Type != JTokenType.Null) {
                _cond = serializer.Deserialize<Idltest.Ast.AST>(_condRaw.CreateReader());
            }

            Idltest.Ast.AST _thenNode = null;
            var _thenNodeRaw = json["thenNode"];
            if (_thenNodeRaw != null && _thenNodeRaw.Type != JTokenType.Null) {
                _thenNode = serializer.Deserialize<Idltest.Ast.AST>(_thenNodeRaw.CreateReader());
            }

            Idltest.Ast.AST _elseNode = null;
            var _elseNodeRaw = json["elseNode"];
            if (_elseNodeRaw != null && _elseNodeRaw.Type != JTokenType.Null) {
                _elseNode = serializer.Deserialize<Idltest.Ast.AST>(_elseNodeRaw.CreateReader());
            }

            return new TIfNodeStruct(
                _tpe, 
                _cond, 
                _thenNode, 
                _elseNode
            );
        }
    }
}