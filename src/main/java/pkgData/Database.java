package pkgData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import pkgMisc.GsonLocalDateSerializer;
import pkgMisc.GsonPointSerializer;
import pkgMisc.GsonSerializerObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.near;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

public class Database {
    private static Database instance = null;
    private static String CONNECTSTRING,
            DATABASENAME = "dbRubensZambelli",
            COLLECTION_CARS = "Cars",
            COLLECTION_PETROL_STATIONS = "PetrolStations";


    private static int PORT = 27017;
    private static String HOST = "127.0.0.1";
    private static MongoClient mongoClient = null;
    private  static MongoDatabase mongoDatabase = null;
    private static Gson gson;
    private static Database database = null;

    public static Database getInstance() throws Exception {
        if (database == null) {
            database = new Database();
            CONNECTSTRING = HOST;
            mongoClient = new MongoClient(CONNECTSTRING, PORT);
            mongoDatabase = mongoClient.getDatabase(DATABASENAME);

            // createGeoIndex();

            gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateSerializer())
                    .registerTypeAdapter(ObjectId.class, new GsonSerializerObjectId())
                    .registerTypeAdapter(Point.class, new GsonPointSerializer())
                    .create();
        }
        return database;
    }

    public ObjectId insertCar(Car car) throws Exception {
        MongoCollection collection = mongoDatabase.getCollection(COLLECTION_CARS);
        String jsonString = gson.toJson(car);
        Document doc = Document.parse(jsonString);
        collection.insertOne(doc);
        car.setId(doc.get("_id", ObjectId.class));
        return (ObjectId) doc.get("_id");
    }

    public ObjectId insertPetrolStation(PetrolStation petrolStation) throws Exception {
        MongoCollection collection = mongoDatabase.getCollection(COLLECTION_PETROL_STATIONS);
        String jsonString = gson.toJson(petrolStation);
        Document doc = Document.parse(jsonString);
        collection.insertOne(doc);
        petrolStation.setId(doc.get("_id", ObjectId.class));
        return (ObjectId) doc.get("_id");
    }

    public ArrayList<Car> getAllCars() throws Exception {
        ArrayList<Car> collCars = new ArrayList<>();
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_CARS);

        for (Document doc : collection.find()) {
            collCars.add(generateCarFromDoc(doc));
        }

        return collCars;
    }

    public ArrayList<PetrolStation> getAllPetrolStations() throws Exception {
        ArrayList<PetrolStation> collPetrolStations = new ArrayList<>();
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_PETROL_STATIONS);

        for (Document doc : collection.find()) {
            collPetrolStations.add(generatePetrolStationFromDoc(doc));
        }

        return collPetrolStations;
    }

    public ArrayList<PetrolStation> getSortedPetrolStations() throws Exception {
        ArrayList<PetrolStation> collSortedPetrolStations = new ArrayList<>();
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_PETROL_STATIONS);

        for (Document doc : collection.find().sort(orderBy(ascending("stationName")))) {
            collSortedPetrolStations.add(generatePetrolStationFromDoc(doc));
        }

        return collSortedPetrolStations;
    }

    public ArrayList<Car> getSortedCars() throws Exception {
        ArrayList<Car> collSortedCars = new ArrayList<>();
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_CARS);

        Bson projection = Projections.exclude("positions");

        for (Document doc : collection.find().projection(projection).sort(orderBy(ascending("carName")))) {
            collSortedCars.add(generateCarFromDoc(doc));
        }

        return collSortedCars;
    }

    public Car getSortedPositions(ObjectId carId) throws Exception {
        Car car = getCarByIdWithoutName(carId);

        car.getPositions().sort( new Comparator<GeoPosition>() {
            @Override
            public int compare(GeoPosition pos1, GeoPosition pos2) {
                return -pos1.getDateTime().compareTo(pos2.getDateTime());
            }
        });

        return car;
    }

    private Car generateCarFromDoc(Document doc) throws Exception {
        String jsonString = doc.toJson();
        Car car = gson.fromJson(jsonString, Car.class);
        System.out.println("==>" + car.getId());

        return car;
    }

    private PetrolStation generatePetrolStationFromDoc(Document doc) throws Exception {
        String jsonString = doc.toJson();
        PetrolStation petrolStation = gson.fromJson(jsonString, PetrolStation.class);
        System.out.println("==>" + petrolStation.getId());

        return petrolStation;
    }

    public Car getCarById(ObjectId id) throws Exception {
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_CARS);
        Document doc = collection.find(Filters.eq("_id", id)).first();

        if (doc == null) {
            throw new Exception("Car with ID " + id + " not found");
        }

        return generateCarFromDoc(doc);
    }

    public Car getCarByIdWithoutName(ObjectId id) throws Exception {
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_CARS);

        Bson projection = Projections.exclude("carName");

        Document doc = collection.find(Filters.eq("_id", id)).projection(projection).first();

        if (doc == null) {
            throw new Exception("Car with ID " + id + " not found");
        }

        return generateCarFromDoc(doc);
    }

    public Car deleteCar(ObjectId carId) throws Exception {
        Car deletedCar = getCarById(carId);
        MongoCollection collection = mongoDatabase.getCollection(COLLECTION_CARS);

        return deletedCar;
    }

    public void initCars() throws Exception {
        Random rand = new Random();

        for(int i = 0; i < 5; i++) {
            Car car = new Car("Car"+(rand.nextInt(9) + 1), new ArrayList<GeoPosition>());

            simulateMovements(car);

            insertCar(car);
        }
    }

    public long updateCar(Car car) {
        MongoCollection collection = mongoDatabase.getCollection(COLLECTION_CARS);
        String jsonString = gson.toJson(car);
        Document doc = Document.parse(jsonString);

        doc.remove("_id"); // oid must be updated
        UpdateResult ur = collection.updateOne(eq("_id", car.getId()), new Document("$set", doc));

        return ur.getModifiedCount();
    }

    public void simulateCarMovements(ObjectId carId) throws Exception {
        Car car = getCarById(carId);
        simulateMovements(car);
        updateCar(car);
    }

    private void simulateMovements(Car car) {
        Random rand = new Random();
        final double deltaLongPerKm = 0.1 / 8.5;
        final double deltaLatPerKm = 0.1 / 14.5;
        final double minLong = 46.669574;
        final double minLat = 12.669993;
        final double maxLong = 47.044382;
        final double maxLat = 14.960316;
        LocalDateTime time = LocalDateTime.now();
        Point currentPosition;

        if(car.getPositions().size() == 0) {
            double longitude = minLong + (maxLong - minLong) * rand.nextDouble();
            double latitude = minLat + (maxLat - minLat) * rand.nextDouble();
            currentPosition = new Point(new Position(longitude, latitude));
        } else {
            // time = car.getPositions().get(car.getPositions().size()-1).getDateTime();
            currentPosition = car.getPositions().get(car.getPositions().size()-1).getLocation();
        }

        for(int j = 0; j < 5; j++) {
            if (rand.nextBoolean()) {
                double deltaLongitude = (rand.nextBoolean() ? 1 : -1) * deltaLongPerKm;
                currentPosition = new Point(new Position(currentPosition.getPosition().getValues().get(0) + deltaLongitude, currentPosition.getPosition().getValues().get(1)));
            } else {
                double deltaLatitude = (rand.nextBoolean() ? 1 : -1) * deltaLatPerKm;
                currentPosition = new Point(new Position(currentPosition.getPosition().getValues().get(0), currentPosition.getPosition().getValues().get(1) + deltaLatitude));
            }

            time = time.plusMinutes(1);

            car.getPositions().add(new GeoPosition(currentPosition, time));
        }
    }

    public void initPetrolStations() throws Exception {
        Random rand = new Random();
        double minLong = 46.669574;
        double minLat = 12.669993;
        double maxLong = 47.044382;
        double maxLat = 14.960316;

        for(int i = 0; i < 5; i++) {
            double longitude = minLong + (maxLong - minLong) * rand.nextDouble();
            double latitude = minLat + (maxLat - minLat) * rand.nextDouble();

            PetrolStation petrolStation = new PetrolStation("Station" + (rand.nextInt(9) + 1), new Point(new Position(longitude, latitude)));

            insertPetrolStation(petrolStation);
        }

    }

    public void createGeoIndex() {
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_PETROL_STATIONS);
        collection.dropIndexes();
        collection.createIndex(Indexes.geo2dsphere("position"));
    }

    public ArrayList<PetrolStation> findNearPetrolStations(Point position, double withinDistance) throws Exception {
        MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_PETROL_STATIONS);
        ArrayList<PetrolStation> result = new ArrayList<>();

        Bson query = near("position", position, withinDistance, 0.);

        // loop for locations that are near
        for (Document doc : collection.find(query)) {
            PetrolStation station = generatePetrolStationFromDoc(doc);
            result.add(station);
        }

        return result;
    }
}