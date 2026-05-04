// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Inheritance {
    [JsonConverter(typeof(DataWithAB_JsonNetConverter))]
    public class DataWithAB : NotificationWithA, NotificationWithB {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance";
        public static readonly string RTTI_CLASSNAME = "DataWithAB";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.DataWithAB";
        public string GetPackageName() { return DataWithAB.RTTI_PACKAGE; }
        public string GetClassName() { return DataWithAB.RTTI_CLASSNAME; }
        public string GetFullClassName() { return DataWithAB.RTTI_FULLCLASSNAME; }

        public DataWithAB() {
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

    }
    public class DataWithAB_JsonNetConverter: JsonNetConverter<DataWithAB> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public DataWithAB_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, DataWithAB v, JsonSerializer serializer) {
            writer.WriteStartObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override DataWithAB ReadJson(JsonReader reader, System.Type objectType, DataWithAB existingValue, bool hasExistingValue, JsonSerializer serializer) {
            reader.Skip();

            return new DataWithAB(

            );
        }
    }
}