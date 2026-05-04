// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Events {
    [JsonConverter(typeof(BranchB_JsonNetConverter))]
    public class BranchB {
        public static readonly string RTTI_PACKAGE = "idltest.events";
        public static readonly string RTTI_CLASSNAME = "BranchB";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.events.BranchB";
        public string GetPackageName() { return BranchB.RTTI_PACKAGE; }
        public string GetClassName() { return BranchB.RTTI_CLASSNAME; }
        public string GetFullClassName() { return BranchB.RTTI_FULLCLASSNAME; }

        public string B { get; set; }

        public BranchB() {
        }

        public BranchB(string b) {
            this.B = b;
        }

    }
    public class BranchB_JsonNetConverter: JsonNetConverter<BranchB> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public BranchB_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, BranchB v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("b");
            writer.WriteValue(v.B);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override BranchB ReadJson(JsonReader reader, System.Type objectType, BranchB existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new BranchB(
                json["b"].Value<string>()
            );
        }
    }
}