// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(OffsetLimit_JsonNetConverter))]
    public class OffsetLimit {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "OffsetLimit";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.OffsetLimit";
        public string GetPackageName() { return OffsetLimit.RTTI_PACKAGE; }
        public string GetClassName() { return OffsetLimit.RTTI_CLASSNAME; }
        public string GetFullClassName() { return OffsetLimit.RTTI_FULLCLASSNAME; }

        public int Offset { get; set; }
        public short Limit { get; set; }

        public OffsetLimit() {
        }

        public OffsetLimit(int offset, short limit) {
            this.Offset = offset;
            this.Limit = limit;
        }

    }
    public class OffsetLimit_JsonNetConverter: JsonNetConverter<OffsetLimit> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public OffsetLimit_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, OffsetLimit v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("offset");
            writer.WriteValue(v.Offset);
            writer.WritePropertyName("limit");
            writer.WriteValue(v.Limit);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override OffsetLimit ReadJson(JsonReader reader, System.Type objectType, OffsetLimit existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new OffsetLimit(
                json["offset"].Value<int>(), 
                json["limit"].Value<short>()
            );
        }
    }
}