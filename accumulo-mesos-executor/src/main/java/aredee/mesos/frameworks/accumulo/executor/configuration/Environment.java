package aredee.mesos.frameworks.accumulo.executor.configuration;

/**
 * Environment Variables Required to be set before launching an Accumulo Server
 */
public class Environment {

    // required
    public static final String JAVA_HOME = "JAVA_HOME";
    public static final String ACCUMULO_HOME = aredee.mesos.frameworks.accumulo.configuration.Environment.ACCUMULO_HOME;
    public static final String HADOOP_PREFIX = aredee.mesos.frameworks.accumulo.configuration.Environment.HADOOP_PREFIX;
    public static final String ZOOKEEPER_HOME = aredee.mesos.frameworks.accumulo.configuration.Environment.ZOOKEEPER_HOME;
    public static final String ACCUMULO_LOG_DIR = aredee.mesos.frameworks.accumulo.configuration.Environment.ACCUMULO_LOG_DIR;

    public static final String[] REQUIRED_ENV = new String[]{JAVA_HOME,
                                                            ACCUMULO_HOME,
                                                            HADOOP_PREFIX,
                                                            ZOOKEEPER_HOME,
                                                            ACCUMULO_LOG_DIR};

    // optional
    public static final String ACCUMULO_XTRAJARS = "ACCUMULO_XTRAJARS";
    public static final String ACCUMULO_GENERAL_OPTS = "ACCUMULO_GENERAL_OPTS";
    public static final String ACCUMULO_JAAS_CONF = "ACCUMULO_JAAS_CONF"; //-Djava.security.auth.login.config=${ACCUMULO_JAAS_CONF}
    public static final String ACCUMULO_KRB5_CONF = "ACCUMULO_KRB5_CONF"; //-Djava.security.krb5.conf=${ACCUMULO_KRB5_CONF}

    public static final String ACCUMULO_MASTER_OPTS = "ACCUMULO_MASTER_OPTS";
    public static final String ACCUMULO_GC_OPTS = "ACCUMULO_GC_OPTS";
    public static final String ACCUMULO_TSERVER_OPTS = "ACCUMULO_TSERVER_OPTS";
    public static final String ACCUMULO_MONITOR_OPTS = "ACCUMULO_MONITOR_OPTS";
    public static final String ACCUMULO_LOGGER_OPTS = "ACCUMULO_LOGGER_OPTS";
    public static final String ACCUMULO_OTHER_OPTS = "ACCUMULO_OTHER_OPTS";
    public static final String ACCUMULO_KILL_CMD = "ACCUMULO_KILL_CMD";

    // generated
    public static final String START_JAR = "START_JAR";
    public static final String ACCUMULO_CONF_DIR = aredee.mesos.frameworks.accumulo.configuration.Environment.ACCUMULO_CONF_DIR;
    public static final String LIB_PATH = "LIB_PATH";
    public static final String LD_LIBRARY_PATH = aredee.mesos.frameworks.accumulo.configuration.Environment.LD_LIBRARY_PATH;
    public static final String DYLD_LIBRARY_PATH = aredee.mesos.frameworks.accumulo.configuration.Environment.DYLD_LIBRARY_PATH;
    /*
    if [ -e "${HADOOP_PREFIX}/lib/native/libhadoop.so" ]; then
            LIB_PATH="${HADOOP_PREFIX}/lib/native"
    LD_LIBRARY_PATH="${LIB_PATH}:${LD_LIBRARY_PATH}"     # For Linux
    DYLD_LIBRARY_PATH="${LIB_PATH}:${DYLD_LIBRARY_PATH}" # For Mac
    fi
    */
}
