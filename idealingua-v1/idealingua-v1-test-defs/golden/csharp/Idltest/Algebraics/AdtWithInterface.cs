// Auto-generated, any modifications may be overwritten in the future.

using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Algebraics {
    using _AFace = Idltest.Algebraics.AFace;
    using _Success = Idltest.Algebraics.Success;

    [JsonConverter(typeof(AdtWithInterface_JsonNetConverter))]
    public abstract class AdtWithInterface {
        public interface IAdtWithInterfaceVisitor {
            void Visit(AFace visitor);
            void Visit(Success visitor);
        }

        public abstract void Visit(IAdtWithInterfaceVisitor visitor);
        private AdtWithInterface() {}

        public sealed class AFace: AdtWithInterface {
            public _AFace Value { get; private set; }
            public AFace(_AFace value) {
                this.Value = value;
            }

            public override void Visit(IAdtWithInterfaceVisitor visitor) {
                visitor.Visit(this);
            }

            // We would normally want to have an operator, but unfortunately if it is an interface,
            // it will fail on "user-defined conversions to or from an interface are not allowed".
            // public static explicit operator _AFace(AFace m) {
            //     return m.Value;
            // }
            //
            // public static explicit operator AFace(_AFace m) {
            //     return new AFace(m);
            // }

        }

        public sealed class Success: AdtWithInterface {
            public _Success Value { get; private set; }
            public Success(_Success value) {
                this.Value = value;
            }

            public override void Visit(IAdtWithInterfaceVisitor visitor) {
                visitor.Visit(this);
            }

            public static explicit operator _Success(Success m) {
                return m.Value;
            }

            public static explicit operator Success(_Success m) {
                return new Success(m);
            }

        }

    }
    public class AdtWithInterface_JsonNetConverter: JsonNetConverter<AdtWithInterface> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AdtWithInterface_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AdtWithInterface al, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (al is AdtWithInterface.AFace) {
                writer.WritePropertyName("AFace");
                var v = (al as AdtWithInterface.AFace).Value;
                // Serializing polymorphic type AFace
                writer.WriteStartObject();
                writer.WritePropertyName(v.GetFullClassName());
                serializer.Serialize(writer, v);
                writer.WriteEndObject();

            } else
            if (al is AdtWithInterface.Success) {
                writer.WritePropertyName("Success");
                var v = (al as AdtWithInterface.Success).Value;
                serializer.Serialize(writer, v);
            } else
            {
                throw new System.Exception("Unknown AdtWithInterface type: " + al);
            }
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AdtWithInterface ReadJson(JsonReader reader, System.Type objectType, AdtWithInterface existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            switch (kv.Name) {
                case "AFace": {
                    var v = serializer.Deserialize<Idltest.Algebraics.AFace>(kv.Value.CreateReader());
                    return new AdtWithInterface.AFace(v);
                }

                case "Success": {
                    var v = serializer.Deserialize<Idltest.Algebraics.Success>(kv.Value.CreateReader());
                    return new AdtWithInterface.Success(v);
                }

                default:
                    throw new System.Exception("Unknown AdtWithInterface type: " + kv.Name);
            }
        }
    }
}