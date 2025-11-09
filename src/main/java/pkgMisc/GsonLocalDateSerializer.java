package pkgMisc;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class GsonLocalDateSerializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
        long millis = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("$date", millis);
        return jsonObject;
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        long time = json.getAsJsonObject().get("$date").getAsLong();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
    }
}
