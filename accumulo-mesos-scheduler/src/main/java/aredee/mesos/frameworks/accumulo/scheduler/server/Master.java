package aredee.mesos.frameworks.accumulo.scheduler.server;

public class Master extends BaseServer {

    public Master(String taskId, String slaveId) {
        super(taskId, slaveId);
    }

    public Master(String taskId){
        super(taskId);
    }

    @Override
    public SERVER_TYPE getType(){ return SERVER_TYPE.MASTER; }
}
