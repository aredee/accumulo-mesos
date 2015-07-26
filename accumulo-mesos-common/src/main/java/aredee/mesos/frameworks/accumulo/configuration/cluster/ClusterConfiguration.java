package aredee.mesos.frameworks.accumulo.configuration.cluster;

import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;

import java.util.Map;

public interface ClusterConfiguration {

    public String getBindAddress();
    public void setBindAddress(String address);

    public int getHttpPort();
    public void setHttpPort(int port);

    public String getMesosMaster();
    public void setMesosMaster(String master);

    public String getFrameworkName();
    public void setFrameworkName(String name);

    public String getZkServers();
    public void setZkServers(String servers);

    public String getTarballUri();
    public void setTarballUri(String uriString);

    //TODO get hadoop namenode for accumulo?

    public void setProcessorConfigurations(Map<ServerType, ProcessConfiguration> processors);
    public Map<ServerType, ProcessConfiguration> getProcessorConfigurations();
   
    public String getAccumuloInstanceName();
    public void setAccumuloInstanceName(String instance);

    public String getAccumuloRootPassword();
    public void setAccumuloRootPassword(String password);

    public double getMaxExecutorMemory();
    public void setMaxExecutorMemory(double maxExecutorMemory);

    public double getMinExecutorMemory();
    public void setMinExecutorMemory(double minExecutorMemory);
    
    public void setMinTservers(int servers);
    public int getMinTservers();
    
}
