package aredee.mesos.frameworks.accumulo.scheduler.server;

public abstract class BaseServer implements AccumuloServer {
    protected String taskId;
    protected String slaveId;

    protected BaseServer(String taskId, String slaveId) {
        this.slaveId = slaveId;
        this.taskId = taskId;
    }

    protected BaseServer(String taskId){
        this.taskId = taskId;
    }

    @Override
    public SERVER_TYPE getType(){
        if(ServerUtils.isTserver(taskId)) return SERVER_TYPE.TSERVER;
        if(ServerUtils.isMaster(taskId)) return SERVER_TYPE.MASTER;
        if(ServerUtils.isMonitor(taskId)) return SERVER_TYPE.MONITOR;
        if(ServerUtils.isGarbageCollector(taskId)) return SERVER_TYPE.GC;
        return SERVER_TYPE.UNKNOWN;
    }

    @Override
    public String getSlaveId() {
        return slaveId;
    }

    @Override
    public void setSlaveId(String newId){
        this.slaveId = newId;
    }

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
        result = getSlaveId().hashCode();
        result = 29 * result + getTaskId().hashCode();
        return result;
    }
}
