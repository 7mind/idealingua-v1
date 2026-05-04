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
    [JsonConverter(typeof(TAppNode_JsonNetConverter))]
    public interface TAppNode: AppNode, IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TAppNode_JsonNetConverter: JsonNetConverter<TAppNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TAppNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TAppNode value, JsonSerializer serializer) {
            // Serializing polymorphic type TAppNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TAppNode ReadJson(JsonReader reader, System.Type objectType, TAppNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TAppNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TAppNode)res;
        }
    }

    [JsonConverter(typeof(TAppNodeStruct_JsonNetConverter))]
    public class TAppNodeStruct : AppNode, TAppNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TAppNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TAppNode.Struct";
        public string GetPackageName() { return TAppNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TAppNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TAppNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }
        public Idltest.Ast.AST Fun { get; set; }
        public List<Idltest.Ast.AST> Args { get; set; }

        public TAppNodeStruct() {
            Args = new List<Idltest.Ast.AST>();
        }

        public TAppNodeStruct(Idltest.Ast.Type tpe, Idltest.Ast.AST fun, List<Idltest.Ast.AST> args) {
            this.Tpe = tpe;
            this.Fun = fun;
            this.Args = args;
        }

        public AppNode ToAppNode() {
            var res = new AppNodeStruct();
            res.Fun = this.Fun;
            res.Args = this.Args;
            return res;
        }

        public void LoadAppNode(AppNode value) {
            this.Fun = value.Fun;
            this.Args = value.Args;
        }

        public TAppNode ToTAppNode() {
            var res = new TAppNodeStruct();
            res.Tpe = this.Tpe;
            res.Fun = this.Fun;
            res.Args = this.Args;
            return res;
        }

        public void LoadTAppNode(TAppNode value) {
            this.Tpe = value.Tpe;
            this.Fun = value.Fun;
            this.Args = value.Args;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TAppNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TAppNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TAppNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TAppNode.");
            }

            return tpe;
        }

        static TAppNodeStruct() {
            var type = typeof(TAppNode);
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
                            TAppNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TAppNodeStruct_JsonNetConverter: JsonNetConverter<TAppNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TAppNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TAppNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("tpe");
            serializer.Serialize(writer, v.Tpe);
            if (v.Fun != null) {
                writer.WritePropertyName("fun");
                serializer.Serialize(writer, v.Fun);
            }

            writer.WritePropertyName("args");
            writer.WriteStartArray();
            foreach (var lv in v.Args) {
                serializer.Serialize(writer, lv);
            }
            writer.WriteEndArray();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TAppNodeStruct ReadJson(JsonReader reader, System.Type objectType, TAppNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            Idltest.Ast.AST _fun = null;
            var _funRaw = json["fun"];
            if (_funRaw != null && _funRaw.Type != JTokenType.Null) {
                _fun = serializer.Deserialize<Idltest.Ast.AST>(_funRaw.CreateReader());
            }

            var _args = new List<Idltest.Ast.AST>();
            foreach (var _args_sv in (JArray)json["args"]) {
                Idltest.Ast.AST _args_d;
                _args_d = serializer.Deserialize<Idltest.Ast.AST>(_args_sv.CreateReader());
                _args.Add(_args_d);
            }

            return new TAppNodeStruct(
                _tpe, 
                _fun, 
                _args
            );
        }
    }
}