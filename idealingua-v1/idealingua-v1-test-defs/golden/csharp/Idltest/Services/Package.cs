// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Services {
    [JsonConverter(typeof(Package_JsonNetConverter))]
    public class Package {
        public static readonly string RTTI_PACKAGE = "idltest.services";
        public static readonly string RTTI_CLASSNAME = "Package";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.services.Package";
        public string GetPackageName() { return Package.RTTI_PACKAGE; }
        public string GetClassName() { return Package.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Package.RTTI_FULLCLASSNAME; }

        public string Name { get; set; }

        public Package() {
        }

        public Package(string name) {
            this.Name = name;
        }

    }
    public class Package_JsonNetConverter: JsonNetConverter<Package> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Package_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Package v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Package ReadJson(JsonReader reader, System.Type objectType, Package existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Package(
                json["name"].Value<string>()
            );
        }
    }
}