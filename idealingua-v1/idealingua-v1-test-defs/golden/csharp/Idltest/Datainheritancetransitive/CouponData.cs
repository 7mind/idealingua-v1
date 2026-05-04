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

namespace Idltest.Datainheritancetransitive {
    [JsonConverter(typeof(CouponData_JsonNetConverter))]
    public interface CouponData: IRTTI {
        Nullable<DateTime> ValidFrom { get; set; }
        Nullable<DateTime> ValidTill { get; set; }
        string Code { get; set; }
    }
    public class CouponData_JsonNetConverter: JsonNetConverter<CouponData> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CouponData_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CouponData value, JsonSerializer serializer) {
            // Serializing polymorphic type CouponData
            writer.WriteStartObject();
            writer.WritePropertyName(value.GetFullClassName());
            serializer.Serialize(writer, value);
            writer.WriteEndObject();

        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CouponData ReadJson(JsonReader reader, System.Type objectType, CouponData existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var kv = json.Properties().First();
            var v_tpe = CouponDataStruct.GetType(kv.Name);
            var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
            return (CouponData)res;
        }
    }

    [JsonConverter(typeof(CouponDataStruct_JsonNetConverter))]
    public class CouponDataStruct : CouponData {
        public static readonly string RTTI_PACKAGE = "idltest.datainheritancetransitive.CouponData";
        public static readonly string RTTI_CLASSNAME = "Struct";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.datainheritancetransitive.CouponData.Struct";
        public string GetPackageName() { return CouponDataStruct.RTTI_PACKAGE; }
        public string GetClassName() { return CouponDataStruct.RTTI_CLASSNAME; }
        public string GetFullClassName() { return CouponDataStruct.RTTI_FULLCLASSNAME; }

        public Nullable<DateTime> ValidFrom { get; set; }
        public Nullable<DateTime> ValidTill { get; set; }
        public string Code { get; set; }

        public CouponDataStruct() {
        }

        public CouponDataStruct(Nullable<DateTime> validFrom, Nullable<DateTime> validTill, string code) {
            this.ValidFrom = validFrom;
            this.ValidTill = validTill;
            this.Code = code;
        }

        public CouponData ToCouponData() {
            var res = new CouponDataStruct();
            res.ValidFrom = this.ValidFrom;
            res.ValidTill = this.ValidTill;
            res.Code = this.Code;
            return res;
        }

        public void LoadCouponData(CouponData value) {
            this.ValidFrom = value.ValidFrom;
            this.ValidTill = value.ValidTill;
            this.Code = value.Code;
        }

        private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
        public static void Register(string id, System.Type tpe) {
            CouponDataStruct.__types[id] = tpe;
        }

        public static void Unregister(string id) {
            CouponDataStruct.__types.Remove(id);
        }

        public static System.Type GetType(string id) {
            if (!CouponDataStruct.__types.TryGetValue(id, out var tpe)) {
                throw new Exception("Unknown class name: " + id + " for interface CouponData.");
            }

            return tpe;
        }

        static CouponDataStruct() {
            var type = typeof(CouponData);
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
                            CouponDataStruct.Register((string)rttiID.GetValue(null), tp);
                        }
                    }
                }
            }
        }

    }
    public class CouponDataStruct_JsonNetConverter: JsonNetConverter<CouponDataStruct> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public CouponDataStruct_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, CouponDataStruct v, JsonSerializer serializer) {
            writer.WriteStartObject();
            if (v.ValidFrom.HasValue) {
                writer.WritePropertyName("validFrom");
                writer.WriteValue(v.ValidFrom.Value.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));
            }

            if (v.ValidTill.HasValue) {
                writer.WritePropertyName("validTill");
                writer.WriteValue(v.ValidTill.Value.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));
            }

            writer.WritePropertyName("code");
            writer.WriteValue(v.Code);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override CouponDataStruct ReadJson(JsonReader reader, System.Type objectType, CouponDataStruct existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            Nullable<DateTime> _validFrom = null;
            var _validFromRaw = json["validFrom"];
            if (_validFromRaw != null && _validFromRaw.Type != JTokenType.Null) {
                _validFrom = DateTime.ParseExact(_validFromRaw.Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None);
            }

            Nullable<DateTime> _validTill = null;
            var _validTillRaw = json["validTill"];
            if (_validTillRaw != null && _validTillRaw.Type != JTokenType.Null) {
                _validTill = DateTime.ParseExact(_validTillRaw.Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None);
            }

            return new CouponDataStruct(
                _validFrom, 
                _validTill, 
                json["code"].Value<string>()
            );
        }
    }
}