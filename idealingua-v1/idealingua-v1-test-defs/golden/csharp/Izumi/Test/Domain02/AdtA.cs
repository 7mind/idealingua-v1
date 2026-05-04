// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    using _AdtA1 = Izumi.Test.Domain02.AdtA1;
    using _AdtA2 = Izumi.Test.Domain02.AdtA2;

    [JsonConverter(typeof(AdtA_JsonNetConverter))]
    public abstract class AdtA {
        public interface IAdtAVisitor {
            void Visit(AdtA1 visitor);
            void Visit(AdtA2 visitor);
        }

        public abstract void Visit(IAdtAVisitor visitor);
        private AdtA() {}

        public sealed class AdtA1: AdtA {
            public _AdtA1 Value { get; private set; }
            public AdtA1(_AdtA1 value) {
                this.Value = value;
            }

            public override void Visit(IAdtAVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _AdtA1(AdtA1 m) {
                return m.Value;
            }

            public static explicit operator AdtA1(_AdtA1 m) {
                return new AdtA1(m);
            }

        }

        public sealed class AdtA2: AdtA {
            public _AdtA2 Value { get; private set; }
            public AdtA2(_AdtA2 value) {
                this.Value = value;
            }

            public override void Visit(IAdtAVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _AdtA2(AdtA2 m) {
                return m.Value;
            }

            public static explicit operator AdtA2(_AdtA2 m) {
                return new AdtA2(m);
            }

        }

    }
    public class AdtA_JsonNetConverter: JsonNetConverter<AdtA> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AdtA_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AdtA al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is AdtA.AdtA1) {
                writer.WritePropertyName("AdtA1");
                var v = (al as AdtA.AdtA1).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is AdtA.AdtA2) {
                writer.WritePropertyName("AdtA2");
                var v = (al as AdtA.AdtA2).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown AdtA type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AdtA ReadJson(JsonReader reader, System.Type objectType, AdtA existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "AdtA1": {
                    var v = serializer.Deserialize<Izumi.Test.Domain02.AdtA1>(kv.Value.CreateReader());
                    return new AdtA.AdtA1(v);
                }

                case "AdtA2": {
                    var v = serializer.Deserialize<Izumi.Test.Domain02.AdtA2>(kv.Value.CreateReader());
                    return new AdtA.AdtA2(v);
                }

                default:
                    throw new System.Exception("Unknown AdtA type: " + kv.Name);
            }
        }
    }
}