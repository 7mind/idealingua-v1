// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(TestValIdentifier_JsonNetConverter))]
    public class TestValIdentifier {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "TestValIdentifier";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.TestValIdentifier";
        public string GetPackageName() { return TestValIdentifier.RTTI_PACKAGE; }
        public string GetClassName() { return TestValIdentifier.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestValIdentifier.RTTI_FULLCLASSNAME; }

        public string UserId { get; set; }

        public TestValIdentifier() {
        }

        public TestValIdentifier(string userId) {
            this.UserId = userId;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(UserId);
            return "TestValIdentifier#" + suffix;
        }

        public static TestValIdentifier From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("TestValIdentifier#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type TestValIdentifier, got {0}", value));
            }

            var parts = value.Substring(18, value.Length - 18).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type TestValIdentifier with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new TestValIdentifier();
            res.UserId = IRT.Transport.UrlEscaper.UnEscape(parts[0]);
            return res;
        }
    }

    public class TestValIdentifier_JsonNetConverter: JsonNetConverter<TestValIdentifier> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestValIdentifier_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestValIdentifier value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestValIdentifier ReadJson(JsonReader reader, System.Type objectType, TestValIdentifier existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return TestValIdentifier.From((string)reader.Value);
        }
    }
}