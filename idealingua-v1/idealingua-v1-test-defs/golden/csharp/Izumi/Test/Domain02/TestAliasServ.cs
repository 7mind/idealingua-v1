// Auto-generated, any modifications may be overwritten in the future.

using System;
using Izumi.Test.Domain01;
using IRT;
using IRT.Marshaller;
using IRT.Transport.Client;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;

namespace Izumi.Test.Domain02 {
    using _TestIDReturn = Izumi.Test.Domain02.TestIDReturn;
    using _DTO1 = Izumi.Test.Domain02.DTO1;
    using _ImportedIDForDomain2 = Izumi.Test.Domain01.IDForDomain2;

    public static class TestAliasServ {
        [JsonConverter(typeof(InGetMassCoupons_JsonNetConverter))]
        public class InGetMassCoupons {
            public static readonly string RTTI_PACKAGE = "izumi.test.domain02.TestAliasServ";
            public static readonly string RTTI_CLASSNAME = "InGetMassCoupons";
            public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.TestAliasServ.InGetMassCoupons";
            public string GetPackageName() { return InGetMassCoupons.RTTI_PACKAGE; }
            public string GetClassName() { return InGetMassCoupons.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InGetMassCoupons.RTTI_FULLCLASSNAME; }

            public Izumi.Test.Domain01.RTestObject1 Iterator { get; set; }

            public InGetMassCoupons() {
            }

            public InGetMassCoupons(Izumi.Test.Domain01.RTestObject1 iterator) {
                this.Iterator = iterator;
            }

        }

        public class InGetMassCoupons_JsonNetConverter: JsonNetConverter<InGetMassCoupons> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InGetMassCoupons_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InGetMassCoupons v, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (v.Iterator != null) {
                    writer.WritePropertyName("iterator");
                    serializer.Serialize(writer, v.Iterator);
                }

                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InGetMassCoupons ReadJson(JsonReader reader, System.Type objectType, InGetMassCoupons existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                Izumi.Test.Domain01.RTestObject1 _iterator = null;
                var _iteratorRaw = json["iterator"];
                if (_iteratorRaw != null && _iteratorRaw.Type != JTokenType.Null) {
                    _iterator = serializer.Deserialize<Izumi.Test.Domain01.RTestObject1>(_iteratorRaw.CreateReader());
                }

                return new InGetMassCoupons(
                    _iterator
                );
            }
        }

        // #i32

        [JsonConverter(typeof(InIfaceMethod_JsonNetConverter))]
        public class InIfaceMethod {
            public static readonly string RTTI_PACKAGE = "izumi.test.domain02.TestAliasServ";
            public static readonly string RTTI_CLASSNAME = "InIfaceMethod";
            public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.TestAliasServ.InIfaceMethod";
            public string GetPackageName() { return InIfaceMethod.RTTI_PACKAGE; }
            public string GetClassName() { return InIfaceMethod.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InIfaceMethod.RTTI_FULLCLASSNAME; }

            public Izumi.Test.Domain01.AnyValTest2 Va { get; set; }

            public InIfaceMethod() {
            }

            public InIfaceMethod(Izumi.Test.Domain01.AnyValTest2 va) {
                this.Va = va;
            }

        }

