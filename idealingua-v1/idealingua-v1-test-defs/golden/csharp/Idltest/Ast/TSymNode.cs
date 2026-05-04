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
    [JsonConverter(typeof(TSymNode_JsonNetConverter))]
    public interface TSymNode: SymNode, IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TSymNode_JsonNetConverter: JsonNetConverter<TSymNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TSymNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TSymNode value, JsonSerializer serializer) {
            // Serializing polymorphic type TSymNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TSymNode ReadJson(JsonReader reader, System.Type objectType, TSymNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TSymNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TSymNode)res;
        }
    }

    [JsonConverter(typeof(TSymNodeStruct_JsonNetConverter))]
    public class TSymNodeStruct : SymNode, TSymNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TSymNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TSymNode.Struct";
        public string GetPackageName() { return TSymNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TSymNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TSymNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }
        public string Lit { get; set; }

        public TSymNodeStruct() {
        }

        public TSymNodeStruct(Idltest.Ast.Type tpe, string lit) {
            this.Tpe = tpe;
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

        public TSymNode ToTSymNode() {
            var res = new TSymNodeStruct();
            res.Tpe = this.Tpe;
            res.Lit = this.Lit;
            return res;
        }

        public void LoadTSymNode(TSymNode value) {
            this.Tpe = value.Tpe;
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TSymNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TSymNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TSymNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TSymNode.");
            }

            return tpe;
        }

        static TSymNodeStruct() {
            var type = typeof(TSymNode);
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
                            TSymNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TSymNodeStruct_JsonNetConverter: JsonNetConverter<TSymNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TSymNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TSymNodeStruct v, JsonSerializer serializer) {
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
        public override TSymNodeStruct ReadJson(JsonReader reader, System.Type objectType, TSymNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            return new TSymNodeStruct(
                _tpe, 
                json["lit"].Value<string>()
            );
        }
    }
}