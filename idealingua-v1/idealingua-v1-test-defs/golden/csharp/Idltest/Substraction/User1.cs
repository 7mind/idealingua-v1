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

namespace Idltest.Substraction {
    [JsonConverter(typeof(User1_JsonNetConverter))]
    public interface User1: IRTTI {
        string Name { get; set; }
        string Id { get; set; }
        string Pass { get; set; }
    }
    public class User1_JsonNetConverter: JsonNetConverter<User1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public User1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, User1 value, JsonSerializer serializer) {
            // Serializing polymorphic type User1
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override User1 ReadJson(JsonReader reader, System.Type objectType, User1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = User1Struct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (User1)res;
        }
    }

    [JsonConverter(typeof(User1Struct_JsonNetConverter))]
    public class User1Struct : User1 {
        public static readonly string RTTI_PACKAGE = "idltest.substraction.User1";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.User1.Struct";
        public string GetPackageName() { return User1Struct.RTTI_PACKAGE; }
        public string GetClassName() { return User1Struct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return User1Struct.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }
        public string Id { get; set; }
        public string Pass { get; set; }

        public User1Struct() {
        }

        public User1Struct(string name, string id, string pass) {
            this.Name = name;
            this.Id = id;
            this.Pass = pass;
        }

        public User1 ToUser1() {
            var res = new User1Struct();
            res.Name = this.Name;
            res.Id = this.Id;
            res.Pass = this.Pass;
            return res;
        }

        public void LoadUser1(User1 value) {
            this.Name = value.Name;
            this.Id = value.Id;
            this.Pass = value.Pass;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            User1Struct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            User1Struct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!User1Struct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface User1.");
            }

            return tpe;
        }

        static User1Struct() {
            var type = typeof(User1);
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
                            User1Struct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class User1Struct_JsonNetConverter: JsonNetConverter<User1Struct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public User1Struct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, User1Struct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id);
            writer.WritePropertyName("pass");
            writer.WriteValue(v.Pass);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override User1Struct ReadJson(JsonReader reader, System.Type objectType, User1Struct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new User1Struct(
                json["name"].Value<string>(), 
                json["id"].Value<string>(), 
                json["pass"].Value<string>()
            );
        }
    }
}