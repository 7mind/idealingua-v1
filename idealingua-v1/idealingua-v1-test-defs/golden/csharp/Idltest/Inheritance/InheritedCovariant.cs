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

namespace Idltest.Inheritance {
    [JsonConverter(typeof(InheritedCovariant_JsonNetConverter))]
    public interface InheritedCovariant: WithCovariance, IRTTI {
        // Would have been covariance, but C# doesn't support it:
        // Idltest.Inheritance.CovariantA Field { get; set; }
    }
    public class InheritedCovariant_JsonNetConverter: JsonNetConverter<InheritedCovariant> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public InheritedCovariant_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, InheritedCovariant value, JsonSerializer serializer) {
            // Serializing polymorphic type InheritedCovariant
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override InheritedCovariant ReadJson(JsonReader reader, System.Type objectType, InheritedCovariant existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = InheritedCovariantStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (InheritedCovariant)res;
        }
    }

    [JsonConverter(typeof(InheritedCovariantStruct_JsonNetConverter))]
    public class InheritedCovariantStruct : WithCovariance, InheritedCovariant {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.InheritedCovariant";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.InheritedCovariant.Struct";
        public string GetPackageName() { return InheritedCovariantStruct.RTTI_PACKAGE; }
        public string GetClassName() { return InheritedCovariantStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return InheritedCovariantStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Inheritance.Covariant Field { get; set; }

        public InheritedCovariantStruct() {
        }

        public InheritedCovariantStruct(Idltest.Inheritance.Covariant field) {
            this.Field = field;
        }

        public WithCovariance ToWithCovariance() {
            var res = new WithCovarianceStruct();
            res.Field = this.Field;
            return res;
        }

        public void LoadWithCovariance(WithCovariance value) {
            this.Field = value.Field;
        }

        public InheritedCovariant ToInheritedCovariant() {
            var res = new InheritedCovariantStruct();
            res.Field = this.Field;
            return res;
        }

        public void LoadInheritedCovariant(InheritedCovariant value) {
            this.Field = value.Field;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            InheritedCovariantStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            InheritedCovariantStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!InheritedCovariantStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface InheritedCovariant.");
            }

            return tpe;
        }

        static InheritedCovariantStruct() {
            var type = typeof(InheritedCovariant);
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
                            InheritedCovariantStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class InheritedCovariantStruct_JsonNetConverter: JsonNetConverter<InheritedCovariantStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public InheritedCovariantStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, InheritedCovariantStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("field");
            // Serializing polymorphic type Covariant
            writer.WriteStartObject();
            writer.WritePropertyName(v.Field.GetFullClassName());
            serializer.Serialize(writer, v.Field);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override InheritedCovariantStruct ReadJson(JsonReader reader, System.Type objectType, InheritedCovariantStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new InheritedCovariantStruct(
                serializer.Deserialize<Idltest.Inheritance.Covariant>(json["field"].CreateReader())
            );
        }
    }
}