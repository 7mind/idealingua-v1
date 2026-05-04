// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Identifiers {
    [JsonConverter(typeof(KVIDGeneric_JsonNetConverter))]
    public class KVIDGeneric {
        public static readonly string RTTI_PACKAGE = "idltest.identifiers";
        public static readonly string RTTI_CLASSNAME = "KVIDGeneric";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.identifiers.KVIDGeneric";
        public string GetPackageName() { return KVIDGeneric.RTTI_PACKAGE; }
        public string GetClassName() { return KVIDGeneric.RTTI_CLASSNAME; }
        public string GetFullClassName() { return KVIDGeneric.RTTI_FULLCLASSNAME; }

        public Dictionary<string, Idltest.Identifiers.BucketID> Test { get; set; }

        public KVIDGeneric() {
            Test = new Dictionary<string, Idltest.Identifiers.BucketID>();
        }

        public KVIDGeneric(Dictionary<string, Idltest.Identifiers.BucketID> test) {
            this.Test = test;
        }

    }
    public class KVIDGeneric_JsonNetConverter: JsonNetConverter<KVIDGeneric> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public KVIDGeneric_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, KVIDGeneric v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("test");
            writer.WriteStartObject();
            foreach(var mkv in v.Test) {
                writer.WritePropertyName(mkv.Key.ToString());
                writer.WriteValue(mkv.Value.ToString());
            }
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override KVIDGeneric ReadJson(JsonReader reader, System.Type objectType, KVIDGeneric existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _test = new Dictionary<string, Idltest.Identifiers.BucketID>();
            foreach (var _test_kv in ((JObject)json["test"]).Properties()) {
                Idltest.Identifiers.BucketID _test_dv;
                _test_dv = Idltest.Identifiers.BucketID.From(_test_kv.Value.Value<string>());
                _test.Add(_test_kv.Name, _test_dv);
            }

            return new KVIDGeneric(
                _test
            );
        }
    }
}