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

namespace Idltest.Anyvals {
    [JsonConverter(typeof(UserData_JsonNetConverter))]
    public interface UserData: IRTTI {
        Idltest.Anyvals.WithRecordId Id { get; set; }
    }
    public class UserData_JsonNetConverter: JsonNetConverter<UserData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public UserData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, UserData value, JsonSerializer serializer) {
            // Serializing polymorphic type UserData
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override UserData ReadJson(JsonReader reader, System.Type objectType, UserData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = UserDataStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (UserData)res;
        }
    }

    [JsonConverter(typeof(UserDataStruct_JsonNetConverter))]
    public class UserDataStruct : UserData {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals.UserData";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.UserData.Struct";
        public string GetPackageName() { return UserDataStruct.RTTI_PACKAGE; }
        public string GetClassName() { return UserDataStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return UserDataStruct.RTTI_FULLCLASSNAME; }

        public Idltest.Anyvals.WithRecordId Id { get; set; }

        public UserDataStruct() {
        }

        public UserDataStruct(Idltest.Anyvals.WithRecordId id) {
            this.Id = id;
        }

        public UserData ToUserData() {
            var res = new UserDataStruct();
            res.Id = this.Id;
            return res;
        }

        public void LoadUserData(UserData value) {
            this.Id = value.Id;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            UserDataStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            UserDataStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!UserDataStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface UserData.");
            }

            return tpe;
        }

        static UserDataStruct() {
            var type = typeof(UserData);
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
                            UserDataStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class UserDataStruct_JsonNetConverter: JsonNetConverter<UserDataStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public UserDataStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, UserDataStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            // Serializing polymorphic type WithRecordId
            writer.WriteStartObject();
            writer.WritePropertyName(v.Id.GetFullClassName());
            serializer.Serialize(writer, v.Id);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override UserDataStruct ReadJson(JsonReader reader, System.Type objectType, UserDataStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new UserDataStruct(
                serializer.Deserialize<Idltest.Anyvals.WithRecordId>(json["id"].CreateReader())
            );
        }
    }
}