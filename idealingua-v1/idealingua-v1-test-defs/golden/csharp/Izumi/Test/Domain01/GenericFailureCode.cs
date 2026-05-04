// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    // GenericFailureCode Enumeration
    public enum GenericFailureCode {
        EntityNotFound,
        EntityAlreadyExists,
        ExpirationFailure,
        ConditionNotMet,
        AccessDenied,
        AssertionFailed,
        UnexpectedException,
        CodecFailed,
        Unknown
    }

    public static class GenericFailureCodeHelpers {
        public static GenericFailureCode From(string value) {
            switch (value) {
                case "EntityNotFound": return GenericFailureCode.EntityNotFound;
                case "EntityAlreadyExists": return GenericFailureCode.EntityAlreadyExists;
                case "ExpirationFailure": return GenericFailureCode.ExpirationFailure;
                case "ConditionNotMet": return GenericFailureCode.ConditionNotMet;
                case "AccessDenied": return GenericFailureCode.AccessDenied;
                case "AssertionFailed": return GenericFailureCode.AssertionFailed;
                case "UnexpectedException": return GenericFailureCode.UnexpectedException;
                case "CodecFailed": return GenericFailureCode.CodecFailed;
                case "Unknown": return GenericFailureCode.Unknown;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(GenericFailureCode), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly GenericFailureCode[] all = new GenericFailureCode[] {
            GenericFailureCode.EntityNotFound,
            GenericFailureCode.EntityAlreadyExists,
            GenericFailureCode.ExpirationFailure,
            GenericFailureCode.ConditionNotMet,
            GenericFailureCode.AccessDenied,
            GenericFailureCode.AssertionFailed,
            GenericFailureCode.UnexpectedException,
            GenericFailureCode.CodecFailed,
            GenericFailureCode.Unknown
        };

        public static GenericFailureCode[] GetAll() {
            return GenericFailureCodeHelpers.all;
        }

        // Extensions

        public static string ToString(this GenericFailureCode e) {
            return Enum.GetName(typeof(GenericFailureCode), e);
        }
    }

    public class GenericFailureCode_JsonNetConverter: JsonNetConverter<GenericFailureCode> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GenericFailureCode_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GenericFailureCode value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GenericFailureCode ReadJson(JsonReader reader, System.Type objectType, GenericFailureCode existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return GenericFailureCodeHelpers.From((string)reader.Value);
        }
    }
}