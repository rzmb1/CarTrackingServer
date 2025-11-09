package pkgService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.geojson.Point;
import org.bson.types.ObjectId;
import pkgData.Car;
import pkgData.Database;
import pkgMisc.GsonLocalDateSerializer;
import pkgMisc.GsonPointSerializer;
import pkgMisc.GsonSerializerObjectId;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDateTime;

@Path("cars")
@RequestScoped
public class Cars {
    @Context
    private UriInfo context;

    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(ObjectId.class, new GsonSerializerObjectId())
            .create();

    public Cars() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCars() {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            response.entity(gson.toJson(db.getAllCars()));
        } catch (Exception e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service GET LIST called");
        return response.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("sorted")
    public Response getSortedCars() {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            response.entity(gson.toJson(db.getSortedCars()));
        } catch (Exception e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service GET LIST called");
        return response.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("near-stations/{carid}")
    public Response getNearPetrolStations(@PathParam("carid") String id, @QueryParam("distance") double distance) {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            Car car = db.getCarById(new ObjectId(id));
            Point position = car.getPositions().get(car.getPositions().size()-1).getLocation();
            response.entity(gson.toJson(db.findNearPetrolStations(position, distance)));
        } catch (Exception e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service GET NEAR called");
        return response.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("simulate-car-movements/{carid}")
    public Response simulateMovements(@PathParam("carid") String id) {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            db.simulateCarMovements(new ObjectId(id));
            response.entity(new Gson().toJson(response.entity(new Gson().toJson("successfully simulated 5 car movements"))));
        } catch (Exception e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service SIMULATE called");
        return response.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("sorted/{carid}")
    public Response getSortedCar(@PathParam("carid") String id) throws Exception {
        Database db = Database.getInstance();
        Response.ResponseBuilder response = Response.status(Response.Status.OK);

        try {
            pkgData.Car retCar = db.getSortedPositions(new ObjectId(id));
            response.entity(gson.toJson(retCar));
        } catch (Exception e) {
            response.status(Response.Status.NOT_FOUND);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service GET called");
        return response.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{carid}")
    public Response getCar(@PathParam("carid") String id) throws Exception {
        Database db = Database.getInstance();
        Response.ResponseBuilder response = Response.status(Response.Status.OK);

        try {
            pkgData.Car retCar = db.getCarById(new ObjectId(id));
            response.entity(new Gson().toJson(retCar));
        } catch (Exception e) {
            response.status(Response.Status.NOT_FOUND);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service GET called");
        return response.build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response newCar(String strCar) throws Exception {
        Response.ResponseBuilder response = Response.status(Response.Status.CREATED);
        Database db = Database.getInstance();

        try {
            pkgData.Car car = new Gson().fromJson(strCar, pkgData.Car.class);
            db.insertCar(car);
            response.entity(car.toString());
        } catch (Exception e) {
            response.status(Response.Status.BAD_REQUEST);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service POST called");
        return response.build();
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_JSON})
    public Response deleteCar(@QueryParam("carid") String id) throws Exception {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        Database db = Database.getInstance();

        try {
            pkgData.Car retCar = db.deleteCar(new ObjectId(id));
            response.entity(retCar.toString());
        } catch (Exception e) {
            response.status(Response.Status.BAD_REQUEST);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service DELETE called");
        return response.build();
    }

    @PATCH
    @Path("{carid}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response patchCar(String strCar, @PathParam("carid") String id) throws Exception {
        System.out.println("=============== web service PATCH called");
        return updateCar(strCar, id);
    }

    @PUT
    @Path("{carid}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response putCar(String strCar, @PathParam("carid") String id) throws Exception {
        try {
            Database.getInstance().getCarById(new ObjectId(id));
            updateCar(strCar, id);
        } catch (Exception e) {
            newCar(strCar);
        }

        return updateCar(strCar, id);
    }

    private Response updateCar(String strCar, String carid) throws Exception {
        Database db = Database.getInstance();
        Response.ResponseBuilder response = Response.status(Response.Status.OK);

        try {
            pkgData.Car newCar = new Gson().fromJson(strCar, pkgData.Car.class);
            pkgData.Car car = db.getCarById(new ObjectId(carid));
            car.setCarName(newCar.getCarName());
            car.setPositions(newCar.getPositions());
            response.entity(car.toString());

            System.out.println("=============== web service PUT/PATCH called");
        } catch (Exception e) {
            response.status(Response.Status.BAD_REQUEST);
            response.entity("[ERROR] " + e.getMessage());
        }

        return response.build();
    }
}