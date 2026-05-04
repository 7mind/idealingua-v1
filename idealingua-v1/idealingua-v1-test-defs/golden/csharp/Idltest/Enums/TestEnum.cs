// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Enums {
    // TestEnum Enumeration
    public enum TestEnum {
        Element1,
        Element2,
        Element3,
        Element4
    }

    public static class TestEnumHelpers {
        public static TestEnum From(string value) {
            switch (value) {
                case "Element1": return TestEnum.Element1;
                case "Element2": return TestEnum.Element2;
                case "Element3": return TestEnum.Element3;
                case "Element4": return TestEnum.Element4;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(TestEnum), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly TestEnum[] all = new TestEnum[] {
            TestEnum.Element1,
            TestEnum.Element2,
            TestEnum.Element3,
            TestEnum.Element4
        };

        public static TestEnum[] GetAll() {
            return TestEnumHelpers.all;
        }

        // Extensions

        public static string ToString(this TestEnum e) {
            return Enum.GetName(typeof(TestEnum), e);
        }
    }

    public class TestEnum_JsonNetConverter: JsonNetConverter<TestEnum> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestEnum_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestEnum value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestEnum ReadJson(JsonReader reader, System.Type objectType, TestEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return TestEnumHelpers.From((string)reader.Value);
        }
    }
}