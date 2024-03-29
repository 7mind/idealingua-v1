
using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace IRT.Marshaller {
    public class JsonNetMarshaller: IJsonMarshaller
    {
        private readonly JsonSerializerSettings settings;
        private readonly JsonSerializer serializer;

        public JsonNetMarshaller(bool pretty = false)
        {
            settings = new JsonSerializerSettings();
            Settings.Converters.Add(new StringEnumConverter());
            Settings.NullValueHandling = NullValueHandling.Ignore;
            Settings.TypeNameHandling = TypeNameHandling.None;
            Settings.ReferenceLoopHandling = ReferenceLoopHandling.Serialize;
            Settings.DateParseHandling = DateParseHandling.None;
            Settings.Formatting = pretty ? Formatting.Indented : Formatting.None;

            serializer = JsonSerializer.Create(settings);
        }

        public JsonSerializerSettings Settings => settings;
        public JsonSerializer Serializer => serializer;

        public string Marshal<I>(I data)
        {
            var ti = typeof(I);
            // For void responses, we need to provide something that wouldn't break the marshaller
            if (ti == typeof(IRT.Void))
            {
                return "{}";
            }
            if (ti.IsInterface)
            {
                if (!(data is IRTTI))
                {
                    throw new Exception("Trying to serialize an interface which doesn't expose an IRTTI interface: " +
                                        typeof(I).ToString());
                }
                return JsonConvert.SerializeObject(new InterfaceMarshalWorkaround(data as IRTTI), Settings);
            }
            else
            {
                return JsonConvert.SerializeObject(data, typeof(I), Settings);
            }
        }

        public O Unmarshal<O>(string data)
        {
            return JsonConvert.DeserializeObject<O>(data, Settings);
        }

        [JsonConverter(typeof(InterfaceMarshalWorkaround_JsonNetConverter))]
        private class InterfaceMarshalWorkaround {
            public IRTTI Value;
            public InterfaceMarshalWorkaround(IRTTI value) {
                Value = value;
            }
        }

        private class InterfaceMarshalWorkaround_JsonNetConverter: JsonNetConverter<InterfaceMarshalWorkaround> {
            public override void WriteJson(JsonWriter writer, InterfaceMarshalWorkaround holder, JsonSerializer serializer) {
                // Serializing polymorphic type
                writer.WriteStartObject();
                writer.WritePropertyName(holder.Value.GetFullClassName());
                serializer.Serialize(writer, holder.Value);
                writer.WriteEndObject();

            }

            public override InterfaceMarshalWorkaround ReadJson(JsonReader reader, System.Type objectType, InterfaceMarshalWorkaround existingValue, bool hasExistingValue, JsonSerializer serializer) {
                throw new Exception("Should not be used for Reading, workaround only for writing.");
            }
        }
    }

    // From here https://github.com/JamesNK/Newtonsoft.Json/blob/master/Src/Newtonsoft.Json/JsonConverter.cs
    public abstract class JsonNetConverter<T> : JsonConverter
    {
        public sealed override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
        {
            WriteJson(writer, (T) value, serializer);
        }

        public abstract void WriteJson(JsonWriter writer, T value, JsonSerializer serializer);

        public sealed override object ReadJson(JsonReader reader, System.Type objectType, object existingValue,
            JsonSerializer serializer)
        {
            return ReadJson(reader, objectType, default(T), false, serializer);
        }

        public abstract T ReadJson(JsonReader reader, System.Type objectType, T existingValue, bool hasExistingValue,
            JsonSerializer serializer);

        public sealed override bool CanConvert(Type objectType)
        {
            return typeof(T).IsAssignableFrom(objectType);
        }
    }

    public static class JsonNetTimeFormats {
        public static readonly string TslDefault = "yyyy-MM-ddTHH:mm:ss.fff";
        public static readonly string[] Tsl = new string[] {
                    "yyyy-MM-ddTHH:mm:ss",
                    "yyyy-MM-ddTHH:mm:ss.f",
                    "yyyy-MM-ddTHH:mm:ss.ff",
                    "yyyy-MM-ddTHH:mm:ss.fff",
                    "yyyy-MM-ddTHH:mm:ss.ffff",
                    "yyyy-MM-ddTHH:mm:ss.fffff",
                    "yyyy-MM-ddTHH:mm:ss.ffffff",
                    "yyyy-MM-ddTHH:mm:ss.fffffff",
                    "yyyy-MM-ddTHH:mm:ss.ffffffff",
                    "yyyy-MM-ddTHH:mm:ss.fffffffff"
                };

        public static readonly string TszDefault = "yyyy-MM-ddTHH:mm:ss.fffzzz";
        public static readonly string[] Tsz = new string[] {
                   "yyyy-MM-ddTHH:mm:ssZ",
                   "yyyy-MM-ddTHH:mm:ss.fZ",
                   "yyyy-MM-ddTHH:mm:ss.ffZ",
                   "yyyy-MM-ddTHH:mm:ss.fffZ",
                   "yyyy-MM-ddTHH:mm:ss.ffffZ",
                   "yyyy-MM-ddTHH:mm:ss.fffffZ",
                   "yyyy-MM-ddTHH:mm:ss.ffffffZ",
                   "yyyy-MM-ddTHH:mm:ss.fffffffZ",
                   "yyyy-MM-ddTHH:mm:ss.ffffffffZ",
                   "yyyy-MM-ddTHH:mm:ss.fffffffffZ",
                   "yyyy-MM-ddTHH:mm:sszzz",
                   "yyyy-MM-ddTHH:mm:ss.fzzz",
                   "yyyy-MM-ddTHH:mm:ss.ffzzz",
                   "yyyy-MM-ddTHH:mm:ss.fffzzz",
                   "yyyy-MM-ddTHH:mm:ss.ffffzzz",
                   "yyyy-MM-ddTHH:mm:ss.fffffzzz",
                   "yyyy-MM-ddTHH:mm:ss.ffffffzzz",
                   "yyyy-MM-ddTHH:mm:ss.fffffffzzz",
                   "yyyy-MM-ddTHH:mm:ss.ffffffffzzz",
                   "yyyy-MM-ddTHH:mm:ss.fffffffffzzz"
                };

        public static readonly string TsuDefault = "yyyy-MM-ddTHH:mm:ss.fffZ";
        public static readonly string[] Tsu = JsonNetTimeFormats.Tsz;
    }

    class JsonNetDateConverter : IsoDateTimeConverter {
        public JsonNetDateConverter() {
            base.DateTimeFormat = "yyyy-MM-dd";
        }
    }

    class JsonNetTimeConverter : IsoDateTimeConverter {
        public JsonNetTimeConverter() {
            base.DateTimeFormat = "HH:mm:ss.fff";
        }
    }

    class JsonNetDateTimeLocalConverter : IsoDateTimeConverter {
        public JsonNetDateTimeLocalConverter() {
            base.DateTimeFormat = "yyyy-MM-ddTHH:mm:ss.fff";
        }
    }

    class JsonNetDateTimeZonedConverter : IsoDateTimeConverter {
        public JsonNetDateTimeZonedConverter() {
            base.DateTimeFormat = "yyyy-MM-ddTHH:mm:ss.fffzzz";
        }
    }
}