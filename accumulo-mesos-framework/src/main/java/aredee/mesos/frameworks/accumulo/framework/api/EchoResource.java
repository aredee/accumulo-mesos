package aredee.mesos.frameworks.accumulo.framework.api;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/echo")
@Produces(MediaType.TEXT_PLAIN)
public class EchoResource {

    @GET
    public String echo() {
        return new String("Hello World!");
    }

}
