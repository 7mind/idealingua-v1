// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Enums {
    // ShortSyntaxEnum Enumeration
    public enum ShortSyntaxEnum {
        Element11,
        Element22
    }

    public static class ShortSyntaxEnumHelpers {
        public static ShortSyntaxEnum From(string value) {
            switch (value) {
                case "Element11": return ShortSyntaxEnum.Element11;
                case "Element22": return ShortSyntaxEnum.Element22;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(ShortSyntaxEnum), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly ShortSyntaxEnum[] all = new ShortSyntaxEnum[] {
            ShortSyntaxEnum.Element11,
            ShortSyntaxEnum.Element22
        };

        public static ShortSyntaxEnum[] GetAll() {
            return ShortSyntaxEnumHelpers.all;
        }

        // Extensions

        public static string ToString(this ShortSyntaxEnum e) {
            return Enum.GetName(typeof(ShortSyntaxEnum), e);
        }
    }

    public class ShortSyntaxEnum_JsonNetConverter: JsonNetConverter<ShortSyntaxEnum> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ShortSyntaxEnum_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ShortSyntaxEnum value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ShortSyntaxEnum ReadJson(JsonReader reader, System.Type objectType, ShortSyntaxEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return ShortSyntaxEnumHelpers.From((string)reader.Value);
        }
    }
}