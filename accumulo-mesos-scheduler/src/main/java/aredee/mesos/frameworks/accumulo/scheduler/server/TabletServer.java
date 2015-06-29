package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public class TabletServer extends BaseServer {
    public TabletServer(String taskId, String slaveId) {
        super(taskId, slaveId);
    }
    public TabletServer(String taskId){
        super(taskId);
    }

    @Override
    public ServerType getType(){ return ServerType.TABLET_SERVER; }

}
