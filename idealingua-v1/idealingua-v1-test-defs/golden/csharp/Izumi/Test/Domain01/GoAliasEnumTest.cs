// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    // GoAliasEnumTest Enumeration
    public enum GoAliasEnumTest {
        Val1,
        Val2
    }

    public static class GoAliasEnumTestHelpers {
        public static GoAliasEnumTest From(string value) {
            switch (value) {
                case "Val1": return GoAliasEnumTest.Val1;
                case "Val2": return GoAliasEnumTest.Val2;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(GoAliasEnumTest), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly GoAliasEnumTest[] all = new GoAliasEnumTest[] {
            GoAliasEnumTest.Val1,
            GoAliasEnumTest.Val2
        };

        public static GoAliasEnumTest[] GetAll() {
            return GoAliasEnumTestHelpers.all;
        }

        // Extensions

        public static string ToString(this GoAliasEnumTest e) {
            return Enum.GetName(typeof(GoAliasEnumTest), e);
        }
    }

    public class GoAliasEnumTest_JsonNetConverter: JsonNetConverter<GoAliasEnumTest> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GoAliasEnumTest_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GoAliasEnumTest value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GoAliasEnumTest ReadJson(JsonReader reader, System.Type objectType, GoAliasEnumTest existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return GoAliasEnumTestHelpers.From((string)reader.Value);
        }
    }
}