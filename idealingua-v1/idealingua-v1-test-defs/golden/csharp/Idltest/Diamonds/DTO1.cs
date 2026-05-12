// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Diamonds {
    [JsonConverter(typeof(DTO1_JsonNetConverter))]
    public class DTO1 {
        public static readonly string RTTI_PACKAGE = "idltest.diamonds";
        public static readonly string RTTI_CLASSNAME = "DTO1";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.diamonds.DTO1";
        public string GetPackageName() { return DTO1.RTTI_PACKAGE; }
        public string GetClassName() { return DTO1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return DTO1.RTTI_FULLCLASSNAME; }

        public int If1Field_overriden { get; set; }
        public int If1Field_inherited { get; set; }
        public long SameField { get; set; }
        public long SameEverywhereField { get; set; }
        public long If3Field { get; set; }
        public long If2Field { get; set; }

        public DTO1() {
        }

        public DTO1(int if1Field_overriden, int if1Field_inherited, long sameField, long sameEverywhereField, long if3Field, long if2Field) {
            this.If1Field_overriden = if1Field_overriden;
            this.If1Field_inherited = if1Field_inherited;
            this.SameField = sameField;
            this.SameEverywhereField = sameEverywhereField;
            this.If3Field = if3Field;
            this.If2Field = if2Field;
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
                json["if3Field"].Value<long>(), 
                json["if2Field"].Value<long>()
            );
        }
    }
}