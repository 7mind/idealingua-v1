// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Dtofields {
    [JsonConverter(typeof(Point_JsonNetConverter))]
    public class Point : Metadata, WHPair {
        public static readonly string RTTI_PACKAGE = "idltest.dtofields";
        public static readonly string RTTI_CLASSNAME = "Point";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.dtofields.Point";
        public string GetPackageName() { return Point.RTTI_PACKAGE; }
        public string GetClassName() { return Point.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Point.RTTI_FULLCLASSNAME; }

        public int W { get; set; }
        public int H { get; set; }
        public string Id { get; set; }
        public string Name { get; set; }
        public int X { get; set; }
        public int Y { get; set; }
        public string Ownfield { get; set; }
        public bool Export { get; set; }

        public Point() {
        }

        public Point(int w, int h, string id, string name, int x, int y, string ownfield, bool export) {
            this.W = w;
            this.H = h;
            this.Id = id;
            this.Name = name;
            this.X = x;
            this.Y = y;
            this.Ownfield = ownfield;
            this.Export = export;
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

        public WHPair ToWHPair() {
            var res = new WHPairStruct();
            res.W = this.W;
            res.H = this.H;
            return res;
        }

        public void LoadWHPair(WHPair value) {
            this.W = value.W;
            this.H = value.H;
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
            writer.WritePropertyName("w");
            writer.WriteValue(v.W);
            writer.WritePropertyName("h");
            writer.WriteValue(v.H);
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id);
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("x");
            writer.WriteValue(v.X);
            writer.WritePropertyName("y");
            writer.WriteValue(v.Y);
            writer.WritePropertyName("ownfield");
            writer.WriteValue(v.Ownfield);
            writer.WritePropertyName("export");
            writer.WriteValue(v.Export);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Point ReadJson(JsonReader reader, System.Type objectType, Point existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Point(
                json["w"].Value<int>(), 
                json["h"].Value<int>(), 
                json["id"].Value<string>(), 
                json["name"].Value<string>(), 
                json["x"].Value<int>(), 
                json["y"].Value<int>(), 
                json["ownfield"].Value<string>(), 
                json["export"].Value<bool>()
            );
        }
    }
}