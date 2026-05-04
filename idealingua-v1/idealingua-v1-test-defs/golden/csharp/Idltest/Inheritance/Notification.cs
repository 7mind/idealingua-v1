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
    [JsonConverter(typeof(Notification_JsonNetConverter))]
    public interface Notification: IRTTI {
    }
    public class Notification_JsonNetConverter: JsonNetConverter<Notification> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Notification_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Notification value, JsonSerializer serializer) {
            // Serializing polymorphic type Notification
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Notification ReadJson(JsonReader reader, System.Type objectType, Notification existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NotificationStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (Notification)res;
        }
    }

    [JsonConverter(typeof(NotificationStruct_JsonNetConverter))]
    public class NotificationStruct : Notification {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.Notification";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.Notification.Struct";
        public string GetPackageName() { return NotificationStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NotificationStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NotificationStruct.RTTI_FULLCLASSNAME; }

        public NotificationStruct() {
        }

        public Notification ToNotification() {
            var res = new NotificationStruct();

            return res;
        }

        public void LoadNotification(Notification value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NotificationStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NotificationStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NotificationStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface Notification.");
            }

            return tpe;
        }

        static NotificationStruct() {
            var type = typeof(Notification);
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
                            NotificationStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NotificationStruct_JsonNetConverter: JsonNetConverter<NotificationStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotificationStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotificationStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotificationStruct ReadJson(JsonReader reader, System.Type objectType, NotificationStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new NotificationStruct(

            );
        }
    }
}