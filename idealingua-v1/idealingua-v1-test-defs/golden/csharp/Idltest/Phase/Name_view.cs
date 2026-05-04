// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Phase {
    [JsonConverter(typeof(Name_view_JsonNetConverter))]
    public class Name_view : Name {
        public static readonly string RTTI_PACKAGE = "idltest.phase";
        public static readonly string RTTI_CLASSNAME = "Name_view";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.phase.Name_view";
        public string GetPackageName() { return Name_view.RTTI_PACKAGE; }
        public string GetClassName() { return Name_view.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Name_view.RTTI_FULLCLASSNAME; }

        public long Bytes { get; set; }
        public string Name { get; set; }
        public List<Idltest.Phase.Name> Relatives { get; set; }

        public Name_view() {
            Relatives = new List<Idltest.Phase.Name>();
        }

        public Name_view(long bytes, string name, List<Idltest.Phase.Name> relatives) {
            this.Bytes = bytes;
            this.Name = name;
            this.Relatives = relatives;
        }

        public Name ToName() {
            var res = new NameStruct();
            res.Name = this.Name;
            return res;
        }

        public void LoadName(Name value) {
            this.Name = value.Name;
        }

    }
    public class Name_view_JsonNetConverter: JsonNetConverter<Name_view> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Name_view_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Name_view v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("bytes");
            writer.WriteValue(v.Bytes);
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("relatives");
            writer.WriteStartArray();
            foreach (var lv in v.Relatives) {
                // Serializing polymorphic type Name
                writer.WriteStartObject();
                writer.WritePropertyName(lv.GetFullClassName());
                serializer.Serialize(writer, lv);
                writer.WriteEndObject();

            }
            writer.WriteEndArray();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Name_view ReadJson(JsonReader reader, System.Type objectType, Name_view existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _relatives = new List<Idltest.Phase.Name>();
            foreach (var _relatives_sv in (JArray)json["relatives"]) {
                Idltest.Phase.Name _relatives_d;
                _relatives_d = serializer.Deserialize<Idltest.Phase.Name>(_relatives_sv.CreateReader());
                _relatives.Add(_relatives_d);
            }

            return new Name_view(
                json["bytes"].Value<long>(), 
                json["name"].Value<string>(), 
                _relatives
            );
        }
    }
}