package aredee.mesos.frameworks.accumulo.scheduler.server;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

public interface AccumuloServer {

    public ServerType getType();

    public String getSlaveId();

    public void setSlaveId(String newId);

    public String getTaskId();
    
    public int getMaxMemorySize();
    
    public int getMinMemorySize();

    public void setMaxMemorySize(int memory);
    
    public void setMinMemorySize(int memory);

}
