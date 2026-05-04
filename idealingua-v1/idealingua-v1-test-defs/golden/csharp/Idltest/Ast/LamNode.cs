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
    [JsonConverter(typeof(LamNode_JsonNetConverter))]
    public interface LamNode: IRTTI {
        List<string> ParamNames { get; set; }
        Idltest.Ast.AST Body { get; set; }
    }
    public class LamNode_JsonNetConverter: JsonNetConverter<LamNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public LamNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, LamNode value, JsonSerializer serializer) {
            // Serializing polymorphic type LamNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override LamNode ReadJson(JsonReader reader, System.Type objectType, LamNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = LamNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (LamNode)res;
        }
    }

    [JsonConverter(typeof(LamNodeStruct_JsonNetConverter))]
    public class LamNodeStruct : LamNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.LamNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.LamNode.Struct";
        public string GetPackageName() { return LamNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return LamNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return LamNodeStruct.RTTI_FULLCLASSNAME; }

        public List<string> ParamNames { get; set; }
        public Idltest.Ast.AST Body { get; set; }

        public LamNodeStruct() {
            ParamNames = new List<string>();
        }

        public LamNodeStruct(List<string> paramNames, Idltest.Ast.AST body) {
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

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            LamNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            LamNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!LamNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface LamNode.");
            }

            return tpe;
        }

        static LamNodeStruct() {
            var type = typeof(LamNode);
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
                            LamNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class LamNodeStruct_JsonNetConverter: JsonNetConverter<LamNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public LamNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, LamNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
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
        public override LamNodeStruct ReadJson(JsonReader reader, System.Type objectType, LamNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
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

            return new LamNodeStruct(
                _paramNames, 
                _body
            );
        }
    }
}