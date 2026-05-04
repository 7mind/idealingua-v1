// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    [JsonConverter(typeof(Type_JsonNetConverter))]
    public class Type {
        public static readonly string RTTI_PACKAGE = "idltest.ast";
        public static readonly string RTTI_CLASSNAME = "Type";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.ast.Type";
        public string GetPackageName() { return Type.RTTI_PACKAGE; }
        public string GetClassName() { return Type.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Type.RTTI_FULLCLASSNAME; }

        public string Label { get; set; }

        public Type() {
        }

        public Type(string label) {
            this.Label = label;
        }

    }
    public class Type_JsonNetConverter: JsonNetConverter<Type> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Type_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Type v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("label");
            writer.WriteValue(v.Label);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Type ReadJson(JsonReader reader, System.Type objectType, Type existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Type(
                json["label"].Value<string>()
            );
        }
    }
}