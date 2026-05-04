// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Syntax {
    [JsonConverter(typeof(TestId1_JsonNetConverter))]
    public class TestId1 {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "idltest.syntax";
        public static readonly string RTTI_CLASSNAME = "TestId1";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.syntax.TestId1";
        public string GetPackageName() { return TestId1.RTTI_PACKAGE; }
        public string GetClassName() { return TestId1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestId1.RTTI_FULLCLASSNAME; }

        public string Value { get; set; }

        public TestId1() {
        }

        public TestId1(string value) {
            this.Value = value;
        }

        public override string ToString() {
            var suffix = IRT.Transport.UrlEscaper.Escape(Value);
            return "TestId1#" + suffix;
        }

        public static TestId1 From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("TestId1#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type TestId1, got {0}", value));
            }

            var parts = value.Substring(8, value.Length - 8).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type TestId1 with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new TestId1();
            res.Value = IRT.Transport.UrlEscaper.UnEscape(parts[0]);
            return res;
        }
    }

    public class TestId1_JsonNetConverter: JsonNetConverter<TestId1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestId1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestId1 value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestId1 ReadJson(JsonReader reader, System.Type objectType, TestId1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return TestId1.From((string)reader.Value);
        }
    }
}