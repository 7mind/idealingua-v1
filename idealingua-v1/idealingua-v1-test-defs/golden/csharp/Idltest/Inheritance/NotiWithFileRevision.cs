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
    [JsonConverter(typeof(NotiWithFileRevision_JsonNetConverter))]
    public interface NotiWithFileRevision: NotiWithFile, IRTTI {
        long FileRevision { get; set; }
    }
    public class NotiWithFileRevision_JsonNetConverter: JsonNetConverter<NotiWithFileRevision> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotiWithFileRevision_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotiWithFileRevision value, JsonSerializer serializer) {
            // Serializing polymorphic type NotiWithFileRevision
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotiWithFileRevision ReadJson(JsonReader reader, System.Type objectType, NotiWithFileRevision existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NotiWithFileRevisionStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (NotiWithFileRevision)res;
        }
    }

    [JsonConverter(typeof(NotiWithFileRevisionStruct_JsonNetConverter))]
    public class NotiWithFileRevisionStruct : NotiWithFile, NotiWithFileRevision {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.NotiWithFileRevision";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.NotiWithFileRevision.Struct";
        public string GetPackageName() { return NotiWithFileRevisionStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NotiWithFileRevisionStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NotiWithFileRevisionStruct.RTTI_FULLCLASSNAME; }

        public DateTime At { get; set; }
        public string UserID { get; set; }
        public string UserName { get; set; }
        public string Message { get; set; }
        public long FileID { get; set; }
        public string FileName { get; set; }
        public long FileRevision { get; set; }

        public NotiWithFileRevisionStruct() {
        }

        public NotiWithFileRevisionStruct(DateTime at, string userID, string userName, string message, long fileID, string fileName, long fileRevision) {
            this.At = at;
            this.UserID = userID;
            this.UserName = userName;
            this.Message = message;
            this.FileID = fileID;
            this.FileName = fileName;
            this.FileRevision = fileRevision;
        }

        public NotiWithFile ToNotiWithFile() {
            var res = new NotiWithFileStruct();
            res.At = this.At;
            res.UserID = this.UserID;
            res.UserName = this.UserName;
            res.Message = this.Message;
            res.FileID = this.FileID;
            res.FileName = this.FileName;
            return res;
        }

        public void LoadNotiWithFile(NotiWithFile value) {
            this.At = value.At;
            this.UserID = value.UserID;
            this.UserName = value.UserName;
            this.Message = value.Message;
            this.FileID = value.FileID;
            this.FileName = value.FileName;
        }

        public NotiWithFileRevision ToNotiWithFileRevision() {
            var res = new NotiWithFileRevisionStruct();
            res.At = this.At;
            res.UserID = this.UserID;
            res.UserName = this.UserName;
            res.Message = this.Message;
            res.FileID = this.FileID;
            res.FileName = this.FileName;
            res.FileRevision = this.FileRevision;
            return res;
        }

        public void LoadNotiWithFileRevision(NotiWithFileRevision value) {
            this.At = value.At;
            this.UserID = value.UserID;
            this.UserName = value.UserName;
            this.Message = value.Message;
            this.FileID = value.FileID;
            this.FileName = value.FileName;
            this.FileRevision = value.FileRevision;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NotiWithFileRevisionStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NotiWithFileRevisionStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NotiWithFileRevisionStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface NotiWithFileRevision.");
            }

            return tpe;
        }

        static NotiWithFileRevisionStruct() {
            var type = typeof(NotiWithFileRevision);
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
                            NotiWithFileRevisionStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NotiWithFileRevisionStruct_JsonNetConverter: JsonNetConverter<NotiWithFileRevisionStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotiWithFileRevisionStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotiWithFileRevisionStruct v, JsonSerializer serializer) {
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

            writer.WritePropertyName("fileID");
            writer.WriteValue(v.FileID);
            writer.WritePropertyName("fileName");
            writer.WriteValue(v.FileName);
            writer.WritePropertyName("fileRevision");
            writer.WriteValue(v.FileRevision);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotiWithFileRevisionStruct ReadJson(JsonReader reader, System.Type objectType, NotiWithFileRevisionStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
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

            return new NotiWithFileRevisionStruct(
                DateTime.ParseExact(json["at"].Value<string>(), JsonNetTimeFormats.Tsz, CultureInfo.InvariantCulture, DateTimeStyles.None), 
                json["userID"].Value<string>(), 
                _userName, 
                _message, 
                json["fileID"].Value<long>(), 
                json["fileName"].Value<string>(), 
                json["fileRevision"].Value<long>()
            );
        }
    }
}