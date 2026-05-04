// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Json {
    [JsonConverter(typeof(JLArray_JsonNetConverter))]
    public class JLArray {
        public static readonly string RTTI_PACKAGE = "idltest.json";
        public static readonly string RTTI_CLASSNAME = "JLArray";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.json.JLArray";
        public string GetPackageName() { return JLArray.RTTI_PACKAGE; }
        public string GetClassName() { return JLArray.RTTI_CLASSNAME; }
        public string GetFullClassName() { return JLArray.RTTI_FULLCLASSNAME; }

        public List<Idltest.Json.JSONLike> Values { get; set; }

        public JLArray() {
            Values = new List<Idltest.Json.JSONLike>();
        }

        public JLArray(List<Idltest.Json.JSONLike> values) {
            this.Values = values;
        }

    }
    public class JLArray_JsonNetConverter: JsonNetConverter<JLArray> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public JLArray_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, JLArray v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("values");
            writer.WriteStartArray();
            foreach (var lv in v.Values) {
                serializer.Serialize(writer, lv);
            }
            writer.WriteEndArray();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override JLArray ReadJson(JsonReader reader, System.Type objectType, JLArray existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _values = new List<Idltest.Json.JSONLike>();
            foreach (var _values_sv in (JArray)json["values"]) {
                Idltest.Json.JSONLike _values_d;
                _values_d = serializer.Deserialize<Idltest.Json.JSONLike>(_values_sv.CreateReader());
                _values.Add(_values_d);
            }

            return new JLArray(
                _values
            );
        }
    }
}