// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Inheritance {
    [JsonConverter(typeof(Point_JsonNetConverter))]
    public class Point : Metadata {
        public static readonly string RTTI_PACKAGE = "idltest.inheritance";
        public static readonly string RTTI_CLASSNAME = "Point";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.inheritance.Point";
        public string GetPackageName() { return Point.RTTI_PACKAGE; }
        public string GetClassName() { return Point.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Point.RTTI_FULLCLASSNAME; }

        public string Id { get; set; }
        public string Name { get; set; }
        public int X { get; set; }
        public int Y { get; set; }

        public Point() {
        }

        public Point(string id, string name, int x, int y) {
            this.Id = id;
            this.Name = name;
            this.X = x;
            this.Y = y;
        }

        public Metadata ToMetadata() {
            var res = new MetadataStruct();
            res.Id = this.Id;
            res.Name = this.Name;
            return res;
        }

        public void LoadMetadata(Metadata value) {
            this.Id = value.Id;
            this.Name = value.Name;
        }

    }
    public class Point_JsonNetConverter: JsonNetConverter<Point> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Point_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Point v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id);
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("x");
            writer.WriteValue(v.X);
            writer.WritePropertyName("y");
            writer.WriteValue(v.Y);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Point ReadJson(JsonReader reader, System.Type objectType, Point existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Point(
                json["id"].Value<string>(), 
                json["name"].Value<string>(), 
                json["x"].Value<int>(), 
                json["y"].Value<int>()
            );
        }
    }
}