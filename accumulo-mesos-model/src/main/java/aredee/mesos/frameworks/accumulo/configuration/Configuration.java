package aredee.mesos.frameworks.accumulo.configuration;

public interface Configuration {

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

    public String getAccumuloTarballUri();
    public void setAccumuloTarballUri(String uriString);

    public String getExecutorJarUri();
    public void setExecutorJarUri(String uriString);

    public double getMinMasterMem();
    public void setMinMasterMem(double minMasterMem);

    public double getMinTserverMem();
    public void setMinTserverMem(double minTserverMem);

    public double getMinGCMem();
    public void setMinGCMem(double minGCMem);

    public double getMinMonitorMem();
    public void setMinMonitorMem(double minMonitorMem);

    public double getMinMasterCpus();
    public void setMinMasterCpus(double minMasterCpus);

    public double getMinTserverCpus();
    public void setMinTserverCpus(double minTserverCpus);

    public double getMinGCCpus();
    public void setMinGCCpus(double minGCCpus);

    public double getMinMonitorCpus();
    public void setMinMonitorCpus(double minMonitorCpus);

    public int getMinTservers();
    public void setMinTservers(int minTservers);

}
