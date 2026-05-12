// Auto-generated, any modifications may be overwritten in the future.

using IRT;
using IRT.Marshaller;
using IRT.Transport.Client;
using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Linq;

namespace Idltest.Events {
    public static class TestBuzzer {
        [JsonConverter(typeof(InUserRegistered_JsonNetConverter))]
        public class InUserRegistered {
            public static readonly string RTTI_PACKAGE = "idltest.events.TestBuzzer";
            public static readonly string RTTI_CLASSNAME = "InUserRegistered";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.events.TestBuzzer.InUserRegistered";
            public string GetPackageName() { return InUserRegistered.RTTI_PACKAGE; }
            public string GetClassName() { return InUserRegistered.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InUserRegistered.RTTI_FULLCLASSNAME; }

            public string FirstName { get; set; }
            public string SecondName { get; set; }

            public InUserRegistered() {
            }

            public InUserRegistered(string firstName, string secondName) {
                this.FirstName = firstName;
                this.SecondName = secondName;
            }

        }

        public class InUserRegistered_JsonNetConverter: JsonNetConverter<InUserRegistered> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InUserRegistered_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InUserRegistered v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("firstName");
                writer.WriteValue(v.FirstName);
                writer.WritePropertyName("secondName");
                writer.WriteValue(v.SecondName);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InUserRegistered ReadJson(JsonReader reader, System.Type objectType, InUserRegistered existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InUserRegistered(
                    json["firstName"].Value<string>(), 
                    json["secondName"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(InHello_JsonNetConverter))]
        public class InHello {
            public static readonly string RTTI_PACKAGE = "idltest.events.TestBuzzer";
            public static readonly string RTTI_CLASSNAME = "InHello";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.events.TestBuzzer.InHello";
            public string GetPackageName() { return InHello.RTTI_PACKAGE; }
            public string GetClassName() { return InHello.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InHello.RTTI_FULLCLASSNAME; }

            public string Name { get; set; }

            public InHello() {
            }

            public InHello(string name) {
                this.Name = name;
            }

        }

        public class InHello_JsonNetConverter: JsonNetConverter<InHello> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InHello_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InHello v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("name");
                writer.WriteValue(v.Name);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InHello ReadJson(JsonReader reader, System.Type objectType, InHello existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InHello(
                    json["name"].Value<string>()
                );
            }
        }

        // #str

        [JsonConverter(typeof(InEnumInput_JsonNetConverter))]
        public class InEnumInput {
            public static readonly string RTTI_PACKAGE = "idltest.events.TestBuzzer";
            public static readonly string RTTI_CLASSNAME = "InEnumInput";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.events.TestBuzzer.InEnumInput";
            public string GetPackageName() { return InEnumInput.RTTI_PACKAGE; }
            public string GetClassName() { return InEnumInput.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InEnumInput.RTTI_FULLCLASSNAME; }

            public Idltest.Events.EnumType Value { get; set; }

            public InEnumInput() {
            }

            public InEnumInput(Idltest.Events.EnumType value) {
                this.Value = value;
            }

        }

        public class InEnumInput_JsonNetConverter: JsonNetConverter<InEnumInput> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InEnumInput_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InEnumInput v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("value");
                writer.WriteValue(v.Value.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InEnumInput ReadJson(JsonReader reader, System.Type objectType, InEnumInput existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InEnumInput(
                    Idltest.Events.EnumTypeHelpers.From(json["value"].Value<string>())
                );
            }
        }

        // #str

        [JsonConverter(typeof(InEnumInputVoid_JsonNetConverter))]
        public class InEnumInputVoid {
            public static readonly string RTTI_PACKAGE = "idltest.events.TestBuzzer";
            public static readonly string RTTI_CLASSNAME = "InEnumInputVoid";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.events.TestBuzzer.InEnumInputVoid";
            public string GetPackageName() { return InEnumInputVoid.RTTI_PACKAGE; }
            public string GetClassName() { return InEnumInputVoid.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InEnumInputVoid.RTTI_FULLCLASSNAME; }

            public Idltest.Events.EnumType Value { get; set; }

            public InEnumInputVoid() {
            }

            public InEnumInputVoid(Idltest.Events.EnumType value) {
                this.Value = value;
            }

        }

        public class InEnumInputVoid_JsonNetConverter: JsonNetConverter<InEnumInputVoid> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InEnumInputVoid_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InEnumInputVoid v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("value");
                writer.WriteValue(v.Value.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InEnumInputVoid ReadJson(JsonReader reader, System.Type objectType, InEnumInputVoid existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InEnumInputVoid(
                    Idltest.Events.EnumTypeHelpers.From(json["value"].Value<string>())
                );
            }
        }

