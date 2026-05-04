// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    // AnEnum Enumeration
    public enum AnEnum {
        VALUE1,
        VALUE2
    }

    public static class AnEnumHelpers {
        public static AnEnum From(string value) {
            switch (value) {
                case "VALUE1": return AnEnum.VALUE1;
                case "VALUE2": return AnEnum.VALUE2;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(AnEnum), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly AnEnum[] all = new AnEnum[] {
            AnEnum.VALUE1,
            AnEnum.VALUE2
        };

        public static AnEnum[] GetAll() {
            return AnEnumHelpers.all;
        }

        // Extensions

        public static string ToString(this AnEnum e) {
            return Enum.GetName(typeof(AnEnum), e);
        }
    }

    public class AnEnum_JsonNetConverter: JsonNetConverter<AnEnum> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AnEnum_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AnEnum value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AnEnum ReadJson(JsonReader reader, System.Type objectType, AnEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return AnEnumHelpers.From((string)reader.Value);
        }
    }
}