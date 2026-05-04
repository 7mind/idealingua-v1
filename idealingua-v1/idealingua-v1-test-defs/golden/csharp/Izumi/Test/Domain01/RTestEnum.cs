// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    // RTestEnum Enumeration
    public enum RTestEnum {
        A
    }

    public static class RTestEnumHelpers {
        public static RTestEnum From(string value) {
            switch (value) {
                case "A": return RTestEnum.A;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(RTestEnum), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly RTestEnum[] all = new RTestEnum[] {
            RTestEnum.A
        };

        public static RTestEnum[] GetAll() {
            return RTestEnumHelpers.all;
        }

        // Extensions

        public static string ToString(this RTestEnum e) {
            return Enum.GetName(typeof(RTestEnum), e);
        }
    }

    public class RTestEnum_JsonNetConverter: JsonNetConverter<RTestEnum> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public RTestEnum_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, RTestEnum value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override RTestEnum ReadJson(JsonReader reader, System.Type objectType, RTestEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return RTestEnumHelpers.From((string)reader.Value);
        }
    }
}