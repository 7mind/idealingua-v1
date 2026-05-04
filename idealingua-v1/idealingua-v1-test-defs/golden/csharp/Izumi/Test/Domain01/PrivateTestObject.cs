// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(PrivateTestObject_JsonNetConverter))]
    public class PrivateTestObject : ExtendedMixin {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "PrivateTestObject";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.PrivateTestObject";
        public string GetPackageName() { return PrivateTestObject.RTTI_PACKAGE; }
        public string GetClassName() { return PrivateTestObject.RTTI_CLASSNAME; }
        public string GetFullClassName() { return PrivateTestObject.RTTI_FULLCLASSNAME; }

        public string Parent_embedded { get; set; }
        public string Parent { get; set; }
        public bool Embedded { get; set; }
        public sbyte Own { get; set; }

        public PrivateTestObject() {
        }

        public PrivateTestObject(string parent_embedded, string parent, bool embedded, sbyte own) {
            this.Parent_embedded = parent_embedded;
            this.Parent = parent;
            this.Embedded = embedded;
            this.Own = own;
        }

        public ExtendedMixin ToExtendedMixin() {
            var res = new ExtendedMixinStruct();
            res.Parent_embedded = this.Parent_embedded;
            res.Parent = this.Parent;
            res.Embedded = this.Embedded;
            res.Own = this.Own;
            return res;
        }

        public void LoadExtendedMixin(ExtendedMixin value) {
            this.Parent_embedded = value.Parent_embedded;
            this.Parent = value.Parent;
            this.Embedded = value.Embedded;
            this.Own = value.Own;
        }

    }
    public class PrivateTestObject_JsonNetConverter: JsonNetConverter<PrivateTestObject> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public PrivateTestObject_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, PrivateTestObject v, JsonSerializer serializer) {
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
        public override PrivateTestObject ReadJson(JsonReader reader, System.Type objectType, PrivateTestObject existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new PrivateTestObject(
                json["parent_embedded"].Value<string>(), 
                json["parent"].Value<string>(), 
                json["embedded"].Value<bool>(), 
                json["own"].Value<sbyte>()
            );
        }
    }
}