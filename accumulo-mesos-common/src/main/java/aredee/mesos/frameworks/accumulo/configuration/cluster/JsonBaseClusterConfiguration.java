package aredee.mesos.frameworks.accumulo.configuration.cluster;

import java.util.HashMap;
import java.util.Map;

import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import aredee.mesos.frameworks.accumulo.configuration.process.BaseProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import com.google.gson.GsonBuilder;

/**
 * 
 * JSON does not handle interfaces well, so this class is to be interface free.
 *
 */
public class JsonBaseClusterConfiguration {

    String bindAddress = Defaults.BIND_ADDRESS;
    int httpPort = Defaults.HTTP_PORT;
    String mesosMaster = Defaults.MESOS_MASTER;
    String frameworkName = Defaults.FRAMEWORK_NAME;
    String zkServers = Defaults.ZK_SERVERS;
    
    int minTservers = Defaults.MIN_TSERVERS;

    String instanceName = "default-instance";
    //String accumuloTarBallUri = null;
    //String executorJarUri = Defaults.EXECUTOR_JAR;
    String tarballUri;
    String accumuloRootPassword = Defaults.ROOT_PASSWORD;

    double maxExecutorMemory = Defaults.MAX_EXECUTOR_MEM;
    double minExecutorMemory = Defaults.MIN_EXECUTOR_MEM;
    
    Map<ServerType, BaseProcessConfiguration> servers;
    
    
    public JsonBaseClusterConfiguration() {
        setDefaultServers();    
    }
    
    public double getMaxExecutorMemory() {
        return maxExecutorMemory;
    }

    public void setMaxExecutorMemory(double maxExecutorMemory) {
        this.maxExecutorMemory = maxExecutorMemory;
    }

    public double getMinExecutorMemory() {
        return minExecutorMemory;
    }

    public void setMinExecutorMemory(double minExecutorMemory) {
        this.minExecutorMemory = minExecutorMemory;
    }
      
    public void setServers(Map<ServerType, BaseProcessConfiguration> processors) {
       servers = processors;
    }
   
    public Map<ServerType, BaseProcessConfiguration> getServers() {
        return servers;
    }
    
    public int getMinTservers() {
        return minTservers;
    }

    public void setMinTservers(int minTservers) {
        this.minTservers = minTservers;
    }

    public String getAccumuloInstanceName() {
        return this.instanceName;
    }
    
    public void setAccumuloInstanceName(String name) {
        this.instanceName = name;
    }

    public String getBindAddress(){ 
        return this.bindAddress; 
    }
    
    public void setBindAddress(String address){
        this.bindAddress = address;
    }

    public int getHttpPort(){ 
        return httpPort; 
    }
    
    public void setHttpPort(int port){
        this.httpPort = port;
    }

    public String getMesosMaster() {
        return this.mesosMaster;
    }
    
    public void setMesosMaster(String master) {
        this.mesosMaster = master;
    }

    public String getFrameworkName() {
        return this.frameworkName;
    }
    
    public void setFrameworkName(String name) {
        this.frameworkName = name;
    }

    public String getZkServers() {
        return this.zkServers;
    }
    
    public void setZkServers(String servers) {
        this.zkServers = servers;
    }

    public String getTarballUri() {
        return this.tarballUri;
    }

    public void setTarballUri(String uriString) {
        this.tarballUri = uriString;
    }
    
    public String getAccumuloRootPassword() { 
        return this.accumuloRootPassword; 
    }
    
    public void setAccumuloRootPassword(String password) { 
        this.accumuloRootPassword = password;
    }

    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
    
    protected void setDefaultServers() {
        servers = new HashMap<ServerType, BaseProcessConfiguration>(5);
        servers.put(ServerType.MASTER, getDefaultMasterServer());
        servers.put(ServerType.TABLET_SERVER, getDefaultTabletServer());
        servers.put(ServerType.MONITOR, getDefaultMonitorServer());
        servers.put(ServerType.GARBAGE_COLLECTOR, getDefaultGCServer());
        servers.put(ServerType.TRACER, getDefaultTracerServer());
    }
    
    protected BaseProcessConfiguration getDefaultTracerServer() {
        // Make it the same as the monitor for now.
        return new BaseProcessConfiguration("" + Defaults.MIN_MONITOR_MEM,
                         Defaults.DEFAULT_1G_MAX_MEMORY, 
                         "" + Defaults.MIN_MONITOR_CPUS, 
                         ServerType.TRACER.getName());
     }   
    protected BaseProcessConfiguration getDefaultMasterServer() {
       return new BaseProcessConfiguration("" + Defaults.MIN_MASTER_MEM,
                        Defaults.DEFAULT_8G_MAX_MEMORY, 
                        "" + Defaults.MIN_MASTER_CPUS, 
                        ServerType.MASTER.getName());
    }
    
    protected BaseProcessConfiguration getDefaultTabletServer() {
        return new BaseProcessConfiguration("" + Defaults.MIN_TSERVER_MEM,
                        Defaults.DEFAULT_8G_MAX_MEMORY, 
                         "" + Defaults.MIN_TSERVER_CPUS, 
                         ServerType.TABLET_SERVER.getName());
    }   
    
    protected BaseProcessConfiguration getDefaultMonitorServer() {
        return new BaseProcessConfiguration(""+Defaults.MIN_MONITOR_MEM,
                        Defaults.DEFAULT_1G_MAX_MEMORY, 
                         ""+Defaults.MIN_MONITOR_CPUS, 
                         ServerType.MONITOR.getName());
    }      
    
    protected BaseProcessConfiguration getDefaultGCServer() {
        return new BaseProcessConfiguration(""+Defaults.MIN_GC_MEM,
                        Defaults.DEFAULT_2G_MAX_MEMORY, 
                         ""+Defaults.MIN_GC_CPUS, 
                         ServerType.GARBAGE_COLLECTOR.getName());
    }


    
    
}
