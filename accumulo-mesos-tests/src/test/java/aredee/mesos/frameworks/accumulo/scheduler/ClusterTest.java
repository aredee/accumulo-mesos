package aredee.mesos.frameworks.accumulo.scheduler;

import java.util.HashSet;
import java.util.Set;

import aredee.mesos.frameworks.accumulo.configuration.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;
import aredee.mesos.frameworks.accumulo.scheduler.server.ServerUtils;

import org.apache.mesos.Protos;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by klucar on 5/11/15.
 */
public class ClusterTest {


    private Protos.ExecutorInfo info = null;
    private ClusterConfiguration config = null;


    @BeforeClass
    public static void setupExecutor(){

    }


    @BeforeClass
    public static void setupConfig(){

    }

    @Test
    public void testLaunchedSet() {
        
        Set<AccumuloServer> serversToLaunch = new HashSet<AccumuloServer>();
     
        AccumuloServer server = ServerUtils.newServer(ServerType.MASTER);
        server.setSlaveId("id-100-1030-30444");
        
        
        serversToLaunch.add(server);
        
        System.out.println(serversToLaunch);
        
        serversToLaunch.remove(server);
        
        System.out.println(serversToLaunch);      
    }

}
