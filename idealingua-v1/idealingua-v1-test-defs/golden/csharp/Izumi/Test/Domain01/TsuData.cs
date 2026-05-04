// Auto-generated, any modifications may be overwritten in the future.

using System;
using IRT;
using System.Globalization;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(TsuData_JsonNetConverter))]
    public class TsuData {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "TsuData";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.TsuData";
        public string GetPackageName() { return TsuData.RTTI_PACKAGE; }
        public string GetClassName() { return TsuData.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TsuData.RTTI_FULLCLASSNAME; }

        public DateTime Since { get; set; }

        public TsuData() {
        }

        public TsuData(DateTime since) {
            this.Since = since;
        }

    }
    public class TsuData_JsonNetConverter: JsonNetConverter<TsuData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TsuData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TsuData v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("since");
            writer.WriteValue(v.Since.ToUniversalTime().ToString(JsonNetTimeFormats.TsuDefault, CultureInfo.InvariantCulture));
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TsuData ReadJson(JsonReader reader, System.Type objectType, TsuData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new TsuData(
                DateTime.ParseExact(json["since"].Value<string>(), JsonNetTimeFormats.Tsu, CultureInfo.InvariantCulture, DateTimeStyles.None)
            );
        }
    }
}