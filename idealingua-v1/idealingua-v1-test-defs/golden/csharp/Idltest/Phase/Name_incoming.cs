// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Phase {
    [JsonConverter(typeof(Name_incoming_JsonNetConverter))]
    public class Name_incoming : Name {
        public static readonly string RTTI_PACKAGE = "idltest.phase";
        public static readonly string RTTI_CLASSNAME = "Name_incoming";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.phase.Name_incoming";
        public string GetPackageName() { return Name_incoming.RTTI_PACKAGE; }
        public string GetClassName() { return Name_incoming.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Name_incoming.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }

        public Name_incoming() {
        }

        public Name_incoming(string name) {
            this.Name = name;
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
    public class Name_incoming_JsonNetConverter: JsonNetConverter<Name_incoming> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Name_incoming_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Name_incoming v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Name_incoming ReadJson(JsonReader reader, System.Type objectType, Name_incoming existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Name_incoming(
                json["name"].Value<string>()
            );
        }
    }
}