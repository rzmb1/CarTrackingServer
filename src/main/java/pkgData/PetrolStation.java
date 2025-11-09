package pkgData;

import com.mongodb.client.model.geojson.Point;
import org.bson.types.ObjectId;

public class PetrolStation {
    private String stationName;
    private ObjectId _id;
    private Point position;

    public PetrolStation(String stationName, Point position) {
        this.stationName = stationName;
        this.position = position;
    }

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }
}
