// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(SimpleAnyValRecord_JsonNetConverter))]
    public class SimpleAnyValRecord {
        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "SimpleAnyValRecord";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.SimpleAnyValRecord";
        public string GetPackageName() { return SimpleAnyValRecord.RTTI_PACKAGE; }
        public string GetClassName() { return SimpleAnyValRecord.RTTI_CLASSNAME; }
        public string GetFullClassName() { return SimpleAnyValRecord.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public SimpleAnyValRecord() {
        }

        public SimpleAnyValRecord(string value) {
            this.Value = value;
        }

    }
    public class SimpleAnyValRecord_JsonNetConverter: JsonNetConverter<SimpleAnyValRecord> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SimpleAnyValRecord_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SimpleAnyValRecord v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("value");
            writer.WriteValue(v.Value);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SimpleAnyValRecord ReadJson(JsonReader reader, System.Type objectType, SimpleAnyValRecord existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new SimpleAnyValRecord(
                json["value"].Value<string>()
            );
        }
    }
}