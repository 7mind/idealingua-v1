// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Events {
    // EnumType Enumeration
    public enum EnumType {
        EnumA,
        EnumB
    }

    public static class EnumTypeHelpers {
        public static EnumType From(string value) {
            switch (value) {
                case "EnumA": return EnumType.EnumA;
                case "EnumB": return EnumType.EnumB;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(EnumType), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly EnumType[] all = new EnumType[] {
            EnumType.EnumA,
            EnumType.EnumB
        };

        public static EnumType[] GetAll() {
            return EnumTypeHelpers.all;
        }

        // Extensions

        public static string ToString(this EnumType e) {
            return Enum.GetName(typeof(EnumType), e);
        }
    }

    public class EnumType_JsonNetConverter: JsonNetConverter<EnumType> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public EnumType_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, EnumType value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override EnumType ReadJson(JsonReader reader, System.Type objectType, EnumType existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return EnumTypeHelpers.From((string)reader.Value);
        }
    }
}