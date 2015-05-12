package aredee.mesos.frameworks.accumulo.configuration;


public final class Defaults {

    // Default Port to bind http interface to
    public final static int HTTP_PORT = 18120;

    // Bind to all interfaces by default
    public final static String BIND_ADDRESS = "0.0.0.0";

    // Look for mesos master on local machine
    public final static String MESOS_MASTER = "127.0.0.1:5050";

    // Framework name
    public final static String FRAMEWORK_NAME = "Accumulo-Mesos";

    // Zookeeper server
    public final static String ZK_SERVERS = "127.0.0.1:2181";

    // Memory settings are MB
    public final static int MIN_MASTER_MEM = 4096;
    public final static int MIN_TSERVER_MEM = 4096;
    public final static int MIN_GC_MEM = 1024;
    public final static int MIN_MONITOR_MEM = 500;

    // Minimum CPU settings
    public final static int MIN_MASTER_CPUS = 4;
    public final static int MIN_TSERVER_CPUS = 4;
    public final static int MIN_GC_CPUS = 2;
    public final static int MIN_MONITOR_CPUS = 1;

    // Minimum number of tservers to launch
    public final static int MIN_TSERVERS = 10;

}
