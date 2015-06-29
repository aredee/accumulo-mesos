package aredee.mesos.frameworks.accumulo.configuration;

public enum ServerType {
    MASTER("master"), ZOOKEEPER("zookeeper"), TABLET_SERVER("tserver"),
    GARBAGE_COLLECTOR("gc"), TRACER("tracer"), MONITOR("monitor"), UNKNOWN("unknown");

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

}
