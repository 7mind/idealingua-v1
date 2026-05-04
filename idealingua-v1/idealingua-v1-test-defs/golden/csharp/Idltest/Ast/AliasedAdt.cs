// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    using _Event = Idltest.Ast.EventData;
    using _Public = Idltest.Ast.PublicData;

    [JsonConverter(typeof(AliasedAdt_JsonNetConverter))]
    public abstract class AliasedAdt {
        public interface IAliasedAdtVisitor {
            void Visit(Event visitor);
            void Visit(Public visitor);
        }

        public abstract void Visit(IAliasedAdtVisitor visitor);
        private AliasedAdt() {}

        public sealed class Event: AliasedAdt {
            public _Event Value { get; private set; }
            public Event(_Event value) {
                this.Value = value;
            }

            public override void Visit(IAliasedAdtVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _Event(Event m) {
                return m.Value;
            }

            public static explicit operator Event(_Event m) {
                return new Event(m);
            }

        }

        public sealed class Public: AliasedAdt {
            public _Public Value { get; private set; }
            public Public(_Public value) {
                this.Value = value;
            }

            public override void Visit(IAliasedAdtVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _Public(Public m) {
                return m.Value;
            }

            public static explicit operator Public(_Public m) {
                return new Public(m);
            }

        }

    }
    public class AliasedAdt_JsonNetConverter: JsonNetConverter<AliasedAdt> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AliasedAdt_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AliasedAdt al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is AliasedAdt.Event) {
                writer.WritePropertyName("event");
                var v = (al as AliasedAdt.Event).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is AliasedAdt.Public) {
                writer.WritePropertyName("public");
                var v = (al as AliasedAdt.Public).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown AliasedAdt type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AliasedAdt ReadJson(JsonReader reader, System.Type objectType, AliasedAdt existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "event": {
                    var v = serializer.Deserialize<Idltest.Ast.EventData>(kv.Value.CreateReader());
                    return new AliasedAdt.Event(v);
                }

                case "public": {
                    var v = serializer.Deserialize<Idltest.Ast.PublicData>(kv.Value.CreateReader());
                    return new AliasedAdt.Public(v);
                }

                default:
                    throw new System.Exception("Unknown AliasedAdt type: " + kv.Name);
            }
        }
    }
}