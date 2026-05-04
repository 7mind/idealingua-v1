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
    [JsonConverter(typeof(NotiWithFile_JsonNetConverter))]
    public interface NotiWithFile: NotiBase, IRTTI {
        long FileID { get; set; }
        string FileName { get; set; }
    }
    public class NotiWithFile_JsonNetConverter: JsonNetConverter<NotiWithFile> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotiWithFile_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotiWithFile value, JsonSerializer serializer) {
            // Serializing polymorphic type NotiWithFile
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotiWithFile ReadJson(JsonReader reader, System.Type objectType, NotiWithFile existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = NotiWithFileStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (NotiWithFile)res;
        }
    }

    [JsonConverter(typeof(NotiWithFileStruct_JsonNetConverter))]
    public class NotiWithFileStruct : NotiBase, NotiWithFile {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance.NotiWithFile";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.NotiWithFile.Struct";
        public string GetPackageName() { return NotiWithFileStruct.RTTI_PACKAGE; }
        public string GetClassName() { return NotiWithFileStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return NotiWithFileStruct.RTTI_FULLCLASSNAME; }

        public DateTime At { get; set; }
        public string UserID { get; set; }
        public string UserName { get; set; }
        public string Message { get; set; }
        public long FileID { get; set; }
        public string FileName { get; set; }

        public NotiWithFileStruct() {
        }

        public NotiWithFileStruct(DateTime at, string userID, string userName, string message, long fileID, string fileName) {
            this.At = at;
            this.UserID = userID;
            this.UserName = userName;
            this.Message = message;
            this.FileID = fileID;
            this.FileName = fileName;
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

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            NotiWithFileStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            NotiWithFileStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!NotiWithFileStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface NotiWithFile.");
            }

            return tpe;
        }

        static NotiWithFileStruct() {
            var type = typeof(NotiWithFile);
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
                            NotiWithFileStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class NotiWithFileStruct_JsonNetConverter: JsonNetConverter<NotiWithFileStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public NotiWithFileStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, NotiWithFileStruct v, JsonSerializer serializer) {
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
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override NotiWithFileStruct ReadJson(JsonReader reader, System.Type objectType, NotiWithFileStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
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

            return new NotiWithFileStruct(
                DateTime.ParseExact(json["at"].Value<string>(), JsonNetTimeFormats.Tsz, CultureInfo.InvariantCulture, DateTimeStyles.None), 
                json["userID"].Value<string>(), 
                _userName, 
                _message, 
                json["fileID"].Value<long>(), 
                json["fileName"].Value<string>()
            );
        }
    }
}