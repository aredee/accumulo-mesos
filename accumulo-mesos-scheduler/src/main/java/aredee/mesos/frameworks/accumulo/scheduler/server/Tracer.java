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

    @Override
    public boolean isServer(String taskId) {
        return isTracer(taskId);
    }
    
    public static boolean isTracer(String id){
        return id.startsWith(ServerType.TRACER.getName());
    }
      
    
}
