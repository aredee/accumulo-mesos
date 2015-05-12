package aredee.mesos.frameworks.accumulo.scheduler.server;

public class Monitor extends BaseServer {
    public Monitor(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public Monitor(String taskId){
        super(taskId);
    }
    @Override
    public SERVER_TYPE getType(){ return SERVER_TYPE.MONITOR; }

}
