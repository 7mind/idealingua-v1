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
    [JsonConverter(typeof(NotificationWithB_JsonNetConverter))]
    public interface NotificationWithB: Notification, IRTTI {
    }
    public class NotificationWithB_JsonNetConverter: JsonNetConverter<NotificationWithB> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotificationWithB_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotificationWithB value, JsonSerializer serializer) {
            // Serializing polymorphic type NotificationWithB
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotificationWithB ReadJson(JsonReader reader, System.Type objectType, NotificationWithB existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NotificationWithBStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (NotificationWithB)res;
        }
    }

    [JsonConverter(typeof(NotificationWithBStruct_JsonNetConverter))]
    public class NotificationWithBStruct : Notification, NotificationWithB {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.NotificationWithB";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.NotificationWithB.Struct";
        public string GetPackageName() { return NotificationWithBStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NotificationWithBStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NotificationWithBStruct.RTTI_FULLCLASSNAME; }

        public NotificationWithBStruct() {
        }

        public Notification ToNotification() {
            var res = new NotificationStruct();

            return res;
        }

        public void LoadNotification(Notification value) {
        }

        public NotificationWithB ToNotificationWithB() {
            var res = new NotificationWithBStruct();

            return res;
        }

        public void LoadNotificationWithB(NotificationWithB value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NotificationWithBStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NotificationWithBStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NotificationWithBStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface NotificationWithB.");
            }

            return tpe;
        }

        static NotificationWithBStruct() {
            var type = typeof(NotificationWithB);
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
                            NotificationWithBStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NotificationWithBStruct_JsonNetConverter: JsonNetConverter<NotificationWithBStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotificationWithBStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotificationWithBStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotificationWithBStruct ReadJson(JsonReader reader, System.Type objectType, NotificationWithBStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new NotificationWithBStruct(

            );
        }
    }
}