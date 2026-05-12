// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using IRT;
using IRT.Marshaller;
using IRT.Transport.Client;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Linq;

namespace Idltest.Services {
    using _SuccessDataData = Idltest.Services.SuccessDataData;
    using _ErrorData = Idltest.Services.ErrorData;

    public static class TestService {
        [JsonConverter(typeof(OutAnotherVoid_JsonNetConverter))]
        public class OutAnotherVoid {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "OutAnotherVoid";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.OutAnotherVoid";
            public string GetPackageName() { return OutAnotherVoid.RTTI_PACKAGE; }
            public string GetClassName() { return OutAnotherVoid.RTTI_CLASSNAME; }
            public string GetFullClassName() { return OutAnotherVoid.RTTI_FULLCLASSNAME; }

            public OutAnotherVoid() {
            }

        }

        public class OutAnotherVoid_JsonNetConverter: JsonNetConverter<OutAnotherVoid> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutAnotherVoid_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutAnotherVoid v, JsonSerializer serializer) {
                writer.WriteStartObject();

                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutAnotherVoid ReadJson(JsonReader reader, System.Type objectType, OutAnotherVoid existingValue, bool hasExistingValue, JsonSerializer serializer) {
                reader.Skip();

                return new OutAnotherVoid(

                );
            }
        }

        [JsonConverter(typeof(InUnitResult_JsonNetConverter))]
        public class InUnitResult {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InUnitResult";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InUnitResult";
            public string GetPackageName() { return InUnitResult.RTTI_PACKAGE; }
            public string GetClassName() { return InUnitResult.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InUnitResult.RTTI_FULLCLASSNAME; }

            public Idltest.Services.Package Package { get; set; }

            public InUnitResult() {
            }

            public InUnitResult(Idltest.Services.Package package) {
                this.Package = package;
            }

        }

