// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Idltest.Upcasts {
    [JsonConverter(typeof(Item_JsonNetConverter))]
    public class Item {
        public static readonly string RTTI_PACKAGE = "idltest.upcasts";
        public static readonly string RTTI_CLASSNAME = "Item";
        public static readonly string RTTI_FULLCLASSNAME = "idltest.upcasts.Item";
        public string GetPackageName() { return Item.RTTI_PACKAGE; }
        public string GetClassName() { return Item.RTTI_CLASSNAME; }
        public string GetFullClassName() { return Item.RTTI_FULLCLASSNAME; }

        public Guid Id { get; set; }
        public string Name { get; set; }
        public int Price { get; set; }

        public Item() {
        }

        public Item(Guid id, string name, int price) {
            this.Id = id;
            this.Name = name;
            this.Price = price;
        }

    }
    public class Item_JsonNetConverter: JsonNetConverter<Item> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public Item_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, Item v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WritePropertyName("name");
            writer.WriteValue(v.Name);
            writer.WritePropertyName("price");
            writer.WriteValue(v.Price);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override Item ReadJson(JsonReader reader, System.Type objectType, Item existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);

            return new Item(
                new System.Guid(json["id"].Value<string>()), 
                json["name"].Value<string>(), 
                json["price"].Value<int>()
            );
        }
    }
}