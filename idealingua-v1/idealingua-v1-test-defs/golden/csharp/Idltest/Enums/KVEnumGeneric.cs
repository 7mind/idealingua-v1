// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Enums {
    [JsonConverter(typeof(KVEnumGeneric_JsonNetConverter))]
    public class KVEnumGeneric {
        public static readonly string RTTI_PACKAGE = "idltest.enums";
        public static readonly string RTTI_CLASSNAME = "KVEnumGeneric";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.enums.KVEnumGeneric";
        public string GetPackageName() { return KVEnumGeneric.RTTI_PACKAGE; }
        public string GetClassName() { return KVEnumGeneric.RTTI_CLASSNAME; }
        public string GetFullClassName() { return KVEnumGeneric.RTTI_FULLCLASSNAME; }

        public Dictionary<string, Idltest.Enums.TestEnum> Test { get; set; }

        public KVEnumGeneric() {
            Test = new Dictionary<string, Idltest.Enums.TestEnum>();
        }

        public KVEnumGeneric(Dictionary<string, Idltest.Enums.TestEnum> test) {
            this.Test = test;
        }

    }
    public class KVEnumGeneric_JsonNetConverter: JsonNetConverter<KVEnumGeneric> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public KVEnumGeneric_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, KVEnumGeneric v, JsonSerializer serializer) {
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
        public override KVEnumGeneric ReadJson(JsonReader reader, System.Type objectType, KVEnumGeneric existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _test = new Dictionary<string, Idltest.Enums.TestEnum>();
            foreach (var _test_kv in ((JObject)json["test"]).Properties()) {
                Idltest.Enums.TestEnum _test_dv;
                _test_dv = Idltest.Enums.TestEnumHelpers.From(_test_kv.Value.Value<string>());
                _test.Add(_test_kv.Name, _test_dv);
            }

            return new KVEnumGeneric(
                _test
            );
        }
    }
}