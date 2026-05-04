// Auto-generated, any modifications may be overwritten in the future.

using System;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Idltest.Services {
    // Environment Enumeration
    public enum Environment {
        Dev,
        Prod
    }

    public static class EnvironmentHelpers {
        public static Environment From(string value) {
            switch (value) {
                case "Dev": return Environment.Dev;
                case "Prod": return Environment.Prod;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }

        public static bool IsValid(string value) {
            return Enum.IsDefined(typeof(Environment), value);
        }

        // The elements in the array are still changeable, please use with care.
        private static readonly Environment[] all = new Environment[] {
            Environment.Dev,
            Environment.Prod
        };

        public static Environment[] GetAll() {
            return EnvironmentHelpers.all;
        }

        // Extensions

        public static string ToString(this Environment e) {
            return Enum.GetName(typeof(Environment), e);
        }
    }

    public class Environment_JsonNetConverter: JsonNetConverter<Environment> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Environment_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Environment value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Environment ReadJson(JsonReader reader, System.Type objectType, Environment existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return EnvironmentHelpers.From((string)reader.Value);
        }
    }
}