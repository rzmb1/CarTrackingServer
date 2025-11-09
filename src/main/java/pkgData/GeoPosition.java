package pkgData;

import com.mongodb.client.model.geojson.Point;

import java.time.LocalDateTime;

public class GeoPosition {
    private Point location;
    private LocalDateTime dateTime;

    public GeoPosition(Point location, LocalDateTime dateTime) {
        this.location = location;
        this.dateTime = dateTime;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
