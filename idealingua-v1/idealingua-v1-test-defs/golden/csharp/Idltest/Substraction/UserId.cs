// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Substraction {
    [JsonConverter(typeof(UserId_JsonNetConverter))]
    public class UserId {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.substraction";
        public static readonly string RTTI_CLASSNAME = "UserId";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.UserId";
        public string GetPackageName() { return UserId.RTTI_PACKAGE; }
        public string GetClassName() { return UserId.RTTI_CLASSNAME; }
        public string GetFullClassName() { return UserId.RTTI_FULLCLASSNAME; }

        public Guid Value { get; set; }

        public UserId() {
        }

        public UserId(Guid value) {
            this.Value = value;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Value.ToString());
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
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type UserId with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new UserId();
            res.Value = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[0]));
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