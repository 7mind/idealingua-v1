// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Clashing.Another {
    // SomeEnum Enumeration
    public enum SomeEnum {
        VALUE
    }

    public static class SomeEnumHelpers {
        public static SomeEnum From(string value) {
            switch (value) {
                case "VALUE": return SomeEnum.VALUE;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(SomeEnum), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly SomeEnum[] all = new SomeEnum[] {
            SomeEnum.VALUE
        };

        public static SomeEnum[] GetAll() {
            return SomeEnumHelpers.all;
        }

        // Extensions

        public static string ToString(this SomeEnum e) {
            return Enum.GetName(typeof(SomeEnum), e);
        }
    }

    public class SomeEnum_JsonNetConverter: JsonNetConverter<SomeEnum> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SomeEnum_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SomeEnum value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SomeEnum ReadJson(JsonReader reader, System.Type objectType, SomeEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return SomeEnumHelpers.From((string)reader.Value);
        }
    }
}