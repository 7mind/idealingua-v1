// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(NestedClass_JsonNetConverter))]
    public class NestedClass {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "NestedClass";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.NestedClass";
        public string GetPackageName() { return NestedClass.RTTI_PACKAGE; }
        public string GetClassName() { return NestedClass.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NestedClass.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.NestedClass C { get; set; }

        public NestedClass() {
        }

        public NestedClass(Izumi.Test.Domain01.NestedClass c) {
            this.C = c;
        }

    }
    public class NestedClass_JsonNetConverter: JsonNetConverter<NestedClass> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NestedClass_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NestedClass v, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (v.C != null) {
                writer.WritePropertyName("c");
                serializer.Serialize(writer, v.C);
            }

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NestedClass ReadJson(JsonReader reader, System.Type objectType, NestedClass existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            Izumi.Test.Domain01.NestedClass _c = null;
            var _cRaw = json["c"];
            if (_cRaw != null && _cRaw.Type != JTokenType.Null) {
                _c = serializer.Deserialize<Izumi.Test.Domain01.NestedClass>(_cRaw.CreateReader());
            }

            return new NestedClass(
                _c
            );
        }
    }
}