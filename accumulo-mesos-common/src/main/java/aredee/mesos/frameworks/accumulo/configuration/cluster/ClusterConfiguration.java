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

    // This is where the accumulo-site.xml file is to be found, defaults to file:./conf/default-accumulo-site.xml
    // as defined in Defaults.ACCUMULO_SITE_URI
    //
    public void setAccumuloSiteUri(String uri);
    public String getAccumuloSiteUri();
   
    public void setAccumuloVersion(String version);
    public String getAccumuloVersion();
    
    public double getMaxExecutorMemory();
    public void setMaxExecutorMemory(double maxExecutorMemory);

    public double getMinExecutorMemory();
    public void setMinExecutorMemory(double minExecutorMemory);
    
    public void setMinTservers(int servers);
    public int getMinTservers();
    
}
