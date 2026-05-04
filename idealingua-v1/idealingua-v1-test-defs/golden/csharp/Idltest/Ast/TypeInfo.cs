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
    [JsonConverter(typeof(TypeInfo_JsonNetConverter))]
    public interface TypeInfo: IRTTI {
        Idltest.Ast.Type Tpe { get; set; }
    }
    public class TypeInfo_JsonNetConverter: JsonNetConverter<TypeInfo> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TypeInfo_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TypeInfo value, JsonSerializer serializer) {
            // Serializing polymorphic type TypeInfo
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TypeInfo ReadJson(JsonReader reader, System.Type objectType, TypeInfo existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TypeInfoStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TypeInfo)res;
        }
    }

    [JsonConverter(typeof(TypeInfoStruct_JsonNetConverter))]
    public class TypeInfoStruct : TypeInfo {
        public static readonly string RTTI_PACKAGE = "idltest.ast.TypeInfo";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.TypeInfo.Struct";
        public string GetPackageName() { return TypeInfoStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TypeInfoStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TypeInfoStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Ast.Type Tpe { get; set; }

        public TypeInfoStruct() {
        }

        public TypeInfoStruct(Idltest.Ast.Type tpe) {
            this.Tpe = tpe;
        }

        public TypeInfo ToTypeInfo() {
            var res = new TypeInfoStruct();
            res.Tpe = this.Tpe;
            return res;
        }

        public void LoadTypeInfo(TypeInfo value) {
            this.Tpe = value.Tpe;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TypeInfoStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TypeInfoStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TypeInfoStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TypeInfo.");
            }

            return tpe;
        }

        static TypeInfoStruct() {
            var type = typeof(TypeInfo);
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
                            TypeInfoStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TypeInfoStruct_JsonNetConverter: JsonNetConverter<TypeInfoStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TypeInfoStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TypeInfoStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("tpe");
            serializer.Serialize(writer, v.Tpe);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TypeInfoStruct ReadJson(JsonReader reader, System.Type objectType, TypeInfoStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _tpe = serializer.Deserialize<Idltest.Ast.Type>(json["tpe"].CreateReader());
            return new TypeInfoStruct(
                _tpe
            );
        }
    }
}