// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(GenericFailure_JsonNetConverter))]
    public class GenericFailure {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "GenericFailure";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.GenericFailure";
        public string GetPackageName() { return GenericFailure.RTTI_PACKAGE; }
        public string GetClassName() { return GenericFailure.RTTI_CLASSNAME; }
        public string GetFullClassName() { return GenericFailure.RTTI_FULLCLASSNAME; }

        public string Message { get; set; }
        public string Diagnostics { get; set; }
        public Dictionary<string, string> Reserved { get; set; }
        public Izumi.Test.Domain01.GenericFailureCode Code { get; set; }

        public GenericFailure() {
            Reserved = new Dictionary<string, string>();
        }

        public GenericFailure(string message, string diagnostics, Dictionary<string, string> reserved, Izumi.Test.Domain01.GenericFailureCode code) {
            this.Message = message;
            this.Diagnostics = diagnostics;
            this.Reserved = reserved;
            this.Code = code;
        }

    }
    public class GenericFailure_JsonNetConverter: JsonNetConverter<GenericFailure> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public GenericFailure_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, GenericFailure v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("message");
            writer.WriteValue(v.Message);
            if (v.Diagnostics != null) {
                writer.WritePropertyName("diagnostics");
                writer.WriteValue(v.Diagnostics);
            }

            writer.WritePropertyName("reserved");
            writer.WriteStartObject();
            foreach(var mkv in v.Reserved) {
                writer.WritePropertyName(mkv.Key.ToString());
                writer.WriteValue(mkv.Value);
            }
            writer.WriteEndObject();

            writer.WritePropertyName("code");
            writer.WriteValue(v.Code.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override GenericFailure ReadJson(JsonReader reader, System.Type objectType, GenericFailure existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            string _diagnostics = null;
            var _diagnosticsRaw = json["diagnostics"];
            if (_diagnosticsRaw != null && _diagnosticsRaw.Type != JTokenType.Null) {
                _diagnostics = _diagnosticsRaw.Value<string>();
            }

            var _reserved = new Dictionary<string, string>();
            foreach (var _reserved_kv in ((JObject)json["reserved"]).Properties()) {
                string _reserved_dv;
                _reserved_dv = _reserved_kv.Value.Value<string>();
                _reserved.Add(_reserved_kv.Name, _reserved_dv);
            }

            return new GenericFailure(
                json["message"].Value<string>(), 
                _diagnostics, 
                _reserved, 
                Izumi.Test.Domain01.GenericFailureCodeHelpers.From(json["code"].Value<string>())
            );
        }
    }
}