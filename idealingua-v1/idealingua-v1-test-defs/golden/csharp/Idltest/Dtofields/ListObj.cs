// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Dtofields {
    [JsonConverter(typeof(ListObj_JsonNetConverter))]
    public class ListObj {
        public static readonly string RTTI_PACKAGE = "idltest.dtofields";
        public static readonly string RTTI_CLASSNAME = "ListObj";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.dtofields.ListObj";
        public string GetPackageName() { return ListObj.RTTI_PACKAGE; }
        public string GetClassName() { return ListObj.RTTI_CLASSNAME; }
        public string GetFullClassName() { return ListObj.RTTI_FULLCLASSNAME; }

        public List<Idltest.Dtofields.NullableObj> All { get; set; }

        public ListObj() {
            All = new List<Idltest.Dtofields.NullableObj>();
        }

        public ListObj(List<Idltest.Dtofields.NullableObj> all) {
            this.All = all;
        }

    }
    public class ListObj_JsonNetConverter: JsonNetConverter<ListObj> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ListObj_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ListObj v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("all");
            writer.WriteStartArray();
            foreach (var lv in v.All) {
                serializer.Serialize(writer, lv);
            }
            writer.WriteEndArray();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ListObj ReadJson(JsonReader reader, System.Type objectType, ListObj existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _all = new List<Idltest.Dtofields.NullableObj>();
            foreach (var _all_sv in (JArray)json["all"]) {
                Idltest.Dtofields.NullableObj _all_d;
                _all_d = serializer.Deserialize<Idltest.Dtofields.NullableObj>(_all_sv.CreateReader());
                _all.Add(_all_d);
            }

            return new ListObj(
                _all
            );
        }
    }
}