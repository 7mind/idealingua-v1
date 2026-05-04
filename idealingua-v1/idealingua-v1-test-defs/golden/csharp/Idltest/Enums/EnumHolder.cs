// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Enums {
    [JsonConverter(typeof(EnumHolder_JsonNetConverter))]
    public class EnumHolder {
        public static readonly string RTTI_PACKAGE = "idltest.enums";
        public static readonly string RTTI_CLASSNAME = "EnumHolder";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.enums.EnumHolder";
        public string GetPackageName() { return EnumHolder.RTTI_PACKAGE; }
        public string GetClassName() { return EnumHolder.RTTI_CLASSNAME; }
        public string GetFullClassName() { return EnumHolder.RTTI_FULLCLASSNAME; }

        public Idltest.Enums.TestEnum En { get; set; }

        public EnumHolder() {
        }

        public EnumHolder(Idltest.Enums.TestEnum en) {
            this.En = en;
        }

    }
    public class EnumHolder_JsonNetConverter: JsonNetConverter<EnumHolder> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public EnumHolder_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, EnumHolder v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("en");
            writer.WriteValue(v.En.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override EnumHolder ReadJson(JsonReader reader, System.Type objectType, EnumHolder existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new EnumHolder(
                Idltest.Enums.TestEnumHelpers.From(json["en"].Value<string>())
            );
        }
    }
}