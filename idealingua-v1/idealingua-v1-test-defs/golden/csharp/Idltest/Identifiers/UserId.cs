// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Identifiers {
    [JsonConverter(typeof(UserId_JsonNetConverter))]
    public class UserId {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.identifiers";
        public static readonly string RTTI_CLASSNAME = "UserId";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.identifiers.UserId";
        public string GetPackageName() { return UserId.RTTI_PACKAGE; }
        public string GetClassName() { return UserId.RTTI_CLASSNAME; }
        public string GetFullClassName() { return UserId.RTTI_FULLCLASSNAME; }

        public Guid Value { get; set; }
        public Guid Company { get; set; }

        public UserId() {
        }

        public UserId(Guid value, Guid company) {
            this.Value = value;
            this.Company = company;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Company.ToString()) + ":" + IRT.Transport.UrlEscaper.Escape(Value.ToString());
            return "UserId#" + suffix;
        }

        public static UserId From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("UserId#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type UserId, got {0}", value));
            }

            var parts = value.Substring(7, value.Length - 7).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 2) {
                throw new ArgumentException(string.Format("Expected identifier for type UserId with 2 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new UserId();
            res.Company = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[0]));
            res.Value = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[1]));
            return res;
        }
    }

    public class UserId_JsonNetConverter: JsonNetConverter<UserId> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public UserId_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, UserId value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override UserId ReadJson(JsonReader reader, System.Type objectType, UserId existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return UserId.From((string)reader.Value);
        }
    }
}