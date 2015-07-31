package aredee.mesos.frameworks.accumulo.configuration.cluster;

import java.util.HashMap;
import java.util.Map;

import aredee.mesos.frameworks.accumulo.configuration.process.BaseProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;

/**
 * Add the interface abstraction here 
 *
 */
public class BaseClusterConfiguration extends JsonBaseClusterConfiguration implements ClusterConfiguration {

 
    public BaseClusterConfiguration() {
        super();    
    }
    public BaseClusterConfiguration(JsonBaseClusterConfiguration json) {
        this.setAccumuloInstanceName(json.getAccumuloInstanceName());
        this.setAccumuloRootPassword(json.getAccumuloRootPassword());
        this.setAccumuloSiteUri(json.getAccumuloSiteUri());
        this.setAccumuloVersion(json.getAccumuloVersion());
        this.setBindAddress(json.getBindAddress());
        this.setServers(json.getServers());
        this.setFrameworkName(json.getFrameworkName());
        this.setHttpPort(json.getHttpPort());
        this.setMaxExecutorMemory(json.getMaxExecutorMemory());
        this.setMesosMaster(json.getMesosMaster());
        this.setMinExecutorMemory(json.getMinExecutorMemory());
        this.setMinTservers(json.getMinTservers());
        this.setTarballUri(json.getTarballUri());
        this.setZkServers(json.getZkServers());
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setProcessorConfigurations(Map<ServerType, ProcessConfiguration> processors) {
        if (processors != null) {
            Map<ServerType, BaseProcessConfiguration> servers = new HashMap<ServerType, BaseProcessConfiguration>(processors.size());
            for(Map.Entry<ServerType, ProcessConfiguration> entry : processors.entrySet()) {
                servers.put(entry.getKey(), new BaseProcessConfiguration(entry.getValue()));            
            }
            setServers(servers);
        } else {
            setServers(new HashMap<ServerType, BaseProcessConfiguration>(0));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<ServerType, ProcessConfiguration> getProcessorConfigurations() {
        Map<ServerType, ProcessConfiguration> servers = new HashMap<ServerType, ProcessConfiguration>(0);
    
        if (getServers() != null) {
            servers = new HashMap<ServerType, ProcessConfiguration>(0);
            // Odd that jdk 1.8 will not allow the cast to the Map<ServerType,ProcessConfiguration>
            for (Map.Entry<ServerType, BaseProcessConfiguration> entry : getServers().entrySet()) {
                servers.put(entry.getKey(), entry.getValue());
            }
        }
        return servers;
    }
 
}
