// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using IRT;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    [JsonConverter(typeof(TLamNode_JsonNetConverter))]
    public interface TLamNode: LamNode, IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TLamNode_JsonNetConverter: JsonNetConverter<TLamNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TLamNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TLamNode value, JsonSerializer serializer) {
            // Serializing polymorphic type TLamNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TLamNode ReadJson(JsonReader reader, System.Type objectType, TLamNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TLamNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TLamNode)res;
        }
    }

    [JsonConverter(typeof(TLamNodeStruct_JsonNetConverter))]
    public class TLamNodeStruct : LamNode, TLamNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TLamNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TLamNode.Struct";
        public string GetPackageName() { return TLamNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TLamNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TLamNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }
        public List<string> ParamNames { get; set; }
        public Idltest.Ast.AST Body { get; set; }

        public TLamNodeStruct() {
            ParamNames = new List<string>();
        }

        public TLamNodeStruct(Idltest.Ast.Type tpe, List<string> paramNames, Idltest.Ast.AST body) {
            this.Tpe = tpe;
            this.ParamNames = paramNames;
            this.Body = body;
        }

        public LamNode ToLamNode() {
            var res = new LamNodeStruct();
            res.ParamNames = this.ParamNames;
            res.Body = this.Body;
            return res;
        }

        public void LoadLamNode(LamNode value) {
            this.ParamNames = value.ParamNames;
            this.Body = value.Body;
        }

        public TLamNode ToTLamNode() {
            var res = new TLamNodeStruct();
            res.Tpe = this.Tpe;
            res.ParamNames = this.ParamNames;
            res.Body = this.Body;
            return res;
        }

        public void LoadTLamNode(TLamNode value) {
            this.Tpe = value.Tpe;
            this.ParamNames = value.ParamNames;
            this.Body = value.Body;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TLamNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TLamNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TLamNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TLamNode.");
            }

            return tpe;
        }

        static TLamNodeStruct() {
            var type = typeof(TLamNode);
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
                            TLamNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TLamNodeStruct_JsonNetConverter: JsonNetConverter<TLamNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TLamNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TLamNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("tpe");
            serializer.Serialize(writer, v.Tpe);
            writer.WritePropertyName("paramNames");
            writer.WriteStartArray();
            foreach (var lv in v.ParamNames) {
                writer.WriteValue(lv);
            }
            writer.WriteEndArray();

            if (v.Body != null) {
                writer.WritePropertyName("body");
                serializer.Serialize(writer, v.Body);
            }

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TLamNodeStruct ReadJson(JsonReader reader, System.Type objectType, TLamNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            var _paramNames = new List<string>();
            foreach (var _paramNames_sv in (JArray)json["paramNames"]) {
                string _paramNames_d;
                _paramNames_d = _paramNames_sv.Value<string>();
                _paramNames.Add(_paramNames_d);
            }

            Idltest.Ast.AST _body = null;
            var _bodyRaw = json["body"];
            if (_bodyRaw != null && _bodyRaw.Type != JTokenType.Null) {
                _body = serializer.Deserialize<Idltest.Ast.AST>(_bodyRaw.CreateReader());
            }

            return new TLamNodeStruct(
                _tpe, 
                _paramNames, 
                _body
            );
        }
    }
}