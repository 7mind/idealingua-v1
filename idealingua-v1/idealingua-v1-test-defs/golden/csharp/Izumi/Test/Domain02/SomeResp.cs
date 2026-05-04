// Auto-generated, any modifications may be overwritten in the future.

using Izumi.Test.Domain01;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain02 {
    using _ImportedBasicFailure = Izumi.Test.Domain01.BasicFailure;
    using _DTO1 = Izumi.Test.Domain02.DTO1;

    [JsonConverter(typeof(SomeResp_JsonNetConverter))]
    public abstract class SomeResp {
        public interface ISomeRespVisitor {
            void Visit(ImportedBasicFailure visitor);
            void Visit(DTO1 visitor);
        }

        public abstract void Visit(ISomeRespVisitor visitor);
        private SomeResp() {}

        public sealed class ImportedBasicFailure: SomeResp {
            public _ImportedBasicFailure Value { get; private set; }
            public ImportedBasicFailure(_ImportedBasicFailure value) {
                this.Value = value;
            }

            public override void Visit(ISomeRespVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _ImportedBasicFailure(ImportedBasicFailure m) {
                return m.Value;
            }

            public static explicit operator ImportedBasicFailure(_ImportedBasicFailure m) {
                return new ImportedBasicFailure(m);
            }

        }

        public sealed class DTO1: SomeResp {
            public _DTO1 Value { get; private set; }
            public DTO1(_DTO1 value) {
                this.Value = value;
            }

            public override void Visit(ISomeRespVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _DTO1(DTO1 m) {
                return m.Value;
            }

            public static explicit operator DTO1(_DTO1 m) {
                return new DTO1(m);
            }

        }

    }
    public class SomeResp_JsonNetConverter: JsonNetConverter<SomeResp> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public SomeResp_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, SomeResp al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is SomeResp.ImportedBasicFailure) {
                writer.WritePropertyName("ImportedBasicFailure");
                var v = (al as SomeResp.ImportedBasicFailure).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is SomeResp.DTO1) {
                writer.WritePropertyName("DTO1");
                var v = (al as SomeResp.DTO1).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown SomeResp type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override SomeResp ReadJson(JsonReader reader, System.Type objectType, SomeResp existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "ImportedBasicFailure": {
                    var v = serializer.Deserialize<Izumi.Test.Domain01.BasicFailure>(kv.Value.CreateReader());
                    return new SomeResp.ImportedBasicFailure(v);
                }

                case "DTO1": {
                    var v = serializer.Deserialize<Izumi.Test.Domain02.DTO1>(kv.Value.CreateReader());
                    return new SomeResp.DTO1(v);
                }

                default:
                    throw new System.Exception("Unknown SomeResp type: " + kv.Name);
            }
        }
    }
}