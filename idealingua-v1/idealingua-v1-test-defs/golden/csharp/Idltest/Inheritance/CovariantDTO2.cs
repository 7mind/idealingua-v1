// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Inheritance {
    [JsonConverter(typeof(CovariantDTO2_JsonNetConverter))]
    public class CovariantDTO2 : InheritedCovariant {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance";
        public static readonly string RTTI_CLASSNAME = "CovariantDTO2";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.CovariantDTO2";
        public string GetPackageName() { return CovariantDTO2.RTTI_PACKAGE; }
        public string GetClassName() { return CovariantDTO2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CovariantDTO2.RTTI_FULLCLASSNAME; }

        public Idltest.Inheritance.Covariant Field { get; set; }

        public CovariantDTO2() {
        }

        public CovariantDTO2(Idltest.Inheritance.Covariant field) {
            this.Field = field;
        }

        public InheritedCovariant ToInheritedCovariant() {
            var res = new InheritedCovariantStruct();
            res.Field = this.Field;
            return res;
        }

        public void LoadInheritedCovariant(InheritedCovariant value) {
            this.Field = value.Field;
        }

    }
    public class CovariantDTO2_JsonNetConverter: JsonNetConverter<CovariantDTO2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CovariantDTO2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CovariantDTO2 v, JsonSerializer serializer) {
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
        public override CovariantDTO2 ReadJson(JsonReader reader, System.Type objectType, CovariantDTO2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new CovariantDTO2(
                serializer.Deserialize<Idltest.Inheritance.Covariant>(json["field"].CreateReader())
            );
        }
    }
}