        public class InIfaceMethod_JsonNetConverter: JsonNetConverter<InIfaceMethod> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InIfaceMethod_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InIfaceMethod v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("va");
                // Serializing polymorphic type AnyValTest2
                writer.WriteStartObject();
                writer.WritePropertyName(v.Va.GetFullClassName());
                serializer.Serialize(writer, v.Va);
                writer.WriteEndObject();

                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InIfaceMethod ReadJson(JsonReader reader, System.Type objectType, InIfaceMethod existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InIfaceMethod(
                    serializer.Deserialize<Izumi.Test.Domain01.AnyValTest2>(json["va"].CreateReader())
                );
            }
        }

        // #i32

        [JsonConverter(typeof(OutTestADTIdReturn_JsonNetConverter))]
        public abstract class OutTestADTIdReturn {
            public interface IOutTestADTIdReturnVisitor {
                void Visit(TestIDReturn visitor);
                void Visit(DTO1 visitor);
            }

            public abstract void Visit(IOutTestADTIdReturnVisitor visitor);
            private OutTestADTIdReturn() {}

            public sealed class TestIDReturn: OutTestADTIdReturn {
                public _TestIDReturn Value { get; private set; }
                public TestIDReturn(_TestIDReturn value) {
                    this.Value = value;
                }

                public override void Visit(IOutTestADTIdReturnVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _TestIDReturn(TestIDReturn m) {
                    return m.Value;
                }

                public static explicit operator TestIDReturn(_TestIDReturn m) {
                    return new TestIDReturn(m);
                }

            }

            public sealed class DTO1: OutTestADTIdReturn {
                public _DTO1 Value { get; private set; }
                public DTO1(_DTO1 value) {
                    this.Value = value;
                }

                public override void Visit(IOutTestADTIdReturnVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _DTO1(DTO1 m) {
                    return m.Value;
                }

                public static explicit operator DTO1(_DTO1 m) {
                    return new DTO1(m);
                }

            }

        }
        public class OutTestADTIdReturn_JsonNetConverter: JsonNetConverter<OutTestADTIdReturn> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutTestADTIdReturn_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutTestADTIdReturn al, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (al is OutTestADTIdReturn.TestIDReturn) {
                    writer.WritePropertyName("TestIDReturn");
                    var v = (al as OutTestADTIdReturn.TestIDReturn).Value;
                    serializer.Serialize(writer, v);
                } else
                if (al is OutTestADTIdReturn.DTO1) {
                    writer.WritePropertyName("DTO1");
                    var v = (al as OutTestADTIdReturn.DTO1).Value;
                    serializer.Serialize(writer, v);
                } else
                {
                    throw new System.Exception("Unknown OutTestADTIdReturn type: " + al);
                }
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutTestADTIdReturn ReadJson(JsonReader reader, System.Type objectType, OutTestADTIdReturn existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var kv = json.Properties().First();
                switch (kv.Name) {
                    case "TestIDReturn": {
                        var v = serializer.Deserialize<Izumi.Test.Domain02.TestIDReturn>(kv.Value.CreateReader());
                        return new OutTestADTIdReturn.TestIDReturn(v);
                    }

                    case "DTO1": {
                        var v = serializer.Deserialize<Izumi.Test.Domain02.DTO1>(kv.Value.CreateReader());
                        return new OutTestADTIdReturn.DTO1(v);
                    }

                    default:
                        throw new System.Exception("Unknown OutTestADTIdReturn type: " + kv.Name);
                }
            }
        }

        [JsonConverter(typeof(OutTestADTIdImportedReturn_JsonNetConverter))]
        public abstract class OutTestADTIdImportedReturn {
            public interface IOutTestADTIdImportedReturnVisitor {
                void Visit(ImportedIDForDomain2 visitor);
                void Visit(DTO1 visitor);
            }

            public abstract void Visit(IOutTestADTIdImportedReturnVisitor visitor);
            private OutTestADTIdImportedReturn() {}

            public sealed class ImportedIDForDomain2: OutTestADTIdImportedReturn {
                public _ImportedIDForDomain2 Value { get; private set; }
                public ImportedIDForDomain2(_ImportedIDForDomain2 value) {
                    this.Value = value;
                }

                public override void Visit(IOutTestADTIdImportedReturnVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _ImportedIDForDomain2(ImportedIDForDomain2 m) {
                    return m.Value;
                }

                public static explicit operator ImportedIDForDomain2(_ImportedIDForDomain2 m) {
                    return new ImportedIDForDomain2(m);
                }

            }

            public sealed class DTO1: OutTestADTIdImportedReturn {
                public _DTO1 Value { get; private set; }
                public DTO1(_DTO1 value) {
                    this.Value = value;
                }

                public override void Visit(IOutTestADTIdImportedReturnVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _DTO1(DTO1 m) {
                    return m.Value;
                }

                public static explicit operator DTO1(_DTO1 m) {
                    return new DTO1(m);
                }

            }

        }
        public class OutTestADTIdImportedReturn_JsonNetConverter: JsonNetConverter<OutTestADTIdImportedReturn> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutTestADTIdImportedReturn_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutTestADTIdImportedReturn al, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (al is OutTestADTIdImportedReturn.ImportedIDForDomain2) {
                    writer.WritePropertyName("ImportedIDForDomain2");
                    var v = (al as OutTestADTIdImportedReturn.ImportedIDForDomain2).Value;
                    serializer.Serialize(writer, v);
                } else
                if (al is OutTestADTIdImportedReturn.DTO1) {
                    writer.WritePropertyName("DTO1");
                    var v = (al as OutTestADTIdImportedReturn.DTO1).Value;
                    serializer.Serialize(writer, v);
                } else
                {
                    throw new System.Exception("Unknown OutTestADTIdImportedReturn type: " + al);
                }
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutTestADTIdImportedReturn ReadJson(JsonReader reader, System.Type objectType, OutTestADTIdImportedReturn existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var kv = json.Properties().First();
                switch (kv.Name) {
                    case "ImportedIDForDomain2": {
                        var v = serializer.Deserialize<Izumi.Test.Domain01.IDForDomain2>(kv.Value.CreateReader());
                        return new OutTestADTIdImportedReturn.ImportedIDForDomain2(v);
                    }

                    case "DTO1": {
                        var v = serializer.Deserialize<Izumi.Test.Domain02.DTO1>(kv.Value.CreateReader());
                        return new OutTestADTIdImportedReturn.DTO1(v);
                    }

                    default:
                        throw new System.Exception("Unknown OutTestADTIdImportedReturn type: " + kv.Name);
                }
            }
        }

    }

    // ============== Service Client ==============
    public interface ITestAliasServClient<C> where C: class, IClientTransportContext {
        void GetMassCoupons(Izumi.Test.Domain01.RTestObject1 iterator, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void IfaceMethod(Izumi.Test.Domain01.AnyValTest2 va, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void TestADTIdReturn(Action<TestAliasServ.OutTestADTIdReturn> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void TestADTIdImportedReturn(Action<TestAliasServ.OutTestADTIdImportedReturn> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
    }

    public class TestAliasServClientGeneric<C>: ITestAliasServClient<C> where C: class, IClientTransportContext {
        public IClientTransport<C> Transport { get; private set; }

        public TestAliasServClientGeneric(IClientTransport<C> t) {
            Transport = t;
        }

        public void SetHTTPTransport(string endpoint, IJsonMarshaller marshaller, bool blocking = false, int timeout = 60) {
            if (blocking) {
                this.Transport = new SyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            } else {
                this.Transport = new AsyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            }
        }
        public void GetMassCoupons(Izumi.Test.Domain01.RTestObject1 iterator, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestAliasServ.InGetMassCoupons(iterator);
            Transport.Send<TestAliasServ.InGetMassCoupons, int>("TestAliasServ", "getMassCoupons", inData,
                new ClientTransportCallback<int>(onSuccess, onFailure, onAny), ctx);
        }

        public void IfaceMethod(Izumi.Test.Domain01.AnyValTest2 va, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestAliasServ.InIfaceMethod(va);
            Transport.Send<TestAliasServ.InIfaceMethod, int>("TestAliasServ", "ifaceMethod", inData,
                new ClientTransportCallback<int>(onSuccess, onFailure, onAny), ctx);
        }

        public void TestADTIdReturn(Action<TestAliasServ.OutTestADTIdReturn> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, TestAliasServ.OutTestADTIdReturn>("TestAliasServ", "testADTIdReturn", null,
                new ClientTransportCallback<TestAliasServ.OutTestADTIdReturn>(onSuccess, onFailure, onAny), ctx);
        }

        public void TestADTIdImportedReturn(Action<TestAliasServ.OutTestADTIdImportedReturn> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, TestAliasServ.OutTestADTIdImportedReturn>("TestAliasServ", "testADTIdImportedReturn", null,
                new ClientTransportCallback<TestAliasServ.OutTestADTIdImportedReturn>(onSuccess, onFailure, onAny), ctx);
        }

    }

    public class TestAliasServClient: TestAliasServClientGeneric<IClientTransportContext> {
        public TestAliasServClient(IClientTransport<IClientTransportContext> t): base(t) {}
    }

    // ============== Service Dispatcher ==============
    public interface ITestAliasServServer<C> {
        int GetMassCoupons(C ctx, Izumi.Test.Domain01.RTestObject1 iterator);
        int IfaceMethod(C ctx, Izumi.Test.Domain01.AnyValTest2 va);
        TestAliasServ.OutTestADTIdReturn TestADTIdReturn(C ctx);
        TestAliasServ.OutTestADTIdImportedReturn TestADTIdImportedReturn(C ctx);
    }

    public class TestAliasServDispatcher<C, D>: IServiceDispatcher<C, D> {
        private static readonly string[] methods = { "getMassCoupons", "ifaceMethod", "testADTIdReturn", "testADTIdImportedReturn" };
        protected IMarshaller<D> marshaller;
        protected ITestAliasServServer<C> server;

        public TestAliasServDispatcher(IMarshaller<D> marshaller, ITestAliasServServer<C> server) {
            this.marshaller = marshaller;
            this.server = server;
        }

        public string GetSupportedService() {
            return "TestAliasServ";
        }

        public string[] GetSupportedMethods() {
            return TestAliasServDispatcher<C, D>.methods;
        }

        public D Dispatch(C ctx, string method, D data) {
            switch(method) {
                case "getMassCoupons": {
                    var obj = marshaller.Unmarshal<TestAliasServ.InGetMassCoupons>(data);
                    return marshaller.Marshal<int>(
                        server.GetMassCoupons(ctx, obj.Iterator)
                    );
                }

                case "ifaceMethod": {
                    var obj = marshaller.Unmarshal<TestAliasServ.InIfaceMethod>(data);
                    return marshaller.Marshal<int>(
                        server.IfaceMethod(ctx, obj.Va)
                    );
                }

                case "testADTIdReturn": {
                    // No input params for this method
                    return marshaller.Marshal<TestAliasServ.OutTestADTIdReturn>(
                        server.TestADTIdReturn(ctx)
                    );
                }

                case "testADTIdImportedReturn": {
                    // No input params for this method
                    return marshaller.Marshal<TestAliasServ.OutTestADTIdImportedReturn>(
                        server.TestADTIdImportedReturn(ctx)
                    );
                }

                default:
                    throw new DispatcherException(string.Format("Method {0} is not supported by TestAliasServDispatcher.", method));
            }
        }
    }

    // ============== Service Server Base ==============
    public abstract class TestAliasServServer<C, D>: TestAliasServDispatcher<C, D>,  ITestAliasServServer<C> {
        public TestAliasServServer(IMarshaller<D> marshaller): base(marshaller, null) {
            server = this;
        }

        public virtual int GetMassCoupons(C ctx, Izumi.Test.Domain01.RTestObject1 iterator) {
            return 0;
        }

        public virtual int IfaceMethod(C ctx, Izumi.Test.Domain01.AnyValTest2 va) {
            return 0;
        }

        public virtual TestAliasServ.OutTestADTIdReturn TestADTIdReturn(C ctx) {
            return null;
        }

        public virtual TestAliasServ.OutTestADTIdImportedReturn TestADTIdImportedReturn(C ctx) {
            return null;
        }

    }
}