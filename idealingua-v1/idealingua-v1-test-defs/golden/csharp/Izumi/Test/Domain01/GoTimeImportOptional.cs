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
    [JsonConverter(typeof(GoTimeImportOptional_JsonNetConverter))]
    public class GoTimeImportOptional {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "GoTimeImportOptional";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.GoTimeImportOptional";
        public string GetPackageName() { return GoTimeImportOptional.RTTI_PACKAGE; }
        public string GetClassName() { return GoTimeImportOptional.RTTI_CLASSNAME; }
        public string GetFullClassName() { return GoTimeImportOptional.RTTI_FULLCLASSNAME; }

        public Nullable<DateTime> O { get; set; }

        public GoTimeImportOptional() {
        }

        public GoTimeImportOptional(Nullable<DateTime> o) {
            this.O = o;
        }

    }
    public class GoTimeImportOptional_JsonNetConverter: JsonNetConverter<GoTimeImportOptional> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GoTimeImportOptional_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GoTimeImportOptional v, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (v.O.HasValue) {
                writer.WritePropertyName("o");
                writer.WriteValue(v.O.Value.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));
            }

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GoTimeImportOptional ReadJson(JsonReader reader, System.Type objectType, GoTimeImportOptional existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            Nullable<DateTime> _o = null;
            var _oRaw = json["o"];
            if (_oRaw != null && _oRaw.Type != JTokenType.Null) {
                _o = DateTime.ParseExact(_oRaw.Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None);
            }

            return new GoTimeImportOptional(
                _o
            );
        }
    }
}