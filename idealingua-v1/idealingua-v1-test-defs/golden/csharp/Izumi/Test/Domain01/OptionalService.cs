// Auto-generated, any modifications may be overwritten in the future.

using System;
using IRT;
using IRT.Marshaller;
using IRT.Transport.Client;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Linq;

namespace Izumi.Test.Domain01 {
    public static class OptionalService {
        [JsonConverter(typeof(InOptionalMethod_JsonNetConverter))]
        public class InOptionalMethod {
            public static readonly string RTTI_PACKAGE = "izumi.test.domain01.OptionalService";
            public static readonly string RTTI_CLASSNAME = "InOptionalMethod";
            public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.OptionalService.InOptionalMethod";
            public string GetPackageName() { return InOptionalMethod.RTTI_PACKAGE; }
            public string GetClassName() { return InOptionalMethod.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InOptionalMethod.RTTI_FULLCLASSNAME; }

            public Izumi.Test.Domain01.NestedClass A { get; set; }
            public Nullable<int> B { get; set; }
            public Izumi.Test.Domain01.NestedClass C { get; set; }

            public InOptionalMethod() {
            }

            public InOptionalMethod(Izumi.Test.Domain01.NestedClass a, Nullable<int> b, Izumi.Test.Domain01.NestedClass c) {
                this.A = a;
                this.B = b;
                this.C = c;
            }

        }

        public class InOptionalMethod_JsonNetConverter: JsonNetConverter<InOptionalMethod> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InOptionalMethod_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InOptionalMethod v, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (v.A != null) {
                    writer.WritePropertyName("a");
                    serializer.Serialize(writer, v.A);
                }

                if (v.B.HasValue) {
                    writer.WritePropertyName("b");
                    writer.WriteValue(v.B.Value);
                }

                if (v.C != null) {
                    writer.WritePropertyName("c");
                    serializer.Serialize(writer, v.C);
                }

                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InOptionalMethod ReadJson(JsonReader reader, System.Type objectType, InOptionalMethod existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                Izumi.Test.Domain01.NestedClass _a = null;
                var _aRaw = json["a"];
                if (_aRaw != null && _aRaw.Type != JTokenType.Null) {
                    _a = serializer.Deserialize<Izumi.Test.Domain01.NestedClass>(_aRaw.CreateReader());
                }

                Nullable<int> _b = null;
                var _bRaw = json["b"];
                if (_bRaw != null && _bRaw.Type != JTokenType.Null) {
                    _b = _bRaw.Value<int>();
                }

                Izumi.Test.Domain01.NestedClass _c = null;
                var _cRaw = json["c"];
                if (_cRaw != null && _cRaw.Type != JTokenType.Null) {
                    _c = serializer.Deserialize<Izumi.Test.Domain01.NestedClass>(_cRaw.CreateReader());
                }

                return new InOptionalMethod(
                    _a, 
                    _b, 
                    _c
                );
            }
        }

        // #i32

    }

    // ============== Service Client ==============
    public interface IOptionalServiceClient<C> where C: class, IClientTransportContext {
        void OptionalMethod(Izumi.Test.Domain01.NestedClass a, Nullable<int> b, Izumi.Test.Domain01.NestedClass c, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
    }

    public class OptionalServiceClientGeneric<C>: IOptionalServiceClient<C> where C: class, IClientTransportContext {
        public IClientTransport<C> Transport { get; private set; }

        public OptionalServiceClientGeneric(IClientTransport<C> t) {
            Transport = t;
        }

        public void SetHTTPTransport(string endpoint, IJsonMarshaller marshaller, bool blocking = false, int timeout = 60) {
            if (blocking) {
                this.Transport = new SyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            } else {
                this.Transport = new AsyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            }
        }
        public void OptionalMethod(Izumi.Test.Domain01.NestedClass a, Nullable<int> b, Izumi.Test.Domain01.NestedClass c, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new OptionalService.InOptionalMethod(a, b, c);
            Transport.Send<OptionalService.InOptionalMethod, int>("OptionalService", "optionalMethod", inData,
                new ClientTransportCallback<int>(onSuccess, onFailure, onAny), ctx);
        }

    }

    public class OptionalServiceClient: OptionalServiceClientGeneric<IClientTransportContext> {
        public OptionalServiceClient(IClientTransport<IClientTransportContext> t): base(t) {}
    }

    // ============== Service Dispatcher ==============
    public interface IOptionalServiceServer<C> {
        int OptionalMethod(C ctx, Izumi.Test.Domain01.NestedClass a, Nullable<int> b, Izumi.Test.Domain01.NestedClass c);
    }

    public class OptionalServiceDispatcher<C, D>: IServiceDispatcher<C, D> {
        private static readonly string[] methods = { "optionalMethod" };
        protected IMarshaller<D> marshaller;
        protected IOptionalServiceServer<C> server;

        public OptionalServiceDispatcher(IMarshaller<D> marshaller, IOptionalServiceServer<C> server) {
            this.marshaller = marshaller;
            this.server = server;
        }

        public string GetSupportedService() {
            return "OptionalService";
        }

        public string[] GetSupportedMethods() {
            return OptionalServiceDispatcher<C, D>.methods;
        }

        public D Dispatch(C ctx, string method, D data) {
            switch(method) {
                case "optionalMethod": {
                    var obj = marshaller.Unmarshal<OptionalService.InOptionalMethod>(data);
                    return marshaller.Marshal<int>(
                        server.OptionalMethod(ctx, obj.A, obj.B, obj.C)
                    );
                }

                default:
                    throw new DispatcherException(string.Format("Method {0} is not supported by OptionalServiceDispatcher.", method));
            }
        }
    }

    // ============== Service Server Base ==============
    public abstract class OptionalServiceServer<C, D>: OptionalServiceDispatcher<C, D>,  IOptionalServiceServer<C> {
        public OptionalServiceServer(IMarshaller<D> marshaller): base(marshaller, null) {
            server = this;
        }

        public virtual int OptionalMethod(C ctx, Izumi.Test.Domain01.NestedClass a, Nullable<int> b, Izumi.Test.Domain01.NestedClass c) {
            return 0;
        }

    }
}