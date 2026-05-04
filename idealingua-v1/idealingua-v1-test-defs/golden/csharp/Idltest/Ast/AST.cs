// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Ast {
    using _IntNode = Idltest.Ast.IntNode;
    using _FloatRenamed = Idltest.Ast.FloatNode;
    using _BoolNode = Idltest.Ast.BoolNode;
    using _SymNode = Idltest.Ast.SymNode;
    using _AppNode = Idltest.Ast.AppNode;
    using _LamNode = Idltest.Ast.LamNode;
    using _IfNode = Idltest.Ast.IfNode;

    [JsonConverter(typeof(AST_JsonNetConverter))]
    public abstract class AST {
        public interface IASTVisitor {
            void Visit(IntNode visitor);
            void Visit(FloatRenamed visitor);
            void Visit(BoolNode visitor);
            void Visit(SymNode visitor);
            void Visit(AppNode visitor);
            void Visit(LamNode visitor);
            void Visit(IfNode visitor);
        }

        public abstract void Visit(IASTVisitor visitor);
        private AST() {}

        public sealed class IntNode: AST {
            public _IntNode Value { get; private set; }
            public IntNode(_IntNode value) {
                this.Value = value;
            }

            public override void Visit(IASTVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _IntNode(IntNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator IntNode(_IntNode m) {
            //     return new IntNode(m);
            // }

        }

        public sealed class FloatRenamed: AST {
            public _FloatRenamed Value { get; private set; }
            public FloatRenamed(_FloatRenamed value) {
                this.Value = value;
            }

            public override void Visit(IASTVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _FloatRenamed(FloatRenamed m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator FloatRenamed(_FloatRenamed m) {
            //     return new FloatRenamed(m);
            // }

        }

        public sealed class BoolNode: AST {
            public _BoolNode Value { get; private set; }
            public BoolNode(_BoolNode value) {
                this.Value = value;
            }

            public override void Visit(IASTVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _BoolNode(BoolNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator BoolNode(_BoolNode m) {
            //     return new BoolNode(m);
            // }

        }

        public sealed class SymNode: AST {
            public _SymNode Value { get; private set; }
            public SymNode(_SymNode value) {
                this.Value = value;
            }

            public override void Visit(IASTVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _SymNode(SymNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator SymNode(_SymNode m) {
            //     return new SymNode(m);
            // }

        }

        public sealed class AppNode: AST {
            public _AppNode Value { get; private set; }
            public AppNode(_AppNode value) {
                this.Value = value;
            }

            public override void Visit(IASTVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _AppNode(AppNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator AppNode(_AppNode m) {
            //     return new AppNode(m);
            // }

        }

        public sealed class LamNode: AST {
            public _LamNode Value { get; private set; }
            public LamNode(_LamNode value) {
                this.Value = value;
            }

            public override void Visit(IASTVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _LamNode(LamNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator LamNode(_LamNode m) {
            //     return new LamNode(m);
            // }

        }

        public sealed class IfNode: AST {
            public _IfNode Value { get; private set; }
            public IfNode(_IfNode value) {
                this.Value = value;
            }

            public override void Visit(IASTVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _IfNode(IfNode m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator IfNode(_IfNode m) {
            //     return new IfNode(m);
            // }

        }

    }
    public class AST_JsonNetConverter: JsonNetConverter<AST> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AST_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AST al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is AST.IntNode) {
                writer.WritePropertyName("IntNode");
                var v = (al as AST.IntNode).Value;
                // Serializing polymorphic type IntNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AST.FloatRenamed) {
                writer.WritePropertyName("FloatRenamed");
                var v = (al as AST.FloatRenamed).Value;
                // Serializing polymorphic type FloatNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AST.BoolNode) {
                writer.WritePropertyName("BoolNode");
                var v = (al as AST.BoolNode).Value;
                // Serializing polymorphic type BoolNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AST.SymNode) {
                writer.WritePropertyName("SymNode");
                var v = (al as AST.SymNode).Value;
                // Serializing polymorphic type SymNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AST.AppNode) {
                writer.WritePropertyName("AppNode");
                var v = (al as AST.AppNode).Value;
                // Serializing polymorphic type AppNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AST.LamNode) {
                writer.WritePropertyName("LamNode");
                var v = (al as AST.LamNode).Value;
                // Serializing polymorphic type LamNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AST.IfNode) {
                writer.WritePropertyName("IfNode");
                var v = (al as AST.IfNode).Value;
                // Serializing polymorphic type IfNode
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            {
                throw new System.Exception("Unknown AST type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AST ReadJson(JsonReader reader, System.Type objectType, AST existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "IntNode": {
                    var v = serializer.Deserialize<Idltest.Ast.IntNode>(kv.Value.CreateReader());
                    return new AST.IntNode(v);
                }

                case "FloatRenamed": {
                    var v = serializer.Deserialize<Idltest.Ast.FloatNode>(kv.Value.CreateReader());
                    return new AST.FloatRenamed(v);
                }

                case "BoolNode": {
                    var v = serializer.Deserialize<Idltest.Ast.BoolNode>(kv.Value.CreateReader());
                    return new AST.BoolNode(v);
                }

                case "SymNode": {
                    var v = serializer.Deserialize<Idltest.Ast.SymNode>(kv.Value.CreateReader());
                    return new AST.SymNode(v);
                }

                case "AppNode": {
                    var v = serializer.Deserialize<Idltest.Ast.AppNode>(kv.Value.CreateReader());
                    return new AST.AppNode(v);
                }

                case "LamNode": {
                    var v = serializer.Deserialize<Idltest.Ast.LamNode>(kv.Value.CreateReader());
                    return new AST.LamNode(v);
                }

                case "IfNode": {
                    var v = serializer.Deserialize<Idltest.Ast.IfNode>(kv.Value.CreateReader());
                    return new AST.IfNode(v);
                }

                default:
                    throw new System.Exception("Unknown AST type: " + kv.Name);
            }
        }
    }
}