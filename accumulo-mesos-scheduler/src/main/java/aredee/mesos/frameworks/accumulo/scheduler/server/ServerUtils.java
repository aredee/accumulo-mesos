package aredee.mesos.frameworks.accumulo.scheduler.server;

import java.util.UUID;


/**
 * Static utility methods for generating and inspecting AccumuloServer types
 */
public class ServerUtils {

    // Don't allow instantiation
    private ServerUtils(){}

    public static AccumuloServer newMaster(){
        return new Master(getUUIDTask(AccumuloServer.SERVER_TYPE.MASTER));
    }

    public static AccumuloServer newTserver(){
        return new Tserver(getUUIDTask(AccumuloServer.SERVER_TYPE.TSERVER));
    }

    public static AccumuloServer newGC(){
        return new GarbageCollector(getUUIDTask(AccumuloServer.SERVER_TYPE.GC));
    }

    public static AccumuloServer newMonitor(){
        return new Monitor(getUUIDTask(AccumuloServer.SERVER_TYPE.MONITOR));
    }


    public static boolean isMaster(String id){
        return id.startsWith(AccumuloServer.SERVER_TYPE.MASTER.getName());
    }

    public static boolean isTserver(String id){
        return id.startsWith(AccumuloServer.SERVER_TYPE.TSERVER.getName());
    }

    public static boolean isGarbageCollector(String id){
        return id.startsWith(AccumuloServer.SERVER_TYPE.GC.getName());
    }

    public static boolean isMonitor(String id){
        return id.startsWith(AccumuloServer.SERVER_TYPE.MONITOR.getName());
    }

    private static String getUUIDTask(AccumuloServer.SERVER_TYPE type){
        return type.getName() + "_" + UUID.randomUUID();
    }

}
