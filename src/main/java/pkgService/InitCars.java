package pkgService;

import com.google.gson.Gson;
import pkgData.Database;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("init-cars")
@RequestScoped
public class InitCars {

    @Context
    private UriInfo context;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response initCars() {
        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        try {
            Database db = Database.getInstance();
            db.initCars();
            response.entity(new Gson().toJson(response.entity(new Gson().toJson("successfully initialized cars"))));
        } catch (Exception e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.entity("[ERROR] " + e.getMessage());
        }
        System.out.println("=============== web service INITIALIZE called");
        return response.build();
    }
}
