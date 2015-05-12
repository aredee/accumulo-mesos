package aredee.mesos.frameworks.accumulo.scheduler.server;

public class Tserver extends BaseServer {
    public Tserver(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public Tserver(String taskId){
        super(taskId);
    }

    @Override
    public SERVER_TYPE getType(){ return SERVER_TYPE.TSERVER; }

}
