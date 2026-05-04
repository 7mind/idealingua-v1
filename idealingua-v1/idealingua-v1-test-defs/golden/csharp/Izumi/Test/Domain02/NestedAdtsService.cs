// Auto-generated, any modifications may be overwritten in the future.

using IRT;
using IRT.Marshaller;
using IRT.Transport.Client;
using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;

namespace Izumi.Test.Domain02 {
    using _AdtA = Izumi.Test.Domain02.AdtA;
    using _Adt2 = Izumi.Test.Domain02.Adt2;

    public static class NestedAdtsService {
        [JsonConverter(typeof(OutAdtNested_JsonNetConverter))]
        public abstract class OutAdtNested {
            public interface IOutAdtNestedVisitor {
                void Visit(AdtA visitor);
                void Visit(Adt2 visitor);
            }

            public abstract void Visit(IOutAdtNestedVisitor visitor);
            private OutAdtNested() {}

            public sealed class AdtA: OutAdtNested {
                public _AdtA Value { get; private set; }
                public AdtA(_AdtA value) {
                    this.Value = value;
                }

                public override void Visit(IOutAdtNestedVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _AdtA(AdtA m) {
                    return m.Value;
                }

                public static explicit operator AdtA(_AdtA m) {
                    return new AdtA(m);
                }

            }

            public sealed class Adt2: OutAdtNested {
                public _Adt2 Value { get; private set; }
                public Adt2(_Adt2 value) {
                    this.Value = value;
                }

                public override void Visit(IOutAdtNestedVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _Adt2(Adt2 m) {
                    return m.Value;
                }

                public static explicit operator Adt2(_Adt2 m) {
                    return new Adt2(m);
                }

            }

        }
        public class OutAdtNested_JsonNetConverter: JsonNetConverter<OutAdtNested> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutAdtNested_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutAdtNested al, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (al is OutAdtNested.AdtA) {
                    writer.WritePropertyName("AdtA");
                    var v = (al as OutAdtNested.AdtA).Value;
                    serializer.Serialize(writer, v);
                } else
                if (al is OutAdtNested.Adt2) {
                    writer.WritePropertyName("Adt2");
                    var v = (al as OutAdtNested.Adt2).Value;
                    serializer.Serialize(writer, v);
                } else
                {
                    throw new System.Exception("Unknown OutAdtNested type: " + al);
                }
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutAdtNested ReadJson(JsonReader reader, System.Type objectType, OutAdtNested existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var kv = json.Properties().First();
                switch (kv.Name) {
                    case "AdtA": {
                        var v = serializer.Deserialize<Izumi.Test.Domain02.AdtA>(kv.Value.CreateReader());
                        return new OutAdtNested.AdtA(v);
                    }

                    case "Adt2": {
                        var v = serializer.Deserialize<Izumi.Test.Domain02.Adt2>(kv.Value.CreateReader());
                        return new OutAdtNested.Adt2(v);
                    }

                    default:
                        throw new System.Exception("Unknown OutAdtNested type: " + kv.Name);
                }
            }
        }

    }

    // ============== Service Client ==============
    public interface INestedAdtsServiceClient<C> where C: class, IClientTransportContext {
        void AdtNested(Action<NestedAdtsService.OutAdtNested> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
    }

    public class NestedAdtsServiceClientGeneric<C>: INestedAdtsServiceClient<C> where C: class, IClientTransportContext {
        public IClientTransport<C> Transport { get; private set; }

        public NestedAdtsServiceClientGeneric(IClientTransport<C> t) {
            Transport = t;
        }

        public void SetHTTPTransport(string endpoint, IJsonMarshaller marshaller, bool blocking = false, int timeout = 60) {
            if (blocking) {
                this.Transport = new SyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            } else {
                this.Transport = new AsyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            }
        }
        public void AdtNested(Action<NestedAdtsService.OutAdtNested> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, NestedAdtsService.OutAdtNested>("NestedAdtsService", "adtNested", null,
                new ClientTransportCallback<NestedAdtsService.OutAdtNested>(onSuccess, onFailure, onAny), ctx);
        }

    }

    public class NestedAdtsServiceClient: NestedAdtsServiceClientGeneric<IClientTransportContext> {
        public NestedAdtsServiceClient(IClientTransport<IClientTransportContext> t): base(t) {}
    }

    // ============== Service Dispatcher ==============
    public interface INestedAdtsServiceServer<C> {
        NestedAdtsService.OutAdtNested AdtNested(C ctx);
    }

    public class NestedAdtsServiceDispatcher<C, D>: IServiceDispatcher<C, D> {
        private static readonly string[] methods = { "adtNested" };
        protected IMarshaller<D> marshaller;
        protected INestedAdtsServiceServer<C> server;

        public NestedAdtsServiceDispatcher(IMarshaller<D> marshaller, INestedAdtsServiceServer<C> server) {
            this.marshaller = marshaller;
            this.server = server;
        }

        public string GetSupportedService() {
            return "NestedAdtsService";
        }

        public string[] GetSupportedMethods() {
            return NestedAdtsServiceDispatcher<C, D>.methods;
        }

        public D Dispatch(C ctx, string method, D data) {
            switch(method) {
                case "adtNested": {
                    // No input params for this method
                    return marshaller.Marshal<NestedAdtsService.OutAdtNested>(
                        server.AdtNested(ctx)
                    );
                }

                default:
                    throw new DispatcherException(string.Format("Method {0} is not supported by NestedAdtsServiceDispatcher.", method));
            }
        }
    }

    // ============== Service Server Base ==============
    public abstract class NestedAdtsServiceServer<C, D>: NestedAdtsServiceDispatcher<C, D>,  INestedAdtsServiceServer<C> {
        public NestedAdtsServiceServer(IMarshaller<D> marshaller): base(marshaller, null) {
            server = this;
        }

        public virtual NestedAdtsService.OutAdtNested AdtNested(C ctx) {
            return null;
        }

    }
}