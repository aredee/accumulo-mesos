package aredee.mesos.frameworks.accumulo.configuration;


import java.util.ResourceBundle;

public final class Defaults {

    private static final ResourceBundle rb = ResourceBundle.getBundle("accumulo-mesos");

    // Default Port to bind http interface to
    public final static int HTTP_PORT = 18120;

    // Bind to all interfaces by default
    public final static String BIND_ADDRESS = "0.0.0.0";

    // Look for mesos master on local machine
    public final static String MESOS_MASTER = "10.0.2.15:5050";

    // Framework name
    public final static String FRAMEWORK_NAME = "Accumulo-Mesos";

    // Zookeeper server
    public final static String ZK_SERVERS = "127.0.0.1:2181";

    // Memory settings are MB
    public final static int MIN_MASTER_MEM = 512;
    public final static int MIN_TSERVER_MEM = 512;
    public final static int MIN_GC_MEM = 128;
    public final static int MIN_MONITOR_MEM = 128;
    
    public final static String DEFAULT_8G_MAX_MEMORY = "8192";
    public final static String DEFAULT_2G_MAX_MEMORY = "2048";
    public final static String DEFAULT_1G_MAX_MEMORY = "1024";

    // Minimum CPU settings
    public final static int MIN_MASTER_CPUS = 1;
    public final static int MIN_TSERVER_CPUS = 1;
    public final static int MIN_GC_CPUS = 1;
    public final static int MIN_MONITOR_CPUS = 1;

    // Minimum number of tservers to launch
    public final static int MIN_TSERVERS = 3;

    // Executor URI
    public final static String ROOT_USER = "root";
    public final static String ROOT_PASSWORD = "secret";

    public final static double MAX_EXECUTOR_MEM = 128;
    public final static double MIN_EXECUTOR_MEM = 16;
    public final static double EXECUTOR_CPUS = 0.2;
    
    // Default location of the accumulo-site.xml file
    public final static String ACCUMULO_SITE_URI = "file:./conf/default-accumulo-site.xml";
    
    
}
