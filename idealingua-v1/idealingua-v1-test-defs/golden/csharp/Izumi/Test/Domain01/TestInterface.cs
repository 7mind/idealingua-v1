// Auto-generated, any modifications may be overwritten in the future.

using System.Collections;
using System.Collections.Generic;
using IRT;
using System;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(TestInterface_JsonNetConverter))]
    public interface TestInterface: IRTTI {
        string UserId { get; set; }
        int AccountBalance { get; set; }
        long LatestLogin { get; set; }
        Dictionary<string, string> Keys { get; set; }
        List<string> Nicknames { get; set; }
    }
    public class TestInterface_JsonNetConverter: JsonNetConverter<TestInterface> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterface_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterface value, JsonSerializer serializer) {
            // Serializing polymorphic type TestInterface
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override TestInterface ReadJson(JsonReader reader, System.Type objectType, TestInterface existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = TestInterfaceStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (TestInterface)res;
        }
    }

    [JsonConverter(typeof(TestInterfaceStruct_JsonNetConverter))]
    public class TestInterfaceStruct : TestInterface {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01.TestInterface";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.TestInterface.Struct";
        public string GetPackageName() { return TestInterfaceStruct.RTTI_PACKAGE; }
        public string GetClassName() { return TestInterfaceStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return TestInterfaceStruct.RTTI_FULLCLASSNAME; }

        public string UserId { get; set; }
        public int AccountBalance { get; set; }
        public long LatestLogin { get; set; }
        public Dictionary<string, string> Keys { get; set; }
        public List<string> Nicknames { get; set; }

        public TestInterfaceStruct() {
            Keys = new Dictionary<string, string>();
            Nicknames = new List<string>();
        }

        public TestInterfaceStruct(string userId, int accountBalance, long latestLogin, Dictionary<string, string> keys, List<string> nicknames) {
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

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            TestInterfaceStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            TestInterfaceStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!TestInterfaceStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface TestInterface.");
            }

            return tpe;
        }

        static TestInterfaceStruct() {
            var type = typeof(TestInterface);
            #if IRT_SCAN_ALL_ASSEMBLIES
                var assemblies = AppDomain.CurrentDomain.GetAssemblies();
            #else
                var assemblies = new[] {Assembly.GetExecutingAssembly()};
            #endif
            foreach (var assembly in assemblies) {
                System.Type[] types = null;
                try {
                    types = assembly.GetTypes();
                } catch (Exception) {
                    // ReflectionTypeLoadException potentially caught here
                    continue;
                }
                foreach (var tp in types) {
                    if (type.IsAssignableFrom(tp) && !tp.IsInterface) {
                        var rttiID = tp.GetField("RTTI_FULLCLASSNAME");
                        if (rttiID != null) {
                            TestInterfaceStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class TestInterfaceStruct_JsonNetConverter: JsonNetConverter<TestInterfaceStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public TestInterfaceStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, TestInterfaceStruct v, JsonSerializer serializer) {
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
        public override TestInterfaceStruct ReadJson(JsonReader reader, System.Type objectType, TestInterfaceStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
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

            return new TestInterfaceStruct(
                json["userId"].Value<string>(), 
                json["accountBalance"].Value<int>(), 
                json["latestLogin"].Value<long>(), 
                _keys, 
                _nicknames
            );
        }
    }
}