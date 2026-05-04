// Auto-generated, any modifications may be overwritten in the future.

using Izumi.Test.Domain01;
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
    using _AdtA2 = Izumi.Test.Domain02.AdtA2;
    using _GenericFailureData = Izumi.Test.Domain01.GenericFailureData;
    using _GenericFailure = Izumi.Test.Domain01.GenericFailure;

    public static class ImportIdService {
        [JsonConverter(typeof(InSome_JsonNetConverter))]
        public class InSome {
            public static readonly string RTTI_PACKAGE = "izumi.test.domain02.ImportIdService";
            public static readonly string RTTI_CLASSNAME = "InSome";
            public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.ImportIdService.InSome";
            public string GetPackageName() { return InSome.RTTI_PACKAGE; }
            public string GetClassName() { return InSome.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSome.RTTI_FULLCLASSNAME; }

            public Izumi.Test.Domain01.ImportAppId Id { get; set; }

            public InSome() {
            }

            public InSome(Izumi.Test.Domain01.ImportAppId id) {
                this.Id = id;
            }

        }

        public class InSome_JsonNetConverter: JsonNetConverter<InSome> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSome_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSome v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("id");
                writer.WriteValue(v.Id.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSome ReadJson(JsonReader reader, System.Type objectType, InSome existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InSome(
                    Izumi.Test.Domain01.ImportAppId.From(json["id"].Value<string>())
                );
            }
        }

        // #u64

        [JsonConverter(typeof(InMixi_JsonNetConverter))]
        public class InMixi {
            public static readonly string RTTI_PACKAGE = "izumi.test.domain02.ImportIdService";
            public static readonly string RTTI_CLASSNAME = "InMixi";
            public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.ImportIdService.InMixi";
            public string GetPackageName() { return InMixi.RTTI_PACKAGE; }
            public string GetClassName() { return InMixi.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InMixi.RTTI_FULLCLASSNAME; }

            public Izumi.Test.Domain01.GenericFailureData Par { get; set; }

            public InMixi() {
            }

            public InMixi(Izumi.Test.Domain01.GenericFailureData par) {
                this.Par = par;
            }

        }

        public class InMixi_JsonNetConverter: JsonNetConverter<InMixi> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InMixi_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InMixi v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("par");
                // Serializing polymorphic type GenericFailureData
                writer.WriteStartObject();
                writer.WritePropertyName(v.Par.GetFullClassName());
                serializer.Serialize(writer, v.Par);
                writer.WriteEndObject();

                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InMixi ReadJson(JsonReader reader, System.Type objectType, InMixi existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InMixi(
                    serializer.Deserialize<Izumi.Test.Domain01.GenericFailureData>(json["par"].CreateReader())
                );
            }
        }

        [JsonConverter(typeof(OutMixi_JsonNetConverter))]
        public abstract class OutMixi {
            public interface IOutMixiVisitor {
                void Visit(AdtA2 visitor);
                void Visit(GenericFailureData visitor);
            }

            public abstract void Visit(IOutMixiVisitor visitor);
            private OutMixi() {}

            public sealed class AdtA2: OutMixi {
                public _AdtA2 Value { get; private set; }
                public AdtA2(_AdtA2 value) {
                    this.Value = value;
                }

                public override void Visit(IOutMixiVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _AdtA2(AdtA2 m) {
                    return m.Value;
                }

                public static explicit operator AdtA2(_AdtA2 m) {
                    return new AdtA2(m);
                }

            }

            public sealed class GenericFailureData: OutMixi {
                public Izumi.Test.Domain01.GenericFailureData Value { get; private set; }
                public GenericFailureData(Izumi.Test.Domain01.GenericFailureData value) {
                    this.Value = value;
                }

                public override void Visit(IOutMixiVisitor visitor) {
                    visitor.Visit(this);
                }

                // We would normally want to have an operator, but unfortunately if it is an interface,
                // it will fail on "user-defined conversions to or from an interface are not allowed".
                // public static explicit operator Izumi.Test.Domain01.GenericFailureData(GenericFailureData m) {
                //     return m.Value;
                // }
                //
                // public static explicit operator GenericFailureData(Izumi.Test.Domain01.GenericFailureData m) {
                //     return new GenericFailureData(m);
                // }

            }

        }
        public class OutMixi_JsonNetConverter: JsonNetConverter<OutMixi> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutMixi_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutMixi al, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (al is OutMixi.AdtA2) {
                    writer.WritePropertyName("AdtA2");
                    var v = (al as OutMixi.AdtA2).Value;
                    serializer.Serialize(writer, v);
                } else
                if (al is OutMixi.GenericFailureData) {
                    writer.WritePropertyName("GenericFailureData");
                    var v = (al as OutMixi.GenericFailureData).Value;
                    // Serializing polymorphic type GenericFailureData
                    writer.WriteStartObject();
                    writer.WritePropertyName(v.GetFullClassName());
                    serializer.Serialize(writer, v);
                    writer.WriteEndObject();

                } else
                {
                    throw new System.Exception("Unknown OutMixi type: " + al);
                }
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutMixi ReadJson(JsonReader reader, System.Type objectType, OutMixi existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var kv = json.Properties().First();
                switch (kv.Name) {
                    case "AdtA2": {
                        var v = serializer.Deserialize<Izumi.Test.Domain02.AdtA2>(kv.Value.CreateReader());
                        return new OutMixi.AdtA2(v);
                    }

                    case "GenericFailureData": {
                        var v = serializer.Deserialize<Izumi.Test.Domain01.GenericFailureData>(kv.Value.CreateReader());
                        return new OutMixi.GenericFailureData(v);
                    }

                    default:
                        throw new System.Exception("Unknown OutMixi type: " + kv.Name);
                }
            }
        }

        [JsonConverter(typeof(InUpdate_JsonNetConverter))]
        public class InUpdate {
            public static readonly string RTTI_PACKAGE = "izumi.test.domain02.ImportIdService";
            public static readonly string RTTI_CLASSNAME = "InUpdate";
            public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain02.ImportIdService.InUpdate";
            public string GetPackageName() { return InUpdate.RTTI_PACKAGE; }
            public string GetClassName() { return InUpdate.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InUpdate.RTTI_FULLCLASSNAME; }

            public Izumi.Test.Domain01.ImportAppId Id { get; set; }

            public InUpdate() {
            }

            public InUpdate(Izumi.Test.Domain01.ImportAppId id) {
                this.Id = id;
            }

        }

        public class InUpdate_JsonNetConverter: JsonNetConverter<InUpdate> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InUpdate_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InUpdate v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("id");
                writer.WriteValue(v.Id.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InUpdate ReadJson(JsonReader reader, System.Type objectType, InUpdate existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InUpdate(
                    Izumi.Test.Domain01.ImportAppId.From(json["id"].Value<string>())
                );
            }
        }

        [JsonConverter(typeof(OutUpdate_JsonNetConverter))]
        public abstract class OutUpdate {
            public interface IOutUpdateVisitor {
                void Visit(AdtA2 visitor);
                void Visit(GenericFailure visitor);
            }

            public abstract void Visit(IOutUpdateVisitor visitor);
            private OutUpdate() {}

            public sealed class AdtA2: OutUpdate {
                public _AdtA2 Value { get; private set; }
                public AdtA2(_AdtA2 value) {
                    this.Value = value;
                }

                public override void Visit(IOutUpdateVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _AdtA2(AdtA2 m) {
                    return m.Value;
                }

                public static explicit operator AdtA2(_AdtA2 m) {
                    return new AdtA2(m);
                }

            }

            public sealed class GenericFailure: OutUpdate {
                public Izumi.Test.Domain01.GenericFailure Value { get; private set; }
                public GenericFailure(Izumi.Test.Domain01.GenericFailure value) {
                    this.Value = value;
                }

                public override void Visit(IOutUpdateVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator Izumi.Test.Domain01.GenericFailure(GenericFailure m) {
                    return m.Value;
                }

                public static explicit operator GenericFailure(Izumi.Test.Domain01.GenericFailure m) {
                    return new GenericFailure(m);
                }

            }

        }
        public class OutUpdate_JsonNetConverter: JsonNetConverter<OutUpdate> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutUpdate_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutUpdate al, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (al is OutUpdate.AdtA2) {
                    writer.WritePropertyName("AdtA2");
                    var v = (al as OutUpdate.AdtA2).Value;
                    serializer.Serialize(writer, v);
                } else
                if (al is OutUpdate.GenericFailure) {
                    writer.WritePropertyName("GenericFailure");
                    var v = (al as OutUpdate.GenericFailure).Value;
                    serializer.Serialize(writer, v);
                } else
                {
                    throw new System.Exception("Unknown OutUpdate type: " + al);
                }
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutUpdate ReadJson(JsonReader reader, System.Type objectType, OutUpdate existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var kv = json.Properties().First();
                switch (kv.Name) {
                    case "AdtA2": {
                        var v = serializer.Deserialize<Izumi.Test.Domain02.AdtA2>(kv.Value.CreateReader());
                        return new OutUpdate.AdtA2(v);
                    }

                    case "GenericFailure": {
                        var v = serializer.Deserialize<Izumi.Test.Domain01.GenericFailure>(kv.Value.CreateReader());
                        return new OutUpdate.GenericFailure(v);
                    }

                    default:
                        throw new System.Exception("Unknown OutUpdate type: " + kv.Name);
                }
            }
        }

    }

    // ============== Service Client ==============
    public interface IImportIdServiceClient<C> where C: class, IClientTransportContext {
        void Some(Izumi.Test.Domain01.ImportAppId id, Action<ulong> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void Mixi(Izumi.Test.Domain01.GenericFailureData par, Action<ImportIdService.OutMixi> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void Update(Izumi.Test.Domain01.ImportAppId id, Action<ImportIdService.OutUpdate> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
    }

    public class ImportIdServiceClientGeneric<C>: IImportIdServiceClient<C> where C: class, IClientTransportContext {
        public IClientTransport<C> Transport { get; private set; }

        public ImportIdServiceClientGeneric(IClientTransport<C> t) {
            Transport = t;
        }

        public void SetHTTPTransport(string endpoint, IJsonMarshaller marshaller, bool blocking = false, int timeout = 60) {
            if (blocking) {
                this.Transport = new SyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            } else {
                this.Transport = new AsyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            }
        }
        public void Some(Izumi.Test.Domain01.ImportAppId id, Action<ulong> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new ImportIdService.InSome(id);
            Transport.Send<ImportIdService.InSome, ulong>("ImportIdService", "some", inData,
                new ClientTransportCallback<ulong>(onSuccess, onFailure, onAny), ctx);
        }

        public void Mixi(Izumi.Test.Domain01.GenericFailureData par, Action<ImportIdService.OutMixi> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new ImportIdService.InMixi(par);
            Transport.Send<ImportIdService.InMixi, ImportIdService.OutMixi>("ImportIdService", "mixi", inData,
                new ClientTransportCallback<ImportIdService.OutMixi>(onSuccess, onFailure, onAny), ctx);
        }

        public void Update(Izumi.Test.Domain01.ImportAppId id, Action<ImportIdService.OutUpdate> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new ImportIdService.InUpdate(id);
            Transport.Send<ImportIdService.InUpdate, ImportIdService.OutUpdate>("ImportIdService", "update", inData,
                new ClientTransportCallback<ImportIdService.OutUpdate>(onSuccess, onFailure, onAny), ctx);
        }

    }

    public class ImportIdServiceClient: ImportIdServiceClientGeneric<IClientTransportContext> {
        public ImportIdServiceClient(IClientTransport<IClientTransportContext> t): base(t) {}
    }

    // ============== Service Dispatcher ==============
    public interface IImportIdServiceServer<C> {
        ulong Some(C ctx, Izumi.Test.Domain01.ImportAppId id);
        ImportIdService.OutMixi Mixi(C ctx, Izumi.Test.Domain01.GenericFailureData par);
        ImportIdService.OutUpdate Update(C ctx, Izumi.Test.Domain01.ImportAppId id);
    }

    public class ImportIdServiceDispatcher<C, D>: IServiceDispatcher<C, D> {
        private static readonly string[] methods = { "some", "mixi", "update" };
        protected IMarshaller<D> marshaller;
        protected IImportIdServiceServer<C> server;

        public ImportIdServiceDispatcher(IMarshaller<D> marshaller, IImportIdServiceServer<C> server) {
            this.marshaller = marshaller;
            this.server = server;
        }

        public string GetSupportedService() {
            return "ImportIdService";
        }

        public string[] GetSupportedMethods() {
            return ImportIdServiceDispatcher<C, D>.methods;
        }

        public D Dispatch(C ctx, string method, D data) {
            switch(method) {
                case "some": {
                    var obj = marshaller.Unmarshal<ImportIdService.InSome>(data);
                    return marshaller.Marshal<ulong>(
                        server.Some(ctx, obj.Id)
                    );
                }

                case "mixi": {
                    var obj = marshaller.Unmarshal<ImportIdService.InMixi>(data);
                    return marshaller.Marshal<ImportIdService.OutMixi>(
                        server.Mixi(ctx, obj.Par)
                    );
                }

                case "update": {
                    var obj = marshaller.Unmarshal<ImportIdService.InUpdate>(data);
                    return marshaller.Marshal<ImportIdService.OutUpdate>(
                        server.Update(ctx, obj.Id)
                    );
                }

                default:
                    throw new DispatcherException(string.Format("Method {0} is not supported by ImportIdServiceDispatcher.", method));
            }
        }
    }

    // ============== Service Server Base ==============
    public abstract class ImportIdServiceServer<C, D>: ImportIdServiceDispatcher<C, D>,  IImportIdServiceServer<C> {
        public ImportIdServiceServer(IMarshaller<D> marshaller): base(marshaller, null) {
            server = this;
        }

        public virtual ulong Some(C ctx, Izumi.Test.Domain01.ImportAppId id) {
            return 0;
        }

        public virtual ImportIdService.OutMixi Mixi(C ctx, Izumi.Test.Domain01.GenericFailureData par) {
            return null;
        }

        public virtual ImportIdService.OutUpdate Update(C ctx, Izumi.Test.Domain01.ImportAppId id) {
            return null;
        }

    }
}