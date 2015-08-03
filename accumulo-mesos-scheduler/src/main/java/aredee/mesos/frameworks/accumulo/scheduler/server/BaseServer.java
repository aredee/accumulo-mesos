package aredee.mesos.frameworks.accumulo.scheduler.server;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public abstract class BaseServer implements AccumuloServer {
    protected String taskId;
    protected String slaveId;
    protected int maxMemory;
    protected int minMemory;
    
    
    protected BaseServer(String taskId, String slaveId) {
        this.slaveId = slaveId;
        this.taskId = taskId;
    }

    protected BaseServer(String taskId){
        this(taskId, "");
    }

    @Override
    public ServerType getType(){
        ServerType type = ServerType.UNKNOWN;
        
        if (taskId != null) {
            if(TabletServer.isTabletServer(taskId)) {
                type = ServerType.TABLET_SERVER;
            } else if (Master.isMaster(taskId)) {
                type = ServerType.MASTER;       
            } else if (Monitor.isMonitor(taskId)) {
                type = ServerType.MONITOR;     
            } else if (GarbageCollector.isGarbageCollector(taskId)) {
                type = ServerType.GARBAGE_COLLECTOR;
            } else if (Tracer.isTacer(taskId)) {
                type = ServerType.TRACER;         
            }
        }
        return type;
    }

    @Override
    public String getSlaveId() {
        return slaveId;
    }

    @Override
    public void setSlaveId(String newId){
        this.slaveId = newId;
    }

    public boolean hasSlaveId(){ 
        return !StringUtils.isEmpty(slaveId); 
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId){ 
        this.taskId = taskId; 
    }

    @Override
    public boolean equals(Object otherObj) {
        
        boolean isEqual = false;
  
        if( otherObj == null) return false;
        
        if( otherObj instanceof AccumuloServer ) {
            AccumuloServer other = (AccumuloServer) otherObj;
            
            if (!this.hasSlaveId()) {
                isEqual = StringUtils.isEmpty(other.getSlaveId()) && this.taskId.equals(other.getTaskId());
            } else if (!StringUtils.isEmpty(other.getSlaveId())){
                isEqual = this.slaveId.equals(other.getSlaveId()) && this.taskId.equals(other.getTaskId());
            }
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        int result = 0;
        if( this.hasSlaveId() ) {
            result = getSlaveId().hashCode();
        }  
        return result + getTaskId().hashCode();
     }
    
    @Override
    public int getMaxMemorySize() {
        return maxMemory;
    }
    
    @Override
    public int getMinMemorySize() {
        return minMemory;
    }
    
    @Override
    public void setMaxMemorySize(int memory) {
        maxMemory = memory;
    }
    @Override
    public void setMinMemorySize(int memory) {
        minMemory = memory;
    }
    
    public String toString() {
        return new Gson().toJson(this);
    }
    
    protected static String getUUIDTask(ServerType type){
        return type.getName() + "_" + UUID.randomUUID();
    }   
}
