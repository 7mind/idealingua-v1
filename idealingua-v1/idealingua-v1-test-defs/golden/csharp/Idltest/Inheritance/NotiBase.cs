// Auto-generated, any modifications may be overwritten in the future.

using System;
using IRT;
using System.Globalization;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using Newtonsoft.Json;
using System.Linq;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Inheritance {
    [JsonConverter(typeof(NotiBase_JsonNetConverter))]
    public interface NotiBase: IRTTI {
        DateTime At { get; set; }
        string UserID { get; set; }
        string UserName { get; set; }
        string Message { get; set; }
    }
    public class NotiBase_JsonNetConverter: JsonNetConverter<NotiBase> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotiBase_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotiBase value, JsonSerializer serializer) {
            // Serializing polymorphic type NotiBase
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotiBase ReadJson(JsonReader reader, System.Type objectType, NotiBase existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NotiBaseStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (NotiBase)res;
        }
    }

    [JsonConverter(typeof(NotiBaseStruct_JsonNetConverter))]
    public class NotiBaseStruct : NotiBase {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.NotiBase";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.NotiBase.Struct";
        public string GetPackageName() { return NotiBaseStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NotiBaseStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NotiBaseStruct.RTTI_FULLCLASSNAME; }

        public DateTime At { get; set; }
        public string UserID { get; set; }
        public string UserName { get; set; }
        public string Message { get; set; }

        public NotiBaseStruct() {
        }

        public NotiBaseStruct(DateTime at, string userID, string userName, string message) {
            this.At = at;
            this.UserID = userID;
            this.UserName = userName;
            this.Message = message;
        }

        public NotiBase ToNotiBase() {
            var res = new NotiBaseStruct();
            res.At = this.At;
            res.UserID = this.UserID;
            res.UserName = this.UserName;
            res.Message = this.Message;
            return res;
        }

        public void LoadNotiBase(NotiBase value) {
            this.At = value.At;
            this.UserID = value.UserID;
            this.UserName = value.UserName;
            this.Message = value.Message;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NotiBaseStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NotiBaseStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NotiBaseStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface NotiBase.");
            }

            return tpe;
        }

        static NotiBaseStruct() {
            var type = typeof(NotiBase);
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
                            NotiBaseStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NotiBaseStruct_JsonNetConverter: JsonNetConverter<NotiBaseStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotiBaseStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotiBaseStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("at");
            writer.WriteValue(v.At.ToString(v.At.Kind == DateTimeKind.Utc ? JsonNetTimeFormats.TsuDefault : JsonNetTimeFormats.TszDefault, CultureInfo.InvariantCulture));
            writer.WritePropertyName("userID");
            writer.WriteValue(v.UserID);
            if (v.UserName != null) {
                writer.WritePropertyName("userName");
                writer.WriteValue(v.UserName);
            }

            if (v.Message != null) {
                writer.WritePropertyName("message");
                writer.WriteValue(v.Message);
            }

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotiBaseStruct ReadJson(JsonReader reader, System.Type objectType, NotiBaseStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            string _userName = null;
            var _userNameRaw = json["userName"];
            if (_userNameRaw != null && _userNameRaw.Type != JTokenType.Null) {
                _userName = _userNameRaw.Value<string>();
            }

            string _message = null;
            var _messageRaw = json["message"];
            if (_messageRaw != null && _messageRaw.Type != JTokenType.Null) {
                _message = _messageRaw.Value<string>();
            }

            return new NotiBaseStruct(
                DateTime.ParseExact(json["at"].Value<string>(), JsonNetTimeFormats.Tsz, CultureInfo.InvariantCulture, DateTimeStyles.None), 
                json["userID"].Value<string>(), 
                _userName, 
                _message
            );
        }
    }
}