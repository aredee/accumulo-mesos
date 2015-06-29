package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public class GarbageCollector extends BaseServer {
    public GarbageCollector(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public GarbageCollector(String taskId){
        super(taskId);
    }
    @Override
    public ServerType getType(){ return ServerType.GARBAGE_COLLECTOR; }

}
