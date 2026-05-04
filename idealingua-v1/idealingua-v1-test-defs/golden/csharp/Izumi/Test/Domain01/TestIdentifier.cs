// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(TestIdentifier_JsonNetConverter))]
    public class TestIdentifier {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "TestIdentifier";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.TestIdentifier";
        public string GetPackageName() { return TestIdentifier.RTTI_PACKAGE; }
        public string GetClassName() { return TestIdentifier.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestIdentifier.RTTI_FULLCLASSNAME; }

        public string UserId { get; set; }
        public string Context { get; set; }
        public sbyte UserType { get; set; }

        public TestIdentifier() {
        }

        public TestIdentifier(string userId, string context, sbyte userType) {
            this.UserId = userId;
            this.Context = context;
            this.UserType = userType;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Context) + ":" + IRT.Transport.UrlEscaper.Escape(UserId) + ":" + UserType.ToString();
            return "TestIdentifier#" + suffix;
        }

        public static TestIdentifier From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("TestIdentifier#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type TestIdentifier, got {0}", value));
            }

            var parts = value.Substring(15, value.Length - 15).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 3) {
                throw new ArgumentException(string.Format("Expected identifier for type TestIdentifier with 3 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new TestIdentifier();
            res.Context = IRT.Transport.UrlEscaper.UnEscape(parts[0]);
            res.UserId = IRT.Transport.UrlEscaper.UnEscape(parts[1]);
            res.UserType = sbyte.Parse(parts[2]);
            return res;
        }
    }

    public class TestIdentifier_JsonNetConverter: JsonNetConverter<TestIdentifier> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestIdentifier_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestIdentifier value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestIdentifier ReadJson(JsonReader reader, System.Type objectType, TestIdentifier existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return TestIdentifier.From((string)reader.Value);
        }
    }
}