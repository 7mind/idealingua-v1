// Auto-generated, any modifications may be overwritten in the future.

using Izumi.Test.Domain01;
using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(DTO1_JsonNetConverter))]
    public class DTO1 : TestInterface2, TestInterface3 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02";
        public static readonly string RTTI_CLASSNAME = "DTO1";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.DTO1";
        public string GetPackageName() { return DTO1.RTTI_PACKAGE; }
        public string GetClassName() { return DTO1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return DTO1.RTTI_FULLCLASSNAME; }

        public int If1Field_overriden { get; set; }
        public int If1Field_inherited { get; set; }
        public long SameField { get; set; }
        public long SameEverywhereField { get; set; }
        public Izumi.Test.Domain01.TestValIdentifier FromOtherDomain { get; set; }
        public Izumi.Test.Domain01.TestValIdentifier FromOtherDomainDirect { get; set; }
        public long If3Field { get; set; }
        public long If2Field { get; set; }

        public DTO1() {
        }

        public DTO1(int if1Field_overriden, int if1Field_inherited, long sameField, long sameEverywhereField, Izumi.Test.Domain01.TestValIdentifier fromOtherDomain, Izumi.Test.Domain01.TestValIdentifier fromOtherDomainDirect, long if3Field, long if2Field) {
            this.If1Field_overriden = if1Field_overriden;
            this.If1Field_inherited = if1Field_inherited;
            this.SameField = sameField;
            this.SameEverywhereField = sameEverywhereField;
            this.FromOtherDomain = fromOtherDomain;
            this.FromOtherDomainDirect = fromOtherDomainDirect;
            this.If3Field = if3Field;
            this.If2Field = if2Field;
        }

        public TestInterface2 ToTestInterface2() {
            var res = new TestInterface2Struct();
            res.If2Field = this.If2Field;
            res.SameField = this.SameField;
            res.SameEverywhereField = this.SameEverywhereField;
            return res;
        }

        public void LoadTestInterface2(TestInterface2 value) {
            this.If2Field = value.If2Field;
            this.SameField = value.SameField;
            this.SameEverywhereField = value.SameEverywhereField;
        }

        public TestInterface3 ToTestInterface3() {
            var res = new TestInterface3Struct();
            res.If1Field_overriden = this.If1Field_overriden;
            res.If1Field_inherited = this.If1Field_inherited;
            res.SameField = this.SameField;
            res.SameEverywhereField = this.SameEverywhereField;
            res.FromOtherDomain = this.FromOtherDomain;
            res.FromOtherDomainDirect = this.FromOtherDomainDirect;
            res.If3Field = this.If3Field;
            return res;
        }

        public void LoadTestInterface3(TestInterface3 value) {
            this.If1Field_overriden = value.If1Field_overriden;
            this.If1Field_inherited = value.If1Field_inherited;
            this.SameField = value.SameField;
            this.SameEverywhereField = value.SameEverywhereField;
            this.FromOtherDomain = value.FromOtherDomain;
            this.FromOtherDomainDirect = value.FromOtherDomainDirect;
            this.If3Field = value.If3Field;
        }

    }
    public class DTO1_JsonNetConverter: JsonNetConverter<DTO1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public DTO1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, DTO1 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("if1Field_overriden");
            writer.WriteValue(v.If1Field_overriden);
            writer.WritePropertyName("if1Field_inherited");
            writer.WriteValue(v.If1Field_inherited);
            writer.WritePropertyName("sameField");
            writer.WriteValue(v.SameField);
            writer.WritePropertyName("sameEverywhereField");
            writer.WriteValue(v.SameEverywhereField);
            writer.WritePropertyName("fromOtherDomain");
            writer.WriteValue(v.FromOtherDomain.ToString());
            writer.WritePropertyName("fromOtherDomainDirect");
            writer.WriteValue(v.FromOtherDomainDirect.ToString());
            writer.WritePropertyName("if3Field");
            writer.WriteValue(v.If3Field);
            writer.WritePropertyName("if2Field");
            writer.WriteValue(v.If2Field);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override DTO1 ReadJson(JsonReader reader, System.Type objectType, DTO1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new DTO1(
                json["if1Field_overriden"].Value<int>(), 
                json["if1Field_inherited"].Value<int>(), 
                json["sameField"].Value<long>(), 
                json["sameEverywhereField"].Value<long>(), 
                Izumi.Test.Domain01.TestValIdentifier.From(json["fromOtherDomain"].Value<string>()), 
                Izumi.Test.Domain01.TestValIdentifier.From(json["fromOtherDomainDirect"].Value<string>()), 
                json["if3Field"].Value<long>(), 
                json["if2Field"].Value<long>()
            );
        }
    }
}