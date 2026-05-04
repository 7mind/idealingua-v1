// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    using _TIntNode = Idltest.Ast.TIntNode;
    using _TFloatNode = Idltest.Ast.TFloatNode;
    using _TBoolNode = Idltest.Ast.TBoolNode;
    using _TSymNode = Idltest.Ast.TSymNode;
    using _TAppNode = Idltest.Ast.TAppNode;
    using _TLamNode = Idltest.Ast.TLamNode;
    using _TIfNode = Idltest.Ast.TIfNode;

    [JsonConverter(typeof(TypedAst_JsonNetConverter))]
    public abstract class TypedAst {
        public interface ITypedAstVisitor {
            void Visit(TIntNode visitor);
            void Visit(TFloatNode visitor);
            void Visit(TBoolNode visitor);
            void Visit(TSymNode visitor);
            void Visit(TAppNode visitor);
            void Visit(TLamNode visitor);
            void Visit(TIfNode visitor);
        }

        public abstract void Visit(ITypedAstVisitor visitor);
        private TypedAst() {}

        public sealed class TIntNode: TypedAst {
            public _TIntNode Value { get; private set; }
            public TIntNode(_TIntNode value) {
                this.Value = value;
            }

            public override void Visit(ITypedAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TIntNode(TIntNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TIntNode(_TIntNode m) {
            //     return new TIntNode(m);
            // }

        }

        public sealed class TFloatNode: TypedAst {
            public _TFloatNode Value { get; private set; }
            public TFloatNode(_TFloatNode value) {
                this.Value = value;
            }

            public override void Visit(ITypedAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TFloatNode(TFloatNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TFloatNode(_TFloatNode m) {
            //     return new TFloatNode(m);
            // }

        }

        public sealed class TBoolNode: TypedAst {
            public _TBoolNode Value { get; private set; }
            public TBoolNode(_TBoolNode value) {
                this.Value = value;
            }

            public override void Visit(ITypedAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TBoolNode(TBoolNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TBoolNode(_TBoolNode m) {
            //     return new TBoolNode(m);
            // }

        }

        public sealed class TSymNode: TypedAst {
            public _TSymNode Value { get; private set; }
            public TSymNode(_TSymNode value) {
                this.Value = value;
            }

            public override void Visit(ITypedAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TSymNode(TSymNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TSymNode(_TSymNode m) {
            //     return new TSymNode(m);
            // }

        }

        public sealed class TAppNode: TypedAst {
            public _TAppNode Value { get; private set; }
            public TAppNode(_TAppNode value) {
                this.Value = value;
            }

            public override void Visit(ITypedAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TAppNode(TAppNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TAppNode(_TAppNode m) {
            //     return new TAppNode(m);
            // }

        }

        public sealed class TLamNode: TypedAst {
            public _TLamNode Value { get; private set; }
            public TLamNode(_TLamNode value) {
                this.Value = value;
            }

            public override void Visit(ITypedAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TLamNode(TLamNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TLamNode(_TLamNode m) {
            //     return new TLamNode(m);
            // }

        }

        public sealed class TIfNode: TypedAst {
            public _TIfNode Value { get; private set; }
            public TIfNode(_TIfNode value) {
                this.Value = value;
            }

            public override void Visit(ITypedAstVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _TIfNode(TIfNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator TIfNode(_TIfNode m) {
            //     return new TIfNode(m);
            // }

        }

    }
    public class TypedAst_JsonNetConverter: JsonNetConverter<TypedAst> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TypedAst_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TypedAst al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is TypedAst.TIntNode) {
                writer.WritePropertyName("TIntNode");
                var v = (al as TypedAst.TIntNode).Value;
                // Serializing polymorphic type TIntNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is TypedAst.TFloatNode) {
                writer.WritePropertyName("TFloatNode");
                var v = (al as TypedAst.TFloatNode).Value;
                // Serializing polymorphic type TFloatNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is TypedAst.TBoolNode) {
                writer.WritePropertyName("TBoolNode");
                var v = (al as TypedAst.TBoolNode).Value;
                // Serializing polymorphic type TBoolNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is TypedAst.TSymNode) {
                writer.WritePropertyName("TSymNode");
                var v = (al as TypedAst.TSymNode).Value;
                // Serializing polymorphic type TSymNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is TypedAst.TAppNode) {
                writer.WritePropertyName("TAppNode");
                var v = (al as TypedAst.TAppNode).Value;
                // Serializing polymorphic type TAppNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is TypedAst.TLamNode) {
                writer.WritePropertyName("TLamNode");
                var v = (al as TypedAst.TLamNode).Value;
                // Serializing polymorphic type TLamNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is TypedAst.TIfNode) {
                writer.WritePropertyName("TIfNode");
                var v = (al as TypedAst.TIfNode).Value;
                // Serializing polymorphic type TIfNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            {
                throw new System.Exception("Unknown TypedAst type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TypedAst ReadJson(JsonReader reader, System.Type objectType, TypedAst existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "TIntNode": {
                    var v = serializer.Deserialize<Idltest.Ast.TIntNode>(kv.Value.CreateReader());
                    return new TypedAst.TIntNode(v);
                }

                case "TFloatNode": {
                    var v = serializer.Deserialize<Idltest.Ast.TFloatNode>(kv.Value.CreateReader());
                    return new TypedAst.TFloatNode(v);
                }

                case "TBoolNode": {
                    var v = serializer.Deserialize<Idltest.Ast.TBoolNode>(kv.Value.CreateReader());
                    return new TypedAst.TBoolNode(v);
                }

                case "TSymNode": {
                    var v = serializer.Deserialize<Idltest.Ast.TSymNode>(kv.Value.CreateReader());
                    return new TypedAst.TSymNode(v);
                }

                case "TAppNode": {
                    var v = serializer.Deserialize<Idltest.Ast.TAppNode>(kv.Value.CreateReader());
                    return new TypedAst.TAppNode(v);
                }

                case "TLamNode": {
                    var v = serializer.Deserialize<Idltest.Ast.TLamNode>(kv.Value.CreateReader());
                    return new TypedAst.TLamNode(v);
                }

                case "TIfNode": {
                    var v = serializer.Deserialize<Idltest.Ast.TIfNode>(kv.Value.CreateReader());
                    return new TypedAst.TIfNode(v);
                }

                default:
                    throw new System.Exception("Unknown TypedAst type: " + kv.Name);
            }
        }
    }
}