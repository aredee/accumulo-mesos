package aredee.mesos.frameworks.accumulo.framework.api;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/")
@Consumes({ "application/json" })
@Produces({ "application/json", "text/html" })
//@io.swagger.annotations.Api(value = "/", description = "the  API")
public class DefaultApi  {

   private final DefaultApiService delegate = ApiServiceFactory.getDefaultApi();

    @GET
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Returns Swagger UI for this API\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Success") })
    */
    public Response rootGet()
    throws NotFoundException {
    return delegate.rootGet();
    }
}

