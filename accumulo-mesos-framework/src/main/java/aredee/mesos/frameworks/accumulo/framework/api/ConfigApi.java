package aredee.mesos.frameworks.accumulo.framework.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/config")
@Consumes({ "application/json" })
@Produces({ "application/json", "text/html" })
//@io.swagger.annotations.Api(value = "/config", description = "the config API")
public class ConfigApi  {

   private final ConfigApiService delegate = ApiServiceFactory.getConfigApi();

    @GET
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Returns current Framework configuration\n", response = Framework.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response") })
    */
    public Response configGet()
    throws NotFoundException {
    return delegate.configGet();
    }
}

