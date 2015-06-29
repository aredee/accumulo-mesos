package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public abstract class BaseServer implements AccumuloServer {
    protected String taskId;
    protected String slaveId;

    protected BaseServer(String taskId, String slaveId) {
        this.slaveId = slaveId;
        this.taskId = taskId;
    }

    protected BaseServer(String taskId){
        this(taskId, "");
    }

    @Override
    public ServerType getType(){
        if(ServerUtils.isTabletServer(taskId)) return ServerType.TABLET_SERVER;
        if(ServerUtils.isMaster(taskId)) return ServerType.MASTER;
        if(ServerUtils.isMonitor(taskId)) return ServerType.MONITOR;
        if(ServerUtils.isGarbageCollector(taskId)) return ServerType.GARBAGE_COLLECTOR;
        return ServerType.UNKNOWN;
    }

    @Override
    public String getSlaveId() {
        return slaveId;
    }

    @Override
    public void setSlaveId(String newId){
        this.slaveId = newId;
    }

    public boolean hasSlaveId(){ return this.slaveId.isEmpty(); }

    @Override
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId){ this.taskId = taskId; }

    @Override
    public boolean equals(Object otherObj) {
        if( otherObj == null) return false;
        if( otherObj == this) return true;
        if( otherObj instanceof AccumuloServer ) {
            AccumuloServer other = (AccumuloServer) otherObj;
            return (this.slaveId == other.getSlaveId() && this.taskId == other.getTaskId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        if( this.hasSlaveId() ) {
            result = getSlaveId().hashCode();
        } else {
            result = 814;
        }
        result = 29 * result + getTaskId().hashCode();
        return result;
    }
}
