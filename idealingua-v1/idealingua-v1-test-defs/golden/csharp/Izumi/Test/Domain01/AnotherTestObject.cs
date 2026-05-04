// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(AnotherTestObject_JsonNetConverter))]
    public class AnotherTestObject {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "AnotherTestObject";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.AnotherTestObject";
        public string GetPackageName() { return AnotherTestObject.RTTI_PACKAGE; }
        public string GetClassName() { return AnotherTestObject.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AnotherTestObject.RTTI_FULLCLASSNAME; }

        public string Parent_embedded { get; set; }
        public string Parent { get; set; }
        public bool Embedded { get; set; }
        public sbyte Own { get; set; }

        public AnotherTestObject() {
        }

        public AnotherTestObject(string parent_embedded, string parent, bool embedded, sbyte own) {
            this.Parent_embedded = parent_embedded;
            this.Parent = parent;
            this.Embedded = embedded;
            this.Own = own;
        }

    }
    public class AnotherTestObject_JsonNetConverter: JsonNetConverter<AnotherTestObject> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AnotherTestObject_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AnotherTestObject v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("parent_embedded");
            writer.WriteValue(v.Parent_embedded);
            writer.WritePropertyName("parent");
            writer.WriteValue(v.Parent);
            writer.WritePropertyName("embedded");
            writer.WriteValue(v.Embedded);
            writer.WritePropertyName("own");
            writer.WriteValue(v.Own);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AnotherTestObject ReadJson(JsonReader reader, System.Type objectType, AnotherTestObject existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new AnotherTestObject(
                json["parent_embedded"].Value<string>(), 
                json["parent"].Value<string>(), 
                json["embedded"].Value<bool>(), 
                json["own"].Value<sbyte>()
            );
        }
    }
}