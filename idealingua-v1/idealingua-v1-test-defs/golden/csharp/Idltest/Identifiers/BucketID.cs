// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Identifiers {
    [JsonConverter(typeof(BucketID_JsonNetConverter))]
    public class BucketID {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.identifiers";
        public static readonly string RTTI_CLASSNAME = "BucketID";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.identifiers.BucketID";
        public string GetPackageName() { return BucketID.RTTI_PACKAGE; }
        public string GetClassName() { return BucketID.RTTI_CLASSNAME; }
        public string GetFullClassName() { return BucketID.RTTI_FULLCLASSNAME; }

        public Guid App { get; set; }
        public Guid User { get; set; }
        public string Bucket { get; set; }

        public BucketID() {
        }

        public BucketID(Guid app, Guid user, string bucket) {
            this.App = app;
            this.User = user;
            this.Bucket = bucket;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(App.ToString()) + ":" + IRT.Transport.UrlEscaper.Escape(Bucket) + ":" + IRT.Transport.UrlEscaper.Escape(User.ToString());
            return "BucketID#" + suffix;
        }

        public static BucketID From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("BucketID#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type BucketID, got {0}", value));
            }

            var parts = value.Substring(9, value.Length - 9).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 3) {
                throw new ArgumentException(string.Format("Expected identifier for type BucketID with 3 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new BucketID();
            res.App = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[0]));
            res.Bucket = IRT.Transport.UrlEscaper.UnEscape(parts[1]);
            res.User = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[2]));
            return res;
        }
    }

    public class BucketID_JsonNetConverter: JsonNetConverter<BucketID> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public BucketID_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, BucketID value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override BucketID ReadJson(JsonReader reader, System.Type objectType, BucketID existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return BucketID.From((string)reader.Value);
        }
    }
}