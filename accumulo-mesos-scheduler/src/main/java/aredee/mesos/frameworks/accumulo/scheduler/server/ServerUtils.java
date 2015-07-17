package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ProcessorConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Static utility methods for generating and inspecting AccumuloServer types
 */
public class ServerUtils {

    private static Map<ServerType, IServerFactory> serverFactories = new HashMap<ServerType, IServerFactory>(7);
    
    // Don't allow instantiation
    private ServerUtils(){}

    public static void addServer(Set<AccumuloServer> launchable, ProcessorConfiguration config ) {
        launchable.add(ServerUtils.newServer(config));
    }
    
    public static AccumuloServer newServer(ProcessorConfiguration config) {
        AccumuloServer server = ServerUtils.newServer(config.toServerType());
        server.setMaxMemorySize(config.getMaxMemorySize());
        server.setMinMemorySize(config.getMinMemorySize());        
        return server;
    }
    public static AccumuloServer newServer(ProcessorConfiguration config, String taskId, String slaveId) {
        AccumuloServer server = ServerUtils.newServer(config.toServerType(),taskId, slaveId);
        server.setMaxMemorySize(config.getMaxMemorySize());
        server.setMinMemorySize(config.getMinMemorySize());        
        return server;
    }   
    public static AccumuloServer newServer(ServerType type) {
        return serverFactories.get(type).newServer();
    }
    public static AccumuloServer newServer(ServerType type, String taskId, String slaveId) {
        return serverFactories.get(type).newServer(taskId,slaveId);
    }  
    private static interface IServerFactory {
        public AccumuloServer newServer();
        public AccumuloServer newServer(String taskId,String slaveId);
    }
    
    static {
        serverFactories.put(ServerType.MASTER, new IServerFactory() {
            public AccumuloServer newServer() { 
                return new Master();
            }
            public AccumuloServer newServer(String taskId,String slaveId) {
                return new Master(taskId,slaveId);
            }
        });
        
        serverFactories.put(ServerType.TABLET_SERVER,  new IServerFactory() {
            public AccumuloServer newServer() { 
                return new TabletServer();
            }
            public AccumuloServer newServer(String taskId,String slaveId) {
                return new TabletServer(taskId,slaveId);
            }
        });
        serverFactories.put(ServerType.GARBAGE_COLLECTOR,  new IServerFactory() {
            public AccumuloServer newServer() { 
                return new GarbageCollector();
            }
            public AccumuloServer newServer(String taskId,String slaveId) {
                return new GarbageCollector(taskId,slaveId);
            }          
        });
        serverFactories.put(ServerType.MONITOR,  new IServerFactory() {
            public AccumuloServer newServer() { 
                return new Monitor();
            }
            public AccumuloServer newServer(String taskId,String slaveId) {
                return new Monitor(taskId,slaveId);
            }         
        });
        serverFactories.put(ServerType.TRACER,  new IServerFactory() {
            public AccumuloServer newServer() { 
                return new Tracer();
            }
            public AccumuloServer newServer(String taskId,String slaveId) {
                return new Tracer(taskId,slaveId);
            }         
        });
      
    }
}
