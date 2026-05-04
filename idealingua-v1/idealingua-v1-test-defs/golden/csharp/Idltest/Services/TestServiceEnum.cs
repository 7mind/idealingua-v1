// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Services {
    // TestServiceEnum Enumeration
    public enum TestServiceEnum {
        Value1,
        Value2
    }

    public static class TestServiceEnumHelpers {
        public static TestServiceEnum From(string value) {
            switch (value) {
                case "Value1": return TestServiceEnum.Value1;
                case "Value2": return TestServiceEnum.Value2;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(TestServiceEnum), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly TestServiceEnum[] all = new TestServiceEnum[] {
            TestServiceEnum.Value1,
            TestServiceEnum.Value2
        };

        public static TestServiceEnum[] GetAll() {
            return TestServiceEnumHelpers.all;
        }

        // Extensions

        public static string ToString(this TestServiceEnum e) {
            return Enum.GetName(typeof(TestServiceEnum), e);
        }
    }

    public class TestServiceEnum_JsonNetConverter: JsonNetConverter<TestServiceEnum> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestServiceEnum_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestServiceEnum value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestServiceEnum ReadJson(JsonReader reader, System.Type objectType, TestServiceEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return TestServiceEnumHelpers.From((string)reader.Value);
        }
    }
}