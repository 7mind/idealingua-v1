// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Json {
    [JsonConverter(typeof(JLObject_JsonNetConverter))]
    public class JLObject {
        public static readonly string RTTI_PACKAGE = "idltest.json";
        public static readonly string RTTI_CLASSNAME = "JLObject";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.json.JLObject";
        public string GetPackageName() { return JLObject.RTTI_PACKAGE; }
        public string GetClassName() { return JLObject.RTTI_CLASSNAME; }
        public string GetFullClassName() { return JLObject.RTTI_FULLCLASSNAME; }

        public Dictionary<string, Idltest.Json.JSONLike> Fields { get; set; }

        public JLObject() {
            Fields = new Dictionary<string, Idltest.Json.JSONLike>();
        }

        public JLObject(Dictionary<string, Idltest.Json.JSONLike> fields) {
            this.Fields = fields;
        }

    }
    public class JLObject_JsonNetConverter: JsonNetConverter<JLObject> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public JLObject_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, JLObject v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("fields");
            writer.WriteStartObject();
            foreach(var mkv in v.Fields) {
                writer.WritePropertyName(mkv.Key.ToString());
                serializer.Serialize(writer, mkv.Value);
            }
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override JLObject ReadJson(JsonReader reader, System.Type objectType, JLObject existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _fields = new Dictionary<string, Idltest.Json.JSONLike>();
            foreach (var _fields_kv in ((JObject)json["fields"]).Properties()) {
                Idltest.Json.JSONLike _fields_dv;
                _fields_dv = serializer.Deserialize<Idltest.Json.JSONLike>(_fields_kv.Value.CreateReader());
                _fields.Add(_fields_kv.Name, _fields_dv);
            }

            return new JLObject(
                _fields
            );
        }
    }
}