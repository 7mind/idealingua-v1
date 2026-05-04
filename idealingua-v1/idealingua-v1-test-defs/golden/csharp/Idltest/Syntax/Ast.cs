// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Syntax {
    using _TestMixin = Idltest.Syntax.TestMixin;
    using _TestDto = Idltest.Syntax.TestDto;
    using _TestOneliners = Idltest.Syntax.TestOneliners;

    [JsonConverter(typeof(Ast_JsonNetConverter))]
    public abstract class Ast {
        public interface IAstVisitor {
            void Visit(TestMixin visitor);
            void Visit(TestDto visitor);
            void Visit(TestOneliners visitor);
        }

        public abstract void Visit(IAstVisitor visitor);
        private Ast() {}

        public sealed class TestMixin: Ast {
            public _TestMixin Value { get; private set; }
            public TestMixin(_TestMixin value) {
                this.Value = value;
            }

            public override void Visit(IAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TestMixin(TestMixin m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TestMixin(_TestMixin m) {
            //     return new TestMixin(m);
            // }

        }

        public sealed class TestDto: Ast {
            public _TestDto Value { get; private set; }
            public TestDto(_TestDto value) {
                this.Value = value;
            }

            public override void Visit(IAstVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _TestDto(TestDto m) {
                return m.Value;
            }

            public static explicit operator TestDto(_TestDto m) {
                return new TestDto(m);
            }

        }

        public sealed class TestOneliners: Ast {
            public _TestOneliners Value { get; private set; }
            public TestOneliners(_TestOneliners value) {
                this.Value = value;
            }

            public override void Visit(IAstVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _TestOneliners(TestOneliners m) {
                return m.Value;
            }

            public static explicit operator TestOneliners(_TestOneliners m) {
                return new TestOneliners(m);
            }

        }

    }
    public class Ast_JsonNetConverter: JsonNetConverter<Ast> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Ast_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Ast al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is Ast.TestMixin) {
                writer.WritePropertyName("TestMixin");
                var v = (al as Ast.TestMixin).Value;
                // Serializing polymorphic type TestMixin
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is Ast.TestDto) {
                writer.WritePropertyName("TestDto");
                var v = (al as Ast.TestDto).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is Ast.TestOneliners) {
                writer.WritePropertyName("TestOneliners");
                var v = (al as Ast.TestOneliners).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown Ast type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Ast ReadJson(JsonReader reader, System.Type objectType, Ast existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "TestMixin": {
                    var v = serializer.Deserialize<Idltest.Syntax.TestMixin>(kv.Value.CreateReader());
                    return new Ast.TestMixin(v);
                }

                case "TestDto": {
                    var v = serializer.Deserialize<Idltest.Syntax.TestDto>(kv.Value.CreateReader());
                    return new Ast.TestDto(v);
                }

                case "TestOneliners": {
                    var v = serializer.Deserialize<Idltest.Syntax.TestOneliners>(kv.Value.CreateReader());
                    return new Ast.TestOneliners(v);
                }

                default:
                    throw new System.Exception("Unknown Ast type: " + kv.Name);
            }
        }
    }
}