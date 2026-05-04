// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Events {
    using _BranchA = Idltest.Events.BranchA;
    using _BranchB = Idltest.Events.BranchB;

    [JsonConverter(typeof(ADTType_JsonNetConverter))]
    public abstract class ADTType {
        public interface IADTTypeVisitor {
            void Visit(BranchA visitor);
            void Visit(BranchB visitor);
        }

        public abstract void Visit(IADTTypeVisitor visitor);
        private ADTType() {}

        public sealed class BranchA: ADTType {
            public _BranchA Value { get; private set; }
            public BranchA(_BranchA value) {
                this.Value = value;
            }

            public override void Visit(IADTTypeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _BranchA(BranchA m) {
                return m.Value;
            }

            public static explicit operator BranchA(_BranchA m) {
                return new BranchA(m);
            }

        }

        public sealed class BranchB: ADTType {
            public _BranchB Value { get; private set; }
            public BranchB(_BranchB value) {
                this.Value = value;
            }

            public override void Visit(IADTTypeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _BranchB(BranchB m) {
                return m.Value;
            }

            public static explicit operator BranchB(_BranchB m) {
                return new BranchB(m);
            }

        }

    }
    public class ADTType_JsonNetConverter: JsonNetConverter<ADTType> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ADTType_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ADTType al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is ADTType.BranchA) {
                writer.WritePropertyName("BranchA");
                var v = (al as ADTType.BranchA).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is ADTType.BranchB) {
                writer.WritePropertyName("BranchB");
                var v = (al as ADTType.BranchB).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown ADTType type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ADTType ReadJson(JsonReader reader, System.Type objectType, ADTType existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "BranchA": {
                    var v = serializer.Deserialize<Idltest.Events.BranchA>(kv.Value.CreateReader());
                    return new ADTType.BranchA(v);
                }

                case "BranchB": {
                    var v = serializer.Deserialize<Idltest.Events.BranchB>(kv.Value.CreateReader());
                    return new ADTType.BranchB(v);
                }

                default:
                    throw new System.Exception("Unknown ADTType type: " + kv.Name);
            }
        }
    }
}