package aredee.mesos.frameworks.accumulo.framework.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/status")
@Consumes({ "application/json" })
@Produces({ "application/json", "text/html" })
//@io.swagger.annotations.Api(value = "/status", description = "the status API")
public class StatusApi  {

   private final StatusApiService delegate = ApiServiceFactory.getStatusApi();

    @GET
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Returns Framework Status\n", response = Object.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response") })
    */
    public Response statusGet()
    throws NotFoundException {
    return delegate.statusGet();
    }
}

