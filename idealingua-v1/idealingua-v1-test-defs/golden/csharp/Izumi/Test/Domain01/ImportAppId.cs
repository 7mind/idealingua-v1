// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(ImportAppId_JsonNetConverter))]
    public class ImportAppId {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "ImportAppId";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.ImportAppId";
        public string GetPackageName() { return ImportAppId.RTTI_PACKAGE; }
        public string GetClassName() { return ImportAppId.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ImportAppId.RTTI_FULLCLASSNAME; }

        public Guid Id { get; set; }

        public ImportAppId() {
        }

        public ImportAppId(Guid id) {
            this.Id = id;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Id.ToString());
            return "ImportAppId#" + suffix;
        }

        public static ImportAppId From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("ImportAppId#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type ImportAppId, got {0}", value));
            }

            var parts = value.Substring(12, value.Length - 12).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type ImportAppId with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new ImportAppId();
            res.Id = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[0]));
            return res;
        }
    }

    public class ImportAppId_JsonNetConverter: JsonNetConverter<ImportAppId> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ImportAppId_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ImportAppId value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ImportAppId ReadJson(JsonReader reader, System.Type objectType, ImportAppId existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return ImportAppId.From((string)reader.Value);
        }
    }
}