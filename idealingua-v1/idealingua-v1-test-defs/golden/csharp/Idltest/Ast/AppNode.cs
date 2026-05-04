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
    [JsonConverter(typeof(AppNode_JsonNetConverter))]
    public interface AppNode: IRTTI {
        Idltest.Ast.AST Fun { get; set; }
        List<Idltest.Ast.AST> Args { get; set; }
    }
    public class AppNode_JsonNetConverter: JsonNetConverter<AppNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AppNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AppNode value, JsonSerializer serializer) {
            // Serializing polymorphic type AppNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AppNode ReadJson(JsonReader reader, System.Type objectType, AppNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = AppNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (AppNode)res;
        }
    }

    [JsonConverter(typeof(AppNodeStruct_JsonNetConverter))]
    public class AppNodeStruct : AppNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.AppNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.AppNode.Struct";
        public string GetPackageName() { return AppNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return AppNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AppNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.AST Fun { get; set; }
        public List<Idltest.Ast.AST> Args { get; set; }

        public AppNodeStruct() {
            Args = new List<Idltest.Ast.AST>();
        }

        public AppNodeStruct(Idltest.Ast.AST fun, List<Idltest.Ast.AST> args) {
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

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            AppNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            AppNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!AppNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface AppNode.");
            }

            return tpe;
        }

        static AppNodeStruct() {
            var type = typeof(AppNode);
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
                            AppNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class AppNodeStruct_JsonNetConverter: JsonNetConverter<AppNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AppNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AppNodeStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
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
        public override AppNodeStruct ReadJson(JsonReader reader, System.Type objectType, AppNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
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

            return new AppNodeStruct(
                _fun, 
                _args
            );
        }
    }
}