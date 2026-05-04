// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Events {
    [JsonConverter(typeof(BranchA_JsonNetConverter))]
    public class BranchA {
        public static readonly string RTTI_PACKAGE = "idltest.events";
        public static readonly string RTTI_CLASSNAME = "BranchA";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.events.BranchA";
        public string GetPackageName() { return BranchA.RTTI_PACKAGE; }
        public string GetClassName() { return BranchA.RTTI_CLASSNAME; }
        public string GetFullClassName() { return BranchA.RTTI_FULLCLASSNAME; }

        public string A { get; set; }

        public BranchA() {
        }

        public BranchA(string a) {
            this.A = a;
        }

    }
    public class BranchA_JsonNetConverter: JsonNetConverter<BranchA> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public BranchA_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, BranchA v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("a");
            writer.WriteValue(v.A);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override BranchA ReadJson(JsonReader reader, System.Type objectType, BranchA existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new BranchA(
                json["a"].Value<string>()
            );
        }
    }
}