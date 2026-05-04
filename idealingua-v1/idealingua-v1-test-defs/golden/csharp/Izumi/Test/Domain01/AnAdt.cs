// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    using _AllTypes = Izumi.Test.Domain01.AllTypes;
    using _TestObject = Izumi.Test.Domain01.TestObject;
    using _AnotherMember = Izumi.Test.Domain01.AnyValTest;

    [JsonConverter(typeof(AnAdt_JsonNetConverter))]
    public abstract class AnAdt {
        public interface IAnAdtVisitor {
            void Visit(AllTypes visitor);
            void Visit(TestObject visitor);
            void Visit(AnotherMember visitor);
        }

        public abstract void Visit(IAnAdtVisitor visitor);
        private AnAdt() {}

        public sealed class AllTypes: AnAdt {
            public _AllTypes Value { get; private set; }
            public AllTypes(_AllTypes value) {
                this.Value = value;
            }

            public override void Visit(IAnAdtVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _AllTypes(AllTypes m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator AllTypes(_AllTypes m) {
            //     return new AllTypes(m);
            // }

        }

        public sealed class TestObject: AnAdt {
            public _TestObject Value { get; private set; }
            public TestObject(_TestObject value) {
                this.Value = value;
            }

            public override void Visit(IAnAdtVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _TestObject(TestObject m) {
                return m.Value;
            }

            public static explicit operator TestObject(_TestObject m) {
                return new TestObject(m);
            }

        }

        public sealed class AnotherMember: AnAdt {
            public _AnotherMember Value { get; private set; }
            public AnotherMember(_AnotherMember value) {
                this.Value = value;
            }

            public override void Visit(IAnAdtVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _AnotherMember(AnotherMember m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator AnotherMember(_AnotherMember m) {
            //     return new AnotherMember(m);
            // }

        }

    }
    public class AnAdt_JsonNetConverter: JsonNetConverter<AnAdt> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AnAdt_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AnAdt al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is AnAdt.AllTypes) {
                writer.WritePropertyName("AllTypes");
                var v = (al as AnAdt.AllTypes).Value;
                // Serializing polymorphic type AllTypes
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AnAdt.TestObject) {
                writer.WritePropertyName("TestObject");
                var v = (al as AnAdt.TestObject).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is AnAdt.AnotherMember) {
                writer.WritePropertyName("AnotherMember");
                var v = (al as AnAdt.AnotherMember).Value;
                // Serializing polymorphic type AnyValTest
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            {
                throw new System.Exception("Unknown AnAdt type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AnAdt ReadJson(JsonReader reader, System.Type objectType, AnAdt existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "AllTypes": {
                    var v = serializer.Deserialize<Izumi.Test.Domain01.AllTypes>(kv.Value.CreateReader());
                    return new AnAdt.AllTypes(v);
                }

                case "TestObject": {
                    var v = serializer.Deserialize<Izumi.Test.Domain01.TestObject>(kv.Value.CreateReader());
                    return new AnAdt.TestObject(v);
                }

                case "AnotherMember": {
                    var v = serializer.Deserialize<Izumi.Test.Domain01.AnyValTest>(kv.Value.CreateReader());
                    return new AnAdt.AnotherMember(v);
                }

                default:
                    throw new System.Exception("Unknown AnAdt type: " + kv.Name);
            }
        }
    }
}