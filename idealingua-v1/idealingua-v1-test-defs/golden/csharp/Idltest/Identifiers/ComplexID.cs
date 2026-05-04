// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Identifiers {
    [JsonConverter(typeof(ComplexID_JsonNetConverter))]
    public class ComplexID {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.identifiers";
        public static readonly string RTTI_CLASSNAME = "ComplexID";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.identifiers.ComplexID";
        public string GetPackageName() { return ComplexID.RTTI_PACKAGE; }
        public string GetClassName() { return ComplexID.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ComplexID.RTTI_FULLCLASSNAME; }

        public Idltest.Identifiers.BucketID Bucket { get; set; }
        public Idltest.Identifiers.UserWithEnumId User { get; set; }
        public int I32 { get; set; }
        public Guid Uid { get; set; }
        public string Str { get; set; }

        public ComplexID() {
        }

        public ComplexID(Idltest.Identifiers.BucketID bucket, Idltest.Identifiers.UserWithEnumId user, int i32, Guid uid, string str) {
            this.Bucket = bucket;
            this.User = user;
            this.I32 = i32;
            this.Uid = uid;
            this.Str = str;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Bucket.ToString()) + ":" + I32.ToString() + ":" + IRT.Transport.UrlEscaper.Escape(Str) + ":" + IRT.Transport.UrlEscaper.Escape(Uid.ToString()) + ":" + IRT.Transport.UrlEscaper.Escape(User.ToString());
            return "ComplexID#" + suffix;
        }

        public static ComplexID From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("ComplexID#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type ComplexID, got {0}", value));
            }

            var parts = value.Substring(10, value.Length - 10).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 5) {
                throw new ArgumentException(string.Format("Expected identifier for type ComplexID with 5 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new ComplexID();
            res.Bucket = BucketID.From(IRT.Transport.UrlEscaper.UnEscape(parts[0]));
            res.I32 = int.Parse(parts[1]);
            res.Str = IRT.Transport.UrlEscaper.UnEscape(parts[2]);
            res.Uid = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[3]));
            res.User = UserWithEnumId.From(IRT.Transport.UrlEscaper.UnEscape(parts[4]));
            return res;
        }
    }

    public class ComplexID_JsonNetConverter: JsonNetConverter<ComplexID> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ComplexID_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ComplexID value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ComplexID ReadJson(JsonReader reader, System.Type objectType, ComplexID existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return ComplexID.From((string)reader.Value);
        }
    }
}