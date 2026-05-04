// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Overlaytest.Withoverlay {
    [JsonConverter(typeof(OverlayEmailAttributes_JsonNetConverter))]
    public class OverlayEmailAttributes {
        public static readonly string RTTI_PACKAGE = "overlaytest.withoverlay";
        public static readonly string RTTI_CLASSNAME = "OverlayEmailAttributes";
        public static readonly string RTTI_FULLCLASSNAME = "overlaytest.withoverlay.OverlayEmailAttributes";
        public string GetPackageName() { return OverlayEmailAttributes.RTTI_PACKAGE; }
        public string GetClassName() { return OverlayEmailAttributes.RTTI_CLASSNAME; }
        public string GetFullClassName() { return OverlayEmailAttributes.RTTI_FULLCLASSNAME; }

        public bool Disposable { get; set; }

        public OverlayEmailAttributes() {
        }

        public OverlayEmailAttributes(bool disposable) {
            this.Disposable = disposable;
        }

    }
    public class OverlayEmailAttributes_JsonNetConverter: JsonNetConverter<OverlayEmailAttributes> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public OverlayEmailAttributes_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, OverlayEmailAttributes v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("disposable");
            writer.WriteValue(v.Disposable);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override OverlayEmailAttributes ReadJson(JsonReader reader, System.Type objectType, OverlayEmailAttributes existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new OverlayEmailAttributes(
                json["disposable"].Value<bool>()
            );
        }
    }
}