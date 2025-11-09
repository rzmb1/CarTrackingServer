package pkgMisc;

import com.google.gson.*;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

import java.lang.reflect.Type;

public class GsonPointSerializer implements JsonSerializer<Point>, JsonDeserializer<Point> {
    @Override
    public JsonElement serialize(Point point, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        JsonArray arrCoo = new JsonArray();
        arrCoo.add(point.getCoordinates().getValues().get(0));
        arrCoo.add(point.getCoordinates().getValues().get(1));
        jo.addProperty("type","Point");
        jo.add("coordinates",arrCoo);
        return jo;
    }

    @Override
    public Point deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new Point(new Position(jsonElement.getAsJsonObject().get("coordinates").getAsJsonArray().get(0).getAsDouble(),jsonElement.getAsJsonObject().get("coordinates").getAsJsonArray().get(1).getAsDouble()));
    }
}