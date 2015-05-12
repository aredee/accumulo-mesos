package aredee.mesos.frameworks.accumulo.scheduler.server;

public class GarbageCollector extends BaseServer {
    public GarbageCollector(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public GarbageCollector(String taskId){
        super(taskId);
    }
    @Override
    public SERVER_TYPE getType(){ return SERVER_TYPE.GC; }

}
