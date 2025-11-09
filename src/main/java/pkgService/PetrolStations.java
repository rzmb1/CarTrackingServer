package pkgService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.geojson.Point;
import org.bson.types.ObjectId;
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

@Path("petrol-stations")
@RequestScoped
public class PetrolStations {
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateSerializer())
            .registerTypeAdapter(ObjectId.class, new GsonSerializerObjectId())
            .registerTypeAdapter(Point.class, new GsonPointSerializer())
            .create();

    @Context
    private UriInfo context;

    public PetrolStations() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPetrolStations() {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            response.entity(new Gson().toJson(db.getAllPetrolStations()));
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
    public Response getSortedPetrolStations() {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            response.entity(new Gson().toJson(db.getSortedPetrolStations()));
        } catch (Exception e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service GET LIST called");
        return response.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("create-geo-index")
    public Response createGeoIndex() {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            db.createGeoIndex();
            response.entity(new Gson().toJson(response.entity(new Gson().toJson("successfully created geo index"))));
        } catch (Exception e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service GEO INDEX called");
        return response.build();
    }
}