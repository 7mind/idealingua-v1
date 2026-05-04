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
    [JsonConverter(typeof(NotificationWithA_JsonNetConverter))]
    public interface NotificationWithA: Notification, IRTTI {
    }
    public class NotificationWithA_JsonNetConverter: JsonNetConverter<NotificationWithA> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotificationWithA_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotificationWithA value, JsonSerializer serializer) {
            // Serializing polymorphic type NotificationWithA
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotificationWithA ReadJson(JsonReader reader, System.Type objectType, NotificationWithA existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NotificationWithAStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (NotificationWithA)res;
        }
    }

    [JsonConverter(typeof(NotificationWithAStruct_JsonNetConverter))]
    public class NotificationWithAStruct : Notification, NotificationWithA {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.NotificationWithA";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.NotificationWithA.Struct";
        public string GetPackageName() { return NotificationWithAStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NotificationWithAStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NotificationWithAStruct.RTTI_FULLCLASSNAME; }

        public NotificationWithAStruct() {
        }

        public Notification ToNotification() {
            var res = new NotificationStruct();

            return res;
        }

        public void LoadNotification(Notification value) {
        }

        public NotificationWithA ToNotificationWithA() {
            var res = new NotificationWithAStruct();

            return res;
        }

        public void LoadNotificationWithA(NotificationWithA value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NotificationWithAStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NotificationWithAStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NotificationWithAStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface NotificationWithA.");
            }

            return tpe;
        }

        static NotificationWithAStruct() {
            var type = typeof(NotificationWithA);
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
                            NotificationWithAStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NotificationWithAStruct_JsonNetConverter: JsonNetConverter<NotificationWithAStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotificationWithAStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotificationWithAStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotificationWithAStruct ReadJson(JsonReader reader, System.Type objectType, NotificationWithAStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new NotificationWithAStruct(

            );
        }
    }
}