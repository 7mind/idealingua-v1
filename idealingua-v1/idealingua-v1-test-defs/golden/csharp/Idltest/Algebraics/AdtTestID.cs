// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    [JsonConverter(typeof(AdtTestID_JsonNetConverter))]
    public class AdtTestID {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.algebraics";
        public static readonly string RTTI_CLASSNAME = "AdtTestID";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.algebraics.AdtTestID";
        public string GetPackageName() { return AdtTestID.RTTI_PACKAGE; }
        public string GetClassName() { return AdtTestID.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AdtTestID.RTTI_FULLCLASSNAME; }

        public string Id { get; set; }

        public AdtTestID() {
        }

        public AdtTestID(string id) {
            this.Id = id;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Id);
            return "AdtTestID#" + suffix;
        }

        public static AdtTestID From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("AdtTestID#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type AdtTestID, got {0}", value));
            }

            var parts = value.Substring(10, value.Length - 10).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type AdtTestID with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new AdtTestID();
            res.Id = IRT.Transport.UrlEscaper.UnEscape(parts[0]);
            return res;
        }
    }

    public class AdtTestID_JsonNetConverter: JsonNetConverter<AdtTestID> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AdtTestID_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AdtTestID value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AdtTestID ReadJson(JsonReader reader, System.Type objectType, AdtTestID existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return AdtTestID.From((string)reader.Value);
        }
    }
}