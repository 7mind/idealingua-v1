// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Phase {
    [JsonConverter(typeof(Name_stored_JsonNetConverter))]
    public class Name_stored : Name_stored_ {
        public static readonly string RTTI_PACKAGE = "idltest.phase";
        public static readonly string RTTI_CLASSNAME = "Name_stored";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.phase.Name_stored";
        public string GetPackageName() { return Name_stored.RTTI_PACKAGE; }
        public string GetClassName() { return Name_stored.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Name_stored.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }
        public long Bytes { get; set; }

        public Name_stored() {
        }

        public Name_stored(string name, long bytes) {
            this.Name = name;
            this.Bytes = bytes;
        }

        public Name_stored_ ToName_stored_() {
            var res = new Name_stored_Struct();
            res.Name = this.Name;
            res.Bytes = this.Bytes;
            return res;
        }

        public void LoadName_stored_(Name_stored_ value) {
            this.Name = value.Name;
            this.Bytes = value.Bytes;
        }

    }
    public class Name_stored_JsonNetConverter: JsonNetConverter<Name_stored> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Name_stored_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Name_stored v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("bytes");
            writer.WriteValue(v.Bytes);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Name_stored ReadJson(JsonReader reader, System.Type objectType, Name_stored existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Name_stored(
                json["name"].Value<string>(), 
                json["bytes"].Value<long>()
            );
        }
    }
}