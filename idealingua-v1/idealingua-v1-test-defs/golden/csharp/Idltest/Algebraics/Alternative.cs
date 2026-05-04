// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    using _TestSuccess = Idltest.Algebraics.Success;
    using _Failure = Idltest.Algebraics.Failure;

    [JsonConverter(typeof(Alternative_JsonNetConverter))]
    public abstract class Alternative {
        public interface IAlternativeVisitor {
            void Visit(TestSuccess visitor);
            void Visit(Failure visitor);
        }

        public abstract void Visit(IAlternativeVisitor visitor);
        private Alternative() {}

        public sealed class TestSuccess: Alternative {
            public _TestSuccess Value { get; private set; }
            public TestSuccess(_TestSuccess value) {
                this.Value = value;
            }

            public override void Visit(IAlternativeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _TestSuccess(TestSuccess m) {
                return m.Value;
            }

            public static explicit operator TestSuccess(_TestSuccess m) {
                return new TestSuccess(m);
            }

        }

        public sealed class Failure: Alternative {
            public _Failure Value { get; private set; }
            public Failure(_Failure value) {
                this.Value = value;
            }

            public override void Visit(IAlternativeVisitor visitor) {
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
    public class Alternative_JsonNetConverter: JsonNetConverter<Alternative> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Alternative_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Alternative al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is Alternative.TestSuccess) {
                writer.WritePropertyName("TestSuccess");
                var v = (al as Alternative.TestSuccess).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is Alternative.Failure) {
                writer.WritePropertyName("Failure");
                var v = (al as Alternative.Failure).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown Alternative type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Alternative ReadJson(JsonReader reader, System.Type objectType, Alternative existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "TestSuccess": {
                    var v = serializer.Deserialize<Idltest.Algebraics.Success>(kv.Value.CreateReader());
                    return new Alternative.TestSuccess(v);
                }

                case "Failure": {
                    var v = serializer.Deserialize<Idltest.Algebraics.Failure>(kv.Value.CreateReader());
                    return new Alternative.Failure(v);
                }

                default:
                    throw new System.Exception("Unknown Alternative type: " + kv.Name);
            }
        }
    }
}