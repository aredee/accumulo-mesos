package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public class GarbageCollector extends BaseServer {
    
    public GarbageCollector() {
        super(getUUIDTask(ServerType.GARBAGE_COLLECTOR));
    }
    public GarbageCollector(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public GarbageCollector(String taskId){
        super(taskId);
    }
    @Override
    public ServerType getType(){ return ServerType.GARBAGE_COLLECTOR; }
    
    public static boolean isGarbageCollector(String id){
        return id.startsWith(ServerType.GARBAGE_COLLECTOR.getName());
    }
  

}
