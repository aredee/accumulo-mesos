package aredee.mesos.frameworks.accumulo.configuration;

import org.apache.accumulo.master.Master;
import org.apache.accumulo.tserver.TabletServer;
import org.apache.accumulo.gc.SimpleGarbageCollector;
import org.apache.accumulo.monitor.Monitor;
import org.apache.accumulo.tracer.TraceServer;

@SuppressWarnings("rawtypes")
public enum ServerType {
    MASTER("master") {
        public Class getServiceClass() {
            return Master.class;
        }
    }, 
    TABLET_SERVER("tserver") {
        public Class getServiceClass() {
            return TabletServer.class;
        }          
    },
    GARBAGE_COLLECTOR("gc"){
        public Class getServiceClass() {
            return SimpleGarbageCollector.class;
        }         
    }, 
    TRACER("tracer") {
        public Class getServiceClass() {
            return TraceServer.class;
        }           
    },
    MONITOR("monitor"){
        public Class getServiceClass() {
            return Monitor.class;
        }
    }, 
    ZOOKEEPER("zookeeper"){
        public Class getServiceClass() {
            return null;
        }      
    },   
    UNKNOWN("unknown"){
        public Class getServiceClass() {
            return null;
        }
    };

    private String name;

    ServerType(String name){
        this.name = name;
    }

    public String getName(){ return this.name; }

    public static ServerType getTypeFromName(String name){
        for( ServerType server: ServerType.values()){
            if( server.getName().equals(name)){
                return server;
            }
        }
        return UNKNOWN;
    }

    public abstract Class getServiceClass();
    
}
