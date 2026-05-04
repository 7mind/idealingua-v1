// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    [JsonConverter(typeof(Adt2_JsonNetConverter))]
    public class Adt2 {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain02";
        public static readonly string RTTI_CLASSNAME = "Adt2";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.Adt2";
        public string GetPackageName() { return Adt2.RTTI_PACKAGE; }
        public string GetClassName() { return Adt2.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Adt2.RTTI_FULLCLASSNAME; }

        public int B { get; set; }

        public Adt2() {
        }

        public Adt2(int b) {
            this.B = b;
        }

    }
    public class Adt2_JsonNetConverter: JsonNetConverter<Adt2> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Adt2_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Adt2 v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("b");
            writer.WriteValue(v.B);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Adt2 ReadJson(JsonReader reader, System.Type objectType, Adt2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Adt2(
                json["b"].Value<int>()
            );
        }
    }
}