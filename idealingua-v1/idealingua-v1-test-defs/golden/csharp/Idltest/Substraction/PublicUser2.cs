// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Substraction {
    [JsonConverter(typeof(PublicUser2_JsonNetConverter))]
    public class PublicUser2 {
        public static readonly string RTTI_PACKAGE = "idltest.substraction";
        public static readonly string RTTI_CLASSNAME = "PublicUser2";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.substraction.PublicUser2";
        public string GetPackageName() { return PublicUser2.RTTI_PACKAGE; }
        public string GetClassName() { return PublicUser2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PublicUser2.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }

        public PublicUser2() {
        }

        public PublicUser2(string name) {
            this.Name = name;
        }

    }
    public class PublicUser2_JsonNetConverter: JsonNetConverter<PublicUser2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PublicUser2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PublicUser2 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override PublicUser2 ReadJson(JsonReader reader, System.Type objectType, PublicUser2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PublicUser2(
                json["name"].Value<string>()
            );
        }
    }
}