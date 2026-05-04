// Auto-generated, any modifications may be overwritten in the future.

using Izumi.Test.Domain01;
using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(AliasedUsageData_JsonNetConverter))]
    public class AliasedUsageData {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02";
        public static readonly string RTTI_CLASSNAME = "AliasedUsageData";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.AliasedUsageData";
        public string GetPackageName() { return AliasedUsageData.RTTI_PACKAGE; }
        public string GetClassName() { return AliasedUsageData.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AliasedUsageData.RTTI_FULLCLASSNAME; }

        public Izumi.Test.Domain01.TestObject TestObj { get; set; }
        public Izumi.Test.Domain01.GoAliasEnumTest EnumField { get; set; }

        public AliasedUsageData() {
        }

        public AliasedUsageData(Izumi.Test.Domain01.TestObject testObj, Izumi.Test.Domain01.GoAliasEnumTest enumField) {
            this.TestObj = testObj;
            this.EnumField = enumField;
        }

    }
    public class AliasedUsageData_JsonNetConverter: JsonNetConverter<AliasedUsageData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AliasedUsageData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AliasedUsageData v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("testObj");
            serializer.Serialize(writer, v.TestObj);
            writer.WritePropertyName("enumField");
            writer.WriteValue(v.EnumField.ToString());
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AliasedUsageData ReadJson(JsonReader reader, System.Type objectType, AliasedUsageData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _testObj = serializer.Deserialize<Izumi.Test.Domain01.TestObject>(json["testObj"].CreateReader());
            return new AliasedUsageData(
                _testObj, 
                Izumi.Test.Domain01.GoAliasEnumTestHelpers.From(json["enumField"].Value<string>())
            );
        }
    }
}