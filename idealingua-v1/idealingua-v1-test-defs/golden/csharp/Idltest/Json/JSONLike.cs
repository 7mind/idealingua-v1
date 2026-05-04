// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Json {
    using _JLObject = Idltest.Json.JLObject;
    using _JLArray = Idltest.Json.JLArray;
    using _JLString = Idltest.Json.JLString;
    using _JLNumber = Idltest.Json.JLNumber;
    using _JLBool = Idltest.Json.JLBool;
    using _JLNull = Idltest.Json.JLNull;

    [JsonConverter(typeof(JSONLike_JsonNetConverter))]
    public abstract class JSONLike {
        public interface IJSONLikeVisitor {
            void Visit(JLObject visitor);
            void Visit(JLArray visitor);
            void Visit(JLString visitor);
            void Visit(JLNumber visitor);
            void Visit(JLBool visitor);
            void Visit(JLNull visitor);
        }

        public abstract void Visit(IJSONLikeVisitor visitor);
        private JSONLike() {}

        public sealed class JLObject: JSONLike {
            public _JLObject Value { get; private set; }
            public JLObject(_JLObject value) {
                this.Value = value;
            }

            public override void Visit(IJSONLikeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _JLObject(JLObject m) {
                return m.Value;
            }

            public static explicit operator JLObject(_JLObject m) {
                return new JLObject(m);
            }

        }

        public sealed class JLArray: JSONLike {
            public _JLArray Value { get; private set; }
            public JLArray(_JLArray value) {
                this.Value = value;
            }

            public override void Visit(IJSONLikeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _JLArray(JLArray m) {
                return m.Value;
            }

            public static explicit operator JLArray(_JLArray m) {
                return new JLArray(m);
            }

        }

        public sealed class JLString: JSONLike {
            public _JLString Value { get; private set; }
            public JLString(_JLString value) {
                this.Value = value;
            }

            public override void Visit(IJSONLikeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _JLString(JLString m) {
                return m.Value;
            }

            public static explicit operator JLString(_JLString m) {
                return new JLString(m);
            }

        }

        public sealed class JLNumber: JSONLike {
            public _JLNumber Value { get; private set; }
            public JLNumber(_JLNumber value) {
                this.Value = value;
            }

            public override void Visit(IJSONLikeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _JLNumber(JLNumber m) {
                return m.Value;
            }

            public static explicit operator JLNumber(_JLNumber m) {
                return new JLNumber(m);
            }

        }

        public sealed class JLBool: JSONLike {
            public _JLBool Value { get; private set; }
            public JLBool(_JLBool value) {
                this.Value = value;
            }

            public override void Visit(IJSONLikeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _JLBool(JLBool m) {
                return m.Value;
            }

            public static explicit operator JLBool(_JLBool m) {
                return new JLBool(m);
            }

        }

        public sealed class JLNull: JSONLike {
            public _JLNull Value { get; private set; }
            public JLNull(_JLNull value) {
                this.Value = value;
            }

            public override void Visit(IJSONLikeVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _JLNull(JLNull m) {
                return m.Value;
            }

            public static explicit operator JLNull(_JLNull m) {
                return new JLNull(m);
            }

        }

    }
    public class JSONLike_JsonNetConverter: JsonNetConverter<JSONLike> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public JSONLike_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, JSONLike al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is JSONLike.JLObject) {
                writer.WritePropertyName("JLObject");
                var v = (al as JSONLike.JLObject).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is JSONLike.JLArray) {
                writer.WritePropertyName("JLArray");
                var v = (al as JSONLike.JLArray).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is JSONLike.JLString) {
                writer.WritePropertyName("JLString");
                var v = (al as JSONLike.JLString).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is JSONLike.JLNumber) {
                writer.WritePropertyName("JLNumber");
                var v = (al as JSONLike.JLNumber).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is JSONLike.JLBool) {
                writer.WritePropertyName("JLBool");
                var v = (al as JSONLike.JLBool).Value;
                serializer.Serialize(writer, v);
            } else
            if (al is JSONLike.JLNull) {
                writer.WritePropertyName("JLNull");
                var v = (al as JSONLike.JLNull).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown JSONLike type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override JSONLike ReadJson(JsonReader reader, System.Type objectType, JSONLike existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "JLObject": {
                    var v = serializer.Deserialize<Idltest.Json.JLObject>(kv.Value.CreateReader());
                    return new JSONLike.JLObject(v);
                }

                case "JLArray": {
                    var v = serializer.Deserialize<Idltest.Json.JLArray>(kv.Value.CreateReader());
                    return new JSONLike.JLArray(v);
                }

                case "JLString": {
                    var v = serializer.Deserialize<Idltest.Json.JLString>(kv.Value.CreateReader());
                    return new JSONLike.JLString(v);
                }

                case "JLNumber": {
                    var v = serializer.Deserialize<Idltest.Json.JLNumber>(kv.Value.CreateReader());
                    return new JSONLike.JLNumber(v);
                }

                case "JLBool": {
                    var v = serializer.Deserialize<Idltest.Json.JLBool>(kv.Value.CreateReader());
                    return new JSONLike.JLBool(v);
                }

                case "JLNull": {
                    var v = serializer.Deserialize<Idltest.Json.JLNull>(kv.Value.CreateReader());
                    return new JSONLike.JLNull(v);
                }

                default:
                    throw new System.Exception("Unknown JSONLike type: " + kv.Name);
            }
        }
    }
}