        [JsonConverter(typeof(InAdtInput_JsonNetConverter))]
        public class InAdtInput {
            public static readonly string RTTI_PACKAGE = "idltest.events.TestBuzzer";
            public static readonly string RTTI_CLASSNAME = "InAdtInput";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.events.TestBuzzer.InAdtInput";
            public string GetPackageName() { return InAdtInput.RTTI_PACKAGE; }
            public string GetClassName() { return InAdtInput.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InAdtInput.RTTI_FULLCLASSNAME; }

            public Idltest.Events.ADTType Value { get; set; }

            public InAdtInput() {
            }

            public InAdtInput(Idltest.Events.ADTType value) {
                this.Value = value;
            }

        }

        public class InAdtInput_JsonNetConverter: JsonNetConverter<InAdtInput> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InAdtInput_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InAdtInput v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("value");
                serializer.Serialize(writer, v.Value);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InAdtInput ReadJson(JsonReader reader, System.Type objectType, InAdtInput existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InAdtInput(
                    serializer.Deserialize<Idltest.Events.ADTType>(json["value"].CreateReader())
                );
            }
        }

        // #str

        [JsonConverter(typeof(InAdtInputVoid_JsonNetConverter))]
        public class InAdtInputVoid {
            public static readonly string RTTI_PACKAGE = "idltest.events.TestBuzzer";
            public static readonly string RTTI_CLASSNAME = "InAdtInputVoid";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.events.TestBuzzer.InAdtInputVoid";
            public string GetPackageName() { return InAdtInputVoid.RTTI_PACKAGE; }
            public string GetClassName() { return InAdtInputVoid.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InAdtInputVoid.RTTI_FULLCLASSNAME; }

            public Idltest.Events.ADTType Value { get; set; }

            public InAdtInputVoid() {
            }

            public InAdtInputVoid(Idltest.Events.ADTType value) {
                this.Value = value;
            }

        }

