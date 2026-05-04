// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Identifiers {
    [JsonConverter(typeof(UserWithEnumId_JsonNetConverter))]
    public class UserWithEnumId {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.identifiers";
        public static readonly string RTTI_CLASSNAME = "UserWithEnumId";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.identifiers.UserWithEnumId";
        public string GetPackageName() { return UserWithEnumId.RTTI_PACKAGE; }
        public string GetClassName() { return UserWithEnumId.RTTI_CLASSNAME; }
        public string GetFullClassName() { return UserWithEnumId.RTTI_FULLCLASSNAME; }

        public Guid Value { get; set; }
        public Guid Company { get; set; }
        public Idltest.Identifiers.DepartmentEnum Dept { get; set; }

        public UserWithEnumId() {
        }

        public UserWithEnumId(Guid value, Guid company, Idltest.Identifiers.DepartmentEnum dept) {
            this.Value = value;
            this.Company = company;
            this.Dept = dept;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Company.ToString()) + ":" + IRT.Transport.UrlEscaper.Escape(Dept.ToString()) + ":" + IRT.Transport.UrlEscaper.Escape(Value.ToString());
            return "UserWithEnumId#" + suffix;
        }

        public static UserWithEnumId From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("UserWithEnumId#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type UserWithEnumId, got {0}", value));
            }

            var parts = value.Substring(15, value.Length - 15).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 3) {
                throw new ArgumentException(string.Format("Expected identifier for type UserWithEnumId with 3 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new UserWithEnumId();
            res.Company = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[0]));
            res.Dept = DepartmentEnumHelpers.From(IRT.Transport.UrlEscaper.UnEscape(parts[1]));
            res.Value = new Guid(IRT.Transport.UrlEscaper.UnEscape(parts[2]));
            return res;
        }
    }

    public class UserWithEnumId_JsonNetConverter: JsonNetConverter<UserWithEnumId> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public UserWithEnumId_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, UserWithEnumId value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override UserWithEnumId ReadJson(JsonReader reader, System.Type objectType, UserWithEnumId existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return UserWithEnumId.From((string)reader.Value);
        }
    }
}