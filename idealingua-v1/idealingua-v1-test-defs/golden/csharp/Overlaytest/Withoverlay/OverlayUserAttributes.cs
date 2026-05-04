// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Overlaytest.Withoverlay {
    [JsonConverter(typeof(OverlayUserAttributes_JsonNetConverter))]
    public class OverlayUserAttributes {
        public static readonly string RTTI_PACKAGE = "overlaytest.withoverlay";
        public static readonly string RTTI_CLASSNAME = "OverlayUserAttributes";
        public static readonly string RTTI_FULLCLASSNAME = "overlaytest.withoverlay.OverlayUserAttributes";
        public string GetPackageName() { return OverlayUserAttributes.RTTI_PACKAGE; }
        public string GetClassName() { return OverlayUserAttributes.RTTI_CLASSNAME; }
        public string GetFullClassName() { return OverlayUserAttributes.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }
        public string Surname { get; set; }

        public OverlayUserAttributes() {
        }

        public OverlayUserAttributes(string name, string surname) {
            this.Name = name;
            this.Surname = surname;
        }

    }
    public class OverlayUserAttributes_JsonNetConverter: JsonNetConverter<OverlayUserAttributes> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public OverlayUserAttributes_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, OverlayUserAttributes v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("surname");
            writer.WriteValue(v.Surname);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override OverlayUserAttributes ReadJson(JsonReader reader, System.Type objectType, OverlayUserAttributes existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new OverlayUserAttributes(
                json["name"].Value<string>(), 
                json["surname"].Value<string>()
            );
        }
    }
}