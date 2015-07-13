package aredee.mesos.frameworks.accumulo.configuration;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClusterConfiguration implements ClusterConfiguration {

    String bindAddress = Defaults.BIND_ADDRESS;
    int httpPort = Defaults.HTTP_PORT;
    String mesosMaster = Defaults.MESOS_MASTER;
    String frameworkName = Defaults.FRAMEWORK_NAME;
    String zkServers = Defaults.ZK_SERVERS;
    
    int minTservers = Defaults.MIN_TSERVERS;

    String instanceName = "default-instance";
    String accumuloTarBallUri = null;
    String executorJarUri = Defaults.EXECUTOR_JAR;
    String accumuloRootPassword = Defaults.ROOT_PASSWORD;

    double maxExecutorMemory = Defaults.MAX_EXECUTOR_MEM;
    double minExecutorMemory = Defaults.MIN_EXECUTOR_MEM;
    
    Map<ServerType, IProcessorConfiguration> servers;
    
    
    public AbstractClusterConfiguration() {
        setDefaultServers();    
    }
    
    @Override
    public double getMaxExecutorMemory() {
        return maxExecutorMemory;
    }

    @Override
    public void setMaxExecutorMemory(double maxExecutorMemory) {
        this.maxExecutorMemory = maxExecutorMemory;
    }

    @Override
    public double getMinExecutorMemory() {
        return minExecutorMemory;
    }

    @Override
    public void setMinExecutorMemory(double minExecutorMemory) {
        this.minExecutorMemory = minExecutorMemory;
    }
    
    @Override
    public void setProcessorConfigurations(Map<ServerType, IProcessorConfiguration> processors) {
        servers = processors;
    }
    
    @Override
    public Map<ServerType, IProcessorConfiguration> getProcessorConfigurations() {
        return servers;
    }
    
    @Override
    public int getMinTservers() {
        return minTservers;
    }

    @Override
    public void setMinTservers(int minTservers) {
        this.minTservers = minTservers;
    }

    @Override
    public String getAccumuloInstanceName() {
        return this.instanceName;
    }
    
    @Override
    public void setAccumuloInstanceName(String name) {
        this.instanceName = name;
    }

    @Override
    public String getBindAddress(){ 
        return this.bindAddress; 
    }
    
    @Override
    public void setBindAddress(String address){
        this.bindAddress = address;
    }

    @Override
    public int getHttpPort(){ 
        return httpPort; 
    }
    
    @Override
    public void setHttpPort(int port){
        this.httpPort = port;
    }

    @Override
    public String getMesosMaster() {
        return this.mesosMaster;
    }
    
    @Override
    public void setMesosMaster(String master) {
        this.mesosMaster = master;
    }

    @Override
    public String getFrameworkName() {
        return this.frameworkName;
    }
    
    @Override
    public void setFrameworkName(String name) {
        this.frameworkName = name;
    }

    @Override
    public String getZkServers() {
        return this.zkServers;
    }
    
    @Override
    public void setZkServers(String servers) {
        this.zkServers = servers;
    }

    // TODO implement below this line
    @Override
    public String getAccumuloTarballUri() {
        return accumuloTarBallUri;
    }
    
    @Override
    public void setAccumuloTarballUri(String uriString) {
        accumuloTarBallUri = uriString;
    }

    @Override
    public String getExecutorJarUri() {
        return this.executorJarUri;
    }
    
    @Override
    public void setExecutorJarUri(String uriString) {
        this.executorJarUri = uriString;
    }

    @Override
    public String getAccumuloRootPassword() { 
        return this.accumuloRootPassword; 
    }
    
    @Override
    public void setAccumuloRootPassword(String password) { 
        this.accumuloRootPassword = password;
    }

    protected void setDefaultServers() {
        servers = new HashMap<ServerType, IProcessorConfiguration>(5);
        servers.put(ServerType.MASTER, getDefaultMasterServer());
        servers.put(ServerType.TABLET_SERVER, getDefaultTabletServer());
        servers.put(ServerType.MONITOR, getDefaultMonitorServer());
        servers.put(ServerType.GARBAGE_COLLECTOR, getDefaultGCServer());
    }
    
    protected IProcessorConfiguration getDefaultMasterServer() {
       return new ProcessorConfiguration("" + Defaults.MIN_MASTER_MEM, 
                        null, 
                        "" + Defaults.MIN_MASTER_CPUS, 
                        ServerType.MASTER.getName());
    }
    
    protected IProcessorConfiguration getDefaultTabletServer() {
        return new ProcessorConfiguration("" + Defaults.MIN_TSERVER_MEM, 
                         null, 
                         "" + Defaults.MIN_TSERVER_CPUS, 
                         ServerType.TABLET_SERVER.getName());
    }   
    
    protected IProcessorConfiguration getDefaultMonitorServer() {
        return new ProcessorConfiguration(""+Defaults.MIN_MONITOR_MEM, 
                         null, 
                         ""+Defaults.MIN_MONITOR_CPUS, 
                         ServerType.MONITOR.getName());
    }      
    
    protected IProcessorConfiguration getDefaultGCServer() {
        return new ProcessorConfiguration(""+Defaults.MIN_GC_MEM, 
                         null, 
                         ""+Defaults.MIN_GC_CPUS, 
                         ServerType.GARBAGE_COLLECTOR.getName());
    }       
    
}
