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
    [JsonConverter(typeof(NotificationWithAB_JsonNetConverter))]
    public interface NotificationWithAB: NotificationWithA, NotificationWithB, IRTTI {
    }
    public class NotificationWithAB_JsonNetConverter: JsonNetConverter<NotificationWithAB> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotificationWithAB_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotificationWithAB value, JsonSerializer serializer) {
            // Serializing polymorphic type NotificationWithAB
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotificationWithAB ReadJson(JsonReader reader, System.Type objectType, NotificationWithAB existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NotificationWithABStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (NotificationWithAB)res;
        }
    }

    [JsonConverter(typeof(NotificationWithABStruct_JsonNetConverter))]
    public class NotificationWithABStruct : NotificationWithA, NotificationWithB, NotificationWithAB {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.NotificationWithAB";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.NotificationWithAB.Struct";
        public string GetPackageName() { return NotificationWithABStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NotificationWithABStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NotificationWithABStruct.RTTI_FULLCLASSNAME; }

        public NotificationWithABStruct() {
        }

        public NotificationWithA ToNotificationWithA() {
            var res = new NotificationWithAStruct();

            return res;
        }

        public void LoadNotificationWithA(NotificationWithA value) {
        }

        public NotificationWithB ToNotificationWithB() {
            var res = new NotificationWithBStruct();

            return res;
        }

        public void LoadNotificationWithB(NotificationWithB value) {
        }

        public NotificationWithAB ToNotificationWithAB() {
            var res = new NotificationWithABStruct();

            return res;
        }

        public void LoadNotificationWithAB(NotificationWithAB value) {
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NotificationWithABStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NotificationWithABStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NotificationWithABStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface NotificationWithAB.");
            }

            return tpe;
        }

        static NotificationWithABStruct() {
            var type = typeof(NotificationWithAB);
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
                            NotificationWithABStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NotificationWithABStruct_JsonNetConverter: JsonNetConverter<NotificationWithABStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotificationWithABStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotificationWithABStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotificationWithABStruct ReadJson(JsonReader reader, System.Type objectType, NotificationWithABStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new NotificationWithABStruct(

            );
        }
    }
}