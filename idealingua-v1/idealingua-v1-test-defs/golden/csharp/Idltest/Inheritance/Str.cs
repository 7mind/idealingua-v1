// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Inheritance {
    [JsonConverter(typeof(Str_JsonNetConverter))]
    public class Str : Empty {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance";
        public static readonly string RTTI_CLASSNAME = "Str";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.Str";
        public string GetPackageName() { return Str.RTTI_PACKAGE; }
        public string GetClassName() { return Str.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Str.RTTI_FULLCLASSNAME; }

        public string @Str_ { get; set; }

        public Str() {
        }

        public Str(string str) {
            this.@Str_ = str;
        }

        public Empty ToEmpty() {
            var res = new EmptyStruct();

            return res;
        }

        public void LoadEmpty(Empty value) {
        }

    }
    public class Str_JsonNetConverter: JsonNetConverter<Str> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Str_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Str v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("str");
            writer.WriteValue(v.@Str_);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Str ReadJson(JsonReader reader, System.Type objectType, Str existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Str(
                json["str"].Value<string>()
            );
        }
    }
}