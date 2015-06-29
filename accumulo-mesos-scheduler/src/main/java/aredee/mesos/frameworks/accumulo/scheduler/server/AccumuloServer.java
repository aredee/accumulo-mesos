package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public interface AccumuloServer {

    public ServerType getType();

    public String getSlaveId();

    public void setSlaveId(String newId);

    public String getTaskId();

}
