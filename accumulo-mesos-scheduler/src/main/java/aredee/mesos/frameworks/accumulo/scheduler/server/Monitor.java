package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public class Monitor extends BaseServer {
    
    public Monitor() {
        super(getUUIDTask(ServerType.MONITOR));    
    }
    public Monitor(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public Monitor(String taskId){
        super(taskId);
    }
    @Override
    public ServerType getType(){ return ServerType.MONITOR; }
    
    @Override
    public boolean isServer(String taskId) {
        return isMonitor(taskId);
    }
    
    public static boolean isMonitor(String id){
        return id.startsWith(ServerType.MONITOR.getName());
    }  
    
}
