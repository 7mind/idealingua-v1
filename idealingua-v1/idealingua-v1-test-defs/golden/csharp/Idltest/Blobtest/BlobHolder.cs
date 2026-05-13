// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Blobtest {
    [JsonConverter(typeof(BlobHolder_JsonNetConverter))]
    public class BlobHolder {
        public static readonly string RTTI_PACKAGE = "idltest.blobtest";
        public static readonly string RTTI_CLASSNAME = "BlobHolder";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.blobtest.BlobHolder";
        public string GetPackageName() { return BlobHolder.RTTI_PACKAGE; }
        public string GetClassName() { return BlobHolder.RTTI_CLASSNAME; }
        public string GetFullClassName() { return BlobHolder.RTTI_FULLCLASSNAME; }

        public byte[] Payload { get; set; }
        public string Label { get; set; }

        public BlobHolder() {
        }

        public BlobHolder(byte[] payload, string label) {
            this.Payload = payload;
            this.Label = label;
        }

    }
    public class BlobHolder_JsonNetConverter: JsonNetConverter<BlobHolder> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public BlobHolder_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, BlobHolder v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("payload");
            writer.WriteValue(System.Convert.ToBase64String(v.Payload));
            writer.WritePropertyName("label");
            writer.WriteValue(v.Label);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override BlobHolder ReadJson(JsonReader reader, System.Type objectType, BlobHolder existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new BlobHolder(
                System.Convert.FromBase64String(json["payload"].Value<string>()), 
                json["label"].Value<string>()
            );
        }
    }
}