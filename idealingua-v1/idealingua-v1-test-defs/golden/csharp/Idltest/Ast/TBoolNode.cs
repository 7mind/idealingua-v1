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
    [JsonConverter(typeof(TBoolNode_JsonNetConverter))]
    public interface TBoolNode: BoolNode, IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TBoolNode_JsonNetConverter: JsonNetConverter<TBoolNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TBoolNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TBoolNode value, JsonSerializer serializer) {
            // Serializing polymorphic type TBoolNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TBoolNode ReadJson(JsonReader reader, System.Type objectType, TBoolNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TBoolNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TBoolNode)res;
        }
    }

    [JsonConverter(typeof(TBoolNodeStruct_JsonNetConverter))]
    public class TBoolNodeStruct : BoolNode, TBoolNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TBoolNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TBoolNode.Struct";
        public string GetPackageName() { return TBoolNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TBoolNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TBoolNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }
        public bool Lit { get; set; }

        public TBoolNodeStruct() {
        }

        public TBoolNodeStruct(Idltest.Ast.Type tpe, bool lit) {
            this.Tpe = tpe;
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

        public TBoolNode ToTBoolNode() {
            var res = new TBoolNodeStruct();
            res.Tpe = this.Tpe;
            res.Lit = this.Lit;
            return res;
        }

        public void LoadTBoolNode(TBoolNode value) {
            this.Tpe = value.Tpe;
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TBoolNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TBoolNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TBoolNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TBoolNode.");
            }

            return tpe;
        }

        static TBoolNodeStruct() {
            var type = typeof(TBoolNode);
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
                            TBoolNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TBoolNodeStruct_JsonNetConverter: JsonNetConverter<TBoolNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TBoolNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TBoolNodeStruct v, JsonSerializer serializer) {
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
        public override TBoolNodeStruct ReadJson(JsonReader reader, System.Type objectType, TBoolNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            return new TBoolNodeStruct(
                _tpe, 
                json["lit"].Value<bool>()
            );
        }
    }
}