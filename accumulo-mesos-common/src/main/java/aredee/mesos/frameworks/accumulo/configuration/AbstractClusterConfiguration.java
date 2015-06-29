package aredee.mesos.frameworks.accumulo.configuration;

public abstract class AbstractClusterConfiguration implements ClusterConfiguration {

    String bindAddress = Defaults.BIND_ADDRESS;
    int httpPort = Defaults.HTTP_PORT;
    String mesosMaster = Defaults.MESOS_MASTER;
    String frameworkName = Defaults.FRAMEWORK_NAME;
    String zkServers = Defaults.ZK_SERVERS;

    double minMasterMem = Defaults.MIN_MASTER_MEM;
    double minTserverMem = Defaults.MIN_TSERVER_MEM;
    double minGCMem = Defaults.MIN_GC_MEM;
    double minMonitorMem = Defaults.MIN_MONITOR_MEM;

    double minMasterCpus = Defaults.MIN_MASTER_CPUS;
    double minTserverCpus = Defaults.MIN_TSERVER_CPUS;
    double minGCCpus = Defaults.MIN_GC_CPUS;
    double minMonitorCpus = Defaults.MIN_MONITOR_CPUS;

    int minTservers = Defaults.MIN_TSERVERS;

    String instanceName = "default-instance";

    String executorJarUri = Defaults.EXECUTOR_JAR;
    String accumuloRootPassword = Defaults.ROOT_PASSWORD;

    double maxExecutorMemory = Defaults.MAX_EXECUTOR_MEM;
    double minExecutorMemory = Defaults.MIN_EXECUTOR_MEM;

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
    public double getMinMasterMem() {
        return minMasterMem;
    }

    @Override
    public void setMinMasterMem(double minMasterMem) {
        this.minMasterMem = minMasterMem;
    }

    @Override
    public double getMinTserverMem() {
        return minTserverMem;
    }

    @Override
    public void setMinTserverMem(double minTserverMem) {
        this.minTserverMem = minTserverMem;
    }

    @Override
    public double getMinGCMem() {
        return minGCMem;
    }

    @Override
    public void setMinGCMem(double minGCMem) {
        this.minGCMem = minGCMem;
    }

    @Override
    public double getMinMonitorMem() {
        return minMonitorMem;
    }

    @Override
    public void setMinMonitorMem(double minMonitorMem) {
        this.minMonitorMem = minMonitorMem;
    }

    @Override
    public double getMinMasterCpus() {
        return minMasterCpus;
    }

    @Override
    public void setMinMasterCpus(double minMasterCpus) {
        this.minMasterCpus = minMasterCpus;
    }

    @Override
    public double getMinTserverCpus() {
        return minTserverCpus;
    }

    @Override
    public void setMinTserverCpus(double minTserverCpus) {
        this.minTserverCpus = minTserverCpus;
    }

    @Override
    public double getMinGCCpus() {
        return minGCCpus;
    }

    @Override
    public void setMinGCCpus(double minGCCpus) {
        this.minGCCpus = minGCCpus;
    }

    @Override
    public double getMinMonitorCpus() {
        return minMonitorCpus;
    }

    @Override
    public void setMinMonitorCpus(double minMonitorCpus) {
        this.minMonitorCpus = minMonitorCpus;
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
    public String getBindAddress(){ return this.bindAddress; }
    @Override
    public void setBindAddress(String address){
        this.bindAddress = address;
    }

    @Override
    public int getHttpPort(){ return httpPort; }
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
        return null;
    }
    @Override
    public void setAccumuloTarballUri(String uriString) {

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
    public String getAccumuloRootPassword() { return this.accumuloRootPassword; }
    @Override
    public void setAccumuloRootPassword(String password) { this.accumuloRootPassword = password; }

}
