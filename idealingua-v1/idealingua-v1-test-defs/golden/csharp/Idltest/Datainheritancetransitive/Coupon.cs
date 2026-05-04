// Auto-generated, any modifications may be overwritten in the future.

using System;
using IRT;
using System.Globalization;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Datainheritancetransitive {
    [JsonConverter(typeof(Coupon_JsonNetConverter))]
    public class Coupon {
        public static readonly string RTTI_PACKAGE = "idltest.datainheritancetransitive";
        public static readonly string RTTI_CLASSNAME = "Coupon";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.datainheritancetransitive.Coupon";
        public string GetPackageName() { return Coupon.RTTI_PACKAGE; }
        public string GetClassName() { return Coupon.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Coupon.RTTI_FULLCLASSNAME; }

        public Nullable<DateTime> ValidFrom { get; set; }
        public Nullable<DateTime> ValidTill { get; set; }
        public string Code { get; set; }
        public string Id { get; set; }

        public Coupon() {
        }

        public Coupon(Nullable<DateTime> validFrom, Nullable<DateTime> validTill, string code, string id) {
            this.ValidFrom = validFrom;
            this.ValidTill = validTill;
            this.Code = code;
            this.Id = id;
        }

    }
    public class Coupon_JsonNetConverter: JsonNetConverter<Coupon> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Coupon_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Coupon v, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (v.ValidFrom.HasValue) {
                writer.WritePropertyName("validFrom");
                writer.WriteValue(v.ValidFrom.Value.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));
            }

            if (v.ValidTill.HasValue) {
                writer.WritePropertyName("validTill");
                writer.WriteValue(v.ValidTill.Value.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));
            }

            writer.WritePropertyName("code");
            writer.WriteValue(v.Code);
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Coupon ReadJson(JsonReader reader, System.Type objectType, Coupon existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            Nullable<DateTime> _validFrom = null;
            var _validFromRaw = json["validFrom"];
            if (_validFromRaw != null && _validFromRaw.Type != JTokenType.Null) {
                _validFrom = DateTime.ParseExact(_validFromRaw.Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None);
            }

            Nullable<DateTime> _validTill = null;
            var _validTillRaw = json["validTill"];
            if (_validTillRaw != null && _validTillRaw.Type != JTokenType.Null) {
                _validTill = DateTime.ParseExact(_validTillRaw.Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None);
            }

            return new Coupon(
                _validFrom, 
                _validTill, 
                json["code"].Value<string>(), 
                json["id"].Value<string>()
            );
        }
    }
}