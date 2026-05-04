// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Inheritance {
    [JsonConverter(typeof(CovariantDTO1_JsonNetConverter))]
    public class CovariantDTO1 : WithCovariance {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance";
        public static readonly string RTTI_CLASSNAME = "CovariantDTO1";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.CovariantDTO1";
        public string GetPackageName() { return CovariantDTO1.RTTI_PACKAGE; }
        public string GetClassName() { return CovariantDTO1.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CovariantDTO1.RTTI_FULLCLASSNAME; }

        public Idltest.Inheritance.Covariant Field { get; set; }

        public CovariantDTO1() {
        }

        public CovariantDTO1(Idltest.Inheritance.Covariant field) {
            this.Field = field;
        }

        public WithCovariance ToWithCovariance() {
            var res = new WithCovarianceStruct();
            res.Field = this.Field;
            return res;
        }

        public void LoadWithCovariance(WithCovariance value) {
            this.Field = value.Field;
        }

    }
    public class CovariantDTO1_JsonNetConverter: JsonNetConverter<CovariantDTO1> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CovariantDTO1_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CovariantDTO1 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("field");
            // Serializing polymorphic type Covariant
            writer.WriteStartObject();
            writer.WritePropertyName(v.Field.GetFullClassName());
            serializer.Serialize(writer, v.Field);
            writer.WriteEndObject();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CovariantDTO1 ReadJson(JsonReader reader, System.Type objectType, CovariantDTO1 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new CovariantDTO1(
                serializer.Deserialize<Idltest.Inheritance.Covariant>(json["field"].CreateReader())
            );
        }
    }
}