// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    using _ComplexAdt = Idltest.Algebraics.ComplexAdt;
    using _ComplexAdt2 = Idltest.Algebraics.ComplexAdt2;

    [JsonConverter(typeof(AdtTester_JsonNetConverter))]
    public abstract class AdtTester {
        public interface IAdtTesterVisitor {
            void Visit(ComplexAdt visitor);
            void Visit(ComplexAdt2 visitor);
        }

        public abstract void Visit(IAdtTesterVisitor visitor);
        private AdtTester() {}

        public sealed class ComplexAdt: AdtTester {
            public _ComplexAdt Value { get; private set; }
            public ComplexAdt(_ComplexAdt value) {
                this.Value = value;
            }

            public override void Visit(IAdtTesterVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _ComplexAdt(ComplexAdt m) {
                return m.Value;
            }

            public static explicit operator ComplexAdt(_ComplexAdt m) {
                return new ComplexAdt(m);
            }

        }

        public sealed class ComplexAdt2: AdtTester {
            public _ComplexAdt2 Value { get; private set; }
            public ComplexAdt2(_ComplexAdt2 value) {
                this.Value = value;
            }

            public override void Visit(IAdtTesterVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _ComplexAdt2(ComplexAdt2 m) {
                return m.Value;
            }

            public static explicit operator ComplexAdt2(_ComplexAdt2 m) {
                return new ComplexAdt2(m);
            }

        }

    }
    public class AdtTester_JsonNetConverter: JsonNetConverter<AdtTester> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AdtTester_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AdtTester al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is AdtTester.ComplexAdt) {
                writer.WritePropertyName("ComplexAdt");
                var v = (al as AdtTester.ComplexAdt).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is AdtTester.ComplexAdt2) {
                writer.WritePropertyName("ComplexAdt2");
                var v = (al as AdtTester.ComplexAdt2).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown AdtTester type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AdtTester ReadJson(JsonReader reader, System.Type objectType, AdtTester existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "ComplexAdt": {
                    var v = serializer.Deserialize<Idltest.Algebraics.ComplexAdt>(kv.Value.CreateReader());
                    return new AdtTester.ComplexAdt(v);
                }

                case "ComplexAdt2": {
                    var v = serializer.Deserialize<Idltest.Algebraics.ComplexAdt2>(kv.Value.CreateReader());
                    return new AdtTester.ComplexAdt2(v);
                }

                default:
                    throw new System.Exception("Unknown AdtTester type: " + kv.Name);
            }
        }
    }
}