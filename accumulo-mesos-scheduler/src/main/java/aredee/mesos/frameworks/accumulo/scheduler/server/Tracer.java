package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public class Tracer extends BaseServer {
    
    public Tracer () {
        super(getUUIDTask(ServerType.TRACER));    
    }
    
    public Tracer(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public Tracer(String taskId){
        super(taskId);
    }

    @Override
    public ServerType getType(){ return ServerType.TRACER; }


    public static boolean isTacer(String id){
        return id.startsWith(ServerType.TRACER.getName());
    }
      
    
}
