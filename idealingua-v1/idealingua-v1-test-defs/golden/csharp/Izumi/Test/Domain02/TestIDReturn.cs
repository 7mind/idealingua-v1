// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(TestIDReturn_JsonNetConverter))]
    public class TestIDReturn {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "izumi.test.domain02";
        public static readonly string RTTI_CLASSNAME = "TestIDReturn";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.TestIDReturn";
        public string GetPackageName() { return TestIDReturn.RTTI_PACKAGE; }
        public string GetClassName() { return TestIDReturn.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestIDReturn.RTTI_FULLCLASSNAME; }

        public int A { get; set; }

        public TestIDReturn() {
        }

        public TestIDReturn(int a) {
            this.A = a;
        }

        public override string ToString() {
            var suffix = A.ToString();
            return "TestIDReturn#" + suffix;
        }

        public static TestIDReturn From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("TestIDReturn#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type TestIDReturn, got {0}", value));
            }

            var parts = value.Substring(13, value.Length - 13).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type TestIDReturn with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new TestIDReturn();
            res.A = int.Parse(parts[0]);
            return res;
        }
    }

    public class TestIDReturn_JsonNetConverter: JsonNetConverter<TestIDReturn> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestIDReturn_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestIDReturn value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestIDReturn ReadJson(JsonReader reader, System.Type objectType, TestIDReturn existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return TestIDReturn.From((string)reader.Value);
        }
    }
}