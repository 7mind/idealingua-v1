// Auto-generated, any modifications may be overwritten in the future.

using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IRT.Marshaller;

namespace Overlaytest.Withoverlay {
    [JsonConverter(typeof(User_JsonNetConverter))]
    public class User {
        public static readonly string RTTI_PACKAGE = "overlaytest.withoverlay";
        public static readonly string RTTI_CLASSNAME = "User";
        public static readonly string RTTI_FULLCLASSNAME = "overlaytest.withoverlay.User";
        public string GetPackageName() { return User.RTTI_PACKAGE; }
        public string GetClassName() { return User.RTTI_CLASSNAME; }
        public string GetFullClassName() { return User.RTTI_FULLCLASSNAME; }

        public Guid Id { get; set; }
        public Overlaytest.Withoverlay.OverlayUserAttributes Attributes { get; set; }

        public User() {
        }

        public User(Guid id, Overlaytest.Withoverlay.OverlayUserAttributes attributes) {
            this.Id = id;
            this.Attributes = attributes;
        }

    }
    public class User_JsonNetConverter: JsonNetConverter<User> {
    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public User_JsonNetConverter() {}

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override void WriteJson(JsonWriter writer, User v, JsonSerializer serializer) {
            writer.WriteStartObject();
            writer.WritePropertyName("id");
            writer.WriteValue(v.Id.ToString());
            writer.WritePropertyName("attributes");
            serializer.Serialize(writer, v.Attributes);
            writer.WriteEndObject();
        }

    #if UNITY_5_3_OR_NEWER
        [UnityEngine.Scripting.RequiredMember]
    #endif
        public override User ReadJson(JsonReader reader, System.Type objectType, User existingValue, bool hasExistingValue, JsonSerializer serializer) {
            var json = JObject.Load(reader);
            var _attributes = serializer.Deserialize<Overlaytest.Withoverlay.OverlayUserAttributes>(json["attributes"].CreateReader());
            return new User(
                new System.Guid(json["id"].Value<string>()), 
                _attributes
            );
        }
    }
}