        public class InAdtInputVoid_JsonNetConverter: JsonNetConverter<InAdtInputVoid> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InAdtInputVoid_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InAdtInputVoid v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("value");
                serializer.Serialize(writer, v.Value);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InAdtInputVoid ReadJson(JsonReader reader, System.Type objectType, InAdtInputVoid existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InAdtInputVoid(
                    serializer.Deserialize<Idltest.Events.ADTType>(json["value"].CreateReader())
                );
            }
        }

    }

    // ============== Client ==============
    public interface ITestBuzzerClient<C> where C: class, IClientTransportContext {
        void Empty(Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void UserRegistered(string firstName, string secondName, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void Hello(string name, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void EnumInput(Idltest.Events.EnumType value, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void EnumInputVoid(Idltest.Events.EnumType value, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void AdtInput(Idltest.Events.ADTType value, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void AdtInputVoid(Idltest.Events.ADTType value, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
    }

    public class TestBuzzerClientGeneric<C, D>: ITestBuzzerClient<C> where C: class, IClientTransportContext {
        public IClientSocketTransport<C, D> Transport { get; private set; }

        public TestBuzzerClientGeneric(IClientSocketTransport<C, D> t) {
            Transport = t;
        }

        public void Empty(Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, IRT.Void>("TestBuzzer", "empty", null,
                new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
        }

        public void UserRegistered(string firstName, string secondName, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestBuzzer.InUserRegistered(firstName, secondName);
            Transport.Send<TestBuzzer.InUserRegistered, IRT.Void>("TestBuzzer", "userRegistered", inData,
                new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
        }

        public void Hello(string name, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestBuzzer.InHello(name);
            Transport.Send<TestBuzzer.InHello, string>("TestBuzzer", "hello", inData,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void EnumInput(Idltest.Events.EnumType value, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestBuzzer.InEnumInput(value);
            Transport.Send<TestBuzzer.InEnumInput, string>("TestBuzzer", "enumInput", inData,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void EnumInputVoid(Idltest.Events.EnumType value, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestBuzzer.InEnumInputVoid(value);
            Transport.Send<TestBuzzer.InEnumInputVoid, IRT.Void>("TestBuzzer", "enumInputVoid", inData,
                new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
        }

        public void AdtInput(Idltest.Events.ADTType value, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestBuzzer.InAdtInput(value);
            Transport.Send<TestBuzzer.InAdtInput, string>("TestBuzzer", "adtInput", inData,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void AdtInputVoid(Idltest.Events.ADTType value, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestBuzzer.InAdtInputVoid(value);
            Transport.Send<TestBuzzer.InAdtInputVoid, IRT.Void>("TestBuzzer", "adtInputVoid", inData,
                new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
        }

    }

    public class TestBuzzerClient: TestBuzzerClientGeneric<IClientTransportContext, string> {
        public TestBuzzerClient(IClientSocketTransport<IClientTransportContext, string> t): base(t) {}
    }

    // ============== Dispatcher ==============
    public interface ITestBuzzerBuzzerHandlers<C> {
        void Empty(C ctx);
        void UserRegistered(C ctx, string firstName, string secondName);
        string Hello(C ctx, string name);
        string EnumInput(C ctx, Idltest.Events.EnumType value);
        void EnumInputVoid(C ctx, Idltest.Events.EnumType value);
        string AdtInput(C ctx, Idltest.Events.ADTType value);
        void AdtInputVoid(C ctx, Idltest.Events.ADTType value);
    }

    public class TestBuzzerDispatcher<C, D>: IServiceDispatcher<C, D> {
        private static readonly string[] methods = { "empty", "userRegistered", "hello", "enumInput", "enumInputVoid", "adtInput", "adtInputVoid" };
        protected IMarshaller<D> marshaller;
        protected ITestBuzzerBuzzerHandlers<C> handlers;

        public TestBuzzerDispatcher(IMarshaller<D> marshaller, ITestBuzzerBuzzerHandlers<C> handlers) {
            this.marshaller = marshaller;
            this.handlers = handlers;
        }

        public string GetSupportedService() {
            return "TestBuzzer";
        }

        public string[] GetSupportedMethods() {
            return TestBuzzerDispatcher<C, D>.methods;
        }

        public D Dispatch(C ctx, string method, D data) {
            switch(method) {
                case "empty": {
                    // No input params for this method
                    handlers.Empty(ctx);
                    return marshaller.Marshal<IRT.Void>(null);
                }

                case "userRegistered": {
                    var obj = marshaller.Unmarshal<TestBuzzer.InUserRegistered>(data);
                    handlers.UserRegistered(ctx, obj.FirstName, obj.SecondName);
                    return marshaller.Marshal<IRT.Void>(null);
                }

                case "hello": {
                    var obj = marshaller.Unmarshal<TestBuzzer.InHello>(data);
                    return marshaller.Marshal<string>(
                        handlers.Hello(ctx, obj.Name)
                    );
                }

                case "enumInput": {
                    var obj = marshaller.Unmarshal<TestBuzzer.InEnumInput>(data);
                    return marshaller.Marshal<string>(
                        handlers.EnumInput(ctx, obj.Value)
                    );
                }

                case "enumInputVoid": {
                    var obj = marshaller.Unmarshal<TestBuzzer.InEnumInputVoid>(data);
                    handlers.EnumInputVoid(ctx, obj.Value);
                    return marshaller.Marshal<IRT.Void>(null);
                }

                case "adtInput": {
                    var obj = marshaller.Unmarshal<TestBuzzer.InAdtInput>(data);
                    return marshaller.Marshal<string>(
                        handlers.AdtInput(ctx, obj.Value)
                    );
                }

                case "adtInputVoid": {
                    var obj = marshaller.Unmarshal<TestBuzzer.InAdtInputVoid>(data);
                    handlers.AdtInputVoid(ctx, obj.Value);
                    return marshaller.Marshal<IRT.Void>(null);
                }

                default:
                    throw new DispatcherException(string.Format("Method {0} is not supported by TestBuzzerDispatcher.", method));
            }
        }
    }

    // ============== Buzzer Handlers Base ==============
    public abstract class TestBuzzerBuzzerHandlers<C, D>: TestBuzzerDispatcher<C, D>,  ITestBuzzerBuzzerHandlers<C> {
        public TestBuzzerBuzzerHandlers(IMarshaller<D> marshaller): base(marshaller, null) {
            handlers = this;
        }

        public virtual void Empty(C ctx) {
            // Nothing to return
        }

        public virtual void UserRegistered(C ctx, string firstName, string secondName) {
            // Nothing to return
        }

        public virtual string Hello(C ctx, string name) {
            return null;
        }

        public virtual string EnumInput(C ctx, Idltest.Events.EnumType value) {
            return null;
        }

        public virtual void EnumInputVoid(C ctx, Idltest.Events.EnumType value) {
            // Nothing to return
        }

        public virtual string AdtInput(C ctx, Idltest.Events.ADTType value) {
            return null;
        }

        public virtual void AdtInputVoid(C ctx, Idltest.Events.ADTType value) {
            // Nothing to return
        }

    }
}