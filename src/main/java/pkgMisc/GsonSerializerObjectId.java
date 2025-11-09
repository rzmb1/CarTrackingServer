package pkgMisc;

import com.google.gson.*;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;

public class GsonSerializerObjectId implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new ObjectId(jsonElement.getAsJsonObject().get("$oid").getAsString());
    }

    @Override
    public JsonElement serialize(ObjectId objectId, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("$oid", objectId.toString());
        return jsonObject;
    }
}