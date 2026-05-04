// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Overlaytest.Withoverlay {
    [JsonConverter(typeof(Email_JsonNetConverter))]
    public class Email {
        public static readonly string RTTI_PACKAGE = "overlaytest.withoverlay";
        public static readonly string RTTI_CLASSNAME = "Email";
        public static readonly string RTTI_FULLCLASSNAME = "overlaytest.withoverlay.Email";
        public string GetPackageName() { return Email.RTTI_PACKAGE; }
        public string GetClassName() { return Email.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Email.RTTI_FULLCLASSNAME; }

        public Overlaytest.Withoverlay.OverlayEmailAttributes Attributes { get; set; }

        public Email() {
        }

        public Email(Overlaytest.Withoverlay.OverlayEmailAttributes attributes) {
            this.Attributes = attributes;
        }

    }
    public class Email_JsonNetConverter: JsonNetConverter<Email> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Email_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Email v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("attributes");
            serializer.Serialize(writer, v.Attributes);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Email ReadJson(JsonReader reader, System.Type objectType, Email existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _attributes = serializer.Deserialize<Overlaytest.Withoverlay.OverlayEmailAttributes>(json["attributes"].CreateReader());
            return new Email(
                _attributes
            );
        }
    }
}