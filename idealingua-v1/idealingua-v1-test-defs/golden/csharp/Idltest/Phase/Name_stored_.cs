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

namespace Idltest.Phase {
    [JsonConverter(typeof(Name_stored__JsonNetConverter))]
    public interface Name_stored_: Name, IRTTI {
        long Bytes { get; set; }
    }
    public class Name_stored__JsonNetConverter: JsonNetConverter<Name_stored_> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Name_stored__JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Name_stored_ value, JsonSerializer serializer) {
            // Serializing polymorphic type Name_stored_
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Name_stored_ ReadJson(JsonReader reader, System.Type objectType, Name_stored_ existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = Name_stored_Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Name_stored_)res;
        }
    }

    [JsonConverter(typeof(Name_stored_Struct_JsonNetConverter))]
    public class Name_stored_Struct : Name, Name_stored_ {
        public static readonly string RTTI_PACKAGE = "idltest.phase.Name_stored_";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.phase.Name_stored_.Struct";
        public string GetPackageName() { return Name_stored_Struct.RTTI_PACKAGE; }
        public string GetClassName() { return Name_stored_Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Name_stored_Struct.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }
        public long Bytes { get; set; }

        public Name_stored_Struct() {
        }

        public Name_stored_Struct(string name, long bytes) {
            this.Name = name;
            this.Bytes = bytes;
        }

        public Name ToName() {
            var res = new NameStruct();
            res.Name = this.Name;
            return res;
        }

        public void LoadName(Name value) {
            this.Name = value.Name;
        }

        public Name_stored_ ToName_stored_() {
            var res = new Name_stored_Struct();
            res.Name = this.Name;
            res.Bytes = this.Bytes;
            return res;
        }

        public void LoadName_stored_(Name_stored_ value) {
            this.Name = value.Name;
            this.Bytes = value.Bytes;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            Name_stored_Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            Name_stored_Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!Name_stored_Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Name_stored_.");
            }

            return tpe;
        }

        static Name_stored_Struct() {
            var type = typeof(Name_stored_);
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
                            Name_stored_Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class Name_stored_Struct_JsonNetConverter: JsonNetConverter<Name_stored_Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Name_stored_Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Name_stored_Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("bytes");
            writer.WriteValue(v.Bytes);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Name_stored_Struct ReadJson(JsonReader reader, System.Type objectType, Name_stored_Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Name_stored_Struct(
                json["name"].Value<string>(), 
                json["bytes"].Value<long>()
            );
        }
    }
}