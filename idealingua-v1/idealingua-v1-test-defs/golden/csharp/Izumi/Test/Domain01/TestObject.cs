// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(TestObject_JsonNetConverter))]
    public class TestObject : TestInterface {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "TestObject";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.TestObject";
        public string GetPackageName() { return TestObject.RTTI_PACKAGE; }
        public string GetClassName() { return TestObject.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestObject.RTTI_FULLCLASSNAME; }

        public string UserId { get; set; }
        public int AccountBalance { get; set; }
        public long LatestLogin { get; set; }
        public Dictionary<string, string> Keys { get; set; }
        public List<string> Nicknames { get; set; }

        public TestObject() {
            Keys = new Dictionary<string, string>();
            Nicknames = new List<string>();
        }

        public TestObject(string userId, int accountBalance, long latestLogin, Dictionary<string, string> keys, List<string> nicknames) {
            this.UserId = userId;
            this.AccountBalance = accountBalance;
            this.LatestLogin = latestLogin;
            this.Keys = keys;
            this.Nicknames = nicknames;
        }

        public TestInterface ToTestInterface() {
            var res = new TestInterfaceStruct();
            res.UserId = this.UserId;
            res.AccountBalance = this.AccountBalance;
            res.LatestLogin = this.LatestLogin;
            res.Keys = this.Keys;
            res.Nicknames = this.Nicknames;
            return res;
        }

        public void LoadTestInterface(TestInterface value) {
            this.UserId = value.UserId;
            this.AccountBalance = value.AccountBalance;
            this.LatestLogin = value.LatestLogin;
            this.Keys = value.Keys;
            this.Nicknames = value.Nicknames;
        }

    }
    public class TestObject_JsonNetConverter: JsonNetConverter<TestObject> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestObject_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestObject v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("userId");
            writer.WriteValue(v.UserId);
            writer.WritePropertyName("accountBalance");
            writer.WriteValue(v.AccountBalance);
            writer.WritePropertyName("latestLogin");
            writer.WriteValue(v.LatestLogin);
            writer.WritePropertyName("keys");
            writer.WriteStartObject();
            foreach(var mkv in v.Keys) {
                writer.WritePropertyName(mkv.Key.ToString());
                writer.WriteValue(mkv.Value);
            }
            writer.WriteEndObject();

            writer.WritePropertyName("nicknames");
            writer.WriteStartArray();
            foreach (var lv in v.Nicknames) {
                writer.WriteValue(lv);
            }
            writer.WriteEndArray();

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestObject ReadJson(JsonReader reader, System.Type objectType, TestObject existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _keys = new Dictionary<string, string>();
            foreach (var _keys_kv in ((JObject)json["keys"]).Properties()) {
                string _keys_dv;
                _keys_dv = _keys_kv.Value.Value<string>();
                _keys.Add(_keys_kv.Name, _keys_dv);
            }

            var _nicknames = new List<string>();
            foreach (var _nicknames_sv in (JArray)json["nicknames"]) {
                string _nicknames_d;
                _nicknames_d = _nicknames_sv.Value<string>();
                _nicknames.Add(_nicknames_d);
            }

            return new TestObject(
                json["userId"].Value<string>(), 
                json["accountBalance"].Value<int>(), 
                json["latestLogin"].Value<long>(), 
                _keys, 
                _nicknames
            );
        }
    }
}