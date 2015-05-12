package aredee.mesos.frameworks.accumulo.scheduler.server;

public interface AccumuloServer {

    // these need to be the same strings used in accumulo-start
    public static enum SERVER_TYPE {
        MASTER("master"), TSERVER("tserver"), GC("gc"), MONITOR("monitor"), UNKNOWN("unknown");

        private String name;
        private SERVER_TYPE(String name){
            this.name = name;
        }

        public String getName(){ return this.name; }
    }

    public SERVER_TYPE getType();

    public String getSlaveId();

    public void setSlaveId(String newId);

    public String getTaskId();

}
