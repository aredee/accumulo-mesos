package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public class Master extends BaseServer {

    public Master() {
        super(getUUIDTask(ServerType.MASTER));
    }
    public Master(String taskId, String slaveId) {
        super(taskId, slaveId);
    }

    public Master(String taskId){
        super(taskId);
    }

    @Override
    public ServerType getType(){ return ServerType.MASTER; }
    
    @Override
    public boolean isServer(String taskId) {
        return isMaster(taskId);
    }
   
    public static boolean isMaster(String id){
        return id.startsWith(ServerType.MASTER.getName());
    }
}
