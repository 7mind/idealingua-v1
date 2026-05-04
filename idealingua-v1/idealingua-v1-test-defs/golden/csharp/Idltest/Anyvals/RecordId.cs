// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Anyvals {
    [JsonConverter(typeof(RecordId_JsonNetConverter))]
    public class RecordId {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.anyvals";
        public static readonly string RTTI_CLASSNAME = "RecordId";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.anyvals.RecordId";
        public string GetPackageName() { return RecordId.RTTI_PACKAGE; }
        public string GetClassName() { return RecordId.RTTI_CLASSNAME; }
        public string GetFullClassName() { return RecordId.RTTI_FULLCLASSNAME; }

        public Guid Value { get; set; }

        public RecordId() {
        }

        public RecordId(Guid value) {
            this.Value = value;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Value.ToString());
            return "RecordId#" + suffix;
        }

        public static RecordId From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("RecordId#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type RecordId, got {0}", value));
            }

            var parts = value.Substring(9, value.Length - 9).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type RecordId with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new RecordId();
            res.Value = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[0]));
            return res;
        }
    }

    public class RecordId_JsonNetConverter: JsonNetConverter<RecordId> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RecordId_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RecordId value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RecordId ReadJson(JsonReader reader, System.Type objectType, RecordId existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return RecordId.From((string)reader.Value);
        }
    }
}