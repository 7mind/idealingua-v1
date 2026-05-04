// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Syntax {
    [JsonConverter(typeof(TestId_JsonNetConverter))]
    public class TestId {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.syntax";
        public static readonly string RTTI_CLASSNAME = "TestId";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.syntax.TestId";
        public string GetPackageName() { return TestId.RTTI_PACKAGE; }
        public string GetClassName() { return TestId.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestId.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public TestId() {
        }

        public TestId(string value) {
            this.Value = value;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Value);
            return "TestId#" + suffix;
        }

        public static TestId From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("TestId#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type TestId, got {0}", value));
            }

            var parts = value.Substring(7, value.Length - 7).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type TestId with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new TestId();
            res.Value = IRT.Transport.UrlEscaper.UnEscape(parts[0]);
            return res;
        }
    }

    public class TestId_JsonNetConverter: JsonNetConverter<TestId> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestId_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestId value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestId ReadJson(JsonReader reader, System.Type objectType, TestId existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return TestId.From((string)reader.Value);
        }
    }
}