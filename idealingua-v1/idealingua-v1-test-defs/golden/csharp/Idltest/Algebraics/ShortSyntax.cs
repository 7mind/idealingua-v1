// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    using _TestSuccess = Idltest.Algebraics.Success;
    using _Failure = Idltest.Algebraics.Failure;

    [JsonConverter(typeof(ShortSyntax_JsonNetConverter))]
    public abstract class ShortSyntax {
        public interface IShortSyntaxVisitor {
            void Visit(TestSuccess visitor);
            void Visit(Failure visitor);
        }

        public abstract void Visit(IShortSyntaxVisitor visitor);
        private ShortSyntax() {}

        public sealed class TestSuccess: ShortSyntax {
            public _TestSuccess Value { get; private set; }
            public TestSuccess(_TestSuccess value) {
                this.Value = value;
            }

            public override void Visit(IShortSyntaxVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _TestSuccess(TestSuccess m) {
                return m.Value;
            }

            public static explicit operator TestSuccess(_TestSuccess m) {
                return new TestSuccess(m);
            }

        }

        public sealed class Failure: ShortSyntax {
            public _Failure Value { get; private set; }
            public Failure(_Failure value) {
                this.Value = value;
            }

            public override void Visit(IShortSyntaxVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _Failure(Failure m) {
                return m.Value;
            }

            public static explicit operator Failure(_Failure m) {
                return new Failure(m);
            }

        }

    }
    public class ShortSyntax_JsonNetConverter: JsonNetConverter<ShortSyntax> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public ShortSyntax_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, ShortSyntax al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is ShortSyntax.TestSuccess) {
                writer.WritePropertyName("TestSuccess");
                var v = (al as ShortSyntax.TestSuccess).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is ShortSyntax.Failure) {
                writer.WritePropertyName("Failure");
                var v = (al as ShortSyntax.Failure).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown ShortSyntax type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override ShortSyntax ReadJson(JsonReader reader, System.Type objectType, ShortSyntax existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "TestSuccess": {
                    var v = serializer.Deserialize<Idltest.Algebraics.Success>(kv.Value.CreateReader());
                    return new ShortSyntax.TestSuccess(v);
                }

                case "Failure": {
                    var v = serializer.Deserialize<Idltest.Algebraics.Failure>(kv.Value.CreateReader());
                    return new ShortSyntax.Failure(v);
                }

                default:
                    throw new System.Exception("Unknown ShortSyntax type: " + kv.Name);
            }
        }
    }
}