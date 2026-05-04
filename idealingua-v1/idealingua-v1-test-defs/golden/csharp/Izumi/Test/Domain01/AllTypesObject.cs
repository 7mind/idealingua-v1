// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using IRT;
using System.Globalization;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Izumi.Test.Domain01 {
    [JsonConverter(typeof(AllTypesObject_JsonNetConverter))]
    public class AllTypesObject : AllTypes {
        public static readonly string RTTI_PACKAGE = "izumi.test.domain01";
        public static readonly string RTTI_CLASSNAME = "AllTypesObject";
        public static readonly string RTTI_FULLCLASSNAME = "izumi.test.domain01.AllTypesObject";
        public string GetPackageName() { return AllTypesObject.RTTI_PACKAGE; }
        public string GetClassName() { return AllTypesObject.RTTI_CLASSNAME; }
        public string GetFullClassName() { return AllTypesObject.RTTI_FULLCLASSNAME; }

        public bool B { get; set; }
        public string S { get; set; }
        public sbyte Int8 { get; set; }
        public short Int16 { get; set; }
        public int Int32 { get; set; }
        public long Int64 { get; set; }
        public float F { get; set; }
        public double D { get; set; }
        public Guid Uuid { get; set; }
        public DateTime Ts { get; set; }
        public DateTime Tslocal { get; set; }
        public DateTime Tsuni { get; set; }
        public TimeSpan Time { get; set; }
        public DateTime Date { get; set; }
        public byte Uint8 { get; set; }
        public ushort Uint16 { get; set; }
        public uint Uint32 { get; set; }
        public ulong Uint64 { get; set; }
        public List<Izumi.Test.Domain01.AllTypes> List { get; set; }
        public List<Izumi.Test.Domain01.AllTypes> Another { get; set; }
        public Dictionary<string, Izumi.Test.Domain01.AllTypes> SelfMap { get; set; }
        public Dictionary<string, Izumi.Test.Domain01.GoAliasEnumTest> EnumMap { get; set; }
        public Izumi.Test.Domain01.AllTypes Option { get; set; }
        public List<Izumi.Test.Domain01.AllTypes> SelfSet { get; set; }
        public Nullable<DateTime> OptionDate { get; set; }
        public Nullable<TimeSpan> OptionTime { get; set; }

        public AllTypesObject() {
            List = new List<Izumi.Test.Domain01.AllTypes>();
            Another = new List<Izumi.Test.Domain01.AllTypes>();
            SelfMap = new Dictionary<string, Izumi.Test.Domain01.AllTypes>();
            EnumMap = new Dictionary<string, Izumi.Test.Domain01.GoAliasEnumTest>();
            SelfSet = new List<Izumi.Test.Domain01.AllTypes>();
        }

        public AllTypesObject(bool b, string s, sbyte int8, short int16, int int32, long int64, float f, double d, Guid uuid, DateTime ts, DateTime tslocal, DateTime tsuni, TimeSpan time, DateTime date, byte uint8, ushort uint16, uint uint32, ulong uint64, List<Izumi.Test.Domain01.AllTypes> list, List<Izumi.Test.Domain01.AllTypes> another, Dictionary<string, Izumi.Test.Domain01.AllTypes> selfMap, Dictionary<string, Izumi.Test.Domain01.GoAliasEnumTest> enumMap, Izumi.Test.Domain01.AllTypes option, List<Izumi.Test.Domain01.AllTypes> selfSet, Nullable<DateTime> optionDate, Nullable<TimeSpan> optionTime) {
            this.B = b;
            this.S = s;
            this.Int8 = int8;
            this.Int16 = int16;
            this.Int32 = int32;
            this.Int64 = int64;
            this.F = f;
            this.D = d;
            this.Uuid = uuid;
            this.Ts = ts;
            this.Tslocal = tslocal;
            this.Tsuni = tsuni;
            this.Time = time;
            this.Date = date;
            this.Uint8 = uint8;
            this.Uint16 = uint16;
            this.Uint32 = uint32;
            this.Uint64 = uint64;
            this.List = list;
            this.Another = another;
            this.SelfMap = selfMap;
            this.EnumMap = enumMap;
            this.Option = option;
            this.SelfSet = selfSet;
            this.OptionDate = optionDate;
            this.OptionTime = optionTime;
        }

        public AllTypes ToAllTypes() {
            var res = new AllTypesStruct();
            res.B = this.B;
            res.S = this.S;
            res.Int8 = this.Int8;
            res.Int16 = this.Int16;
            res.Int32 = this.Int32;
            res.Int64 = this.Int64;
            res.F = this.F;
            res.D = this.D;
            res.Uuid = this.Uuid;
            res.Ts = this.Ts;
            res.Tslocal = this.Tslocal;
            res.Tsuni = this.Tsuni;
            res.Time = this.Time;
            res.Date = this.Date;
            res.Uint8 = this.Uint8;
            res.Uint16 = this.Uint16;
            res.Uint32 = this.Uint32;
            res.Uint64 = this.Uint64;
            res.List = this.List;
            res.Another = this.Another;
            res.SelfMap = this.SelfMap;
            res.EnumMap = this.EnumMap;
            res.Option = this.Option;
            res.SelfSet = this.SelfSet;
            res.OptionDate = this.OptionDate;
            res.OptionTime = this.OptionTime;
            return res;
        }

        public void LoadAllTypes(AllTypes value) {
            this.B = value.B;
            this.S = value.S;
            this.Int8 = value.Int8;
            this.Int16 = value.Int16;
            this.Int32 = value.Int32;
            this.Int64 = value.Int64;
            this.F = value.F;
            this.D = value.D;
            this.Uuid = value.Uuid;
            this.Ts = value.Ts;
            this.Tslocal = value.Tslocal;
            this.Tsuni = value.Tsuni;
            this.Time = value.Time;
            this.Date = value.Date;
            this.Uint8 = value.Uint8;
            this.Uint16 = value.Uint16;
            this.Uint32 = value.Uint32;
            this.Uint64 = value.Uint64;
            this.List = value.List;
            this.Another = value.Another;
            this.SelfMap = value.SelfMap;
            this.EnumMap = value.EnumMap;
            this.Option = value.Option;
            this.SelfSet = value.SelfSet;
            this.OptionDate = value.OptionDate;
            this.OptionTime = value.OptionTime;
        }

    }
    public class AllTypesObject_JsonNetConverter: JsonNetConverter<AllTypesObject> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public AllTypesObject_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, AllTypesObject v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("b");
            writer.WriteValue(v.B);
            writer.WritePropertyName("s");
            writer.WriteValue(v.S);
            writer.WritePropertyName("int8");
            writer.WriteValue(v.Int8);
            writer.WritePropertyName("int16");
            writer.WriteValue(v.Int16);
            writer.WritePropertyName("int32");
            writer.WriteValue(v.Int32);
            writer.WritePropertyName("int64");
            writer.WriteValue(v.Int64);
            writer.WritePropertyName("f");
            writer.WriteValue(v.F);
            writer.WritePropertyName("d");
            writer.WriteValue(v.D);
            writer.WritePropertyName("uuid");
            writer.WriteValue(v.Uuid.ToString());
            writer.WritePropertyName("ts");
            writer.WriteValue(v.Ts.ToString(v.Ts.Kind == DateTimeKind.Utc ? JsonNetTimeFormats.TsuDefault : JsonNetTimeFormats.TszDefault, CultureInfo.InvariantCulture));
            writer.WritePropertyName("tslocal");
            writer.WriteValue(v.Tslocal.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));
            writer.WritePropertyName("tsuni");
            writer.WriteValue(v.Tsuni.ToUniversalTime().ToString(JsonNetTimeFormats.TsuDefault, CultureInfo.InvariantCulture));
            writer.WritePropertyName("time");
            writer.WriteValue(string.Format("{0:00}:{1:00}:{2:00}.{3:000}", (int)v.Time.TotalHours, v.Time.Minutes, v.Time.Seconds, v.Time.Milliseconds));
            writer.WritePropertyName("date");
            writer.WriteValue(v.Date.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture));
            writer.WritePropertyName("uint8");
            writer.WriteValue(v.Uint8);
            writer.WritePropertyName("uint16");
            writer.WriteValue(v.Uint16);
            writer.WritePropertyName("uint32");
            writer.WriteValue(v.Uint32);
            writer.WritePropertyName("uint64");
            writer.WriteValue(v.Uint64);
            writer.WritePropertyName("list");
            writer.WriteStartArray();
            foreach (var lv in v.List) {
                // Serializing polymorphic type AllTypes
                writer.WriteStartObject();
                writer.WritePropertyName(lv.GetFullClassName());
                serializer.Serialize(writer, lv);
                writer.WriteEndObject();

            }
            writer.WriteEndArray();

            writer.WritePropertyName("another");
            writer.WriteStartArray();
            foreach (var lv in v.Another) {
                // Serializing polymorphic type AllTypes
                writer.WriteStartObject();
                writer.WritePropertyName(lv.GetFullClassName());
                serializer.Serialize(writer, lv);
                writer.WriteEndObject();

            }
            writer.WriteEndArray();

            writer.WritePropertyName("selfMap");
            writer.WriteStartObject();
            foreach(var mkv in v.SelfMap) {
                writer.WritePropertyName(mkv.Key.ToString());
                // Serializing polymorphic type AllTypes
                writer.WriteStartObject();
                writer.WritePropertyName(mkv.Value.GetFullClassName());
                serializer.Serialize(writer, mkv.Value);
                writer.WriteEndObject();

            }
            writer.WriteEndObject();

            writer.WritePropertyName("enumMap");
            writer.WriteStartObject();
            foreach(var mkv in v.EnumMap) {
                writer.WritePropertyName(mkv.Key.ToString());
                writer.WriteValue(mkv.Value.ToString());
            }
            writer.WriteEndObject();

            if (v.Option != null) {
                writer.WritePropertyName("option");
                // Serializing polymorphic type AllTypes
                writer.WriteStartObject();
                writer.WritePropertyName(v.Option.GetFullClassName());
                serializer.Serialize(writer, v.Option);
                writer.WriteEndObject();

            }

            writer.WritePropertyName("selfSet");
            writer.WriteStartArray();
            foreach (var lv in v.SelfSet) {
                // Serializing polymorphic type AllTypes
                writer.WriteStartObject();
                writer.WritePropertyName(lv.GetFullClassName());
                serializer.Serialize(writer, lv);
                writer.WriteEndObject();

            }
            writer.WriteEndArray();

            if (v.OptionDate.HasValue) {
                writer.WritePropertyName("optionDate");
                writer.WriteValue(v.OptionDate.Value.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));
            }

            if (v.OptionTime.HasValue) {
                writer.WritePropertyName("optionTime");
                writer.WriteValue(string.Format("{0:00}:{1:00}:{2:00}.{3:000}", (int)v.OptionTime.Value.TotalHours, v.OptionTime.Value.Minutes, v.OptionTime.Value.Seconds, v.OptionTime.Value.Milliseconds));
            }

            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override AllTypesObject ReadJson(JsonReader reader, System.Type objectType, AllTypesObject existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _list = new List<Izumi.Test.Domain01.AllTypes>();
            foreach (var _list_sv in (JArray)json["list"]) {
                Izumi.Test.Domain01.AllTypes _list_d;
                _list_d = serializer.Deserialize<Izumi.Test.Domain01.AllTypes>(_list_sv.CreateReader());
                _list.Add(_list_d);
            }

            var _another = new List<Izumi.Test.Domain01.AllTypes>();
            foreach (var _another_sv in (JArray)json["another"]) {
                Izumi.Test.Domain01.AllTypes _another_d;
                _another_d = serializer.Deserialize<Izumi.Test.Domain01.AllTypes>(_another_sv.CreateReader());
                _another.Add(_another_d);
            }

            var _selfMap = new Dictionary<string, Izumi.Test.Domain01.AllTypes>();
            foreach (var _selfMap_kv in ((JObject)json["selfMap"]).Properties()) {
                Izumi.Test.Domain01.AllTypes _selfMap_dv;
                _selfMap_dv = serializer.Deserialize<Izumi.Test.Domain01.AllTypes>(_selfMap_kv.Value.CreateReader());
                _selfMap.Add(_selfMap_kv.Name, _selfMap_dv);
            }

            var _enumMap = new Dictionary<string, Izumi.Test.Domain01.GoAliasEnumTest>();
            foreach (var _enumMap_kv in ((JObject)json["enumMap"]).Properties()) {
                Izumi.Test.Domain01.GoAliasEnumTest _enumMap_dv;
                _enumMap_dv = Izumi.Test.Domain01.GoAliasEnumTestHelpers.From(_enumMap_kv.Value.Value<string>());
                _enumMap.Add(_enumMap_kv.Name, _enumMap_dv);
            }

            Izumi.Test.Domain01.AllTypes _option = null;
            var _optionRaw = json["option"];
            if (_optionRaw != null && _optionRaw.Type != JTokenType.Null) {
                _option = serializer.Deserialize<Izumi.Test.Domain01.AllTypes>(_optionRaw.CreateReader());
            }

            var _selfSet = new List<Izumi.Test.Domain01.AllTypes>();
            foreach (var _selfSet_lv in (JArray)json["selfSet"]) {
                Izumi.Test.Domain01.AllTypes _selfSet_d;
                _selfSet_d = serializer.Deserialize<Izumi.Test.Domain01.AllTypes>(_selfSet_lv.CreateReader());
                _selfSet.Add(_selfSet_d);
            }

            Nullable<DateTime> _optionDate = null;
            var _optionDateRaw = json["optionDate"];
            if (_optionDateRaw != null && _optionDateRaw.Type != JTokenType.Null) {
                _optionDate = DateTime.ParseExact(_optionDateRaw.Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None);
            }

            Nullable<TimeSpan> _optionTime = null;
            var _optionTimeRaw = json["optionTime"];
            if (_optionTimeRaw != null && _optionTimeRaw.Type != JTokenType.Null) {
                _optionTime = TimeSpan.Parse(_optionTimeRaw.Value<string>());
            }

            return new AllTypesObject(
                json["b"].Value<bool>(), 
                json["s"].Value<string>(), 
                json["int8"].Value<sbyte>(), 
                json["int16"].Value<short>(), 
                json["int32"].Value<int>(), 
                json["int64"].Value<long>(), 
                json["f"].Value<float>(), 
                json["d"].Value<double>(), 
                new System.Guid(json["uuid"].Value<string>()), 
                DateTime.ParseExact(json["ts"].Value<string>(), JsonNetTimeFormats.Tsz, CultureInfo.InvariantCulture, DateTimeStyles.None), 
                DateTime.ParseExact(json["tslocal"].Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None), 
                DateTime.ParseExact(json["tsuni"].Value<string>(), JsonNetTimeFormats.Tsu, CultureInfo.InvariantCulture, DateTimeStyles.None), 
                TimeSpan.Parse(json["time"].Value<string>()), 
                DateTime.Parse(json["date"].Value<string>(), CultureInfo.InvariantCulture), 
                json["uint8"].Value<byte>(), 
                json["uint16"].Value<ushort>(), 
                json["uint32"].Value<uint>(), 
                json["uint64"].Value<ulong>(), 
                _list, 
                _another, 
                _selfMap, 
                _enumMap, 
                _option, 
                _selfSet, 
                _optionDate, 
                _optionTime
            );
        }
    }
}