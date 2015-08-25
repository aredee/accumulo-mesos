package aredee.mesos.frameworks.accumulo.framework.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/cluster")
@Consumes({ "application/json" })
@Produces({ "application/json", "text/html" })
//@io.swagger.annotations.Api(value = "/cluster", description = "the cluster API")
public class ClusterApi  {

   private final ClusterApiService delegate = ApiServiceFactory.getClusterApi();

    @POST
    @Path("/kill")
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Kill all running processes using Mesos\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 202, message = "Kill accepted"),

        @io.swagger.annotations.ApiResponse(code = 409, message = "Cluster not running") })

     */
    public Response clusterKillPost()
    throws NotFoundException {
    return delegate.clusterKillPost();
    }

    @POST
    @Path("/master/reprovision")
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Restarts Accumulo master server using a new Mesos Resource\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Reprovision accepted"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Master not running") })
    */
    public Response clusterMasterReprovisionPost()
    throws NotFoundException {
    return delegate.clusterMasterReprovisionPost();
    }

    @POST
    @Path("/master/restart")
    
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Restarts Accumulo master server\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Restart accepted"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Master not running") })
    */
    public Response clusterMasterRestartPost()
    throws NotFoundException {
    return delegate.clusterMasterRestartPost();
    }

    @GET
    @Path("/monitor")
        /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get location(s) of Accumulo Monitor server\n", response = Monitor.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Monitor found") })
    */
    public Response clusterMonitorGet()
    throws NotFoundException {
    return delegate.clusterMonitorGet();
    }

    @POST
    @Path("/start")
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Start configured Accumulo cluster\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Start accepted"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Cluster already running") })
    */
    public Response clusterStartPost()
    throws NotFoundException {
    return delegate.clusterStartPost();
    }

    @POST
    @Path("/stop")
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Stop running Accumulo cluster using Accumulo\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Stop accepted"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Cluster not running") })
    */
    public Response clusterStopPost()
    throws NotFoundException {
    return delegate.clusterStopPost();
    }

    @POST
    @Path("/tserver/reprovision")
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Reprovisions an Accumulo Tserver using a new Mesos Resource\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Reprovision accepted"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "tserver does not exist"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "tserver exists but is not running") })

    @ApiParam(value = "ID of the tserver",required=true)
    */
    public Response clusterTserverReprovisionPost( @QueryParam("id") String id)
    throws NotFoundException {
    return delegate.clusterTserverReprovisionPost(id);
    }

    @POST
    @Path("/tserver/restart")
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Restarts an Accumulo Tserver\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Reprovision accepted"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "tserver does not exist"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "tserver exists but is not running") })

    @ApiParam(value = "ID of the tserver",required=true)
    */
    public Response clusterTserverRestartPost(@QueryParam("id") String id)
    throws NotFoundException {
    return delegate.clusterTserverRestartPost(id);
    }

    @POST
    @Path("/tserver/rollingrestart")
    /*
    @io.swagger.annotations.ApiOperation(value = "", notes = "Perform a rolling restart of all cluster servers.\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Rolling restart accepted") })

    @ApiParam(value = "Include master server in restart", defaultValue="false")
    @ApiParam(value = "Number of servers to restart at once.", allowableValues="{}", defaultValue="1")
    @ApiParam(value = "Reprovision servers on new Mesos Resources", defaultValue="false")

    */
    public Response clusterTserverRollingrestartPost( @QueryParam("master") Boolean master,
    @QueryParam("group") Integer group,
    @QueryParam("reprovision") Boolean reprovision)
    throws NotFoundException {
    return delegate.clusterTserverRollingrestartPost(master,group,reprovision);
    }
}

