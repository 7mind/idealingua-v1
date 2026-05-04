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
    [JsonConverter(typeof(TFloatNode_JsonNetConverter))]
    public interface TFloatNode: FloatNode, IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TFloatNode_JsonNetConverter: JsonNetConverter<TFloatNode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TFloatNode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TFloatNode value, JsonSerializer serializer) {
            // Serializing polymorphic type TFloatNode
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TFloatNode ReadJson(JsonReader reader, System.Type objectType, TFloatNode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TFloatNodeStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TFloatNode)res;
        }
    }

    [JsonConverter(typeof(TFloatNodeStruct_JsonNetConverter))]
    public class TFloatNodeStruct : FloatNode, TFloatNode {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TFloatNode";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TFloatNode.Struct";
        public string GetPackageName() { return TFloatNodeStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TFloatNodeStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TFloatNodeStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }
        public float Lit { get; set; }

        public TFloatNodeStruct() {
        }

        public TFloatNodeStruct(Idltest.Ast.Type tpe, float lit) {
            this.Tpe = tpe;
            this.Lit = lit;
        }

        public FloatNode ToFloatNode() {
            var res = new FloatNodeStruct();
            res.Lit = this.Lit;
            return res;
        }

        public void LoadFloatNode(FloatNode value) {
            this.Lit = value.Lit;
        }

        public TFloatNode ToTFloatNode() {
            var res = new TFloatNodeStruct();
            res.Tpe = this.Tpe;
            res.Lit = this.Lit;
            return res;
        }

        public void LoadTFloatNode(TFloatNode value) {
            this.Tpe = value.Tpe;
            this.Lit = value.Lit;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TFloatNodeStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TFloatNodeStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TFloatNodeStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TFloatNode.");
            }

            return tpe;
        }

        static TFloatNodeStruct() {
            var type = typeof(TFloatNode);
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
                            TFloatNodeStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TFloatNodeStruct_JsonNetConverter: JsonNetConverter<TFloatNodeStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TFloatNodeStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TFloatNodeStruct v, JsonSerializer serializer) {
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
        public override TFloatNodeStruct ReadJson(JsonReader reader, System.Type objectType, TFloatNodeStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            return new TFloatNodeStruct(
                _tpe, 
                json["lit"].Value<float>()
            );
        }
    }
}