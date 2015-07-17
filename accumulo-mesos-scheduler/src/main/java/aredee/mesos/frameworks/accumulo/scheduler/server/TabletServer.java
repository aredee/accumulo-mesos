package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public class TabletServer extends BaseServer {
    
    public TabletServer () {
        super(getUUIDTask(ServerType.TABLET_SERVER));    
    }
    
    public TabletServer(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public TabletServer(String taskId){
        super(taskId);
    }

    @Override
    public ServerType getType(){ return ServerType.TABLET_SERVER; }
    
    public static boolean isTabletServer(String id){
        return id.startsWith(ServerType.TABLET_SERVER.getName());
    }
   

}
