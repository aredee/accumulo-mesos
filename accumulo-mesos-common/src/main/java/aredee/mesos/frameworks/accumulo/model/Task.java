package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;


public class Task  {

    private String slaveId = null;
    private String taskId = null;
    private ServerProfile serverProfile = null;

    /**
    **/
    @JsonProperty("slaveId")
    public String getSlaveId() {
        return slaveId;
    }
    public void setSlaveId(String slaveId) {
        this.slaveId = slaveId;
    }
    public boolean hasSlaveId(){
        if( slaveId == null ) return false;
        if( slaveId.isEmpty()) return false;
        return true;
    }

    /**
    **/
    @JsonProperty("taskId")
    public String getTaskId() {
    return taskId;
    }
    public void setTaskId(String id) {
    this.taskId = id;
    }
    public boolean hasTaskId(){
        if( taskId == null ) return false;
        if( taskId.isEmpty()) return false;
        return true;
    }


    /**
     **/
    @JsonProperty("serverProfile")
    public ServerProfile getServerProfile() {
        return serverProfile;
    }
    public void setServerProfile(ServerProfile profile) {
        this.serverProfile = profile;
    }


    public ServerProfile.TypeEnum getType(){
        return serverProfile.getType();
    }

    public String assignTaskId(){
        assert( taskId != null);
        assert( serverProfile != null);

        this.taskId = getType()+"::"+serverProfile.getId()+"::"+ UUID.randomUUID();

        return taskId;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class Task {\n");

        sb.append("  slaveId: ").append(slaveId).append("\n");
        sb.append("  taskId: ").append(taskId).append("\n");
        sb.append("  serverProfile: ").append(serverProfile).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
