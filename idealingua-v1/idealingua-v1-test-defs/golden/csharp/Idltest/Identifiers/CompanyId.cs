// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Identifiers {
    [JsonConverter(typeof(CompanyId_JsonNetConverter))]
    public class CompanyId {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.identifiers";
        public static readonly string RTTI_CLASSNAME = "CompanyId";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.identifiers.CompanyId";
        public string GetPackageName() { return CompanyId.RTTI_PACKAGE; }
        public string GetClassName() { return CompanyId.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CompanyId.RTTI_FULLCLASSNAME; }

        public Guid Value { get; set; }
        public long Iid { get; set; }

        public CompanyId() {
        }

        public CompanyId(Guid value, long iid) {
            this.Value = value;
            this.Iid = iid;
        }

        public override string ToString() {
            var suffix = Iid.ToString() + ":" + IRT.Transport.UrlEscaper.Escape(Value.ToString());
            return "CompanyId#" + suffix;
        }

        public static CompanyId From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("CompanyId#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type CompanyId, got {0}", value));
            }

            var parts = value.Substring(10, value.Length - 10).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 2) {
                throw new ArgumentException(string.Format("Expected identifier for type CompanyId with 2 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new CompanyId();
            res.Iid = long.Parse(parts[0]);
            res.Value = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[1]));
            return res;
        }
    }

    public class CompanyId_JsonNetConverter: JsonNetConverter<CompanyId> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CompanyId_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CompanyId value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CompanyId ReadJson(JsonReader reader, System.Type objectType, CompanyId existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return CompanyId.From((string)reader.Value);
        }
    }
}