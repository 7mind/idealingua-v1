// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(IDForDomain2_JsonNetConverter))]
    public class IDForDomain2 {
        private static char[] idSplitter = new char[]{':'};

        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "IDForDomain2";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.IDForDomain2";
        public string GetPackageName() { return IDForDomain2.RTTI_PACKAGE; }
        public string GetClassName() { return IDForDomain2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return IDForDomain2.RTTI_FULLCLASSNAME; }

        public int A { get; set; }

        public IDForDomain2() {
        }

        public IDForDomain2(int a) {
            this.A = a;
        }

        public override string ToString() {
            var suffix = A.ToString();
            return "IDForDomain2#" + suffix;
        }

        public static IDForDomain2 From(string value) {
            if (value == null) {
                throw new ArgumentNullException("value");
            }

            if (!value.StartsWith("IDForDomain2#", StringComparison.Ordinal)) {
                throw new ArgumentException(string.Format("Expected identifier for type IDForDomain2, got {0}", value));
            }

            var parts = value.Substring(13, value.Length - 13).Split(idSplitter, StringSplitOptions.None);
            if (parts.Length != 1) {
                throw new ArgumentException(string.Format("Expected identifier for type IDForDomain2 with 1 parts, got {0} in string {1}", parts.Length, value));
            }

            var res = new IDForDomain2();
            res.A = int.Parse(parts[0]);
            return res;
        }
    }

    public class IDForDomain2_JsonNetConverter: JsonNetConverter<IDForDomain2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public IDForDomain2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, IDForDomain2 value, JsonSerializer serializer) {
            writer.WriteValue(value.ToString());
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override IDForDomain2 ReadJson(JsonReader reader, System.Type objectType, IDForDomain2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            return IDForDomain2.From((string)reader.Value);
        }
    }
}