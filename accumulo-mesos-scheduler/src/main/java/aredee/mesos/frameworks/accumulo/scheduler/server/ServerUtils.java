package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

import java.util.UUID;


/**
 * Static utility methods for generating and inspecting AccumuloServer types
 */
public class ServerUtils {

    // Don't allow instantiation
    private ServerUtils(){}

    public static AccumuloServer newMaster(){
        return new Master(getUUIDTask(ServerType.MASTER));
    }

    public static AccumuloServer newTabletServer(){
        return new TabletServer(getUUIDTask(ServerType.TABLET_SERVER));
    }

    public static AccumuloServer newGarbageCollector(){
        return new GarbageCollector(getUUIDTask(ServerType.GARBAGE_COLLECTOR));
    }

    public static AccumuloServer newMonitor(){
        return new Monitor(getUUIDTask(ServerType.MONITOR));
    }


    public static boolean isMaster(String id){
        return id.startsWith(ServerType.MASTER.getName());
    }

    public static boolean isTabletServer(String id){
        return id.startsWith(ServerType.TABLET_SERVER.getName());
    }

    public static boolean isGarbageCollector(String id){
        return id.startsWith(ServerType.GARBAGE_COLLECTOR.getName());
    }

    public static boolean isMonitor(String id){
        return id.startsWith(ServerType.MONITOR.getName());
    }

    private static String getUUIDTask(ServerType type){
        return type.getName() + "_" + UUID.randomUUID();
    }

}