        public class InUnitResult_JsonNetConverter: JsonNetConverter<InUnitResult> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InUnitResult_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InUnitResult v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("package");
                serializer.Serialize(writer, v.Package);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InUnitResult ReadJson(JsonReader reader, System.Type objectType, InUnitResult existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var _package = serializer.Deserialize<Idltest.Services.Package>(json["package"].CreateReader());
                return new InUnitResult(
                    _package
                );
            }
        }

        // #str

        [JsonConverter(typeof(InSimpleMethod_JsonNetConverter))]
        public class InSimpleMethod {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InSimpleMethod";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InSimpleMethod";
            public string GetPackageName() { return InSimpleMethod.RTTI_PACKAGE; }
            public string GetClassName() { return InSimpleMethod.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSimpleMethod.RTTI_FULLCLASSNAME; }

            public string A { get; set; }

            public InSimpleMethod() {
            }

            public InSimpleMethod(string a) {
                this.A = a;
            }

        }

        public class InSimpleMethod_JsonNetConverter: JsonNetConverter<InSimpleMethod> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSimpleMethod_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSimpleMethod v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("a");
                writer.WriteValue(v.A);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSimpleMethod ReadJson(JsonReader reader, System.Type objectType, InSimpleMethod existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InSimpleMethod(
                    json["a"].Value<string>()
                );
            }
        }

        // #str

        [JsonConverter(typeof(InSimpleIntMethod_JsonNetConverter))]
        public class InSimpleIntMethod {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InSimpleIntMethod";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InSimpleIntMethod";
            public string GetPackageName() { return InSimpleIntMethod.RTTI_PACKAGE; }
            public string GetClassName() { return InSimpleIntMethod.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSimpleIntMethod.RTTI_FULLCLASSNAME; }

            public int A { get; set; }

            public InSimpleIntMethod() {
            }

            public InSimpleIntMethod(int a) {
                this.A = a;
            }

        }

        public class InSimpleIntMethod_JsonNetConverter: JsonNetConverter<InSimpleIntMethod> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSimpleIntMethod_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSimpleIntMethod v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("a");
                writer.WriteValue(v.A);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSimpleIntMethod ReadJson(JsonReader reader, System.Type objectType, InSimpleIntMethod existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InSimpleIntMethod(
                    json["a"].Value<int>()
                );
            }
        }

        // #i32

        [JsonConverter(typeof(InSimpleMethodWithGenerics_JsonNetConverter))]
        public class InSimpleMethodWithGenerics {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InSimpleMethodWithGenerics";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InSimpleMethodWithGenerics";
            public string GetPackageName() { return InSimpleMethodWithGenerics.RTTI_PACKAGE; }
            public string GetClassName() { return InSimpleMethodWithGenerics.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSimpleMethodWithGenerics.RTTI_FULLCLASSNAME; }

            public List<string> A { get; set; }

            public InSimpleMethodWithGenerics() {
                A = new List<string>();
            }

            public InSimpleMethodWithGenerics(List<string> a) {
                this.A = a;
            }

        }

        public class InSimpleMethodWithGenerics_JsonNetConverter: JsonNetConverter<InSimpleMethodWithGenerics> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSimpleMethodWithGenerics_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSimpleMethodWithGenerics v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("a");
                writer.WriteStartArray();
                foreach (var lv in v.A) {
                    writer.WriteValue(lv);
                }
                writer.WriteEndArray();

                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSimpleMethodWithGenerics ReadJson(JsonReader reader, System.Type objectType, InSimpleMethodWithGenerics existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var _a = new List<string>();
                foreach (var _a_sv in (JArray)json["a"]) {
                    string _a_d;
                    _a_d = _a_sv.Value<string>();
                    _a.Add(_a_d);
                }

                return new InSimpleMethodWithGenerics(
                    _a
                );
            }
        }

        // #lst

        [JsonConverter(typeof(OutSimple_JsonNetConverter))]
        public class OutSimple {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "OutSimple";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.OutSimple";
            public string GetPackageName() { return OutSimple.RTTI_PACKAGE; }
            public string GetClassName() { return OutSimple.RTTI_CLASSNAME; }
            public string GetFullClassName() { return OutSimple.RTTI_FULLCLASSNAME; }

            public OutSimple() {
            }

        }

        public class OutSimple_JsonNetConverter: JsonNetConverter<OutSimple> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutSimple_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutSimple v, JsonSerializer serializer) {
                writer.WriteStartObject();

                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutSimple ReadJson(JsonReader reader, System.Type objectType, OutSimple existingValue, bool hasExistingValue, JsonSerializer serializer) {
                reader.Skip();

                return new OutSimple(

                );
            }
        }

        [JsonConverter(typeof(InSimpleEnum_JsonNetConverter))]
        public class InSimpleEnum {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InSimpleEnum";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InSimpleEnum";
            public string GetPackageName() { return InSimpleEnum.RTTI_PACKAGE; }
            public string GetClassName() { return InSimpleEnum.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSimpleEnum.RTTI_FULLCLASSNAME; }

            public Idltest.Services.TestServiceEnum V { get; set; }

            public InSimpleEnum() {
            }

            public InSimpleEnum(Idltest.Services.TestServiceEnum v) {
                this.V = v;
            }

        }

        public class InSimpleEnum_JsonNetConverter: JsonNetConverter<InSimpleEnum> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSimpleEnum_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSimpleEnum v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("v");
                writer.WriteValue(v.V.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSimpleEnum ReadJson(JsonReader reader, System.Type objectType, InSimpleEnum existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InSimpleEnum(
                    Idltest.Services.TestServiceEnumHelpers.From(json["v"].Value<string>())
                );
            }
        }

        // #str

        [JsonConverter(typeof(InSimpleEnum2_JsonNetConverter))]
        public class InSimpleEnum2 {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InSimpleEnum2";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InSimpleEnum2";
            public string GetPackageName() { return InSimpleEnum2.RTTI_PACKAGE; }
            public string GetClassName() { return InSimpleEnum2.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSimpleEnum2.RTTI_FULLCLASSNAME; }

            public Idltest.Services.Environment E { get; set; }

            public InSimpleEnum2() {
            }

            public InSimpleEnum2(Idltest.Services.Environment e) {
                this.E = e;
            }

        }

        public class InSimpleEnum2_JsonNetConverter: JsonNetConverter<InSimpleEnum2> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSimpleEnum2_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSimpleEnum2 v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("e");
                writer.WriteValue(v.E.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSimpleEnum2 ReadJson(JsonReader reader, System.Type objectType, InSimpleEnum2 existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InSimpleEnum2(
                    Idltest.Services.EnvironmentHelpers.From(json["e"].Value<string>())
                );
            }
        }

        // #str

        [JsonConverter(typeof(InReturnsList_JsonNetConverter))]
        public class InReturnsList {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InReturnsList";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InReturnsList";
            public string GetPackageName() { return InReturnsList.RTTI_PACKAGE; }
            public string GetClassName() { return InReturnsList.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InReturnsList.RTTI_FULLCLASSNAME; }

            public Idltest.Services.Environment E { get; set; }

            public InReturnsList() {
            }

            public InReturnsList(Idltest.Services.Environment e) {
                this.E = e;
            }

        }

        public class InReturnsList_JsonNetConverter: JsonNetConverter<InReturnsList> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InReturnsList_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InReturnsList v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("e");
                writer.WriteValue(v.E.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InReturnsList ReadJson(JsonReader reader, System.Type objectType, InReturnsList existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InReturnsList(
                    Idltest.Services.EnvironmentHelpers.From(json["e"].Value<string>())
                );
            }
        }

        // #lst

        [JsonConverter(typeof(InReturnsMap_JsonNetConverter))]
        public class InReturnsMap {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InReturnsMap";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InReturnsMap";
            public string GetPackageName() { return InReturnsMap.RTTI_PACKAGE; }
            public string GetClassName() { return InReturnsMap.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InReturnsMap.RTTI_FULLCLASSNAME; }

            public Idltest.Services.Environment E { get; set; }

            public InReturnsMap() {
            }

            public InReturnsMap(Idltest.Services.Environment e) {
                this.E = e;
            }

        }

        public class InReturnsMap_JsonNetConverter: JsonNetConverter<InReturnsMap> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InReturnsMap_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InReturnsMap v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("e");
                writer.WriteValue(v.E.ToString());
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InReturnsMap ReadJson(JsonReader reader, System.Type objectType, InReturnsMap existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InReturnsMap(
                    Idltest.Services.EnvironmentHelpers.From(json["e"].Value<string>())
                );
            }
        }

        // #map

        [JsonConverter(typeof(InSimpleGoReserved_JsonNetConverter))]
        public class InSimpleGoReserved {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InSimpleGoReserved";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InSimpleGoReserved";
            public string GetPackageName() { return InSimpleGoReserved.RTTI_PACKAGE; }
            public string GetClassName() { return InSimpleGoReserved.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSimpleGoReserved.RTTI_FULLCLASSNAME; }

            public Idltest.Services.Package Package { get; set; }

            public InSimpleGoReserved() {
            }

            public InSimpleGoReserved(Idltest.Services.Package package) {
                this.Package = package;
            }

        }

        public class InSimpleGoReserved_JsonNetConverter: JsonNetConverter<InSimpleGoReserved> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSimpleGoReserved_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSimpleGoReserved v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("package");
                serializer.Serialize(writer, v.Package);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSimpleGoReserved ReadJson(JsonReader reader, System.Type objectType, InSimpleGoReserved existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var _package = serializer.Deserialize<Idltest.Services.Package>(json["package"].CreateReader());
                return new InSimpleGoReserved(
                    _package
                );
            }
        }

        // #bit

        [JsonConverter(typeof(InSimpleVoid_JsonNetConverter))]
        public class InSimpleVoid {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InSimpleVoid";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InSimpleVoid";
            public string GetPackageName() { return InSimpleVoid.RTTI_PACKAGE; }
            public string GetClassName() { return InSimpleVoid.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InSimpleVoid.RTTI_FULLCLASSNAME; }

            public string A { get; set; }

            public InSimpleVoid() {
            }

            public InSimpleVoid(string a) {
                this.A = a;
            }

        }

        public class InSimpleVoid_JsonNetConverter: JsonNetConverter<InSimpleVoid> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InSimpleVoid_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InSimpleVoid v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("a");
                writer.WriteValue(v.A);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InSimpleVoid ReadJson(JsonReader reader, System.Type objectType, InSimpleVoid existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InSimpleVoid(
                    json["a"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(InGreetSingularOut_JsonNetConverter))]
        public class InGreetSingularOut {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InGreetSingularOut";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InGreetSingularOut";
            public string GetPackageName() { return InGreetSingularOut.RTTI_PACKAGE; }
            public string GetClassName() { return InGreetSingularOut.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InGreetSingularOut.RTTI_FULLCLASSNAME; }

            public string FirstName { get; set; }
            public string SecondName { get; set; }

            public InGreetSingularOut() {
            }

            public InGreetSingularOut(string firstName, string secondName) {
                this.FirstName = firstName;
                this.SecondName = secondName;
            }

        }

        public class InGreetSingularOut_JsonNetConverter: JsonNetConverter<InGreetSingularOut> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InGreetSingularOut_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InGreetSingularOut v, JsonSerializer serializer) {
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
            public override InGreetSingularOut ReadJson(JsonReader reader, System.Type objectType, InGreetSingularOut existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InGreetSingularOut(
                    json["firstName"].Value<string>(), 
                    json["secondName"].Value<string>()
                );
            }
        }

        // #str

        [JsonConverter(typeof(InGreetImplicitStructOut_JsonNetConverter))]
        public class InGreetImplicitStructOut {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InGreetImplicitStructOut";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InGreetImplicitStructOut";
            public string GetPackageName() { return InGreetImplicitStructOut.RTTI_PACKAGE; }
            public string GetClassName() { return InGreetImplicitStructOut.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InGreetImplicitStructOut.RTTI_FULLCLASSNAME; }

            public string FirstName { get; set; }
            public string SecondName { get; set; }

            public InGreetImplicitStructOut() {
            }

            public InGreetImplicitStructOut(string firstName, string secondName) {
                this.FirstName = firstName;
                this.SecondName = secondName;
            }

        }

        public class InGreetImplicitStructOut_JsonNetConverter: JsonNetConverter<InGreetImplicitStructOut> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InGreetImplicitStructOut_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InGreetImplicitStructOut v, JsonSerializer serializer) {
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
            public override InGreetImplicitStructOut ReadJson(JsonReader reader, System.Type objectType, InGreetImplicitStructOut existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InGreetImplicitStructOut(
                    json["firstName"].Value<string>(), 
                    json["secondName"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(OutGreetImplicitStructOut_JsonNetConverter))]
        public class OutGreetImplicitStructOut {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "OutGreetImplicitStructOut";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.OutGreetImplicitStructOut";
            public string GetPackageName() { return OutGreetImplicitStructOut.RTTI_PACKAGE; }
            public string GetClassName() { return OutGreetImplicitStructOut.RTTI_CLASSNAME; }
            public string GetFullClassName() { return OutGreetImplicitStructOut.RTTI_FULLCLASSNAME; }

            public string A { get; set; }

            public OutGreetImplicitStructOut() {
            }

            public OutGreetImplicitStructOut(string a) {
                this.A = a;
            }

        }

        public class OutGreetImplicitStructOut_JsonNetConverter: JsonNetConverter<OutGreetImplicitStructOut> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutGreetImplicitStructOut_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutGreetImplicitStructOut v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("a");
                writer.WriteValue(v.A);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutGreetImplicitStructOut ReadJson(JsonReader reader, System.Type objectType, OutGreetImplicitStructOut existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new OutGreetImplicitStructOut(
                    json["a"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(InGreetImplicitStructMultilineSyntax_JsonNetConverter))]
        public class InGreetImplicitStructMultilineSyntax {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InGreetImplicitStructMultilineSyntax";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InGreetImplicitStructMultilineSyntax";
            public string GetPackageName() { return InGreetImplicitStructMultilineSyntax.RTTI_PACKAGE; }
            public string GetClassName() { return InGreetImplicitStructMultilineSyntax.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InGreetImplicitStructMultilineSyntax.RTTI_FULLCLASSNAME; }

            public string Region { get; set; }
            public sbyte Age { get; set; }

            public InGreetImplicitStructMultilineSyntax() {
            }

            public InGreetImplicitStructMultilineSyntax(string region, sbyte age) {
                this.Region = region;
                this.Age = age;
            }

        }

        public class InGreetImplicitStructMultilineSyntax_JsonNetConverter: JsonNetConverter<InGreetImplicitStructMultilineSyntax> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InGreetImplicitStructMultilineSyntax_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InGreetImplicitStructMultilineSyntax v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("region");
                writer.WriteValue(v.Region);
                writer.WritePropertyName("age");
                writer.WriteValue(v.Age);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InGreetImplicitStructMultilineSyntax ReadJson(JsonReader reader, System.Type objectType, InGreetImplicitStructMultilineSyntax existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InGreetImplicitStructMultilineSyntax(
                    json["region"].Value<string>(), 
                    json["age"].Value<sbyte>()
                );
            }
        }

        [JsonConverter(typeof(OutGreetImplicitStructMultilineSyntax_JsonNetConverter))]
        public class OutGreetImplicitStructMultilineSyntax {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "OutGreetImplicitStructMultilineSyntax";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.OutGreetImplicitStructMultilineSyntax";
            public string GetPackageName() { return OutGreetImplicitStructMultilineSyntax.RTTI_PACKAGE; }
            public string GetClassName() { return OutGreetImplicitStructMultilineSyntax.RTTI_CLASSNAME; }
            public string GetFullClassName() { return OutGreetImplicitStructMultilineSyntax.RTTI_FULLCLASSNAME; }

            public string Bullshit { get; set; }

            public OutGreetImplicitStructMultilineSyntax() {
            }

            public OutGreetImplicitStructMultilineSyntax(string bullshit) {
                this.Bullshit = bullshit;
            }

        }

        public class OutGreetImplicitStructMultilineSyntax_JsonNetConverter: JsonNetConverter<OutGreetImplicitStructMultilineSyntax> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutGreetImplicitStructMultilineSyntax_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutGreetImplicitStructMultilineSyntax v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("bullshit");
                writer.WriteValue(v.Bullshit);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutGreetImplicitStructMultilineSyntax ReadJson(JsonReader reader, System.Type objectType, OutGreetImplicitStructMultilineSyntax existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new OutGreetImplicitStructMultilineSyntax(
                    json["bullshit"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(InGreetImplicitStructureMultilineCurlyBracesSyntax_JsonNetConverter))]
        public class InGreetImplicitStructureMultilineCurlyBracesSyntax {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InGreetImplicitStructureMultilineCurlyBracesSyntax";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InGreetImplicitStructureMultilineCurlyBracesSyntax";
            public string GetPackageName() { return InGreetImplicitStructureMultilineCurlyBracesSyntax.RTTI_PACKAGE; }
            public string GetClassName() { return InGreetImplicitStructureMultilineCurlyBracesSyntax.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InGreetImplicitStructureMultilineCurlyBracesSyntax.RTTI_FULLCLASSNAME; }

            public string Region { get; set; }
            public sbyte Age { get; set; }

            public InGreetImplicitStructureMultilineCurlyBracesSyntax() {
            }

            public InGreetImplicitStructureMultilineCurlyBracesSyntax(string region, sbyte age) {
                this.Region = region;
                this.Age = age;
            }

        }

        public class InGreetImplicitStructureMultilineCurlyBracesSyntax_JsonNetConverter: JsonNetConverter<InGreetImplicitStructureMultilineCurlyBracesSyntax> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InGreetImplicitStructureMultilineCurlyBracesSyntax_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InGreetImplicitStructureMultilineCurlyBracesSyntax v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("region");
                writer.WriteValue(v.Region);
                writer.WritePropertyName("age");
                writer.WriteValue(v.Age);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override InGreetImplicitStructureMultilineCurlyBracesSyntax ReadJson(JsonReader reader, System.Type objectType, InGreetImplicitStructureMultilineCurlyBracesSyntax existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InGreetImplicitStructureMultilineCurlyBracesSyntax(
                    json["region"].Value<string>(), 
                    json["age"].Value<sbyte>()
                );
            }
        }

        [JsonConverter(typeof(OutGreetImplicitStructureMultilineCurlyBracesSyntax_JsonNetConverter))]
        public class OutGreetImplicitStructureMultilineCurlyBracesSyntax {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "OutGreetImplicitStructureMultilineCurlyBracesSyntax";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax";
            public string GetPackageName() { return OutGreetImplicitStructureMultilineCurlyBracesSyntax.RTTI_PACKAGE; }
            public string GetClassName() { return OutGreetImplicitStructureMultilineCurlyBracesSyntax.RTTI_CLASSNAME; }
            public string GetFullClassName() { return OutGreetImplicitStructureMultilineCurlyBracesSyntax.RTTI_FULLCLASSNAME; }

            public string Bullshit { get; set; }

            public OutGreetImplicitStructureMultilineCurlyBracesSyntax() {
            }

            public OutGreetImplicitStructureMultilineCurlyBracesSyntax(string bullshit) {
                this.Bullshit = bullshit;
            }

        }

        public class OutGreetImplicitStructureMultilineCurlyBracesSyntax_JsonNetConverter: JsonNetConverter<OutGreetImplicitStructureMultilineCurlyBracesSyntax> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutGreetImplicitStructureMultilineCurlyBracesSyntax_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutGreetImplicitStructureMultilineCurlyBracesSyntax v, JsonSerializer serializer) {
                writer.WriteStartObject();
                writer.WritePropertyName("bullshit");
                writer.WriteValue(v.Bullshit);
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutGreetImplicitStructureMultilineCurlyBracesSyntax ReadJson(JsonReader reader, System.Type objectType, OutGreetImplicitStructureMultilineCurlyBracesSyntax existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new OutGreetImplicitStructureMultilineCurlyBracesSyntax(
                    json["bullshit"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(InGreetAlgebraicOut_JsonNetConverter))]
        public class InGreetAlgebraicOut {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InGreetAlgebraicOut";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InGreetAlgebraicOut";
            public string GetPackageName() { return InGreetAlgebraicOut.RTTI_PACKAGE; }
            public string GetClassName() { return InGreetAlgebraicOut.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InGreetAlgebraicOut.RTTI_FULLCLASSNAME; }

            public string FirstName { get; set; }
            public string SecondName { get; set; }

            public InGreetAlgebraicOut() {
            }

            public InGreetAlgebraicOut(string firstName, string secondName) {
                this.FirstName = firstName;
                this.SecondName = secondName;
            }

        }

        public class InGreetAlgebraicOut_JsonNetConverter: JsonNetConverter<InGreetAlgebraicOut> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InGreetAlgebraicOut_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InGreetAlgebraicOut v, JsonSerializer serializer) {
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
            public override InGreetAlgebraicOut ReadJson(JsonReader reader, System.Type objectType, InGreetAlgebraicOut existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InGreetAlgebraicOut(
                    json["firstName"].Value<string>(), 
                    json["secondName"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(OutGreetAlgebraicOut_JsonNetConverter))]
        public abstract class OutGreetAlgebraicOut {
            public interface IOutGreetAlgebraicOutVisitor {
                void Visit(SuccessDataData visitor);
                void Visit(ErrorData visitor);
            }

            public abstract void Visit(IOutGreetAlgebraicOutVisitor visitor);
            private OutGreetAlgebraicOut() {}

            public sealed class SuccessDataData: OutGreetAlgebraicOut {
                public _SuccessDataData Value { get; private set; }
                public SuccessDataData(_SuccessDataData value) {
                    this.Value = value;
                }

                public override void Visit(IOutGreetAlgebraicOutVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _SuccessDataData(SuccessDataData m) {
                    return m.Value;
                }

                public static explicit operator SuccessDataData(_SuccessDataData m) {
                    return new SuccessDataData(m);
                }

            }

            public sealed class ErrorData: OutGreetAlgebraicOut {
                public _ErrorData Value { get; private set; }
                public ErrorData(_ErrorData value) {
                    this.Value = value;
                }

                public override void Visit(IOutGreetAlgebraicOutVisitor visitor) {
                    visitor.Visit(this);
                }

                // We would normally want to have an operator, but unfortunately if it is an interface,
                // it will fail on "user-defined conversions to or from an interface are not allowed".
                // public static explicit operator _ErrorData(ErrorData m) {
                //     return m.Value;
                // }
                //
                // public static explicit operator ErrorData(_ErrorData m) {
                //     return new ErrorData(m);
                // }

            }

        }
        public class OutGreetAlgebraicOut_JsonNetConverter: JsonNetConverter<OutGreetAlgebraicOut> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutGreetAlgebraicOut_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutGreetAlgebraicOut al, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (al is OutGreetAlgebraicOut.SuccessDataData) {
                    writer.WritePropertyName("SuccessDataData");
                    var v = (al as OutGreetAlgebraicOut.SuccessDataData).Value;
                    serializer.Serialize(writer, v);
                } else
                if (al is OutGreetAlgebraicOut.ErrorData) {
                    writer.WritePropertyName("ErrorData");
                    var v = (al as OutGreetAlgebraicOut.ErrorData).Value;
                    // Serializing polymorphic type ErrorData
                    writer.WriteStartObject();
                    writer.WritePropertyName(v.GetFullClassName());
                    serializer.Serialize(writer, v);
                    writer.WriteEndObject();

                } else
                {
                    throw new System.Exception("Unknown OutGreetAlgebraicOut type: " + al);
                }
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutGreetAlgebraicOut ReadJson(JsonReader reader, System.Type objectType, OutGreetAlgebraicOut existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var kv = json.Properties().First();
                switch (kv.Name) {
                    case "SuccessDataData": {
                        var v = serializer.Deserialize<Idltest.Services.SuccessDataData>(kv.Value.CreateReader());
                        return new OutGreetAlgebraicOut.SuccessDataData(v);
                    }

                    case "ErrorData": {
                        var v = serializer.Deserialize<Idltest.Services.ErrorData>(kv.Value.CreateReader());
                        return new OutGreetAlgebraicOut.ErrorData(v);
                    }

                    default:
                        throw new System.Exception("Unknown OutGreetAlgebraicOut type: " + kv.Name);
                }
            }
        }

        [JsonConverter(typeof(InGreetAlgebraicMultilineSyntax_JsonNetConverter))]
        public class InGreetAlgebraicMultilineSyntax {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InGreetAlgebraicMultilineSyntax";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InGreetAlgebraicMultilineSyntax";
            public string GetPackageName() { return InGreetAlgebraicMultilineSyntax.RTTI_PACKAGE; }
            public string GetClassName() { return InGreetAlgebraicMultilineSyntax.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InGreetAlgebraicMultilineSyntax.RTTI_FULLCLASSNAME; }

            public string FirstName { get; set; }
            public string SecondName { get; set; }

            public InGreetAlgebraicMultilineSyntax() {
            }

            public InGreetAlgebraicMultilineSyntax(string firstName, string secondName) {
                this.FirstName = firstName;
                this.SecondName = secondName;
            }

        }

        public class InGreetAlgebraicMultilineSyntax_JsonNetConverter: JsonNetConverter<InGreetAlgebraicMultilineSyntax> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InGreetAlgebraicMultilineSyntax_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InGreetAlgebraicMultilineSyntax v, JsonSerializer serializer) {
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
            public override InGreetAlgebraicMultilineSyntax ReadJson(JsonReader reader, System.Type objectType, InGreetAlgebraicMultilineSyntax existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InGreetAlgebraicMultilineSyntax(
                    json["firstName"].Value<string>(), 
                    json["secondName"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(OutGreetAlgebraicMultilineSyntax_JsonNetConverter))]
        public abstract class OutGreetAlgebraicMultilineSyntax {
            public interface IOutGreetAlgebraicMultilineSyntaxVisitor {
                void Visit(SuccessDataData visitor);
                void Visit(ErrorData visitor);
            }

            public abstract void Visit(IOutGreetAlgebraicMultilineSyntaxVisitor visitor);
            private OutGreetAlgebraicMultilineSyntax() {}

            public sealed class SuccessDataData: OutGreetAlgebraicMultilineSyntax {
                public _SuccessDataData Value { get; private set; }
                public SuccessDataData(_SuccessDataData value) {
                    this.Value = value;
                }

                public override void Visit(IOutGreetAlgebraicMultilineSyntaxVisitor visitor) {
                    visitor.Visit(this);
                }

                public static explicit operator _SuccessDataData(SuccessDataData m) {
                    return m.Value;
                }

                public static explicit operator SuccessDataData(_SuccessDataData m) {
                    return new SuccessDataData(m);
                }

            }

            public sealed class ErrorData: OutGreetAlgebraicMultilineSyntax {
                public _ErrorData Value { get; private set; }
                public ErrorData(_ErrorData value) {
                    this.Value = value;
                }

                public override void Visit(IOutGreetAlgebraicMultilineSyntaxVisitor visitor) {
                    visitor.Visit(this);
                }

                // We would normally want to have an operator, but unfortunately if it is an interface,
                // it will fail on "user-defined conversions to or from an interface are not allowed".
                // public static explicit operator _ErrorData(ErrorData m) {
                //     return m.Value;
                // }
                //
                // public static explicit operator ErrorData(_ErrorData m) {
                //     return new ErrorData(m);
                // }

            }

        }
        public class OutGreetAlgebraicMultilineSyntax_JsonNetConverter: JsonNetConverter<OutGreetAlgebraicMultilineSyntax> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public OutGreetAlgebraicMultilineSyntax_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, OutGreetAlgebraicMultilineSyntax al, JsonSerializer serializer) {
                writer.WriteStartObject();
                if (al is OutGreetAlgebraicMultilineSyntax.SuccessDataData) {
                    writer.WritePropertyName("SuccessDataData");
                    var v = (al as OutGreetAlgebraicMultilineSyntax.SuccessDataData).Value;
                    serializer.Serialize(writer, v);
                } else
                if (al is OutGreetAlgebraicMultilineSyntax.ErrorData) {
                    writer.WritePropertyName("ErrorData");
                    var v = (al as OutGreetAlgebraicMultilineSyntax.ErrorData).Value;
                    // Serializing polymorphic type ErrorData
                    writer.WriteStartObject();
                    writer.WritePropertyName(v.GetFullClassName());
                    serializer.Serialize(writer, v);
                    writer.WriteEndObject();

                } else
                {
                    throw new System.Exception("Unknown OutGreetAlgebraicMultilineSyntax type: " + al);
                }
                writer.WriteEndObject();
            }

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override OutGreetAlgebraicMultilineSyntax ReadJson(JsonReader reader, System.Type objectType, OutGreetAlgebraicMultilineSyntax existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);
                var kv = json.Properties().First();
                switch (kv.Name) {
                    case "SuccessDataData": {
                        var v = serializer.Deserialize<Idltest.Services.SuccessDataData>(kv.Value.CreateReader());
                        return new OutGreetAlgebraicMultilineSyntax.SuccessDataData(v);
                    }

                    case "ErrorData": {
                        var v = serializer.Deserialize<Idltest.Services.ErrorData>(kv.Value.CreateReader());
                        return new OutGreetAlgebraicMultilineSyntax.ErrorData(v);
                    }

                    default:
                        throw new System.Exception("Unknown OutGreetAlgebraicMultilineSyntax type: " + kv.Name);
                }
            }
        }

        [JsonConverter(typeof(InAlternative_JsonNetConverter))]
        public class InAlternative {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InAlternative";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InAlternative";
            public string GetPackageName() { return InAlternative.RTTI_PACKAGE; }
            public string GetClassName() { return InAlternative.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InAlternative.RTTI_FULLCLASSNAME; }

            public string FirstName { get; set; }
            public string SecondName { get; set; }

            public InAlternative() {
            }

            public InAlternative(string firstName, string secondName) {
                this.FirstName = firstName;
                this.SecondName = secondName;
            }

        }

        public class InAlternative_JsonNetConverter: JsonNetConverter<InAlternative> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InAlternative_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InAlternative v, JsonSerializer serializer) {
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
            public override InAlternative ReadJson(JsonReader reader, System.Type objectType, InAlternative existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InAlternative(
                    json["firstName"].Value<string>(), 
                    json["secondName"].Value<string>()
                );
            }
        }

        [JsonConverter(typeof(InAlternativeSame_JsonNetConverter))]
        public class InAlternativeSame {
            public static readonly string RTTI_PACKAGE = "idltest.services.TestService";
            public static readonly string RTTI_CLASSNAME = "InAlternativeSame";
            public static readonly string RTTI_FULLCLASSNAME = "idltest.services.TestService.InAlternativeSame";
            public string GetPackageName() { return InAlternativeSame.RTTI_PACKAGE; }
            public string GetClassName() { return InAlternativeSame.RTTI_CLASSNAME; }
            public string GetFullClassName() { return InAlternativeSame.RTTI_FULLCLASSNAME; }

            public string FirstName { get; set; }
            public string SecondName { get; set; }

            public InAlternativeSame() {
            }

            public InAlternativeSame(string firstName, string secondName) {
                this.FirstName = firstName;
                this.SecondName = secondName;
            }

        }

        public class InAlternativeSame_JsonNetConverter: JsonNetConverter<InAlternativeSame> {
        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public InAlternativeSame_JsonNetConverter() {}

        #if UNITY_5_3_OR_NEWER
            [UnityEngine.Scripting.RequiredMember]
        #endif
            public override void WriteJson(JsonWriter writer, InAlternativeSame v, JsonSerializer serializer) {
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
            public override InAlternativeSame ReadJson(JsonReader reader, System.Type objectType, InAlternativeSame existingValue, bool hasExistingValue, JsonSerializer serializer) {
                var json = JObject.Load(reader);

                return new InAlternativeSame(
                    json["firstName"].Value<string>(), 
                    json["secondName"].Value<string>()
                );
            }
        }

    }

    // ============== Service Client ==============
    public interface ITestServiceClient<C> where C: class, IClientTransportContext {
        void UnitToUnit(Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void AnotherVoid(Action<TestService.OutAnotherVoid> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void UnitResult(Idltest.Services.Package package, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void Parameterless(Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void SimpleMethod(string a, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void SimpleIntMethod(int a, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void SimpleMethodWithGenerics(List<string> a, Action<List<string>> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void Simple(Action<TestService.OutSimple> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void SimpleEnum(Idltest.Services.TestServiceEnum v, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void SimpleEnum2(Idltest.Services.Environment e, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void ReturnsList(Idltest.Services.Environment e, Action<List<Idltest.Services.Package>> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void ReturnsMap(Idltest.Services.Environment e, Action<Dictionary<string, Idltest.Services.Package>> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void SimpleGoReserved(Idltest.Services.Package package, Action<bool> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void SimpleVoid(string a, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void GreetSingularOut(string firstName, string secondName, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void GreetImplicitStructOut(string firstName, string secondName, Action<TestService.OutGreetImplicitStructOut> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void GreetImplicitStructMultilineSyntax(string region, sbyte age, Action<TestService.OutGreetImplicitStructMultilineSyntax> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void GreetImplicitStructureMultilineCurlyBracesSyntax(string region, sbyte age, Action<TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void GreetAlgebraicOut(string firstName, string secondName, Action<TestService.OutGreetAlgebraicOut> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void GreetAlgebraicMultilineSyntax(string firstName, string secondName, Action<TestService.OutGreetAlgebraicMultilineSyntax> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void Alternative(string firstName, string secondName, Action<Either<Idltest.Services.ErrorData, Idltest.Services.SuccessData> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void AlternativeSame(string firstName, string secondName, Action<Either<Idltest.Services.SuccessData, Idltest.Services.SuccessData> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void AlternativeGeneric(Action<Either<List<Idltest.Services.ErrorData>, List<Idltest.Services.SuccessData>> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
        void AlternativeGeneric2(Action<Either<Dictionary<string, Idltest.Services.ErrorData>, Dictionary<string, Idltest.Services.SuccessData>> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null);
    }

    public class TestServiceClientGeneric<C>: ITestServiceClient<C> where C: class, IClientTransportContext {
        public IClientTransport<C> Transport { get; private set; }

        public TestServiceClientGeneric(IClientTransport<C> t) {
            Transport = t;
        }

        public void SetHTTPTransport(string endpoint, IJsonMarshaller marshaller, bool blocking = false, int timeout = 60) {
            if (blocking) {
                this.Transport = new SyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            } else {
                this.Transport = new AsyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
            }
        }
        public void UnitToUnit(Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, IRT.Void>("TestService", "unitToUnit", null,
                new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
        }

        public void AnotherVoid(Action<TestService.OutAnotherVoid> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, TestService.OutAnotherVoid>("TestService", "anotherVoid", null,
                new ClientTransportCallback<TestService.OutAnotherVoid>(onSuccess, onFailure, onAny), ctx);
        }

        public void UnitResult(Idltest.Services.Package package, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InUnitResult(package);
            Transport.Send<TestService.InUnitResult, IRT.Void>("TestService", "unitResult", inData,
                new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
        }

        public void Parameterless(Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, string>("TestService", "parameterless", null,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void SimpleMethod(string a, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InSimpleMethod(a);
            Transport.Send<TestService.InSimpleMethod, string>("TestService", "simpleMethod", inData,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void SimpleIntMethod(int a, Action<int> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InSimpleIntMethod(a);
            Transport.Send<TestService.InSimpleIntMethod, int>("TestService", "simpleIntMethod", inData,
                new ClientTransportCallback<int>(onSuccess, onFailure, onAny), ctx);
        }

        public void SimpleMethodWithGenerics(List<string> a, Action<List<string>> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InSimpleMethodWithGenerics(a);
            Transport.Send<TestService.InSimpleMethodWithGenerics, List<string>>("TestService", "simpleMethodWithGenerics", inData,
                new ClientTransportCallback<List<string>>(onSuccess, onFailure, onAny), ctx);
        }

        public void Simple(Action<TestService.OutSimple> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, TestService.OutSimple>("TestService", "simple", null,
                new ClientTransportCallback<TestService.OutSimple>(onSuccess, onFailure, onAny), ctx);
        }

        public void SimpleEnum(Idltest.Services.TestServiceEnum v, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InSimpleEnum(v);
            Transport.Send<TestService.InSimpleEnum, string>("TestService", "simpleEnum", inData,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void SimpleEnum2(Idltest.Services.Environment e, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InSimpleEnum2(e);
            Transport.Send<TestService.InSimpleEnum2, string>("TestService", "simpleEnum2", inData,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void ReturnsList(Idltest.Services.Environment e, Action<List<Idltest.Services.Package>> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InReturnsList(e);
            Transport.Send<TestService.InReturnsList, List<Idltest.Services.Package>>("TestService", "returnsList", inData,
                new ClientTransportCallback<List<Idltest.Services.Package>>(onSuccess, onFailure, onAny), ctx);
        }

        public void ReturnsMap(Idltest.Services.Environment e, Action<Dictionary<string, Idltest.Services.Package>> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InReturnsMap(e);
            Transport.Send<TestService.InReturnsMap, Dictionary<string, Idltest.Services.Package>>("TestService", "returnsMap", inData,
                new ClientTransportCallback<Dictionary<string, Idltest.Services.Package>>(onSuccess, onFailure, onAny), ctx);
        }

        public void SimpleGoReserved(Idltest.Services.Package package, Action<bool> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InSimpleGoReserved(package);
            Transport.Send<TestService.InSimpleGoReserved, bool>("TestService", "simpleGoReserved", inData,
                new ClientTransportCallback<bool>(onSuccess, onFailure, onAny), ctx);
        }

        public void SimpleVoid(string a, Action onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InSimpleVoid(a);
            Transport.Send<TestService.InSimpleVoid, IRT.Void>("TestService", "simpleVoid", inData,
                new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
        }

        public void GreetSingularOut(string firstName, string secondName, Action<string> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InGreetSingularOut(firstName, secondName);
            Transport.Send<TestService.InGreetSingularOut, string>("TestService", "greetSingularOut", inData,
                new ClientTransportCallback<string>(onSuccess, onFailure, onAny), ctx);
        }

        public void GreetImplicitStructOut(string firstName, string secondName, Action<TestService.OutGreetImplicitStructOut> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InGreetImplicitStructOut(firstName, secondName);
            Transport.Send<TestService.InGreetImplicitStructOut, TestService.OutGreetImplicitStructOut>("TestService", "greetImplicitStructOut", inData,
                new ClientTransportCallback<TestService.OutGreetImplicitStructOut>(onSuccess, onFailure, onAny), ctx);
        }

        public void GreetImplicitStructMultilineSyntax(string region, sbyte age, Action<TestService.OutGreetImplicitStructMultilineSyntax> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InGreetImplicitStructMultilineSyntax(region, age);
            Transport.Send<TestService.InGreetImplicitStructMultilineSyntax, TestService.OutGreetImplicitStructMultilineSyntax>("TestService", "greetImplicitStructMultilineSyntax", inData,
                new ClientTransportCallback<TestService.OutGreetImplicitStructMultilineSyntax>(onSuccess, onFailure, onAny), ctx);
        }

        public void GreetImplicitStructureMultilineCurlyBracesSyntax(string region, sbyte age, Action<TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InGreetImplicitStructureMultilineCurlyBracesSyntax(region, age);
            Transport.Send<TestService.InGreetImplicitStructureMultilineCurlyBracesSyntax, TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax>("TestService", "greetImplicitStructureMultilineCurlyBracesSyntax", inData,
                new ClientTransportCallback<TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax>(onSuccess, onFailure, onAny), ctx);
        }

        public void GreetAlgebraicOut(string firstName, string secondName, Action<TestService.OutGreetAlgebraicOut> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InGreetAlgebraicOut(firstName, secondName);
            Transport.Send<TestService.InGreetAlgebraicOut, TestService.OutGreetAlgebraicOut>("TestService", "greetAlgebraicOut", inData,
                new ClientTransportCallback<TestService.OutGreetAlgebraicOut>(onSuccess, onFailure, onAny), ctx);
        }

        public void GreetAlgebraicMultilineSyntax(string firstName, string secondName, Action<TestService.OutGreetAlgebraicMultilineSyntax> onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InGreetAlgebraicMultilineSyntax(firstName, secondName);
            Transport.Send<TestService.InGreetAlgebraicMultilineSyntax, TestService.OutGreetAlgebraicMultilineSyntax>("TestService", "greetAlgebraicMultilineSyntax", inData,
                new ClientTransportCallback<TestService.OutGreetAlgebraicMultilineSyntax>(onSuccess, onFailure, onAny), ctx);
        }

        public void Alternative(string firstName, string secondName, Action<Either<Idltest.Services.ErrorData, Idltest.Services.SuccessData> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InAlternative(firstName, secondName);
            Transport.Send<TestService.InAlternative, Either<Idltest.Services.ErrorData, Idltest.Services.SuccessData> >("TestService", "alternative", inData,
                new ClientTransportCallback<Either<Idltest.Services.ErrorData, Idltest.Services.SuccessData> >(onSuccess, onFailure, onAny), ctx);
        }

        public void AlternativeSame(string firstName, string secondName, Action<Either<Idltest.Services.SuccessData, Idltest.Services.SuccessData> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            var inData = new TestService.InAlternativeSame(firstName, secondName);
            Transport.Send<TestService.InAlternativeSame, Either<Idltest.Services.SuccessData, Idltest.Services.SuccessData> >("TestService", "alternativeSame", inData,
                new ClientTransportCallback<Either<Idltest.Services.SuccessData, Idltest.Services.SuccessData> >(onSuccess, onFailure, onAny), ctx);
        }

        public void AlternativeGeneric(Action<Either<List<Idltest.Services.ErrorData>, List<Idltest.Services.SuccessData>> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, Either<List<Idltest.Services.ErrorData>, List<Idltest.Services.SuccessData>> >("TestService", "alternativeGeneric", null,
                new ClientTransportCallback<Either<List<Idltest.Services.ErrorData>, List<Idltest.Services.SuccessData>> >(onSuccess, onFailure, onAny), ctx);
        }

        public void AlternativeGeneric2(Action<Either<Dictionary<string, Idltest.Services.ErrorData>, Dictionary<string, Idltest.Services.SuccessData>> > onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null) {
            // No input params for this method
            Transport.Send<object, Either<Dictionary<string, Idltest.Services.ErrorData>, Dictionary<string, Idltest.Services.SuccessData>> >("TestService", "alternativeGeneric2", null,
                new ClientTransportCallback<Either<Dictionary<string, Idltest.Services.ErrorData>, Dictionary<string, Idltest.Services.SuccessData>> >(onSuccess, onFailure, onAny), ctx);
        }

    }

    public class TestServiceClient: TestServiceClientGeneric<IClientTransportContext> {
        public TestServiceClient(IClientTransport<IClientTransportContext> t): base(t) {}
    }

    // ============== Service Dispatcher ==============
    public interface ITestServiceServer<C> {
        void UnitToUnit(C ctx);
        TestService.OutAnotherVoid AnotherVoid(C ctx);
        void UnitResult(C ctx, Idltest.Services.Package package);
        string Parameterless(C ctx);
        string SimpleMethod(C ctx, string a);
        int SimpleIntMethod(C ctx, int a);
        List<string> SimpleMethodWithGenerics(C ctx, List<string> a);
        TestService.OutSimple Simple(C ctx);
        string SimpleEnum(C ctx, Idltest.Services.TestServiceEnum v);
        string SimpleEnum2(C ctx, Idltest.Services.Environment e);
        List<Idltest.Services.Package> ReturnsList(C ctx, Idltest.Services.Environment e);
        Dictionary<string, Idltest.Services.Package> ReturnsMap(C ctx, Idltest.Services.Environment e);
        bool SimpleGoReserved(C ctx, Idltest.Services.Package package);
        void SimpleVoid(C ctx, string a);
        string GreetSingularOut(C ctx, string firstName, string secondName);
        TestService.OutGreetImplicitStructOut GreetImplicitStructOut(C ctx, string firstName, string secondName);
        TestService.OutGreetImplicitStructMultilineSyntax GreetImplicitStructMultilineSyntax(C ctx, string region, sbyte age);
        TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax GreetImplicitStructureMultilineCurlyBracesSyntax(C ctx, string region, sbyte age);
        TestService.OutGreetAlgebraicOut GreetAlgebraicOut(C ctx, string firstName, string secondName);
        TestService.OutGreetAlgebraicMultilineSyntax GreetAlgebraicMultilineSyntax(C ctx, string firstName, string secondName);
        Either<Idltest.Services.ErrorData, Idltest.Services.SuccessData>  Alternative(C ctx, string firstName, string secondName);
        Either<Idltest.Services.SuccessData, Idltest.Services.SuccessData>  AlternativeSame(C ctx, string firstName, string secondName);
        Either<List<Idltest.Services.ErrorData>, List<Idltest.Services.SuccessData>>  AlternativeGeneric(C ctx);
        Either<Dictionary<string, Idltest.Services.ErrorData>, Dictionary<string, Idltest.Services.SuccessData>>  AlternativeGeneric2(C ctx);
    }

    public class TestServiceDispatcher<C, D>: IServiceDispatcher<C, D> {
        private static readonly string[] methods = { "unitToUnit", "anotherVoid", "unitResult", "parameterless", "simpleMethod", "simpleIntMethod", "simpleMethodWithGenerics", "simple", "simpleEnum", "simpleEnum2", "returnsList", "returnsMap", "simpleGoReserved", "simpleVoid", "greetSingularOut", "greetImplicitStructOut", "greetImplicitStructMultilineSyntax", "greetImplicitStructureMultilineCurlyBracesSyntax", "greetAlgebraicOut", "greetAlgebraicMultilineSyntax", "alternative", "alternativeSame", "alternativeGeneric", "alternativeGeneric2" };
        protected IMarshaller<D> marshaller;
        protected ITestServiceServer<C> server;

        public TestServiceDispatcher(IMarshaller<D> marshaller, ITestServiceServer<C> server) {
            this.marshaller = marshaller;
            this.server = server;
        }

        public string GetSupportedService() {
            return "TestService";
        }

        public string[] GetSupportedMethods() {
            return TestServiceDispatcher<C, D>.methods;
        }

        public D Dispatch(C ctx, string method, D data) {
            switch(method) {
                case "unitToUnit": {
                    // No input params for this method
                    server.UnitToUnit(ctx);
                    return marshaller.Marshal<IRT.Void>(null);
                }

                case "anotherVoid": {
                    // No input params for this method
                    return marshaller.Marshal<TestService.OutAnotherVoid>(
                        server.AnotherVoid(ctx)
                    );
                }

                case "unitResult": {
                    var obj = marshaller.Unmarshal<TestService.InUnitResult>(data);
                    server.UnitResult(ctx, obj.Package);
                    return marshaller.Marshal<IRT.Void>(null);
                }

                case "parameterless": {
                    // No input params for this method
                    return marshaller.Marshal<string>(
                        server.Parameterless(ctx)
                    );
                }

                case "simpleMethod": {
                    var obj = marshaller.Unmarshal<TestService.InSimpleMethod>(data);
                    return marshaller.Marshal<string>(
                        server.SimpleMethod(ctx, obj.A)
                    );
                }

                case "simpleIntMethod": {
                    var obj = marshaller.Unmarshal<TestService.InSimpleIntMethod>(data);
                    return marshaller.Marshal<int>(
                        server.SimpleIntMethod(ctx, obj.A)
                    );
                }

                case "simpleMethodWithGenerics": {
                    var obj = marshaller.Unmarshal<TestService.InSimpleMethodWithGenerics>(data);
                    return marshaller.Marshal<List<string>>(
                        server.SimpleMethodWithGenerics(ctx, obj.A)
                    );
                }

                case "simple": {
                    // No input params for this method
                    return marshaller.Marshal<TestService.OutSimple>(
                        server.Simple(ctx)
                    );
                }

                case "simpleEnum": {
                    var obj = marshaller.Unmarshal<TestService.InSimpleEnum>(data);
                    return marshaller.Marshal<string>(
                        server.SimpleEnum(ctx, obj.V)
                    );
                }

                case "simpleEnum2": {
                    var obj = marshaller.Unmarshal<TestService.InSimpleEnum2>(data);
                    return marshaller.Marshal<string>(
                        server.SimpleEnum2(ctx, obj.E)
                    );
                }

                case "returnsList": {
                    var obj = marshaller.Unmarshal<TestService.InReturnsList>(data);
                    return marshaller.Marshal<List<Idltest.Services.Package>>(
                        server.ReturnsList(ctx, obj.E)
                    );
                }

                case "returnsMap": {
                    var obj = marshaller.Unmarshal<TestService.InReturnsMap>(data);
                    return marshaller.Marshal<Dictionary<string, Idltest.Services.Package>>(
                        server.ReturnsMap(ctx, obj.E)
                    );
                }

                case "simpleGoReserved": {
                    var obj = marshaller.Unmarshal<TestService.InSimpleGoReserved>(data);
                    return marshaller.Marshal<bool>(
                        server.SimpleGoReserved(ctx, obj.Package)
                    );
                }

                case "simpleVoid": {
                    var obj = marshaller.Unmarshal<TestService.InSimpleVoid>(data);
                    server.SimpleVoid(ctx, obj.A);
                    return marshaller.Marshal<IRT.Void>(null);
                }

                case "greetSingularOut": {
                    var obj = marshaller.Unmarshal<TestService.InGreetSingularOut>(data);
                    return marshaller.Marshal<string>(
                        server.GreetSingularOut(ctx, obj.FirstName, obj.SecondName)
                    );
                }

                case "greetImplicitStructOut": {
                    var obj = marshaller.Unmarshal<TestService.InGreetImplicitStructOut>(data);
                    return marshaller.Marshal<TestService.OutGreetImplicitStructOut>(
                        server.GreetImplicitStructOut(ctx, obj.FirstName, obj.SecondName)
                    );
                }

                case "greetImplicitStructMultilineSyntax": {
                    var obj = marshaller.Unmarshal<TestService.InGreetImplicitStructMultilineSyntax>(data);
                    return marshaller.Marshal<TestService.OutGreetImplicitStructMultilineSyntax>(
                        server.GreetImplicitStructMultilineSyntax(ctx, obj.Region, obj.Age)
                    );
                }

                case "greetImplicitStructureMultilineCurlyBracesSyntax": {
                    var obj = marshaller.Unmarshal<TestService.InGreetImplicitStructureMultilineCurlyBracesSyntax>(data);
                    return marshaller.Marshal<TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax>(
                        server.GreetImplicitStructureMultilineCurlyBracesSyntax(ctx, obj.Region, obj.Age)
                    );
                }

                case "greetAlgebraicOut": {
                    var obj = marshaller.Unmarshal<TestService.InGreetAlgebraicOut>(data);
                    return marshaller.Marshal<TestService.OutGreetAlgebraicOut>(
                        server.GreetAlgebraicOut(ctx, obj.FirstName, obj.SecondName)
                    );
                }

                case "greetAlgebraicMultilineSyntax": {
                    var obj = marshaller.Unmarshal<TestService.InGreetAlgebraicMultilineSyntax>(data);
                    return marshaller.Marshal<TestService.OutGreetAlgebraicMultilineSyntax>(
                        server.GreetAlgebraicMultilineSyntax(ctx, obj.FirstName, obj.SecondName)
                    );
                }

                case "alternative": {
                    var obj = marshaller.Unmarshal<TestService.InAlternative>(data);
                    return marshaller.Marshal<Either<Idltest.Services.ErrorData, Idltest.Services.SuccessData> >(
                        server.Alternative(ctx, obj.FirstName, obj.SecondName)
                    );
                }

                case "alternativeSame": {
                    var obj = marshaller.Unmarshal<TestService.InAlternativeSame>(data);
                    return marshaller.Marshal<Either<Idltest.Services.SuccessData, Idltest.Services.SuccessData> >(
                        server.AlternativeSame(ctx, obj.FirstName, obj.SecondName)
                    );
                }

                case "alternativeGeneric": {
                    // No input params for this method
                    return marshaller.Marshal<Either<List<Idltest.Services.ErrorData>, List<Idltest.Services.SuccessData>> >(
                        server.AlternativeGeneric(ctx)
                    );
                }

                case "alternativeGeneric2": {
                    // No input params for this method
                    return marshaller.Marshal<Either<Dictionary<string, Idltest.Services.ErrorData>, Dictionary<string, Idltest.Services.SuccessData>> >(
                        server.AlternativeGeneric2(ctx)
                    );
                }

                default:
                    throw new DispatcherException(string.Format("Method {0} is not supported by TestServiceDispatcher.", method));
            }
        }
    }

    // ============== Service Server Base ==============
    public abstract class TestServiceServer<C, D>: TestServiceDispatcher<C, D>,  ITestServiceServer<C> {
        public TestServiceServer(IMarshaller<D> marshaller): base(marshaller, null) {
            server = this;
        }

        public virtual void UnitToUnit(C ctx) {
            // Nothing to return
        }

        public virtual TestService.OutAnotherVoid AnotherVoid(C ctx) {
            return null;
        }

        public virtual void UnitResult(C ctx, Idltest.Services.Package package) {
            // Nothing to return
        }

        public virtual string Parameterless(C ctx) {
            return null;
        }

        public virtual string SimpleMethod(C ctx, string a) {
            return null;
        }

        public virtual int SimpleIntMethod(C ctx, int a) {
            return 0;
        }

        public virtual List<string> SimpleMethodWithGenerics(C ctx, List<string> a) {
            return null;
        }

        public virtual TestService.OutSimple Simple(C ctx) {
            return null;
        }

        public virtual string SimpleEnum(C ctx, Idltest.Services.TestServiceEnum v) {
            return null;
        }

        public virtual string SimpleEnum2(C ctx, Idltest.Services.Environment e) {
            return null;
        }

        public virtual List<Idltest.Services.Package> ReturnsList(C ctx, Idltest.Services.Environment e) {
            return null;
        }

        public virtual Dictionary<string, Idltest.Services.Package> ReturnsMap(C ctx, Idltest.Services.Environment e) {
            return null;
        }

        public virtual bool SimpleGoReserved(C ctx, Idltest.Services.Package package) {
            return false;
        }

        public virtual void SimpleVoid(C ctx, string a) {
            // Nothing to return
        }

        public virtual string GreetSingularOut(C ctx, string firstName, string secondName) {
            return null;
        }

        public virtual TestService.OutGreetImplicitStructOut GreetImplicitStructOut(C ctx, string firstName, string secondName) {
            return null;
        }

        public virtual TestService.OutGreetImplicitStructMultilineSyntax GreetImplicitStructMultilineSyntax(C ctx, string region, sbyte age) {
            return null;
        }

        public virtual TestService.OutGreetImplicitStructureMultilineCurlyBracesSyntax GreetImplicitStructureMultilineCurlyBracesSyntax(C ctx, string region, sbyte age) {
            return null;
        }

        public virtual TestService.OutGreetAlgebraicOut GreetAlgebraicOut(C ctx, string firstName, string secondName) {
            return null;
        }

        public virtual TestService.OutGreetAlgebraicMultilineSyntax GreetAlgebraicMultilineSyntax(C ctx, string firstName, string secondName) {
            return null;
        }

        public virtual Either<Idltest.Services.ErrorData, Idltest.Services.SuccessData>  Alternative(C ctx, string firstName, string secondName) {
            return null;
        }

        public virtual Either<Idltest.Services.SuccessData, Idltest.Services.SuccessData>  AlternativeSame(C ctx, string firstName, string secondName) {
            return null;
        }

        public virtual Either<List<Idltest.Services.ErrorData>, List<Idltest.Services.SuccessData>>  AlternativeGeneric(C ctx) {
            return null;
        }

        public virtual Either<Dictionary<string, Idltest.Services.ErrorData>, Dictionary<string, Idltest.Services.SuccessData>>  AlternativeGeneric2(C ctx) {
            return null;
        }

    }
}