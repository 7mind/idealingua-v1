// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Identifiers {
    // DepartmentEnum Enumeration
    public enum DepartmentEnum {
        Engineering,
        Sales
    }

    public static class DepartmentEnumHelpers {
        public static DepartmentEnum From(string value) {
            switch (value) {
                case "Engineering": return DepartmentEnum.Engineering;
                case "Sales": return DepartmentEnum.Sales;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(DepartmentEnum), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly DepartmentEnum[] all = new DepartmentEnum[] {
            DepartmentEnum.Engineering,
            DepartmentEnum.Sales
        };

        public static DepartmentEnum[] GetAll() {
            return DepartmentEnumHelpers.all;
        }

        // Extensions

        public static string ToString(this DepartmentEnum e) {
            return Enum.GetName(typeof(DepartmentEnum), e);
        }
    }

    public class DepartmentEnum_JsonNetConverter: JsonNetConverter<DepartmentEnum> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public DepartmentEnum_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, DepartmentEnum value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override DepartmentEnum ReadJson(JsonReader reader, System.Type objectType, DepartmentEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return DepartmentEnumHelpers.From((string)reader.Value);
        }
    }
}