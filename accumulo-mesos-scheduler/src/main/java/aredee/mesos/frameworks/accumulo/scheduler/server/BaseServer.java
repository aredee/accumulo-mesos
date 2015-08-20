package aredee.mesos.frameworks.accumulo.scheduler.server;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

/**
 * 
 * Define what it means to be an AccumuloServer
 *
 */
public abstract class BaseServer implements AccumuloServer {
    protected String taskId;
    protected String slaveId;
    protected int maxMemory;
    protected int minMemory;
    
    protected BaseServer(String taskId, String slaveId) {
        // This insures that if the getters and setters modify or restrict
        // the IDs these will be handled accordingly. See setTaskId().
        setSlaveId(slaveId);
        setTaskId(taskId);
    }

    /**
     * Each Subtype will define what its taskId will look like
     * 
     * @param taskId
     */
    protected BaseServer(String taskId){
        this(taskId, "");
    }

    /**
     * Old functionality moved to ServerUtils.getType(String taskId);
     * An abstract class should have one abstract method. Parent should
     * not reference children.
     */
    @Override
    public abstract ServerType getType();
  
    /**
     * Match taskId to server type
     * 
     * @param taskId to match against
     * 
     * @return true if taskId matches server type, that is to say if this is a 
     * Master server and the taskId belongs to a Master server.
     */
    public abstract boolean isServer(String taskId);
    
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
        
        // Don't allow a malformed/incorrect taskIds to be assigned. TaskIds start with
        // the name of the server so it would not be good to have a tserver taskId set on
        // a master server.
        //
        if(isServer(taskId)) {
            this.taskId = taskId; 
        } else {
            throw new RuntimeException("Task id " + taskId + " does not match server type");
        }
    }

    @Override
    public boolean equals(Object otherObj) {
        
        boolean isEqual = false;
  
        if( otherObj == null) return false;
        
        if( otherObj instanceof AccumuloServer ) {
            AccumuloServer other = (AccumuloServer) otherObj;
            
            isEqual = (this.hashCode() == other.hashCode());
            
            /**
            if (!this.hasSlaveId()) {
                isEqual = StringUtils.isEmpty(other.getSlaveId()) && this.taskId.equals(other.getTaskId());
            } else if (!StringUtils.isEmpty(other.getSlaveId())){
                isEqual = this.slaveId.equals(other.getSlaveId()) && this.taskId.equals(other.getTaskId());
            }
            **/
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        int result;
        if( this.hasSlaveId() ) {
            result = (getSlaveId() + getTaskId()).hashCode();
        } else {
            result = getTaskId().hashCode();
        }
        return result;
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
