// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Dtofields {
    [JsonConverter(typeof(OptionalObj_JsonNetConverter))]
    public class OptionalObj {
        public static readonly string RTTI_PACKAGE = "idltest.dtofields";
        public static readonly string RTTI_CLASSNAME = "OptionalObj";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.dtofields.OptionalObj";
        public string GetPackageName() { return OptionalObj.RTTI_PACKAGE; }
        public string GetClassName() { return OptionalObj.RTTI_CLASSNAME; }
        public string GetFullClassName() { return OptionalObj.RTTI_FULLCLASSNAME; }

        public Idltest.Dtofields.NullableObj No { get; set; }

        public OptionalObj() {
        }

        public OptionalObj(Idltest.Dtofields.NullableObj no) {
            this.No = no;
        }

    }
    public class OptionalObj_JsonNetConverter: JsonNetConverter<OptionalObj> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public OptionalObj_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, OptionalObj v, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (v.No != null) {
                writer.WritePropertyName("no");
                serializer.Serialize(writer, v.No);
            }

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override OptionalObj ReadJson(JsonReader reader, System.Type objectType, OptionalObj existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            Idltest.Dtofields.NullableObj _no = null;
            var _noRaw = json["no"];
            if (_noRaw != null && _noRaw.Type != JTokenType.Null) {
                _no = serializer.Deserialize<Idltest.Dtofields.NullableObj>(_noRaw.CreateReader());
            }

            return new OptionalObj(
                _no
            );
        }
    }
}