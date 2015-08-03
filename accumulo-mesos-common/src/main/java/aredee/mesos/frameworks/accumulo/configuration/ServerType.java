package aredee.mesos.frameworks.accumulo.configuration;

import org.apache.accumulo.master.Master;
import org.apache.accumulo.tserver.TabletServer;
import org.apache.accumulo.gc.SimpleGarbageCollector;
import org.apache.accumulo.monitor.Monitor;
import org.apache.accumulo.tracer.TraceServer;

public enum ServerType {
    MASTER("master", Master.class),
    TABLET_SERVER("tserver", TabletServer.class),
    GARBAGE_COLLECTOR("gc", SimpleGarbageCollector.class),
    TRACER("tracer", TraceServer.class),
    MONITOR("monitor", Monitor.class),
    ZOOKEEPER("zookeeper", null),
    UNKNOWN("unknown", null);

    private String name;
    private Class serviceClass;

    ServerType(String name, Class serviceClass){
        this.name = name;
        this.serviceClass = serviceClass;
    }

    public String getName(){ return this.name; }

    public Class getServiceClass() { return this.serviceClass; }

    public static ServerType getTypeFromName(String name){
        for( ServerType server: ServerType.values()){
            if( server.getName().equals(name)){
                return server;
            }
        }
        return UNKNOWN;
    }

}
